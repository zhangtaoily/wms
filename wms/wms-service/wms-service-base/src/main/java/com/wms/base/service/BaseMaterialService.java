package com.wms.base.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.base.domain.BaseMaterial;
import com.wms.base.mapper.BaseMaterialMapper;
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
 * 物料管理引擎服务：物料编码唯一校验；同时作为物料导入导出的宿主服务
 */
@Service
public class BaseMaterialService extends BaseWmsFormService<BaseMaterial, BaseMaterialMapper> {

    @Resource
    private BaseMaterialMapper materialMapper;

    @Override
    public BaseMaterialMapper supplierMapper() {
        return materialMapper;
    }

    @Override
    public TableNode doFetchTableNode() {
        TableNode root = new TableNode();
        root.setTableClass(BaseMaterial.class);
        root.setMapper(BaseMaterialMapper.class);
        root.setTableName(TableInfoHelper.getTableInfo(BaseMaterial.class).getTableName());
        root.setPrimaryKey(TableInfoHelper.getTableInfo(BaseMaterial.class).getKeyColumn());
        return root;
    }

    @Override
    public FunctionEnum getSupportType() {
        return FunctionEnum.MATERIAL;
    }

    @Override
    public DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> queryPageList(PageQuery pageQuery) {
        return super.queryTablePageList(BaseMaterial.class, pageQuery);
    }

    @Override
    public Tuple2<Integer, String> checkSave(BasicBO bo, Map<String, List<Map<String, Object>>> oldTableEntityMap,
                                             Map<String, List<Map<String, Object>>> newTableEntityMap) {
        Map<String, Object> data = bo.getData();
        Object codeVal = data.get("materialCode");
        if (codeVal == null || StrUtil.isBlank(String.valueOf(codeVal))) {
            return Tuple2.of(0, "物料编码不能为空");
        }
        if (exists(materialMapper, "material_code", String.valueOf(codeVal).trim(), parseId(data.get("id")))) {
            return Tuple2.of(0, "物料编码已存在：" + codeVal);
        }
        return Tuple2.of(1, null);
    }
}
