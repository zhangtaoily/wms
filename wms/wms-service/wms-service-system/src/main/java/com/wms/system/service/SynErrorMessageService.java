package com.wms.system.service;
import com.wms.domain.SynErrorMessage;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SynErrorMessageService{
    public List<SynErrorMessage> getAll(Integer pageNum, Integer pageSize,String username);
}
