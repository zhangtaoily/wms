package com.wms.web.controller.system;

import cn.hutool.json.JSONObject;
import com.wms.common.core.domain.R;
import com.wms.common.core.domain.entity.SysDept;
import com.wms.system.service.ISysDeptService;
import com.wms.system.service.SysInterfaceConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/interfaceConfig")
public class SysInterfaceConfigController {
    private final SysInterfaceConfigService interfaceService;
    @GetMapping(value = "/preview/{configId}")
    public R<JSONObject> preview(@PathVariable Long configId) {
        JSONObject data = interfaceService.PreviewParm(configId);
        return R.ok(data);
    }
}
