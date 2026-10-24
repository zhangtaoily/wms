package com.wms.base.service.doc;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.base.domain.doc.DocOutNotice;
import com.wms.base.domain.doc.DocOutNoticeDetail;
import com.wms.base.mapper.doc.DocOutNoticeDetailMapper;
import com.wms.base.mapper.doc.DocOutNoticeMapper;
import com.wms.base.view.doc.DocOutNoticeDataView;
import com.wms.common.api.BasicBO;
import com.wms.common.core.domain.PageQuery;
import com.wms.common.enums.FunctionEnum;
import com.wms.common.exception.ServiceException;
import com.wms.common.utils.Tuple2;
import com.wms.domain.SysFieldConfig;
import com.wms.page.DynamicsTableDataInfo;
import com.wms.service.common.service.TableNode;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 出库通知单服务（FR-05）：
 * 1. 仅接受 发货通知单/领料申请单/采购退料单 三源导入或手工新建，其余来源拒绝
 * 2. 按 ERP 单号判重，防止重复导入
 */
@Service
public class DocOutNoticeService extends AbstractM3DocService<DocOutNotice, DocOutNoticeMapper> {

    /** 允许的通知来源 */
    private static final List<String> NOTICE_TYPES = Arrays.asList("发货通知单", "领料申请单", "采购退料单", "手工新建");

    @Resource
    private DocOutNoticeMapper docOutNoticeMapper;
    @Resource
    private DocOutNoticeDetailMapper docOutNoticeDetailMapper;

    @Override
    public DocOutNoticeMapper supplierMapper() {
        return docOutNoticeMapper;
    }

    @Override
    public TableNode doFetchTableNode() {
        TableInfo info = TableInfoHelper.getTableInfo(DocOutNotice.class);
        TableInfo detailInfo = TableInfoHelper.getTableInfo(DocOutNoticeDetail.class);

        TableNode root = new TableNode();
        root.setTableClass(DocOutNotice.class);
        root.setMapper(DocOutNoticeMapper.class);
        root.setTableName(info.getTableName());
        root.setPrimaryKey(info.getKeyColumn());

        TableNode detail = new TableNode();
        detail.setTableClass(DocOutNoticeDetail.class);
        detail.setMapper(DocOutNoticeDetailMapper.class);
        detail.setTableName(detailInfo.getTableName());
        detail.setPrimaryKey(detailInfo.getKeyColumn());
        detail.setForeignKey("notice_id");
        detail.setOrderBy(" order by seq");
        detail.setParentTable(info.getTableName());

        root.addChild(detail);
        return root;
    }

    @Override
    public FunctionEnum getSupportType() {
        return FunctionEnum.WMS_OUT_NOTICE;
    }

    @Override
    protected String codeColumn() {
        return "notice_no";
    }

    @Override
    public DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> queryPageList(PageQuery pageQuery) {
        return super.queryTablePageList(new DocOutNoticeDataView().fetchDataView(), pageQuery);
    }

    /** 保存前校验：来源类型白名单 + ERP 单号判重（FR-05） */
    @Override
    public Tuple2<Integer, Object> save(BasicBO bo) {
        checkNotice(bo.getData());
        return super.save(bo);
    }

    @Override
    public Tuple2<Boolean, Object> batchSave(BasicBO bo) {
        if (bo.getDatas() != null) {
            bo.getDatas().forEach(this::checkNotice);
        }
        return super.batchSave(bo);
    }

    private void checkNotice(Map<String, Object> data) {
        String noticeType = data.get("noticeType") == null ? null : data.get("noticeType").toString();
        if (!NOTICE_TYPES.contains(noticeType)) {
            throw new ServiceException("出库通知仅接受发货通知单/领料申请单/采购退料单导入或手工新建");
        }
        Object erpNoVal = data.get("erpNo");
        if (erpNoVal != null && StrUtil.isNotEmpty(erpNoVal.toString())) {
            Long excludeId = parseId(data.get("id"));
            if (exists(docOutNoticeMapper, "erp_no", erpNoVal.toString(), excludeId)) {
                throw new ServiceException("ERP单号" + erpNoVal + "已导入，不能重复导入");
            }
        }
    }
}
