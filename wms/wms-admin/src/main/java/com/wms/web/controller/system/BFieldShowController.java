package com.wms.web.controller.system;

import com.wms.common.core.controller.BaseController;
import com.wms.common.core.domain.R;
import com.wms.common.enums.FunctionEnum;
import com.wms.system.service.BFieldShowService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 用户自定义列配置（动态列表引擎支撑接口）
 *
 * @author li-shuang
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/field-show")
public class BFieldShowController extends BaseController {

    @Resource
    private BFieldShowService fieldShowService;

    /**
     * 恢复默认值
     * 删除用户对应模块下面的配置
     *
     * @param functionName 模块名称
     * @return R
     */
    @DeleteMapping("/data/{functionName}")
    public R resetConfig(@PathVariable String functionName) {
        // 检查是否存在传入的模块
        FunctionEnum functionEnum = FunctionEnum.fromCode(functionName);
        int i = fieldShowService.deleteByUserIdAndFunctionName(functionEnum, getUserId());
        // 返回数据
        return R.ok(i);
    }

    /**
     * 更新配置
     *
     * @param functionName 模块名称
     * @return R
     */
    @PostMapping("/data/{functionName}")
    public R updatedConfig(@PathVariable String functionName) {
        // 检查是否存在传入的模块
        FunctionEnum functionEnum = FunctionEnum.fromCode(functionName);
        Long userId = getUserId();
        if (userId == null) {
            return R.fail("用户未登录");
        }
        // 返回数据
        return R.ok(fieldShowService.updatedByUserIdAndFunctionName(functionEnum, userId));
    }
}
