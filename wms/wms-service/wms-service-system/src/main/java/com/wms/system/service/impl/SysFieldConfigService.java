package com.wms.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.core.domain.PageQuery;
import com.wms.common.core.page.TableDataInfo;
import com.wms.common.enums.FunctionEnum;
import com.wms.common.utils.ListUtils;
import com.wms.common.utils.StringUtils;
import com.wms.domain.SysFieldConfig;
import com.wms.domain.basic.BFieldShow;
import com.wms.domain.system.SysFilterSchemeDetail;
import com.wms.mapper.BFieldShowMapper;
import com.wms.mapper.SysFieldConfigMapper;
import com.wms.page.DynamicsTableDataInfo;
import com.wms.service.common.service.AbstractDynamicsFormService;
import com.wms.service.common.service.TableNode;
import com.wms.spi.ISysFieldConfigService;
import com.wms.system.service.SysFilterSchemeDetailService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.wms.common.helper.LoginHelper.getUserId;

@Service
public class SysFieldConfigService extends AbstractDynamicsFormService<SysFieldConfig, SysFieldConfigMapper> implements ISysFieldConfigService {

    @Resource
    private SysFieldConfigMapper baseMapper;
    @Resource
    private BFieldShowMapper fieldShowMapper;
    @Resource
    private SysFilterSchemeDetailService filterSchemeDetailService;

    public List<SysFieldConfig> listDynamicField(FunctionEnum name) {
        LambdaQueryWrapper<SysFieldConfig> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SysFieldConfig::getName, name);
        List<SysFieldConfig> sysFieldConfigs = baseMapper.selectList(lqw);
        // 查找用户自定义列 表数据
        LambdaQueryWrapper<BFieldShow> fieldShowLambdaQueryWrapper  = new LambdaQueryWrapper<>();
        fieldShowLambdaQueryWrapper.eq(BFieldShow::getModuleName, name);
        fieldShowLambdaQueryWrapper.eq(BFieldShow::getUserId,getUserId());
        List<BFieldShow> fieldShows = fieldShowMapper.selectList(fieldShowLambdaQueryWrapper);
        if(!fieldShows.isEmpty()){
            Map<Long, BFieldShow> bfieldShowMap = fieldShows.stream()
                .collect(Collectors.toMap(BFieldShow::getFieldConfigId, Function.identity()));
            // 记录需要删除的id
            List<Long> deleteIds = ListUtils.empty();
            for (SysFieldConfig sysFieldConfig : sysFieldConfigs) {
                BFieldShow bFieldShow = bfieldShowMap.get(sysFieldConfig.getId());
                if (bFieldShow != null) {
                    sysFieldConfig.setSortIndex(bFieldShow.getSortIndex());
                    sysFieldConfig.setIsShow(bFieldShow.getIsShow());
                    sysFieldConfig.setColumnWidth(String.valueOf(bFieldShow.getColumnWidth()));
                }else{
                    deleteIds.add(sysFieldConfig.getId());
                }
            }
            // 删除不在用户自定义列的列
            if(!deleteIds.isEmpty()){
                sysFieldConfigs.removeIf(item -> deleteIds.contains(item.getId()));
            }
        }
        return sysFieldConfigs;
    }

    public List<SysFieldConfig> listSearchField(FunctionEnum name) {
        LambdaQueryWrapper<SysFieldConfig> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SysFieldConfig::getName, name);
        lqw.eq(SysFieldConfig::getSearch, 1);
        lqw.orderByAsc(SysFieldConfig::getSortIndex);
        List<SysFieldConfig> sysFieldConfigs = baseMapper.selectList(lqw);

        return sysFieldConfigs;
    }

    @Override
    public List<SysFieldConfig> listField(FunctionEnum name) {
        LambdaQueryWrapper<SysFieldConfig> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SysFieldConfig::getName, name);
        lqw.orderByAsc(SysFieldConfig::getSortIndex);
        return baseMapper.selectList(lqw);
    }

    /** PDA 引擎字段配置（M6）：mobile_show=1 的列按 mobile_seq 升序（空值排末尾），scan_input 标记扫码输入 */
    @Override
    public List<Map<String, Object>> listMobileField(FunctionEnum name) {
        List<SysFieldConfig> configs = listField(name);
        List<Map<String, Object>> rows = new java.util.ArrayList<>();
        for (SysFieldConfig item : configs) {
            if (item.getMobileShow() == null || item.getMobileShow() != 1) {
                continue;
            }
            Map<String, Object> row = new TreeMap<>();
            row.put("columnName", item.getColumnName());
            row.put("headerText", item.getHeaderText());
            row.put("fieldType", item.getFieldType());
            row.put("sqlColumn", item.getSqlColumn());
            row.put("scanInput", item.getScanInput());
            row.put("mobileSeq", item.getMobileSeq());
            row.put("columnWidth", item.getColumnWidth());
            rows.add(row);
        }
        rows.sort(java.util.Comparator.comparing(
            m -> m.get("mobileSeq") == null ? Integer.MAX_VALUE : (Integer) m.get("mobileSeq")));
        return rows;
    }

    public List<String> getColumns(FunctionEnum name) {
        List<SysFieldConfig> configs = listDynamicField(name);

        List<String> columns = ListUtils.empty();
        for (SysFieldConfig item : configs) {
            String column = String.format("%s %s", item.getSqlColumn(), item.getColumnName());
            columns.add(column);
        }

        return columns;
    }

    public List<String> getWrapperColumns(FunctionEnum name) {
        List<SysFieldConfig> configs = listDynamicField(name);

        List<String> columns = ListUtils.empty();
        for (SysFieldConfig item : configs) {
            String column = String.format("(%s) %s", item.getSqlColumn(), item.getColumnName());
            columns.add(column);
        }

        return columns;
    }

    @Override
    public SysFieldConfigMapper supplierMapper() {
        return baseMapper;
    }

    @Override
    public TableNode doFetchTableNode() {
        TableNode root = new TableNode();
        root.setTableClass(SysFieldConfig.class);
        root.setMapper(SysFieldConfigMapper.class);
        root.setTableName(TableInfoHelper.getTableInfo(SysFieldConfig.class).getTableName());
        root.setPrimaryKey(TableInfoHelper.getTableInfo(SysFieldConfig.class).getKeyColumn());
        return root;
    }

    @Override
    public FunctionEnum getSupportType() {
        return FunctionEnum.FIELD_CONFIG;
    }

    @Override
    public DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> queryPageList(PageQuery pageQuery) {
        return super.queryTablePageList(SysFieldConfig.class, pageQuery);
    }
    /**
     * 根据用户ID查找出对应的ID列表
     * @param functionName 模块名称
     * @return ID列表
     */
    public List<Long> getFieldConfigIdsByFunctionName(String functionName) {
        List<SysFieldConfig> list = list(wrapper -> wrapper.eq(SysFieldConfig::getName, functionName));
        return list.stream().map(SysFieldConfig::getId).collect(Collectors.toList());
    }

    @Override
    public void afterDelete(Long id) {
        super.afterDelete(id);
        // 需要删除过滤方案中存在的数据
        filterSchemeDetailService.delete(wrapper -> wrapper.eq(SysFilterSchemeDetail::getFieldConfigId, id));
        // 需要删除字段显示配置中存在的数据
        LambdaQueryWrapper<BFieldShow> lqw = Wrappers.lambdaQuery();
        lqw.eq(BFieldShow::getFieldConfigId, id);
        fieldShowMapper.delete(lqw);
    }

    @Override
    public TableDataInfo<SysFieldConfig> pageFieldConfig(SysFieldConfig fieldConfig, PageQuery pageQuery) {
        LambdaQueryWrapper<SysFieldConfig> lqw = new LambdaQueryWrapper<>();
        lqw.eq(StringUtils.isNotBlank(fieldConfig.getName()), SysFieldConfig::getName, fieldConfig.getName());
        lqw.eq(StringUtils.isNotBlank(fieldConfig.getModuleGroup()), SysFieldConfig::getModuleGroup, fieldConfig.getModuleGroup());
        lqw.like(StringUtils.isNotBlank(fieldConfig.getHeaderText()), SysFieldConfig::getHeaderText, fieldConfig.getHeaderText());
        lqw.orderByAsc(SysFieldConfig::getName).orderByAsc(SysFieldConfig::getSortIndex);
        Page<SysFieldConfig> page = baseMapper.selectPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    @Override
    public SysFieldConfig selectFieldConfigById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public int insertFieldConfig(SysFieldConfig fieldConfig) {
        return baseMapper.insert(fieldConfig);
    }

    @Override
    public int updateFieldConfig(SysFieldConfig fieldConfig) {
        return baseMapper.updateById(fieldConfig);
    }

    @Override
    public int deleteFieldConfigByIds(Long[] ids) {
        int rows = 0;
        for (Long id : ids) {
            if (baseMapper.deleteById(id) > 0) {
                // 级联清理过滤方案明细与字段显示配置
                afterDelete(id);
                rows++;
            }
        }
        return rows;
    }
}
