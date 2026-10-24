package com.wms.common.thread;

import java.util.Objects;

/**
 * 租户上下文，基于 ThreadLocal 在当前线程中传递租户ID。
 * 典型使用场景：API 拦截器中根据请求来源设置租户ID，后续业务代码中获取租户ID进行数据隔离。
 *
 * @author wms
 */
public class PlatContext {

    private static final ThreadLocal<Integer> TENANT_ID_HOLDER = new ThreadLocal<>();

    /**
     * 羊绒租户ID
     */
    public static final int CASHMERE = 2;

    /**
     * 羊毛租户ID
     */
    public static final int WOOL = 1;

    private PlatContext() {
    }

    /**
     * 设置当前线程的租户ID
     *
     * @param tenantId 租户ID
     */
    public static void set(Integer tenantId) {
        TENANT_ID_HOLDER.set(tenantId);
    }

    /**
     * 获取当前线程的租户ID，可能为 null
     *
     * @return 租户ID
     */
    public static Integer get() {
        return TENANT_ID_HOLDER.get();
    }

    /**
     * 获取当前线程的租户ID，如果未设置则返回默认值
     *
     * @param defaultTenantId 默认租户ID
     * @return 租户ID
     */
    public static Integer getOrDefault(Integer defaultTenantId) {
        Integer tenantId = TENANT_ID_HOLDER.get();
        return tenantId != null ? tenantId : defaultTenantId;
    }

    /**
     * 判断当前租户是否为羊绒
     */
    public static boolean isCashmere() {
        return Objects.equals(TENANT_ID_HOLDER.get(), CASHMERE);
    }

    /**
     * 判断当前租户是否为羊毛
     */
    public static boolean isWool() {
        return Objects.equals(TENANT_ID_HOLDER.get(), WOOL);
    }

    /**
     * 清除当前线程的租户ID
     */
    public static void clear() {
        TENANT_ID_HOLDER.remove();
    }

}
