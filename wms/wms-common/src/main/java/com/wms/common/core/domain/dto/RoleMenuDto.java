package com.wms.common.core.domain.dto;

import lombok.Data;

/**
 * @author wac
 * @date 2024/8/8
 * @Description
 */
@Data
public class RoleMenuDto {
    private Long id;
    private Long roleId;
    private Long menuId;
    private String menuName;
    private String menuSql;
}
