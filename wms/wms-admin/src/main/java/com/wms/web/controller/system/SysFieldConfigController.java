package com.wms.web.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import com.wms.common.annotation.Log;
import com.wms.common.core.controller.BaseController;
import com.wms.common.core.domain.PageQuery;
import com.wms.common.core.domain.R;
import com.wms.common.core.page.TableDataInfo;
import com.wms.common.enums.BusinessType;
import com.wms.common.enums.FunctionEnum;
import com.wms.domain.SysFieldConfig;
import com.wms.spi.ISysFieldConfigService;
import com.wms.system.service.SysFilterSchemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 字段配置表
 *
 * @author ruoyi
 * @date 2024-07-31
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/field/config")
public class SysFieldConfigController extends BaseController {

    private final ISysFieldConfigService sysFieldConfigService;

    private final SysFilterSchemeService sysFilterSchemeService;

    @GetMapping("/list/{name}")
    @SaIgnore()
    public R<Map> list(@PathVariable("name") FunctionEnum name) {
        Map<String, Object> map = new HashMap<>();
        List<SysFieldConfig> fieldConfigList = sysFieldConfigService.listSearchField(name);
        List<Map<String, Object>> defaultSchemeDetails = sysFilterSchemeService.getDefaultSchemeDetails(name);
        map.put("fieldConfigList",fieldConfigList);
        map.put("defaultScheme",defaultSchemeDetails);
        return R.ok(map);
    }

    @GetMapping("/list/function/{name}/fields")
    @SaIgnore()
    public R<List<SysFieldConfig>> listFunctionField(@PathVariable("name") FunctionEnum name) {
        return R.ok(sysFieldConfigService.listField(name));
    }

    /**
     * PDA 引擎字段配置（M6）：按 mobile_show/mobile_seq 渲染移动端列表与表单列，
     * scan_input=1 的字段由 PDA 渲染为扫码输入框唤起扫码
     */
    @GetMapping("/mobile/{name}")
    @SaIgnore()
    public R<List<Map<String, Object>>> listMobileField(@PathVariable("name") FunctionEnum name) {
        return R.ok(sysFieldConfigService.listMobileField(name));
    }

    /**
     * 分页查询字段配置列表
     */
    @SaCheckPermission("SYS_FieldConfig")
    @GetMapping("/list")
    public TableDataInfo<SysFieldConfig> page(SysFieldConfig fieldConfig, PageQuery pageQuery) {
        return sysFieldConfigService.pageFieldConfig(fieldConfig, pageQuery);
    }

    /**
     * 获取字段配置详细信息
     */
    @SaCheckPermission("SYS_FieldConfig")
    @GetMapping("/{id}")
    public R<SysFieldConfig> getInfo(@PathVariable("id") Long id) {
        return R.ok(sysFieldConfigService.selectFieldConfigById(id));
    }

    /**
     * 新增字段配置
     */
    @SaCheckPermission("SYS_FieldConfig")
    @Log(title = "字段配置", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@RequestBody SysFieldConfig fieldConfig) {
        return toAjax(sysFieldConfigService.insertFieldConfig(fieldConfig));
    }

    /**
     * 修改字段配置
     */
    @SaCheckPermission("SYS_FieldConfig")
    @Log(title = "字段配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@RequestBody SysFieldConfig fieldConfig) {
        return toAjax(sysFieldConfigService.updateFieldConfig(fieldConfig));
    }

    /**
     * 删除字段配置
     */
    @SaCheckPermission("SYS_FieldConfig")
    @Log(title = "字段配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Long[] ids) {
        return toAjax(sysFieldConfigService.deleteFieldConfigByIds(ids));
    }
}
