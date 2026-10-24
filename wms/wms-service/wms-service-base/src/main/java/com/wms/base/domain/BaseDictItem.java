package com.wms.base.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;

import java.util.Date;

/**
 * 业务字典项
 */
@Data
@TableName("base_dict_item")
public class BaseDictItem extends IdEntity {
    private static final long serialVersionUID = 1L;

    /** 所属字典ID（base_dict.id） */
    private Long dictId;

    /** 序号 */
    private Integer seq;

    /** 显示值 */
    private String displayVal;

    /** 存储值 */
    private String storeVal;

    /** 扩展值1 */
    private String extVal1;

    /** 扩展值2 */
    private String extVal2;

    /** 是否启用（1=启用 0=停用） */
    private Integer enabled;

    private String creator;

    private Date createTime;

    private String updater;

    private Date updateTime;
}
