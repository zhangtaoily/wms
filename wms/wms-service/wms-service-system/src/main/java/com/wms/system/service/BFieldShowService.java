package com.wms.system.service;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.common.core.domain.PageQuery;
import com.wms.common.enums.FunctionEnum;
import com.wms.domain.SysFieldConfig;
import com.wms.domain.basic.BFieldShow;
import com.wms.domain.view.BFieldShowDataView;
import com.wms.mapper.BFieldShowMapper;
import com.wms.page.DynamicsTableDataInfo;
import com.wms.service.common.service.AbstractDynamicsFormService;
import com.wms.service.common.service.TableNode;
import com.wms.system.service.impl.SysFieldConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户自定义列配置（动态列表引擎支撑服务）
 *
 * @author li-shuang
 */
@Slf4j
@Service
public class BFieldShowService extends AbstractDynamicsFormService<BFieldShow, BFieldShowMapper> {
    @Resource
    private BFieldShowMapper fieldShowMapper;

    @Resource
    private SysFieldConfigService fieldConfigService;

    @Override
    public BFieldShowMapper supplierMapper() {
        return fieldShowMapper;
    }

    @Override
    public TableNode doFetchTableNode() {
        //主表
        TableNode root = new TableNode();
        root.setTableClass(BFieldShow.class);
        root.setTableName(TableInfoHelper.getTableInfo(BFieldShow.class).getTableName());
        root.setMapper(BFieldShowMapper.class);
        root.setParentTable(null);
        root.setPrimaryKey(TableInfoHelper.getTableInfo(BFieldShow.class).getKeyColumn());
        root.setJoinColumns("sys_field_config.header_text headerText");
        root.setJoinTables("left join sys_field_config on sys_field_config.id = b_field_show.field_config_id");
        root.setOrderBy(" order by sort_index");
        return root;
    }

    @Override
    public FunctionEnum getSupportType() {
        return FunctionEnum.B_FIELD_SHOW;
    }

    @Override
    public DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> queryPageList(PageQuery pageQuery) {
        BFieldShowDataView bFieldShowDataView = new BFieldShowDataView();
        return super.queryTablePageList(bFieldShowDataView.fetchDataView(), pageQuery);
    }

    /**
     * 获取用户对应模块下面的字段配置id列表
     *
     * @param functionName 模块名称
     * @param userId       用户id
     * @return R
     */
    public List<Long> getFieldConfigIdsByFunctionNameAndUserId(String functionName, Long userId) {
        List<BFieldShow> list = list(wrapper -> wrapper.eq(BFieldShow::getModuleName, functionName).eq(BFieldShow::getUserId, userId));
        return list.stream().map(BFieldShow::getFieldConfigId).collect(Collectors.toList());
    }


    /**
     * 恢复默认值
     * 删除用户对应模块下面的配置
     *
     * @param functionEnum 模块名称
     * @param userId       用户id
     * @return R
     */
    public int deleteByUserIdAndFunctionName(FunctionEnum functionEnum, Long userId) {
        return delete(wrapper -> wrapper.eq(BFieldShow::getUserId, userId).eq(BFieldShow::getModuleName, functionEnum));
    }

    /**
     * 更新用户对应模块下面的配置
     *
     * @param functionEnum 模块名称
     * @param userId       用户id
     * @return Boolean
     */
    public Boolean updatedByUserIdAndFunctionName(FunctionEnum functionEnum, Long userId) {
        String functionName = functionEnum.getCode();
        // 最新数据，配置id
        List<Long> fieldConfigIdList = fieldConfigService.getFieldConfigIdsByFunctionName(functionName);
        // 用户配置的列表数据，关联的配置id
        List<Long> fieldShowIdList = getFieldConfigIdsByFunctionNameAndUserId(functionName, userId);

        // 找出删除的数据
        // 找出需要删除的数据：用户配置的ID列表中有，但最新数据中没有的ID
        List<Long> toDeleteIds = fieldShowIdList.stream()
            .filter(id -> !fieldConfigIdList.contains(id))
            .collect(Collectors.toList());

        // 找出新增的数据
        // 找出需要新增的数据：最新数据中有，但用户配置的ID列表中没有的ID
        List<Long> toAddIds = fieldConfigIdList.stream()
            .filter(id -> !fieldShowIdList.contains(id))
            .collect(Collectors.toList());

        // 删除多余的配置
        if (!toDeleteIds.isEmpty()) {
            delete(wrapper -> wrapper.eq(BFieldShow::getModuleName, functionName).eq(BFieldShow::getUserId, userId).in(BFieldShow::getFieldConfigId, toDeleteIds));
        }
        // 新增新配置
        if (!toAddIds.isEmpty()) {
            List<BFieldShow> fieldShowList = new ArrayList<>();
            // 查询字段配置表的数据
            List<SysFieldConfig> list = fieldConfigService.list(wrapper -> wrapper.in(SysFieldConfig::getId, toAddIds));
            // 转换为用户自定义列的数据
            for (SysFieldConfig sysFieldConfig : list) {
                BFieldShow fieldShow = new BFieldShow();
                fieldShow.setIsShow(sysFieldConfig.getIsShow());
                fieldShow.setUserId(userId);
                fieldShow.setModuleName(functionName);
                fieldShow.setFieldConfigId(sysFieldConfig.getId());
                fieldShow.setSortIndex(sysFieldConfig.getSortIndex());
                fieldShow.setColumnWidth(Integer.valueOf(sysFieldConfig.getColumnWidth()));
                fieldShowList.add(fieldShow);
            }
            // 插入数据
            saveBatch(fieldShowList);
        }
        return true;
    }
}
