package com.wms.vo;

import lombok.Data;

/**
 * 染厂修改的数据
 * @author li shuang
 */
@Data
public class DyeingFactoryColorVo {
    /**
     * 明细行id
     */
    private Long id;
    /**
     * 颜色
     */
    private String color;
    /**
     * 打版色号
     */
    private String printColorCode;
    /**
     * 公司色号
     */
    private String compColorCode;
    /**
     * 确认色
     */
    private String confirmColor;


    /**
     * 打版色号id
     */
    private Long printColorId;
    /**
     * 公司色号id
     */
    private Long compColorId;
}
