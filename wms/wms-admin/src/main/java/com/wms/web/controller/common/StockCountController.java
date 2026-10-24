package com.wms.web.controller.common;

import cn.dev33.satoken.annotation.SaIgnore;
import com.wms.base.service.doc.DocStockCountService;
import com.wms.common.core.domain.R;
import com.wms.common.utils.Tuple2;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 盘存单盘盈亏主管确认接口（M5，Q2）
 * 盘存单生效后生成的盘盈亏调整记录，由仓库主管在此确认放行（调整台账并回写 ERP）或驳回。
 * 权限：仓库主管（正式环境经权限树按角色控制菜单与按钮，接口层与 Cancel/Close 控制器同基线）。
 */
@Validated
@RestController
@RequestMapping("/common/table/")
public class StockCountController {

    @Resource
    private DocStockCountService docStockCountService;

    /**
     * 盘盈亏主管确认
     *
     * @param id       盘存单ID
     * @param approved true=放行（调整台账+回写ERP） false=驳回
     * @param body     {remark: 确认/驳回备注}
     */
    @SaIgnore()
    @PatchMapping("/stockCount/confirm/{id}/{approved}")
    public R confirmAdjust(@PathVariable("id") Long id, @PathVariable("approved") String approved,
                           @RequestBody(required = false) Map<String, String> body) {
        boolean pass = "true".equalsIgnoreCase(approved) || "1".equals(approved) || "yes".equalsIgnoreCase(approved);
        String remark = body == null ? null : body.get("remark");
        Tuple2<Boolean, String> result = docStockCountService.confirmAdjust(id, pass, remark);
        return result.getFrist() ? R.ok(result.getSecond()) : R.fail(result.getSecond());
    }
}
