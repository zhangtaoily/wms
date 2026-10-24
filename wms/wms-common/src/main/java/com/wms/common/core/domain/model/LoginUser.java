package com.wms.common.core.domain.model;

import com.wms.common.core.domain.dto.RoleDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * 登录用户身份权限
 *
 * @author Lion Li
 */

@Data
@NoArgsConstructor
public class LoginUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 部门名
     */
    private String deptName;

    /**
     * 用户唯一标识
     */
    private String token;

    /**
     * 用户类型
     */
    private String userType;

    /**
     * 登录时间
     */
    private Long loginTime;

    /**
     * 过期时间
     */
    private Long expireTime;

    /**
     * 登录IP地址
     */
    private String ipaddr;

    /**
     * 登录地点
     */
    private String loginLocation;

    /**
     * 浏览器类型
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 菜单权限
     */
    private Set<String> menuPermission;

    /**
     * 角色权限
     */
    private Set<String> rolePermission;

    /**
     * 用户名
     */
    private String username;

    /**
     * 角色对象
     */
    private List<RoleDTO> roles;

    /**
     * 数据权限 当前角色ID
     */
    private Long roleId;

    /**
     * 员工工号
     */
    private String employeeNo;

    /**
     * 员工工号
     */
    private Long employeeId;
    /**
     * OAID
     */
    private Long oaId;

    /**
     * 获取登录id
     */
    public String getLoginId() {
        if (userType == null) {
            throw new IllegalArgumentException("用户类型不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        return userType + ":" + userId;
    }

    /**
     * 销售科长
     *
     * @return
     */
    public boolean isSectionChief() {
        if (roles == null) {
            throw new IllegalArgumentException("用户没有授予角色");
        }
        return roles.stream().anyMatch(item -> "sectionChief".equals(item.getRoleKey()));
    }

    /**
     * 销售总监
     *
     * @return
     */
    public boolean isDirector() {
        if (roles == null) {
            throw new IllegalArgumentException("用户没有授予角色");
        }
        return roles.stream().anyMatch(item -> "director".equals(item.getRoleKey()));
    }

    /**
     * 是否是总监或者销售科长 、 考核科
     *
     * @return
     */
    public boolean isDirectorOrSectionChief() {
        if (roles == null) {
            throw new IllegalArgumentException("用户没有授予角色");
        }
        return roles.stream().anyMatch(item -> "director".equals(item.getRoleKey()) || "salesAssessment".equals(item.getRoleKey()) ||
                "sectionChief".equals(item.getRoleKey()));
    }
}
