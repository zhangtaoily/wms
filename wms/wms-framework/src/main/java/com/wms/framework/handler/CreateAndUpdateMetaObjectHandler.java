package com.wms.framework.handler;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpStatus;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.wms.common.core.domain.BaseEntity;
import com.wms.common.core.domain.model.LoginUser;
import com.wms.common.exception.ServiceException;
import com.wms.common.helper.LoginHelper;
import com.wms.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * MP注入处理器
 *
 * @author Lion Li
 * @date 2021/4/25
 */
@Slf4j
public class CreateAndUpdateMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        try {
            if (ObjectUtil.isNotNull(metaObject) && metaObject.getOriginalObject() instanceof BaseEntity) {
                BaseEntity baseEntity = (BaseEntity) metaObject.getOriginalObject();
                LocalDateTime current = ObjectUtil.isNotNull(baseEntity.getCreateTime())
                    ? baseEntity.getCreateTime() : LocalDateTime.now();
                baseEntity.setCreateTime(current);
                baseEntity.setUpdateTime(current);
                String loginId = StringUtils.isNotBlank(baseEntity.getCreateBy())
                    ? baseEntity.getCreateBy() : getLoginId();
                // 当前已登录 且 创建人为空 则填充
                baseEntity.setCreateBy(loginId);
                // 当前已登录 且 更新人为空 则填充
                baseEntity.setUpdateBy(loginId);
            }
        } catch (Exception e) {
            throw new ServiceException("自动注入异常 => " + e.getMessage(), HttpStatus.HTTP_UNAUTHORIZED);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        try {
            if (ObjectUtil.isNotNull(metaObject) && metaObject.getOriginalObject() instanceof BaseEntity) {
                BaseEntity baseEntity = (BaseEntity) metaObject.getOriginalObject();
                baseEntity.setUpdateTime(LocalDateTime.now());
                String loginId = getLoginId();
                // 当前已登录 更新人填充(不管为不为空)
                if (StringUtils.isNotBlank(loginId)) {
                    baseEntity.setUpdateBy(loginId);
                }
            }
        } catch (Exception e) {
            throw new ServiceException("自动注入异常 => " + e.getMessage(), HttpStatus.HTTP_UNAUTHORIZED);
        }
    }

    /**
     * 获取登录用户ID
     */
    private String getLoginId() {
        LoginUser loginUser;
        try {
            loginUser = LoginHelper.getLoginUser();
        } catch (Exception e) {
            log.warn("自动注入警告 => 用户未登录");
            return null;
        }
        if (ObjectUtil.isNotNull(loginUser) && loginUser.getLoginId() != null) {
            return loginUser.getUserId().toString();
        }
        return null;
    }

}
