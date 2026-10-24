package com.wms.web.controller.common;

import cn.dev33.satoken.annotation.SaIgnore;
import com.wms.common.core.domain.R;
import com.wms.common.enums.FunctionEnum;
import com.wms.service.common.spi.CommonFormManager;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.wms.common.utils.ListUtils.getLongListByStringList;

/**
 * 关闭单据接口
 *
 * @author li-shuang
 */
@Validated
@RestController
@RequestMapping("/common/table/")
public class CloseTableController {

    /**
     * 关闭
     */
    @SaIgnore()
    @PatchMapping("/close/{function}/{id}")
    public R close(@PathVariable("function") String function, @PathVariable("id") Long id) {
        return R.ok(CommonFormManager.getInstance().getFormService(FunctionEnum.valueOf(function)).close(id));
    }

    /**
     * 批量关闭表单
     */
    @SaIgnore()
    @PatchMapping("/close/batch/{function}")
    public R batchClose(@PathVariable("function") String function, @RequestBody String[] ids) {
        Long[] longIds = getLongListByStringList(ids);
        return R.ok(CommonFormManager.getInstance().getFormService(FunctionEnum.valueOf(function)).batchClose(longIds));
    }

    /**
     * 批量关闭表单
     */
    @SaIgnore()
    @PatchMapping("/re-close/batch/{function}")
    public R batchReClose(@PathVariable("function") String function, @RequestBody String[] ids) {
        Long[] longIds = getLongListByStringList(ids);
        return R.ok(CommonFormManager.getInstance().getFormService(FunctionEnum.valueOf(function)).batchReClose(longIds));
    }

    /**
     * 批量关闭表单明细
     */
    @SaIgnore()
    @PatchMapping("/close/details/batch/{function}")
    public R batchCloseDetails(@PathVariable("function") String function, @RequestBody String[] ids) {
        Long[] longIds = getLongListByStringList(ids);
        return R.ok(CommonFormManager.getInstance().getFormService(FunctionEnum.valueOf(function)).batchCloseDetails(longIds));
    }

    /**
     * 批量反关闭表单明细
     */
    @SaIgnore()
    @PatchMapping("/re-close/details/batch/{function}")
    public R batchReCloseDetails(@PathVariable("function") String function, @RequestBody String[] ids) {
        Long[] longIds = getLongListByStringList(ids);
        return R.ok(CommonFormManager.getInstance().getFormService(FunctionEnum.valueOf(function)).batchReCloseDetails(longIds));
    }

}
