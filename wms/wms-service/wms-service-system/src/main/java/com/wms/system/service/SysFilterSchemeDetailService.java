package com.wms.system.service;

import com.wms.domain.system.SysFilterSchemeDetail;
import com.wms.service.common.service.AbstractCrudService;
import com.wms.system.mapper.SysFilterSchemeDetailMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class SysFilterSchemeDetailService extends AbstractCrudService<SysFilterSchemeDetail, SysFilterSchemeDetailMapper> {
    @Resource
    private SysFilterSchemeDetailMapper sysFilterSchemeDetailMapper;

    @Override
    public SysFilterSchemeDetailMapper supplierMapper() {
        return sysFilterSchemeDetailMapper;
    }
}
