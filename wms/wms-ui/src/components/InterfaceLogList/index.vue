<template>
  <el-form>
    <DynamicList
      v-if="listIsShow"
      ref="dynamicListLog"
      :functionName="logFunctionName"
      :customizeConditions="customizeConditions"
      openMode="dialog"
    >
      <template #second-buttons>
        <el-button size="mini" @click="handleResend">重新发送</el-button>
      </template>
    </DynamicList>
  </el-form>
</template>

<script>
import DynamicList from "@/components/DynamicList/index.vue";
import { reSend } from "@/api/system/interfaceConfig";
export default {
  name: "InterfaceLogList",
  components: {
    DynamicList,
  },
  props: {
    functionName: {
      type: String,
    },
    logId: {
      type: String || Number,
    },
  },
  data() {
    return {
      // 接口日志的模块名
      logFunctionName: "SYS_INTERFACE_LOG",
    };
  },
  computed: {
    // 列表是否显示
    listIsShow() {
      if (this.$route.query.mode == "add" || this.$route.query.mode == "copy") {
        return false;
      }
      return Boolean(this.queryId);
    },
    queryId() {
      return this.$route.query.id || this.logId;
    },
    // 接口日志搜索条件
    customizeConditions() {
      return [
        {
          logicalOperator: "AND",
          fieldType: "TEXT",
          field: "model_id",
          operate: "EQ",
          value: this.functionName,
          filterGroup: "ADVANCE",
        },
        {
          logicalOperator: "AND",
          fieldType: "TEXT",
          field: "pact_id",
          operate: "EQ",
          value: this.queryId,
          filterGroup: "ADVANCE",
        },
      ];
    },
  },
  methods: {
    // 重新发送
    handleResend() {
      const ids = this.$refs.dynamicListLog.getIds();
      if (ids.length <= 0) {
        return this.$message.warning("请选择数据");
      } else {
        reSend(this.logFunctionName, ids[0])
          .then((res) => {
            this.$message.success("发送成功");
            this.$refs.dynamicListLog.getList();
          })
          .catch((error) => {
            this.$refs.dynamicListLog.getList();
          });
      }
    },
  },
};
</script>

<style>
</style>
