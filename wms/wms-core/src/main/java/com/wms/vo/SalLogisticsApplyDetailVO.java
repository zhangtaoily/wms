package com.wms.vo;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 物流申请单明细VO
 */
@Data
public class SalLogisticsApplyDetailVO implements Serializable {

    private static final long serialVersionUID = 335857545747208643L;

    /**
     * 物流申请单ID
     */
    @NotNull(message = "物流申请单ID不能为null")
    private Long logisticsApplyId;
    /**
     * 明细列表
     */
    @NotEmpty(message = "明细列表列表不能为空")
    @Valid
    private List<DetailItem> detailList;

    /**
     * 明细项 - 使用 static 内部类
     */
    @Data
    public static class DetailItem implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 关联发货通知单ID
         */
        private Long noticeId;

        /**
         * 备注
         */
        private String remark;

        /**
         * 总数量
         */
        private BigDecimal totalQty;

        /**
         * 是否补充（0：否，1：是）
         */
        private Integer isSupplement;
    }
}
