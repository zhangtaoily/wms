<template>
  <div>
    <div class="pda-card">
      <div class="pda-card-title"><span>调拨仓库</span></div>
      <select v-model="fromWarehouseId" class="pda-select">
        <option v-for="w in warehouses" :key="'f' + w.id" :value="w.id">源：{{ w.warehouse_name }}</option>
      </select>
      <div style="height: 8px"></div>
      <select v-model="toWarehouseId" class="pda-select">
        <option v-for="w in warehouses" :key="'t' + w.id" :value="w.id">目标：{{ w.warehouse_name }}</option>
      </select>
    </div>

    <div class="pda-scan">
      <input v-model="trPallet" type="text" placeholder="扫描 / 输入 调拨托盘" @keyup.enter="scanPallet">
      <button class="pda-btn" @click="scanPallet">扫描</button>
    </div>
    <div v-if="palletInfo" class="pda-card">
      <div class="pda-row"><span>托盘</span><b>{{ palletInfo.palletNo }}</b></div>
      <div class="pda-row"><span>在库箱数</span><b>{{ palletInfo.inStockBoxes }} 箱</b></div>
      <div class="pda-row"><span>批次</span><b>{{ palletInfo.batchNo || "-" }}</b></div>
    </div>

    <div class="pda-foot">
      <button class="pda-btn block" @click="confirmTransfer">确认调拨</button>
      <div class="pda-note">
        调拨面向仓间转移；与移库的职责边界见 PRD 第 9 节 Q3：
        移库单仅承接同一仓库内仓位变更，跨仓/跨货主转移以「出库单+入库单」组合承接（P0 不做独立调拨单据）。
      </div>
    </div>
  </div>
</template>

<script>
import { palletTrace } from "@/api/terminal/asrs";
import { getTableList } from "@/api/system/common";

/**
 * PDA 调拨（Q3 语义引导页）：同仓选择引导改用「移库」；跨仓提示按 出库单+入库单 组合承接，
 * P0 不做独立调拨单据（开发计划 3.5 Q3 决策）。
 */
export default {
  name: "PdaTransferGuide",
  data() {
    return {
      warehouses: [],
      fromWarehouseId: null,
      toWarehouseId: null,
      trPallet: "",
      palletInfo: null,
    };
  },
  created() {
    getTableList({ functionName: "WAREHOUSE", pageNum: 1, pageSize: 999, conditions: [] }).then((res) => {
      this.warehouses = res.rows || [];
      if (this.warehouses.length) {
        this.fromWarehouseId = this.warehouses[0].id;
        this.toWarehouseId = this.warehouses.length > 1 ? this.warehouses[1].id : this.warehouses[0].id;
      }
    });
  },
  methods: {
    scanPallet() {
      const no = (this.trPallet || "").trim();
      if (!no) return;
      palletTrace(no)
        .then((res) => {
          this.palletInfo = res.data ? res.data.pallet : null;
          this.trPallet = "";
        })
        .catch(() => {
          this.palletInfo = null;
        });
    },
    confirmTransfer() {
      if (this.fromWarehouseId == null || this.toWarehouseId == null) {
        this.$message.warning("请选择源/目标仓库");
        return;
      }
      if (!this.palletInfo) {
        this.$message.warning("请扫描调拨托盘");
        return;
      }
      if (String(this.fromWarehouseId) === String(this.toWarehouseId)) {
        this.$message.info("同仓库内仓位变更请使用「移库」功能");
        return;
      }
      this.$message.info(
        "跨仓转移按「出库单+入库单」组合承接（Q3）：请在 PC 端分别创建调拨出库单与调拨入库单，P0 不做独立调拨单据"
      );
    },
  },
};
</script>

<style scoped>
.pda-select {
  width: 100%;
  height: 40px;
  border: 1px solid #d4dce4;
  border-radius: 8px;
  padding: 0 10px;
  font-size: 14px;
  background: #fff;
  outline: none;
}
</style>
