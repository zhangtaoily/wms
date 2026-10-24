package com.wms.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * 系统设置 sys_setting（键值型系统参数）
 */
@Data
@TableName("sys_setting")
public class SysSetting {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 设置编码（唯一）
     */
    @NotBlank(message = "设置编码不能为空")
    private String settingCode;

    /**
     * 设置名称
     */
    @NotBlank(message = "设置名称不能为空")
    private String settingName;

    /**
     * 设置内容
     */
    private String content;

    private String creator;

    private LocalDateTime createTime;

    private String updater;

    private LocalDateTime updateTime;
}
