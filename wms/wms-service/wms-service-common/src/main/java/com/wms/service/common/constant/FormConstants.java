package com.wms.service.common.constant;

/**
 * 表单常量类
 * 集中管理动态表单相关的常量定义
 */
public final class FormConstants {

    private FormConstants() {
        // 防止实例化
        throw new UnsupportedOperationException("FormConstants is a utility class and cannot be instantiated");
    }

    // ================================
    // 字段名称常量
    // ================================

    /** 主键字段名 */
    public static final String ID = "id";

    /** 编码字段名 */
    public static final String CODE = "code";

    /** 状态字段名 */
    public static final String STATUS = "pactState";

    /** 合同日期字段名 */
    public static final String PACT_DATE = "pactDate";

    /** 创建人字段名 */
    public static final String CREATE_BY = "createBy";

    /** 创建时间字段名 */
    public static final String CREATE_TIME = "createTime";

    /** 更新人字段名 */
    public static final String UPDATE_BY = "updateBy";

    /** 更新时间字段名 */
    public static final String UPDATE_TIME = "updateTime";

    /** 审核人字段名 */
    public static final String AUDITOR = "auditor";

    /** 审核时间字段名 */
    public static final String AUDIT_TIME = "auditTime";

    /** 是否已审核字段名 */
    public static final String IS_AUDITED = "isAudited";

    /** 状态描述字段名 */
    public static final String PACT_STATE_DESC = "pactStateDesc";

    /** 只读字段名 */
    public static final String READONLY = "readonly";

    /** 是否关闭字段名 */
    public static final String IS_CLOSED = "isClosed";

    /** 关闭人字段名 */
    public static final String CLOSED_BY = "closedBy";

    /** 关闭时间字段名 */
    public static final String CLOSED_TIME = "closeTime";

    /** 是否取消字段名 */
    public static final String IS_CANCEL = "isCancel";

    /** 取消人字段名 */
    public static final String CANCEL_BY = "cancelBy";

    /** 取消OA状态字段名 */
    public static final String CANCEL_OA_STATE = "cancelOaState";

    /** 取消时间字段名 */
    public static final String CANCEL_TIME = "cancelTime";

    /** 同步状态字段名 */
    public static final String SYNC_STATUS = "syncStatus";

    /** 审批级别字段名 */
    public static final String AP_LEVEL = "aplevel";

    /** 请求ID字段名 */
    public static final String REQUEST_ID = "requestId";

    /** 合同状态字段名 */
    public static final String PACT_STATE = "pactState";

    /** 是否技术评审字段名 */
    public static final String IS_TECH_REVIEW = "isTechReview";

    /** 是否计划评审字段名 */
    public static final String IS_PLAN_REVIEW = "isPlanReview";

    /** 是否初始化字段名 */
    public static final String IS_INIT = "isInit";

    /** ERP ID字段名 */
    public static final String ERP_ID = "erpId";

    /** ERP 编码字段名 */
    public static final String ERP_CODE = "erpCode";

    /** 审批意见字段名 */
    public static final String APPROVAL_OPINIONS = "approvalOpinions";

    /** 是否检查字段名 */
    public static final String IS_CHECK = "isCheck";

    /** 检查人字段名 */
    public static final String CHECK_BY = "checkBy";

    /** 检查时间字段名 */
    public static final String CHECK_TIME = "checkTime";

    /** 是否纸质合同字段名 */
    public static final String IS_PAPER_CONT = "isPaperCont";

    /** 响应合同编码字段名 */
    public static final String RESPONSE_PACT_CODE = "responsePactCode";

    /** 变更类型字段名 */
    public static final String CHANGE_TYPE = "changeType";

    // ================================
    // 业务常量
    // ================================

    /** 字典键前缀 */
    public static final String DICT_KEY = "__dict__";

    /** 变更说明标志位 */
    public static final String CHANGE_INFO_FLAG = "---------------------------";

    // ================================
    // 默认值常量
    // ================================

    /** 默认分页大小 */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /** 最大分页大小 */
    public static final int MAX_PAGE_SIZE = 1000;

    /** 默认缓存时间（秒） */
    public static final int DEFAULT_CACHE_TIMEOUT = 300;

    /** 最大批量操作数量 */
    public static final int MAX_BATCH_SIZE = 100;

    // ================================
    // 错误消息常量
    // ================================

    /** 表单数据为空错误消息 */
    public static final String ERROR_FORM_DATA_EMPTY = "表单数据不能为空";

    /** 表单验证失败错误消息 */
    public static final String ERROR_FORM_VALIDATION_FAILED = "表单验证失败";

    /** 表单保存失败错误消息 */
    public static final String ERROR_FORM_SAVE_FAILED = "表单保存失败";

    /** 表单不存在错误消息 */
    public static final String ERROR_FORM_NOT_FOUND = "表单不存在";

    /** 无权限操作错误消息 */
    public static final String ERROR_NO_PERMISSION = "无权限执行此操作";

    // ================================
    // 正则表达式常量
    // ================================

    /** 编码格式正则（字母数字下划线） */
    public static final String REGEX_CODE = "^[a-zA-Z0-9_]+$";

    /** 日期格式正则（yyyy-MM-dd） */
    public static final String REGEX_DATE = "^\\d{4}-\\d{2}-\\d{2}$";

    /** 时间格式正则（HH:mm:ss） */
    public static final String REGEX_TIME = "^\\d{2}:\\d{2}:\\d{2}$";

    /** 邮箱格式正则 */
    public static final String REGEX_EMAIL = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    /** 手机号格式正则 */
    public static final String REGEX_PHONE = "^1[3-9]\\d{9}$";

    // ================================
    // 配置常量
    // ================================

    /** 默认字符集 */
    public static final String DEFAULT_CHARSET = "UTF-8";

    /** 默认日期格式 */
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    /** 默认时间格式 */
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    /** 默认日期时间格式 */
    public static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
}
