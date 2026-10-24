package com.wms.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.common.api.BasicBO;
import com.wms.common.core.domain.PageQuery;
import com.wms.common.enums.FunctionEnum;
import com.wms.common.utils.JsonUtils;
import com.wms.common.utils.Tuple2;
import com.wms.domain.SysFieldConfig;
import com.wms.domain.system.SysEmployee;
import com.wms.domain.view.SysEmployeeDataView;
import com.wms.page.DynamicsTableDataInfo;
import com.wms.service.common.service.AbstractDynamicsFormService;
import com.wms.service.common.service.TableNode;
import com.wms.system.mapper.SysEmployeeMapper;
import com.wms.service.common.spi.ISysEmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Service
public class SysEmployeeService extends AbstractDynamicsFormService<SysEmployee, SysEmployeeMapper> implements ISysEmployeeService {

    @Resource
    private SysEmployeeMapper SysEmployeeMapper;

    @Override
    public SysEmployeeMapper supplierMapper() {
        return SysEmployeeMapper;
    }

    @Override
    public TableNode doFetchTableNode() {
        //主表
        TableNode root = new TableNode();
        root.setTableClass(SysEmployee.class);
        root.setMapper(SysEmployeeMapper.class);
        root.setTableName(TableInfoHelper.getTableInfo(SysEmployee.class).getTableName());
        root.setPrimaryKey(TableInfoHelper.getTableInfo(SysEmployee.class).getKeyColumn());
        root.setParentTable(null);
        root.setJoinColumns("oa.name oaName,manager.name managerName");
        root.setJoinTables(" left join sys_employee oa on oa.id = sys_employee.oa_id" +
            " left join sys_employee manager on manager.id = sys_employee.manager_id");

        return root;
    }
    @Override
    public FunctionEnum getSupportType() {
        return FunctionEnum.SYS_EMPLOYEE;
    }

    @Override
    public DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> queryPageList(PageQuery pageQuery) {
        SysEmployeeDataView sysEmployeeDataView=new SysEmployeeDataView();
        return super.queryTablePageList(sysEmployeeDataView.fetchDataView(), pageQuery);
    }

    @Override
    public Map<String, Object> initBind() {
        return null;
    }

    @Override
    public void afterPass(Long id,
                          TableNode root,
                          Map<String, List<Map<String, Object>>> dbOldData,
                          Map<String, List<Map<String, Object>>> dbNewData) {

        super.afterPass(id, root, dbOldData, dbNewData);
    }

    @Override
    public SysEmployee getSysEmployee(Long id) {
        SysEmployee sysEmployee =  get(id);
        if(sysEmployee ==null){
            throw new RuntimeException("用户没有关联OA账号!");
        }
        return sysEmployee;
    }

    @Override
    public Tuple2<Boolean, Object> upsert(SysEmployee employee) {
        // 根据oaId查找已有记录
        SysEmployee existing = null;
        if (employee.getOaId() != null) {
            existing = get(query -> query.eq(SysEmployee::getOaId, employee.getOaId()));
        }
        // 如果已存在，设置id走更新逻辑
        if (existing != null) {
            employee.setId(existing.getId());
            employee.setUpdateTime(existing.getUpdateTime());
        }

        String entityJson = JsonUtils.toJsonString(employee);
        Map<String, Object> dict = JsonUtils.parseMap(entityJson);
        Tuple2<Integer, Object> tuple2 = save(BasicBO.valueOf(dict));
        if (tuple2.isSuccess()) {
            return Tuple2.of(true, tuple2.getSecond());
        }
        log.error("员工数据同步异常:{}, 场景数据:{}", tuple2.getSecond(), entityJson);
        return Tuple2.of(false, tuple2.getSecond());
    }
}
