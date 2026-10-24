package com.wms.web.controller.system;

import cn.dev33.satoken.annotation.SaIgnore;
import com.wms.common.constant.Constants;
import com.wms.common.core.domain.R;
import com.wms.common.core.domain.entity.SysUser;
import com.wms.common.core.domain.model.EmailLoginBody;
import com.wms.common.core.domain.model.LoginBody;
import com.wms.common.core.domain.model.LoginUser;
import com.wms.common.core.domain.model.SmsLoginBody;
import com.wms.common.helper.LoginHelper;
import com.wms.service.common.vo.RouterVo;
import com.wms.service.common.spi.ISysUserService;
import com.wms.system.service.SysLoginService;
import com.wms.system.service.SysModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 登录验证
 *
 * @author Lion Li
 */
@Validated
@RequiredArgsConstructor
@RestController
public class SysLoginController {

    private final SysLoginService loginService;
    private final ISysUserService userService;
    private final SysModuleService moduleService;


    /**
     * 登录方法
     *
     * @param loginBody 登录信息
     * @return 结果
     */
    @SaIgnore
    @PostMapping("/login")
    public R<Map<String, Object>> login(@Validated @RequestBody LoginBody loginBody) {
        Map<String, Object> ajax = new HashMap<>();
        // 生成令牌
        String token = loginService.login(loginBody.getUsername(), loginBody.getPassword(), loginBody.getCode(),
            loginBody.getUuid());
        ajax.put(Constants.TOKEN, token);
        return R.ok(ajax);
    }

    /**
     * 短信登录
     *
     * @param smsLoginBody 登录信息
     * @return 结果
     */
    @SaIgnore
    @PostMapping("/smsLogin")
    public R<Map<String, Object>> smsLogin(@Validated @RequestBody SmsLoginBody smsLoginBody) {
        Map<String, Object> ajax = new HashMap<>();
        // 生成令牌
        String token = loginService.smsLogin(smsLoginBody.getPhonenumber(), smsLoginBody.getSmsCode());
        ajax.put(Constants.TOKEN, token);
        return R.ok(ajax);
    }

    /**
     * 邮件登录
     *
     * @param body 登录信息
     * @return 结果
     */
    @PostMapping("/emailLogin")
    public R<Map<String, Object>> emailLogin(@Validated @RequestBody EmailLoginBody body) {
        Map<String, Object> ajax = new HashMap<>();
        // 生成令牌
        String token = loginService.emailLogin(body.getEmail(), body.getEmailCode());
        ajax.put(Constants.TOKEN, token);
        return R.ok(ajax);
    }

    /**
     * 小程序登录(示例)
     *
     * @param xcxCode 小程序code
     * @return 结果
     */
    @SaIgnore
    @PostMapping("/xcxLogin")
    public R<Map<String, Object>> xcxLogin(@NotBlank(message = "{xcx.code.not.blank}") String xcxCode) {
        Map<String, Object> ajax = new HashMap<>();
        // 生成令牌
        String token = loginService.xcxLogin(xcxCode);
        ajax.put(Constants.TOKEN, token);
        return R.ok(ajax);
    }

    /**
     * 退出登录
     */
    @SaIgnore
    @PostMapping("/logout")
    public R<Void> logout() {
        loginService.logout();
        return R.ok("退出成功");
    }

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping("getInfo")
    public R<Map<String, Object>> getInfo() {
        LoginUser loginUser = LoginHelper.getLoginUser();
        SysUser user = null;
        if (loginUser != null) {
            user = userService.selectUserById(loginUser.getUserId());
        }
        Map<String, Object> ajax = new HashMap<>();
        ajax.put("user", user);
        ajax.put("roles", loginUser.getRolePermission());
        // 菜单权限字符（旧） ∪ 两级模块权限字符（sys_func_module.module_code）
        Set<String> permissions = new HashSet<>(loginUser.getMenuPermission());
        permissions.addAll(moduleService.selectModulePermsByUserId(loginUser.getUserId()));
        ajax.put("permissions", permissions);
        return R.ok(ajax);
    }

    /**
     * 获取路由信息
     *
     * @return 路由信息
     */
    @SaIgnore
    @GetMapping("getRouters")
    public R<List<RouterVo>> getRouters() {
        Long userId = LoginHelper.getUserId();
        return R.ok(moduleService.buildRoutersByUserId(userId));
    }
}
