package com.wms.base.service.doc;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.base.domain.SyncRecord;
import com.wms.base.mapper.SyncRecordMapper;
import com.wms.base.view.doc.SyncRecordDataView;
import com.wms.common.core.domain.PageQuery;
import com.wms.common.core.domain.R;
import com.wms.common.enums.FunctionEnum;
import com.wms.common.exception.ServiceException;
import com.wms.common.utils.ApplicationContextHolder;
import com.wms.common.utils.Tuple2;
import com.wms.domain.SysFieldConfig;
import com.wms.page.DynamicsTableDataInfo;
import com.wms.service.common.service.TableNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 外部系统同步记录服务（M7 同步工程化，PRD 闭环⑤）
 * 1. 幂等 upsert：以 (biz_type, doc_no, direction=回写) 为幂等键，验收① 同一单据重复触发不产生重复 ERP 单号
 * 2. 状态语义：成功=已同步；未同步=待自动重试；失败=重试超限转人工（Q1）
 * 3. 定时补偿：指数退避 dueList（base_interval × 2^retry_count），次数/间隔由系统设置 SYNC_RETRY 配置
 * 4. 人工重推：引擎 PATCH /common/table/reSend/SYNC_RECORD/{id} → reTry → 按 biz_type 路由对应 ERP 桩
 * 同时作为同步监控列表的引擎表单服务（列表只读，记录由同步轴写入）
 */
@Slf4j
@Service
public class SyncRecordService extends AbstractM3DocService<SyncRecord, SyncRecordMapper> {

    /** 单据类型（biz_type），与各 ERP 桩一一对应 */
    public static final String BIZ_INBOUND = "入库单";
    public static final String BIZ_OUTBOUND = "出库单";
    public static final String BIZ_STOCK_MOVE = "移库单";
    public static final String BIZ_STOCK_COUNT = "盘存单";

    public static final String STATUS_SUCCESS = "成功";
    public static final String STATUS_PENDING = "未同步";
    public static final String STATUS_MANUAL = "失败";

    /** 系统设置：同步重试配置（Q1 可配置，JSON：{"maxRetry":5,"baseIntervalSec":60}） */
    public static final String SETTING_CODE = "SYNC_RETRY";
    private static final int DEFAULT_MAX_RETRY = 5;
    private static final int DEFAULT_BASE_INTERVAL_SEC = 60;

    @Resource
    private SyncRecordMapper syncRecordMapper;

    @Override
    public SyncRecordMapper supplierMapper() {
        return syncRecordMapper;
    }

    @Override
    public TableNode doFetchTableNode() {
        TableInfo info = TableInfoHelper.getTableInfo(SyncRecord.class);
        TableNode root = new TableNode();
        root.setTableClass(SyncRecord.class);
        root.setMapper(SyncRecordMapper.class);
        root.setTableName(info.getTableName());
        root.setPrimaryKey(info.getKeyColumn());
        return root;
    }

    @Override
    public FunctionEnum getSupportType() {
        return FunctionEnum.SYNC_RECORD;
    }

    @Override
    protected String codeColumn() {
        return null;
    }

    @Override
    public DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> queryPageList(PageQuery pageQuery) {
        return super.queryTablePageList(new SyncRecordDataView().fetchDataView(), pageQuery);
    }

    /**
     * 同步结果落账（幂等 upsert，同步轴各桩调用）
     *
     * @param bizType 单据类型（BIZ_*）
     * @param docId   单据ID
     * @param docNo   单据号
     * @param extNo   ERP 单号（成功时非空）
     * @param success 本次同步是否成功
     * @param msg     成功确认号/失败原因
     */
    public void record(String bizType, Long docId, String docNo, String extNo, boolean success, String msg) {
        try {
            SyncRecord exists = selectByKey(bizType, docNo);
            Date now = new Date();
            if (exists == null) {
                SyncRecord rec = new SyncRecord();
                rec.setTargetSystem("ERP");
                rec.setDirection("回写");
                rec.setBizType(bizType);
                rec.setDocId(docId);
                rec.setDocNo(docNo);
                rec.setStatus(success ? STATUS_SUCCESS : STATUS_PENDING);
                rec.setRetryCount(0);
                fillResult(rec, extNo, success, msg, now);
                rec.setCreator("系统");
                rec.setCreateTime(now);
                rec.setUpdateTime(now);
                syncRecordMapper.insert(rec);
                return;
            }
            SyncRecord upd = new SyncRecord();
            upd.setId(exists.getId());
            if (success) {
                upd.setStatus(STATUS_SUCCESS);
            } else if (STATUS_SUCCESS.equals(exists.getStatus())) {
                // 已成功记录不回退（幂等保护）
                return;
            } else {
                int retryCount = nvl(exists.getRetryCount());
                upd.setStatus(retryCount + 1 >= maxRetry() ? STATUS_MANUAL : STATUS_PENDING);
                upd.setRetryCount(retryCount + 1);
            }
            fillResult(upd, extNo, success, msg, now);
            upd.setUpdateTime(now);
            syncRecordMapper.updateById(upd);
        } catch (Exception ex) {
            // 同步记录落账失败不影响单据同步主流程
            log.error("同步记录写入失败：bizType={} docNo={}", bizType, docNo, ex);
        }
    }

    /** 定时补偿到期清单：未同步 且 指数退避时间已到（base_interval × 2^retry_count，Q1） */
    public List<SyncRecord> dueList(int limit) {
        int baseIntervalSec = baseIntervalSec();
        QueryWrapper<SyncRecord> qw = new QueryWrapper<>();
        qw.eq("status", STATUS_PENDING).orderByAsc("id").last("limit " + Math.max(1, limit));
        List<SyncRecord> all = syncRecordMapper.selectList(qw);
        long now = System.currentTimeMillis();
        List<SyncRecord> due = new java.util.ArrayList<>();
        for (SyncRecord rec : all) {
            int backoffSec = baseIntervalSec * (1 << Math.min(nvl(rec.getRetryCount()), 10));
            Date last = rec.getLastTime() == null ? rec.getCreateTime() : rec.getLastTime();
            long dueAt = (last == null ? 0 : last.getTime()) + backoffSec * 1000L;
            if (now >= dueAt) {
                due.add(rec);
            }
            if (due.size() >= limit) {
                break;
            }
        }
        return due;
    }

    /** 人工重推（验收②超限转人工后的入口）：重置重试状态并按 biz_type 路由对应 ERP 桩 */
    public Tuple2<Boolean, String> manualPush(Long recordId) {
        SyncRecord rec = syncRecordMapper.selectById(recordId);
        if (rec == null) {
            return Tuple2.of(false, "同步记录不存在");
        }
        if (STATUS_SUCCESS.equals(rec.getStatus())) {
            return Tuple2.of(true, rec.getExtNo());
        }
        if (rec.getDocId() == null) {
            return Tuple2.of(false, "同步记录缺少单据ID，无法重推");
        }
        Tuple2<Boolean, String> result;
        switch (rec.getBizType() == null ? "" : rec.getBizType()) {
            case BIZ_INBOUND:
                result = ApplicationContextHolder.getBean(ErpInboundSyncStub.class).sync(rec.getDocId());
                break;
            case BIZ_OUTBOUND:
                result = ApplicationContextHolder.getBean(ErpOutboundSyncStub.class).sync(rec.getDocId());
                break;
            case BIZ_STOCK_MOVE:
                result = ApplicationContextHolder.getBean(ErpStockMoveSyncStub.class).sync(rec.getDocId());
                break;
            case BIZ_STOCK_COUNT:
                result = ApplicationContextHolder.getBean(ErpStockCountSyncStub.class).sync(rec.getDocId());
                break;
            default:
                return Tuple2.of(false, "未知单据类型：" + rec.getBizType());
        }
        if (result.getFrist()) {
            SyncRecord upd = new SyncRecord();
            upd.setId(rec.getId());
            upd.setStatus(STATUS_SUCCESS);
            upd.setExtNo(result.getSecond());
            upd.setLastMsg("人工重推成功：" + result.getSecond());
            upd.setLastTime(new Date());
            upd.setUpdateTime(new Date());
            syncRecordMapper.updateById(upd);
        }
        return result;
    }

    /** 重发同步（引擎 PATCH /table/reSend/{function}/{id} 入口）：id 即同步记录ID */
    @Override
    public R reTry(Long logId) {
        Tuple2<Boolean, String> result = manualPush(logId);
        return result.getFrist() ? R.ok(result.getSecond()) : R.fail(result.getSecond());
    }

    @Override
    public void beforePass(Long id, TableNode root, Map<String, List<Map<String, Object>>> dbOldData) {
        throw new ServiceException("同步记录由系统同步轴写入，不支持人工生效");
    }

    @Override
    public Tuple2<Integer, Object> save(com.wms.common.api.BasicBO bo) {
        return Tuple2.of(0, "同步记录由系统同步轴写入，不支持手工维护");
    }

    @Override
    public Tuple2<Boolean, Object> batchSave(com.wms.common.api.BasicBO bo) {
        return Tuple2.of(false, "同步记录由系统同步轴写入，不支持手工维护");
    }

    @Override
    public Tuple2<Boolean, String> checkDelete(Long id) {
        return Tuple2.of(false, "同步记录由系统同步轴写入，不支持删除");
    }

    @Override
    public Tuple2<Boolean, String> checkBatchDelete(Long[] ids) {
        return Tuple2.of(false, "同步记录由系统同步轴写入，不支持删除");
    }

    private SyncRecord selectByKey(String bizType, String docNo) {
        QueryWrapper<SyncRecord> qw = new QueryWrapper<>();
        qw.eq("biz_type", bizType).eq("doc_no", docNo).eq("direction", "回写");
        return syncRecordMapper.selectOne(qw);
    }

    private void fillResult(SyncRecord rec, String extNo, boolean success, String msg, Date now) {
        if (success) {
            rec.setExtNo(StrUtil.isEmpty(extNo) ? rec.getExtNo() : extNo);
            rec.setLastMsg(StrUtil.isEmpty(msg) ? "同步成功" : msg);
        } else {
            rec.setLastMsg(msg);
        }
        rec.setLastTime(now);
    }

    /** Q1 配置：自动重试最大次数（系统设置 SYNC_RETRY，缺省 5 次） */
    @SuppressWarnings("unchecked")
    public int maxRetry() {
        Object v = retrySetting().get("maxRetry");
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        try {
            return v == null ? DEFAULT_MAX_RETRY : new BigDecimal(v.toString()).intValue();
        } catch (NumberFormatException e) {
            return DEFAULT_MAX_RETRY;
        }
    }

    /** Q1 配置：退避基准间隔秒（缺省 60s） */
    @SuppressWarnings("unchecked")
    public int baseIntervalSec() {
        Object v = retrySetting().get("baseIntervalSec");
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        try {
            return v == null ? DEFAULT_BASE_INTERVAL_SEC : new BigDecimal(v.toString()).intValue();
        } catch (NumberFormatException e) {
            return DEFAULT_BASE_INTERVAL_SEC;
        }
    }

    /** 读取系统设置 SYNC_RETRY（JSON 文本），读取失败回退默认值 */
    private Map<String, Object> retrySetting() {
        try {
            Map<String, Object> row = syncRecordMapper.query(
                "SELECT content FROM sys_setting WHERE setting_code = '" + SETTING_CODE + "'");
            String content = row == null || row.get("content") == null ? null : row.get("content").toString();
            if (StrUtil.isEmpty(content)) {
                return new TreeMap<>();
            }
            return new TreeMap<>(com.wms.common.utils.JsonUtils.parseMap(content));
        } catch (Exception e) {
            return new TreeMap<>();
        }
    }

    private static int nvl(Integer v) {
        return v == null ? 0 : v;
    }
}
