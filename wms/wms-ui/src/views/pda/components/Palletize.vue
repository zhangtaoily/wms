<template>
  <div>
    <!-- 未建托：扫托盘条码 -->
    <div v-if="!palletNo" class="pda-scan">
      <input v-model="scanPallet" type="text" placeholder="扫描 / 输入 托盘条码" @keyup.enter="createPallet">
      <button class="pda-btn" @click="createPallet">建托</button>
    </div>

    <template v-else>
      <!-- 托盘卡 -->
      <div class="pda-card">
        <div class="pda-card-title">
          <span>托盘 {{ palletNo }}</span>
          <span class="pda-tag green">组托方式：件入条码组托</span>
        </div>
        <div class="pda-row"><span>已入箱数</span><b>{{ boxes.length }} 箱</b></div>
        <div class="pda-row"><span>合计标重</span><b>{{ totalWeight }} KG</b></div>
        <div class="pda-row"><span>批次</span><b>{{ batchNo || "-" }}</b></div>
      </div>

      <!-- 扫箱码入托 -->
      <div class="pda-scan">
        <input v-model="scanBox" type="text" placeholder="扫描 / 输入 箱码入托" @keyup.enter="addBox">
        <button class="pda-btn" @click="addBox">扫描</button>
      </div>

      <!-- 托内箱码列表 -->
      <div class="pda-card">
        <div class="pda-card-title"><span>托内箱码</span></div>
        <div v-for="(b, i) in boxes" :key="i" class="pda-box-row">
          <span class="pda-box-code">{{ b.boxCode }}</span>
          <span class="pda-box-sub">{{ b.batchNo }} · {{ b.vatNo || "-" }} · {{ b.stdWeight || "-" }}KG</span>
          <span class="pda-del" @click="boxes.splice(i, 1)">✕</span>
        </div>
        <div v-if="!boxes.length" class="pda-empty">请扫描箱码组托</div>
      </div>
    </template>

    <div class="pda-foot">
      <button class="pda-btn block" :disabled="!palletNo || submitting" @click="donePallet">完成组托</button>
      <div class="pda-note">同托箱码归属同一托盘号且批号一致；生效后生成托盘台账并归属箱码（与 PC 端同源）。</div>
    </div>
  </div>
</template>

<script>
import { boxTrace } from "@/api/terminal/asrs";
import { save, submitData } from "@/api/system/common";

/**
 * PDA 组托（件入条码组托）：扫托盘建托 → 逐箱扫码入托 → 完成组托生成组托单（引擎 save→submit 生效）。
 * 后端 beforePass 校验同托批号一致（同托箱码归属一致，验收④）。
 */
export default {
  name: "PdaPalletize",
  data() {
    return {
      palletNo: "",
      scanPallet: "",
      scanBox: "",
      boxes: [],
      submitting: false,
    };
  },
  computed: {
    totalWeight() {
      return Math.round(this.boxes.reduce((s, r) => s + (Number(r.stdWeight) || 0), 0) * 100) / 100;
    },
    batchNo() {
      const first = this.boxes.find((b) => b.batchNo);
      return first ? first.batchNo : "";
    },
  },
  methods: {
    createPallet() {
      const no = (this.scanPallet || "").trim();
      if (!no) {
        this.$message.warning("请扫描托盘条码");
        return;
      }
      this.palletNo = no;
      this.scanPallet = "";
    },
    addBox() {
      const code = (this.scanBox || "").trim();
      if (!code) return;
      if (this.boxes.some((b) => b.boxCode === code)) {
        this.$message.warning(`箱码 ${code} 已在托内`);
        this.scanBox = "";
        return;
      }
      boxTrace(code).then((res) => {
        const info = res.data || {};
        this.boxes.push({
          boxCode: code,
          batchNo: info.exists ? info.batchNo : "",
          vatNo: info.exists ? info.vatNo : "",
          stdWeight: info.exists ? info.stdWeight : undefined,
        });
        if (!info.exists) {
          this.$message.info(`箱码 ${code} 无台账记录，请先在 PC 端完成码单收录`);
        }
        this.scanBox = "";
      });
    },
    /** 完成组托：生成 WMS_PALLET_ORDER（托盘组 + 箱明细）并提交生效 */
    donePallet() {
      if (!this.boxes.length) {
        this.$message.warning("请先扫描箱码再完成组托");
        return;
      }
      if (this.boxes.some((b) => !b.batchNo || b.stdWeight == null)) {
        this.$message.warning("存在无台账记录的箱码，请先完成码单收录");
        return;
      }
      const data = {
        palletType: "件入条码组托",
        orderDate: this.fmtNow(),
        batchNo: this.batchNo,
        remark: "PDA 组托",
        doc_pallet_order_pallet: [
          { seq: 1, palletNo: this.palletNo, boxCount: this.boxes.length, stdWeight: this.totalWeight },
        ],
        doc_pallet_order_detail: this.boxes.map((b, i) => ({
          seq: i + 1,
          palletNo: this.palletNo,
          boxCode: b.boxCode,
          batchNo: b.batchNo,
          vatNo: b.vatNo,
          boxCount: 1,
          stdWeight: b.stdWeight,
        })),
      };
      this.submitting = true;
      save("WMS_PALLET_ORDER", { data })
        .then((res) => {
          const id = res.data && res.data.id;
          return submitData("WMS_PALLET_ORDER", id, { data }).then(() => id);
        })
        .then(() => {
          this.$message.success(`组托完成：托盘 ${this.palletNo} 已生成托盘台账（${this.boxes.length} 箱）`);
          this.reset();
        })
        .catch(() => {
          this.$message.error("组托提交失败");
        })
        .finally(() => {
          this.submitting = false;
        });
    },
    reset() {
      this.palletNo = "";
      this.boxes = [];
    },
    fmtToday() {
      const d = new Date();
      const p = (n) => String(n).padStart(2, "0");
      return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
    },
    /** 组托单 orderDate 为 LocalDateTime（引擎实体），须带时间部分 */
    fmtNow() {
      const d = new Date();
      const p = (n) => String(n).padStart(2, "0");
      return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
    },
  },
};
</script>
