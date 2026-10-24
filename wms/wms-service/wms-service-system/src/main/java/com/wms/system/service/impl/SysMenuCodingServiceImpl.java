package com.wms.system.service.impl;

import com.wms.common.enums.FunctionEnum;
import com.wms.service.common.spi.ISysMenuCodingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 菜单编码Service业务层处理
 *
 * @author ruoyi
 * @date 2024-08-16
 */
@RequiredArgsConstructor
@Service
public class SysMenuCodingServiceImpl implements ISysMenuCodingService {


    @Override
    public List<Map<String, String>> template() {
        List<Map<String, String>> cessList = new ArrayList<>();
        for (FunctionEnum value : FunctionEnum.values()) {
            Map<String, String> map = new HashMap<>();
            map.put("key", value.getCode());
            map.put("value", value.getDesc());
            cessList.add(map);
        }

        return cessList;
    }

}
