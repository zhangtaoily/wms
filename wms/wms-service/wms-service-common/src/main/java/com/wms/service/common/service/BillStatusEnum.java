package com.wms.service.common.service;

import cn.hutool.core.util.ReflectUtil;
import com.wms.common.core.service.UserService;
import com.wms.common.helper.LoginHelper;
import com.wms.common.utils.ApplicationContextHolder;
import com.wms.common.utils.ListUtils;
import com.wms.common.utils.Tuple2;
import com.wms.mapper.CustomizeMapper;
import com.wms.service.common.constant.FormConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.util.Map;

import static com.wms.service.common.service.AbstractDynamicsFormService.getStatusFieldValue;

@Getter
@AllArgsConstructor
public enum BillStatusEnum {

    DRAFT(0, "草稿", "暂存") {
        @Override
        public Tuple2<Boolean, String> execute(TableNode root, Long id) {
            return null;
        }

        @Override
        public Tuple2<Boolean, String> execute(TableNode root, Long id, Map<String, Object> bo) {
            return null;
        }
    }, CREATE(1, "制单中", "创建/撤回") {
        @Override
        public Tuple2<Boolean, String> execute(TableNode root, Long id) {
            //查询单据当前状态
            CustomizeMapper customizeMapper = (CustomizeMapper) ApplicationContextHolder.getBean(root.getMapper());
            Tuple2<Object, Object> entity = getStatusFieldValue(root, id);
            if (entity.getFrist() != null) {
                // 显式列更新（含置空列）：全局 updateStrategy=NOT_NULL 下 updateById 不再携带 null 字段，
                // 撤回需清空的 auditor/audit_time/response_pact_code 改由 UpdateWrapper 显式 set
                com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<Object> uw =
                    new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();
                uw.eq(root.getPrimaryKey(), id)
                    .set("status", BillStatusEnum.CREATE.getCode());
                if (FieldUtils.getField(root.getTableClass(), FormConstants.AUDITOR, true) != null) {
                    uw.set("auditor", null);
                }
                if (FieldUtils.getField(root.getTableClass(), FormConstants.AUDIT_TIME, true) != null) {
                    uw.set("audit_time", null);
                }
                boolean hasField = FieldUtils.getField(root.getTableClass(), FormConstants.SYNC_STATUS, true) != null;
                if (hasField) {
                    uw.set("sync_status", 2);
                }
                boolean responsePactCodeHasField = FieldUtils.getField(root.getTableClass(), FormConstants.RESPONSE_PACT_CODE, true) != null;
                if (responsePactCodeHasField) {
                    uw.set("response_pact_code", null);
                }
                customizeMapper.update(uw);

                return Tuple2.of(true, "操作成功");
            }
            return Tuple2.of(false, "失败");
        }

        @Override
        public Tuple2<Boolean, String> execute(TableNode root, Long id, Map<String, Object> bo) {
            return null;
        }
    },

    AUDIT(2, "审核中", "提交操作") {
        @Override
        public Tuple2<Boolean, String> execute(TableNode root, Long id) {
            //查询单据当前状态
            CustomizeMapper customizeMapper = (CustomizeMapper) ApplicationContextHolder.getBean(root.getMapper());
            Tuple2<Object, Object> entity = getStatusFieldValue(root, id);
            if (entity.getFrist() != null) {
                //从制单中转审核中
                if (BillStatusEnum.CREATE.getCode().equals(entity.getSecond())) {
                    ReflectUtil.setFieldValue(entity.getFrist(), FormConstants.STATUS, BillStatusEnum.AUDIT.getCode());
                    customizeMapper.updateById(entity.getFrist());
                    return Tuple2.of(true, "操作成功");
                }
            }
            return Tuple2.of(false, "失败");
        }

        @Override
        public Tuple2<Boolean, String> execute(TableNode root, Long id, Map<String, Object> bo) {
            return null;
        }
    },

    PASS(3, "通过", "审批OK") {
        @Override
        public Tuple2<Boolean, String> execute(TableNode root, Long id) {
            //查询单据当前状态
            CustomizeMapper customizeMapper = (CustomizeMapper) ApplicationContextHolder.getBean(root.getMapper());
            Tuple2<Object, Object> entity = getStatusFieldValue(root, id);
            if (entity.getFrist() != null) {
                //审批OK
                if (BillStatusEnum.AUDIT.getCode().equals(entity.getSecond()) || BillStatusEnum.CREATE.getCode().equals(entity.getSecond())) {
                    ReflectUtil.setFieldValue(entity.getFrist(), FormConstants.STATUS, BillStatusEnum.PASS.getCode());
                    ReflectUtil.setFieldValue(entity.getFrist(), FormConstants.AUDITOR, LoginHelper.getUserId());
                    ReflectUtil.setFieldValue(entity.getFrist(), FormConstants.AUDIT_TIME, LocalDateTime.now());
                    boolean hasField = FieldUtils.getField(entity.getClass(), FormConstants.IS_AUDITED, true) != null;
                    if (hasField) {
                        ReflectUtil.setFieldValue(entity.getFrist(), FormConstants.IS_AUDITED, 1);
                    }
                    customizeMapper.updateById(entity.getFrist());
                    return Tuple2.of(true, "操作成功");
                }
            }
            return Tuple2.of(false, "失败");
        }

        @SneakyThrows
        @Override
        public Tuple2<Boolean, String> execute(TableNode root, Long id, Map<String, Object> bo) {
            //查询单据当前状态
            CustomizeMapper customizeMapper = (CustomizeMapper) ApplicationContextHolder.getBean(root.getMapper());
            Tuple2<Object, Object> entity = getStatusFieldValue(root, id);
            if (entity.getFrist() != null) {
                //审批OK
                if (BillStatusEnum.AUDIT.getCode().equals(entity.getSecond()) || BillStatusEnum.CREATE.getCode().equals(entity.getSecond())) {
                    // 获取OA传递的员工工号
                    String auditor = URLDecoder.decode((String) bo.get("auditor"), "UTF-8");
                    // 根据工号获取用户id
                    UserService userService = ApplicationContextHolder.getBean(UserService.class);
                    Long userId = userService.getUserIdByEmployeeNo(auditor);
                    ReflectUtil.setFieldValue(entity.getFrist(), FormConstants.STATUS, BillStatusEnum.PASS.getCode());
                    ReflectUtil.setFieldValue(entity.getFrist(), FormConstants.AUDITOR, userId != null ? userId : auditor);
                    ReflectUtil.setFieldValue(entity.getFrist(), FormConstants.AUDIT_TIME, LocalDateTime.now());
                    boolean hasField = FieldUtils.getField(entity.getClass(), FormConstants.IS_AUDITED, true) != null;
                    if (hasField) {
                        ReflectUtil.setFieldValue(entity.getFrist(), FormConstants.IS_AUDITED, 1);
                    }
                    customizeMapper.updateById(entity.getFrist());
                    return Tuple2.of(true, "操作成功");
                }
            }
            return Tuple2.of(false, "失败");
        }
    },

    REJECT(4, "驳回", "审批NO") {
        @Override
        public Tuple2<Boolean, String> execute(TableNode root, Long id) {
            //查询单据当前状态
            CustomizeMapper customizeMapper = (CustomizeMapper) ApplicationContextHolder.getBean(root.getMapper());
            Tuple2<Object, Object> entity = getStatusFieldValue(root, id);
            if (entity.getFrist() != null) {
                //审批OK
                if (BillStatusEnum.AUDIT.getCode().equals(entity.getSecond())) {
                    ReflectUtil.setFieldValue(entity.getFrist(), FormConstants.STATUS, BillStatusEnum.REJECT.getCode());
                    ReflectUtil.setFieldValue(entity.getFrist(), FormConstants.AUDITOR, LoginHelper.getUserId());
                    ReflectUtil.setFieldValue(entity.getFrist(), FormConstants.AUDIT_TIME, LocalDateTime.now());
                    boolean hasField = FieldUtils.getField(entity.getClass(), FormConstants.IS_AUDITED, true) != null;
                    if (hasField) {
                        ReflectUtil.setFieldValue(entity.getFrist(), FormConstants.IS_AUDITED, 1);
                    }
                    customizeMapper.updateById(entity.getFrist());
                    return Tuple2.of(true, "操作成功");
                }
            }
            return Tuple2.of(false, "失败");
        }

        @SneakyThrows
        @Override
        public Tuple2<Boolean, String> execute(TableNode root, Long id, Map<String, Object> bo) {
            //查询单据当前状态
            CustomizeMapper customizeMapper = (CustomizeMapper) ApplicationContextHolder.getBean(root.getMapper());
            Tuple2<Object, Object> entity = getStatusFieldValue(root, id);
            if (entity.getFrist() != null) {
                //审批OK
                if (BillStatusEnum.AUDIT.getCode().equals(entity.getSecond())) {
                    String auditor = URLDecoder.decode((String) bo.get("auditor"), "UTF-8");
                    ReflectUtil.setFieldValue(entity.getFrist(), FormConstants.STATUS, BillStatusEnum.REJECT.getCode());
                    ReflectUtil.setFieldValue(entity.getFrist(), FormConstants.AUDITOR, auditor);
                    ReflectUtil.setFieldValue(entity.getFrist(), FormConstants.AUDIT_TIME, LocalDateTime.now());
                    boolean hasField = FieldUtils.getField(entity.getClass(), FormConstants.IS_AUDITED, true) != null;
                    if (hasField) {
                        ReflectUtil.setFieldValue(entity.getFrist(), FormConstants.IS_AUDITED, 1);
                    }
                    customizeMapper.updateById(entity.getFrist());
                    return Tuple2.of(true, "操作成功");
                }
            }
            return Tuple2.of(false, "失败");
        }
    };

    private final Integer code;
    private final String desc;
    private final String action;

    public static BillStatusEnum getStatusEnumDesc(Integer code) {
        Map<Integer, BillStatusEnum> codeEnumMap = ListUtils.adaptArray(BillStatusEnum.values()).toMap(BillStatusEnum::getCode, item -> item);
        return codeEnumMap.get(code);
    }

    public abstract Tuple2<Boolean, String> execute(TableNode root, Long id);

    public abstract Tuple2<Boolean, String> execute(TableNode root, Long id, Map<String, Object> bo);
}
