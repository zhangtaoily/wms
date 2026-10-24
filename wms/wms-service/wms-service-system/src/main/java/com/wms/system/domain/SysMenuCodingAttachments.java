package com.wms.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.wms.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;



/**
 * 菜单编码对象 sys_menu_coding_attachments
 *
 * @author ruoyi
 * @date 2024-08-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu_coding_attachments")
public class SysMenuCodingAttachments extends BaseEntity {

    private static final long serialVersionUID=1L;

    /**
     * ID
     */

    /**
     * 功能名称
     */
    private String functionName;
    /**
     * 前缀
     */
    private String codingPrefix;
    /**
     * 编码前缀缀长度
     */
    private Long codingLength;
    /**
     * 删除标志
     */
    @TableLogic
    private String delFlag;
    /**
     * 前缀种类
     */
    private String codingType;
    /**
     * 顺序
     */
    private Long sort;

}
