package com.wms.common.interceptor;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * SQL注入检测拦截器
 * 基于特征评分机制，精准匹配SQL注入场景
 *
 * @author Lion Li
 */
@Slf4j
@Intercepts({
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class})
})
public class SqlInjectionInterceptor implements Interceptor {

    private static final int DEFAULT_RISK_THRESHOLD = 3;
    private static final int MAX_CACHE_SIZE = 1000;

    // ======================== 一级特征：高危，单条命中即判定注入 ========================

    private static final Pattern STACKED_QUERY = Pattern.compile(
        "(?i);\\s*(drop|delete|insert|update|alter|create|truncate|exec|execute)\\b"
    );

    private static final Pattern COMMENT_BYPASS = Pattern.compile(
        "(?i)(/\\*!\\d|/\\*!\\s*select|/\\*!\\s*union|/\\*!\\s*insert|/\\*!\\s*update|/\\*!\\s*delete)"
    );

    private static final Pattern SYSTEM_TABLE_ACCESS = Pattern.compile(
        "(?i)\\b(information_schema|mysql\\.|sys\\.|performance_schema|master\\.\\.|msdb\\.\\.)\\b"
    );

    private static final Pattern XP_CMDSHELL = Pattern.compile(
        "(?i)\\b(xp_cmdshell|sp_executesql|sp_execute_external_script|xp_regread|xp_regwrite)\\b"
    );

    // ======================== 二级特征：中危，累计命中阈值判定注入 ========================

    private static final Pattern TAUTOLOGY = Pattern.compile(
        "(?i)\\b(or|and)\\s+['\"]?\\d+['\"]?\\s*=\\s*['\"]?\\d+['\"]?"
    );

    private static final Pattern UNION_SELECT = Pattern.compile(
        "(?i)\\bunion\\b\\s+(all\\s+)?\\bselect\\b"
    );

    private static final Pattern TIME_BASED = Pattern.compile(
        "(?i)\\b(sleep\\s*\\(|benchmark\\s*\\(|waitfor\\s+delay|pg_sleep\\s*\\(|dbms_lock\\.sleep)\\b"
    );

    private static final Pattern INFO_COLLECTION = Pattern.compile(
        "(?i)\\b(version\\s*\\(\\s*\\)|database\\s*\\(\\s*\\)|user\\s*\\(\\s*\\)|current_user|@@version|@@servername|@@hostname|@@datadir)\\b"
    );

    private static final Pattern SUSPICIOUS_COMMENT = Pattern.compile(
        "(?i)(--\\s+(?:select|insert|update|delete|drop|alter|create|union|exec|execute|or\\s+1=1)|/\\*(?!\\+)[^!].*?(?:select|union|insert|update|delete|drop|exec).*?\\*/)"
    );

    private static final Pattern HEX_ENCODING = Pattern.compile(
        "(?i)\\b0x[0-9a-f]{4,}"
    );

    private static final Pattern CHAR_ENCODING = Pattern.compile(
        "(?i)\\bchar\\s*\\(\\s*\\d+"
    );

    private static final Pattern LOAD_FILE = Pattern.compile(
        "(?i)\\b(load_file\\s*\\(|into\\s+outfile|into\\s+dumpfile|utl_file\\.|utl_http\\.|utl_inaddr\\.)\\b"
    );

    private static final Pattern ERROR_BASED = Pattern.compile(
        "(?i)\\b(extractvalue\\s*\\(|updatexml\\s*\\()\\b"
    );

    private static final Pattern CONDITIONAL_BLIND = Pattern.compile(
        "(?i)\\b(if\\s*\\(.*(?:sleep|benchmark|waitfor)\\b|case\\s+when.*(?:sleep|benchmark|waitfor)\\b)"
    );

    // ======================== 三级特征：低危，辅助判定 ========================

    private static final Pattern SEMICOLON_SELECT = Pattern.compile(
        "(?i);\\s*\\bselect\\b"
    );

    private static final Pattern DDL_IN_DML = Pattern.compile(
        "(?i)\\b(drop\\s+table|drop\\s+database|alter\\s+table|truncate\\s+table|create\\s+table)\\b"
    );

    private static final Pattern PARAM_DANGEROUS = Pattern.compile(
        "(?i)\\b(or|and)\\s+['\"]?\\d+['\"]?\\s*=\\s*['\"]?\\d+['\"]?|\\bunion\\b\\s+(all\\s+)?\\bselect\\b|\\bsleep\\s*\\(|\\bbenchmark\\s*\\(|;\\s*(?:drop|delete|insert|update|alter|create|truncate|exec|execute)\\b|\\bload_file\\s*\\(|\\bextractvalue\\s*\\(|\\bupdatexml\\s*\\("
    );

    private volatile int riskThreshold = DEFAULT_RISK_THRESHOLD;
    private boolean enabled = true;
    private boolean logWarning = true;
    private boolean throwException = false;

    private final Map<String, Boolean> sqlCache = new ConcurrentHashMap<>();
    private final Set<String> safeMappedStatementIds = ConcurrentHashMap.newKeySet();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if (!enabled) {
            return invocation.proceed();
        }

        log.info("执行sql注入检测开始");


        Object[] args = invocation.getArgs();
        MappedStatement mappedStatement = (MappedStatement) args[0];
        Object parameter = args.length > 1 ? args[1] : null;

        String msId = mappedStatement.getId();
        if (safeMappedStatementIds.contains(msId)) {
            return invocation.proceed();
        }

        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        String sql = boundSql.getSql();

        if (sql == null || sql.isEmpty()) {
            return invocation.proceed();
        }

        String cacheKey = buildCacheKey(sql);
        Boolean cached = sqlCache.get(cacheKey);
        if (cached != null) {
            if (cached) {
                handleInjectionDetected(sql);
            }
            return invocation.proceed();
        }

        boolean injectionDetected = detectSqlInjection(sql, boundSql.getParameterMappings(), parameter);

        cacheResult(cacheKey, injectionDetected);

        if (!injectionDetected && SqlCommandType.SELECT.equals(mappedStatement.getSqlCommandType())) {
            safeMappedStatementIds.add(msId);
        }

        if (injectionDetected) {
            handleInjectionDetected(sql);
        }

        return invocation.proceed();
    }

    private void handleInjectionDetected(String sql) {
        String warningMsg = "检测到SQL注入风险，SQL: " + sql;
        if (logWarning) {
            log.info(warningMsg);
        }
        if (throwException) {
            throw new SecurityException("SQL注入检测失败: " + warningMsg);
        }
    }

    private String buildCacheKey(String sql) {
        if (sql.length() <= 200) {
            return sql;
        }
        return sql.substring(0, 100) + "|" + sql.length() + "|" + sql.substring(sql.length() - 100);
    }

    private void cacheResult(String cacheKey, boolean result) {
        if (sqlCache.size() >= MAX_CACHE_SIZE) {
            Iterator<String> it = sqlCache.keySet().iterator();
            int removeCount = MAX_CACHE_SIZE / 4;
            for (int i = 0; i < removeCount && it.hasNext(); i++) {
                it.next();
                it.remove();
            }
        }
        sqlCache.put(cacheKey, result);
    }

    /**
     * 基于评分机制的SQL注入检测
     */
    private boolean detectSqlInjection(String sql, List<?> parameterMappings, Object parameter) {
        try {
            int score = 0;

            if (STACKED_QUERY.matcher(sql).find()) {
                log.info("检测到堆叠查询注入: {}", truncate(sql));
                return true;
            }
            if (COMMENT_BYPASS.matcher(sql).find()) {
                log.info("检测到注释绕过注入: {}", truncate(sql));
                return true;
            }
            if (SYSTEM_TABLE_ACCESS.matcher(sql).find()) {
                log.info("检测到系统表访问: {}", truncate(sql));
                return true;
            }
            if (XP_CMDSHELL.matcher(sql).find()) {
                log.info("检测到命令执行注入: {}", truncate(sql));
                return true;
            }

            if (TAUTOLOGY.matcher(sql).find()) {
                score += 2;
                log.info("检测到永真条件: {}", truncate(sql));
            }
            if (UNION_SELECT.matcher(sql).find()) {
                score += 2;
                log.info("检测到UNION SELECT: {}", truncate(sql));
            }
            if (TIME_BASED.matcher(sql).find()) {
                score += 2;
                log.debug("检测到延时注入函数: {}", truncate(sql));
            }
            if (INFO_COLLECTION.matcher(sql).find()) {
                score += 2;
                log.info("检测到信息收集函数: {}", truncate(sql));
            }
            if (SUSPICIOUS_COMMENT.matcher(sql).find()) {
                score += 1;
                log.info("检测到可疑注释: {}", truncate(sql));
            }
            if (HEX_ENCODING.matcher(sql).find()) {
                score += 1;
                log.info("检测到HEX编码: {}", truncate(sql));
            }
            if (CHAR_ENCODING.matcher(sql).find()) {
                score += 1;
                log.info("检测到CHAR编码: {}", truncate(sql));
            }
            if (LOAD_FILE.matcher(sql).find()) {
                score += 2;
                log.info("检测到文件操作: {}", truncate(sql));
            }
            if (ERROR_BASED.matcher(sql).find()) {
                score += 2;
                log.info("检测到错误注入函数: {}", truncate(sql));
            }
            if (CONDITIONAL_BLIND.matcher(sql).find()) {
                score += 2;
                log.info("检测到条件盲注: {}", truncate(sql));
            }

            if (SEMICOLON_SELECT.matcher(sql).find()) {
                score += 1;
                log.info("检测到分号后SELECT: {}", truncate(sql));
            }
            if (DDL_IN_DML.matcher(sql).find()) {
                score += 2;
                log.info("检测到DDL语句: {}", truncate(sql));
            }

            score += analyzeSqlStructure(sql);

            score += analyzeParameterValues(parameterMappings, parameter);

            if (score >= riskThreshold) {
                log.info("SQL注入风险评分达标, score={}, sql={}", score, truncate(sql));
                return true;
            }

            log.debug("SQL注入检测通过, score={}, sql={}", score, truncate(sql));
            return false;

        } catch (JSQLParserException e) {
            log.error("SQL解析失败: {}", e.getMessage());
            return hasFallbackInjectionSigns(sql);
        } catch (Exception e) {
            log.error("SQL注入检测异常", e);
            return true;
        }
    }

    /**
     * 使用JSqlParser进行SQL结构分析
     */
    private int analyzeSqlStructure(String sql) throws JSQLParserException {
        int score = 0;
        Statement statement = CCJSqlParserUtil.parse(sql);

        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(statement);

        for (String tableName : tableList) {
            if (tableName.contains(" ") || tableName.contains(";")
                || tableName.contains("--") || tableName.contains("/*")) {
                score += 2;
            }
        }

        if (statement instanceof Select) {
            score += analyzeSelect((Select) statement);
        } else if (statement instanceof Insert) {
            score += analyzeInsert((Insert) statement);
        } else if (statement instanceof Update) {
            score += analyzeUpdate((Update) statement);
        } else if (statement instanceof Delete) {
            score += analyzeDelete((Delete) statement);
        }

        return score;
    }

    private int analyzeSelect(Select select) {
        int score = 0;

        if (select.getSelectBody() instanceof SetOperationList) {
            SetOperationList setOpList = (SetOperationList) select.getSelectBody();
            if (setOpList.getOperations() != null && setOpList.getOperations().size() > 2) {
                score += 2;
            }
        }

        if (select.getSelectBody() instanceof PlainSelect) {
            PlainSelect plainSelect = (PlainSelect) select.getSelectBody();

            if (plainSelect.getSelectItems() != null) {
                if (plainSelect.getSelectItems().size() > 30) {
                    score += 1;
                }
                int globalInfoFuncCount = 0;
                for (SelectItem item : plainSelect.getSelectItems()) {
                    String itemStr = item.toString().toLowerCase();
                    if (itemStr.matches("(?i).*\\bversion\\s*\\(.*")) globalInfoFuncCount++;
                    if (itemStr.matches("(?i).*\\bdatabase\\s*\\(.*")) globalInfoFuncCount++;
                    if (itemStr.matches("(?i).*\\buser\\s*\\(.*")) globalInfoFuncCount++;
                }
                if (globalInfoFuncCount >= 2) {
                    score += 2;
                }
            }

            if (plainSelect.getWhere() != null) {
                score += analyzeWhereExpression(plainSelect.getWhere());
            }
        }

        return score;
    }

    private int analyzeInsert(Insert insert) {
        int score = 0;

        if (insert.getSelect() != null) {
            String selectSql = insert.getSelect().toString().toLowerCase();
            if (selectSql.contains("information_schema")
                || selectSql.contains("mysql.")
                || selectSql.contains("sys.")) {
                score += 2;
            }
        }

        if (insert.getItemsList() != null) {
            String valuesStr = insert.getItemsList().toString().toLowerCase();
            if (valuesStr.contains("(select") || valuesStr.contains(" select ")) {
                score += 2;
            }
            if (countOccurrences(valuesStr, "),(") > 10) {
                score += 1;
            }
        }

        return score;
    }

    private int analyzeUpdate(Update update) {
        int score = 0;

        if (update.getUpdateSets() != null) {
            for (net.sf.jsqlparser.statement.update.UpdateSet updateSet : update.getUpdateSets()) {
                if (updateSet.getExpressions() != null) {
                    for (Expression expr : updateSet.getExpressions()) {
                        String exprStr = expr.toString().toLowerCase();
                        if (exprStr.contains("(select") || exprStr.contains(" select ")) {
                            score += 2;
                        }
                    }
                }
            }
        }

        if (update.getWhere() != null) {
            score += analyzeWhereExpression(update.getWhere());
        } else {
            String tableName = update.getTable() != null ? update.getTable().getName().toLowerCase() : "";
            if (!isTempOrConfigTable(tableName)) {
                score += 1;
            }
        }

        return score;
    }

    private int analyzeDelete(Delete delete) {
        int score = 0;

        if (delete.getWhere() != null) {
            score += analyzeWhereExpression(delete.getWhere());
        } else {
            String tableName = delete.getTable() != null ? delete.getTable().getName().toLowerCase() : "";
            if (!isTempOrConfigTable(tableName)) {
                score += 1;
            }
        }

        return score;
    }

    private boolean isTempOrConfigTable(String tableName) {
        if (tableName == null || tableName.isEmpty()) {
            return false;
        }
        return tableName.startsWith("tmp_")
            || tableName.startsWith("temp_")
            || tableName.startsWith("etl_")
            || tableName.endsWith("_temp")
            || tableName.endsWith("_tmp")
            || tableName.contains("config")
            || tableName.contains("sys_config");
    }

    private int analyzeWhereExpression(Expression where) {
        int score = 0;
        String whereStr = where.toString().toLowerCase();

        if (whereStr.contains(" or 1=1") || whereStr.contains(" or '1'='1")) {
            score += 2;
        }
        if (whereStr.contains(" or exists(select") || whereStr.contains(" and (select")) {
            score += 2;
        }

        return score;
    }

    /**
     * 检测参数值中的注入特征
     */
    @SuppressWarnings("unchecked")
    private int analyzeParameterValues(List<?> parameterMappings, Object parameter) {
        if (CollectionUtils.isEmpty(parameterMappings) || parameter == null) {
            return 0;
        }

        int score = 0;

        try {
            if (parameter instanceof Map) {
                Map<String, Object> paramMap = (Map<String, Object>) parameter;
                for (Object value : paramMap.values()) {
                    score += checkParamValue(value);
                }
            } else if (parameter instanceof String) {
                score += checkParamValue(parameter);
            } else if (parameter instanceof Collection) {
                for (Object item : (Collection<?>) parameter) {
                    score += checkParamValue(item);
                }
            } else {
                score += checkPojoParamValue(parameter);
            }
        } catch (Exception e) {
            log.debug("参数值检测异常", e);
        }

        return score;
    }

    private int checkParamValue(Object value) {
        if (value == null) {
            return 0;
        }

        if (!(value instanceof String)) {
            return 0;
        }

        String strValue = (String) value;
        if (strValue.isEmpty()) {
            return 0;
        }

        int score = 0;

        if (PARAM_DANGEROUS.matcher(strValue).find()) {
            score += 2;
        }

        return score;
    }

    private int checkPojoParamValue(Object pojo) {
        if (pojo == null) {
            return 0;
        }

        Class<?> clazz = pojo.getClass();
        if (clazz.getName().startsWith("java.")) {
            return 0;
        }

        int score = 0;

        try {
            for (Field field : getAllFields(clazz)) {
                field.setAccessible(true);
                Object value = field.get(pojo);
                if (value instanceof String) {
                    score += checkParamValue(value);
                }
            }
        } catch (Exception e) {
            log.debug("POJO参数检测异常: {}", e.getMessage());
        }

        return score;
    }

    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    /**
     * 解析失败时的兜底检测
     */
    private boolean hasFallbackInjectionSigns(String sql) {
        if (STACKED_QUERY.matcher(sql).find()) return true;
        if (COMMENT_BYPASS.matcher(sql).find()) return true;
        if (XP_CMDSHELL.matcher(sql).find()) return true;

        int score = 0;
        if (TAUTOLOGY.matcher(sql).find()) score += 2;
        if (UNION_SELECT.matcher(sql).find()) score += 2;
        if (TIME_BASED.matcher(sql).find()) score += 2;
        if (SYSTEM_TABLE_ACCESS.matcher(sql).find()) score += 2;
        if (DDL_IN_DML.matcher(sql).find()) score += 2;
        if (ERROR_BASED.matcher(sql).find()) score += 2;
        if (CONDITIONAL_BLIND.matcher(sql).find()) score += 2;
        if (SUSPICIOUS_COMMENT.matcher(sql).find()) score += 1;
        if (HEX_ENCODING.matcher(sql).find()) score += 1;
        if (CHAR_ENCODING.matcher(sql).find()) score += 1;

        return score >= riskThreshold;
    }

    private int countOccurrences(String text, String pattern) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(pattern, index)) != -1) {
            count++;
            index += pattern.length();
        }
        return count;
    }

    private String truncate(String sql) {
        return sql.length() > 200 ? sql.substring(0, 200) + "..." : sql;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        this.enabled = Boolean.parseBoolean(properties.getProperty("enabled", "true"));
        this.logWarning = Boolean.parseBoolean(properties.getProperty("logWarning", "true"));
        this.throwException = Boolean.parseBoolean(properties.getProperty("throwException", "false"));
        this.riskThreshold = Integer.parseInt(properties.getProperty("riskThreshold", String.valueOf(DEFAULT_RISK_THRESHOLD)));
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isLogWarning() {
        return logWarning;
    }

    public void setLogWarning(boolean logWarning) {
        this.logWarning = logWarning;
    }

    public boolean isThrowException() {
        return throwException;
    }

    public void setThrowException(boolean throwException) {
        this.throwException = throwException;
    }

    public int getRiskThreshold() {
        return riskThreshold;
    }

    public void setRiskThreshold(int riskThreshold) {
        this.riskThreshold = riskThreshold;
    }
}
