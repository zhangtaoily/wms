<template>
  <div>
    <!-- 类型页签 -->
    <div class="pda-card">
      <div class="pda-card-title">业务类型</div>
      <div>
        <span
          v-for="t in codeTypes"
          :key="t"
          class="pda-tag"
          :class="t === codeType ? 'blue' : 'gray'"
          style="margin-right: 8px; cursor: pointer; padding: 4px 12px"
          @click="codeType = t"
        >{{ t }}</span>
      </div>
      <div v-if="codeType === '产件入库'" class="pda-note">MES 移交箱，属性随箱核对。</div>
    </div>

    <!-- 扫描箱码（scan_input 唤起扫码） -->
    <div class="pda-scan">
      <input
        v-model="scanCode"
        type="text"
        placeholder="扫描 / 输入 箱码"
        @keyup.enter="scanBox"
      >
      <button class="pda-btn" @click="scanBox">扫描</button>
    </div>

    <!-- 已收录箱列表 -->
    <div class="pda-card">
      <div class="pda-card-title">
        <span>已收录 {{ rows.length }} 箱</span>
        <span class="pda-tag" :class="rows.length ? 'green' : 'gray'">合计 {{ totalWeight }} KG</span>
      </div>
      <div v-for="(r, i) in rows" :key="i" class="pda-box-row">
        <span class="pda-box-code">{{ r.boxBarcode }}</span>
        <span class="pda-box-sub">{{ r.batchNo || "批号未填" }} · 标重 {{ r.stdWeight || "-" }}KG</span>
        <span class="pda-del" @click="rows.splice(i, 1)">✕</span>
      </div>
      <div v-if="!rows.length" class="pda-empty">请扫描箱码开始收录</div>
    </div>

    <div class="pda-foot">
      <button class="pda-btn block" :disabled="submitting" @click="submitCode">提交码单</button>
      <div class="pda-note">提交后生成码单收录单并生效，箱码台账同步生成（与 PC 端同源）。</div>
    </div>
  </div>
</template>

<script>
import { boxTrace } from "@/api/terminal/asrs";
import { save, submitData } from "@/api/system/common";

/**
 * PDA 码单收录：扫描箱码收录（箱码台账已有时自动回填批号/标重），提交生成码单收录单（引擎 save→submit 生效），
 * 与 PC 端同一单据体系（验收④ 数据同源）。
 */
export default {
  name: "PdaScanCodeCollect",
  data() {
    return {
      codeTypes: ["产件入库", "采购入库", "调拨入库"],
      codeType: "产件入库",
      scanCode: "",
      rows: [],
      submitting: false,
    };
  },
  computed: {
    totalWeight() {
      return Math.round(this.rows.reduce((s, r) => s + (Number(r.stdWeight) || 0), 0) * 100) / 100;
    },
  },
  methods: {
    /** 扫描箱码：台账已存在自动回填，未收录则新增手工行 */
    scanBox() {
      const code = (this.scanCode || "").trim();
      if (!code) return;
      if (this.rows.some((r) => r.boxBarcode === code)) {
        this.$message.warning(`箱码 ${code} 已收录`);
        this.scanCode = "";
        return;
      }
      boxTrace(code).then((res) => {
        const info = res.data || {};
        const row = {
          boxBarcode: code,
          batchNo: info.exists ? info.batchNo : "",
          invBatchNo: info.exists ? info.batchNo : "",
          tubes: info.exists ? info.tubes : undefined,
          stdWeight: info.exists ? info.stdWeight : undefined,
          grossWeight: info.exists ? info.grossWeight : undefined,
          netWeight: info.exists ? info.netWeight : undefined,
          known: !!info.exists,
        };
        this.rows.push(row);
        if (!info.exists) {
          this.$message.info(`箱码 ${code} 无台账记录，请补填批号与标重`);
        }
        this.scanCode = "";
      });
    },
    /** 提交码单：生成 WMS_CODE_COLLECT 单据并提交生效 */
    submitCode() {
      if (!this.rows.length) {
        this.$message.warning("请先扫描箱码再提交码单");
        return;
      }
      const missing = this.rows.find((r) => !r.batchNo || r.stdWeight == null);
      if (missing) {
        this.$message.warning(`箱码 ${missing.boxBarcode} 批号/标重未补全`);
        return;
      }
      const data = {
        bizType: this.codeType,
        collectDate: this.fmtToday(),
        batchNo: this.rows[0].batchNo,
        qty: this.rows.length,
        remark: "PDA 码单收录",
        doc_code_collect_detail: this.rows.map((r, i) => ({
          seq: i + 1,
          boxBarcode: r.boxBarcode,
          batchNo: r.batchNo,
          invBatchNo: r.invBatchNo || r.batchNo,
          tubes: r.tubes,
          stdWeight: r.stdWeight,
          grossWeight: r.grossWeight,
          netWeight: r.netWeight,
        })),
      };
      this.submitting = true;
      save("WMS_CODE_COLLECT", { data })
        .then((res) => {
          const id = res.data && res.data.id;
          return submitData("WMS_CODE_COLLECT", id, { data }).then(() => id);
        })
        .then(() => {
          this.$message.success(`码单提交成功（${this.codeType}，${this.rows.length} 箱）`);
          this.rows = [];
        })
        .catch(() => {
          this.$message.error("码单提交失败");
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
