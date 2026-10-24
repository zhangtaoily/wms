<template>
  <div class="app-container">
    <!-- 汇总 -->
    <el-row :gutter="16" class="sum-row">
      <el-col :span="8">
        <el-card shadow="never">
          <div class="sum-item">
            <div class="sum-label">同步不平（未同步/失败转人工）</div>
            <div class="sum-value" :class="summary.syncIssueCount ? 'bad' : 'good'">
              {{ summary.syncIssueCount || 0 }}
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="sum-item">
            <div class="sum-label">台账差异批次（WMS 账 vs 单据推算账）</div>
            <div class="sum-value" :class="summary.ledgerDiffCount ? 'bad' : 'good'">
              {{ summary.ledgerDiffCount || 0 }}
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="sum-item">
            <div class="sum-label">对账逻辑</div>
            <div class="sum-tip">单据流水推算账 = 入库(生效,红冲取负) − 出库(生效,红冲取负) ± 已确认盘盈亏</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 同步完整性 -->
    <el-card shadow="never" class="block">
      <div slot="header">
        <span>同步完整性对账</span>
        <el-button size="mini" type="primary" plain icon="el-icon-refresh" style="float: right" @click="load">刷新</el-button>
      </div>
      <el-table :data="syncIssues" border size="small" max-height="320">
        <el-table-column label="单据类型" prop="bizType" width="100" align="center" />
        <el-table-column label="单据号" prop="docNo" min-width="140" />
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag size="mini" :type="scope.row.status === '失败' ? 'danger' : 'warning'">
              {{ scope.row.status === "失败" ? "失败(转人工)" : "未同步" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="重试次数" prop="retryCount" width="90" align="center" />
        <el-table-column label="最近消息" prop="lastMsg" min-width="200" show-overflow-tooltip />
        <el-table-column label="最近同步时间" prop="lastTime" width="160" align="center" />
        <el-table-column label="处理建议" width="220" align="center">
          <template slot-scope="scope">
            <span class="tip-text">同步监控页人工重推（reSend）</span>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!syncIssues.length" class="empty">同步链路完整：所有回写单据均同步成功</div>
    </el-card>

    <!-- 台账差异 -->
    <el-card shadow="never" class="block">
      <div slot="header"><span>台账对账（差异批次，按作业日志+单据流水定位）</span></div>
      <el-table :data="ledgerDiffs" border size="small" max-height="420">
        <el-table-column type="expand">
          <template slot-scope="scope">
            <div class="rel-box">
              <div class="rel-title">差异定位单据（{{ scope.row.batchNo }}）</div>
              <el-table :data="scope.row.relatedDocs" border size="mini" style="width: 90%; margin: 8px auto">
                <el-table-column label="单据类型" prop="docType" width="100" align="center" />
                <el-table-column label="单据号" prop="docNo" min-width="150" />
                <el-table-column label="单据日期" prop="docDate" width="120" align="center" />
              </el-table>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="批次" prop="batchNo" min-width="140" />
        <el-table-column label="推算标重(KG)" prop="expectedQty" width="130" align="right" />
        <el-table-column label="台账标重(KG)" prop="actualQty" width="130" align="right" />
        <el-table-column label="标重差异" width="120" align="right">
          <template slot-scope="scope">
            <span :class="num(scope.row.diffQty) !== 0 ? 'bad' : ''">{{ scope.row.diffQty }}</span>
          </template>
        </el-table-column>
        <el-table-column label="推算箱数" prop="expectedBox" width="110" align="right" />
        <el-table-column label="台账箱数" prop="actualBox" width="110" align="right" />
        <el-table-column label="箱数差异" width="110" align="right">
          <template slot-scope="scope">
            <span :class="num(scope.row.diffBox) !== 0 ? 'bad' : ''">{{ scope.row.diffBox }}</span>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!ledgerDiffs.length" class="empty">台账平账：全部批次账实一致</div>
      <div class="tip-text" style="margin-top: 8px">
        不平差异按作业日志+单据流水定位，人工核查后在业务单据调整或盘存单校平（PRD 7.4）。
      </div>
    </el-card>
  </div>
</template>

<script>
import { getReconcile } from "@/api/sync/reconcile";

/**
 * 同步对账报表（M7，7.4）：
 * ① 同步完整性：回写单据中未成功的同步记录（转人工重推）
 * ② 台账对账：批次台账 vs 单据流水推算账，差异批次展开定位相关单据
 */
export default {
  name: "SyncReconcile",
  data() {
    return {
      summary: {},
      syncIssues: [],
      ledgerDiffs: [],
    };
  },
  created() {
    this.load();
  },
  methods: {
    load() {
      getReconcile().then((res) => {
        const data = res.data || {};
        this.summary = data.summary || {};
        this.syncIssues = data.syncIssues || [];
        this.ledgerDiffs = data.ledgerDiffs || [];
      });
    },
    num(v) {
      return Number(v) || 0;
    },
  },
};
</script>

<style scoped>
.sum-row {
  margin-bottom: 4px;
}
.sum-item {
  text-align: center;
  padding: 6px 0;
}
.sum-label {
  font-size: 13px;
  color: #606266;
  margin-bottom: 6px;
}
.sum-value {
  font-size: 30px;
  font-weight: 700;
}
.sum-value.good {
  color: #1f9d55;
}
.sum-value.bad {
  color: #d93838;
}
.sum-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.8;
  text-align: left;
}
.block {
  margin-bottom: 16px;
}
.empty {
  text-align: center;
  color: #67c23a;
  padding: 14px 0;
  font-size: 13px;
}
.tip-text {
  font-size: 12px;
  color: #909399;
}
.rel-box {
  padding: 8px 16px;
}
.rel-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}
.bad {
  color: #d93838;
  font-weight: 600;
}
</style>
