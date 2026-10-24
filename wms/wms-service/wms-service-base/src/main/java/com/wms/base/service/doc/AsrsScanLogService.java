package com.wms.base.service.doc;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wms.base.domain.doc.AsrsScanLog;
import com.wms.base.mapper.doc.AsrsScanLogMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 扫码终端作业日志服务（M6）：终端每次扫码/下发动作实时留痕（验收③ 作业日志留痕）
 */
@Service
public class AsrsScanLogService {

    @Resource
    private AsrsScanLogMapper asrsScanLogMapper;

    /** 写入终端流水（失败也不阻断作业主流程） */
    public void log(String action, String locCode, String palletNo, boolean success, String msg) {
        try {
            AsrsScanLog scanLog = new AsrsScanLog();
            scanLog.setScanTime(new Date());
            scanLog.setAction(action);
            scanLog.setLocCode(locCode);
            scanLog.setPalletNo(palletNo);
            scanLog.setResult(success ? "成功" : "失败");
            scanLog.setMsg(msg);
            scanLog.setCreateTime(new Date());
            asrsScanLogMapper.insert(scanLog);
        } catch (Exception ignore) {
            // 流水留痕失败不影响终端作业主流程
        }
    }

    /** 近期流水（终端/运维查询） */
    public List<AsrsScanLog> recent(int limit) {
        QueryWrapper<AsrsScanLog> qw = new QueryWrapper<>();
        qw.orderByDesc("id").last("limit " + Math.max(1, limit));
        return asrsScanLogMapper.selectList(qw);
    }

    /** 按托盘追溯终端流水 */
    public List<AsrsScanLog> byPallet(String palletNo) {
        QueryWrapper<AsrsScanLog> qw = new QueryWrapper<>();
        if (StrUtil.isNotEmpty(palletNo)) {
            qw.eq("pallet_no", palletNo);
        }
        qw.orderByDesc("id").last("limit 100");
        return asrsScanLogMapper.selectList(qw);
    }
}
