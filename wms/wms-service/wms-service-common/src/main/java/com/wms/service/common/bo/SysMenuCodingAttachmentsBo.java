package com.wms.service.common.bo;

import com.wms.common.core.validate.AddGroup;
import com.wms.common.core.validate.EditGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;

import java.util.Date;

import com.wms.common.core.domain.BaseEntity;

/**
 * 菜单编码业务对象 sys_menu_coding_attachments
 *
 * @author ruoyi
 * @date 2024-08-16
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class SysMenuCodingAttachmentsBo extends BaseEntity {



    /**
     * 功能名称
     */
    @NotBlank(message = "功能名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String functionName;

    /**
     * 前缀
     */
    @NotBlank(message = "前缀不能为空", groups = { AddGroup.class, EditGroup.class })
    private String codingPrefix;

    /**
     * 编码前缀缀长度
     */
    @NotBlank(message = "编码前缀缀长度不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long codingLength;

    /**
     * 前缀种类
     */
    @NotBlank(message = "前缀种类不能为空", groups = { AddGroup.class, EditGroup.class })
    private String codingType;

    /**
     * 顺序
     */
    @NotNull(message = "顺序不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long sort;


}
