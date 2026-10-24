package com.wms.service.common.service;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.common.core.domain.PageQuery;
import com.wms.common.core.domain.ProcessRowParams;
import com.wms.common.core.domain.model.LoginUser;
import com.wms.common.enums.FunctionEnum;
import com.wms.common.enums.OrderByEnum;
import com.wms.common.helper.LoginHelper;
import com.wms.common.rule.CompiledCondition;
import com.wms.common.rule.ConditionNode;
import com.wms.common.rule.Operator;
import com.wms.common.utils.ApplicationContextHolder;
import com.wms.common.utils.ListUtils;
import com.wms.common.utils.Predicates;
import com.wms.common.utils.StringUtils;
import com.wms.domain.SysFieldConfig;
import com.wms.mapper.CustomizeMapper;
import com.wms.page.DynamicsTableDataInfo;
import com.wms.service.common.service.plugin.PluginFacade;
import com.wms.service.common.spi.ISysMenuService;
import com.wms.service.common.spi.ISysUserService;
import com.wms.spi.ISysFieldConfigService;
import com.wms.sql.ConditionTreeCompiler;
import com.wms.sql.DataViewMetaData;
import com.wms.sql.Limit;
import com.wms.sql.Offset;
import com.wms.sql.OrderBy;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.wms.common.helper.LoginHelper.getLoginUser;

@Slf4j
@Service
public class DynamicsQueryService {
    public ISysMenuService getSysMenuService() {
        return ApplicationContextHolder.getBean(ISysMenuService.class);
    }
    @Resource
    private ISysUserService sysUserService;
    @Resource
    protected PluginFacade pluginFacade;

    /**
     * 动态列表并发查询线程池。
     * java -jar 启动时 ForkJoinPool.commonPool 的 contextClassLoader 是 AppClassLoader，
     * 无法加载 BOOT-INF/lib 中的类（数据权限拦截器反射加载 Mapper 类会抛 ClassNotFoundException），
     * 因此改用专用线程池并显式绑定当前类的类加载器。
     */
    private static final ExecutorService QUERY_EXECUTOR = new ThreadPoolExecutor(
        Runtime.getRuntime().availableProcessors() + 1,
        Runtime.getRuntime().availableProcessors() + 1,
        60L, TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(256),
        new ThreadFactory() {
            private final AtomicInteger seq = new AtomicInteger(1);
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "dyn-query-" + seq.getAndIncrement());
                t.setContextClassLoader(DynamicsQueryService.class.getClassLoader());
                t.setDaemon(true);
                return t;
            }
        },
        new ThreadPoolExecutor.CallerRunsPolicy()
    );

    /**
     * 单表动态查询
     *
     * @param supportType 功能类型
     * @param supplierMapper 数据访问器
     * @param cla 实体类
     * @param pageQuery 查询条件
     * @return 分页结果
     */
    @SneakyThrows
    public DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> queryTablePageList(
        FunctionEnum supportType,
        CustomizeMapper supplierMapper,
        Class<?> cla,
        PageQuery pageQuery
    ) {
        //表格头信息
        List<SysFieldConfig> headers = getSysFieldConfigs(supportType, pageQuery);
        List<SqlManager.OrderBy> orderByFields = ListUtils.select(headers, item -> StringUtils.isNotEmpty(item.getSort()) && Predicates.isNotNull(item.getOrderByIndex())).collect(item -> new SqlManager.OrderBy(item.getColumnName(), item.getOrderByIndex(), OrderByEnum.valueOf(item.getSort())));
        for (SysFieldConfig sysFieldConfig : headers) {
            sysFieldConfig.setColumnName(sysFieldConfig.getColumnName().replace("`", ""));
        }
        Collections.sort(orderByFields, Comparator.comparingInt(SqlManager.OrderBy::getSortIndex).thenComparing(SqlManager.OrderBy::getField));
        //表头排序
        Collections.sort(headers, Comparator.comparingInt(SysFieldConfig::getSortIndex));

        Integer offset = pageQuery.fetchOffset();
        Integer limit = pageQuery.getPageSize();
        String tableName = TableInfoHelper.getTableInfo(cla).getTableName();

        List<SysFieldConfig> queryFields = getSysSearchFieldConfigs(supportType, pageQuery);
        Map<String, SysFieldConfig> column_config_map = ListUtils.toMap(queryFields, SysFieldConfig::getColumnName, c -> c);
        //动态查询条件（条件树 DSL 优先，FAST/ADVANCE 兼容）
        WhereWithParams where = fetchWhere(pageQuery, column_config_map);
        String whereClause = where.getClause();

        //数据权限
        String dataPermissions = appendDataPermissions(supportType);
        if (StringUtils.isEmpty(whereClause)) {
            whereClause = dataPermissions;
        }
        if (StringUtils.isNotEmpty(whereClause) && StringUtils.isNotEmpty(dataPermissions)) {
            whereClause = whereClause + " and " + dataPermissions;
        }

        // 实施友好（逻辑列自动解析）：sql_column 留空或填简单列名（驼峰/下划线均可），
        // 引擎自动在主表解析并限定表名；表名限定/CASE/子查询等高级写法原样兼容
        TableInfo claTableInfo = TableInfoHelper.getTableInfo(cla);
        List<SysFieldConfig> selectConfigs = StringUtils.isNotEmpty(pageQuery.getDialogName())
            ? ApplicationContextHolder.getBean(ISysFieldConfigService.class).listDynamicField(FunctionEnum.valueOf(pageQuery.getDialogName()))
            : ApplicationContextHolder.getBean(ISysFieldConfigService.class).listDynamicField(supportType);
        List<String> selectColumns = resolveSelectColumns(selectConfigs, tableName, claTableInfo);
        String sql = SqlManager.builder().tableName(tableName).fields(selectColumns).orderBy(orderByFields).limit(limit).offset(offset).where(whereClause).build().generateSql();
        String countSql = SqlManager.builder().tableName(tableName).where(whereClause).build().generateCountSql();

        List<Object> whereParams = where.getParams();
        CompletableFuture[] futures = new CompletableFuture[2];

        futures[0] = CompletableFuture.supplyAsync(() -> supplierMapper.queryList(sql, toCtParamMap(whereParams)), QUERY_EXECUTOR);
        futures[1] = CompletableFuture.supplyAsync(() -> supplierMapper.count(countSql, toCtParamMap(whereParams)), QUERY_EXECUTOR);

        // 等待所有的CompletableFuture完成
        CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(futures);
        combinedFuture.get();

        List<Map<String, Object>> rows = (List<Map<String, Object>>) futures[0].join();
        long count = (long) futures[1].join();

        Map<String, SysFieldConfig> dynamics_column_config_map = ListUtils.toMap(headers, SysFieldConfig::getColumnName, c -> c);
        // 数据库查询参数
        Long userId = LoginHelper.getUserId();
        if (userId != null) {
            int isListMergeCode = sysUserService.getIsListMergeCodeByUserId(userId);
            ProcessRowParams processRowParams = new ProcessRowParams();
            processRowParams.setIsListMergeCode(isListMergeCode);
            if (pageQuery.getProcessRowParams() == null) {
                pageQuery.setProcessRowParams(processRowParams);
            }
        }
        //map排序
        List<TreeMap<String, Object>> treeMaps = processRow(rows, dynamics_column_config_map, pageQuery.getProcessRowParams());

        DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> result = new DynamicsTableDataInfo<>(headers, treeMaps, count);
        result.setSumRow(pluginFacade.sumRow(rows, dynamics_column_config_map));
        return result;
    }

    /**
     * 数据权限
     *
     * @param supportType 功能类型
     * @return 数据权限SQL
     */
    private String appendDataPermissions(FunctionEnum supportType) {
        List<String> dataPermissions = getSysMenuService().fetchDataPermissions(supportType);
        if (ListUtils.isNotEmpty(dataPermissions)) {

            List<String> temp = ListUtils.empty();
            for (String permission : dataPermissions) {
                if (StringUtils.isNotEmpty(permission)) {
                    LoginUser loginUser = getLoginUser();
                    if (loginUser == null) {
                        throw new RuntimeException("未登陆");
                    }
                    permission = permission.replace("{empId}", String.valueOf(loginUser.getEmployeeId()));
                    permission = permission.replace("{userId}", String.valueOf(loginUser.getUserId()));
                    temp.add(String.format("(%s)", permission));
                }
            }
            if (ListUtils.isNotEmpty(temp)) {
                String dataPermissionsSql = ListUtils.makeString(temp, " or ");
                return String.format(" (%s)", dataPermissionsSql);
            }
        }
        return "";
    }

    /**
     * 行格式化、状态枚举值处理
     *
     * @param rows             表格行
     * @param columnConfigMap  列名称与字段配置的映射
     * @param processRowParams processRowParams 行处理参数
     * @return 转换后的行
     */
    private List<TreeMap<String, Object>> processRow(List<Map<String, Object>> rows, Map<String, SysFieldConfig> columnConfigMap, ProcessRowParams processRowParams) {
        List<TreeMap<String, Object>> treeMaps = ListUtils.empty();

        Set<String> codeSet = new HashSet<>();
        for (Map<String, Object> entries : rows) {
            Object diagId = entries.get("id");
            log.info("DIAG rows id type={}, value={}", diagId == null ? "null" : diagId.getClass().getName(), diagId);
            if (processRowParams != null && processRowParams.isListMergeCode == 1) {
                //相同code只保留第一个
                if (entries.get("code") != null && !codeSet.add(entries.get("code").toString())) {
                    entries.put("code", "");
                }
            }
            // 使用插件服务处理整个数据行
            TreeMap<String, Object> treeMap = pluginFacade.processRow(entries, columnConfigMap);
            //当前页面上的所有行
            treeMaps.add(treeMap);
        }
        return treeMaps;
    }

    /**
     * 多表的动态查询
     *
     * @param supportType 功能类型
     * @param supplierMapper 数据访问器
     * @param dataViewMetaData 数据视图
     * @param pageQuery 前端传的查询条件
     * @return 分页列表
     */
    @SneakyThrows
    public DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> queryTablePageList(
        FunctionEnum supportType,
        CustomizeMapper supplierMapper,
        DataViewMetaData dataViewMetaData,
        PageQuery pageQuery
    ) {
        List<SysFieldConfig> headers = getSysFieldConfigs(supportType, pageQuery);
        List<OrderBy.OrderByOption> orderByFields = ListUtils.select(headers, item -> StringUtils.isNotEmpty(item.getSort()) && Predicates.isNotNull(item.getOrderByIndex())).collect(item -> new OrderBy.OrderByOption(item.getColumnName(), item.getOrderByIndex(), OrderByEnum.valueOf(item.getSort())));
        for (SysFieldConfig sysFieldConfig : headers) {
            sysFieldConfig.setColumnName(sysFieldConfig.getColumnName().replace("`", ""));
        }
        Collections.sort(orderByFields, Comparator.comparingInt(OrderBy.OrderByOption::getSortIndex).thenComparing(OrderBy.OrderByOption::getField));
        //表头排序
        Collections.sort(headers, Comparator.comparingInt(SysFieldConfig::getSortIndex));

        Map<String, SysFieldConfig> column_config_map = ListUtils.toMap(headers, SysFieldConfig::getColumnName, c -> c);
        Integer offset = pageQuery.fetchOffset();
        Integer limit = pageQuery.getPageSize();
        //动态查询条件（条件树 DSL 优先，FAST/ADVANCE 兼容）
        WhereWithParams where = fetchWhere(pageQuery, column_config_map);
        String whereClause = where.getClause();

        //数据权限
        String dataPermissions = appendDataPermissions(supportType);
        if (StringUtils.isEmpty(whereClause)) {
            whereClause = dataPermissions;
        }
        if (StringUtils.isNotEmpty(whereClause) && StringUtils.isNotEmpty(dataPermissions)) {
            whereClause = whereClause + " and " + dataPermissions;
        }

        //排序
        OrderBy orderBy = new OrderBy();
        orderBy.setOrderByOption(orderByFields);

        //分页-页显示数量
        Limit limitWrapper = new Limit();
        limitWrapper.setLimit(limit);

        //分页-偏移量
        Offset offsetWrapper = new Offset();
        offsetWrapper.setOffset(offset);

        // 实施友好（逻辑列自动解析）：重写简单列为主表限定形式（详见 resolveSelectColumns），
        // 并兜底附加主键 id（WMS 动态表主键列统一为 id），保证前端行级操作可取主键
        TableInfo dvTableInfo = TableInfoHelper.getTableInfo(dataViewMetaData.fetchMainEntity());
        List<String> dvColumns = dataViewMetaData.fetchColumns();
        for (int i = 0; i < dvColumns.size(); i++) {
            dvColumns.set(i, resolveSelectColumn(dvColumns.get(i), dataViewMetaData.fetchMainTableName(), dvTableInfo));
        }
        if (dvColumns.stream().noneMatch(DynamicsQueryService::hasIdAlias)) {
            dvColumns.add(dvColumns.size(), dataViewMetaData.fetchMainTableName() + ".id");
        }
        //查询列表的sql
        String sql = dataViewMetaData.where(whereClause).orderBy(orderBy).limit(limitWrapper).offset(offsetWrapper).genSqlExpr();
        String countSql = dataViewMetaData.count().where(whereClause).genCountSqlExpr();

        List<Object> whereParams = where.getParams();
        CompletableFuture[] futures = new CompletableFuture[2];
        futures[0] = CompletableFuture.supplyAsync(() -> supplierMapper.queryList(sql, toCtParamMap(whereParams)), QUERY_EXECUTOR);
        futures[1] = CompletableFuture.supplyAsync(() -> supplierMapper.count(countSql, toCtParamMap(whereParams)), QUERY_EXECUTOR);

        // 等待所有的CompletableFuture完成
        CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(futures);
        combinedFuture.get(); // 等待所有任务完成

        List<Map<String, Object>> rows = (List<Map<String, Object>>) futures[0].join();
        long count = (long) futures[1].join();
        // 数据库查询参数
        Long userId = LoginHelper.getUserId();
        if (userId != null) {
            int isListMergeCode = sysUserService.getIsListMergeCodeByUserId(userId);
            ProcessRowParams processRowParams = new ProcessRowParams();
            processRowParams.setIsListMergeCode(isListMergeCode);
            if (pageQuery.getProcessRowParams() == null) {
                pageQuery.setProcessRowParams(processRowParams);
            }
        }
        //map排序
        List<TreeMap<String, Object>> treeMaps = processRow(rows, column_config_map, pageQuery.getProcessRowParams());

        DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> result = new DynamicsTableDataInfo<>(headers, treeMaps, count);
        result.setSumRow(pluginFacade.sumRow(rows, column_config_map));
        return result;
    }

    /**
     * 配置的动态列字段
     *
     * @param supportType 功能类型
     * @param pageQuery 查询条件
     * @return 字段配置列表
     */
    protected List<SysFieldConfig> getSysFieldConfigs(FunctionEnum supportType, PageQuery pageQuery) {
        if (StringUtils.isNotEmpty(pageQuery.getDialogName())) {
            return ApplicationContextHolder.getBean(ISysFieldConfigService.class).listDynamicField(FunctionEnum.valueOf(pageQuery.getDialogName()));
        }
        return ApplicationContextHolder.getBean(ISysFieldConfigService.class).listDynamicField(supportType);
    }

    /**
     * 配置的查询列字段
     *
     * @param supportType 功能类型
     * @param pageQuery 查询条件
     * @return 查询字段配置列表
     */
    protected List<SysFieldConfig> getSysSearchFieldConfigs(FunctionEnum supportType, PageQuery pageQuery) {
        if (StringUtils.isNotEmpty(pageQuery.getDialogName())) {
            return ApplicationContextHolder.getBean(ISysFieldConfigService.class).listSearchField(FunctionEnum.valueOf(pageQuery.getDialogName()));
        }
        return ApplicationContextHolder.getBean(ISysFieldConfigService.class).listSearchField(supportType);
    }


    /**
     * 拼接where语句
     *
     * @param pageQuery
     * @param column_config_map
     * @return
     */
    private static String fetchDynamicsWhere(PageQuery pageQuery, Map<String, SysFieldConfig> column_config_map) {
        List<PageQuery.ConditionItem> conditionItems = pageQuery.getConditions();
        List<String> fastDynamicsWhere = ListUtils.empty();
        List<String> advanceDynamicsWhere = ListUtils.empty();

        //快捷过滤
        List<PageQuery.ConditionItem> fastFilters = ListUtils.select(conditionItems, item -> item.getFilterGroup() == PageQuery.FilterGroup.FAST);
        //高级过滤
        List<PageQuery.ConditionItem> advanceFilters = ListUtils.select(conditionItems, item -> item.getFilterGroup() == PageQuery.FilterGroup.ADVANCE);

        extracted(column_config_map, fastFilters, fastDynamicsWhere);
        extracted(column_config_map, advanceFilters, advanceDynamicsWhere);

        if (ListUtils.isEmpty(fastFilters) && ListUtils.isEmpty(advanceFilters)) {
            extracted(column_config_map, conditionItems, fastDynamicsWhere);
        }

        return ListUtils.makeStringSkipNull(" and ", ListUtils.isEmpty(fastDynamicsWhere) ? null : "(" + ListUtils.makeString(fastDynamicsWhere, "") + ")", ListUtils.isEmpty(advanceDynamicsWhere) ? null : "(" + ListUtils.makeString(advanceDynamicsWhere, "") + ")");
    }

    /**
     * 拼接 where：条件树 DSL（参数化，三层防线）优先；否则走 FAST/ADVANCE 扁平条件（兼容，原链路签名不变）
     *
     * @param pageQuery 查询条件
     * @param column_config_map 字段白名单（columnName → 配置）
     * @return where 片段 + 绑定参数
     */
    private WhereWithParams fetchWhere(PageQuery pageQuery, Map<String, SysFieldConfig> column_config_map) {
        ConditionNode conditionTree = pageQuery.getConditionTree();
        if (conditionTree != null) {
            CompiledCondition compiled = ConditionTreeCompiler.compile(conditionTree, column_config_map);
            if (StringUtils.isEmpty(compiled.getFragment())) {
                return new WhereWithParams("", new ArrayList<>());
            }
            // ? → #{ctP.pN}（Map 键跳转绑定），交 @Select("${sql}") 的 SqlSourceBuilder 二次解析
            String clause = String.format("(%s)", compiled.toMybatisFragment("ctP"));
            return new WhereWithParams(clause, compiled.getParams());
        }
        String dynamicsWhere = fetchDynamicsWhere(pageQuery, column_config_map);
        return new WhereWithParams(StringUtils.isNotEmpty(dynamicsWhere) ? String.format("(%s)", dynamicsWhere) : "", new ArrayList<>());
    }

    /**
     * where 片段 + 绑定参数载体（条件树参数化链路使用；数据权限等会话值仍走原文本拼接）
     */
    private static class WhereWithParams {
        private final String clause;
        private final List<Object> params;

        WhereWithParams(String clause, List<Object> params) {
            this.clause = clause;
            this.params = params;
        }

        String getClause() {
            return clause;
        }

        List<Object> getParams() {
            return params;
        }
    }

    /**
     * 条件树绑定参数列表 → MyBatis 绑定 Map（p0 → v0 ...），配合 CustomizeMapper 双参重载 #{ctP.pN}；
     * FAST/ADVANCE 旧链路参数为空列表，转换结果为空 Map，SQL 中无占位符，行为不变。
     */
    private static Map<String, Object> toCtParamMap(List<Object> params) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < params.size(); i++) {
            map.put("p" + i, params.get(i));
        }
        return map;
    }

    private static void extracted(Map<String, SysFieldConfig> column_config_map, List<PageQuery.ConditionItem> filters, List<String> dynamicsWhere) {
        for (PageQuery.ConditionItem item : filters) {

            String field = item.getField();
            SysFieldConfig sysFieldConfig = column_config_map.get(field);
            if (sysFieldConfig == null) {
                continue;
            }

            String value = item.getValue();
            if (StringUtils.isEmpty(value) && (item.getOperate() != Operator.IS_NULL && item.getOperate() != Operator.IS_NOT_NULL)) {
                continue;
            }

            String logicalOperator = item.getLogicalOperator().getSymbol();
            if (ListUtils.isEmpty(dynamicsWhere)) {
                logicalOperator = "";
            }

            if (StringUtils.isNotEmpty(sysFieldConfig.getSqlQuery())) {
                String query = sysFieldConfig.getSqlQuery().replace("{value}", value);
                String where = String.format("%s (%s in (%s))", logicalOperator, sysFieldConfig.getSqlColumn(), query);
                dynamicsWhere.add(where);
            } else {
                String statement = String.format("%s %s", logicalOperator, item.fetchStatement(sysFieldConfig.getSqlColumn()));
                dynamicsWhere.add(statement);
            }
        }
    }

    /**
     * 获取查询列
     *
     * @param supportType 功能类型
     * @param pageQuery 查询条件
     * @return 列名列表
     */
    public List<String> getColumns(FunctionEnum supportType, PageQuery pageQuery) {
        if (StringUtils.isNotEmpty(pageQuery.getDialogName())) {
            return ApplicationContextHolder.getBean(ISysFieldConfigService.class).getWrapperColumns(FunctionEnum.valueOf(pageQuery.getDialogName()));
        }
        return ApplicationContextHolder.getBean(ISysFieldConfigService.class).getWrapperColumns(supportType);
    }

    /**
     * 逻辑列自动解析（实施友好）：
     * 简单列（sql_column 留空、或为不含 . / 空格 / 括号的标识符）→ 驼峰转下划线后在主表实体中解析，
     * 命中则输出 主表.列（避免 JOIN 场景歧义）；未命中保持原样（兼容 DataView 自带 join 别名列）。
     * 高级表达式（表名限定/CASE/子查询等，含 . 或空格或括号）原样兼容。
     */
    private List<String> resolveSelectColumns(List<SysFieldConfig> configs, String mainTableName, TableInfo mainTableInfo) {
        List<String> columns = ListUtils.empty();
        if (ListUtils.isEmpty(configs)) {
            return columns;
        }
        for (SysFieldConfig config : configs) {
            columns.add(resolveSelectColumn(
                StringUtils.isEmpty(config.getSqlColumn()) ? "" : "(" + config.getSqlColumn() + ") " + config.getColumnName(),
                mainTableName, mainTableInfo));
        }
        return columns;
    }

    /**
     * 解析单个列表达式（格式 "(expr) alias" 或裸列）：
     * expr 为简单标识符且其下划线形式存在于主表 → 重写为 (主表.列) alias；否则原样返回
     */
    private static String resolveSelectColumn(String wrapperExpr, String mainTableName, TableInfo mainTableInfo) {
        if (wrapperExpr == null) {
            return wrapperExpr;
        }
        java.util.regex.Matcher m = java.util.regex.Pattern
            .compile("^\\(([^()]*)\\)\\s*(\\S+)\\s*$").matcher(wrapperExpr.trim());
        if (!m.matches()) {
            return wrapperExpr;
        }
        String expr = m.group(1).trim();
        String alias = m.group(2);
        // sql_column 留空或为字面量 "null"（管理页空值经 %s 格式化的产物）：
        // 按列名（驼峰转下划线）在主表解析；主表无该列时输出 NULL（配置缺失不中断列表）
        if (expr.isEmpty() || expr.equalsIgnoreCase("null")) {
            String byAlias = StringUtils.toUnderScoreCase(alias);
            if (mainTableInfo != null && hasColumn(mainTableInfo, byAlias)) {
                return "(" + mainTableName + "." + byAlias + ") " + alias;
            }
            return "(NULL) " + alias;
        }
        // 简单标识符（无 . 空格 括号）才做主表解析
        if (!expr.matches("[A-Za-z0-9_]+")) {
            return wrapperExpr;
        }
        String column = StringUtils.toUnderScoreCase(expr);
        if (mainTableInfo != null && hasColumn(mainTableInfo, column)) {
            return "(" + mainTableName + "." + column + ") " + alias;
        }
        return "(" + expr + ") " + alias;
    }

    /** 主表是否含指定列（含主键列） */
    private static boolean hasColumn(TableInfo tableInfo, String column) {
        if (column.equalsIgnoreCase(tableInfo.getKeyColumn())) {
            return true;
        }
        return tableInfo.getFieldList().stream().anyMatch(f -> column.equalsIgnoreCase(f.getColumn()));
    }

    /**
     * 判断 SELECT 列（形如 "(t.col) alias" 或 "col alias" 或 "col"）的别名是否为主键 id
     */
    private static boolean hasIdAlias(String columnExpr) {
        if (StringUtils.isEmpty(columnExpr)) {
            return false;
        }
        String expr = columnExpr.trim();
        int sp = expr.lastIndexOf(' ');
        String alias = sp > -1 ? expr.substring(sp + 1).trim() : expr;
        return "id".equalsIgnoreCase(alias) || alias.toLowerCase().endsWith(".id");
    }
}
