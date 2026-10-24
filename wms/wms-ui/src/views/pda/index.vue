<template>
  <div class="pda-page">
    <div class="pda-frame">
      <div class="pda-head">
        <span v-if="view !== 'home'" class="pda-back" @click="back">‹</span>
        <div class="pda-title">{{ title }}</div>
        <span v-if="view !== 'home'" class="pda-home" @click="goHome">主页</span>
      </div>
      <div class="pda-body">
        <!-- 工作台：2×4 网格八功能 -->
        <div v-if="view === 'home'" class="pda-grid">
          <div
            v-for="app in apps"
            :key="app.key"
            class="pda-app"
            @click="open(app.key)"
          >
            <div class="pda-app-ico" :style="{ background: app.color }">{{ app.ico }}</div>
            <div class="pda-app-name">{{ app.name }}</div>
          </div>
        </div>

        <scan-code-collect v-else-if="view === 'code'" @back="back" />
        <palletize v-else-if="view === 'pallet'" @back="back" />
        <doc-list-view v-else-if="view === 'inbound'" kind="inbound" @back="back" />
        <doc-list-view v-else-if="view === 'outbound'" kind="outbound" @back="back" />
        <move-task v-else-if="view === 'move'" @back="back" />
        <transfer-guide v-else-if="view === 'transfer'" @back="back" />
        <sync-query v-else-if="view === 'sync'" @back="back" />
        <pallet-trace v-else-if="view === 'scanP'" @back="back" />
      </div>
    </div>
  </div>
</template>

<script>
import ScanCodeCollect from "./components/ScanCodeCollect.vue";
import Palletize from "./components/Palletize.vue";
import DocListView from "./components/DocListView.vue";
import MoveTask from "./components/MoveTask.vue";
import TransferGuide from "./components/TransferGuide.vue";
import SyncQuery from "./components/SyncQuery.vue";
import PalletTrace from "./components/PalletTrace.vue";

/**
 * PDA 工作台（M6）：2×4 网格八功能，与 PC 同一单据体系（同一引擎接口保存/查询）。
 * 码单收录/组托/移库为可写动作（save→submit 引擎生效）；
 * 入库/出库为同源查询视图；同步状态查询/扫托查询为聚合查询。
 */
export default {
  name: "PdaWorkbench",
  components: {
    ScanCodeCollect,
    Palletize,
    DocListView,
    MoveTask,
    TransferGuide,
    SyncQuery,
    PalletTrace,
  },
  data() {
    return {
      view: "home",
      history: [],
      apps: [
        { key: "code", name: "码单收录", ico: "▤", color: "#2456a6" },
        { key: "pallet", name: "组托", ico: "◫", color: "#1f9d55" },
        { key: "inbound", name: "入库", ico: "⇥", color: "#0e9aa7" },
        { key: "outbound", name: "出库", ico: "⇤", color: "#d97b12" },
        { key: "move", name: "移库", ico: "⇄", color: "#7c5cd6" },
        { key: "transfer", name: "调拨", ico: "⇉", color: "#c2487c" },
        { key: "sync", name: "同步状态", ico: "⟳", color: "#5b6a7c" },
        { key: "scanP", name: "扫托查询", ico: "⌕", color: "#2f81f7" },
      ],
    };
  },
  computed: {
    title() {
      if (this.view === "home") return "PDA 工作台";
      const app = this.apps.find((a) => a.key === this.view);
      return app ? app.name : "PDA 工作台";
    },
  },
  methods: {
    open(key) {
      this.history.push(this.view);
      this.view = key;
    },
    back() {
      this.view = this.history.length ? this.history.pop() : "home";
    },
    goHome() {
      this.history = [];
      this.view = "home";
    },
  },
};
</script>

<style src="./pda.css"></style>
