package com.wms.system.domain.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;

@Data
public class DictDataVo {
    /**
     * 字典类型
     */
    private String dictType;

    /**
     * 字典项
     */
    private List<DictDataItem> dictItems;

    @Data
    public static class DictDataItem {
        /**
         * 字典标签
         */
        private String dictLabel;
        /**
         * 字典键值
         */
        private String dictValue;
        /**
         * 操作,启用，禁用
         */
        private Integer enable;
    }
}
