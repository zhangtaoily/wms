package com.wms.base.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.base.domain.BaseBizBatch;
import com.wms.base.domain.InvBatch;
import com.wms.base.mapper.BaseBizBatchMapper;
import com.wms.base.mapper.InvBatchMapper;
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
 * 业务批次引擎服务：业务批号唯一校验；删除约束（库存批次引用）
 */
@Service
public class BaseBizBatchService extends BaseWmsFormService<BaseBizBatch, BaseBizBatchMapper> {

    @Resource
    private BaseBizBatchMapper bizBatchMapper;
    @Resource
    private InvBatchMapper invBatchMapper;

    @Override
    public BaseBizBatchMapper supplierMapper() {
        return bizBatchMapper;
    }

    @Override
    public TableNode doFetchTableNode() {
        TableNode root = new TableNode();
        root.setTableClass(BaseBizBatch.class);
        root.setMapper(BaseBizBatchMapper.class);
        root.setTableName(TableInfoHelper.getTableInfo(BaseBizBatch.class).getTableName());
        root.setPrimaryKey(TableInfoHelper.getTableInfo(BaseBizBatch.class).getKeyColumn());
        return root;
    }

    @Override
    public FunctionEnum getSupportType() {
        return FunctionEnum.BIZ_BATCH;
    }

    @Override
    public DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> queryPageList(PageQuery pageQuery) {
        return super.queryTablePageList(BaseBizBatch.class, pageQuery);
    }

    @Override
    public Tuple2<Integer, String> checkSave(BasicBO bo, Map<String, List<Map<String, Object>>> oldTableEntityMap,
                                             Map<String, List<Map<String, Object>>> newTableEntityMap) {
        Map<String, Object> data = bo.getData();
        Object noVal = data.get("bizBatchNo");
        if (noVal == null || StrUtil.isBlank(String.valueOf(noVal))) {
            return Tuple2.of(0, "批次编号不能为空");
        }
        if (exists(bizBatchMapper, "biz_batch_no", String.valueOf(noVal).trim(), parseId(data.get("id")))) {
            return Tuple2.of(0, "批次编号已存在：" + noVal);
        }
        return Tuple2.of(1, null);
    }

    @Override
    public Tuple2<Boolean, String> checkDelete(Long id) {
        BaseBizBatch batch = bizBatchMapper.selectById(id);
        if (batch == null) {
            return Tuple2.of(true, null);
        }
        if (invBatchMapper.selectCount(new QueryWrapper<InvBatch>().eq("biz_batch_no", batch.getBizBatchNo())) > 0) {
            return Tuple2.of(false, "批次[" + batch.getBizBatchNo() + "]已生成库存批次，不能删除");
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
