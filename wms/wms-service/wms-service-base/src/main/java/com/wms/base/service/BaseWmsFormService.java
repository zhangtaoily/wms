package com.wms.base.service;

import com.wms.common.utils.MapUtils;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wms.common.api.BasicBO;
import com.wms.common.core.domain.IdEntity;
import com.wms.common.helper.LoginHelper;
import com.wms.common.utils.Tuple2;
import com.wms.mapper.CustomizeMapper;
import com.wms.service.common.service.AbstractDynamicsFormService;

import java.util.Date;
import java.util.Map;

/**
 * 基础数据动态表单服务基类
 * 引擎不自动填充审计字段，此处统一注入 creator/updater
 */
public abstract class BaseWmsFormService<E extends IdEntity, M extends CustomizeMapper<E>> extends AbstractDynamicsFormService<E, M> {

    /** 立库(自营) 仓库类型 */
    protected static final String WH_TYPE_LIKU = "立库(自营)";

    @Override
    public Tuple2<Integer, Object> save(BasicBO bo) {
        fillAudit(bo.getData());
        preserveCreateAudit(bo.getData());
        return super.save(bo);
    }

    @Override
    public Tuple2<Boolean, Object> batchSave(BasicBO bo) {
        if (bo.getDatas() != null) {
            bo.getDatas().forEach(d -> {
                fillAudit(d);
                preserveCreateAudit(d);
            });
        }
        return super.batchSave(bo);
    }

    /**
     * 更新前回填原记录 createTime/creator：
     * 实体字段全量 SET，data 缺失这些键会将其置空，触发 NOT NULL 约束
     */
    private void preserveCreateAudit(Map<String, Object> data) {
        Long id = parseId(data.get("id"));
        if (id == null) {
            return;
        }
        Object old = get(id);
        if (old == null) {
            return;
        }
        copyAuditIfAbsent(data, old, "createTime");
        copyAuditIfAbsent(data, old, "creator");
    }

    private static void copyAuditIfAbsent(Map<String, Object> data, Object bean, String prop) {
        if (data.containsKey(prop)) {
            return;
        }
        try {
            String getter = "get" + Character.toUpperCase(prop.charAt(0)) + prop.substring(1);
            Object v = bean.getClass().getMethod(getter).invoke(bean);
            if (v != null) {
                data.put(prop, v);
            }
        } catch (Exception ignore) {
            // 实体无该属性时跳过
        }
    }

    /** 基础数据无单据初始化绑定，返回空避免 add 模式带出单据默认键 */
    @Override
    public Map<String, Object> initBind() {
        return MapUtils.empty();
    }

    /**
     * 新增填 creator，更新填 updater（键为 snake_case，与表单数据键一致）
     */
    static void fillAudit(Map<String, Object> data) {
        if (data == null) {
            return;
        }
        boolean isUpdate = parseId(data.get("id")) != null;
        try {
            if (isUpdate) {
                data.put("updater", LoginHelper.getUsername());
                data.put("updateTime", new Date());
            } else {
                data.put("creator", LoginHelper.getUsername());
            }
        } catch (Exception ignore) {
            // 无登录上下文（系统任务等）时跳过审计填充
        }
    }

    protected static Long parseId(Object idVal) {
        if (idVal == null || StrUtil.isBlank(String.valueOf(idVal))) {
            return null;
        }
        return Long.valueOf(String.valueOf(idVal));
    }

    /**
     * 唯一性校验：column 值是否已存在（排除自身 id）
     */
    protected <T extends IdEntity> boolean exists(CustomizeMapper<T> mapper, String column, Object value, Long excludeId) {
        QueryWrapper<T> qw = new QueryWrapper<>();
        qw.eq(column, value);
        if (excludeId != null) {
            qw.ne("id", excludeId);
        }
        return mapper.selectCount(qw) > 0;
    }
}
