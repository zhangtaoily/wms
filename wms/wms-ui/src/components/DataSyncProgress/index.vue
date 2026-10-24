<template>
  <div class="data-sync-container">
    <div>
      <h2>数据同步进度</h2>
    </div>

    <el-collapse v-if="codeListMap" accordion>
      <el-collapse-item title="详情" :name="详情">
        <el-collapse v-if="codeListMap" v-model="activeName" accordion>
          <el-collapse-item
            v-for="(item, index) in Object.entries(codeListMap)"
            :key="index"
            :title="'——'+item[0] + '，共' + item[1].length + '条'"
            :name="String(index)"
          >
            <div v-for="(code, codeIndex) in item[1]" :key="codeIndex">
              {{ code }}
            </div>
          </el-collapse-item>
        </el-collapse>
      </el-collapse-item>
    </el-collapse>
    <div class="progress-container">
      <div class="progress-info">
        <span class="percentage">{{ progress }}%</span>
        <span class="items-count">({{ processedItems }}/{{ totalItems }})</span>
      </div>
      <el-progress
        :percentage="progress"
        :status="progressStatus"
        :stroke-width="16"
      ></el-progress>
      <div class="status-message">{{ statusMessage }}</div>
    </div>

    <div class="action-buttons">
      <el-button type="primary" @click="startSync" :disabled="isSyncing">
        开始同步
      </el-button>
      <el-button
        type="danger"
        @click="cancelSync"
        :disabled="!isSyncing || isCompleted"
      >
        取消同步
      </el-button>
    </div>

    <div v-if="error" class="error-message">
      <el-alert :title="error" type="error" show-icon></el-alert>
    </div>

    <div class="sync-details" v-if="syncDetails.length > 0">
      <h3>同步详情</h3>
      <ul>
        <li v-for="(detail, index) in syncDetails" :key="index">
          [{{ detail.timestamp }}] {{ detail.message }}
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
import { cancelDataSync } from "@/api/system-maintain/index.js";
export default {
  name: "DataSyncProgress",
  props: {
    getQueryParams: {
      type: Function,
    },
  },
  data() {
    return {
      openDetails: false,
      activeName: null,
      codeListMap: null,
      progress: 0,
      processedItems: 0,
      totalItems: 0,
      statusMessage: "等待开始同步...",
      error: null,
      eventSource: null,
      isSyncing: false,
      syncDetails: [],
      lastUpdateTime: null,
      userId: this.$store.state.user.id,
    };
  },
  computed: {
    isCompleted() {
      return this.progress === 100;
    },
    progressStatus() {
      if (this.progress === 100) return "success";
      if (this.error) return "exception";
      return null;
    },
  },
  methods: {
    getQueryString() {
      const params = this.getQueryParams();
      console.log("params", params);
      // 将参数转换为查询字符串
      const queryString = new URLSearchParams(params).toString();
      return queryString;
    },
    startSync() {
      const queryString = this.getQueryString();
      console.log("queryString", queryString);
      if (queryString) {
        // 重置状态
        this.resetState();
        this.isSyncing = true;
        this.statusMessage = "正在连接服务器...";
        this.addSyncDetail("开始数据同步流程");

        // 创建EventSource连接
        this.createEventSource(queryString);

        this.addSyncDetail("已建立服务器连接，等待数据...");
      }
    },
    createEventSource(queryString) {
      const url = `${process.env.VUE_APP_BASE_API}/system-maintain/data-import/progress/${this.userId}?${queryString}`;
      this.eventSource = new EventSource(url);

      // 处理消息事件
      this.eventSource.addEventListener("progress", (event) => {
        const data = JSON.parse(event.data);
        this.updateProgress(data);
      });

      // 处理消息事件
      this.eventSource.addEventListener("codeListMap", (event) => {
        const data = JSON.parse(event.data);
        console.log("codeListMap", data);
        this.codeListMap = data;
      });

      // 处理错误
      this.eventSource.onerror = (error) => {
        if (this.progress < 100 && !this.error) {
          this.error = "同步连接异常中断";
          this.statusMessage = "同步异常中断";
        }
        this.closeConnection();
        console.error("EventSource error:", error);
      };
    },
    updateProgress(data) {
      this.progress = data.progress;
      this.processedItems = data.processedItems;
      this.totalItems = data.totalItems;
      this.statusMessage = data.message;

      // 添加同步详情，限制频率避免太多消息
      // const now = new Date();
      // if (!this.lastUpdateTime || now - this.lastUpdateTime > 1000) {
      //   this.addSyncDetail(data.message);
      //   this.lastUpdateTime = now;
      // }
      this.addSyncDetail(data.message);

      // 同步完成时关闭连接
      if (data.status === "COMPLETED" || data.status === "FAILED") {
        this.addSyncDetail(
          `同步${data.status === "COMPLETED" ? "完成" : "失败"}`
        );
        this.closeConnection();
      }
    },
    cancelSync() {
      this.addSyncDetail("用户请求取消同步");
      cancelDataSync(this.userId)
        .then((response) => {
          this.statusMessage = "同步已取消";
          this.addSyncDetail("同步已成功取消");
        })
        .catch((error) => {
          this.error = "取消同步时出错: " + error.message;
          this.addSyncDetail("取消同步时出错: " + error.message);
        })
        .finally(() => {
          this.closeConnection();
        });
    },
    closeConnection() {
      if (this.eventSource) {
        this.eventSource.close();
        this.eventSource = null;
      }
      this.isSyncing = false;
    },
    resetState() {
      this.progress = 0;
      this.processedItems = 0;
      this.totalItems = 0;
      this.error = null;
      this.syncDetails = [];
      this.lastUpdateTime = null;
      this.codeListMap = null;
    },
    addSyncDetail(message) {
      const timestamp = new Date().toLocaleTimeString();
      this.syncDetails.push({ timestamp, message });
      // 限制详情数量，避免内存问题
      if (this.syncDetails.length > 50) {
        this.syncDetails.shift();
      }
    },
  },
  beforeDestroy() {
    // 组件销毁前关闭连接
    this.closeConnection();
  },
};
</script>

<style scoped>
.data-sync-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.progress-container {
  margin: 30px 0;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 4px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 16px;
}

.percentage {
  font-weight: bold;
  color: #409eff;
}

.status-message {
  margin-top: 10px;
  font-size: 14px;
  color: #666;
}

.action-buttons {
  margin: 20px 0;
  text-align: center;
}

.sync-details {
  margin-top: 30px;
  padding: 15px;
  background: #f9f9f9;
  border-radius: 4px;
  max-height: 300px;
  overflow-y: auto;
}

.sync-details h3 {
  margin-top: 0;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.sync-details ul {
  list-style-type: none;
  padding-left: 0;
}

.sync-details li {
  padding: 5px 0;
  border-bottom: 1px dashed #eee;
  font-size: 13px;
}

.error-message {
  margin: 20px 0;
}
</style>