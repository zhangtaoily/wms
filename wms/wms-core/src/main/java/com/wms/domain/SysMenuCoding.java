package com.wms.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.wms.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;



/**
 * 菜单编码对象 sys_menu_coding
 *
 * @author ruoyi
 * @date 2024-08-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu_coding")
public class SysMenuCoding extends BaseEntity {

    private static final long serialVersionUID=1L;

    /**
     * ID
     */

    /**
     * 功能名称
     */
    private String functionName;
    /**
     * 删除标志
     */
    @TableLogic
    private String delFlag;
    /**
     * 预览
     */
    private String preview;
    /**
     * 编码次数
     */
    private Integer codingNum;

}
