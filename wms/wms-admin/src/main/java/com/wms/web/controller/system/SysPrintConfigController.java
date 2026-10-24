package com.wms.web.controller.system;

import cn.dev33.satoken.annotation.SaIgnore;
import com.wms.common.core.controller.BaseController;
import com.wms.common.core.domain.PageQuery;
import com.wms.common.core.domain.R;
import com.wms.domain.SysFieldConfig;
import com.wms.domain.system.SysPrintConfig;
import com.wms.page.DynamicsTableDataInfo;
import com.wms.system.service.impl.SysPrintConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.TreeMap;


/**
 * 打印模板
 *
 * @author ruoyi
 * @date 2024-08-06
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/print/config")
public class SysPrintConfigController extends BaseController {

    @Resource
    private SysPrintConfigService sysPrintConfigService;

    /**
     * 查询打印模板列表
     */
    @PostMapping("/list")
    @SaIgnore()
    public DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> list(@RequestBody PageQuery pageQuery) {
        return sysPrintConfigService.queryPageList(pageQuery);
    }

    /**
     * selectPrintConfigByModelName详细信息
     *
     * @param modelName
     */
    @GetMapping("/{modelName}")
    @SaIgnore()
    public R<List<SysPrintConfig>> selectPrintConfigByModelName(@PathVariable String modelName) {
        List<SysPrintConfig> sysPrintConfigs = sysPrintConfigService.selectPrintConfigByModelName(modelName);

        return R.ok(sysPrintConfigs);
    }
}
