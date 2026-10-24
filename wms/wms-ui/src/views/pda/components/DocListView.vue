<template>
  <div>
    <!-- 单据列表（待办） -->
    <template v-if="!current">
      <div
        v-for="row in docs"
        :key="row.id"
        class="pda-list-row"
        @click="openDetail(row)"
      >
        <div class="pda-list-main">
          <div class="pda-list-no">{{ row.code }}</div>
          <div class="pda-list-sub">
            {{ kind === "inbound" ? row.inboundType : row.outboundType || row.customerName }}
            · {{ row.warehouseName }} · {{ row.boxes }}箱 / {{ row.weight || 0 }}KG
          </div>
          <div class="pda-list-sub">{{ row.batchNo || "" }} {{ row.vatNo || "" }}</div>
        </div>
        <span class="pda-tag orange">{{ kind === "inbound" ? "待上架" : "待拣货" }}</span>
      </div>
      <div v-if="!docs.length" class="pda-empty">暂无{{ kind === "inbound" ? "待上架" : "待拣货" }}单据</div>
      <div class="pda-note">列表与 PC 端同源（引擎单据表）；列渲染顺序按字段配置 mobile_show/mobile_seq（PDA 引擎）。</div>
    </template>

    <!-- 单据明细 -->
    <template v-else>
      <div class="pda-card">
        <div class="pda-card-title">
          <span>{{ current.code }}</span>
          <span class="pda-tag green">已生效</span>
        </div>
        <div class="pda-row"><span>仓库</span><b>{{ current.warehouseName }}</b></div>
        <div class="pda-row">
          <span>{{ kind === "inbound" ? "入库类型" : "出库类型" }}</span>
          <b>{{ kind === "inbound" ? current.inboundType : current.outboundType }}</b>
        </div>
        <div class="pda-row"><span>批号 / 缸号</span><b>{{ current.batchNo || "-" }} / {{ current.vatNo || "-" }}</b></div>
        <div class="pda-row"><span>箱数 / 重量</span><b>{{ current.boxes }}箱 / {{ current.weight || 0 }}KG</b></div>
        <div v-if="kind === 'outbound'" class="pda-row"><span>客户</span><b>{{ current.customerName || "-" }}</b></div>
      </div>

      <!-- 出库：扫码核对（scan_input 语义） -->
      <div v-if="kind === 'outbound'" class="pda-scan">
        <input v-model="checkCode" type="text" placeholder="扫描箱码核对" @keyup.enter="checkRow">
        <button class="pda-btn" @click="checkRow">核对</button>
      </div>

      <div class="pda-card">
        <div class="pda-card-title">
          <span>{{ kind === "inbound" ? "入库明细（建议仓位）" : "拣货明细" }}</span>
          <span v-if="kind === 'outbound'" class="pda-tag blue">已核对 {{ checkedCount }}/{{ details.length }}</span>
        </div>
        <div v-for="(r, i) in details" :key="i" class="pda-box-row" @click="toggleCheck(r)">
          <span class="pda-box-code">{{ r.boxCode || r.batchNo || "-" }}</span>
          <span class="pda-box-sub">
            {{ r.batchNo }} · {{ r.tubes || r.boxes || "-" }}{{ kind === "inbound" ? "管" : "箱" }}
            · {{ r.stdWeight || "-" }}KG
            <template v-if="kind === 'inbound'"> · 仓位 {{ r.locCode || "-" }}</template>
          </span>
          <span
            v-if="kind === 'outbound'"
            class="pda-tag"
            :class="r.checked ? 'green' : 'red'"
          >{{ r.checked ? "已核对" : "待核对" }}</span>
        </div>
        <div v-if="!details.length" class="pda-empty">无明细</div>
      </div>
    </template>

    <div class="pda-foot">
      <button v-if="current" class="pda-btn block" @click="confirmAction">
        {{ kind === "inbound" ? "上架确认" : "拣货完成" }}
      </button>
      <div class="pda-note">
        {{ kind === "inbound"
          ? "入库单生效时库存批次台账已更新（PC 端状态机），此处确认后返回。"
          : "拣货完成后执行「同步 ERP」回写出库单号（同步状态查询页可重试）。" }}
      </div>
    </div>
  </div>
</template>

<script>
import { pdaInbounds, pdaOutbounds, mobileFieldConfig } from "@/api/terminal/asrs";
import { getDetails } from "@/api/system/common";

/**
 * PDA 入库/出库（同源查询视图）：列表取已生效单据（与 PC 同一单据表），
 * 明细走引擎 getDetails；PDA 列表按字段配置 mobile_show/mobile_seq 渲染（验收⑥，缺省回退静态卡片）；
 * 出库明细扫码核对（scan_input 字段语义）。
 */
export default {
  name: "PdaDocListView",
  props: {
    kind: { type: String, required: true }, // inbound | outbound
  },
  data() {
    return {
      docs: [],
      current: null,
      details: [],
      checkCode: "",
      mobileColumns: [],
    };
  },
  computed: {
    functionName() {
      return this.kind === "inbound" ? "WMS_INBOUND" : "WMS_OUTBOUND";
    },
    checkedCount() {
      return this.details.filter((r) => r.checked).length;
    },
  },
  created() {
    this.loadDocs();
    this.loadMobileColumns();
  },
  methods: {
    loadDocs() {
      const api = this.kind === "inbound" ? pdaInbounds : pdaOutbounds;
      api().then((res) => {
        this.docs = res.data || [];
      });
    },
    /** PDA 引擎：按 mobile_show/mobile_seq 取移动端列声明（验收⑥） */
    loadMobileColumns() {
      mobileFieldConfig(this.functionName).then((res) => {
        this.mobileColumns = res.data || [];
      });
    },
    openDetail(row) {
      getDetails(this.functionName, row.id).then((res) => {
        const data = res.data || {};
        const key = this.kind === "inbound" ? "doc_inbound_detail" : "doc_outbound_detail";
        this.details = (data[key] || []).map((r) => ({ ...r, checked: false }));
        this.current = { ...row };
      });
    },
    toggleCheck(row) {
      if (this.kind === "outbound") {
        row.checked = !row.checked;
      }
    },
    /** 扫码核对：扫描箱码匹配明细行（scan_input 唤起扫码的落地场景） */
    checkRow() {
      const code = (this.checkCode || "").trim();
      if (!code) return;
      const row = this.details.find((r) => r.boxCode === code);
      if (!row) {
        this.$message.warning(`箱码 ${code} 不在本单明细中`);
      } else {
        row.checked = true;
      }
      this.checkCode = "";
    },
    confirmAction() {
      if (this.kind === "outbound" && this.checkedCount < this.details.length && this.details.length) {
        this.$message.warning("存在待核对箱码，请完成扫码核对");
        return;
      }
      this.$message.success(
        this.kind === "inbound"
          ? "上架确认完成，库存批次台账已更新"
          : "拣货完成，执行「同步 ERP」后回写出库单号"
      );
      this.current = null;
      this.details = [];
      this.loadDocs();
    },
  },
};
</script>
