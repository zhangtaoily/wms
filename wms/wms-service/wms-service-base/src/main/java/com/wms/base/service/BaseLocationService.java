package com.wms.base.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.base.domain.BaseLocation;
import com.wms.base.domain.BaseWarehouse;
import com.wms.base.domain.InvBatchStock;
import com.wms.base.mapper.BaseLocationMapper;
import com.wms.base.mapper.BaseWarehouseMapper;
import com.wms.base.mapper.InvBatchStockMapper;
import com.wms.base.view.LocationDataView;
import com.wms.common.api.BasicBO;
import com.wms.common.core.domain.PageQuery;
import com.wms.common.enums.FunctionEnum;
import com.wms.common.utils.Tuple2;
import com.wms.domain.SysFieldConfig;
import com.wms.page.DynamicsTableDataInfo;
import com.wms.service.common.service.TableNode;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * 仓位信息引擎服务：仓位编号唯一校验；立库四段式校验；删除约束（库存引用）
 */
@Service
public class BaseLocationService extends BaseWmsFormService<BaseLocation, BaseLocationMapper> {

    /** 立库四段式仓位编号，如 3-2-8-1 */
    private static final Pattern LIKU_LOC_PATTERN = Pattern.compile("^\\d+-\\d+-\\d+-\\d+$");

    @Resource
    private BaseLocationMapper locationMapper;
    @Resource
    private BaseWarehouseMapper warehouseMapper;
    @Resource
    private InvBatchStockMapper batchStockMapper;

    @Override
    public BaseLocationMapper supplierMapper() {
        return locationMapper;
    }

    @Override
    public TableNode doFetchTableNode() {
        TableNode root = new TableNode();
        root.setTableClass(BaseLocation.class);
        root.setMapper(BaseLocationMapper.class);
        root.setTableName(TableInfoHelper.getTableInfo(BaseLocation.class).getTableName());
        root.setPrimaryKey(TableInfoHelper.getTableInfo(BaseLocation.class).getKeyColumn());
        return root;
    }

    @Override
    public FunctionEnum getSupportType() {
        return FunctionEnum.LOCATION;
    }

    @Override
    public DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> queryPageList(PageQuery pageQuery) {
        return super.queryTablePageList(new LocationDataView().fetchDataView(), pageQuery);
    }

    @Override
    public Tuple2<Integer, String> checkSave(BasicBO bo, Map<String, List<Map<String, Object>>> oldTableEntityMap,
                                             Map<String, List<Map<String, Object>>> newTableEntityMap) {
        Map<String, Object> data = bo.getData();
        Object codeVal = data.get("locCode");
        if (codeVal == null || StrUtil.isBlank(String.valueOf(codeVal))) {
            return Tuple2.of(0, "仓位编号不能为空");
        }
        String locCode = String.valueOf(codeVal).trim();
        Long excludeId = parseId(data.get("id"));
        if (exists(locationMapper, "loc_code", locCode, excludeId)) {
            return Tuple2.of(0, "仓位编号已存在：" + locCode);
        }
        // 立库(自营) 仓库的仓位须为四段式（如 3-2-8-1）或 Y 开头（如 Y-066）
        Long warehouseId = parseId(data.get("warehouseId"));
        if (warehouseId != null) {
            BaseWarehouse warehouse = warehouseMapper.selectById(warehouseId);
            if (warehouse != null && WH_TYPE_LIKU.equals(warehouse.getWhType())
                && !LIKU_LOC_PATTERN.matcher(locCode).matches() && !locCode.startsWith("Y-")) {
                return Tuple2.of(0, "立库仓位编号须为四段式（如 3-2-8-1）或以 Y 开头（如 Y-066）");
            }
        }
        return Tuple2.of(1, null);
    }

    @Override
    public Tuple2<Boolean, String> checkDelete(Long id) {
        BaseLocation location = locationMapper.selectById(id);
        if (location == null) {
            return Tuple2.of(true, null);
        }
        QueryWrapper<InvBatchStock> qw = new QueryWrapper<>();
        qw.eq("warehouse_id", location.getWarehouseId());
        qw.eq("loc_code", location.getLocCode());
        if (batchStockMapper.selectCount(qw) > 0) {
            return Tuple2.of(false, "仓位[" + location.getLocCode() + "]存在库存记录，不能删除");
        }
        return Tuple2.of(true, null);
    }

    @Override
    public Tuple2<Boolean, String> checkBatchDelete(Long[] ids) {
        for (Long id : ids) {
            Tuple2<Boolean, String> result = checkDelete(id);
            if (!result.isSuccess()) {
                return result;
            }
        }
        return Tuple2.of(true, null);
    }
}
