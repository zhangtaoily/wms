package com.wms.common.translation.impl;

import com.wms.common.annotation.TranslationType;
import com.wms.common.constant.TransConstant;
import com.wms.common.core.service.UserService;
import com.wms.common.translation.TranslationInterface;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 用户名翻译实现
 *
 * @author Lion Li
 */
@Component
@AllArgsConstructor
@TranslationType(type = TransConstant.USER_ID_TO_NAME)
public class UserNameTranslationImpl implements TranslationInterface<String> {

    private final UserService userService;

    @Override
    public String translation(Object key, String other) {
        if (key instanceof Long) {
            return userService.selectUserNameById((Long) key);
        }
        return null;
    }
}
