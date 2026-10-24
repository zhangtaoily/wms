package com.wms.web.controller.common;

import cn.dev33.satoken.annotation.SaIgnore;
import com.wms.common.core.domain.R;
import com.wms.sync.service.ReconcileService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 对账报表接口（M7，PRD 7.4）：WMS 台账 vs 单据流水推算账 + 同步完整性对账，
 * 不平差异按作业日志+单据流水定位，人工核查后在同步监控页重推或在业务单据调整。
 */
@Validated
@RestController
@RequestMapping("/common/sync/")
public class SyncReconcileController {

    @Resource
    private ReconcileService reconcileService;

    /** 对账报表：{syncIssues, ledgerDiffs, summary} */
    @SaIgnore()
    @GetMapping("/reconcile")
    public R reconcile() {
        return R.ok(reconcileService.reconcile());
    }
}
