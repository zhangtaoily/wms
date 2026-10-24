package com.wms.web.controller.common;

import cn.dev33.satoken.annotation.SaIgnore;
import com.wms.common.core.domain.R;
import com.wms.common.enums.FunctionEnum;
import com.wms.common.utils.Tuple2;
import com.wms.service.common.spi.CommonFormManager;
import com.wms.vo.BatchCancelVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.wms.common.utils.ListUtils.getLongListByStringList;

/**
 * 取消单据接口
 * @author li-shuang
 */
@Validated
@RestController
@RequestMapping("/common/table/")
public class CancelTableController {

    /**
     * 批量取消表单
     */
    @SaIgnore()
    @PatchMapping("/cancel/batch/{function}")
    public R batchCancel(@PathVariable("function") String function, @RequestBody BatchCancelVo vo) {
        Tuple2<Boolean, String> tuple2 = CommonFormManager.getInstance().getFormService(FunctionEnum.valueOf(function)).batchCancel(vo);
        if(tuple2.getFrist()){
            return R.ok(tuple2.getSecond());
        }else{
            return R.fail(tuple2.getSecond());
        }
    }

    /**
     * 批量反取消表单
     */
    @SaIgnore()
    @PatchMapping("/re-cancel/batch/{function}")
    public R batchReCancel(@PathVariable("function") String function, @RequestBody String[] ids) {
        Long[] longIds = getLongListByStringList(ids);
        return R.ok(CommonFormManager.getInstance().getFormService(FunctionEnum.valueOf(function)).batchReCancel(longIds).getSecond());
    }

    /**
     * 批量取消表单明细
     */
    @SaIgnore()
    @PatchMapping("/cancel/details/batch/{function}")
    public R batchCancelDetails(@PathVariable("function") String function, @RequestBody BatchCancelVo vo) {
        Tuple2<Boolean, String> tuple2 = CommonFormManager.getInstance().getFormService(FunctionEnum.valueOf(function)).batchCancelDetails(vo);
        if(tuple2.getFrist()){
            return R.ok(tuple2.getSecond());
        }else{
            return R.fail(tuple2.getSecond());
        }
    }

    /**
     * 批量反取消表单明细
     */
    @SaIgnore()
    @PatchMapping("/re-cancel/details/batch/{function}")
    public R batchReCancelDetails(@PathVariable("function") String function, @RequestBody String[] ids) {
        Long[] longIds = getLongListByStringList(ids);
        return R.ok(CommonFormManager.getInstance().getFormService(FunctionEnum.valueOf(function)).batchReCancelDetails(longIds).getSecond());
    }
}
