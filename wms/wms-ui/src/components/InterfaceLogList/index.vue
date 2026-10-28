<template>
  <el-form>
    <DynamicList
      v-if="listIsShow"
      ref="dynamicListLog"
      :functionName="logFunctionName"
      :customizeConditions="customizeConditions"
      openMode="dialog"
    >
    </DynamicList>
  </el-form>
</template>

<script>
import DynamicList from "@/components/DynamicList/index.vue";
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
};
</script>

<style>
</style>
