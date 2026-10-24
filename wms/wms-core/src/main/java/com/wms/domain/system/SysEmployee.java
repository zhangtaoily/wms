package com.wms.domain.system;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * 【请填写功能名称】对象 h_org_user
 *
 * @author ruoyi
 * @date 2024-08-09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_employee")
public class SysEmployee extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 是否被删除 0 否 ； 1 是
     */
    private Integer deleted;
    /**
     * 拓展字段1
     */
    private String extend1;
    /**
     *
     */
    private String extend2;
    /**
     *
     */
    private String extend3;
    /**
     *
     */
    private Long extend4;
    /**
     *
     */
    private Long extend5;
    /**
     * 是否在职
     */
    private Integer active;
    /**
     * 是否admin
     */
    private Integer admin;
    /**
     * 生日
     */
    private LocalDate birthday;
    /**
     * 是否老板
     */
    private Integer boss;

    /**
     * 离职时间
     */
    private LocalDate departureDate;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 员工编号
     */
    private String employeeNo;
    /**
     * 员工等级
     */
    private Long employeeRank;
    /**
     * 入职时间
     */
    private LocalDate entryDate;
    /**
     * 性别
     */
    private String gender;
    /**
     * 是否领导
     */
    private Integer leader;
    /**
     * 直属主管id
     */
    private Integer managerId;
    /**
     * 电话
     */
    private String mobile;
    /**
     * 姓名
     */
    private String name;
    /**
     * 密级
     */
    private String privacyLevel;
    /**
     * 状态
     */
    private String status;
    /**
     * 组织corpId
     */
    private String corpId;
    /**
     * 是否禁用
     */
    private Integer enabled;
    /**
     * 人事创建时间
     */
    private LocalDateTime hrcreateTime;
    /**
     * 人事更改时间
     */
    private LocalDateTime hrmodifyTime;
    /**
     * 备注
     */
    private String remark;
    private String departmentName;
    private Integer oaId;
    private String job;
    /**
     * OA密码
     */
    private String password;
    private Integer accountType;
    /**
     * 部门ID
     */
    private String departmentId;
}
