package com.wms.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wms.common.enums.dicts.DyeMethodEnum;
import com.wms.common.utils.StringUtils;
import com.wms.common.utils.Tuple2;
import lombok.Data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author li shuang
 */
@Data
public class BatchCancelVo {
    private Long id;
    /**
     * ID列表
     */
    private Long[] ids;

    /**
     * 取消原因
     */
    private String cancelReason;
    /**
     * 是否产生费用
     */
    private Integer isGf;
    /**
     * 是否通知染厂
     */
    private Integer isNd;
    /**
     * 绞染车间
     */
    private String jrcj;
    /**
     * 筒染车间
     */
    private String trcj;
    /**
     * 染色方式
     */
    private String dyeMethod;
    /**
     * 明细数据
     */
    private List<Details> details;

    public HashMap<String, Object> getParams() {
        HashMap<String, Object> params = new HashMap<>();
        // 是否产生费用
        params.put("sfcsfy", getIsGf());
        // 是否通知染厂
        params.put("sftzrc", getIsNd());
        // 绞染车间
        params.put("jrcj", getJrcj());
        // 筒染车间
        params.put("trcj", getTrcj());
        return params;
    }

    public HashMap<String, Object> getGoodsParams() {
        HashMap<String, Object> params = new HashMap<>();
        // 取消类型
        params.put("cancelStatus", "2");
        // 取消明细行id
        params.put("goodsIdList", String.join(",", Arrays.stream(getIds()).map(String::valueOf).toArray(String[]::new)));
        return params;
    }

    public HashMap<String, Object> getGoodsParamsByOrder() {
        HashMap<String, Object> params = new HashMap<>();
        // 是否产生费用
        params.put("sfcsfy", getIsGf());
        // 是否通知染厂
        params.put("sftzrc", getIsNd());
        // 绞染车间
        params.put("jrcj", getJrcj());
        // 筒染车间
        params.put("trcj", getTrcj());
        // 取消类型
        params.put("cancelStatus", "2");
        // 取消明细行id
        params.put("goodsIdList", String.join(",", Arrays.stream(getIds()).map(String::valueOf).toArray(String[]::new)));
        return params;
    }

    @Data
    public static class Details {
        private Long id;
        private Integer sortIndex;
        /**
         * 复精梳批号
         */
        private String rbNo;
        /**
         * 纺纱批号
         */
        private String sbNo;
        /**
         * 染色方式
         */
        private String dyeMethod;

    }

    /**
     * 订单校验参数
     * @return
     */
    public Tuple2<Boolean, String> verifyOrder(){
        if(this.getIsGf() == null){
            return Tuple2.of(false, "是否产生费用必填");
        }
        if(this.getIsNd() == null){
            return Tuple2.of(false, "是否通知染厂必填");
        }
        return Tuple2.of(true, "");
    }

    /**
     * 校验明细
     * @return
     */
    public Tuple2<Boolean, String> verifyDetails(){
        // 绞染筒染 车间必填
        if(DyeMethodEnum.TWIST_DYEING.getCode().equals(this.dyeMethod) && this.jrcj == null){
            return Tuple2.of(false, "染色方式为绞染,染色车间必填");
        }
        if(DyeMethodEnum.CONE_DYEING.getCode().equals(this.dyeMethod) && this.trcj == null){
            return Tuple2.of(false, "染色方式为筒染,染色车间必填");
        }
        if(DyeMethodEnum.STRIP_DYEING.getCode().equals(this.dyeMethod) && this.details == null){
            return Tuple2.of(false, "复精梳批号必填");
        }
        for (Details detail : this.details) {
            // 条染，复精梳批号不能为空
            if( DyeMethodEnum.STRIP_DYEING.getCode().equals(detail.dyeMethod) && StringUtils.isEmpty(detail.rbNo)){
                return Tuple2.of(false, "第"+detail.getSortIndex()+"行,复精梳批号必填");
            }
        }
        return Tuple2.of(true, "");
    }
}
