package com.wms.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 角色枚举
 * @author li shuang
 */

@AllArgsConstructor
@Getter
public enum RoleEnum {

    SYS("sys","系统管理员"),
    SECTION_CHIEF("sectionChief","销售科长"),
    INTER_TWO("inter-two","跟单员(只能看到跟单数据)"),
    TECH_REVIEW("techReview","技术评审"),
    ADMIN("admin","超级管理员"),
    COMMON("common","业务超级角色"),
    SALER("saler","销售员"),
    DIRECTOR("director","销售总监"),
    CEO("ceo","总裁"),
    UPDATE_PAPER_CONT("updatePaperCont","修改纸质合同"),
    AUDITOR("auditor","计划评审"),
    SEARCH("search","查看角色"),
    SALES_ASSESSMENT("salesAssessment","销售考核"),
    CONT_TERMS("contTerms","合同条款"),
    INTER("inter","跟单员");

    private final String code;
    private final String desc;
}
