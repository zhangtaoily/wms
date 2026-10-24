package com.wms.service.common.service;

import cn.hutool.core.util.ReflectUtil;
import com.wms.common.api.BasicBO;
import com.wms.common.enums.BehaviorEnum;
import com.wms.common.helper.LoginHelper;
import com.wms.common.utils.ApplicationContextHolder;
import com.wms.common.utils.JsonUtils;
import com.wms.common.utils.ListUtils;
import com.wms.common.utils.Tuple2;
import com.wms.enums.CancelOaStatusEnum;
import com.wms.enums.CancelStatusEnum;
import com.wms.mapper.CustomizeMapper;
import com.wms.service.common.constant.FormConstants;
import com.wms.service.common.spi.IWorkFlowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.wms.common.utils.ObjectUtil.convertToLong;
import static com.wms.service.common.constant.FormConstants.STATUS;

@Slf4j
@Service("commonWorkFlowService")
public class WorkFlowService {
    public IWorkFlowService getWorkFlowService() {
        return ApplicationContextHolder.getBean(IWorkFlowService.class);
    }

    @Transactional(rollbackFor = Exception.class)
    public Tuple2<Integer, String> submit(IFormContext ctx, Long id, BasicBO bo) {
        Tuple2<Integer, String> beforeResult = ctx.beforeSubmit(id, bo);
        if (!beforeResult.isSuccess()) {
            return beforeResult;
        }

        ctx.getSupportType();

        TableNode root = ctx.fetchTableNode();
        CustomizeMapper customizeMapper = ctx.supplierMapper();
        Object entity = customizeMapper.selectById(id);
        if (entity == null) {
            return Tuple2.of(0, "未找到记录");
        }

        Object currentStatus = ReflectUtil.getFieldValue(entity, STATUS);
        if (!BillStatusEnum.CREATE.getCode().equals(currentStatus) && !BillStatusEnum.REJECT.getCode().equals(currentStatus)) {
            return Tuple2.of(0, "当前状态不允许提交");
        }

        BillStatusEnum.AUDIT.execute(root,id);

        Boolean isIgnoreOa = ctx.isIgnoreOa(id);
        if (!isIgnoreOa) {
            ctx.afterSubmit(id);
        }else{
            Tuple2<Boolean, String> passResult = pass(ctx, id, null);
            if (!passResult.getFrist()) {
                return Tuple2.of(0, passResult.getSecond());
            }
        }

        return Tuple2.of(1, "提交成功");
    }

    @Transactional(rollbackFor = Exception.class)
    public Tuple2<Boolean, String> pass(IFormContext ctx, Long id, Map<String, Object> bo) {
        TableNode root = ctx.fetchTableNode();

        Map<String, List<Map<String, Object>>> dbOldData = ctx.doLoad(id);
        ctx.beforePass(id, root, dbOldData);

        Tuple2<Boolean, String> passResult;
        if (bo == null) {
            passResult = BillStatusEnum.PASS.execute(root, id);
            log.info("执行pass后:{}", JsonUtils.toJsonString(passResult));
        } else {
            passResult = BillStatusEnum.PASS.execute(root, id, bo);
            log.info("执行pass后:{}", JsonUtils.toJsonString(passResult));
            ctx.afterPassStateChanged(id, root, bo);
        }

        if (!passResult.getFrist()) {
            log.warn("单据{}状态变更失败，当前状态不允许审核通过", id);
            return Tuple2.of(false, passResult.getSecond());
        }

        //审核后数据
        Map<String, List<Map<String, Object>>> dbNewData = ctx.doLoad(id);

        //分发
        ctx.afterPass(id, root, dbOldData, dbNewData);

        return Tuple2.of(true, "审批通过");
    }

    @Transactional(rollbackFor = Exception.class)
    public Tuple2<Boolean, String> reject(IFormContext ctx, Long id, Map<String, Object> bo) {
        TableNode root = ctx.fetchTableNode();

        if (bo == null) {
            BillStatusEnum.REJECT.execute(root, id);
        } else {
            BillStatusEnum.REJECT.execute(root, id, bo);
            //根据OA传递进来的数据修改数据
            ctx.afterRejectStateChanged(id, root, bo);
        }

        ctx.afterReject(id);

        return Tuple2.of(true, "审批驳回");
    }

    @Transactional(rollbackFor = Exception.class)
    public Tuple2<Boolean, String> rollBack(IFormContext ctx, Long id) {
        Tuple2<Boolean, String> checkResult = ctx.checkRollBack(id);
        if (!checkResult.getFrist()) {
            throw new RuntimeException(checkResult.getSecond());
        }
        //更新前数据
        Map<String, List<Map<String, Object>>> oldTableEntityMap = ctx.doLoad(id);
        //撤回前的操作
        Tuple2<Boolean, String> erpResult = ctx.beforeRollBack(id, ctx.fetchTableNode(), oldTableEntityMap);
        if (!erpResult.getFrist()) {
            throw new RuntimeException(erpResult.getSecond());
        }

        Tuple2<Boolean, String> result = BillStatusEnum.CREATE.execute(ctx.fetchTableNode(), id);
        if (!result.getFrist()) {
            return result;
        }

        //更新后数据
        Map<String, List<Map<String, Object>>> newTableEntityMap = ctx.doLoad(id);

        //反写
        Tuple2<Boolean, String> writeBackResult = ctx.writeBack(ctx.fetchTableNode(), ctx.getSupportType(), oldTableEntityMap,
            newTableEntityMap, BehaviorEnum.ROLL_BACK);
        if (!writeBackResult.getFrist()) {
            throw new RuntimeException(writeBackResult.getSecond());
        }

        Tuple2<Boolean, String> afterRollBackResult = ctx.afterRollBack(id, ctx.fetchTableNode(), oldTableEntityMap);
        if (!afterRollBackResult.getFrist()) {
            throw new RuntimeException(afterRollBackResult.getSecond());
        }

        //流程撤销
        if (!ctx.isIgnoreOa(id)) {
            //如果是通过状态，只做撤回，不删流程
            // 获取主表名
            String mainTableName = ctx.fetchTableNode().getTableName();

            // 获取主表数据
            Map<String, Object> mainTable = ListUtils.safeGetFirst(oldTableEntityMap.get(mainTableName));

            // 获取pactState字段值
            Object pactState = 0;
            if (mainTable != null) {
                pactState = mainTable.get(FormConstants.PACT_STATE);
            }

            if (BillStatusEnum.PASS.getCode().equals((Integer) pactState)) {
                return Tuple2.of(true, "撤回成功");
            }
            //取requestId;
            if (mainTable != null && mainTable.get(FormConstants.REQUEST_ID) != null) {
                Long requestId = Long.parseLong(mainTable.get(FormConstants.REQUEST_ID).toString());
                getWorkFlowService().deleteRequest(requestId);
            }
        }

        log.info("Username{}撤回{}的数据:{}", LoginHelper.getUsername(), ctx.getSupportType().getDesc(), id);

        return Tuple2.of(true, "撤回成功");
    }

    @Transactional(rollbackFor = Exception.class)
    public Tuple2<Boolean, String> callBack(IFormContext ctx, Map<String, Object> bo) {
        if ("1".equals(bo.get("cancelStatus"))) {
            Long id = convertToLong(bo.get("id"));
            Long[] newIds = new Long[]{id};
            if ("1".equals(bo.get("status"))) {
                // 整单取消
                // 回写取消状态为审批通过
                ctx.batchChangeCancelOaStatus(newIds, CancelOaStatusEnum.CANCEL_OA_PASS.getCode());
                return ctx.batchChangeCancelStatus(newIds, CancelStatusEnum.CANCEL.getCode(), bo);
            } else {
                // 回写取消状态为审批驳回
                ctx.batchChangeCancelOaStatus(newIds, CancelOaStatusEnum.CANCEL_OA_REJECT.getCode());
                return Tuple2.of(true, null);
            }
        } else if ("2".equals(bo.get("cancelStatus"))) {
            String idsString = (String) bo.get("goodsIdList");
            Long[] ids = Arrays.stream(idsString.split(","))
                .map(Long::valueOf)
                .toArray(Long[]::new);
            if ("1".equals(bo.get("status"))) {
                // 明细取消
                ctx.batchChangeDetailsCancelOaStatus(ids, CancelOaStatusEnum.CANCEL_OA_PASS.getCode());
                return ctx.batchChangeDetailsCancelStatus(ids, CancelStatusEnum.CANCEL.getCode(), bo);
            } else {
                // 回写取消状态为正常
                ctx.batchChangeDetailsCancelOaStatus(ids, CancelOaStatusEnum.CANCEL_OA_REJECT.getCode());
                return Tuple2.of(true, null);
            }
        } else if ("1".equals(bo.get("status"))) {
            //通过
            pass(ctx,Long.parseLong((String) bo.get("id")), bo);
        } else {
            //不通过
            reject(ctx,Long.parseLong((String) bo.get("id")), bo);
        }

        return Tuple2.of(true, "回调成功");
    }
}
