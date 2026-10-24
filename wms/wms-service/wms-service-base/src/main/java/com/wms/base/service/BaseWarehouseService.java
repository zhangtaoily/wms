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

/**
 * 仓库信息引擎服务：仓库编码唯一校验；删除约束（仓位/库存引用）
 */
@Service
public class BaseWarehouseService extends BaseWmsFormService<BaseWarehouse, BaseWarehouseMapper> {

    @Resource
    private BaseWarehouseMapper warehouseMapper;
    @Resource
    private BaseLocationMapper locationMapper;
    @Resource
    private InvBatchStockMapper batchStockMapper;

    @Override
    public BaseWarehouseMapper supplierMapper() {
        return warehouseMapper;
    }

    @Override
    public TableNode doFetchTableNode() {
        TableNode root = new TableNode();
        root.setTableClass(BaseWarehouse.class);
        root.setMapper(BaseWarehouseMapper.class);
        root.setTableName(TableInfoHelper.getTableInfo(BaseWarehouse.class).getTableName());
        root.setPrimaryKey(TableInfoHelper.getTableInfo(BaseWarehouse.class).getKeyColumn());
        return root;
    }

    @Override
    public FunctionEnum getSupportType() {
        return FunctionEnum.WAREHOUSE;
    }

    @Override
    public DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> queryPageList(PageQuery pageQuery) {
        return super.queryTablePageList(BaseWarehouse.class, pageQuery);
    }

    @Override
    public Tuple2<Integer, String> checkSave(BasicBO bo, Map<String, List<Map<String, Object>>> oldTableEntityMap,
                                             Map<String, List<Map<String, Object>>> newTableEntityMap) {
        Map<String, Object> data = bo.getData();
        Object noVal = data.get("warehouseNo");
        if (noVal == null || StrUtil.isBlank(String.valueOf(noVal))) {
            return Tuple2.of(0, "仓库编码不能为空");
        }
        if (exists(warehouseMapper, "warehouse_no", String.valueOf(noVal).trim(), parseId(data.get("id")))) {
            return Tuple2.of(0, "仓库编码已存在：" + noVal);
        }
        return Tuple2.of(1, null);
    }

    @Override
    public Tuple2<Boolean, String> checkDelete(Long id) {
        if (locationMapper.selectCount(new QueryWrapper<BaseLocation>().eq("warehouse_id", id)) > 0) {
            return Tuple2.of(false, "该仓库下存在仓位，不能删除");
        }
        if (batchStockMapper.selectCount(new QueryWrapper<InvBatchStock>().eq("warehouse_id", id)) > 0) {
            return Tuple2.of(false, "该仓库存在库存记录，不能删除");
        }
        return Tuple2.of(true, null);
    }

    @Override
    public Tuple2<Boolean, String> checkBatchDelete(Long[] ids) {
        for (Long id : ids) {
            Tuple2<Boolean, String> result = checkDelete(id);
            if (!result.isSuccess()) {
                BaseWarehouse warehouse = warehouseMapper.selectById(id);
                String name = warehouse == null ? String.valueOf(id) : warehouse.getWarehouseName();
                return Tuple2.of(false, "仓库[" + name + "]删除失败：" + result.getSecond());
            }
        }
        return Tuple2.of(true, null);
    }
}
