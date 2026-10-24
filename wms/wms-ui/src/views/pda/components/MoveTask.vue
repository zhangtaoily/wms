<template>
  <div>
    <!-- 仓库选择（移库单需源/目标仓库一致，Q3） -->
    <div class="pda-card">
      <div class="pda-card-title"><span>仓库</span></div>
      <select v-model="warehouseId" class="pda-select">
        <option v-for="w in warehouses" :key="w.id" :value="w.id">{{ w.warehouse_name }}</option>
      </select>
    </div>

    <!-- 步骤① 扫源托盘 -->
    <div class="pda-scan">
      <input v-model="mvPallet" type="text" placeholder="① 扫描 / 输入 源托盘" @keyup.enter="scanPallet">
      <button class="pda-btn" @click="scanPallet">扫描</button>
    </div>
    <div v-if="palletInfo" class="pda-card">
      <div class="pda-card-title">
        <span>托盘 {{ palletInfo.palletNo }}</span>
        <span class="pda-tag" :class="palletInfo.status === '在库' ? 'green' : 'gray'">{{ palletInfo.status }}</span>
      </div>
      <div class="pda-row"><span>现仓位</span><b>{{ palletInfo.locCode || "-" }}</b></div>
      <div class="pda-row"><span>在库箱数</span><b>{{ palletInfo.inStockBoxes }} 箱</b></div>
      <div class="pda-row"><span>批次</span><b>{{ palletInfo.batchNo || "-" }}</b></div>
    </div>

    <!-- 步骤② 扫目标仓位 -->
    <div class="pda-scan">
      <input v-model="mvLoc" type="text" placeholder="② 扫描 / 输入 目标仓位" @keyup.enter="noop">
      <button class="pda-btn" @click="noop">确认</button>
    </div>

    <div class="pda-foot">
      <button class="pda-btn block" :disabled="submitting" @click="confirmMove">确认移库</button>
      <div class="pda-note">
        移库类型：托盘挪库（默认）。数量不变 · 仅仓位转移；生效后仓位变更将同步 ERP（ERP 启用仓位管理）。
      </div>
    </div>
  </div>
</template>

<script>
import { palletTrace } from "@/api/terminal/asrs";
import { save, submitData, getTableList } from "@/api/system/common";

/**
 * PDA 移库（托盘挪库，默认）：扫源托盘 → 扫目标仓位 → 确认移库生成移库单（引擎 save→submit 生效），
 * 生效后台账仓位转移并同步 ERP（与 PC 端同一移库单体系，Q3 同仓承接）。
 */
export default {
  name: "PdaMoveTask",
  data() {
    return {
      warehouses: [],
      warehouseId: null,
      mvPallet: "",
      palletInfo: null,
      mvLoc: "",
      submitting: false,
    };
  },
  created() {
    this.loadWarehouses();
  },
  methods: {
    noop() {},
    loadWarehouses() {
      getTableList({ functionName: "WAREHOUSE", pageNum: 1, pageSize: 999, conditions: [] }).then((res) => {
        this.warehouses = res.rows || [];
        if (this.warehouses.length) this.warehouseId = this.warehouses[0].id;
      });
    },
    scanPallet() {
      const no = (this.mvPallet || "").trim();
      if (!no) return;
      palletTrace(no)
        .then((res) => {
          this.palletInfo = res.data ? res.data.pallet : null;
          if (!this.palletInfo) {
            this.$message.warning("托盘不存在");
          }
          this.mvPallet = "";
        })
        .catch(() => {
          this.palletInfo = null;
        });
    },
    /** 确认移库：校验 → 生成移库单（托盘挪库）并提交生效 */
    confirmMove() {
      if (!this.palletInfo) {
        this.$message.warning("请先扫描源托盘");
        return;
      }
      if (!this.mvLoc || !this.mvLoc.trim()) {
        this.$message.warning("请扫描目标仓位");
        return;
      }
      if (!this.warehouseId) {
        this.$message.warning("请选择仓库");
        return;
      }
      const toLoc = this.mvLoc.trim();
      const fromLoc = this.palletInfo.locCode || "";
      const data = {
        moveType: "托盘挪库",
        moveDate: this.fmtToday(),
        fromWarehouseId: this.warehouseId,
        fromLocCode: fromLoc,
        toWarehouseId: this.warehouseId,
        toLocCode: toLoc,
        batchNo: this.palletInfo.batchNo,
        remark: "PDA 托盘挪库",
        doc_stock_move_detail: [
          { seq: 1, palletNo: this.palletInfo.palletNo, batchNo: this.palletInfo.batchNo, toLocCode: toLoc },
        ],
      };
      this.submitting = true;
      save("WMS_STOCK_MOVE", { data })
        .then((res) => {
          const id = res.data && res.data.id;
          return submitData("WMS_STOCK_MOVE", id, { data }).then(() => id);
        })
        .then(() => {
          this.$message.success(
            `移库生效：托盘 ${this.palletInfo.palletNo}  ${fromLoc || "-"} → ${toLoc}。仓位变更将同步 ERP`
          );
          this.palletInfo = null;
          this.mvPallet = "";
          this.mvLoc = "";
        })
        .catch(() => {
          this.$message.error("移库提交失败");
        })
        .finally(() => {
          this.submitting = false;
        });
    },
    fmtToday() {
      const d = new Date();
      const p = (n) => String(n).padStart(2, "0");
      return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
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
