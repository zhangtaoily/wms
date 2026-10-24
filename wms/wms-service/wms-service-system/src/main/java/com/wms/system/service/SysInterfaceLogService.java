package com.wms.system.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.core.domain.PageQuery;
import com.wms.common.core.domain.model.LoginUser;
import com.wms.common.enums.FunctionEnum;
import com.wms.common.helper.LoginHelper;
import com.wms.common.utils.ListUtils;
import com.wms.domain.SynErrorMessage;
import com.wms.domain.SysFieldConfig;
import com.wms.domain.SysInterfaceLog;
import com.wms.domain.system.SysEmployee;
import com.wms.domain.view.InterfaceLogDataView;
import com.wms.page.DynamicsTableDataInfo;
import com.wms.service.common.service.AbstractDynamicsFormService;
import com.wms.service.common.service.TableNode;
import com.wms.service.common.spi.ISysInterfaceLogService;
import com.wms.service.common.utils.TableNodeHelper;
import com.wms.system.mapper.InterfaceLogMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;

@Service
public class SysInterfaceLogService extends AbstractDynamicsFormService<SysInterfaceLog, InterfaceLogMapper> implements ISysInterfaceLogService {
    @Resource
    private InterfaceLogMapper interfaceLogMapper;

    @Override
    public InterfaceLogMapper supplierMapper() {
        return interfaceLogMapper;
    }

    @Override
    public TableNode doFetchTableNode() {
        //主表
        TableNode root = new TableNode();
        root.setTableClass(SysInterfaceLog.class);
        root.setMapper(InterfaceLogMapper.class);
        root.setTableName(TableInfoHelper.getTableInfo(SysInterfaceLog.class).getTableName());
        root.setPrimaryKey(TableInfoHelper.getTableInfo(SysInterfaceLog.class).getKeyColumn());
        root.setJoinColumns(TableNodeHelper.getCommonJoinColumns());
        root.setJoinTables(TableNodeHelper.getCommonJoinTables(root.getTableName()));

        return root;
    }

    @Override
    public FunctionEnum getSupportType() {
        return FunctionEnum.SYS_INTERFACE_LOG;
    }

    @Override
    public DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> queryPageList(PageQuery pageQuery) {
        InterfaceLogDataView interfaceLogDataView = new InterfaceLogDataView();
        return super.queryTablePageList(interfaceLogDataView.fetchDataView(), pageQuery);
    }

    public List<SynErrorMessage> getAll(Integer pageNum, Integer pageSize) {
        Long userId = 1L;
        LoginUser loginUser = LoginHelper.getLoginUser();
        if (loginUser != null) {
            userId = loginUser.getUserId();
        }
        // 创建 Page 对象，指定当前页码和每页大小
        Page<SysInterfaceLog> page = new Page<>(pageNum, pageSize);
        LocalDate date = LocalDate.now().minusDays(1);
        List<SysInterfaceLog> pageResult = interfaceLogMapper.selectPageByUser(page.offset(),page.getSize(), userId, date);
        List<SynErrorMessage> errorMessages = new ArrayList<>();
        pageResult.forEach(sysInterfaceLog -> {
            SynErrorMessage synErrorMessage = new SynErrorMessage();
            String responseMsg = sysInterfaceLog.getResponseMsg();
            if (StringUtils.isNotEmpty(responseMsg) && responseMsg.contains("\"code\"")) {
                // 去除多余空格和换行符，确保 JSON 格式合法
                //把msg的截取出来
                String[] split = responseMsg.split("\"msg\":");
                synErrorMessage.setErrorMsg(split[1].substring(0, split[1].length() - 1));
            } else {
                synErrorMessage.setErrorMsg(responseMsg);
            }
            synErrorMessage.setDocId(sysInterfaceLog.getPactId());
            synErrorMessage.setDocCode(sysInterfaceLog.getPactCode());

            synErrorMessage.setDocDate(LocalDate.from(sysInterfaceLog.getCreateTime()));
            // WMS：单据类型按接口模型ID透传（CRM 单据类型映射已随业务代码移除）
            synErrorMessage.setDocType(sysInterfaceLog.getModelId());
            synErrorMessage.setDocTypeNum(0);
            errorMessages.add(synErrorMessage);
        });

        return errorMessages;
    }

    @Override
    public Boolean hasInProgressLog(Long id) {
        //最新的一条是待执行或者执行中的分发日志
        List<SysInterfaceLog> sysInterfaceLogs = this.list(query -> query.eq(SysInterfaceLog::getPactId, id)
            .orderByDesc(SysInterfaceLog::getCreateTime).last("limit 1"));

        if(ListUtils.isEmpty(sysInterfaceLogs)){
            return false;
        }

        return sysInterfaceLogs.get(0).getStatus().equals("0") || sysInterfaceLogs.get(0).getStatus().equals("1");
    }
}
