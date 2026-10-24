<template>
  <div style="overflow: hidden">
    <conditionGroup
      :openFloor="false"
      :floor="floor"
      :conditionList="conditionList"
      :parentData="this"
      :keyOptions="keyOptions"
      :operateMap="operateMap"
      :val-list="valueOptions"
    >
    </conditionGroup>
    <div class="top-right-btn">
      <el-button type="warning" plain size="mini" @click="clearAll"
        >清 空</el-button
      >
      <el-button type="primary" size="mini" @click="query">确 定</el-button>
    </div>
  </div>
</template>

<script>
import conditionGroup from "./conditionGroup";

const condition = {
  id: 1,
  index: 1,
  condition: "",
  operate: "AND",
  field: "",
  value: "",
  header: true,
  checked: false,
  pid: -1,
  floor: 1,
};

import {allOperateList,operateMap} from "@/utils/variables.js"
export default {
  name: "DynamicQuery",
  props: {
    keyOptions: {
      type: Array,
      default: [],
    },
    // 默认查询方案
    defaultScheme: {
      type: Array,
      default: () => [],
    },
  },
  components: {
    conditionGroup,
  },
  data() {
    return {
      customCondition: [],
      conditionList: [],
      floor: 1,
      // keyOptions: [
      //   { 'key': 'ID', 'val': 'id' },
      //   { 'key': '名称', 'val': 'name' },
      //   { 'key': '类型', 'val': 'type' },
      //   { 'key': '来源', 'val': 'source' },
      //   { 'key': '昵称', 'val': 'nick_name' },
      //   { 'key': '创建时间', 'val': 'create_time' },
      //   { 'key': '更新时间', 'val': 'update_time' },
      // ],
      // 运算符Map
      operateMap:operateMap,
      valueOptions: {
        type: {
          dom: "select",
          data: [
            { key: "time", val: "time" },
            { key: "date", val: "date" },
          ],
        },
        source: {
          dom: "select",
          data: [
            { key: "m1", val: "m" },
            { key: "a1", val: "a" },
          ],
        },
        create_time: {
          dom: "date",
        },
        update_time: {
          dom: "date",
        },
      },
    };
  },
  created() {},
  watch: {
    // 监听 defaultScheme 的变化
    defaultScheme: {
      handler: function (newVal, oldVal) {
        // 更新 conditionList
        this.conditionList = newVal.map((item,index) => {
          return {
            logicalOperator: item.operate,
            condition: item.operate,
            operate: item.logicalOperator,
            field: item.columnName,
            value: item.value,
            index: index + 1
          };
        });
        console.log("defaultScheme", this.conditionList, newVal);
        this.query()
      },
    },
  },
  // mounted(){
  //   this.query()
  // },
  methods: {
    // 查询
    query() {
      console.log('这就是我当前高级过滤的查询query',this.conditionList);
      // 传递参数
      this.$emit(
        "handle-high-query",
        this.getConditionList()
      );
      // 隐藏弹窗
      this.$emit("change-dynamic-query-disabled",false);
    },
    // 清除所有高级查询数据
    clearAll() {
      this.conditionList = [Object.assign({}, condition)];
      // 传递参数
      this.$emit("handle-high-query", []);
      // 隐藏弹窗
      this.$emit("change-dynamic-query-disabled",false);
    },
    // 获取参数的类型
    getType(field) {
      console.log(this.keyOptions, field);
      return this.keyOptions.find((item) => item.val == field)?.type;
    },
    // 将输入的数据结构，转换为实际开发需要点结构
    transformedConditions() {
      return this.conditionList.map((condition) => {
        return {
          field: condition.field,
          fieldType: this.getType(condition.field),
          logicalOperator: condition.operate,
          operate: condition.condition,
          value: condition.value,
          filterGroup: "ADVANCE",
        };
      });
    },
    // 获取转换后的查询条件
    getConditionList(){
      return this.transformedConditions(this.conditionList)
    }
  },
};
</script>

<style scoped></style>
