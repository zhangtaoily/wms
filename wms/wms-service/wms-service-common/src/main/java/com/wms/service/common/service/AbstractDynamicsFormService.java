package com.wms.service.common.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.Sequence;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.wms.common.api.BasicBO;
import com.wms.common.constant.SqlConstant;
import com.wms.common.core.domain.*;
import com.wms.common.core.domain.event.OperLogEvent;
import com.wms.common.core.domain.model.LoginUser;
import com.wms.common.enums.*;
import com.wms.common.enums.dicts.WorkTimingEnum;
import com.wms.common.helper.LoginHelper;
import com.wms.common.thread.ThreadPool;
import com.wms.common.utils.*;
import com.wms.common.utils.spring.SpringUtils;
import com.wms.domain.SysFieldConfig;
import com.wms.domain.WfConfig;
import com.wms.domain.vo.PushDownVo;
import com.wms.domain.system.SysAutoCodeSet;
import com.wms.enums.CancelOaStatusEnum;
import com.wms.enums.CancelStatusEnum;
import com.wms.enums.CloseStatusEnum;
import com.wms.mapper.CustomizeMapper;
import com.wms.mapper.WfConfigMapper;
import com.wms.page.DynamicsTableDataInfo;
import com.wms.service.common.constant.FormConstants;
import com.wms.service.common.meta.LambdaFieldUtils;
import com.wms.service.common.spi.*;
import com.wms.sql.DataViewMetaData;
import com.wms.vo.BatchCancelVo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.logging.log4j.util.Strings;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.baomidou.mybatisplus.core.toolkit.Wrappers.query;
import static com.wms.common.helper.LoginHelper.getLoginUser;
import static com.wms.common.helper.LoginHelper.isAdmin;
import static com.wms.common.utils.ObjectUtil.convertToLong;

/**
 * 抽象通用的动态表单服务
 *
 * @param <E> 实体类型
 * @param <M> Mapper类型
 */
@Slf4j
public abstract class AbstractDynamicsFormService<E extends IdEntity, M extends CustomizeMapper<E>>
    extends AbstractCrudService<E, M> implements ICommonFormService,IFormContext {

    public static final Sequence sequence = new Sequence(NetUtil.getLocalhost());

    protected volatile TableNode tableNode;

    @Resource
    private ISysUserService sysUserService;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    protected ICallBack callBack;
    @Resource
    protected DynamicsQueryService dynamicsQueryService;
    @Resource(name = "commonWorkFlowService")
    protected WorkFlowService workFlowService;

    public abstract M supplierMapper();

    public abstract TableNode doFetchTableNode();

    public synchronized TableNode fetchTableNode() {
        if (tableNode == null) {
            tableNode = doFetchTableNode();
        }
        return tableNode;
    }

    public Tuple2<Integer, String> checkSave(BasicBO bo, Map<String, List<Map<String, Object>>> oldTableEntityMap, Map<String, List<Map<String, Object>>> newTableEntityMap) {
        return Tuple2.of(1, null);
    }

    public void afterSave(TableNode root, Map<String, List<Map<String, Object>>> oldTableEntityMap, Map<String, List<Object>> tableEntityMap) {
    }

    @Override
    public Tuple2<Integer, String> beforeSubmit(Long id, BasicBO bo) {
        Tuple2<Object, Object> tuple2 = getStatusFieldValue(fetchTableNode(), id);
        if (BillStatusEnum.DRAFT.getCode().equals(tuple2.getSecond()) ||
            BillStatusEnum.AUDIT.getCode().equals(tuple2.getSecond()) ||
            BillStatusEnum.PASS.getCode().equals(tuple2.getSecond()) ||
            BillStatusEnum.REJECT.getCode().equals(tuple2.getSecond())) {
            return Tuple2.of(0, "不能提交草稿、审核中、通过、驳回的数据");
        }

        return Tuple2.of(1, null);
    }

    @Override
    public void afterSubmit(Long id) {
        getWfConfigService().createWf(getSupportType(), id, () -> getSourceId(get(id)), requestId -> updateRequestId(id, requestId));
    }

    private Long getSourceId(IdEntity entity) {
        return null;
    }

    public void updateRequestId(Long id, Long requestId) {
    }

    public void beforeClose() {
    }

    public void afterClose() {
    }

    public Tuple2<Boolean, Object> beforeCopy(Long id, BasicBO bo) {
        return Tuple2.of(true, null);
    }

    public void afterCopy(Long id, BasicBO bo, Map<String, Object> viewMap) {
    }

    public void afterAssociationCopy(Long id, BasicBO bo, Map<String, Object> viewMap) {
    }

    public Tuple2<Boolean, Object> beforeInit(Long id) {
        return Tuple2.of(true, null);
    }

    public void afterInit(Long id, Map<String, Object> viewMap) {
    }

    public Tuple2<Boolean, String> checkDelete(Long id) {
        return Tuple2.of(true, null);
    }

    public Tuple2<Boolean, String> checkBatchDelete(Long[] id) {
        return Tuple2.of(true, null);
    }

    public void beforeDelete(Long id) {
    }

    public void afterDelete(Long id) {
    }

    public void afterDelete(Long[] id) {
    }

    public void beforePass(Long id, TableNode root, Map<String, List<Map<String, Object>>> dbOldData) {
        try {
            IInterfaceHelper _interfaceHelp = getInterfaceHelper();
            _interfaceHelp.setAPIInfo(dbOldData, root, id, 2);
        } catch (Exception ex) {
        }
    }

    public void afterRejectStateChanged(Long id, TableNode root, Map<String, Object> bo) {
    }

    public void afterPassStateChanged(Long id, TableNode root, Map<String, Object> bo) {
    }

    public void afterPass(Long id, TableNode root, Map<String, List<Map<String, Object>>> dbOldData, Map<String, List<Map<String, Object>>> dbNewData) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // 主事务提交后执行异步任务
                CompletableFuture.runAsync(() -> {
                    try {
                        IInterfaceHelper _interfaceHelp = getInterfaceHelper();
                        _interfaceHelp.setAPIInfo(dbNewData, root, id, 1);
                    } catch (Exception ex) {
                        log.error("异步操作异常", ex);
                    }
                }, ThreadPool.fiveThreadExecutor);
            }
        });
    }

    public void afterReject(Long id) {
    }

    public Tuple2<Boolean, String> checkRollBack(Long id) {
        //当前是通过状态的单据，如果有最新执行中的分发日志。则不能撤回。
        if (BillStatusEnum.PASS.getCode().equals(getStatusFieldValue(fetchTableNode(), id).getSecond())) {
            //通过后 有没有 执行中的分发日志
            Boolean hasInProgressLog = ApplicationContextHolder.getBean(ISysInterfaceLogService.class).hasInProgressLog(id);
            if (hasInProgressLog) {
                return Tuple2.of(false, "单据有执行中的分发日志，不能撤回");
            }
            return Tuple2.of(true, "撤回成功");
        }

        return Tuple2.of(true, "校验成功");
    }

    public Tuple2<Boolean, String> writeBack(TableNode root, FunctionEnum functionEnum, Map<String, List<Map<String, Object>>> oldTableEntityMap, Map<String, List<Map<String, Object>>> newTableEntityMap, BehaviorEnum behaviorEnum) {
        return Tuple2.of(true, "");
    }

    public Tuple2<Boolean, String> asyncWriteBack(TableNode root, FunctionEnum functionEnum, Map<String, List<Map<String, Object>>> oldTableEntityMap, Map<String, List<Map<String, Object>>> newTableEntityMap) {
        Long pactId = Long.parseLong(
            ListUtils.safeGetFirst(newTableEntityMap.get(fetchTableNode().getTableName())).get("id").toString()
        );

        // 反写操作改为异步（新增以下代码）
        // 注册事务同步回调
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // 主事务提交后执行异步任务
                CompletableFuture.runAsync(() -> {
                    try {
                        // 新建事务（使用REQUIRES_NEW传播行为）
                        transactionTemplate.execute(status -> {
                            Tuple2<Boolean, String> writeBackResult =
                                callBack.CallBackCommon(functionEnum, oldTableEntityMap, newTableEntityMap, false);
                            if (!writeBackResult.isSuccess()) {
                                log.info("反写失败{},单据类型{}", pactId, getSupportType());
                                return false;
                            }
                            return true;
                        });
                    } catch (Exception e) {
                        log.error("异步反写任务执行失败", e);
                    }
                }, ThreadPool.oneThreadExecutor);
            }
        });


        return Tuple2.of(true, "提交反写成功");
    }

    public Tuple2<Boolean, String> checkCommon(FunctionEnum functionEnum, Map<String, List<Map<String, Object>>> oldTableEntityMap, Map<String, List<Map<String, Object>>> newTableEntityMap) {
        return Tuple2.of(true, "");
    }

    public Tuple2<Boolean, String> beforeRollBack(Long id, TableNode root, Map<String, List<Map<String, Object>>> dbData) {
        try {
            IInterfaceHelper _interfaceHelp = getInterfaceHelper();
            return _interfaceHelp.setAPIInfo(dbData, root, id, 3);
        } catch (Exception ex) {
            return Tuple2.of(false, ex.getMessage());
        }
    }

    public Tuple2<Boolean, String> afterRollBack(Long id, TableNode root, Map<String, List<Map<String, Object>>> dbData) {
        // CRM 版曾按 sal_* 表回置取消/关闭状态，WMS 单据撤回由状态机（BillStatusEnum）处理
        return Tuple2.of(true, null);
    }

    /**
     * 单表动态查询
     *
     * @param cla
     * @param pageQuery
     * @return
     */
    @SneakyThrows
    public DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> queryTablePageList(Class<?> cla, PageQuery pageQuery) {
        return dynamicsQueryService.queryTablePageList(getSupportType(), supplierMapper(), cla, pageQuery);
    }

    /**
     * 多表的动态查询
     *
     * @param dataViewMetaData 数据视图
     * @param pageQuery 前端传的查询条件
     * @return 分页列表
     */
    @SneakyThrows
    public DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> queryTablePageList(DataViewMetaData dataViewMetaData,
                                                                                             PageQuery pageQuery) {
       return dynamicsQueryService.queryTablePageList(getSupportType(), supplierMapper(), dataViewMetaData, pageQuery);
    }

    /**
     * 根据主表id加载表单数据
     *
     * @param id 主表ID
     * @return 表和行数据的Map
     */
    public Map<String, List<Map<String, Object>>> doLoad(Long id) {
        Map<String, List<Map<String, Object>>> resultMap = MapUtils.empty();
        TableNode root = fetchTableNode();

        // 批量预加载所有表结构
        List<TableNode> allNodes = root.tree2List();
        Map<String, TableNode> nodeMap = ListUtils.toMap(allNodes, TableNode::getTableName, n -> n);

        // 主表查询
        loadMainTableData(id, root, resultMap);

        // 批量加载所有子表数据（使用广度优先遍历）
        Deque<TableNode> queue = new ArrayDeque<>(root.getChildren());
        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            Map<TableNode, Set<Long>> parentIdsMap = new HashMap<>();

            // 收集当前层级所有父ID
            for (int i = 0; i < levelSize; i++) {
                TableNode node = queue.poll();
                TableNode parentNode = nodeMap.get(node.getParentTable());
                List<Map<String, Object>> parentData = new ArrayList<>();
                if (resultMap.get(parentNode.getTableName()) != null) {
                    parentData = resultMap.get(parentNode.getTableName());
                }
                Set<Long> parentIds = parentData.stream()
                    .map(m -> convertToLong(m.get(parentNode.getPrimaryKey())))
                    .collect(Collectors.toSet());
                parentIdsMap.put(node, parentIds);

                queue.addAll(node.getChildren());
            }

            // 批量查询当前层级所有子表数据
            batchLoadChildData(parentIdsMap, resultMap, nodeMap);
        }

        // 统一处理BIGDECIMAL格式
        processBigDecimal(allNodes, resultMap);
        return resultMap;
    }


    private void recursive(Long id, TableNode tree, Map<String, List<Map<String, Object>>> resultMap) {
        for (TableNode child : tree.getChildren()) {
            Class<?> table = child.getTableClass();
            String tableName = TableInfoHelper.getTableInfo(table).getTableName();
            CustomizeMapper customizeMapper = (CustomizeMapper) ApplicationContextHolder.getBean(child.getMapper());

            String fields = fetchFieldAndAlias(table);

            String sql = SqlConstant.SELECT + ListUtils.makeStringSkipNull(",", fields, child.getJoinColumns()) + SqlConstant.FROM + ListUtils.makeStringSkipNull(" ", tableName, child.getJoinTables()) + SqlConstant.WHERE + tableName + "." + child.getForeignKey() + "='" + id + "'" + ListUtils.makeStringSkipNull(" ", child.getOrderBy());

            List<Map<String, Object>> data = customizeMapper.queryList(sql);

            final List<Map<String, Object>> finalData = data;
            resultMap.compute(tableName, (k, v) -> {
                if (v == null) {
                    return finalData;
                } else {
                    v.addAll(finalData);
                    return v;
                }
            });

            for (Map<String, Object> item : data) {
                recursive(Long.valueOf(item.get(child.getPrimaryKey()).toString()), child, resultMap);
            }
        }
    }

    private void loadMainTableData(Long id, TableNode root, Map<String, List<Map<String, Object>>> resultMap) {
        Class<?> table = root.getTableClass();
        String sql = buildMainTableSql(root, table, id);
        List<Map<String, Object>> data = ((CustomizeMapper) ApplicationContextHolder.getBean(root.getMapper())).queryList(sql);
        resultMap.put(root.getTableName(), data);
    }

    private void batchLoadChildData(Map<TableNode, Set<Long>> parentIdsMap,
                                    Map<String, List<Map<String, Object>>> resultMap,
                                    Map<String, TableNode> nodeMap) {
        // 遍历nodeMap中的每个节点，对于每个节点，如果resultMap中不存在该节点的数据，则初始化一个新的list。
        // 以便后续在处理子节点数据时不会出现空指针异常。
        nodeMap.forEach((k, v) -> {
            resultMap.computeIfAbsent(k, k1 -> new ArrayList<Map<String, Object>>());
        });
        parentIdsMap.forEach((node, parentIds) -> {
            if (parentIds.isEmpty()) return;

            String sql = buildChildTableSql(node, parentIds);
            List<Map<String, Object>> childData = ((CustomizeMapper) ApplicationContextHolder.getBean(node.getMapper())).queryList(sql);

            if (ListUtils.isEmpty(childData)) return;
            // 按父ID分组
            Map<Long, List<Map<String, Object>>> groupedData = childData.stream()
                .collect(Collectors.groupingBy(m -> convertToLong(m.get(StrUtil.toCamelCase(node.getForeignKey())))));

            // 批量填充结果集
            groupedData.forEach((parentId, records) -> {
                String parentTable = node.getParentTable();
                TableNode parentNode = nodeMap.get(parentTable);

                resultMap.get(parentTable).stream()
                    .filter(m -> parentId.equals(convertToLong(m.get(StrUtil.toCamelCase(parentNode.getPrimaryKey())))))
                    .findFirst()
                    .ifPresent(parentRecord -> {
                        resultMap.compute(node.getTableName(), (k, v) -> {
                            if (v == null) return new ArrayList<>(records);
                            v.addAll(records);
                            return v;
                        });
                    });
            });
        });
    }

    // 以下为工具方法（原有方法保持不动，仅展示改动部分）
    private String buildMainTableSql(TableNode root, Class<?> table, Long rootId) {
        String fields = fetchFieldAndAlias(table);
        if (root instanceof EnhancedTableNode) {
            String tableName = TableInfoHelper.getTableInfo(table).getTableName();
            return SqlConstant.SELECT
                + ListUtils.makeStringSkipNull(",", fields, ((EnhancedTableNode) root).generateJoinColumnsSql())
                + SqlConstant.FROM
                + ListUtils.makeStringSkipNull(" ", tableName, ((EnhancedTableNode) root).generateJoinTablesSql())
                + SqlConstant.WHERE
                + root.getTableName() + "." + root.getPrimaryKey() + "='" + rootId + "'";
        }
        String tableName = TableInfoHelper.getTableInfo(table).getTableName();
        return SqlConstant.SELECT
            + ListUtils.makeStringSkipNull(",", fields, root.getJoinColumns())
            + SqlConstant.FROM
            + ListUtils.makeStringSkipNull(" ", tableName, root.getJoinTables())
            + SqlConstant.WHERE
            + root.getTableName() + "." + root.getPrimaryKey() + "='" + rootId + "'";
    }

    private String buildChildTableSql(TableNode node, Set<Long> parentIds) {
        String fields = fetchFieldAndAlias(node.getTableClass());
        String tableName = TableInfoHelper.getTableInfo(node.getTableClass()).getTableName();
        if(node instanceof EnhancedTableNode){
            return SqlConstant.SELECT
                + ListUtils.makeStringSkipNull(",", fields, ((EnhancedTableNode) node).generateJoinColumnsSql())
                + SqlConstant.FROM
                + ListUtils.makeStringSkipNull(" ", tableName, ((EnhancedTableNode) node).generateJoinTablesSql())
                + SqlConstant.WHERE
                + tableName + "." + node.getForeignKey() + " IN (" + String.join(",", parentIds.stream().map(String::valueOf).toArray(String[]::new)) + ")"
                + ListUtils.makeStringSkipNull(" ", ((EnhancedTableNode) node).generateOrderBySql());
        }
        return SqlConstant.SELECT
            + ListUtils.makeStringSkipNull(",", fields, node.getJoinColumns())
            + SqlConstant.FROM
            + ListUtils.makeStringSkipNull(" ", tableName, node.getJoinTables())
            + SqlConstant.WHERE
            + tableName + "." + node.getForeignKey() + " IN (" + String.join(",", parentIds.stream().map(String::valueOf).toArray(String[]::new)) + ")"
            + ListUtils.makeStringSkipNull(" ", node.getOrderBy());
    }

    private static void processBigDecimal(List<TableNode> nodes, Map<String, List<Map<String, Object>>> resultMap) {
        nodes.forEach(node -> {
            List<Map<String, Object>> rows = resultMap.get(node.getTableName());
            if (rows != null) {
                rows.forEach(row -> row.forEach((k, v) -> {
                    if (v instanceof BigDecimal) {
                        row.put(k, ((BigDecimal) v).toPlainString());
                    }
                }));
            }
        });
    }

    /**
     * 根据主表ID加载表单数据并转换为树形结构
     *
     * @param id 主表ID
     * @return 树形结构
     */
    public Map<String, Object> init(Long id) {
        beforeInit(id);

        Map<String, List<Map<String, Object>>> loadMap = doLoad(id);
        TableNode root = fetchTableNode();
        List<TableNode> list = ListUtils.distinct(root.tree2List());
        Map<String, List<TableNode>> parentTableMap = ListUtils.toMapList(list, TableNode::getParentTable);
        Map<String, Object> viewMap = new HashMap<>();

        List<Map<String, Object>> rows = loadMap.get(root.getTableName());
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                viewMap.putAll(row);
                //状态描述
                appendStateDesc(row, viewMap);
                //添加只读属性
                appendReadonly(row, viewMap);
            }

            addChildrenData(root, loadMap, parentTableMap, viewMap);
        }

        afterInit(id, viewMap);

        return viewMap;
    }

    /**
     * 批量获取表单数据
     *
     * @param ids ID集合
     * @return 多条记录表单数据
     */
    public List<Map<String, Object>> batchInit(List<String> ids) {
        List<Map<String, Object>> viewList = new ArrayList<>();

        for (String id : ids) {
            Map<String, Object> viewMap = init(Long.valueOf(id));
            viewList.add(viewMap);
        }
        return viewList;
    }

    public Map<String, Object> doPushDown(String function, List<PushDownVo> pushDowns) {
        return new HashMap<>();
    }

    /**
     * 根据主表ID下推
     *
     * @param function 下推目标
     * @return 树形结构
     */
    public Map<String, Object> push(String function, List<PushDownVo> pushDowns) {
        Map<String, Object> viewMap = doPushDown(function, pushDowns);
        viewMap.put(FormConstants.CREATE_BY, null);
        viewMap.put(FormConstants.CREATE_TIME, null);
        viewMap.put(FormConstants.UPDATE_BY, null);
        viewMap.put(FormConstants.UPDATE_TIME, null);
        viewMap.put(FormConstants.AUDITOR, null);
        viewMap.put(FormConstants.AUDIT_TIME, null);
        viewMap.put(FormConstants.IS_CLOSED, 0);
        viewMap.put(FormConstants.IS_CANCEL, 0);
        //初始化单据状态
        viewMap.put(FormConstants.PACT_STATE, 0);
        viewMap.put(FormConstants.PACT_STATE_DESC, "草稿");
        viewMap.put(FormConstants.IS_PLAN_REVIEW, null);
        viewMap.put(FormConstants.IS_TECH_REVIEW, null);
        viewMap.put(FormConstants.IS_INIT, null);
        viewMap.put(FormConstants.ERP_ID, null);
        viewMap.put(FormConstants.ERP_CODE, null);
        viewMap.put(FormConstants.APPROVAL_OPINIONS, null);
        viewMap.put(FormConstants.IS_CHECK, null);
        viewMap.put(FormConstants.CHECK_BY, null);
        viewMap.put(FormConstants.CHECK_TIME, null);
        viewMap.put(FormConstants.IS_PAPER_CONT, null);
        viewMap.put(FormConstants.PACT_DATE, DateUtils.dateTimeNow("yyyy-MM-dd"));
        return viewMap;
    }

    /**
     * 添加状态枚举的状态描述
     *
     * @param row     行
     * @param viewMap 视图
     */
    private static void appendStateDesc(Map<String, Object> row, Map<String, Object> viewMap) {
        if (row.get(FormConstants.STATUS) != null) {
            Map<String, Object> statusMap = MapUtils.empty();

            BillStatusEnum statusEnum = BillStatusEnum.getStatusEnumDesc(Integer.parseInt(row.get(FormConstants.STATUS).toString()));
            statusMap.put(FormConstants.PACT_STATE_DESC, statusEnum == null ? null : statusEnum.getDesc());
            viewMap.putAll(statusMap);
        }
    }

    /**
     * 添加只读属性
     *
     * @param row     行
     * @param viewMap 视图
     */
    private static void appendReadonly(Map<String, Object> row, Map<String, Object> viewMap) {
        if (row.get(FormConstants.STATUS) != null) {
            BillStatusEnum statusEnum = BillStatusEnum.getStatusEnumDesc(Integer.parseInt(row.get(FormConstants.STATUS).toString()));
            viewMap.put(FormConstants.READONLY, 0);
            if (statusEnum == BillStatusEnum.AUDIT || statusEnum == BillStatusEnum.PASS) {
                viewMap.put(FormConstants.READONLY, 1);
            }
        }

        //取消
        if (row.get(FormConstants.IS_CANCEL) != null && Objects.equals(row.get(FormConstants.IS_CANCEL), 1)) {
            viewMap.put(FormConstants.READONLY, 1);
        }
        //关闭
        if (row.get(FormConstants.IS_CLOSED) != null && Objects.equals(row.get(FormConstants.IS_CLOSED), 1)) {
            viewMap.put(FormConstants.READONLY, 1);
        }
    }

    /**
     * 初始化绑定操作，初始化单据日期，单据状态
     *
     * @return 初始化字段与值的映射
     */
    public Map<String, Object> initBind() {
        Map<String, Object> resultMap = MapUtils.empty();
        resultMap.put(FormConstants.PACT_DATE, LocalDate.now());
        resultMap.put(FormConstants.STATUS, BillStatusEnum.DRAFT.getCode());
        resultMap.put(FormConstants.PACT_STATE_DESC, BillStatusEnum.DRAFT.getDesc());
        return resultMap;
    }

    /**
     * 递归添加子表数据
     *
     * @param parent         父级tableNode
     * @param loadMap        加载的表数据
     * @param parentTableMap 父级表映射
     * @param viewMap        视图数据-前端展示的数据
     */
    private void addChildrenData(TableNode parent, Map<String, List<Map<String, Object>>> loadMap, Map<String, List<TableNode>> parentTableMap, Map<String, Object> viewMap) {
        List<TableNode> nodes = parentTableMap.get(parent.getTableName());
        if (nodes != null) {
            for (TableNode node : nodes) {
                List<Map<String, Object>> childRows = loadMap.get(node.getTableName());
                if (childRows != null) {
                    for (Map<String, Object> item : childRows) {
                        List<TableNode> grandsonNodes = parentTableMap.get(node.getTableName());
                        if (grandsonNodes != null) {
                            for (TableNode grandsonNode : grandsonNodes) {
                                final List<Map<String, Object>> grandsonRows = getGrandsonMaps(grandsonNode, loadMap, item);
                                item.put(grandsonNode.getTableName(), grandsonRows);
                            }
                        }
                    }

                    viewMap.put(node.getTableName(), childRows);
                }
                addChildrenData(node, loadMap, parentTableMap, viewMap);
            }
        }
    }

    private static List<Map<String, Object>> getGrandsonMaps(TableNode grandsonNode, Map<String, List<Map<String, Object>>> resultMap, Map<String, Object> item) {
        List<Map<String, Object>> grandsonRows = resultMap.get(grandsonNode.getTableName());
        String foreignKey = StringUtils.toCamelCase(grandsonNode.getForeignKey());
        return ListUtils.select(grandsonRows, row -> Predicates.equals(row.get(foreignKey), item.get(FormConstants.ID)));
    }

    /**
     * 字段与字段别名-sql查询列
     *
     * @param cla 实体的class
     * @return 逗号分隔的查询列
     */
    private String fetchFieldAndAlias(Class<?> cla) {
        String tableName = TableInfoHelper.getTableInfo(cla).getTableName();
        List<TableFieldInfo> tableFieldInfos = TableInfoHelper.getTableInfo(cla).getFieldList();

        List<String> fields = ListUtils.of(tableName + ".id as id");
        for (TableFieldInfo tableFieldInfo : tableFieldInfos) {
            String field = tableName + "." + tableFieldInfo.getColumn() + " AS " + tableFieldInfo.getProperty();
            fields.add(field);
        }

        return ListUtils.makeString(fields, ",");
    }

    /**
     * 保存
     * 多线程不安全，调用方需要保证在多线程运行下的线程安全
     *
     * @param bo 前端传的待保存数据,data是一个树形结构
     * @return 保存结果-树形结构
     */
    @Transactional(rollbackFor = Exception.class)
    public Tuple2<Integer, Object> save(BasicBO bo) {
        Map<String, List<Map<String, Object>>> oldTableEntityMap = MapUtils.empty();
        Map<String, List<Map<String, Object>>> newTableEntityMap = MapUtils.empty();
        Map<String, Object> viewMap;
        Map<String, List<Object>> tableEntityMap;
        TableNode root = fetchTableNode();
        List<TableNode> list = ListUtils.distinct(root.tree2List());
        Long rootId;

        bo.getData().put(FormConstants.PACT_STATE, BillStatusEnum.CREATE.getCode());
        //判断是新增还是修改
        if (bo.isAdd()) {

            newTableEntityMap = getTableListMap(bo);

            Tuple2<Integer, String> tuple2 = checkSave(bo, oldTableEntityMap, newTableEntityMap);
            if (!tuple2.isSuccess()) {
                return Tuple2.of(tuple2.getFrist(), tuple2.getSecond());
            }

            tableEntityMap = fetchTableEntityWithCodeAndSatusMap(bo, false);

            //借助MDM编码重复校验
            Tuple2<Boolean, Object> result = duplicateCodeCheck(tableEntityMap);
            if (!result.isSuccess()) {
                return Tuple2.of(0, result.getSecond());
            }
            //重复校验
            result = checkSame(list, tableEntityMap);
            if (!result.getFrist()) {
                return Tuple2.of(0, result.getSecond());
            }

            tableEntityMap = doAdd(tableEntityMap);

            //保存后数据
            rootId = ((IdEntity) (tableEntityMap.get(root.getTableName()).get(0))).getId();
            newTableEntityMap = doLoad(rootId);

        } else {
            rootId = Long.valueOf(String.valueOf(bo.getData().get(root.getPrimaryKey())));
            //数据已经被删除
            CustomizeMapper customizeMapper = (CustomizeMapper) ApplicationContextHolder.getBean(root.getMapper());
            Object entity = customizeMapper.selectById(rootId);
            if (entity == null) {
                return Tuple2.of(0, "数据已被删除");
            }

            oldTableEntityMap = doLoad(rootId);

            newTableEntityMap = getTableListMap(bo);

            Tuple2<Integer, String> tuple2 = checkSave(bo, oldTableEntityMap, newTableEntityMap);
            if (!tuple2.isSuccess()) {
                return Tuple2.of(tuple2.getFrist(), tuple2.getSecond());
            }

            tableEntityMap = getTableEntityMap(bo);

            Tuple2<Boolean, Object> result;
            //借助MDM编码重复校验：仅当 code 发生变动时执行
            if (isCodeChanged(root, oldTableEntityMap, newTableEntityMap)) {
                result = duplicateCodeCheck(tableEntityMap);
                if (!result.isSuccess()) {
                    return Tuple2.of(0, result.getSecond());
                }
            }

            //重复校验
            result = checkSameForUpdate(list, tableEntityMap);
            if (!result.isSuccess()) {
                return Tuple2.of(0, result.getSecond());
            }

            //不能修改状态校验
            Tuple2<Object, Object> statusFieldValue = getStatusFieldValue(root, rootId);
            if (BillStatusEnum.AUDIT.getCode().equals(statusFieldValue.getSecond()) || BillStatusEnum.PASS.getCode().equals(statusFieldValue.getSecond())) {
                return Tuple2.of(0, "不能修改审核通过或审核中的数据");
            }

            Tuple2<Boolean, Object> updateResult = doUpdate(bo, false);
            if (!updateResult.getFrist()) {
                return Tuple2.of(0, updateResult.getSecond());
            }

            //保存后数据
            newTableEntityMap = doLoad(rootId);
            tableEntityMap = (Map<String, List<Object>>) updateResult.getSecond();
        }

        afterSave(root, oldTableEntityMap, tableEntityMap);

        //反写
        Tuple2<Boolean, String> writeBackResult = writeBack(root, getSupportType(), oldTableEntityMap, newTableEntityMap, bo.isAdd() ? BehaviorEnum.ADD : BehaviorEnum.EDIT);
        if (!writeBackResult.getFrist()) {
            throw new RuntimeException(writeBackResult.getSecond());
        }

        viewMap = init(rootId);

        return Tuple2.of(1, viewMap);
    }

    protected Tuple2<Boolean, Object> duplicateCodeCheck(Map<String, List<Object>> tableEntityMap) {
        return Tuple2.of(true, null);
    }

    /**
     * 判断主表 code 字段在保存前后是否发生变动
     *
     * @param root              主表节点
     * @param oldTableEntityMap 保存前的数据（Map 格式）
     * @param newTableEntityMap 保存后的数据（Map 格式）
     * @return true=code 变动，需执行重复校验；false=未变动
     */
    private boolean isCodeChanged(TableNode root,
                                  Map<String, List<Map<String, Object>>> oldTableEntityMap,
                                  Map<String, List<Map<String, Object>>> newTableEntityMap) {
        // 主表无 code 字段时直接返回 false，跳过重复校验
        if (FieldUtils.getField(root.getTableClass(), FormConstants.CODE, true) == null) {
            return false;
        }
        String oldCode = extractMainCode(root, oldTableEntityMap);
        String newCode = extractMainCode(root, newTableEntityMap);
        return !Objects.equals(oldCode, newCode);
    }

    /**
     * 从表数据映射中提取主表首行的 code 字段值
     */
    private String extractMainCode(TableNode root, Map<String, List<Map<String, Object>>> tableEntityMap) {
        if (tableEntityMap == null || tableEntityMap.isEmpty()) {
            return null;
        }
        List<Map<String, Object>> rows = tableEntityMap.get(root.getTableName());
        if (ListUtils.isEmpty(rows)) {
            return null;
        }
        Object code = rows.get(0).get(FormConstants.CODE);
        return code == null ? null : code.toString();
    }

    /**
     * 变更保存
     *
     * @param bo 前端传的待保存数据,data是一个树形结构
     * @return 保存结果-树形结构
     */
    @Transactional(rollbackFor = Exception.class)
    public Tuple2<Integer, Object> changeSave(BasicBO bo) {
        bo.setChange(true);
        Map<String, List<Map<String, Object>>> oldTableEntityMap = MapUtils.empty();
        Map<String, List<Map<String, Object>>> newTableEntityMap = MapUtils.empty();
        Map<String, Object> viewMap;
        Map<String, List<Object>> tableEntityMap;
        TableNode root = fetchTableNode();
        List<TableNode> list = ListUtils.distinct(root.tree2List());
        Long rootId = Long.valueOf(String.valueOf(bo.getData().get(root.getPrimaryKey())));
        if (rootId == null) {
            return Tuple2.of(0, "变更保存没有传ID");
        }
        //数据已经被删除
        CustomizeMapper customizeMapper = (CustomizeMapper) ApplicationContextHolder.getBean(root.getMapper());
        Object entity = customizeMapper.selectById(rootId);
        if (entity == null) {
            return Tuple2.of(0, "数据已被删除");
        }

        oldTableEntityMap = doLoad(rootId);

        newTableEntityMap = getTableListMap(bo);

        Tuple2<Integer, String> tuple2 = checkSave(bo, oldTableEntityMap, newTableEntityMap);
        if (!tuple2.isSuccess()) {
            return Tuple2.of(tuple2.getFrist(), tuple2.getSecond());
        }

        tableEntityMap = getTableEntityMap(bo);

        //重复校验
        final Tuple2<Boolean, Object> result = checkSameForUpdate(list, tableEntityMap);
        if (!result.getFrist()) {
            return Tuple2.of(0, result.getSecond());
        }

        Tuple2<Boolean, Object> updateResult = doUpdate(bo, true);
        if (!updateResult.getFrist()) {
            return Tuple2.of(0, updateResult.getSecond());
        }

        //保存后数据
        newTableEntityMap = doLoad(rootId);
        tableEntityMap = (Map<String, List<Object>>) updateResult.getSecond();

        afterSave(root, oldTableEntityMap, tableEntityMap);

        //反写
        Tuple2<Boolean, String> writeBackResult = writeBack(root, getSupportType(), oldTableEntityMap, newTableEntityMap, BehaviorEnum.CHANGE);
        if (!writeBackResult.getFrist()) {
            throw new RuntimeException(writeBackResult.getSecond());
        }

        viewMap = init(rootId);

        return Tuple2.of(1, viewMap);
    }

    /**
     * 批量更新，目前用到批量更新的时候字段配置。后面如果有业务单据的批量更新，再扩展
     *
     * @param bo 业务对象
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Tuple2<Boolean, Object> batchSave(BasicBO bo) {
        TableNode root = fetchTableNode();
        Map<String, List<Object>> tableEntityMap = getTableEntityMapForBatch(bo);
        List<Object> entities = tableEntityMap.get(root.getTableName());
        //Mapper
        CustomizeMapper customizeMapper = (CustomizeMapper) ApplicationContextHolder.getBean(root.getMapper());

        for (Object entity : entities) {
            if (((IdEntity) entity).getId() != null) {
                //更新
                customizeMapper.updateById(entity);
            } else {
                //新增
                Long id = sequence.nextId();
                ((IdEntity) entity).setId(id);
                customizeMapper.insert(entity);
            }
        }

        return Tuple2.of(true, tableEntityMap);
    }

    /**
     * 检查字段值是否重复，全表比较
     *
     * @param list           表
     * @param tableEntityMap 表与实体数据的映射
     * @return 结果，Boolean类型表示检查结果是否通过
     */
    private static Tuple2<Boolean, Object> checkSame(List<TableNode> list, Map<String, List<Object>> tableEntityMap) {
        for (TableNode node : list) {
            if (!node.getUnionColumns().keySet().isEmpty()) {
                CustomizeMapper customizeMapper = (CustomizeMapper) ApplicationContextHolder.getBean(node.getMapper());
                List<Object> entities = tableEntityMap.get(node.getTableName());

                // 收集所有需要查询的字段和对应的值
                for (Map.Entry<String, String> column : node.getUnionColumns().entrySet()) {
                    Set<Object> values = new HashSet<>();
                    for (Object row : entities) {
                        Object fieldValue = ReflectUtil.getFieldValue(row, StrUtil.toCamelCase(column.getKey()));
                        values.add(fieldValue);
                    }

                    // 生成查询条件
                    String condition = String.format("%s %s", column.getKey(), " IN (" + values.stream().map(value -> {
                        value = ((String) value).replace("'", "''");
                        return "'" + value + "'";
                    }).collect(Collectors.joining(",")) + ")");

                    // 执行查询
                    long count = customizeMapper.count(String.format(SqlConstant.SELECT + " COUNT(*) " + SqlConstant.FROM + " %s " + SqlConstant.WHERE + " %s", node.getTableName(), condition));
                    if (count > 0) {
                        return Tuple2.of(false, column.getValue() + "存在重复数据");
                    }
                }
            }
        }
        return Tuple2.of(true, "");
    }

    /**
     * 检查字段值是否重复，排除自身的全表比较
     *
     * @param list           表
     * @param tableEntityMap 表与实体的映射
     * @return 结果，Boolean类型表示检查结果是否通过
     */
    private static Tuple2<Boolean, Object> checkSameForUpdate(List<TableNode> list, Map<String, List<Object>> tableEntityMap) {
        for (TableNode node : list) {
            if (!node.getUnionColumns().keySet().isEmpty()) {
                CustomizeMapper customizeMapper = (CustomizeMapper) ApplicationContextHolder.getBean(node.getMapper());
                List<Object> entities = tableEntityMap.get(node.getTableName());

                String queryCondition;
                // 收集所有需要查询的字段和对应的值
                for (Map.Entry<String, String> column : node.getUnionColumns().entrySet()) {
                    Set<Object> values = new HashSet<>();
                    for (Object row : entities) {
                        Object fieldValue = ReflectUtil.getFieldValue(row, StrUtil.toCamelCase(column.getKey()));
                        values.add(fieldValue);
                    }

                    if (ListUtils.adaptIterable(values).reject(Objects::isNull).isEmpty()) {
                        continue;
                    }

                    // 生成查询条件
                    queryCondition = String.format("%s %s", column.getKey(), " IN (" + values.stream().map(value -> {
                        value = ((String) value).replace("'", "''");
                        return "'" + value + "'";
                    }).collect(Collectors.joining(",")) + ")");

                    String idValues = entities.stream().map(value -> "'" + ((IdEntity) value).getId() + "'").collect(Collectors.joining(","));
                    queryCondition = queryCondition + " AND id NOT IN(" + idValues + ")";

                    // 执行查询
                    long count = customizeMapper.count(String.format(SqlConstant.SELECT + " COUNT(*) " + SqlConstant.FROM + " %s " + SqlConstant.WHERE + " %s", node.getTableName(), queryCondition));
                    if (count > 0) {
                        return Tuple2.of(false, "唯一约束字段存在重复数据");
                    }
                }
            }
        }
        return Tuple2.of(true, "");
    }

    /**
     * 暂存
     *
     * @param bo 前端传的参数
     * @return 结果
     */
    @SneakyThrows
    @Transactional(rollbackFor = Exception.class)
    public Tuple2<Boolean, Object> staging(BasicBO bo) {
        TableNode root = fetchTableNode();
        Map<String, Object> viewMap;
        Long rootId;
        bo.getData().put(FormConstants.STATUS, BillStatusEnum.DRAFT.getCode());
        //判断是新增还是修改
        if (bo.isAdd()) {
            Map<String, List<Object>> tableEntityMap = getTableEntityMap(bo);
            tableEntityMap = doAdd(tableEntityMap);

            //保存后数据
            rootId = ((IdEntity) (tableEntityMap.get(root.getTableName()).get(0))).getId();
        } else {
            rootId = Long.valueOf(String.valueOf(bo.getData().get(root.getPrimaryKey())));
            //数据已经被删除
            CustomizeMapper customizeMapper = (CustomizeMapper) ApplicationContextHolder.getBean(root.getMapper());
            Object entity = customizeMapper.selectById(rootId);
            if (entity == null) {
                return Tuple2.of(false, "数据已被删除");
            }

            //不能修改状态校验
            Tuple2<Object, Object> statusFieldValue = getStatusFieldValue(root, rootId);
            if (BillStatusEnum.CREATE.getCode().equals(statusFieldValue.getSecond()) || BillStatusEnum.AUDIT.getCode().equals(statusFieldValue.getSecond()) || BillStatusEnum.REJECT.getCode().equals(statusFieldValue.getSecond()) || BillStatusEnum.PASS.getCode().equals(statusFieldValue.getSecond())) {
                return Tuple2.of(false, "不能暂存制单中、审核中、审核通过、驳回的数据");
            }

            Tuple2<Boolean, Object> updateResult = doStagingUpdate(bo);
            if (!updateResult.getFrist()) {
                return Tuple2.of(false, updateResult.getSecond());
            }
        }

        viewMap = init(rootId);

        return Tuple2.of(true, viewMap);
    }

    /**
     * 执行更新
     *
     * @return 更新结果
     */
    public Tuple2<Boolean, Object> doUpdate(Long rootId, Map<String, List<Object>> tableEntityMap) {
        TableNode root = fetchTableNode();
        List<TableNode> list = ListUtils.distinct(root.tree2List());
        Map<String, TableNode> tableNodeMap = ListUtils.toMap(list, TableNode::getTableName);
        Map<String, List<Map<String, Object>>> dbData = doLoad(rootId);
        extracted(tableEntityMap, tableNodeMap, dbData);
        return Tuple2.of(true, tableEntityMap);
    }

    /**
     * 执行更新
     *
     * @param bo 前端传递过来的数据
     * @return 更新结果
     */
    private Tuple2<Boolean, Object> doUpdate(BasicBO bo, boolean isChangeUpdate) {
        TableNode root = fetchTableNode();
        Long rootId = bo.getData().get(root.getPrimaryKey()) == null ? null : Long.parseLong(String.valueOf(bo.getData().get(root.getPrimaryKey())));

        List<TableNode> list = ListUtils.distinct(root.tree2List());
        Map<String, TableNode> tableNodeMap = ListUtils.toMap(list, TableNode::getTableName);
        Map<String, List<Map<String, Object>>> dbData = doLoad(rootId);

        final Map<String, List<Object>> tableEntityMap = fetchTableEntityWithCodeAndSatusMap(bo, isChangeUpdate);

        extracted(tableEntityMap, tableNodeMap, dbData);

        return Tuple2.of(true, tableEntityMap);
    }

    /**
     * 执行暂存更新
     *
     * @param bo 前端传递过来的数据
     * @return 更新结果
     */
    private Tuple2<Boolean, Object> doStagingUpdate(BasicBO bo) {
        TableNode root = fetchTableNode();
        Long rootId = bo.getData().get(root.getPrimaryKey()) == null ? null : Long.parseLong(String.valueOf(bo.getData().get(root.getPrimaryKey())));

        List<TableNode> list = ListUtils.distinct(root.tree2List());
        Map<String, TableNode> tableNodeMap = ListUtils.toMap(list, item -> item.getTableName());
        Map<String, List<Map<String, Object>>> dbData = doLoad(rootId);

        final Map<String, List<Object>> tableEntityMap = getTableEntityMap(bo);

        extracted(tableEntityMap, tableNodeMap, dbData);

        return Tuple2.of(true, tableEntityMap);
    }

    public void extracted(Map<String, List<Object>> tableEntityMap, Map<String, TableNode> tableNodeMap, Map<String, List<Map<String, Object>>> dbData) {
        for (Map.Entry entities : tableEntityMap.entrySet()) {
            List<Object> entitiesList = (List<Object>) entities.getValue();

            //前端传递过来的数据
            String tableName = (String) entities.getKey();
            TableNode tableNode = tableNodeMap.get(tableName);
            CustomizeMapper customizeMapper = (CustomizeMapper) ApplicationContextHolder.getBean(tableNode.getMapper());
            //和现有数据比较
            List<Map<String, Object>> dbTableData = dbData.get(tableName);

            Map<Long, Object> idEntityMap = ListUtils.toMap(entitiesList, entity -> ((IdEntity) entity).getId());
            List<Long> dbIds = ListUtils.collect(dbTableData, entity -> Long.valueOf(entity.get(FormConstants.ID).toString()));
            List<Long> remoteIds = ListUtils.collect(entitiesList, entity -> ((IdEntity) entity).getId());

            ListUtils.ListDiff diff = ListUtils.diff(remoteIds, dbIds, (id) -> id, (id) -> id);

            //需要新增的数据
            diff.getAdds().forEach(id -> {
                Object entity = idEntityMap.get(id);
                customizeMapper.insert(entity);
            });

            //需要更新的数据
            diff.getCommons().forEach(id -> {
                Object entity = idEntityMap.get(id);
                customizeMapper.updateById(entity);
            });

            //需要删除的数据
            diff.getDeletes().forEach(id -> {
                customizeMapper.deleteById(id);
            });
        }
    }

    /**
     * 执行新增
     *
     * @param tableEntityMap 表与实体映射
     * @return 返回新增后的表与实体映射
     */
    public Map<String, List<Object>> doAdd(Map<String, List<Object>> tableEntityMap) {
        TableNode root = fetchTableNode();

        List<TableNode> list = ListUtils.distinct(root.tree2List());
        Map<String, TableNode> tableNodeMap = ListUtils.toMap(list, item -> item.getTableName());
        for (Map.Entry entities : tableEntityMap.entrySet()) {
            List<Object> entitiesList = (List<Object>) entities.getValue();
            String tableName = (String) entities.getKey();
            TableNode tableNode = tableNodeMap.get(tableName);
            MybatisBatch<Object> mybatisBatch = new MybatisBatch<>(ApplicationContextHolder.getBean(SqlSessionFactory.class), entitiesList);
            MybatisBatch.Method<Object> method = new MybatisBatch.Method<>(tableNode.getMapper());
            mybatisBatch.execute(method.insert());
        }

        return tableEntityMap;
    }

    /**
     * 将前端传的树形map转换成表与行的映射
     *
     * @param bo 业务对象
     * @return 表与行的映射
     */
    public Map<String, List<Object>> getTableEntityMap(BasicBO bo) {
        TableNode root = fetchTableNode();
        List<TableNode> list = ListUtils.distinct(root.tree2List());
        Map<String, List<TableNode>> parentNodeMap = ListUtils.toMapList(list, TableNode::getParentTable);
        Map<String, List<Object>> tableEntityMap = MapUtils.empty();

        Map<String, Object> data = bo.getData();

        if (bo.isAdd()) {
            Long parentId = null;
            convertToEntity(root, parentId, data, parentNodeMap, tableEntityMap);
        } else {
            convertToEntityForUpdate(root, Predicates.isNull(data.get(root.getPrimaryKey())) ? null : Long.parseLong(String.valueOf(data.get(root.getPrimaryKey()))), Predicates.isNull(StringUtils.toCamelCase(root.getForeignKey())) ? null : Long.parseLong(String.valueOf(data.get(StringUtils.toCamelCase(root.getForeignKey())))), data, parentNodeMap, tableEntityMap);
        }

        return tableEntityMap;
    }

    /**
     * 将前端传的树形map转换成表与行的映射
     *
     * @param data data
     * @return 表与行的映射
     */
    public Map<String, List<Object>> getTableEntityMap(Map<String, Object> data, boolean isAdd) {
        TableNode root = fetchTableNode();
        List<TableNode> list = ListUtils.distinct(root.tree2List());
        Map<String, List<TableNode>> parentNodeMap = ListUtils.toMapList(list, TableNode::getParentTable);
        Map<String, List<Object>> tableEntityMap = MapUtils.empty();
        if (isAdd) {
            Long parentId = null;
            convertToEntity(root, parentId, data, parentNodeMap, tableEntityMap);
        } else {
            convertToEntityForUpdate(root, Predicates.isNull(data.get(root.getPrimaryKey())) ? null : Long.parseLong(String.valueOf(data.get(root.getPrimaryKey()))), Predicates.isNull(StringUtils.toCamelCase(root.getForeignKey())) ? null : Long.parseLong(String.valueOf(data.get(StringUtils.toCamelCase(root.getForeignKey())))), data, parentNodeMap, tableEntityMap);
        }
        return tableEntityMap;
    }

    /**
     * 将前端传的树形map转换成表与行的映射
     *
     * @param data 数据
     * @return 表与行的映射
     */
    public Map<String, List<Object>> getTableEntityMapByAdd(Map<String, Object> data) {
        TableNode root = fetchTableNode();
        List<TableNode> list = ListUtils.distinct(root.tree2List());
        Map<String, List<TableNode>> parentNodeMap = ListUtils.toMapList(list, TableNode::getParentTable);
        Map<String, List<Object>> tableEntityMap = MapUtils.empty();
        Long parentId = null;
        convertToEntity(root, parentId, data, parentNodeMap, tableEntityMap);
        return tableEntityMap;
    }

    /**
     * 将前端传的树形map转换成表与行的映射,值为list的形式
     *
     * @param bo 业务对象
     * @return 表与行的映射
     */
    public Map<String, List<Map<String, Object>>> getTableListMap(BasicBO bo) {
        return getTableListMap(bo.getData());
    }

    /**
     * 将前端传的树形map转换成表与行的映射,值为list的形式
     *
     * @param data 对象
     * @return 表与行的映射
     */
    public Map<String, List<Map<String, Object>>> getTableListMap(Map<String, Object> data) {
        TableNode root = fetchTableNode();
        List<TableNode> list = ListUtils.distinct(root.tree2List());
        Map<String, List<TableNode>> parentNodeMap = ListUtils.toMapList(list, TableNode::getParentTable);
        Map<String, List<Map<String, Object>>> tableEntityMap = MapUtils.empty();
        extractTableRow(root, data, parentNodeMap, tableEntityMap);
        return tableEntityMap;
    }

    @SneakyThrows
    private void extractTableRow(TableNode node, Map<String, Object> data, Map<String, List<TableNode>> parentNodeMap, Map<String, List<Map<String, Object>>> tableRowMap) {
        //保存表与行的数据
        tableRowMap.compute(node.getTableName(), (k, v) -> {
            if (v == null) {
                return ListUtils.of(data);
            } else {
                v.add(data);
                return v;
            }
        });

        //子集
        List<TableNode> tableNodes = parentNodeMap.get(node.getTableName());
        if (tableNodes != null) {
            for (TableNode child : tableNodes) {
                List<Map<String, Object>> entities = (List<Map<String, Object>>) data.get(child.getTableName());
                if (entities != null) {
                    for (Map<String, Object> map : entities) {
                        extractTableRow(child, map, parentNodeMap, tableRowMap);
                    }
                }
            }
        }
    }

    /**
     * 将前端传的树形map转换成表与行的映射
     *
     * @param bo 业务对象
     * @return 表与行的映射
     */
    @SneakyThrows
    public Map<String, List<Object>> getTableEntityMapForBatch(BasicBO bo) {
        TableNode root = fetchTableNode();
        Map<String, List<Object>> tableEntityMap = MapUtils.empty();
        List<Field> fields = getAllFields(root.getTableClass());
        List<Map<String, Object>> datas = bo.getDatas();
        List<Object> entities = ListUtils.empty();

        for (Map<String, Object> data : datas) {
            Object entity = root.getTableClass().newInstance();
            //表的列
            for (Field field : fields) {
                field.setAccessible(true);
                //字段
                if (data.containsKey(field.getName())) {
                    Object object = data.get(field.getName());
                    assignFieldValue(field, object, entity);
                }
            }
            entities.add(entity);
        }

        tableEntityMap.put(root.getTableName(), entities);

        return tableEntityMap;
    }

    /**
     * 前端传的数据没有单据号与单据状态，通过这个方法，可以生成单据号与状态
     *
     * @param bo 业务对象
     * @return 表与行的映射
     */
    private Map<String, List<Object>> fetchTableEntityWithCodeAndSatusMap(BasicBO bo, Boolean isChangeUpdate) {
        Map<String, List<Object>> tableEntityMap = getTableEntityMap(bo);
        TableNode root = fetchTableNode();
        String mainTableName = root.getTableName();
        Object mainTableEntity = ListUtils.safeGet(tableEntityMap.get(mainTableName), 0);

        boolean hasField = FieldUtils.getField(root.getTableClass(), FormConstants.CODE, true) != null;
        Object codeValue = ReflectUtil.getFieldValue(mainTableEntity, FormConstants.CODE);
        if (hasField && (Objects.isNull(codeValue) || StringUtils.isEmpty(codeValue.toString()))) {
            //给主表生成单据号
            String code = fetchCode(bo);
            ReflectUtil.setFieldValue(mainTableEntity, FormConstants.CODE, code);
        }

        hasField = FieldUtils.getField(root.getTableClass(), FormConstants.STATUS, true) != null;
        if (hasField) {
            //主表单据状态
            ReflectUtil.setFieldValue(mainTableEntity, FormConstants.STATUS, BillStatusEnum.CREATE.getCode());
        }

        //变更保存，单据默认为通过
        if (isChangeUpdate) {
            ReflectUtil.setFieldValue(mainTableEntity, FormConstants.STATUS, BillStatusEnum.PASS.getCode());
        }

        return tableEntityMap;
    }

    /**
     * 通过反射的方式将给实体赋值
     *
     * @param node           表节点
     * @param parentId       父级ID-外键
     * @param data           字段与值的映射
     * @param parentNodeMap  父级表与子级表的映射
     * @param tableEntityMap 表与行的映射
     */
    @SneakyThrows
    private void convertToEntity(TableNode node, Long parentId, Map<String, Object> data, Map<String, List<TableNode>> parentNodeMap, Map<String, List<Object>> tableEntityMap) {
        Object entity = node.getTableClass().newInstance();
        List<Field> fields = getAllFields(node.getTableClass());
        Long id = sequence.nextId();
        //表的列
        for (Field field : fields) {
            field.setAccessible(true);
            //字段
            if (data.containsKey(field.getName()) && !field.getName().equals(node.getPrimaryKey()) && !field.getName().equals(StrUtil.toCamelCase(node.getForeignKey()))) {
                Object object = data.get(field.getName());
                assignFieldValue(field, object, entity);
            } else {
                //id
                if (node.getPrimaryKey().equals(field.getName())) {
                    field.set(entity, id);
                }
                //parentId
                if (ObjectUtil.equals(StringUtils.toCamelCase(node.getForeignKey()), field.getName())) {
                    field.set(entity, parentId);
                }
            }
        }

        //保存表与行的数据
        tableEntityMap.compute(node.getTableName(), (k, v) -> {
            if (v == null) {
                return ListUtils.of(entity);
            } else {
                v.add(entity);
                return v;
            }
        });

        //子集
        List<TableNode> tableNodes = parentNodeMap.get(node.getTableName());
        if (tableNodes != null) {
            for (TableNode child : tableNodes) {
                List<Map<String, Object>> entities = (List<Map<String, Object>>) data.get(child.getTableName());
                if (entities != null) {
                    for (Map<String, Object> map : entities) {
                        convertToEntity(child, id, map, parentNodeMap, tableEntityMap);
                    }
                }
            }
        }
    }

    @SneakyThrows
    private void convertToEntityForUpdate(TableNode node, Long id, Long parentId, Map<String, Object> data, Map<String, List<TableNode>> parentNodeMap, Map<String, List<Object>> tableEntityMap) {
        Object entity = node.getTableClass().newInstance();
        List<Field> fields = getAllFields(node.getTableClass());
        if (Predicates.isNull(id)) {
            id = sequence.nextId();
        }

        //表的列
        for (Field field : fields) {
            field.setAccessible(true);
            //字段
            if (data.containsKey(field.getName())) {
                Object object = data.get(field.getName());
                assignFieldValue(field, object, entity);
                //id
                if (node.getPrimaryKey().equals(field.getName()) && object == null) {
                    field.set(entity, id);
                }
                //parentId
                if (ObjectUtil.equals(StringUtils.toCamelCase(node.getForeignKey()), field.getName()) && object == null) {
                    field.set(entity, parentId);
                }
            } else {
                //id
                if (node.getPrimaryKey().equals(field.getName())) {
                    field.set(entity, id);
                }
                //parentId
                if (ObjectUtil.equals(StringUtils.toCamelCase(node.getForeignKey()), field.getName())) {
                    field.set(entity, parentId);
                }
            }
        }

        //保存表与行的数据
        tableEntityMap.compute(node.getTableName(), (k, v) -> {
            if (v == null) {
                return ListUtils.of(entity);
            } else {
                v.add(entity);
                return v;
            }
        });

        //子集
        List<TableNode> tableNodes = parentNodeMap.get(node.getTableName());
        if (tableNodes != null) {
            for (TableNode child : tableNodes) {
                List<Map<String, Object>> entities = (List<Map<String, Object>>) data.get(child.getTableName());
                if (ListUtils.isNotEmpty(entities)) {
                    for (Map<String, Object> map : entities) {
                        convertToEntityForUpdate(child, map.get(FormConstants.ID) == null ? null : Long.parseLong(map.get(FormConstants.ID).toString()), id, map, parentNodeMap, tableEntityMap);
                    }
                } else {
                    tableEntityMap.compute(child.getTableName(), (k, v) -> {
                        if (v == null) {
                            return ListUtils.empty();
                        } else {
                            return v;
                        }
                    });
                }
            }
        }
    }

    /**
     * 给实体字段赋值
     *
     * @param field  字段
     * @param object 对象
     * @param entity 实体
     * @throws IllegalAccessException 反射对字段没有访问权限的异常，setAccessible(true)已经设置
     */
    private static void assignFieldValue(Field field, Object object, Object entity) throws IllegalAccessException {
        // 表单空串：非 String 字段（日期/数字）统一视为 null，避免 LocalDate.parse("") 等解析异常
        if (object != null && object.getClass() == String.class
            && StringUtils.isEmpty((String) object) && field.getType() != String.class) {
            object = null;
        }

        if (object != null && (field.getType().isAssignableFrom(object.getClass()) || field.getType().isPrimitive())) {
            field.set(entity, object);
        }

        if (object == null) {
            field.set(entity, null);
        }

        if (object != null && !field.getType().isAssignableFrom(object.getClass())) {
            if (field.getType() == Integer.class) {
                if (ObjectUtil.isNullOrEmpty(object)) {
                    field.set(entity, null);
                } else {
                    field.set(entity, Integer.valueOf(object.toString()));
                }
            }
            if (field.getType() == Long.class) {
                field.set(entity, Long.valueOf(object.toString()));
            }
            if (field.getType() == Double.class) {
                field.set(entity, Double.valueOf(object.toString()));
            }
            if (field.getType() == BigDecimal.class) {
                field.set(entity, new BigDecimal(object.toString()));
            }
            if (field.getType() == String.class) {
                field.set(entity, String.valueOf(object));
            }
            if (field.getType() == LocalDate.class) {
                field.set(entity, LocalDate.parse(object.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }
            if (field.getType() == LocalDateTime.class) {
                try {
                    field.set(entity, LocalDateTime.parse(object.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                } catch (Exception ex) {
                    field.set(entity, LocalDateTime.parse(object.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")));
                }
            }
            if (field.getType() == Date.class) {
                String dateStr = object.toString();
                if (dateStr.length() == 10) {
                    field.set(entity, DateUtil.parse(dateStr, "yyyy-MM-dd"));
                } else {
                    field.set(entity, DateUtil.parse(dateStr));
                }
            }
        }
    }

    /**
     * 获取实体class的所有字段
     *
     * @param clazz 实体的class
     * @return 字段列表
     */
    public List<Field> getAllFields(Class<?> clazz) {
        List<Field> fieldList = ListUtils.empty();
        while (clazz != null) {

            fieldList.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fieldList;
    }

    /**
     * 获取自动编码序号
     *
     * @param bo 前端传的业务对象
     * @return 自动编码序号
     */
    public String fetchCode(BasicBO bo) {
        SysAutoCodeSet sysAutoCodeSet = ApplicationContextHolder.getBean(ISysAutoCodeSetService.class).getAutoCodeSet(getSupportType());
        if (sysAutoCodeSet == null) {
            return null;
        }

        TableNode root = fetchTableNode();
        //编码前缀，不一定有前缀
        String masterCode = getMasterCode(bo, sysAutoCodeSet.getPrefix());
        //编码长度
        int length = StringUtils.isEmpty(masterCode) ? sysAutoCodeSet.getSerialLen() : masterCode.length() + sysAutoCodeSet.getSerialLen();
        String maxCodeSql;
        if (masterCode == null) {
            //没有前缀执行的SQL
            maxCodeSql = SqlConstant.SELECT + "MAX(code) maxCode " + SqlConstant.FROM + root.getTableName() + SqlConstant.WHERE + " code NOT LIKE '%-%' " + SqlConstant.AND + " length(code) = " + length;
        } else {
            //有前缀执行的SQL
            maxCodeSql = SqlConstant.SELECT + " MAX(code) maxCode " + SqlConstant.FROM + root.getTableName() + SqlConstant.WHERE + " code NOT LIKE '%-%' " + SqlConstant.AND + " code " + SqlConstant.LIKE + " '" + masterCode + "%' " + SqlConstant.AND + " length(code) = " + length;
        }

        CustomizeMapper customizeMapper = (CustomizeMapper) ApplicationContextHolder.getBean(root.getMapper());
        Map<String, Object> map = customizeMapper.query(maxCodeSql);
        //已经存在序列号
        String existsCode = map == null ? "" : (String) map.get("maxCode");
        if (StringUtils.isEmpty(existsCode)) {
            //没有序列号，从1开始并拼接0，凑齐编码长度
            String serialStr = StringUtils.padl("1", sysAutoCodeSet.getSerialLen(), '0');
            return StringUtils.isEmpty(masterCode) ? serialStr : masterCode + serialStr;
        } else {
            //有序列号，+1并拼接0，凑齐编码长度
            int serialNum;
            if (StringUtils.isNotEmpty(masterCode)) {
                serialNum = Integer.parseInt(existsCode.replace(masterCode, ""));
            } else {
                serialNum = Integer.parseInt(existsCode);
            }
            String serialStr = StringUtils.padl(String.valueOf(serialNum + 1), sysAutoCodeSet.getSerialLen(), '0');
            return StringUtils.isEmpty(masterCode) ? serialStr : masterCode + serialStr;
        }
    }

    public Tuple2<String, String> getMaxCode(BasicBO bo) {
        SysAutoCodeSet sysAutoCodeSet = ApplicationContextHolder.getBean(ISysAutoCodeSetService.class).getAutoCodeSet(getSupportType());
        if (sysAutoCodeSet == null) {
            return null;
        }

        TableNode root = fetchTableNode();
        //编码前缀，不一定有前缀
        String masterCode = getMasterCode(bo, sysAutoCodeSet.getPrefix());
        //编码长度
        int length = StringUtils.isEmpty(masterCode) ? sysAutoCodeSet.getSerialLen() : masterCode.length() + sysAutoCodeSet.getSerialLen();
        String maxCodeSql;
        if (masterCode == null) {
            //没有前缀执行的SQL
            maxCodeSql = SqlConstant.SELECT + "MAX(code) maxCode " + SqlConstant.FROM + root.getTableName() + SqlConstant.WHERE + " code NOT LIKE '%-%' " + SqlConstant.AND + " length(code) = " + length;
        } else {
            //有前缀执行的SQL
            maxCodeSql = SqlConstant.SELECT + " MAX(code) maxCode " + SqlConstant.FROM + root.getTableName() + SqlConstant.WHERE + " code NOT LIKE '%-%' " + SqlConstant.AND + " code " + SqlConstant.LIKE + " '" + masterCode + "%' " + SqlConstant.AND + " length(code) = " + length;
        }

        CustomizeMapper customizeMapper = (CustomizeMapper) ApplicationContextHolder.getBean(root.getMapper());
        Map<String, Object> map = customizeMapper.query(maxCodeSql);
        //已经存在序列号
        String existsCode = map == null ? "" : (String) map.get("maxCode");
        if (StringUtils.isEmpty(existsCode)) {
            //没有序列号，从1开始并拼接0，凑齐编码长度
            String serialStr = StringUtils.padl("1", sysAutoCodeSet.getSerialLen(), '0');
            return Tuple2.of(masterCode, serialStr);
        } else {
            //有序列号，+1并拼接0，凑齐编码长度
            int serialNum;
            if (StringUtils.isNotEmpty(masterCode)) {
                serialNum = Integer.parseInt(existsCode.replace(masterCode, ""));
            } else {
                serialNum = Integer.parseInt(existsCode);
            }
            String serialStr = StringUtils.padl(String.valueOf(serialNum), sysAutoCodeSet.getSerialLen(), '0');
            return Tuple2.of(masterCode, serialStr);
        }
    }

    /**
     * 获取编码实际前缀
     *
     * @param bo     前端传的业务对象
     * @param prefix 配置的编码前缀，配置年月日以及字段
     * @return 实际的前缀
     */
    private String getMasterCode(BasicBO bo, String prefix) {
        if (StringUtils.isEmpty(prefix)) {
            return prefix;
        }

        LocalDate localDate = LocalDate.now();
        String year = String.valueOf(localDate.getYear());
        String month = StringUtils.padl(String.valueOf(localDate.getMonthValue()), 2, '0');
        String day = StringUtils.padl(String.valueOf(localDate.getDayOfMonth()), 2, '0');

        if (prefix.contains("[") && prefix.contains("]")) {
            //年月日格式替换
            prefix = prefix.replace("[yyyy]", year).replace("[mm]", month).replace("[dd]", day).replace("[YYYY]", year).replace("[MM]", month).replace("[DD]", day).replace("[yy]", year.substring(2)).replace("[YY]", year.substring(2));

            //从字段取值
            prefix = getMasterCodeFromField(bo, prefix);
        }

        return prefix;
    }

    /**
     * 递归解析配置的字段
     *
     * @param bo     前端传的业务对象
     * @param prefix 配置的编码前缀，配置年月日以及字段
     * @return 实际的前缀
     */
    public String getMasterCodeFromField(BasicBO bo, String prefix) {
        //处理前段文字
        String strFirstCode;
        //处理后段文件
        String strLastCode = Strings.EMPTY;

        if (prefix.contains("[") && prefix.indexOf("]") > prefix.indexOf("[")) {
            strFirstCode = prefix.substring(0, prefix.indexOf("["));
            if (prefix.length() > prefix.indexOf("]") + 1) {
                strLastCode = prefix.substring(prefix.indexOf("]") + 1);
            }
            prefix = prefix.substring(prefix.indexOf("["), prefix.indexOf("]") - prefix.indexOf("[") + 1);

            String strTableName = prefix.substring(1, prefix.indexOf("."));
            String strColumnName = prefix.substring(prefix.indexOf(".") + 1, prefix.length() - 1);

            Map<String, String> filedMap = fetchTableNode().getAutoCodeJoinColumns();

            String field = filedMap.get(strTableName + "." + strColumnName);
            if (StringUtils.isNotEmpty(field)) {
                String fieldValue = (String) bo.getData().get(StrUtil.toCamelCase(field));
                Map<String, Object> objectMap = supplierMapper().query(String.format(SqlConstant.SELECT + " %s " + SqlConstant.FROM + " %s " + SqlConstant.WHERE + " id='%s'", strColumnName, strTableName, fieldValue));
                String value = (String) objectMap.get(StrUtil.toCamelCase(strColumnName));
                if (StringUtils.isEmpty(value)) {
                    prefix = Strings.EMPTY;
                } else {
                    prefix = value;
                }
            }

            return getMasterCodeFromField(bo, strFirstCode + prefix + strLastCode);
        }

        return prefix;
    }

    /**
     * 删除，根据主表ID删除，屋里删除
     *
     * @param id 主表ID
     * @return 删除结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Tuple2<Boolean, String> remove(Long id) {
        Tuple2<Boolean, String> result = checkDelete(id);
        if (!result.getFrist()) {
            return result;
        }

        TableNode root = fetchTableNode();
        List<TableNode> list = ListUtils.distinct(root.tree2List());
        Map<String, List<TableNode>> parentTableNameMap = ListUtils.toMapList(list, item -> item.getParentTable());

        //删除主表
        CustomizeMapper customizeMapper = (CustomizeMapper) ApplicationContextHolder.getBean(root.getMapper());

        //审核中，审核通过不能修改
        Object entity = customizeMapper.selectById(id);
        Object statusValue = ReflectUtil.getFieldValue(entity, FormConstants.STATUS);
        if (BillStatusEnum.AUDIT.getCode().equals(statusValue) || BillStatusEnum.PASS.getCode().equals(statusValue)) {
            return Tuple2.of(false, "状态为制单中、审核驳回的数据才允许删除！");
        }

        beforeDelete(id);

        Map<String, List<Map<String, Object>>> oldTableEntityMap = doLoad(id);

        customizeMapper.deleteById(id);
        //删除子表
        recursiveDelete(root, id, parentTableNameMap);

        afterDelete(id);

        //反写
        Tuple2<Boolean, String> writeBackResult = writeBack(root, getSupportType(), oldTableEntityMap, null, BehaviorEnum.DELETE);
        if (!writeBackResult.getFrist()) {
            throw new RuntimeException(writeBackResult.getSecond());
        }

        log.info("Username{}删除{}的数据:{}", LoginHelper.getUsername(), getSupportType().getDesc(), JsonUtils.toJsonString(oldTableEntityMap));

        //操作日志
        saveOperLog(oldTableEntityMap);

        return Tuple2.of(true, "删除成功");
    }

    private void saveOperLog(Map<String, List<Map<String, Object>>> oldTableEntityMap) {
        OperLogEvent operLog = new OperLogEvent();
        operLog.setStatus(BusinessStatus.SUCCESS.ordinal());
        // 请求的地址
        String ip = ServletUtils.getClientIP();
        operLog.setOperIp(ip);
        if (ServletUtils.getRequest().getRequestURI() != null) {
            operLog.setOperUrl(StringUtils.substring(ServletUtils.getRequest().getRequestURI(), 0, 255));
        }
        try {
            LoginUser loginUser = LoginHelper.getLoginUser();
            if (loginUser != null) {
                operLog.setOperName(loginUser.getUsername());
                operLog.setDeptName(loginUser.getDeptName());
            }
        } catch (Exception ex) {
            operLog.setOperName("api");
        }

        // 设置请求方式
        operLog.setRequestMethod(ServletUtils.getRequest().getMethod());
        // 处理设置注解上的参数
        operLog.setBusinessType(BusinessType.DELETE.ordinal());
        operLog.setTitle(getSupportType().getDesc());
        operLog.setOperatorType(OperatorType.MANAGE.ordinal());
        operLog.setOperParam(JsonUtils.toJsonString(oldTableEntityMap));
        // 发布事件保存数据库
        SpringUtils.context().publishEvent(operLog);
    }

    /**
     * 删除，根据主表ID删除，屋里删除
     *
     * @param id 主表ID
     * @return 删除结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Tuple2<Boolean, String> batchRemove(Long[] id) {

        for (Long one : Arrays.stream(id).distinct().toArray(Long[]::new)) {
            Tuple2<Boolean, String> result = remove(one);
            if (!result.getFrist()) {
                //删除失败必须如实返回（如 checkDelete 校验不通过），否则前端会误以为删除成功
                return result;
            }
        }

        return Tuple2.of(true, "删除成功");
    }

    /**
     * 递归删除 子表及孙表
     *
     * @param node               表节点
     * @param id                 id
     * @param parentTableNameMap 父表名与子表映射
     */
    public void recursiveDelete(TableNode node, Long id, Map<String, List<TableNode>> parentTableNameMap) {
        List<TableNode> childTableNodes = parentTableNameMap.get(node.getTableName());
        if (childTableNodes != null) {
            for (TableNode tableNode : childTableNodes) {
                CustomizeMapper customizeMapper = (CustomizeMapper) ApplicationContextHolder.getBean(tableNode.getMapper());

                List<Map<String, Object>> childList = customizeMapper.queryList("select id from " + tableNode.getTableName() + " where " + tableNode.getForeignKey() + " = '" + id + "'");

                if (childList != null && childList.size() > 0) {
                    for (Map<String, Object> item : childList) {
                        Long childId = (Long) item.get(FormConstants.ID);
                        customizeMapper.deleteById(childId);
                        recursiveDelete(tableNode, childId, parentTableNameMap);
                    }
                }
            }
        }
    }

    /**
     * 表头重新赋值
     *
     * @param id
     * @param row
     */
    public void refreshFieldOnCopyMain(Long id, Map<String, Object> row) {
    }

    /**
     * 重新给子表字段赋值
     *
     * @param id  主表ID
     * @param row 子表行
     */
    public void refreshFieldOnCopyGoods(Long id, Map<String, Object> row) {
    }

    /**
     * 复制，选择一条或多条子表记录复制  关联关系也一起复制
     *
     * @param id 主表ID
     * @param bo 业务对象
     * @return 复制结果
     */
    public Tuple2<Boolean, Object> copyAll(Long id, BasicBO bo) {
        Tuple2<Boolean, Object> tuple2 = beforeCopy(id, bo);
        if (!tuple2.getFrist()) {
            return tuple2;
        }

        Map<String, List<Map<String, Object>>> resultMap = doLoad(id);

        TableNode root = fetchTableNode();

        //剔除没有选中的子表数据
        List<TableNode> childNode = root.getChildren();
        Set<String> detailIds = (bo != null && ListUtils.isNotEmpty(bo.getDetailId()))
            ? new HashSet<>(bo.getDetailId()) : null;
        for (TableNode node : childNode) {
            List<Map<String, Object>> rows = resultMap.get(node.getTableName());
            if (ListUtils.isNotEmpty(rows)) {
                if (detailIds != null) {
                    Iterator<Map<String, Object>> iterator = rows.iterator();
                    while (iterator.hasNext()) {
                        Map<String, Object> row = iterator.next();
                        String detailId = row.get(FormConstants.ID).toString();
                        if (!detailIds.contains(detailId)) {
                            iterator.remove();
                        }
                    }
                }
                //递归清理孙表数据（含isCleanOnCopy=true的表）
                removeRows(node, detailIds, resultMap);
            }
        }

        //表头字段重新赋值
        List<TableNode> list = ListUtils.distinct(root.tree2List());
        List<Map<String, Object>> rows = resultMap.get(root.fetchRoot().getTableName());
        if (ListUtils.isNotEmpty(rows)) {
            for (Map<String, Object> row : rows) {
                refreshMainTableCommonFields(row);
            }
        }
        //子表字段重新赋值
        TableNode goodsTableNode = root.getGoodsTableNode();
        if (goodsTableNode != null) {
            List<Map<String, Object>> goodsRows = resultMap.get(goodsTableNode.getTableName());
            if (ListUtils.isNotEmpty(goodsRows)) {
                for (Map<String, Object> row : goodsRows) {
                    refreshSonTableCommonFields(row);
                }
            }
        }

        //转成树
        Map<String, List<TableNode>> parentTableMap = ListUtils.toMapList(list, TableNode::getParentTable);
        Map<String, Object> viewMap = new HashMap<>();
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                row.put(FormConstants.ID, null);
                viewMap.putAll(row);
            }
            //清空ID
            addChildrenDataAndRemoveId(root, resultMap, parentTableMap, viewMap);
        }

        afterAssociationCopy(id, bo, viewMap);

        return Tuple2.of(true, viewMap);
    }

    /**
     * 复制，选择一条或多条子表记录复制
     *
     * @param id 主表ID
     * @param bo 业务对象
     * @return 复制结果
     */
    public Tuple2<Boolean, Object> copy(Long id, BasicBO bo) {
        Tuple2<Boolean, Object> tuple2 = beforeCopy(id, bo);
        if (!tuple2.getFrist()) {
            return tuple2;
        }

        Map<String, List<Map<String, Object>>> resultMap = doLoad(id);

        TableNode root = fetchTableNode();

        //剔除没有选中的子表数据
        List<TableNode> childNode = root.getChildren();
        Set<String> detailIds = (bo != null && ListUtils.isNotEmpty(bo.getDetailId()))
            ? new HashSet<>(bo.getDetailId()) : null;
        for (TableNode node : childNode) {
            List<Map<String, Object>> rows = resultMap.get(node.getTableName());
            if (ListUtils.isNotEmpty(rows)) {
                if (detailIds != null) {
                    Iterator<Map<String, Object>> iterator = rows.iterator();
                    while (iterator.hasNext()) {
                        Map<String, Object> row = iterator.next();
                        String detailId = row.get(FormConstants.ID).toString();
                        if (!detailIds.contains(detailId)) {
                            iterator.remove();
                        }
                    }
                }
                //递归清理孙表数据（含isCleanOnCopy=true的表）
                removeRows(node, detailIds, resultMap);
            }
        }

        //表头字段重新赋值
        List<TableNode> list = ListUtils.distinct(root.tree2List());
        List<Map<String, Object>> rows = resultMap.get(root.fetchRoot().getTableName());
        if (ListUtils.isNotEmpty(rows)) {
            for (Map<String, Object> row : rows) {
                refreshMainTableCommonFields(row);
            }
        }
        //子表字段重新赋值
        TableNode goodsTableNode = root.getGoodsTableNode();
        if (goodsTableNode != null) {
            List<Map<String, Object>> goodsRows = resultMap.get(goodsTableNode.getTableName());
            if (ListUtils.isNotEmpty(goodsRows)) {
                for (Map<String, Object> row : goodsRows) {
                    refreshSonTableCommonFields(row);
                }
            }
        }

        //转成树
        Map<String, List<TableNode>> parentTableMap = ListUtils.toMapList(list, TableNode::getParentTable);
        Map<String, Object> viewMap = new HashMap<>();
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                row.put(FormConstants.ID, null);
                viewMap.putAll(row);
            }
            //清空ID
            addChildrenDataAndRemoveId(root, resultMap, parentTableMap, viewMap);
        }

        afterCopy(id, bo, viewMap);

        return Tuple2.of(true, viewMap);
    }

    private void removeRows(TableNode node, Set<String> detailIds, Map<String, List<Map<String, Object>>> result) {
        List<TableNode> childTableNodes = node.getChildren();
        for (TableNode grandNode : childTableNodes) {
            //孙表
            List<Map<String, Object>> grandRows = result.get(grandNode.getTableName());
            if (ListUtils.isNotEmpty(grandRows)) {
                Iterator<Map<String, Object>> grandIterator = grandRows.iterator();
                while (grandIterator.hasNext()) {
                    if (grandNode.getIsCleanOnCopy()) {
                        grandIterator.next();
                        grandIterator.remove();
                    } else if (detailIds != null) {
                        Map<String, Object> grand = grandIterator.next();
                        if (!detailIds.contains(grand.get(StrUtil.toCamelCase(grandNode.getForeignKey())).toString())) {
                            grandIterator.remove();
                        }
                    } else {
                        grandIterator.next();
                    }
                }
            }
            removeRows(grandNode, detailIds, result);
        }
    }

    private void addChildrenDataAndRemoveId(TableNode parent, Map<String, List<Map<String, Object>>> resultMap, Map<String, List<TableNode>> parentTableMap, Map<String, Object> viewMap) {
        List<TableNode> nodes = parentTableMap.get(parent.getTableName());
        if (nodes != null) {
            for (TableNode node : nodes) {
                List<Map<String, Object>> childRows = resultMap.get(node.getTableName());
                if (childRows != null) {
                    for (Map<String, Object> item : childRows) {
                        List<TableNode> grandsonNodes = parentTableMap.get(node.getTableName());
                        if (grandsonNodes != null) {
                            for (TableNode grandsonNode : grandsonNodes) {
                                final List<Map<String, Object>> grandsonRows = getGrandsonMaps(grandsonNode, resultMap, item);

                                grandsonRows.forEach(grandson -> {
                                    grandson.remove(grandsonNode.getPrimaryKey());
                                    grandson.remove(StrUtil.toCamelCase(grandsonNode.getForeignKey()));
                                });

                                item.put(grandsonNode.getTableName(), grandsonRows);
                            }
                        }
                    }

                    childRows.forEach(item -> {
                        item.remove(node.getPrimaryKey());
                        item.remove(StrUtil.toCamelCase(node.getForeignKey()));
                        item.put(FormConstants.IS_CLOSED, null);
                        item.put(FormConstants.CLOSED_BY, null);
                        item.put(FormConstants.CLOSED_TIME, null);
                        item.put(FormConstants.IS_CANCEL, null);
                        item.put(FormConstants.CANCEL_BY, null);
                        item.put(FormConstants.CANCEL_TIME, null);
                    });

                    viewMap.put(node.getTableName(), childRows);
                }

                addChildrenDataAndRemoveId(node, resultMap, parentTableMap, viewMap);
            }
        }
    }

    /**
     * 撤回/反审核
     *
     * @param id 主表ID
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Tuple2<Boolean, String> rollBack(Long id) {
        return workFlowService.rollBack(this,id);
    }

    /**
     * 是否跳过OA审批
     *
     * @return
     */
    @Override
    public Boolean isIgnoreOa(Long id) {
        WfConfig wfConfig = getWfConfigMapper().getWfFunctionName(getSupportType().getCode());
        if (wfConfig == null) {
            return true;
        }
        return false;
    }

    /**
     * 取消流程是否跳过OA审批
     *
     * @param functionEnum FunctionEnum
     * @param vo           vo
     * @param isGoods      是否为明细
     * @return Boolean
     */
    public Boolean cancelFlowIsIgnoreOa(FunctionEnum functionEnum, BatchCancelVo vo, Boolean isGoods) {
        WfConfig wfConfig = getWfConfigMapper().getWfFunctionName(functionEnum.getCode());
        if (wfConfig == null) {
            return true;
        }
        Boolean flowIsIgnoreOa;
        if (isGoods) {
            flowIsIgnoreOa = cancelFlowIsIgnoreOaGoods(vo);
        } else {
            flowIsIgnoreOa = cancelFlowIsIgnoreOa(vo);
        }
        if (flowIsIgnoreOa) {
            return true;
        }
        return false;
    }

    /**
     * 取消流程是否跳过OA审批
     *
     * @param vo vo
     * @return Boolean
     */
    public Boolean cancelFlowIsIgnoreOa(BatchCancelVo vo) {
        return false;
    }

    /**
     * 取消流程是否跳过OA审批
     *
     * @param vo vo
     * @return Boolean
     */
    public Boolean cancelFlowIsIgnoreOaGoods(BatchCancelVo vo) {
        return false;
    }

    /**
     * 提交，要做幂等性控制
     *
     * @param id 主表ID
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Tuple2<Integer, String> submit(Long id, BasicBO bo) {
        return workFlowService.submit(this,id,bo);
    }

    /**
     * 关闭
     *
     * @param id
     * @return
     */
    public Tuple2<Boolean, String> close(Long id) {
        beforeClose();

        TableNode root = fetchTableNode();

        //查询单据状态
        CustomizeMapper customizeMapper = (CustomizeMapper) ApplicationContextHolder.getBean(root.getMapper());
        Object entity = customizeMapper.selectById(id);
        if (entity != null) {
            // 手动关闭
            ReflectUtil.setFieldValue(entity, FormConstants.IS_CLOSED, CloseStatusEnum.MANUAL_CLOSE);
            customizeMapper.updateById(entity);
            return Tuple2.of(true, "关闭成功");
        }

        afterClose();

        return Tuple2.of(false, "关闭失败");
    }

    /**
     * 批量关闭单据
     *
     * @param ids 表头id列表
     * @return Tuple2<Boolean, String>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Tuple2<Boolean, String> batchClose(Long[] ids) {
        Tuple2<Boolean, String> tuple2 = batchChangeCloseStatus(ids, CloseStatusEnum.MANUAL_CLOSE.getCode());
        if (tuple2.getFrist()) {
            return Tuple2.of(true, "关闭成功");
        } else {
            return Tuple2.of(false, "关闭失败" + tuple2.getSecond());
        }
    }

    /**
     * 批量反关闭单据
     *
     * @param ids id列表
     * @return Tuple2<Boolean, String>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Tuple2<Boolean, String> batchReClose(Long[] ids) {
        Tuple2<Boolean, String> tuple2 = batchChangeCloseStatus(ids, CloseStatusEnum.NORMAL.getCode());
        if (tuple2.getFrist()) {
            return Tuple2.of(true, "关闭成功");
        } else {
            return Tuple2.of(false, "关闭失败" + tuple2.getSecond());
        }
    }

    /**
     * 批量修改单据关闭状态
     *
     * @param ids      ids
     * @param isClosed 关闭状态
     * @return int
     */
    private Tuple2<Boolean, String> batchChangeCloseStatus(Long[] ids, Integer isClosed) {
        TableNode root = fetchTableNode();
        //查询单据状态
        CustomizeMapper customizeMapper = (CustomizeMapper) ApplicationContextHolder.getBean(root.getMapper());

        Map<String, List<Map<String, Object>>> oldMap = doLoad(ids[0]);
        // 获取主表数据
        Map<String, Object> masterMap = oldMap.get(root.getTableName()).get(0);
        // 校验状态
        if (!BillStatusEnum.PASS.getCode().equals(masterMap.get(FormConstants.PACT_STATE))) {
            return Tuple2.of(false, "通过的单据才能关闭");
        }
        // 构建批量更新的条件
        UpdateWrapper<BaseEntity> updateWrapper = Wrappers.update();
        // 自动关闭的单据不能更新-- 现在可以更新
        updateWrapper.in("id", ids).set("is_closed", isClosed).set("closed_by", LoginHelper.getUserId()).set("closed_time", LocalDateTime.now());

        // 执行批量更新
        int rowsAffected = customizeMapper.update(updateWrapper);
        // 更新子表状态
        CustomizeMapper detailsMapper = (CustomizeMapper) ApplicationContextHolder.getBean(root.getChildren().get(0).getMapper());
        // 获取外键
        String foreignKey = root.getChildren().get(0).getForeignKey();

        // 构建批量更新的条件
        UpdateWrapper<BaseEntity> updateDetailsWrapper = Wrappers.update();
        updateDetailsWrapper.in(foreignKey, ids).set("is_closed", isClosed).set("closed_by", LoginHelper.getUserId()).set("closed_time", LocalDateTime.now());

        // 执行批量更新
        detailsMapper.update(updateDetailsWrapper);
        return Tuple2.of(true, null);
    }

    /**
     * 批量关闭单据明细
     *
     * @param ids 明细id列表
     * @return Tuple2<Boolean, String>
     */
    @Override
    public Tuple2<Boolean, String> batchCloseDetails(Long[] ids) {
        Tuple2<Boolean, String> tuple2 = batchChangeDetailsCloseStatus(ids, CloseStatusEnum.MANUAL_CLOSE.getCode());
        if (tuple2.getFrist()) {
            return Tuple2.of(true, "关闭成功");
        } else {
            return Tuple2.of(false, "关闭失败" + tuple2.getSecond());
        }
    }

    /**
     * 批量反关闭单据明细
     *
     * @param ids 明细id列表
     * @return Tuple2<Boolean, String>
     */
    @Override
    public Tuple2<Boolean, String> batchReCloseDetails(Long[] ids) {
        Tuple2<Boolean, String> tuple2 = batchChangeDetailsCloseStatus(ids, CloseStatusEnum.NORMAL.getCode());
        if (tuple2.getFrist()) {
            return Tuple2.of(true, "关闭成功");
        } else {
            return Tuple2.of(false, "关闭失败" + tuple2.getSecond());
        }
    }

    /**
     * 批量修改单据明细关闭状态
     *
     * @param ids      ids
     * @param isClosed 关闭状态
     * @return int
     */
    private Tuple2<Boolean, String> batchChangeDetailsCloseStatus(Long[] ids, Integer isClosed) {
        TableNode root = fetchTableNode();
        //查询单据状态
        CustomizeMapper customizeMapper = (CustomizeMapper) ApplicationContextHolder.getBean(root.getChildren().get(0).getMapper());
        // 获取外键
        String foreignKey = root.getChildren().get(0).getForeignKey();
        QueryWrapper<Map<String, Object>> queryWrapper = query();
        queryWrapper.select("DISTINCT " + foreignKey).in("id", ids);
        List<Map<String, Long>> list = customizeMapper.selectMaps(queryWrapper);
        // 单据id
        Long pactId = convertToLong(list.get(0).get(foreignKey));
        Map<String, List<Map<String, Object>>> oldMap = doLoad(pactId);
        // 获取主表数据
        Map<String, Object> masterMap = oldMap.get(root.getTableName()).get(0);
        // 校验状态
        if (!BillStatusEnum.PASS.getCode().equals(masterMap.get(FormConstants.PACT_STATE))) {
            return Tuple2.of(false, "通过的单据才能关闭");
        }
        // 构建批量更新的条件
        UpdateWrapper<CancelClosedIdEntity> updateWrapper = Wrappers.update();
        updateWrapper.in("id", ids).set("is_closed", isClosed).set("closed_by", LoginHelper.getUserId()).set("closed_time", LocalDateTime.now());
        // 执行批量更新
        int rowsAffected = customizeMapper.update(updateWrapper);
        // 查出所有明细数据，判断是否更新表头--
        ArrayList<Long> needUpdatedId = new ArrayList<>();
        // 遍历判断是否需要更新表头
        for (Map<String, Long> map : list) {
            Long id = map.get(foreignKey);
            QueryWrapper<Map<String, Object>> countWrapper = query();
            countWrapper.eq(foreignKey, id);
            // 查询表头下的明细合计
            Long count1 = customizeMapper.selectCount(countWrapper);
            // 查询表头下明细并且关闭状态是1或者2的合计
            countWrapper.and(wrapper -> wrapper.eq("is_closed", CloseStatusEnum.AUTO_CLOSE.getCode()).or().eq("is_closed", CloseStatusEnum.MANUAL_CLOSE.getCode()));
            Long count2 = customizeMapper.selectCount(countWrapper);
            // 判断是否相同，相同就更新表头
            if (count1.equals(count2)) {
                needUpdatedId.add(id);
            }
        }
        CustomizeMapper masterTableMapper = (CustomizeMapper) ApplicationContextHolder.getBean(root.getMapper());
        // 构建批量更新的条件
        UpdateWrapper<BaseEntity> updateWrapper2 = Wrappers.update();
        if (!needUpdatedId.isEmpty()) {
            updateWrapper2.in("id", needUpdatedId).ne("is_closed", CloseStatusEnum.AUTO_CLOSE.getCode()).set("is_closed", isClosed).set("closed_by", LoginHelper.getUserId()).set("closed_time", LocalDateTime.now());
            // 更新表头状态执行批量更新
            masterTableMapper.update(updateWrapper2);
        }
        Long id = convertToLong(list.get(0).get(foreignKey));
        return Tuple2.of(true, null);
    }

    /**
     * 状态字段的值
     *
     * @param root 主表
     * @param id   主表ID
     * @return 状态字段的值
     */
    public static Tuple2<Object, Object> getStatusFieldValue(TableNode root, Long id) {
        //查询单据当前状态
        CustomizeMapper customizeMapper = (CustomizeMapper) ApplicationContextHolder.getBean(root.getMapper());
        Object entity = customizeMapper.selectById(id);
        if (entity != null) {
            List<Field> fields = TableInfoHelper.getAllFields(entity.getClass());
            Map<String, Field> fieldMap = ListUtils.toMap(fields, field -> field.getName());
            Object fieldValue = ReflectUtil.getFieldValue(entity, fieldMap.get(FormConstants.STATUS));
            return Tuple2.of(entity, fieldValue);
        }
        return Tuple2.of(entity, null);
    }

    /**
     * 审核通过
     *
     * @param id 主表ID
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Tuple2<Boolean, String> pass(Long id, Map<String, Object> bo) {
        return workFlowService.pass(this, id, bo);
    }

    /**
     * 审核驳回
     *
     * @param id 主表ID
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Tuple2<Boolean, String> reject(Long id, Map<String, Object> bo) {
        return workFlowService.reject(this, id, bo);
    }

    @Transactional(rollbackFor = Exception.class)
    public Tuple2<Boolean, String> callBack(Map<String, Object> bo) {
        return workFlowService.callBack(this,bo);
    }

    public WfConfigMapper getWfConfigMapper() {
        return ApplicationContextHolder.getBean(WfConfigMapper.class);
    }

    public IWfConfigService getWfConfigService() {
        return ApplicationContextHolder.getBean(IWfConfigService.class);
    }

    public IInterfaceHelper getInterfaceHelper() {
        return ApplicationContextHolder.getBean(IInterfaceHelper.class);
    }

    public IWorkFlowService getWorkFlowService() {
        return ApplicationContextHolder.getBean(IWorkFlowService.class);
    }

    public ISysEmployeeService getSysEmployeeService() {
        return ApplicationContextHolder.getBean(ISysEmployeeService.class);
    }

    /**
     * 批量分发
     *
     * @param ids
     * @return
     */
    public Boolean muliSend(Long[] ids) {
        for (Long id : ids) {
            Map<String, List<Map<String, Object>>> resultMap = doLoad(id);
            TableNode root = fetchTableNode();
            IInterfaceHelper _interfaceHelper = getInterfaceHelper();
            _interfaceHelper.setAPIInfo(resultMap, root, id, 1);
        }
        return true;
    }

    public Boolean muliSend(Long[] ids, int workTiming) {
        for (Long id : ids) {
            Map<String, List<Map<String, Object>>> resultMap = doLoad(id);
            TableNode root = fetchTableNode();
            IInterfaceHelper _interfaceHelper = getInterfaceHelper();
            _interfaceHelper.setAPIInfo(resultMap, root, id, workTiming);
        }
        return true;
    }

    /**
     * 全量分发
     *
     * @return
     */
    public Boolean allSend() {
        TableNode root = fetchTableNode();
        TableNode tree = fetchTableNode();
        Class<?> table = tree.getTableClass();
        String tableName = TableInfoHelper.getTableInfo(table).getTableName();
        CustomizeMapper customizeMapper = (CustomizeMapper) ApplicationContextHolder.getBean(tree.getMapper());

        String sql = SqlConstant.SELECT + tableName + ".id " + SqlConstant.FROM + ListUtils.makeStringSkipNull(" ", tableName, tree.getJoinTables()) + SqlConstant.WHERE + tableName + ".pact_state=3";
        List<Map<String, Object>> data = customizeMapper.queryList(sql);
        for (Map<String, Object> item : data) {
            Long id = (Long) item.get("id");
            Map<String, List<Map<String, Object>>> resultMap = doLoad(id);
            IInterfaceHelper _interfaceHelper = getInterfaceHelper();
            _interfaceHelper.setAPIInfo(resultMap, root, id);
        }
        return true;
    }

    public R reTry(Long logId) {
        IInterfaceHelper _interfaceHelper = getInterfaceHelper();
        R result = _interfaceHelper.ReSend(logId);
        return result;
    }

    /**
     * 批量取消单据
     *
     * @param vo 取消vo
     * @return Tuple2<Boolean, String>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Tuple2<Boolean, String> batchCancel(BatchCancelVo vo) {
        Long[] ids = vo.getIds();
        if (ids.length == 0) {
            return Tuple2.of(false, "请选择单据");
        }
        Long id = ids[0];
        // 校验是否有下游数据
        Tuple2<Boolean, String> isAllowCancel = verifyIsAllowCancel(id, vo);
        if (!isAllowCancel.getFrist()) {
            return Tuple2.of(false, isAllowCancel.getSecond());
        }
        Tuple2<Boolean, String> beforeCancelTuple2 = beforeCancel(vo, false);
        if (!beforeCancelTuple2.getFrist()) {
            return Tuple2.of(false, beforeCancelTuple2.getSecond());
        }
        FunctionEnum functionEnum = FunctionEnum.fromCode(getSupportType().getCode() + "_CANCEL");
        Boolean isIgnoreOa = cancelFlowIsIgnoreOa(functionEnum, vo, false);
        TableNode root = fetchTableNode();
        Map<String, List<Map<String, Object>>> oldMap = doLoad(id);
        Map<String, Object> map = oldMap.get(root.getTableName()).get(0);
        //查询单据状态
        if (!BillStatusEnum.PASS.getCode().equals(map.get(FormConstants.PACT_STATE))) {
            return Tuple2.of(false, "通过的单据才能取消");
        }
        //取消审核中 不允许再次取消
        if (CancelOaStatusEnum.CANCEL_OA_ING.getCode().equals(map.get(FormConstants.CANCEL_OA_STATE))) {
            return Tuple2.of(false, "取消审批中，不能再次取消");
        }
        if (vo.getCancelReason() != null) {
            try {
                // 保存取消原因
                // 构建批量更新的条件
                String cancelReason = genCancelReason(vo.getCancelReason(), false, oldMap, vo);
                UpdateWrapper<BaseEntity> updateWrapper = Wrappers.update();
                updateWrapper.in("id", ids).set("cancel_reason", cancelReason);
                updateByUpdateWrapper(updateWrapper);
            } catch (Exception e) {
                return Tuple2.of(false, "取消原因保存失败:" + e.getMessage());
            }
        }
        if (isIgnoreOa || isAdmin()) {
            return batchChangeCancelStatus(ids, CancelStatusEnum.CANCEL.getCode(), null);
        } else {
            getWfConfigService().createWf(functionEnum, id, () -> getSourceId(get(id)), null);
            // 创建oa流程
            // 更新表头取消状态为oa审核中
            UpdateWrapper<BaseEntity> updateWrapper = Wrappers.update();
            updateWrapper.in("id", ids).set("cancel_oa_state", CancelOaStatusEnum.CANCEL_OA_ING.getCode()).set("cancel_by", LoginHelper.getUserId());
            updateByUpdateWrapper(updateWrapper);
            // 更新明细取消状态
            String foreignKey = root.getChildren().get(0).getForeignKey();
            CustomizeMapper customizeMapper = (CustomizeMapper) ApplicationContextHolder.getBean(root.getChildren().get(0).getMapper());
            UpdateWrapper<BaseEntity> goodsUpdateWrapper = Wrappers.update();
            goodsUpdateWrapper.in(foreignKey, ids).set("cancel_oa_state", CancelOaStatusEnum.CANCEL_OA_ING.getCode()).set("cancel_by", LoginHelper.getUserId());
            customizeMapper.update(goodsUpdateWrapper);
            return Tuple2.of(true, "已创建OA取消流程");
        }
    }

    /**
     * 批量反取消单据
     *
     * @param ids id列表
     * @return Tuple2<Boolean, String>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Tuple2<Boolean, String> batchReCancel(Long[] ids) {
        if (!LoginHelper.isAdmin()) {
            return Tuple2.of(false, "请联系管理员反取消单据");
        }
        return batchChangeCancelStatus(ids, CancelStatusEnum.NORMAL.getCode(), null);
    }

    /**
     * 批量修改单据取消状态
     *
     * @param ids      ids
     * @param isCancel 取消状态
     * @return int
     */
    @Transactional(rollbackFor = Exception.class)
    public Tuple2<Boolean, String> batchChangeCancelStatus(Long[] ids, Integer isCancel, Map<String, Object> bo) {
        Long userId;
        if (bo != null && bo.get("auditor") != null) {

            if(ObjectUtil.isEmpty(bo.get("auditor"))){
                throw new RuntimeException("审批人不能为空");
            }

            String auditor = (String) bo.get("auditor");
            Long auditorId = sysUserService.getUserIdByEmployeeNo(auditor);
            if (auditorId == null) {
                userId = Long.valueOf(auditor);
            } else {
                userId = auditorId;
            }
        } else {
            userId = LoginHelper.getUserId();
        }
        TableNode root = fetchTableNode();
        Map<String, List<Map<String, Object>>> oldMap = doLoad(ids[0]);
        Map<String, Object> map = oldMap.get(root.getTableName()).get(0);
        if (!BillStatusEnum.PASS.getCode().equals(map.get(FormConstants.PACT_STATE))) {
            return Tuple2.of(false, "通过的单据才能取消");
        }


        //查询单据状态
        CustomizeMapper customizeMapper = (CustomizeMapper) ApplicationContextHolder.getBean(root.getMapper());
        // 构建批量更新的条件
        UpdateWrapper<BaseEntity> updateWrapper = Wrappers.update();
        updateWrapper.in("id", ids).set("is_cancel", isCancel).set("cancel_by", userId).set("cancel_time", LocalDateTime.now());

        // 执行批量更新
        int rowsAffected = customizeMapper.update(updateWrapper);
        // 更新子表状态
        CustomizeMapper detailsMapper = (CustomizeMapper) ApplicationContextHolder.getBean(root.getChildren().get(0).getMapper());
        // 获取外键
        String foreignKey = root.getChildren().get(0).getForeignKey();

        // 构建批量更新的条件
        UpdateWrapper<BaseEntity> updateDetailsWrapper = Wrappers.update();
        updateDetailsWrapper.in(foreignKey, ids).set("is_cancel", isCancel).set("cancel_by", userId).set("cancel_time", LocalDateTime.now());

        // 执行批量更新
        detailsMapper.update(updateDetailsWrapper);

        Map<String, List<Map<String, Object>>> newMap = doLoad(ids[0]);
        if (isCancel.equals(CancelStatusEnum.CANCEL.getCode())) {
            Tuple2<Boolean, String> res = writeBack(root, getSupportType(), oldMap, newMap, BehaviorEnum.CANCEL);
            if (!res.getFrist()) {
                return Tuple2.of(false, "取消失败,反写失败");
            }
        } else {
            Tuple2<Boolean, String> res = checkCommon(getSupportType(), oldMap, newMap);
            if (!res.getFrist()) {
                return Tuple2.of(false, "取消失败,校验失败");
            }
            Tuple2<Boolean, String> res2 = writeBack(root, getSupportType(), oldMap, newMap, BehaviorEnum.CANCEL);
            if (!res2.getFrist()) {
                return Tuple2.of(false, "取消失败,反写失败");
            }
        }

        if (rowsAffected > 0) {
            Tuple2<Boolean, String> res2 = afterCancel(ids, isCancel, bo);
            if (!res2.isSuccess()) {
                return Tuple2.of(false, "下游单据取消失败");
            }
            return Tuple2.of(true, "取消成功");
        } else {
            return Tuple2.of(false, "取消失败");
        }
    }

    /**
     * 取消后执行
     *
     * @param ids
     */
    public Tuple2<Boolean, String> afterCancel(Long[] ids, Integer isCancel, Map<String, Object> bo) {
        return Tuple2.of(true, "");
    }

    /**
     * 批量修改取消审批状态
     *
     * @param ids   ids
     * @param state 取消状态
     * @return int
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchChangeCancelOaStatus(Long[] ids, Integer state) {
        TableNode root = fetchTableNode();
        CustomizeMapper customizeMapper = (CustomizeMapper) ApplicationContextHolder.getBean(root.getMapper());
        // 构建批量更新的条件
        UpdateWrapper<BaseEntity> updateWrapper = Wrappers.update();
        updateWrapper.in("id", ids).set("cancel_oa_state", state);
        // 执行批量更新
        int rowsAffected = customizeMapper.update(updateWrapper);
        // 更新子表状态
        CustomizeMapper detailsMapper = (CustomizeMapper) ApplicationContextHolder.getBean(root.getChildren().get(0).getMapper());
        // 获取外键
        String foreignKey = root.getChildren().get(0).getForeignKey();
        // 构建批量更新的条件
        UpdateWrapper<BaseEntity> updateDetailsWrapper = Wrappers.update();
        updateDetailsWrapper.in(foreignKey, ids).set("cancel_oa_state", state);
        // 执行批量更新
        detailsMapper.update(updateDetailsWrapper);
    }

    /**
     * 是否允许取消
     *
     * @return Tuple2
     */
    @Override
    public Tuple2<Boolean, String> verifyIsAllowCancel(Long id, BatchCancelVo vo) {
        return Tuple2.of(true, "");
    }

    /**
     * 是否允许取消
     *
     * @return Tuple2
     */
    @Override
    public Tuple2<Boolean, String> verifyGoodsIsAllowCancel(BatchCancelVo vo) {
        return Tuple2.of(true, "");
    }

    /**
     * 取消前执行
     *
     * @return Tuple2
     */
    @Override
    public Tuple2<Boolean, String> beforeCancel(BatchCancelVo vo, Boolean isGoods) {
        return Tuple2.of(true, "");
    }

    /**
     * 批量取消单据明细
     *
     * @return Tuple2<Boolean, String>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Tuple2<Boolean, String> batchCancelDetails(BatchCancelVo vo) {
        Long[] ids = vo.getIds();
        if (ids.length == 0) {
            return Tuple2.of(false, "请选择单据");
        }
        // 表头id
        Long id = vo.getId();
        // 校验是否有下游数据
        Tuple2<Boolean, String> isAllowCancel = verifyGoodsIsAllowCancel(vo);
        if (!isAllowCancel.getFrist()) {
            return Tuple2.of(false, isAllowCancel.getSecond());
        }
        Tuple2<Boolean, String> beforeCancelTuple2 = beforeCancel(vo, true);
        if (!beforeCancelTuple2.getFrist()) {
            return Tuple2.of(false, beforeCancelTuple2.getSecond());
        }
        FunctionEnum functionEnum = FunctionEnum.fromCode(getSupportType().getCode() + "_CANCEL");
        Boolean isIgnoreOa = cancelFlowIsIgnoreOa(functionEnum, vo, true);
        TableNode root = fetchTableNode();
        // 获取明细Mapper
        CustomizeMapper customizeMapper = (CustomizeMapper) ApplicationContextHolder.getBean(root.getChildren().get(0).getMapper());
        // 获取外键
        String foreignKey = root.getChildren().get(0).getForeignKey();
        QueryWrapper<Map<String, Object>> queryWrapper = query();
        queryWrapper.select("DISTINCT " + foreignKey).in("id", ids);
        List<Map<String, Long>> list = customizeMapper.selectMaps(queryWrapper);
        // 单据id
        Long pactId = convertToLong(list.get(0).get(foreignKey));
        Map<String, List<Map<String, Object>>> oldMap = doLoad(pactId);
        // 获取主表数据
        Map<String, Object> masterMap = oldMap.get(root.getTableName()).get(0);
        // 校验状态
        if (!BillStatusEnum.PASS.getCode().equals(masterMap.get(FormConstants.PACT_STATE))) {
            return Tuple2.of(false, "通过的单据才能取消");
        }
        //取消审核中 不允许再次取消
        if (CancelOaStatusEnum.CANCEL_OA_ING.getCode().equals(masterMap.get(FormConstants.CANCEL_OA_STATE))) {
            return Tuple2.of(false, "取消审批中，不能再次取消");
        }
        if (vo.getCancelReason() != null) {
            try {
                // 保存取消原因
                // 构建批量更新的条件
                String cancelReason = genCancelReason(vo.getCancelReason(), true, oldMap, vo);
                UpdateWrapper<BaseEntity> updateWrapper = Wrappers.update();
                updateWrapper.eq("id", id).set("cancel_reason", cancelReason);
                updateByUpdateWrapper(updateWrapper);
            } catch (Exception e) {
                return Tuple2.of(false, "取消原因保存失败:" + e.getMessage());
            }
        }
        if (isIgnoreOa || isAdmin()) {
            return batchChangeDetailsCancelStatus(ids, CancelStatusEnum.CANCEL.getCode());
        } else {
            getWfConfigService().createWf(functionEnum, id, () -> getSourceId(get(id)), null, vo.getGoodsParams());
            // 创建oa流程
            // 更新表头取消状态为oa审核中
            UpdateWrapper<BaseEntity> updateWrapper = Wrappers.update();
            updateWrapper.eq("id", id).set("cancel_oa_state", CancelOaStatusEnum.CANCEL_OA_ING.getCode()).set("cancel_by", LoginHelper.getUserId());
            updateByUpdateWrapper(updateWrapper);
            // 更新明细取消状态
            for (Long goodsId : ids) {
                UpdateWrapper<BaseEntity> goodsupdateWrapper = Wrappers.update();
                goodsupdateWrapper.eq("id", goodsId).set("cancel_oa_state", CancelOaStatusEnum.CANCEL_OA_ING.getCode()).set("cancel_by", LoginHelper.getUserId());
                customizeMapper.update(goodsupdateWrapper);
            }
            return Tuple2.of(true, "创建oa流程成功");
        }
    }

    /**
     * 批量反取消单据明细
     *
     * @param ids 明细id列表
     * @return Tuple2<Boolean, String>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Tuple2<Boolean, String> batchReCancelDetails(Long[] ids) {
        if (!LoginHelper.isAdmin()) {
            return Tuple2.of(false, "请联系管理员反取消单据");
        }
        return batchChangeDetailsCancelStatus(ids, CancelStatusEnum.NORMAL.getCode());
    }

    /**
     * 批量修改单据明细取消状态
     *
     * @param ids      ids
     * @param isCancel 取消状态
     * @return int
     */
    private Tuple2<Boolean, String> batchChangeDetailsCancelStatus(Long[] ids, Integer isCancel) {
        return batchChangeDetailsCancelStatus(ids, isCancel, null);
    }

    /**
     * 批量修改单据明细取消状态
     *
     * @param ids      ids
     * @param isCancel 取消状态
     * @return int
     */
    @Transactional(rollbackFor = Exception.class)
    public Tuple2<Boolean, String> batchChangeDetailsCancelStatus(Long[] ids, Integer isCancel, Map<String, Object> bo) {
        Long userId;
        if (bo != null && bo.get("auditor") != null) {
            String auditor = (String) bo.get("auditor");
            Long auditorId = sysUserService.getUserIdByEmployeeNo(auditor);
            if (auditorId == null) {
                userId = Long.valueOf(auditor);
            } else {
                userId = auditorId;
            }
        } else {
            userId = LoginHelper.getUserId();
        }
        TableNode root = fetchTableNode();
        // 获取明细Mapper
        CustomizeMapper customizeMapper = (CustomizeMapper) ApplicationContextHolder.getBean(root.getChildren().get(0).getMapper());
        // 更新表头
        CustomizeMapper masterTableMapper = (CustomizeMapper) ApplicationContextHolder.getBean(root.getMapper());

        // 获取外键
        String foreignKey = root.getChildren().get(0).getForeignKey();
        QueryWrapper<Map<String, Object>> queryWrapper = query();
        queryWrapper.select("DISTINCT " + foreignKey).in("id", ids);
        List<Map<String, Long>> list = customizeMapper.selectMaps(queryWrapper);
        // 单据id
        Long pactId = convertToLong(list.get(0).get(foreignKey));
        Map<String, List<Map<String, Object>>> oldMap = doLoad(pactId);
        // 获取主表数据
        Map<String, Object> masterMap = oldMap.get(root.getTableName()).get(0);
        // 校验状态
        if (!BillStatusEnum.PASS.getCode().equals(masterMap.get(FormConstants.PACT_STATE))) {
            return Tuple2.of(false, "通过的单据才能取消");
        }


        // 构建批量更新的条件
        UpdateWrapper<CancelClosedIdEntity> updateWrapper = Wrappers.update();
        updateWrapper.in("id", ids).set("is_cancel", isCancel).set("cancel_by", userId).set("cancel_time", LocalDateTime.now());
        // 执行批量更新
        int rowsAffected = customizeMapper.update(updateWrapper);

        // 查出所有明细数据，判断是否更新表头--
        // 需要更新取消状态的列表
        ArrayList<Long> needUpdatedId = new ArrayList<>();
        // 需要更新表头关闭状态为自动关闭的列表
        ArrayList<Long> needUpdatedAutoClosedId = new ArrayList<>();
        // 遍历判断是否需要更新表头--如果 明细行 所有的都变为取消。表头变为取消；
        for (Map<String, Long> map : list) {
            Long id = map.get(foreignKey);
            QueryWrapper<Map<String, Object>> countWrapper = query();
            countWrapper.eq(foreignKey, id);
            // 查询表头下的明细合计
            Long count1 = customizeMapper.selectCount(countWrapper);
            // 查询表头下明细并且取消状态是isCancel的合计
            countWrapper.eq("is_cancel", isCancel);
            Long count2 = customizeMapper.selectCount(countWrapper);
            // 判断是否相同，相同就更新表头
            if (count1.equals(count2)) {
                needUpdatedId.add(id);
            }
            // 取消操作执行--如果明细中即存在取消又有关闭，则表头显示：自动关闭,；
            if (CancelStatusEnum.CANCEL.getCode().equals(isCancel)) {
                countWrapper.and(wrapper -> wrapper.eq("is_closed", CloseStatusEnum.AUTO_CLOSE.getCode()).or().eq("is_closed", CloseStatusEnum.MANUAL_CLOSE.getCode()));
                Long count3 = customizeMapper.selectCount(countWrapper);
                if (count3 > 0) {
                    // 收集需要修改关闭状态为自动关闭的id列表
                    needUpdatedAutoClosedId.add(id);
                }
            }
        }
        if (!needUpdatedAutoClosedId.isEmpty()) {
            // 构建批量更新的条件
            UpdateWrapper<CancelClosedIdEntity> closedMapper = Wrappers.update();
            closedMapper.in("id", needUpdatedAutoClosedId).set("is_closed", CloseStatusEnum.AUTO_CLOSE.getCode()).set("closed_by", userId).set("closed_time", LocalDateTime.now());
            // 执行批量更新
            masterTableMapper.update(closedMapper);
        }
        // 构建批量更新的条件
        if (!needUpdatedId.isEmpty()) {
            UpdateWrapper<BaseEntity> updateWrapper2 = Wrappers.update();
            updateWrapper2.in("id", needUpdatedId).set("is_cancel", isCancel).set("cancel_by", userId).set("cancel_time", LocalDateTime.now());

            // 更新表头状态执行批量更新
            masterTableMapper.update(updateWrapper2);
        }

        Map<String, List<Map<String, Object>>> newMap = doLoad(pactId);
        if (isCancel.equals(CancelStatusEnum.CANCEL.getCode())) {
            Tuple2<Boolean, String> res = writeBack(root, getSupportType(), oldMap, newMap, BehaviorEnum.CANCEL);
            if (!res.getFrist()) {
                return Tuple2.of(false, "取消失败,反写失败");
            }
        } else {
            Tuple2<Boolean, String> res = checkCommon(getSupportType(), oldMap, newMap);
            if (!res.getFrist()) {
                return Tuple2.of(false, "取消失败,校验失败");
            }
            Tuple2<Boolean, String> res2 = writeBack(root, getSupportType(), oldMap, newMap, BehaviorEnum.CANCEL);
            if (!res2.getFrist()) {
                return Tuple2.of(false, "取消失败,反写失败");
            }
        }

        if (rowsAffected > 0) {
            Tuple2<Boolean, String> res2 = afterDetailCancel(ids, isCancel, bo);
            if (!res2.isSuccess()) {
                return Tuple2.of(false, "下游单据取消失败");
            }
            return Tuple2.of(true, "取消成功");
        } else {
            return Tuple2.of(false, "取消失败");
        }
    }

    public Tuple2<Boolean, String> afterDetailCancel(Long[] ids, Integer isCancel, Map<String, Object> bo) {
        return Tuple2.of(true, "");
    }

    /**
     * 批量修改取消审批状态
     *
     * @param ids   ids
     * @param state 取消状态
     * @return int
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchChangeDetailsCancelOaStatus(Long[] ids, Integer state) {
        TableNode root = fetchTableNode();
        // 获取明细Mapper
        CustomizeMapper customizeMapper = (CustomizeMapper) ApplicationContextHolder.getBean(root.getChildren().get(0).getMapper());
        // 更新表头
        CustomizeMapper masterTableMapper = (CustomizeMapper) ApplicationContextHolder.getBean(root.getMapper());
        // 获取外键
        String foreignKey = root.getChildren().get(0).getForeignKey();
        QueryWrapper<Map<String, Object>> queryWrapper = query();
        queryWrapper.select("DISTINCT " + foreignKey).in("id", ids);
        List<Map<String, Long>> list = customizeMapper.selectMaps(queryWrapper);
        // 主表单据ids
        Set<Long> set = list.stream().map(map -> map.get(foreignKey)).collect(Collectors.toSet());
        // 构建批量更新的条件
        UpdateWrapper<CancelClosedIdEntity> updateWrapper = Wrappers.update();
        updateWrapper.in("id", ids).set("cancel_oa_state", state);
        // 执行批量更新
        customizeMapper.update(updateWrapper);
        // 构建表头批量更新的条件
        UpdateWrapper<BaseEntity> masterUpdateWrapper = Wrappers.update();
        masterUpdateWrapper.in("id", set).set("cancel_oa_state", state);
        // 更新表头状态执行批量更新
        masterTableMapper.update(masterUpdateWrapper);
    }

    /**
     * 通用更新方法
     *
     * @param updateWrapper 更新条件构造器
     * @return 影响行数
     */
    @Transactional(rollbackFor = Exception.class)
    public int updateByUpdateWrapper(UpdateWrapper<?> updateWrapper) {
        TableNode root = fetchTableNode();
        CustomizeMapper mapper = (CustomizeMapper) ApplicationContextHolder.getBean(root.getMapper());
        return mapper.update(updateWrapper);
    }

    /**
     * 生成取消原因
     *
     * @param cancelReason 输入的取消原因
     * @param isGoods      是否明细取消
     * @return
     */
    public String genCancelReason(String cancelReason, Boolean isGoods, Map<String, List<Map<String, Object>>> map, BatchCancelVo vo) {
        return cancelReason;
    }

    /**
     * 获取主表数据
     *
     * @param id   主表id
     * @param root TableNode
     * @return Map
     */
    public Map<String, Object> getMasterTableMap(Long id, TableNode root) {
        // 获取主表Class
        Class<?> table = root.getTableClass();
        String fields = fetchFieldAndAlias(table);
        // 主表表名
        String tableName = TableInfoHelper.getTableInfo(table).getTableName();
        // 主表Mapper
        CustomizeMapper customizeMapper = (CustomizeMapper) ApplicationContextHolder.getBean(root.getMapper());

        String sql = SqlConstant.SELECT + ListUtils.makeStringSkipNull(",", fields, root.getJoinColumns()) + SqlConstant.FROM + ListUtils.makeStringSkipNull(" ", tableName, root.getJoinTables()) + SqlConstant.WHERE + root.getTableName() + "." + root.getPrimaryKey() + "='" + id + "'";

        Map<String, Object> data = customizeMapper.query(sql);

        return data;
    }

    /**
     * 根据明细id获取子孙表数据-树形结构
     *
     * @param id   明细id
     * @param root 主表TableNode
     * @return Tuple2
     */
    public Map<String, Object> getGoodsTree(Long id, TableNode root) {
        // 子表TableNode
        TableNode goodsTableNode = root.getGoodsTableNode();
        // 获取子表数据map集合
        Tuple2<Boolean, Map> tupleRes = getGoodsMap(id, root);
        if (!tupleRes.getFrist()) {
            return null;
        }
        Map<String, List<Map<String, Object>>> loadMap = tupleRes.getSecond();
        List<TableNode> list = ListUtils.distinct(root.tree2List());
        Map<String, List<TableNode>> parentTableMap = ListUtils.toMapList(list, TableNode::getParentTable);
        Map<String, Object> viewMap = new HashMap<>();

        List<Map<String, Object>> rows = loadMap.get(goodsTableNode.getTableName());
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                viewMap.putAll(row);
                //状态描述
                appendStateDesc(row, viewMap);
                //添加只读属性
                appendReadonly(row, viewMap);
            }
            addChildrenData(root, loadMap, parentTableMap, viewMap);
        }
        return loadMap.get(goodsTableNode.getTableName()).get(0);
    }

    /**
     * 根据明细id获取子孙表数据
     *
     * @param id   明细id
     * @param root 主表TableNode
     * @return
     */
    public Tuple2<Boolean, Map> getGoodsMap(Long id, TableNode root) {
        Map<String, List<Map<String, Object>>> resultMap = new HashMap<>();
        Optional<TableNode> childOptional = root.getChildren().stream().filter(TableNode::getIsGoods).findFirst();

        // 检查是否找到了符合条件的节点
        if (!childOptional.isPresent()) {
            log.error("没有找到 isGoods 为 true 的节点");
            return Tuple2.of(false, null);
        }
        TableNode goodsTableNode = childOptional.get();
        Class<?> table = goodsTableNode.getTableClass();
        String tableName = TableInfoHelper.getTableInfo(table).getTableName();
        CustomizeMapper customizeMapper = (CustomizeMapper) ApplicationContextHolder.getBean(goodsTableNode.getMapper());

        String fields = fetchFieldAndAlias(table);

        String sql = SqlConstant.SELECT + ListUtils.makeStringSkipNull(",", fields, goodsTableNode.getJoinColumns()) + SqlConstant.FROM + ListUtils.makeStringSkipNull(" ", tableName, goodsTableNode.getJoinTables()) + SqlConstant.WHERE + tableName + ".id" + "='" + id + "'" + ListUtils.makeStringSkipNull(" ", goodsTableNode.getOrderBy());

        List<Map<String, Object>> data = customizeMapper.queryList(sql);

        final List<Map<String, Object>> finalData = data;
        resultMap.compute(tableName, (k, v) -> {
            if (v == null) {
                return finalData;
            } else {
                v.addAll(finalData);
                return v;
            }
        });

        for (Map<String, Object> item : data) {
            recursive(Long.valueOf(item.get(goodsTableNode.getPrimaryKey()).toString()), goodsTableNode, resultMap);
        }
        return Tuple2.of(true, resultMap);
    }

    public List<Map<String, Object>> getDetailList(BasicBO bo) {
        TableNode root = fetchTableNode();
        List<TableNode> tableNodeList = root.tree2List();
        TableNode goods = ListUtils.select(tableNodeList, TableNode::getIsGoods).getFirst();

        Class<?> goodsTable = goods.getTableClass();
        String tableName = goods.getTableName();
        String fields = fetchFieldAndAlias(goodsTable);

        //查子表
        String idIn = ListUtils.toNumberIn(bo.getDetailId().toArray());
        String sql = SqlConstant.SELECT + ListUtils.makeStringSkipNull(",", fields, goods.getJoinColumns()) + SqlConstant.FROM + ListUtils.makeStringSkipNull(" ", tableName, goods.getJoinTables()) + SqlConstant.WHERE + goods.getTableName() + "." + goods.getPrimaryKey() + SqlConstant.IN + idIn;
        CustomizeMapper customizeMapper = (CustomizeMapper) ApplicationContextHolder.getBean(goods.getMapper());
        List<Map<String, Object>> childData = customizeMapper.queryList(sql);

        List<Map<String, Object>> resultList = ListUtils.empty();
        for (Map<String, Object> row : childData) {
            Map<String, List<Map<String, Object>>> resultMap = MapUtils.empty();
            //获取下一级表的数据
            recursive(Long.valueOf(row.get(goods.getPrimaryKey()).toString()), goods, resultMap);

            //将下级结果塞到子表
            row.putAll(resultMap);
            //将子表数据添加到返回结果
            resultList.add(row);
        }

        return resultList;
    }

    public Map<String, Object> getDetailListWithHead(BasicBO bo) {
        TableNode root = fetchTableNode();
        List<TableNode> tableNodeList = root.tree2List();
        TableNode goods = ListUtils.select(tableNodeList, TableNode::getIsGoods).getFirst();

        Class<?> goodsTable = goods.getTableClass();
        String goodsTableName = goods.getTableName();
        String fields = fetchFieldAndAlias(goodsTable);

        //查子表
        String idIn = ListUtils.toNumberIn(bo.getDetailId().toArray());
        String sql = SqlConstant.SELECT + ListUtils.makeStringSkipNull(",", fields, goods.getJoinColumns()) + SqlConstant.FROM + ListUtils.makeStringSkipNull(" ", goodsTableName, goods.getJoinTables()) + SqlConstant.WHERE + goods.getTableName() + "." + goods.getPrimaryKey() + SqlConstant.IN + idIn;
        CustomizeMapper customizeMapper = (CustomizeMapper) ApplicationContextHolder.getBean(goods.getMapper());
        List<Map<String, Object>> childData = customizeMapper.queryList(sql);

        //提取主表ID
        String foreignKey = goods.getForeignKey();
        List<Object> mainIds = ListUtils.collect(childData, item -> item.get(StrUtil.toCamelCase(foreignKey)));
        //只取一个表头
        Long mainId = (Long) ListUtils.safeGetFirst(mainIds);
        Map<String, Object> viewMap = init(mainId);
        //剔除明细子表数据
        viewMap.remove(goodsTableName);

        //将明细数据放到viewMap
        for (Map<String, Object> row : childData) {
            List<Map<String, Object>> resultList = ListUtils.empty();

            Map<String, List<Map<String, Object>>> resultMap = MapUtils.empty();
            //获取下一级表的数据
            recursive(Long.valueOf(row.get(goods.getPrimaryKey()).toString()), goods, resultMap);
            //将下级结果塞到子表
            row.putAll(resultMap);
            row.put(StrUtil.toCamelCase(foreignKey), mainId);
            //将子表数据添加到返回结果
            resultList.add(row);

            //将resultList放到viewMap
            viewMap.compute(goodsTableName, (k, v) -> {
                if (v == null) {
                    return resultList;
                } else {
                    List<Map<String, Object>> temp = (List<Map<String, Object>>) v;
                    temp.addAll(resultList);
                    return temp;
                }
            });
        }

        return viewMap;
    }


    /**
     * 清除表头数据的通用字段
     *
     * @param row
     */
    public void refreshMainTableCommonFields(Map<String, Object> row) {
        row.put(FormConstants.CREATE_BY, null);
        row.put(FormConstants.CREATE_TIME, null);
        row.put(FormConstants.PACT_DATE, LocalDate.now());
        row.put(FormConstants.UPDATE_BY, null);
        row.put(FormConstants.UPDATE_TIME, null);
        row.put(FormConstants.CODE, null);
        row.put(FormConstants.STATUS, BillStatusEnum.DRAFT.getCode());
        row.put(FormConstants.READONLY, 0);
        row.put(FormConstants.PACT_STATE_DESC, BillStatusEnum.DRAFT.getDesc());
        row.put(FormConstants.IS_CLOSED, null);
        row.put(FormConstants.CLOSED_BY, null);
        row.put(FormConstants.CLOSED_TIME, null);
        row.put(FormConstants.IS_CANCEL, null);
        row.put(FormConstants.CANCEL_BY, null);
        row.put(FormConstants.CANCEL_TIME, null);
        row.put(FormConstants.AP_LEVEL, null);
        row.put(FormConstants.REQUEST_ID, null);
        row.put(FormConstants.AUDITOR, null);
        row.put(FormConstants.AUDIT_TIME, null);
        row.put(FormConstants.SYNC_STATUS, null);
        row.put(FormConstants.RESPONSE_PACT_CODE, null);
        row.put(FormConstants.IS_PLAN_REVIEW, null);
        row.put(FormConstants.IS_TECH_REVIEW, null);
        row.put(FormConstants.IS_INIT, null);
        row.put(FormConstants.ERP_ID, null);
        row.put(FormConstants.ERP_CODE, null);
        row.put(FormConstants.APPROVAL_OPINIONS, null);
        row.put(FormConstants.IS_CHECK, null);
        row.put(FormConstants.CHECK_BY, null);
        row.put(FormConstants.CHECK_TIME, null);
        row.put(FormConstants.IS_PAPER_CONT, null);
        row.put("changeState", null);
        row.put("cancelOaState", null);
        row.put("cancelReason", null);
        refreshFieldOnCopyMain(null, row);
    }

    /**
     * 清除子表数据的通用字段
     *
     * @param row
     */
    public void refreshSonTableCommonFields(Map<String, Object> row) {
        row.put(FormConstants.ERP_ID, null);
        row.put(FormConstants.ERP_CODE, null);
        row.put(FormConstants.IS_PLAN_REVIEW, null);
        row.put(FormConstants.IS_TECH_REVIEW, null);
        row.put("cancelOaState", null);
        row.put("cancelReason", null);
        refreshFieldOnCopyGoods(null, row);
    }

    /**
     * 分发
     *
     * @param id id
     */
    public void sendApiInfo(Long id) {
        sendApiInfo(id, doLoad(id), WorkTimingEnum.AFTER_ACTIVATION);
    }

    /**
     * 分发
     *
     * @param id id
     */
    public void sendApiInfo(Long id, WorkTimingEnum workTimingEnum) {
        sendApiInfo(id, doLoad(id), workTimingEnum);
    }

    /**
     * 分发
     *
     * @param id
     * @param dbNewData
     */
    public void sendApiInfo(Long id, Map<String, List<Map<String, Object>>> dbNewData, WorkTimingEnum workTimingEnum) {
        TableNode root = fetchTableNode();
        new Thread(() -> {
            try {
                IInterfaceHelper _interfaceHelp = getInterfaceHelper();
                _interfaceHelp.setAPIInfo(dbNewData, root, id, workTimingEnum.getCode());
            } catch (Exception ex) {
                log.error("分发异常", ex);
            }
        }).start();
    }

    /**
     * 获取子合同和母合同对比数据
     *
     * @param beforeChangeId 变更单id
     * @param sourceId       原始单据id
     * @return map
     */
    public Map getComparisonData(Long sourceId, Long beforeChangeId) {
        // 子表名称
        String subTableName = fetchTableNode().getGoodsTableNode().getTableName();
        // 变更前数据
        Map<String, Object> beforeChangeData = init(beforeChangeId);
        beforeChangeData.put("readonly", 1);
        // 变更后数据
        Map<String, Object> afterChangeData = init(sourceId);
        // 获取字段变更配置
        return ApplicationContextHolder.getBean(IChangeFieldService.class).getFilteredChangeFieldList(getSupportType(), beforeChangeData, afterChangeData, subTableName);
    }

    /**
     * 检查功能
     *
     * @param ids
     * @return
     */
    public Tuple2<Boolean, String> check(List<Long> ids, Integer isCheck) {
        TableNode root = fetchTableNode();
        // 更新表头
        CustomizeMapper masterTableMapper = (CustomizeMapper) ApplicationContextHolder.getBean(root.getMapper());
        // 执行批量更新
        UpdateWrapper<BaseEntity> updateWrapper = Wrappers.update();
        if (isCheck == 1) {
            updateWrapper.in("id", ids).set("is_check", isCheck).set("check_by", LoginHelper.getUserId()).set("check_time", LocalDateTime.now());
        } else {
            updateWrapper.in("id", ids).set("is_check", isCheck).set("check_by", LoginHelper.getUserId());
        }
        // 更新表头状态执行批量更新
        masterTableMapper.update(updateWrapper);
        return Tuple2.of(true, "");
    }

    /**
     * 检查功能
     *
     * @param ids
     * @return
     */
    public Tuple2<Boolean, String> check(List<Long> ids) {
        check(ids, 1);
        return Tuple2.of(true, "");
    }

    /**
     * 反检查功能
     *
     * @param ids
     * @return
     */
    public Tuple2<Boolean, String> reCheck(List<Long> ids) {
        check(ids, 0);
        return Tuple2.of(true, "");
    }

    /**
     * 此方法可以返回实体的字段名
     * <p>
     * 该方法委托给LambdaFieldUtils.getColumnName处理，实现代码复用。
     * </p>
     *
     * @param fn
     * @param <T>
     * @return
     */
    public static <T> String getColumnName(SFunction<T, ?> fn) {
        return LambdaFieldUtils.getColumnName(fn);
    }
    public static <T> String getFieldName(SFunction<T, ?> fn) {
        return LambdaFieldUtils.getFieldName(fn);
    }
}
