package com.wms.web.controller.common;

import cn.dev33.satoken.annotation.SaIgnore;
import com.wms.base.service.doc.AsrsTerminalService;
import com.wms.base.service.doc.PdaService;
import com.wms.common.core.domain.R;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 立库扫码终端 + PDA 工作台接口（M6）
 * 终端为车间 H5 页面，走免登白名单（/terminal/scan、/pda），通用表单接口本就 SaIgnore；
 * WCS 为模拟下发（与 ERP 同步桩同模式），协议冻结后替换 AsrsTerminalService 指令适配层。
 */
@Validated
@RestController
@RequestMapping("/common/terminal/")
public class TerminalController {

    @Resource
    private AsrsTerminalService asrsTerminalService;
    @Resource
    private PdaService pdaService;

    /** 月台回路列表（11 个，FR-07） */
    @SaIgnore()
    @GetMapping("/bays")
    public R bays() {
        return R.ok(asrsTerminalService.bays());
    }

    /** 任务检索（扫描/输入批次任务号）：业务批次任务 + 托盘批次任务 双面板 */
    @SaIgnore()
    @GetMapping("/tasks")
    public R tasks(@RequestParam(value = "keyword", required = false) String keyword) {
        return R.ok(asrsTerminalService.searchTasks(keyword));
    }

    /** 开始作业：仓储任务下发 WCS 选托下架 {whTaskId, bay} */
    @SaIgnore()
    @PostMapping("/start")
    public R start(@RequestBody Map<String, Object> body) {
        Long whTaskId = body.get("whTaskId") == null ? null : Long.valueOf(body.get("whTaskId").toString());
        String bay = body.get("bay") == null ? null : body.get("bay").toString();
        return R.ok(asrsTerminalService.startOutbound(whTaskId, bay));
    }

    /** 空托出库：下发空托出库作业任务 {bay} */
    @SaIgnore()
    @PostMapping("/start/empty")
    public R startEmpty(@RequestBody Map<String, Object> body) {
        String bay = body.get("bay") == null ? null : body.get("bay").toString();
        return R.ok(asrsTerminalService.startEmptyOutbound(bay));
    }

    /** 条码出库测试（终端联调模式）{bay} */
    @SaIgnore()
    @PostMapping("/start/test")
    public R startTest(@RequestBody Map<String, Object> body) {
        String bay = body.get("bay") == null ? null : body.get("bay").toString();
        return R.ok(asrsTerminalService.startTestOutbound(bay));
    }

    /** 作业结果回流：WCS 完成上报 {opTaskId, palletNo} */
    @SaIgnore()
    @PostMapping("/complete")
    public R complete(@RequestBody Map<String, Object> body) {
        Long opTaskId = body.get("opTaskId") == null ? null : Long.valueOf(body.get("opTaskId").toString());
        String palletNo = body.get("palletNo") == null ? null : body.get("palletNo").toString();
        return R.ok(asrsTerminalService.completeOutbound(opTaskId, palletNo));
    }

    /** 作业任务列表（执行中/近期，终端回流演示与状态查看） */
    @SaIgnore()
    @GetMapping("/op-tasks")
    public R opTasks(@RequestParam(value = "execStatus", required = false) String execStatus) {
        return R.ok(asrsTerminalService.opTasks(execStatus));
    }

    /** PDA 扫托查询：托盘→箱码→批次→仓位 追溯 */
    @SaIgnore()
    @GetMapping("/pda/trace")
    public R trace(@RequestParam("palletNo") String palletNo) {
        return R.ok(pdaService.palletTrace(palletNo));
    }

    /** PDA 码单收录扫箱回填：箱码台账信息 */
    @SaIgnore()
    @GetMapping("/pda/box")
    public R box(@RequestParam("boxCode") String boxCode) {
        return R.ok(pdaService.boxTrace(boxCode));
    }

    /** PDA 入库待办：已生效入库单精简列表 */
    @SaIgnore()
    @GetMapping("/pda/inbounds")
    public R inbounds() {
        return R.ok(pdaService.pendingInbounds());
    }

    /** PDA 出库待办：已生效出库单精简列表 */
    @SaIgnore()
    @GetMapping("/pda/outbounds")
    public R outbounds() {
        return R.ok(pdaService.pendingOutbounds());
    }

    /** PDA 同步状态查询：四类单据 sync_status 聚合 */
    @SaIgnore()
    @GetMapping("/pda/sync")
    public R sync() {
        return R.ok(pdaService.syncStatusList());
    }
}
