package com.wms.common.core.service;

/**
 * 通用 用户服务
 *
 * @author Lion Li
 */
public interface UserService {

    /**
     * 通过用户ID查询用户账户
     *
     * @param userId 用户ID
     * @return 用户账户
     */
    String selectUserNameById(Long userId);

    /**
     * 通过工号获取用户id
     * @param employeeNo
     * @return
     */
    Long getUserIdByEmployeeNo(String employeeNo);
}
