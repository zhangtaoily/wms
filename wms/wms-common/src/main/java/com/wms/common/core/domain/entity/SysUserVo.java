package com.wms.common.core.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 用户对象 sys_user
 *
 * @author Lion Li
 */

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUserVo extends SysUser {
    /**
     * 销售组织
     */
    private String salOrgCnName;
    /**
     * 备货组织
     */
    private String prdOrgCnName;
    /**
     * 默认销售员名称
     */
    private String salerName;

}
