package com.wms.base.service.doc;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.wms.base.domain.InvBatch;
import com.wms.base.domain.InvBatchStock;
import com.wms.base.domain.InvBox;
import com.wms.base.domain.InvPallet;
import com.wms.base.mapper.InvBatchMapper;
import com.wms.base.mapper.InvBatchStockMapper;
import com.wms.base.mapper.InvBoxMapper;
import com.wms.base.mapper.InvPalletMapper;
import com.wms.common.exception.ServiceException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 库存台账写入服务（M3 入库三层台账链）
 * 箱码（inv_box）→ 托盘（inv_pallet）→ 库存批次（inv_batch / inv_batch_stock）
 * 全链幂等：重复生效不重复累计
 */
@Service
public class InventoryLedgerService {

    @Resource
    private InvBoxMapper invBoxMapper;
    @Resource
    private InvPalletMapper invPalletMapper;
    @Resource
    private InvBatchMapper invBatchMapper;
    @Resource
    private InvBatchStockMapper invBatchStockMapper;

    /** 箱码台账：码单收录生成待入箱（同箱码幂等跳过） */
    public void collectBox(String boxBarcode, String batchNo, Integer tubes,
                           BigDecimal stdWeight, BigDecimal grossWeight, BigDecimal netWeight) {
        if (StrUtil.isEmpty(boxBarcode) || existsBox(boxBarcode)) {
            return;
        }
        InvBox box = new InvBox();
        box.setBoxCode(boxBarcode);
        box.setBatchNo(batchNo);
        box.setStatus("待入");
        box.setTubes(tubes);
        box.setStdWeight(stdWeight);
        box.setGrossWeight(grossWeight);
        box.setNetWeight(netWeight);
        box.setCreateTime(new Date());
        invBoxMapper.insert(box);
    }

    /** 托盘台账：组托生成/累加托盘，并将箱码归属到托盘（同托箱码归属一致） */
    public void palletize(String palletNo, String batchNo, Integer boxCount, BigDecimal totalWeight, List<String> boxCodes) {
        if (StrUtil.isEmpty(palletNo)) {
            return;
        }
        InvPallet pallet = selectPallet(palletNo);
        if (pallet == null) {
            pallet = new InvPallet();
            pallet.setPalletNo(palletNo);
            pallet.setIsEmpty(0);
            pallet.setStatus("货");
            pallet.setBatchNo(batchNo);
            pallet.setBoxCount(boxCount == null ? 0 : boxCount);
            pallet.setTotalBoxes(boxCount == null ? 0 : boxCount);
            pallet.setTotalWeight(totalWeight == null ? BigDecimal.ZERO : totalWeight);
            pallet.setCreateTime(new Date());
            invPalletMapper.insert(pallet);
        } else {
            InvPallet upd = new InvPallet();
            upd.setId(pallet.getId());
            upd.setIsEmpty(0);
            upd.setStatus("货");
            upd.setBoxCount(nvl(pallet.getBoxCount()) + (boxCount == null ? 0 : boxCount));
            upd.setTotalBoxes(nvl(pallet.getTotalBoxes()) + (boxCount == null ? 0 : boxCount));
            upd.setTotalWeight(nvl(pallet.getTotalWeight()).add(totalWeight == null ? BigDecimal.ZERO : totalWeight));
            upd.setUpdateTime(new Date());
            invPalletMapper.updateById(upd);
        }
        if (boxCodes != null) {
            for (String boxCode : boxCodes) {
                if (StrUtil.isEmpty(boxCode)) {
                    continue;
                }
                UpdateWrapper<InvBox> uw = new UpdateWrapper<>();
                uw.eq("box_code", boxCode).set("pallet_no", palletNo);
                invBoxMapper.update(null, uw);
            }
        }
    }

    /** 批次台账：入库累计批次总量与批次仓位账（inv_batch_no 维度幂等累计） */
    public void inboundStock(String invBatchNo, String vatNo, String comp, String colorCode, String colorName,
                             Long warehouseId, String warehouseName, String locCode,
                             Integer boxes, Integer tubes, BigDecimal stdWeight, BigDecimal grossWeight, BigDecimal netWeight) {
        if (StrUtil.isEmpty(invBatchNo)) {
            return;
        }
        // 批次总量台账（inv_batch，inv_batch_no 唯一）
        QueryWrapper<InvBatch> batchQw = new QueryWrapper<>();
        batchQw.eq("inv_batch_no", invBatchNo);
        InvBatch batch = invBatchMapper.selectOne(batchQw);
        if (batch == null) {
            batch = new InvBatch();
            batch.setInvBatchNo(invBatchNo);
            batch.setBizBatchNo(invBatchNo);
            batch.setVatNo(vatNo);
            batch.setComp(comp);
            batch.setTotalBoxes(boxes == null ? 0 : boxes);
            batch.setTotalPieces(boxes == null ? 0 : boxes);
            batch.setTotalQty(stdWeight == null ? BigDecimal.ZERO : stdWeight);
            batch.setCreateTime(new Date());
            invBatchMapper.insert(batch);
        } else {
            InvBatch upd = new InvBatch();
            upd.setId(batch.getId());
            upd.setTotalBoxes(nvl(batch.getTotalBoxes()) + (boxes == null ? 0 : boxes));
            upd.setTotalPieces(nvl(batch.getTotalPieces()) + (boxes == null ? 0 : boxes));
            upd.setTotalQty(nvl(batch.getTotalQty()).add(stdWeight == null ? BigDecimal.ZERO : stdWeight));
            upd.setUpdateTime(new Date());
            invBatchMapper.updateById(upd);
        }

        // 批次仓位账（inv_batch_stock，批次×仓库×仓位 维度）
        QueryWrapper<InvBatchStock> stockQw = new QueryWrapper<>();
        stockQw.eq("inv_batch_no", invBatchNo).eq("warehouse_id", warehouseId).eq("loc_code", locCode);
        InvBatchStock stock = invBatchStockMapper.selectOne(stockQw);
        if (stock == null) {
            stock = new InvBatchStock();
            stock.setInvBatchId(batch.getId());
            stock.setInvBatchNo(invBatchNo);
            stock.setVatNo(vatNo);
            stock.setWarehouseId(warehouseId);
            stock.setWarehouseName(warehouseName);
            stock.setLocCode(locCode);
            stock.setPieces(boxes == null ? 0 : boxes);
            stock.setBoxes(boxes == null ? 0 : boxes);
            stock.setTubes(tubes == null ? 0 : tubes);
            stock.setStdWeight(stdWeight == null ? BigDecimal.ZERO : stdWeight);
            stock.setGrossWeight(grossWeight == null ? BigDecimal.ZERO : grossWeight);
            stock.setNetWeight(netWeight == null ? BigDecimal.ZERO : netWeight);
            stock.setComp(comp);
            stock.setColorCode(colorCode);
            stock.setColorName(colorName);
            stock.setAllowQty(stdWeight == null ? BigDecimal.ZERO : stdWeight);
            stock.setSyncStatus("2");
            stock.setCreateTime(new Date());
            invBatchStockMapper.insert(stock);
        } else {
            InvBatchStock upd = new InvBatchStock();
            upd.setId(stock.getId());
            upd.setPieces(nvl(stock.getPieces()) + (boxes == null ? 0 : boxes));
            upd.setBoxes(nvl(stock.getBoxes()) + (boxes == null ? 0 : boxes));
            upd.setTubes(nvl(stock.getTubes()) + (tubes == null ? 0 : tubes));
            upd.setStdWeight(nvl(stock.getStdWeight()).add(stdWeight == null ? BigDecimal.ZERO : stdWeight));
            upd.setGrossWeight(nvl(stock.getGrossWeight()).add(grossWeight == null ? BigDecimal.ZERO : grossWeight));
            upd.setNetWeight(nvl(stock.getNetWeight()).add(netWeight == null ? BigDecimal.ZERO : netWeight));
            upd.setUpdateTime(new Date());
            invBatchStockMapper.updateById(upd);
        }
    }

    /** 箱码在库：入库生效后更新箱状态/库位/入库时间 */
    public void markBoxInStock(String boxCode, String locCode, String warehouseName) {
        if (StrUtil.isEmpty(boxCode)) {
            return;
        }
        UpdateWrapper<InvBox> uw = new UpdateWrapper<>();
        uw.eq("box_code", boxCode)
            .set("status", "在库")
            .set("in_time", new Date())
            .set("loc_code", locCode);
        if (StrUtil.isNotEmpty(warehouseName)) {
            uw.set("warehouse_name", warehouseName);
        }
        invBoxMapper.update(null, uw);
    }

    /**
     * 批次扣账（M4 出库生效）：按 批次×仓库 逐仓位行条件扣减，任一不足即抛异常回滚（防超扣，并发安全）
     */
    public void deductStock(String invBatchNo, Long warehouseId, Integer boxes, Integer tubes, Integer pieces,
                            BigDecimal stdWeight, BigDecimal grossWeight, BigDecimal netWeight) {
        if (StrUtil.isEmpty(invBatchNo)) {
            return;
        }
        QueryWrapper<InvBatchStock> qw = new QueryWrapper<>();
        qw.eq("inv_batch_no", invBatchNo).eq("warehouse_id", warehouseId).orderByAsc("id");
        List<InvBatchStock> rows = invBatchStockMapper.selectList(qw);
        int remainBoxes = nvl(boxes);
        int remainTubes = nvl(tubes);
        int remainPieces = nvl(pieces);
        BigDecimal remainStd = nvl(stdWeight);
        BigDecimal remainGross = nvl(grossWeight);
        BigDecimal remainNet = nvl(netWeight);
        for (InvBatchStock row : rows) {
            if (remainBoxes <= 0 && remainTubes <= 0 && remainPieces <= 0
                && remainStd.compareTo(BigDecimal.ZERO) <= 0
                && remainGross.compareTo(BigDecimal.ZERO) <= 0
                && remainNet.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            int takeBoxes = Math.min(remainBoxes, nvl(row.getBoxes()));
            int takeTubes = Math.min(remainTubes, nvl(row.getTubes()));
            int takePieces = Math.min(remainPieces, nvl(row.getPieces()));
            BigDecimal takeStd = minPositive(remainStd, row.getStdWeight());
            BigDecimal takeGross = minPositive(remainGross, row.getGrossWeight());
            BigDecimal takeNet = minPositive(remainNet, row.getNetWeight());
            if (takeBoxes <= 0 && takeTubes <= 0 && takePieces <= 0
                && takeStd.compareTo(BigDecimal.ZERO) <= 0
                && takeGross.compareTo(BigDecimal.ZERO) <= 0
                && takeNet.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            // 条件 UPDATE 原子扣减：库存不足或被并发抢占时影响行数为 0，转下一仓位行
            UpdateWrapper<InvBatchStock> uw = new UpdateWrapper<>();
            uw.eq("id", row.getId())
                .ge("boxes", takeBoxes).ge("tubes", takeTubes).ge("pieces", takePieces)
                .ge("std_weight", takeStd).ge("gross_weight", takeGross).ge("net_weight", takeNet)
                .setSql("boxes = boxes - " + takeBoxes)
                .setSql("tubes = tubes - " + takeTubes)
                .setSql("pieces = pieces - " + takePieces)
                .setSql("std_weight = std_weight - " + takeStd)
                .setSql("gross_weight = gross_weight - " + takeGross)
                .setSql("net_weight = net_weight - " + takeNet)
                .set("update_time", new Date());
            if (invBatchStockMapper.update(null, uw) == 0) {
                continue;
            }
            remainBoxes -= takeBoxes;
            remainTubes -= takeTubes;
            remainPieces -= takePieces;
            remainStd = remainStd.subtract(takeStd);
            remainGross = remainGross.subtract(takeGross);
            remainNet = remainNet.subtract(takeNet);
        }
        if (remainBoxes > 0 || remainTubes > 0 || remainPieces > 0
            || remainStd.compareTo(BigDecimal.ZERO) > 0
            || remainGross.compareTo(BigDecimal.ZERO) > 0
            || remainNet.compareTo(BigDecimal.ZERO) > 0) {
            throw new ServiceException("批次" + invBatchNo + "在指定仓库库存不足，出库扣账失败");
        }
        // 批次总账同步扣减（条件防超扣）
        UpdateWrapper<InvBatch> bw = new UpdateWrapper<>();
        bw.eq("inv_batch_no", invBatchNo)
            .ge("total_boxes", nvl(boxes)).ge("total_pieces", nvl(pieces)).ge("total_qty", nvl(stdWeight))
            .setSql("total_boxes = total_boxes - " + nvl(boxes))
            .setSql("total_pieces = total_pieces - " + nvl(pieces))
            .setSql("total_qty = total_qty - " + nvl(stdWeight))
            .set("update_time", new Date());
        if (invBatchMapper.update(null, bw) == 0) {
            throw new ServiceException("批次" + invBatchNo + "总量台账不足，出库扣账失败");
        }
    }

    /**
     * 批次回加（M4 出库红冲）：同入库链路累加；inv_batch_stock 无该批次×仓库行时新建（仓位空）
     */
    public void restoreStock(String invBatchNo, Long warehouseId, String warehouseName,
                             String vatNo, String comp, String colorCode, String colorName,
                             Integer boxes, Integer tubes, BigDecimal stdWeight,
                             BigDecimal grossWeight, BigDecimal netWeight) {
        if (StrUtil.isEmpty(invBatchNo)) {
            return;
        }
        QueryWrapper<InvBatch> batchQw = new QueryWrapper<>();
        batchQw.eq("inv_batch_no", invBatchNo);
        InvBatch batch = invBatchMapper.selectOne(batchQw);
        if (batch == null) {
            batch = new InvBatch();
            batch.setInvBatchNo(invBatchNo);
            batch.setBizBatchNo(invBatchNo);
            batch.setVatNo(vatNo);
            batch.setComp(comp);
            batch.setTotalBoxes(boxes == null ? 0 : boxes);
            batch.setTotalPieces(boxes == null ? 0 : boxes);
            batch.setTotalQty(stdWeight == null ? BigDecimal.ZERO : stdWeight);
            batch.setCreateTime(new Date());
            invBatchMapper.insert(batch);
        } else {
            InvBatch upd = new InvBatch();
            upd.setId(batch.getId());
            upd.setTotalBoxes(nvl(batch.getTotalBoxes()) + nvl(boxes));
            upd.setTotalPieces(nvl(batch.getTotalPieces()) + nvl(boxes));
            upd.setTotalQty(nvl(batch.getTotalQty()).add(stdWeight == null ? BigDecimal.ZERO : stdWeight));
            upd.setUpdateTime(new Date());
            invBatchMapper.updateById(upd);
        }
        QueryWrapper<InvBatchStock> stockQw = new QueryWrapper<>();
        stockQw.eq("inv_batch_no", invBatchNo).eq("warehouse_id", warehouseId).orderByAsc("id");
        List<InvBatchStock> rows = invBatchStockMapper.selectList(stockQw);
        if (rows.isEmpty()) {
            InvBatchStock stock = new InvBatchStock();
            stock.setInvBatchId(batch.getId());
            stock.setInvBatchNo(invBatchNo);
            stock.setVatNo(vatNo);
            stock.setWarehouseId(warehouseId);
            stock.setWarehouseName(warehouseName);
            stock.setPieces(nvl(boxes));
            stock.setBoxes(nvl(boxes));
            stock.setTubes(nvl(tubes));
            stock.setStdWeight(stdWeight == null ? BigDecimal.ZERO : stdWeight);
            stock.setGrossWeight(grossWeight == null ? BigDecimal.ZERO : grossWeight);
            stock.setNetWeight(netWeight == null ? BigDecimal.ZERO : netWeight);
            stock.setComp(comp);
            stock.setColorCode(colorCode);
            stock.setColorName(colorName);
            stock.setAllowQty(stdWeight == null ? BigDecimal.ZERO : stdWeight);
            stock.setSyncStatus("2");
            stock.setCreateTime(new Date());
            invBatchStockMapper.insert(stock);
        } else {
            InvBatchStock first = rows.get(0);
            InvBatchStock upd = new InvBatchStock();
            upd.setId(first.getId());
            upd.setPieces(nvl(first.getPieces()) + nvl(boxes));
            upd.setBoxes(nvl(first.getBoxes()) + nvl(boxes));
            upd.setTubes(nvl(first.getTubes()) + nvl(tubes));
            upd.setStdWeight(nvl(first.getStdWeight()).add(stdWeight == null ? BigDecimal.ZERO : stdWeight));
            upd.setGrossWeight(nvl(first.getGrossWeight()).add(grossWeight == null ? BigDecimal.ZERO : grossWeight));
            upd.setNetWeight(nvl(first.getNetWeight()).add(netWeight == null ? BigDecimal.ZERO : netWeight));
            upd.setUpdateTime(new Date());
            invBatchStockMapper.updateById(upd);
        }
    }

    /** 箱码出库：出库生效后更新箱状态/出库时间 */
    public void markBoxOutStock(String boxCode) {
        if (StrUtil.isEmpty(boxCode)) {
            return;
        }
        UpdateWrapper<InvBox> uw = new UpdateWrapper<>();
        uw.eq("box_code", boxCode)
            .set("status", "已出库")
            .set("out_time", new Date());
        invBoxMapper.update(null, uw);
    }

    /** 箱码回库（出库红冲）：恢复在库状态 */
    public void markBoxRestoreStock(String boxCode) {
        if (StrUtil.isEmpty(boxCode)) {
            return;
        }
        UpdateWrapper<InvBox> uw = new UpdateWrapper<>();
        uw.eq("box_code", boxCode)
            .set("status", "在库")
            .set("out_time", null);
        invBoxMapper.update(null, uw);
    }

    /**
     * 仓位转移（M5 移库生效，FR-08）：同一仓库内 源仓位→目标仓位 的数量搬运，
     * inv_batch 批次总量不变（总量不变仅仓位归属变化），仅 inv_batch_stock 行间转移。
     * 源行条件 UPDATE 原子扣减（不足抛异常回滚，防超移），目标行累加（无则新建）。
     */
    public void moveStock(String invBatchNo, Long warehouseId, String warehouseName,
                          String fromLocCode, String toLocCode,
                          Integer boxes, Integer tubes, Integer pieces,
                          BigDecimal stdWeight, BigDecimal grossWeight, BigDecimal netWeight) {
        if (StrUtil.isEmpty(invBatchNo) || warehouseId == null
            || StrUtil.isEmpty(fromLocCode) || StrUtil.isEmpty(toLocCode)
            || fromLocCode.equals(toLocCode)) {
            return;
        }
        QueryWrapper<InvBatchStock> srcQw = new QueryWrapper<>();
        srcQw.eq("inv_batch_no", invBatchNo).eq("warehouse_id", warehouseId).eq("loc_code", fromLocCode);
        InvBatchStock source = invBatchStockMapper.selectOne(srcQw);
        if (source == null) {
            throw new ServiceException("批次" + invBatchNo + "在仓位" + fromLocCode + "无库存台账，移库失败");
        }
        int moveBoxes = nvl(boxes);
        int moveTubes = nvl(tubes);
        int movePieces = nvl(pieces);
        BigDecimal moveStd = nvl(stdWeight);
        BigDecimal moveGross = nvl(grossWeight);
        BigDecimal moveNet = nvl(netWeight);
        // 源行条件扣减：账面不足或被并发抢占时影响行数为 0，整体回滚
        UpdateWrapper<InvBatchStock> srcUw = new UpdateWrapper<>();
        srcUw.eq("id", source.getId())
            .ge("boxes", moveBoxes).ge("tubes", moveTubes).ge("pieces", movePieces)
            .ge("std_weight", moveStd).ge("gross_weight", moveGross).ge("net_weight", moveNet)
            .setSql("boxes = boxes - " + moveBoxes)
            .setSql("tubes = tubes - " + moveTubes)
            .setSql("pieces = pieces - " + movePieces)
            .setSql("std_weight = std_weight - " + moveStd)
            .setSql("gross_weight = gross_weight - " + moveGross)
            .setSql("net_weight = net_weight - " + moveNet)
            .set("update_time", new Date());
        if (invBatchStockMapper.update(null, srcUw) == 0) {
            throw new ServiceException("批次" + invBatchNo + "在仓位" + fromLocCode + "库存不足，移库失败");
        }
        // 目标行累加（无则新建，物料属性沿用源行）
        QueryWrapper<InvBatchStock> dstQw = new QueryWrapper<>();
        dstQw.eq("inv_batch_no", invBatchNo).eq("warehouse_id", warehouseId).eq("loc_code", toLocCode);
        InvBatchStock target = invBatchStockMapper.selectOne(dstQw);
        if (target == null) {
            InvBatchStock stock = new InvBatchStock();
            stock.setInvBatchId(source.getInvBatchId());
            stock.setInvBatchNo(invBatchNo);
            stock.setVatNo(source.getVatNo());
            stock.setWarehouseId(warehouseId);
            stock.setWarehouseName(StrUtil.isNotEmpty(warehouseName) ? warehouseName : source.getWarehouseName());
            stock.setLocCode(toLocCode);
            stock.setPieces(movePieces);
            stock.setBoxes(moveBoxes);
            stock.setTubes(moveTubes);
            stock.setStdWeight(moveStd);
            stock.setGrossWeight(moveGross);
            stock.setNetWeight(moveNet);
            stock.setComp(source.getComp());
            stock.setColorCode(source.getColorCode());
            stock.setColorName(source.getColorName());
            stock.setAllowQty(moveStd);
            stock.setSyncStatus("2");
            stock.setCreateTime(new Date());
            invBatchStockMapper.insert(stock);
        } else {
            InvBatchStock upd = new InvBatchStock();
            upd.setId(target.getId());
            upd.setPieces(nvl(target.getPieces()) + movePieces);
            upd.setBoxes(nvl(target.getBoxes()) + moveBoxes);
            upd.setTubes(nvl(target.getTubes()) + moveTubes);
            upd.setStdWeight(nvl(target.getStdWeight()).add(moveStd));
            upd.setGrossWeight(nvl(target.getGrossWeight()).add(moveGross));
            upd.setNetWeight(nvl(target.getNetWeight()).add(moveNet));
            upd.setUpdateTime(new Date());
            invBatchStockMapper.updateById(upd);
        }
    }

    /** 箱码仓位变更（M5 移库生效）：更新箱码仓位与仓库名称 */
    public void moveBoxLoc(String boxCode, String toLocCode, String warehouseName) {
        if (StrUtil.isEmpty(boxCode) || StrUtil.isEmpty(toLocCode)) {
            return;
        }
        UpdateWrapper<InvBox> uw = new UpdateWrapper<>();
        uw.eq("box_code", boxCode)
            .set("loc_code", toLocCode);
        if (StrUtil.isNotEmpty(warehouseName)) {
            uw.set("warehouse_name", warehouseName);
        }
        invBoxMapper.update(null, uw);
    }

    /** 托盘仓位变更（M5 移库生效，含托盘挪库/空托移库）：更新托盘仓位 */
    public void movePalletLoc(String palletNo, String toLocCode) {
        if (StrUtil.isEmpty(palletNo) || StrUtil.isEmpty(toLocCode)) {
            return;
        }
        UpdateWrapper<InvPallet> uw = new UpdateWrapper<>();
        uw.eq("pallet_no", palletNo)
            .set("loc_code", toLocCode)
            .set("update_time", new Date());
        invPalletMapper.update(null, uw);
    }

    /**
     * 盘盈亏调整（M5 盘存单主管确认后，Q2）：按 盈亏增量（实盘-盘前）校平台账。
     * 正向盘盈累加（无仓位行则新建），负向盘亏条件扣减（账面不足抛异常回滚）；
     * inv_batch_stock 行调整的同时同步 inv_batch 批次总量。
     */
    public void adjustStock(String invBatchNo, String vatNo, String comp, String colorCode, String colorName,
                            Long warehouseId, String warehouseName, String locCode,
                            Integer diffTubes, BigDecimal diffStd, BigDecimal diffGross, BigDecimal diffNet) {
        if (StrUtil.isEmpty(invBatchNo) || warehouseId == null) {
            return;
        }
        int dTubes = nvl(diffTubes);
        BigDecimal dStd = nvl(diffStd);
        BigDecimal dGross = nvl(diffGross);
        BigDecimal dNet = nvl(diffNet);
        if (dTubes == 0 && dStd.compareTo(BigDecimal.ZERO) == 0
            && dGross.compareTo(BigDecimal.ZERO) == 0 && dNet.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        boolean gain = dTubes > 0 || dStd.compareTo(BigDecimal.ZERO) > 0
            || dGross.compareTo(BigDecimal.ZERO) > 0 || dNet.compareTo(BigDecimal.ZERO) > 0;
        int sign = gain ? 1 : -1;
        int aTubes = Math.abs(dTubes);
        BigDecimal aStd = dStd.abs();
        BigDecimal aGross = dGross.abs();
        BigDecimal aNet = dNet.abs();

        // 批次仓位账：正向累加/新建，负向条件扣减（盘亏不得超过账面）
        QueryWrapper<InvBatchStock> stockQw = new QueryWrapper<>();
        stockQw.eq("inv_batch_no", invBatchNo).eq("warehouse_id", warehouseId);
        if (StrUtil.isNotEmpty(locCode)) {
            stockQw.eq("loc_code", locCode);
        }
        stockQw.orderByAsc("id");
        List<InvBatchStock> rows = invBatchStockMapper.selectList(stockQw);
        if (gain) {
            InvBatchStock target = StrUtil.isEmpty(locCode) ? (rows.isEmpty() ? null : rows.get(0))
                : rows.stream().filter(r -> locCode.equals(r.getLocCode())).findFirst().orElse(null);
            if (target == null) {
                QueryWrapper<InvBatch> batchQw = new QueryWrapper<>();
                batchQw.eq("inv_batch_no", invBatchNo);
                InvBatch batch = invBatchMapper.selectOne(batchQw);
                InvBatchStock stock = new InvBatchStock();
                stock.setInvBatchId(batch == null ? null : batch.getId());
                stock.setInvBatchNo(invBatchNo);
                stock.setVatNo(vatNo);
                stock.setWarehouseId(warehouseId);
                stock.setWarehouseName(warehouseName);
                stock.setLocCode(locCode);
                stock.setTubes(aTubes);
                stock.setStdWeight(aStd);
                stock.setGrossWeight(aGross);
                stock.setNetWeight(aNet);
                stock.setComp(comp);
                stock.setColorCode(colorCode);
                stock.setColorName(colorName);
                stock.setAllowQty(aStd);
                stock.setSyncStatus("2");
                stock.setCreateTime(new Date());
                invBatchStockMapper.insert(stock);
            } else {
                InvBatchStock upd = new InvBatchStock();
                upd.setId(target.getId());
                upd.setTubes(nvl(target.getTubes()) + aTubes);
                upd.setStdWeight(nvl(target.getStdWeight()).add(aStd));
                upd.setGrossWeight(nvl(target.getGrossWeight()).add(aGross));
                upd.setNetWeight(nvl(target.getNetWeight()).add(aNet));
                upd.setUpdateTime(new Date());
                invBatchStockMapper.updateById(upd);
            }
        } else {
            // 负向：逐仓位行条件扣减，不足即整体失败
            BigDecimal remainStd = aStd;
            BigDecimal remainGross = aGross;
            BigDecimal remainNet = aNet;
            int remainTubes = aTubes;
            for (InvBatchStock row : rows) {
                if (remainTubes <= 0 && remainStd.compareTo(BigDecimal.ZERO) <= 0
                    && remainGross.compareTo(BigDecimal.ZERO) <= 0 && remainNet.compareTo(BigDecimal.ZERO) <= 0) {
                    break;
                }
                int takeTubes = Math.min(remainTubes, nvl(row.getTubes()));
                BigDecimal takeStd = minPositive(remainStd, row.getStdWeight());
                BigDecimal takeGross = minPositive(remainGross, row.getGrossWeight());
                BigDecimal takeNet = minPositive(remainNet, row.getNetWeight());
                if (takeTubes <= 0 && takeStd.compareTo(BigDecimal.ZERO) <= 0
                    && takeGross.compareTo(BigDecimal.ZERO) <= 0 && takeNet.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                UpdateWrapper<InvBatchStock> uw = new UpdateWrapper<>();
                uw.eq("id", row.getId())
                    .ge("tubes", takeTubes)
                    .ge("std_weight", takeStd).ge("gross_weight", takeGross).ge("net_weight", takeNet)
                    .setSql("tubes = tubes - " + takeTubes)
                    .setSql("std_weight = std_weight - " + takeStd)
                    .setSql("gross_weight = gross_weight - " + takeGross)
                    .setSql("net_weight = net_weight - " + takeNet)
                    .set("update_time", new Date());
                if (invBatchStockMapper.update(null, uw) == 0) {
                    continue;
                }
                remainTubes -= takeTubes;
                remainStd = remainStd.subtract(takeStd);
                remainGross = remainGross.subtract(takeGross);
                remainNet = remainNet.subtract(takeNet);
            }
            if (remainTubes > 0 || remainStd.compareTo(BigDecimal.ZERO) > 0
                || remainGross.compareTo(BigDecimal.ZERO) > 0 || remainNet.compareTo(BigDecimal.ZERO) > 0) {
                throw new ServiceException("批次" + invBatchNo + "账面库存不足，盘亏调整失败");
            }
        }

        // 批次总量台账同步校平（inv_batch 总量：负向条件防超扣）
        if (gain) {
            QueryWrapper<InvBatch> batchQw = new QueryWrapper<>();
            batchQw.eq("inv_batch_no", invBatchNo);
            InvBatch batch = invBatchMapper.selectOne(batchQw);
            if (batch == null) {
                batch = new InvBatch();
                batch.setInvBatchNo(invBatchNo);
                batch.setBizBatchNo(invBatchNo);
                batch.setVatNo(vatNo);
                batch.setComp(comp);
                batch.setTotalQty(aStd);
                batch.setCreateTime(new Date());
                invBatchMapper.insert(batch);
            } else {
                InvBatch upd = new InvBatch();
                upd.setId(batch.getId());
                upd.setTotalQty(nvl(batch.getTotalQty()).add(aStd));
                upd.setUpdateTime(new Date());
                invBatchMapper.updateById(upd);
            }
        } else {
            UpdateWrapper<InvBatch> bw = new UpdateWrapper<>();
            bw.eq("inv_batch_no", invBatchNo)
                .ge("total_qty", aStd)
                .setSql("total_qty = total_qty - " + aStd)
                .set("update_time", new Date());
            if (invBatchMapper.update(null, bw) == 0) {
                throw new ServiceException("批次" + invBatchNo + "总量台账不足，盘亏调整失败");
            }
        }
    }

    /** 取两者中非负较小值 */
    private static BigDecimal minPositive(BigDecimal remain, BigDecimal available) {
        BigDecimal avail = available == null ? BigDecimal.ZERO : available;
        BigDecimal take = remain.min(avail);
        return take.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : take;
    }

    private boolean existsBox(String boxCode) {
        QueryWrapper<InvBox> qw = new QueryWrapper<>();
        qw.eq("box_code", boxCode);
        return invBoxMapper.selectCount(qw) > 0;
    }

    private InvPallet selectPallet(String palletNo) {
        QueryWrapper<InvPallet> qw = new QueryWrapper<>();
        qw.eq("pallet_no", palletNo);
        return invPalletMapper.selectOne(qw);
    }

    private static int nvl(Integer v) {
        return v == null ? 0 : v;
    }

    private static BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
