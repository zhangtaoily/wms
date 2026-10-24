package com.wms.common.translation.impl;

import com.wms.common.annotation.TranslationType;
import com.wms.common.constant.TransConstant;
import com.wms.common.core.service.DictService;
import com.wms.common.translation.TranslationInterface;
import com.wms.common.utils.StringUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 字典翻译实现
 *
 * @author Lion Li
 */
@Component
@AllArgsConstructor
@TranslationType(type = TransConstant.DICT_TYPE_TO_LABEL)
public class DictTypeTranslationImpl implements TranslationInterface<String> {

    private final DictService dictService;

    @Override
    public String translation(Object key, String other) {
        if (key instanceof String && StringUtils.isNotBlank(other)) {
            return dictService.getDictLabel(other, key.toString());
        }
        return null;
    }
}
