package com.wms.system.service;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.common.constant.Constants;
import com.wms.common.core.domain.PageQuery;
import com.wms.common.enums.FunctionEnum;
import com.wms.common.utils.ListUtils;
import com.wms.domain.SysFieldConfig;
import com.wms.domain.system.SysFilterScheme;
import com.wms.domain.system.SysFilterSchemeDetail;
import com.wms.domain.view.SysFilterSchemeDataView;
import com.wms.page.DynamicsTableDataInfo;
import com.wms.service.common.service.AbstractDynamicsFormService;
import com.wms.service.common.service.TableNode;
import com.wms.service.common.utils.TableNodeHelper;
import com.wms.system.mapper.SysFilterSchemeDetailMapper;
import com.wms.system.mapper.SysFilterSchemeMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.wms.common.helper.LoginHelper.getUserId;

@Service
public class SysFilterSchemeService extends AbstractDynamicsFormService<SysFilterScheme, SysFilterSchemeMapper> {
    @Resource
    private SysFilterSchemeMapper sysFilterSchemeMapper;

    @Override
    public SysFilterSchemeMapper supplierMapper() {
        return sysFilterSchemeMapper;
    }

    @Override
    public TableNode doFetchTableNode() {
        //主表
        TableNode root = new TableNode();
        root.setTableClass(SysFilterScheme.class);
        root.setMapper(SysFilterSchemeMapper.class);
        root.setTableName(TableInfoHelper.getTableInfo(SysFilterScheme.class).getTableName());
        root.setPrimaryKey(TableInfoHelper.getTableInfo(SysFilterScheme.class).getKeyColumn());
        root.setJoinColumns(TableNodeHelper.getCommonJoinColumns());
        root.setJoinTables(TableNodeHelper.getCommonJoinTables(root.getTableName()));

        //子表请求参数
        TableNode detail = new TableNode();
        detail.setTableClass(SysFilterSchemeDetail.class);
        detail.setParentTable(TableInfoHelper.getTableInfo(SysFilterScheme.class).getTableName());
        detail.setMapper(SysFilterSchemeDetailMapper.class);
        detail.setForeignKey("filter_scheme_id");
        detail.setTableName(TableInfoHelper.getTableInfo(SysFilterSchemeDetail.class).getTableName());
        detail.setPrimaryKey(TableInfoHelper.getTableInfo(SysFilterSchemeDetail.class).getKeyColumn());
        detail.setOrderBy("order by sys_filter_scheme_detail.sort_index");
        detail.setJoinColumns(" sys_field_config.column_name columnName,sys_field_config.field_type fieldType,sys_field_config.header_text headerText");
        detail.setJoinTables(" left join sys_field_config on sys_field_config.id = sys_filter_scheme_detail.field_config_id ");
        //主表添加子表
        root.setChildren(ListUtils.of(detail));
        return root;
    }

    @Override
    public FunctionEnum getSupportType() {
        return FunctionEnum.SYS_FILTER_SCHEME;
    }

    @Override
    public DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> queryPageList(PageQuery pageQuery) {
        SysFilterSchemeDataView sysFilterSchemeDataView = new SysFilterSchemeDataView();
        return super.queryTablePageList(sysFilterSchemeDataView.fetchDataView(), pageQuery);
    }

    /**
     * 保存后，如果选择了默认方案，需要把其他方案的《是否默认方案》 勾选为<否>
     *
     * @param root
     * @param oldTableEntityMap
     * @param tableEntityMap
     */
    @Override
    public void afterSave(TableNode root, Map<String, List<Map<String, Object>>> oldTableEntityMap, Map<String, List<Object>> tableEntityMap) {
        super.afterSave(root, oldTableEntityMap, tableEntityMap);
        SysFilterScheme newData = (SysFilterScheme) tableEntityMap.get(root.getTableName()).get(0);
        // 判断是否勾选了默认方案
        if (newData.getIsDefault().equals(Constants.YES)) {
            if (oldTableEntityMap != null) {
                Map<String, Object> oldData = oldTableEntityMap.get(root.getTableName()).get(0);
                if (!oldData.get("isDefault").equals(Constants.NO)) {
                    return;
                }
            }
            // 修改其他方案为非默认
            updateBatch(
                set -> set.set(SysFilterScheme::getIsDefault, Constants.NO),
                where -> where.eq(SysFilterScheme::getUserId, getUserId())
                    .eq(SysFilterScheme::getFunctionName, newData.getFunctionName())
                    .ne(SysFilterScheme::getId, newData.getId()));
        }
    }

    /**
     * 查询默认方案
     *
     * @param functionEnum 模块枚举
     * @return Map
     */
    public SysFilterScheme getDefaultScheme(FunctionEnum functionEnum) {
        // 用户id
        Long userId = getUserId();
        // 模块名称
        String functionName = functionEnum.getCode();
        // 查询默认方案
        SysFilterScheme defaultFilterScheme = get(query -> query
                                                    .eq(SysFilterScheme::getFunctionName, functionName)
                                                    .eq(SysFilterScheme::getUserId, userId)
                                                    .eq(SysFilterScheme::getIsDefault, Constants.YES));
        if (defaultFilterScheme == null) {
            return null;
            // 查询admin的默认方案 ,为系统默认方案
//            return get(query -> query
//                        .eq(SysFilterScheme::getFunctionName, functionName)
//                        .eq(SysFilterScheme::getUserId, UserConstants.ADMIN_ID)
//                        .eq(SysFilterScheme::getIsDefault, Constants.YES));
        } else {
            return defaultFilterScheme;
        }
    }

    /**
     * 查询默认方案详情列表
     *
     * @param functionEnum 模块枚举
     * @return Map
     */
    public List<Map<String, Object>> getDefaultSchemeDetails(FunctionEnum functionEnum) {
        SysFilterScheme defaultScheme = getDefaultScheme(functionEnum);
        List<Map<String, Object>> maps = new ArrayList<>();
        if(defaultScheme != null){
            Map<String, List<Map<String, Object>>> map = doLoad(defaultScheme.getId());
            maps = map.get(TableInfoHelper.getTableInfo(SysFilterSchemeDetail.class).getTableName());
        }
        return maps;
    }
}
