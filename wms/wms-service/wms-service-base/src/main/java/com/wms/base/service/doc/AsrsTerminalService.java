package com.wms.base.service.doc;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.wms.base.domain.InvBox;
import com.wms.base.domain.InvPallet;
import com.wms.base.domain.doc.AsrsOpLog;
import com.wms.base.domain.doc.AsrsOpTask;
import com.wms.base.domain.doc.AsrsWhTask;
import com.wms.base.mapper.InvBoxMapper;
import com.wms.base.mapper.InvPalletMapper;
import com.wms.base.mapper.doc.AsrsOpLogMapper;
import com.wms.base.mapper.doc.AsrsOpTaskMapper;
import com.wms.base.mapper.doc.AsrsWhTaskMapper;
import com.wms.common.exception.ServiceException;
import com.wms.service.common.service.BillStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 立库扫码终端服务（M6，FR-07）：
 * 1. 任务检索：业务批次任务（仓储任务聚合）/ 托盘批次任务（托盘聚合）双面板
 * 2. 开始作业：WCS 指令下发（选托下架任务→输送线/堆垛机），未选任务由前端阻断（文案与 PRD 一致）
 * 3. 作业结果回流：任务完成 + 作业日志留痕 + 空托状态回写 + 终端流水（asrs_scan_log）
 * 4. 空托出库 / 条码出库测试（终端联调模式，不联动台账）
 * 说明：WCS 为模拟下发（与 ERP 同步桩同模式），协议冻结后替换指令适配层；
 *      批次扣账已在出库单生效时完成（M4 先锁批次后扣账），终端只负责任务执行与状态回写。
 */
@Slf4j
@Service
public class AsrsTerminalService {

    /** 月台回路 11 个（FR-07） */
    public static final String[] SCAN_BAYS = {
        "月台1号回路", "月台2号回路", "月台3号回路", "月台4号回路",
        "月台5号回路", "月台6号回路", "月台7号回路", "月台8号回路",
        "出口自动", "5#自动线", "自动组托组箱"
    };

    private static final String TASK_BARCODE_OUT = "条码出库";

    @Resource
    private AsrsWhTaskMapper asrsWhTaskMapper;
    @Resource
    private AsrsOpTaskMapper asrsOpTaskMapper;
    @Resource
    private AsrsOpLogMapper asrsOpLogMapper;
    @Resource
    private InvBoxMapper invBoxMapper;
    @Resource
    private InvPalletMapper invPalletMapper;
    @Resource
    private AsrsScanLogService asrsScanLogService;

    /** 月台回路列表 */
    public List<String> bays() {
        List<String> bays = new ArrayList<>();
        for (String bay : SCAN_BAYS) {
            bays.add(bay);
        }
        return bays;
    }

    /** 执行中/近期作业任务（终端回流演示与状态查看） */
    public List<AsrsOpTask> opTasks(String execStatus) {
        QueryWrapper<AsrsOpTask> qw = new QueryWrapper<>();
        qw.eq("status", BillStatusEnum.PASS.getCode());
        if (StrUtil.isNotEmpty(execStatus)) {
            qw.eq("exec_status", execStatus);
        }
        qw.orderByDesc("id").last("limit 30");
        return asrsOpTaskMapper.selectList(qw);
    }

    /**
     * 任务检索：扫描/输入批次任务号，返回双面板任务卡
     * 业务批次任务=待执行仓储任务（按批号）；托盘批次任务=该批下在库箱按托盘分组
     */
    public Map<String, Object> searchTasks(String keyword) {
        QueryWrapper<AsrsWhTask> qw = new QueryWrapper<>();
        qw.eq("status", BillStatusEnum.PASS.getCode())
            .eq("exec_status", "待执行")
            .eq("task_type", TASK_BARCODE_OUT)
            .orderByDesc("id");
        if (StrUtil.isNotEmpty(keyword)) {
            qw.and(w -> w.like("task_no", keyword).or().like("batch_no", keyword));
        }
        List<AsrsWhTask> tasks = asrsWhTaskMapper.selectList(qw);

        List<Map<String, Object>> bizTasks = new ArrayList<>();
        Map<String, List<Map<String, Object>>> palletGroups = new LinkedHashMap<>();
        for (AsrsWhTask task : tasks) {
            Map<String, Object> card = bizTaskCard(task);
            bizTasks.add(card);
            // 托盘批次任务：批下在库箱按托盘分组
            if (StrUtil.isNotEmpty(task.getBatchNo())) {
                QueryWrapper<InvBox> boxQw = new QueryWrapper<>();
                boxQw.eq("batch_no", task.getBatchNo()).eq("status", "在库").isNotNull("pallet_no");
                List<InvBox> boxes = invBoxMapper.selectList(boxQw);
                Map<String, List<InvBox>> groups = new LinkedHashMap<>();
                for (InvBox box : boxes) {
                    groups.computeIfAbsent(box.getPalletNo(), k -> new ArrayList<>()).add(box);
                }
                for (Map.Entry<String, List<InvBox>> entry : groups.entrySet()) {
                    palletGroups.computeIfAbsent(entry.getKey(), k -> new ArrayList<>())
                        .add(palletTaskCard(entry.getKey(), entry.getValue()));
                }
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("bizTasks", bizTasks);
        result.put("palletTasks", new ArrayList<>(palletGroups.values()));
        return result;
    }

    /** 业务批次任务卡：单号/时间/批次缸号/需出/已出 */
    private Map<String, Object> bizTaskCard(AsrsWhTask task) {
        Map<String, Object> card = new HashMap<>();
        card.put("id", task.getId());
        card.put("no", task.getCode());
        card.put("time", task.getTaskDate() == null ? "" : task.getTaskDate().toString());
        card.put("batch", StrUtil.isNotEmpty(task.getBatchNo()) ? task.getBatchNo() : "");
        card.put("vat", "");
        // 需出：批下在库箱合计；已出：批下今日出库箱合计
        BigDecimal[] need = boxSummary(q -> q.eq("batch_no", nz(task.getBatchNo())).eq("status", "在库"));
        BigDecimal[] done = boxSummary(q -> q.eq("batch_no", nz(task.getBatchNo())).isNotNull("out_time"));
        card.put("need", need[0].stripTrailingZeros().toPlainString() + "KG, " + need[1].intValue() + "筒");
        card.put("done", done[0].stripTrailingZeros().toPlainString() + "KG, " + done[2].intValue() + "箱");
        card.put("taskBoxes", task.getBoxes());
        return card;
    }

    /** 托盘批次任务卡：托号/批次缸号/需出（托下在库箱合计） */
    private Map<String, Object> palletTaskCard(String palletNo, List<InvBox> boxes) {
        Map<String, Object> card = new HashMap<>();
        BigDecimal std = BigDecimal.ZERO;
        int tubes = 0;
        String batch = "";
        String vat = "";
        for (InvBox box : boxes) {
            std = std.add(box.getStdWeight() == null ? BigDecimal.ZERO : box.getStdWeight());
            tubes += box.getTubes() == null ? 0 : box.getTubes();
            if (StrUtil.isEmpty(batch) && StrUtil.isNotEmpty(box.getBatchNo())) {
                batch = box.getBatchNo();
            }
            if (StrUtil.isEmpty(vat) && StrUtil.isNotEmpty(box.getVatNo())) {
                vat = box.getVatNo();
            }
        }
        card.put("id", palletNo);
        card.put("palletNo", palletNo);
        card.put("no", palletNo);
        card.put("time", "");
        card.put("batch", batch);
        card.put("vat", vat);
        card.put("need", std.stripTrailingZeros().toPlainString() + "KG, " + tubes + "筒");
        card.put("done", "0KG, 0箱");
        card.put("boxes", boxes.size());
        return card;
    }

    /**
     * 开始作业（FR-07 验收②）：仓储任务下发 WCS 选托下架，输送线按月台回路启动
     */
    @Transactional(rollbackFor = Exception.class)
    public AsrsOpTask startOutbound(Long whTaskId, String bay) {
        checkBay(bay);
        if (whTaskId == null) {
            // 验收①：未选任务阻断（前端提示文案一致，此处兜底）
            throw new ServiceException("请选择有效的批次任务，再进行选托下架作业！");
        }
        AsrsWhTask task = asrsWhTaskMapper.selectById(whTaskId);
        if (task == null || !BillStatusEnum.PASS.getCode().equals(task.getPactState())) {
            throw new ServiceException("批次任务不存在或未生效");
        }
        if (!"待执行".equals(task.getExecStatus())) {
            throw new ServiceException("批次任务" + task.getCode() + "当前状态为" + task.getExecStatus() + "，不能开始作业");
        }
        AsrsOpTask opTask = buildOpTask(TASK_BARCODE_OUT, bay, fromLocByBatch(task.getBatchNo()));
        opTask.setWhTaskId(whTaskId);
        opTask.setRemark("选托下架：" + task.getCode());
        asrsOpTaskMapper.insert(opTask);

        // 仓储任务转执行中
        UpdateWrapper<AsrsWhTask> uw = new UpdateWrapper<>();
        uw.eq("id", whTaskId).set("exec_status", "执行中").set("update_time", new Date());
        asrsWhTaskMapper.update(null, uw);

        asrsScanLogService.log("出库请求", bay, null, true,
            "任务下发成功，输送线已启动选托下架作业（" + task.getCode() + "）");
        log.info("立库终端下发任务：{} 回路：{}", task.getCode(), bay);
        return opTask;
    }

    /** 空托出库：直接下发空托出库作业任务（无台账数量变化） */
    @Transactional(rollbackFor = Exception.class)
    public AsrsOpTask startEmptyOutbound(String bay) {
        checkBay(bay);
        AsrsOpTask opTask = buildOpTask("空托出库", bay, null);
        opTask.setRemark("终端空托出库");
        asrsOpTaskMapper.insert(opTask);
        asrsScanLogService.log("出库请求", bay, null, true, "空托出库任务下发成功");
        return opTask;
    }

    /** 条码出库测试（终端联调模式）：下发测试指令，不联动仓储任务与台账 */
    @Transactional(rollbackFor = Exception.class)
    public AsrsOpTask startTestOutbound(String bay) {
        checkBay(bay);
        AsrsOpTask opTask = buildOpTask(TASK_BARCODE_OUT, bay, null);
        opTask.setRemark("条码出库测试（终端联调）");
        asrsOpTaskMapper.insert(opTask);
        asrsScanLogService.log("出库请求", bay, null, true, "条码出库测试指令下发成功");
        return opTask;
    }

    /**
     * 作业结果回流（验收③）：WCS 完成上报 → 作业任务成功 + 仓储任务已完成 + 作业日志留痕 + 空托状态
     *
     * @param opTaskId 作业任务ID
     * @param palletNo 下架托盘号（输送线选托结果，可空）
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> completeOutbound(Long opTaskId, String palletNo) {
        AsrsOpTask opTask = asrsOpTaskMapper.selectById(opTaskId);
        if (opTask == null) {
            throw new ServiceException("作业任务不存在");
        }
        if (!"执行中".equals(opTask.getExecStatus())) {
            throw new ServiceException("作业任务" + opTask.getWcsTaskId() + "当前状态为" + opTask.getExecStatus() + "，不能回流");
        }
        if (StrUtil.isNotEmpty(palletNo)) {
            opTask.setPalletNo(palletNo);
        }
        AsrsOpTask upd = new AsrsOpTask();
        upd.setId(opTask.getId());
        upd.setPalletNo(opTask.getPalletNo());
        upd.setExecStatus("成功");
        upd.setAsrsTime(new Date());
        upd.setUpdateTime(new Date());
        asrsOpTaskMapper.updateById(upd);

        String whTaskNo = null;
        if (opTask.getWhTaskId() != null) {
            AsrsWhTask whTask = asrsWhTaskMapper.selectById(opTask.getWhTaskId());
            if (whTask != null) {
                whTaskNo = whTask.getCode();
                UpdateWrapper<AsrsWhTask> uw = new UpdateWrapper<>();
                uw.eq("id", whTask.getId()).set("exec_status", "已完成").set("update_time", new Date());
                asrsWhTaskMapper.update(null, uw);
            }
        }

        // 作业日志留痕（FR-11：WCS 上报生成，只读）
        AsrsOpLog opLog = new AsrsOpLog();
        opLog.setPactState(BillStatusEnum.PASS.getCode());
        opLog.setAnalysis("待分析");
        opLog.setOpType(StrUtil.isNotEmpty(opTask.getOpType()) ? opTask.getOpType() : "出库");
        opLog.setResult("正常");
        opLog.setFromLoc(opTask.getFromLoc());
        opLog.setToLoc(opTask.getToLoc());
        opLog.setPalletNo(opTask.getPalletNo());
        opLog.setWcsTime(opTask.getTaskTime());
        opLog.setProcTime(new Date());
        opLog.setDocDate(LocalDate.now());
        opLog.setCreateTime(new Date());
        asrsOpLogMapper.insert(opLog);

        // 空托状态回写：托下在库箱数为 0 时托盘转空托
        markPalletEmptyIfCleared(opTask.getPalletNo());

        asrsScanLogService.log("出库完成", opTask.getToLoc(), opTask.getPalletNo(), true,
            "任务" + (whTaskNo == null ? opTask.getWcsTaskId() : whTaskNo) + "下架作业完成");
        log.info("立库终端作业回流完成：opTask={} whTask={}", opTask.getId(), whTaskNo);

        Map<String, Object> result = new HashMap<>();
        result.put("opTaskId", opTask.getId());
        result.put("wcsTaskId", opTask.getWcsTaskId());
        result.put("whTaskNo", whTaskNo);
        result.put("palletNo", opTask.getPalletNo());
        return result;
    }

    /** 作业任务下发公共构造（WCS 任务号，执行中） */
    private AsrsOpTask buildOpTask(String taskType, String bay, String fromLoc) {
        AsrsOpTask opTask = new AsrsOpTask();
        opTask.setWcsTaskId("WCS" + System.currentTimeMillis());
        opTask.setPactState(BillStatusEnum.PASS.getCode());
        opTask.setTaskType(taskType);
        opTask.setOpType("出库");
        opTask.setFromLoc(fromLoc);
        opTask.setToLoc("出口自动");
        opTask.setPort(bay);
        opTask.setExecStatus("执行中");
        opTask.setTaskTime(new Date());
        opTask.setCreateTime(new Date());
        return opTask;
    }

    /** 源仓位：批次首行箱码的最后仓位（出库生效后箱转"已出库"但仓位保留，任务下发仍可定位源仓位） */
    private String fromLocByBatch(String batchNo) {
        if (StrUtil.isEmpty(batchNo)) {
            return null;
        }
        QueryWrapper<InvBox> qw = new QueryWrapper<>();
        qw.eq("batch_no", batchNo).orderByAsc("id").last("limit 1");
        List<InvBox> boxes = invBoxMapper.selectList(qw);
        return boxes.isEmpty() ? null : boxes.get(0).getLocCode();
    }

    /** 托盘清空判定：托下在库箱数=0 → 空托 */
    private void markPalletEmptyIfCleared(String palletNo) {
        if (StrUtil.isEmpty(palletNo)) {
            return;
        }
        QueryWrapper<InvBox> qw = new QueryWrapper<>();
        qw.eq("pallet_no", palletNo).eq("status", "在库");
        if (invBoxMapper.selectCount(qw) > 0) {
            return;
        }
        QueryWrapper<InvPallet> palletQw = new QueryWrapper<>();
        palletQw.eq("pallet_no", palletNo);
        InvPallet pallet = invPalletMapper.selectOne(palletQw);
        if (pallet == null) {
            return;
        }
        UpdateWrapper<InvPallet> uw = new UpdateWrapper<>();
        uw.eq("id", pallet.getId())
            .set("is_empty", 1)
            .set("status", "空")
            .set("box_count", 0)
            .set("update_time", new Date());
        invPalletMapper.update(null, uw);
    }

    private void checkBay(String bay) {
        if (StrUtil.isEmpty(bay)) {
            throw new ServiceException("请选择有效的月台回路");
        }
        for (String allowed : SCAN_BAYS) {
            if (allowed.equals(bay)) {
                return;
            }
        }
        throw new ServiceException("月台回路不合法：" + bay);
    }

    /** 箱码台账聚合：[0]=标重合计 [1]=筒数合计 [2]=箱数 */
    private BigDecimal[] boxSummary(java.util.function.Function<QueryWrapper<InvBox>, QueryWrapper<InvBox>> cond) {
        QueryWrapper<InvBox> qw = cond.apply(new QueryWrapper<>());
        List<InvBox> boxes = invBoxMapper.selectList(qw);
        BigDecimal std = BigDecimal.ZERO;
        int tubes = 0;
        for (InvBox box : boxes) {
            std = std.add(box.getStdWeight() == null ? BigDecimal.ZERO : box.getStdWeight());
            tubes += box.getTubes() == null ? 0 : box.getTubes();
        }
        return new BigDecimal[]{std, BigDecimal.valueOf(tubes), BigDecimal.valueOf(boxes.size())};
    }

    private static String nz(String v) {
        return v == null ? "" : v;
    }
}
