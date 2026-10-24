<template>
  <div class="app-container home">
    <el-row :gutter="20">
      <el-col :sm="24" :lg="24">
        <el-card shadow="hover" class="welcome-card">
          <div class="welcome-title">WMS 智能仓储管理系统</div>
          <div class="welcome-sub">欢迎，{{ name }}</div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :sm="24" :lg="12">
        <el-card shadow="hover">
          <div slot="header"><span>系统信息</span></div>
          <div class="info-item"><span>版本</span><span>V5.0.0</span></div>
          <div class="info-item"><span>模块</span><span>入库 / 出库 / 库内 / 库存 / 计费 / 报表</span></div>
        </el-card>
      </el-col>
      <el-col :sm="24" :lg="12">
        <el-card shadow="hover">
          <div slot="header"><span>工作台</span></div>
          <div class="placeholder">功能建设中，敬请期待</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { mapGetters } from "vuex";

export default {
  name: "Index",
  computed: {
    ...mapGetters(["name"]),
  },
};
</script>

<style scoped lang="scss">
.home {
  .welcome-card {
    margin-bottom: 20px;
    text-align: center;
    .welcome-title {
      font-size: 24px;
      font-weight: bold;
      color: #303133;
    }
    .welcome-sub {
      margin-top: 10px;
      font-size: 14px;
      color: #909399;
    }
  }
  .info-item {
    display: flex;
    justify-content: space-between;
    line-height: 32px;
    font-size: 14px;
    color: #606266;
  }
  .placeholder {
    line-height: 60px;
    text-align: center;
    color: #909399;
    font-size: 14px;
  }
}
</style>
