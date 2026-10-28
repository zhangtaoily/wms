package com.wms.web.controller.system;

import cn.dev33.satoken.annotation.SaIgnore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页
 *
 * @author Lion Li
 */
@RestController
public class SysIndexController {

    /**
     * 访问首页，提示语
     */
    @SaIgnore
    @GetMapping("/")
    public String index() {
        return "欢迎使用云梭智能仓储管理系统（WMS），当前版本：v5.0.0，请通过前端地址访问。";
    }
}
