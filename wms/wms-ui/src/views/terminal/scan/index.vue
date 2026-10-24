<template>
  <div class="scan-page">
    <!-- 头部 -->
    <div class="scan-head">
      <div class="sh-brand">
        <div class="brand-mark">W</div>
        <div>
          <div class="sh-title">立库扫码终端</div>
          <div class="sh-sub">YUNSUO WMS · ASRS SCAN TERMINAL V3.69</div>
        </div>
      </div>
      <div class="scan-exit" @click="handleExit">退出终端</div>
    </div>

    <!-- 主界面：三个入口 -->
    <div v-if="view === 'main'" class="scan-main">
      <div class="scan-btns">
        <div class="scan-btn" @click="enterWork('barcode')">
          <div class="sb-ico">▥</div>
          <div class="sb-text">条码出库</div>
        </div>
        <div class="scan-btn" @click="enterWork('empty')">
          <div class="sb-ico">▢</div>
          <div class="sb-text">空托出库</div>
        </div>
        <div class="scan-btn" @click="enterWork('test')">
          <div class="sb-ico">▣</div>
          <div class="sb-text">条码出库测试</div>
        </div>
      </div>
    </div>

    <!-- 条码出库作业视图 -->
    <div v-else class="scan-work">
      <div class="sw-top">
        <div class="sw-search">
          <input
            v-model="keyword"
            type="text"
            placeholder="扫描 / 输入 批次任务号"
            @keyup.enter="loadTasks"
          >
          <button class="sw-btn" @click="loadTasks">检 索</button>
        </div>
        <div class="bay-tabs">
          <span
            v-for="bay in bays"
            :key="bay"
            class="bay-tab"
            :class="{ active: bay === activeBay }"
            @click="activeBay = bay"
          >{{ bay }}</span>
        </div>
        <div class="sw-actions">
          <span class="btn-start" @click="handleStart">开始作业</span>
          <span class="btn-cancel" @click="view = 'main'">取消返回</span>
        </div>
      </div>

      <div class="sw-panels">
        <div class="sw-panel">
          <div class="swp-title">业务批次任务</div>
          <div class="sw-list">
            <div
              v-for="t in bizTasks"
              :key="'b' + t.id"
              class="task-card"
              :class="{ selected: selectedTaskId === t.id }"
              @click="selectTask(t)"
            >
              <div class="tc-no"><span>{{ t.no }}</span><span class="tc-time">{{ t.time }}</span></div>
              <div class="tc-batch">{{ t.batch }} / {{ t.vat }}</div>
              <div class="tc-qty"><span class="need">需出: {{ t.need }}</span><span class="done">已出: {{ t.done }}</span></div>
            </div>
            <div v-if="!bizTasks.length" class="sw-empty">暂无待执行批次任务</div>
          </div>
          <div class="sw-sum">【业务批次任务合计】 {{ bizTasks.length }} 单</div>
        </div>
        <div class="sw-panel">
          <div class="swp-title">托盘批次任务</div>
          <div class="sw-list">
            <div
              v-for="t in palletTasks"
              :key="'p' + t.palletNo"
              class="task-card"
              :class="{ selected: selectedTaskId === t.id }"
              @click="selectTask(t)"
            >
              <div class="tc-no"><span>{{ t.palletNo }}</span><span class="tc-time">{{ t.time }}</span></div>
              <div class="tc-batch">{{ t.batch }} / {{ t.vat }}</div>
              <div class="tc-qty"><span class="need">需出: {{ t.need }}</span><span class="done">已出: {{ t.done }}</span></div>
            </div>
            <div v-if="!palletTasks.length" class="sw-empty">暂无托盘批次任务</div>
          </div>
          <div class="sw-sum">【托盘批次任务合计】 {{ palletTasks.length }} 托</div>
        </div>
      </div>

      <!-- WCS 回流（模拟上报，演示验收③） -->
      <div class="sw-callback">
        <div class="sw-cb-title">作业回流（WCS 完成上报）</div>
        <div v-for="t in opTasks" :key="t.id" class="sw-cb-row">
          <span class="cb-wcs">{{ t.wcsTaskId }}</span>
          <span class="cb-type">{{ t.taskType }}</span>
          <span class="cb-status" :class="t.execStatus === '执行中' ? 'running' : 'ok'">{{ t.execStatus }}</span>
          <span
            v-if="t.execStatus === '执行中'"
            class="cb-btn"
            @click="handleComplete(t)"
          >完成回流</span>
        </div>
        <div v-if="!opTasks.length" class="sw-empty">暂无执行中作业</div>
      </div>
    </div>

    <!-- 底部 -->
    <div class="scan-foot">
      <span>操作员: 终端操作员</span>
      <span>{{ nowText }}</span>
    </div>
  </div>
</template>

<script>
import {
  getBays,
  searchTasks,
  startOutbound,
  startEmptyOutbound,
  startTestOutbound,
  completeOutbound,
  getOpTasks,
} from "@/api/terminal/asrs";

/**
 * 立库扫码终端 H5（M6 FR-07）
 * 条码出库/空托出库/条码出库测试；11 个月台回路；业务批次任务/托盘批次任务双面板选任务；
 * 未选任务点"开始作业"阻断并提示（验收① 文案与 PRD 一致）；
 * 开始作业下发 WCS 选托下架任务，作业完成回流后任务/作业日志/托盘状态联动（验收③）。
 */
export default {
  name: "AsrsScanTerminal",
  data() {
    return {
      view: "main",
      mode: "barcode",
      bays: [],
      activeBay: "",
      keyword: "",
      bizTasks: [],
      palletTasks: [],
      selectedTaskId: null,
      selectedTask: null,
      opTasks: [],
      nowText: "--:--:--",
      timer: null,
    };
  },
  created() {
    this.loadBays();
    this.loadTasks();
    this.loadOpTasks();
    this.timer = setInterval(() => {
      this.nowText = new Date().toLocaleTimeString("zh-CN", { hour12: false });
    }, 1000);
  },
  beforeDestroy() {
    if (this.timer) clearInterval(this.timer);
  },
  methods: {
    loadBays() {
      getBays().then((res) => {
        this.bays = res.data || [];
        if (!this.activeBay && this.bays.length) this.activeBay = this.bays[0];
      });
    },
    loadTasks() {
      searchTasks(this.keyword).then((res) => {
        const data = res.data || {};
        this.bizTasks = data.bizTasks || [];
        this.palletTasks = data.palletTasks || [];
        this.selectedTaskId = null;
        this.selectedTask = null;
      });
    },
    loadOpTasks() {
      getOpTasks().then((res) => {
        this.opTasks = (res.data || []).filter((t) => t.execStatus === "执行中");
      });
    },
    enterWork(mode) {
      this.mode = mode;
      this.view = "work";
      this.loadTasks();
      this.loadOpTasks();
    },
    selectTask(task) {
      if (this.selectedTaskId === task.id) {
        this.selectedTaskId = null;
        this.selectedTask = null;
        return;
      }
      this.selectedTaskId = task.id;
      this.selectedTask = task;
    },
    /** 开始作业：未选任务阻断（验收①），选中则下发 WCS 选托下架（验收②） */
    handleStart() {
      if (this.mode === "empty") {
        startEmptyOutbound({ bay: this.activeBay }).then(() => {
          this.$message.success("空托出库任务下发成功，输送线已启动！");
          this.loadOpTasks();
        });
        return;
      }
      if (this.mode === "test") {
        startTestOutbound({ bay: this.activeBay }).then(() => {
          this.$message.success("条码出库测试指令下发成功！");
          this.loadOpTasks();
        });
        return;
      }
      if (!this.selectedTask) {
        this.$message.warning("请选择有效的批次任务，再进行选托下架作业！");
        return;
      }
      startOutbound({ whTaskId: this.selectedTaskId, bay: this.activeBay }).then(() => {
        this.$message.success("任务下发成功，输送线已启动选托下架作业！");
        this.loadTasks();
        this.loadOpTasks();
      });
    },
    /** WCS 完成上报（模拟）：任务完成 + 作业日志 + 空托状态回写 */
    handleComplete(task) {
      completeOutbound({ opTaskId: task.id, palletNo: task.palletNo }).then((res) => {
        const data = res.data || {};
        this.$message.success(
          `作业回流完成：${data.wcsTaskId || ""}${data.whTaskNo ? "（任务 " + data.whTaskNo + "）" : ""} 已留痕`
        );
        this.loadTasks();
        this.loadOpTasks();
      });
    },
    handleExit() {
      if (window.history.length > 1) {
        this.$router.go(-1);
      }
    },
  },
};
</script>

<style scoped>
.scan-page {
  min-height: 100vh;
  background: #0f1922;
  color: #d5e2ec;
  display: flex;
  flex-direction: column;
  font-size: 14px;
}
.scan-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 22px;
  background: #16222e;
  border-bottom: 1px solid #233240;
}
.sh-brand {
  display: flex;
  align-items: center;
  gap: 10px;
}
.brand-mark {
  width: 36px;
  height: 36px;
  border-radius: 6px;
  background: #2f81f7;
  color: #fff;
  font-weight: 700;
  font-size: 19px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.sh-title {
  font-size: 17px;
  font-weight: 600;
  color: #fff;
}
.sh-sub {
  font-size: 10px;
  color: #5b7286;
  letter-spacing: 0.5px;
}
.scan-exit {
  padding: 6px 14px;
  border: 1px solid #3c5265;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
}
.scan-exit:hover {
  border-color: #2f81f7;
  color: #fff;
}
.scan-main {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
.scan-btns {
  display: flex;
  gap: 26px;
}
.scan-btn {
  width: 210px;
  height: 150px;
  border-radius: 10px;
  background: #182733;
  border: 1px solid #2a3b4a;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.2s;
}
.scan-btn:hover {
  border-color: #2f81f7;
  background: #1d3140;
}
.sb-ico {
  font-size: 38px;
  color: #2f81f7;
}
.sb-text {
  font-size: 17px;
  font-weight: 600;
  color: #eaf2f8;
}
.scan-work {
  flex: 1;
  padding: 14px 22px;
  overflow: auto;
}
.sw-search {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}
.sw-search input {
  flex: 1;
  max-width: 420px;
  height: 36px;
  border-radius: 4px;
  border: 1px solid #2a3b4a;
  background: #101b24;
  color: #eaf2f8;
  padding: 0 12px;
  outline: none;
}
.sw-btn {
  height: 36px;
  padding: 0 18px;
  border: none;
  border-radius: 4px;
  background: #2f81f7;
  color: #fff;
  cursor: pointer;
}
.bay-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}
.bay-tab {
  padding: 6px 12px;
  border-radius: 4px;
  background: #16222e;
  border: 1px solid #2a3b4a;
  font-size: 12px;
  cursor: pointer;
}
.bay-tab.active {
  background: #2f81f7;
  border-color: #2f81f7;
  color: #fff;
}
.sw-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 14px;
}
.btn-start,
.btn-cancel {
  padding: 8px 22px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}
.btn-start {
  background: #1f9d55;
  color: #fff;
}
.btn-cancel {
  border: 1px solid #3c5265;
  color: #9fb3c2;
}
.sw-panels {
  display: flex;
  gap: 16px;
}
.sw-panel {
  flex: 1;
  min-width: 0;
  background: #16222e;
  border: 1px solid #233240;
  border-radius: 8px;
  padding: 12px;
}
.swp-title {
  font-size: 14px;
  font-weight: 600;
  color: #eaf2f8;
  margin-bottom: 10px;
}
.sw-list {
  min-height: 160px;
  max-height: 380px;
  overflow: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.task-card {
  border: 1px solid #2a3b4a;
  border-radius: 6px;
  padding: 10px 12px;
  cursor: pointer;
  background: #101b24;
}
.task-card.selected {
  border-color: #2f81f7;
  box-shadow: 0 0 0 1px #2f81f7 inset;
}
.tc-no {
  display: flex;
  justify-content: space-between;
  font-weight: 600;
  color: #eaf2f8;
}
.tc-time {
  font-size: 11px;
  color: #5b7286;
  font-weight: 400;
}
.tc-batch {
  font-size: 12px;
  color: #9fb3c2;
  margin: 4px 0;
}
.tc-qty {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
}
.need {
  color: #f0a020;
}
.done {
  color: #35c26e;
}
.sw-sum {
  margin-top: 10px;
  font-size: 12px;
  color: #5b7286;
}
.sw-empty {
  text-align: center;
  color: #5b7286;
  padding: 20px 0;
  font-size: 12px;
}
.sw-callback {
  margin-top: 16px;
  background: #16222e;
  border: 1px solid #233240;
  border-radius: 8px;
  padding: 12px;
}
.sw-cb-title {
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 8px;
}
.sw-cb-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 0;
  border-bottom: 1px dashed #22303d;
  font-size: 12px;
}
.cb-wcs {
  color: #eaf2f8;
  min-width: 150px;
}
.cb-type {
  color: #9fb3c2;
}
.cb-status.running {
  color: #f0a020;
}
.cb-status.ok {
  color: #35c26e;
}
.cb-btn {
  margin-left: auto;
  padding: 3px 10px;
  border-radius: 4px;
  background: #1f9d55;
  color: #fff;
  cursor: pointer;
}
.scan-foot {
  display: flex;
  justify-content: space-between;
  padding: 10px 22px;
  background: #16222e;
  border-top: 1px solid #233240;
  font-size: 12px;
  color: #5b7286;
}
</style>
