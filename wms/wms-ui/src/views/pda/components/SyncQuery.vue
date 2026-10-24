<template>
  <div>
    <div v-for="row in list" :key="row.functionName + row.id" class="pda-list-row" style="cursor: default">
      <div class="pda-list-main">
        <div class="pda-list-no">{{ row.code }} <span class="pda-tag blue" style="margin-left: 6px">{{ row.docType }}</span></div>
        <div class="pda-list-sub">ERP 单号：{{ row.erpNo || "-" }}</div>
        <div class="pda-list-sub">{{ fmtTime(row.updateTime) }}</div>
      </div>
      <div style="display: flex; align-items: center; gap: 8px">
        <span class="pda-tag" :class="row.syncStatus === 1 ? 'green' : 'red'">
          {{ row.syncStatus === 1 ? "同步成功" : "未同步" }}
        </span>
        <span
          v-if="row.syncStatus !== 1"
          class="pda-tag orange"
          style="cursor: pointer"
          @click="retry(row)"
        >重试</span>
      </div>
    </div>
    <div v-if="!list.length" class="pda-empty">暂无单据</div>
    <div class="pda-note">未同步单据可幂等重试（以 ERP 单号判重，闭环⑤）。</div>
  </div>
</template>

<script>
import { pdaSyncList } from "@/api/terminal/asrs";
import { reSendData } from "@/api/system/common";

/**
 * PDA 同步状态查询：四类单据（入库/出库/移库/盘存）sync_status 聚合；
 * 未同步单据幂等重试（复用引擎 reSend，与 PC 端重发同步同链路）。
 */
export default {
  name: "PdaSyncQuery",
  data() {
    return {
      list: [],
    };
  },
  created() {
    this.load();
  },
  methods: {
    load() {
      pdaSyncList().then((res) => {
        this.list = res.data || [];
      });
    },
    retry(row) {
      reSendData(row.functionName, row.id).then(() => {
        this.$message.success(`单据 ${row.code} 已重新同步 ERP`);
        this.load();
      });
    },
    fmtTime(t) {
      if (!t) return "";
      return String(t).replace("T", " ").slice(0, 19);
    },
  },
};
</script>
