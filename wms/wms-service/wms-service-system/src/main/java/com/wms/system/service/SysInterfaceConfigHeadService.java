package com.wms.system.service;

import com.wms.domain.system.SysInterfaceConfigHead;
import com.wms.service.common.service.AbstractCrudService;
import com.wms.system.mapper.SysInterfaceConfigHeadMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class SysInterfaceConfigHeadService extends AbstractCrudService<SysInterfaceConfigHead, SysInterfaceConfigHeadMapper> {
    @Resource
    private SysInterfaceConfigHeadMapper sysInterfaceConfigHeadMapper;

    @Override
    public SysInterfaceConfigHeadMapper supplierMapper() {
        return sysInterfaceConfigHeadMapper;
    }
}
