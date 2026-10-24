package com.wms.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.domain.SynErrorMessage;
import com.wms.system.mapper.SynErrorMessageMapper;
import com.wms.system.service.SynErrorMessageService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class SynErrorMessageServiceImpl implements SynErrorMessageService {

    @Resource
    private SynErrorMessageMapper synErrorMessageMapper;

    public List<SynErrorMessage> getAll(Integer pageNum, Integer pageSize, String username) {
        LambdaQueryWrapper<SynErrorMessage> synErrorMessageWrapper = new LambdaQueryWrapper<>();

        // 创建 Page 对象，指定当前页码和每页大小
        Page<SynErrorMessage> page = new Page<>(pageNum, pageSize);
        Page<SynErrorMessage> pageResult = null;
        if (username.equals("admin")){
            pageResult = synErrorMessageMapper.selectPage(page, synErrorMessageWrapper.orderByDesc(SynErrorMessage::getDocDate));
        }else {
            pageResult = synErrorMessageMapper.selectPage(page, synErrorMessageWrapper.orderByDesc(SynErrorMessage::getDocDate)
                .eq(SynErrorMessage::getCreateBy, username));
        }

        return pageResult.getRecords();
    }
}
