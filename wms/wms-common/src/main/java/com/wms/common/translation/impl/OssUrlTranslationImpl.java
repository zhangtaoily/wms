package com.wms.common.translation.impl;

import com.wms.common.annotation.TranslationType;
import com.wms.common.constant.TransConstant;
import com.wms.common.core.service.OssService;
import com.wms.common.translation.TranslationInterface;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * OSS翻译实现
 *
 * @author Lion Li
 */
@Component
@AllArgsConstructor
@TranslationType(type = TransConstant.OSS_ID_TO_URL)
public class OssUrlTranslationImpl implements TranslationInterface<String> {

    private final OssService ossService;

    @Override
    public String translation(Object key, String other) {
        return ossService.selectUrlByIds(key.toString());
    }
}
