package com.wms.service.common.bo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.wms.common.core.validate.AddGroup;
import com.wms.common.core.validate.EditGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;

import java.util.Date;
import java.util.List;

import com.wms.common.core.domain.BaseEntity;

/**
 * 菜单编码业务对象 sys_menu_coding
 *
 * @author ruoyi
 * @date 2024-08-16
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class SysMenuCodingBo extends BaseEntity {

    /**
     * ID
     */


    /**
     * 功能名称
     */
    @NotBlank(message = "功能名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String functionName;

    /**
     * 预览
     */
    @NotBlank(message = "预览不能为空", groups = { AddGroup.class, EditGroup.class })
    private String preview;
    @TableField(exist = false)
    private String  nativeSql;

    /**
     * 编码次数
     */
    private Integer codingNum;


    private List<SysMenuCodingAttachmentsBo> sysMenuCodingAttachmentsList;

}
