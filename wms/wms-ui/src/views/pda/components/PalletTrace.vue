<template>
  <div>
    <div class="pda-scan">
      <input v-model="scanPallet" type="text" placeholder="扫描 / 输入 托盘条码" @keyup.enter="query">
      <button class="pda-btn" @click="query">查询</button>
    </div>

    <template v-if="pallet">
      <!-- 托盘卡 -->
      <div class="pda-card">
        <div class="pda-card-title">
          <span>托盘 {{ pallet.palletNo }}</span>
          <span class="pda-tag" :class="pallet.status === '货' ? 'green' : 'gray'">状态：{{ pallet.status }}</span>
        </div>
        <div class="pda-row"><span>仓位</span><b>{{ pallet.locCode || "-" }}</b></div>
        <div class="pda-row"><span>在库箱数</span><b>{{ pallet.inStockBoxes }} 箱</b></div>
        <div class="pda-row"><span>批次</span><b>{{ pallet.batchNo || "-" }}</b></div>
      </div>

      <!-- 箱码明细（托盘→箱码→批次→仓位 追溯，验收⑤） -->
      <div class="pda-card">
        <div class="pda-card-title"><span>箱码明细</span></div>
        <div v-for="(b, i) in boxes" :key="i" class="pda-box-row">
          <span class="pda-box-code">{{ b.boxCode }}</span>
          <span class="pda-box-sub">
            {{ b.batchNo }} · {{ b.vatNo || "-" }} · {{ b.stdWeight || "-" }}KG · 仓位 {{ b.locCode || "-" }}
          </span>
          <span class="pda-tag" :class="b.status === '在库' ? 'green' : 'red'">{{ b.status }}</span>
        </div>
        <div v-if="!boxes.length" class="pda-empty">托下无箱码</div>
      </div>
    </template>
    <div v-else class="pda-empty">请扫描托盘条码查询</div>
    <div class="pda-note">追溯链路：托盘 → 箱码 → 批次 → 仓位（与 PC 端托盘/箱码/库存批次台账同源）。</div>
  </div>
</template>

<script>
import { palletTrace } from "@/api/terminal/asrs";

/**
 * PDA 扫托查询：托盘→箱码→批次→仓位 逐级追溯（验收⑤），与 PC 端台账同源。
 */
export default {
  name: "PdaPalletTrace",
  data() {
    return {
      scanPallet: "",
      pallet: null,
      boxes: [],
    };
  },
  methods: {
    query() {
      const no = (this.scanPallet || "").trim();
      if (!no) {
        this.$message.warning("请扫描托盘条码");
        return;
      }
      palletTrace(no)
        .then((res) => {
          const data = res.data || {};
          this.pallet = data.pallet || null;
          this.boxes = data.boxes || [];
          this.scanPallet = "";
        })
        .catch(() => {
          this.pallet = null;
          this.boxes = [];
        });
    },
  },
};
</script>
