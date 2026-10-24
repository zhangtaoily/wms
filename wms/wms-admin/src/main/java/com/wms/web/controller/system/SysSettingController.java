package com.wms.web.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.wms.common.annotation.Log;
import com.wms.common.core.controller.BaseController;
import com.wms.common.core.domain.PageQuery;
import com.wms.common.core.domain.R;
import com.wms.common.core.page.TableDataInfo;
import com.wms.common.enums.BusinessType;
import com.wms.domain.SysSetting;
import com.wms.system.service.SysSettingService;
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

/**
 * 系统设置
 *
 * @author wms
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/setting")
public class SysSettingController extends BaseController {

    private final SysSettingService settingService;

    /**
     * 分页查询系统设置
     */
    @SaCheckPermission("SYS_Setting")
    @GetMapping("/list")
    public TableDataInfo<SysSetting> list(SysSetting setting, PageQuery pageQuery) {
        return settingService.pageSetting(setting, pageQuery);
    }

    /**
     * 获取系统设置详细信息
     */
    @SaCheckPermission("SYS_Setting")
    @GetMapping("/{id}")
    public R<SysSetting> getInfo(@PathVariable("id") Long id) {
        return R.ok(settingService.selectSettingById(id));
    }

    /**
     * 新增系统设置
     */
    @SaCheckPermission("SYS_Setting")
    @Log(title = "系统设置", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Validated @RequestBody SysSetting setting) {
        if (!settingService.checkSettingCodeUnique(setting)) {
            return R.fail("新增设置'" + setting.getSettingCode() + "'失败，设置编码已存在");
        }
        return toAjax(settingService.insertSetting(setting));
    }

    /**
     * 修改系统设置
     */
    @SaCheckPermission("SYS_Setting")
    @Log(title = "系统设置", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Validated @RequestBody SysSetting setting) {
        if (!settingService.checkSettingCodeUnique(setting)) {
            return R.fail("修改设置'" + setting.getSettingCode() + "'失败，设置编码已存在");
        }
        return toAjax(settingService.updateSetting(setting));
    }

    /**
     * 删除系统设置
     */
    @SaCheckPermission("SYS_Setting")
    @Log(title = "系统设置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Long[] ids) {
        return toAjax(settingService.deleteSettingByIds(ids));
    }
}
