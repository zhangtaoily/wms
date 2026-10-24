package com.wms.common.translation.impl;

import com.wms.common.annotation.TranslationType;
import com.wms.common.constant.TransConstant;
import com.wms.common.core.service.DeptService;
import com.wms.common.translation.TranslationInterface;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 部门翻译实现
 *
 * @author Lion Li
 */
@Component
@AllArgsConstructor
@TranslationType(type = TransConstant.DEPT_ID_TO_NAME)
public class DeptNameTranslationImpl implements TranslationInterface<String> {

    private final DeptService deptService;

    @Override
    public String translation(Object key, String other) {
        return deptService.selectDeptNameByIds(key.toString());
    }
}
