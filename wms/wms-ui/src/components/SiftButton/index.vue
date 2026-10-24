<template>
  <el-row :gutter="5" class="sift-button">
    <el-col :span="1.5">
      <div
        style="display: flex; justify-content: space-between; align-items: end"
      >
        <el-popover placement="bottom" width="100" trigger="click">
          <el-checkbox
            :indeterminate="isIndeterminate"
            v-model="checkAll"
            @change="handleCheckAllChange"
            >全选</el-checkbox
          >
          <div style="margin: 15px 0"></div>
          <el-checkbox-group
            v-model="checkedSiftList"
            @change="handleCheckedSiftListChange"
            style="height: 300px; overflow: scroll"
          >
            <el-checkbox v-for="item in siftList" :label="item" :key="item">{{
              item
            }}</el-checkbox>
          </el-checkbox-group>
          <el-button style="margin-right: 5px" slot="reference" size="mini"
            >快捷过滤<i class="el-icon-arrow-down" style="margin-left: 5px"></i
          ></el-button>
        </el-popover>
        <el-input
          v-if="openSearchInput"
          placeholder="请输入筛选内容"
          size="mini"
          v-model="siftInput"
          @keyup.enter.native="handleSiftInputChange"
          clearable
        >
          <!-- @change="handleSiftInputChange" -->
        </el-input>
      </div>
    </el-col>
    <el-col v-if="siftDateRangeTypeList.length > 0" :span="1.5">
      <el-dropdown size="small" divided split-button trigger="click">
        <span style="font-size: 12px">{{
          siftDateRangeTypeList[0].key + " "
        }}</span>
        <el-date-picker
          editable
          :value-format="
            siftDateRangeTypeList[0].fieldType == 'DATETIME_RANGE'
              ? 'yyyy-MM-dd HH:mm:ss'
              : 'yyyy-MM-dd'
          "
          style="max-width: 200px"
          size="mini"
          v-model="siftDateRangeTypeList[0].value"
          type="datetimerange"
          :picker-options="pickerOptions"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          align="right"
        >
        </el-date-picker>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item
            v-for="item in siftDateRangeTypeList"
            :key="item.key"
            :span="1.5"
          >
            <span style="font-size: 12px">{{ item.key + " " }}</span>
            <el-date-picker
              editable
              :value-format="
                item.fieldType == 'DATETIME_RANGE'
                  ? 'yyyy-MM-dd HH:mm:ss'
                  : 'yyyy-MM-dd'
              "
              style="max-width: 200px"
              size="mini"
              v-model="item.value"
              type="datetimerange"
              :picker-options="pickerOptions"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              align="right"
              @click.stop.native="stopPropagation"
            >
            </el-date-picker>
          </el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </el-col>
    <el-col v-if="openAdvancedQuery" :span="1.5">
      <!-- 高级过滤 -->
      <el-popover
        v-model="isDynamicQueryDisabled"
        placement="right"
        width="600"
      >
        <DynamicQuery
          ref="dynamicQueryRef"
          @change-dynamic-query-disabled="changeDynamicQueryDisabled"
          :keyOptions="keyOptions"
          @handle-high-query="handleHighQuery"
          :defaultScheme="filteredDefaultScheme"
        />
        <el-button
          :class="isBtnFocus ? 'btn-bg' : ''"
          icon="el-icon-setting"
          slot="reference"
          size="mini"
          >高级过滤</el-button
        >
      </el-popover>
    </el-col>
    <el-col v-if="openSearchButton" :span="1.5">
      <el-button
        :loading="isLoading"
        icon="el-icon-search"
        size="mini"
        @click="dataChange"
        >搜索</el-button
      >
      <!-- <el-button icon="el-icon-refresh" size="mini" @click="handleResetQuery"
        >重置</el-button
      > -->
    </el-col>
  </el-row>
</template>

<script>
import QueryForm from "@/components/QueryForm/QueryForm.vue";
import QueryFormTopPanel from "@/components/QueryForm/QueryFormTopPanel";
import FieldManagement from "@/components/QueryForm/FieldManagement";
import DynamicQuery from "@/components/DynamicQuery/index.vue";
import _ from "lodash";
import { dateRangeOperateList } from "@/utils/variables.js";
export default {
  name: "SiftButton",
  created() {},
  mounted() {
    // console.log("mounted", this.options);
  },
  components: {
    QueryForm,
    QueryFormTopPanel,
    FieldManagement,
    DynamicQuery,
  },
  props: {
    // 接受可以过滤字段的参数
    options: {
      type: Array, // table数据
      default: () => [],
    },
    // 默认查询方案
    defaultScheme: {
      type: Array,
      default: () => [],
    },
    // 搜索
    handleQuery: {
      type: Function,
      required: true,
    },
    // 重置
    resetQuery: {
      type: Function,
      required: true,
    },
    // 只接收传入的筛选条件
    onlyAcceptConditions: {
      type: Boolean,
      default: () => false,
    },
    // 开启高级查询
    openAdvancedQuery: {
      type: Boolean,
      default: () => true,
    },
    // 显示搜索输入框
    openSearchInput: {
      type: Boolean,
      default: () => true,
    },
    // 显示搜索按钮
    openSearchButton: {
      type: Boolean,
      default: () => true,
    },
    // 接受的 搜索值
    inputValue: {
      type: String,
      default: () => null,
    },
  },
  watch: {
    options: {
      handler(newVal) {
        this.siftDateRangeTypeList = [];
        this.siftList = [];
        this.keyOptions = [];
        newVal.map((item) => {
          // 转换传入的options
          if (
            item.fieldType == "DATE_RANGE" ||
            item.fieldType == "DATETIME_RANGE"
          ) {
            // 关闭时间筛选组件
            if (!this.onlyAcceptConditions) {
              // 转换时间类型
              const obj = {
                key: item.headerText,
                fieldType: item.fieldType,
                columnName: item.columnName,
                fieldType: "",
              };
              if (item.defaultValue && this.defaultScheme.length == 0) {
                // 默认最近一周
                const weekTime = this.getStartAndEndTime("WEEK");
                obj.value = [this.formatDate(weekTime[0]), this.formatDate(weekTime[1])];
              }
              obj.key = item.headerText;
              obj.fieldType = item.fieldType;
              // console.log("DATETIME_RANGE", obj);
              this.isInitSiftDateRangeTypeList = true;
              this.siftDateRangeTypeList.push(obj);
            }
          }
          // 文本类型
          this.siftList.push(item.headerText);
          // 高级查询参数
          this.keyOptions.push({
            key: item.headerText,
            val: item.columnName,
            type: item.fieldType,
          });
          // 是否默认选中
          if (item.checked) {
            this.checkedSiftList.push(item.headerText);
          }
        });
        this.oldSiftDateRangeTypeList = this.siftDateRangeTypeList;
      },
    },
    // 时间组件发生了改变
    siftDateRangeTypeList: {
      handler(newVal) {
        const list = [];
        newVal.map((item) => {
          if (item.value && item.value[0]) {
            const field = this.options.find(
              (item2) => item2.headerText == item.key
            );
            list.push({
              field: field.columnName,
              operate: "GE_LE",
              value: item.value[0] + ";" + item.value[1],
              logicalOperator: item.logicalOperator || "AND",
              fieldType: field.fieldType,
              filterGroup: "ADVANCE",
            });
          }
        });
        // console.log("时间发生变化，", list);
        this.queryDateList = list;
        if (this.isInitSiftDateRangeTypeList) {
          this.isInitSiftDateRangeTypeList = false;
          this.dataChange();
        } else {
          this.dataChange();
        }
      },
      deep: true,
    },
    // 监听传入的搜索条件，赋值到siftInput中
    inputValue: {
      handler(newVal) {
        this.siftInput = newVal;
      },
      immediate: true,
    },
    siftInput: {
      handler: _.debounce(function (newVal) {
        this.handleSiftInputChange();
      }, 500), // 300 毫秒的防抖时间
      immediate: false, // 是否立即执行一次
    },
    // 监听 defaultScheme 的变化
    defaultScheme: {
      handler: function (newVal, oldVal) {
        // console.log("defaultScheme watch", newVal, oldVal);
        if (newVal.length == 0) {
          // console.log(
          //   "this.oldSiftDateRangeTypeList;",
          //   this.oldSiftDateRangeTypeList
          // );
          if (this.oldSiftDateRangeTypeList) {
            this.siftDateRangeTypeList = this.oldSiftDateRangeTypeList;
            this.$forceUpdate();
          }
          return;
        }
        // 关闭时间筛选组件
        if (!this.onlyAcceptConditions) {
          // 转换时间类型
          let obj = {};
          if (newVal?.length >= 0) {
            let filteredDefaultScheme = [];
            let siftDateRangeTypeList = [];
            newVal.forEach((item) => {
              obj = {
                key: "",
                value: [],
                fieldType: "",
                logicalOperator: "",
              };
              if (
                item.fieldType == "DATE_RANGE" ||
                item.fieldType == "DATETIME_RANGE"
              ) {
                const dateList = this.getStartAndEndTimeBy(item.operate);
                if (dateList == null) {
                  filteredDefaultScheme.push(item);
                  return;
                }
                if (item.fieldType == "DATETIME_RANGE") {
                  obj.value = [
                    this.formatDate(dateList[0]),
                    this.formatDate(dateList[1]),
                  ];
                } else {
                  obj.value = [];
                  obj.value[0] = this.parseTime(dateList[0], "{y}-{m}-{d}");
                  obj.value[1] = this.parseTime(dateList[1], "{y}-{m}-{d}");
                  // obj.value = [
                  //   this.parseTime(dateList[0], "{y}-{m}-{d}"),
                  //   this.parseTime(dateList[1], "{y}-{m}-{d}"),
                  // ];
                }

                obj.key = item.headerText;
                obj.logicalOperator = item?.logicalOperator;
                obj.fieldType = item.fieldType;
                siftDateRangeTypeList.push(obj);
              } else {
                filteredDefaultScheme.push(item);
              }
            });
            this.filteredDefaultScheme = filteredDefaultScheme;
            if (siftDateRangeTypeList == []) {
              if (this.oldSiftDateRangeTypeList) {
                this.siftDateRangeTypeList = this.oldSiftDateRangeTypeList;
              }
            } else {
              // 创建一个 Set 用于存储新数据的 key
              let newKeys = new Set(
                siftDateRangeTypeList.map((item) => item.key)
              );

              // 过滤掉 this.siftDateRangeTypeList 中与新数据 key 相同的项
              this.siftDateRangeTypeList = this.siftDateRangeTypeList.filter(
                (item) => !newKeys.has(item.key)
              );

              // 将新数据 push 到 this.siftDateRangeTypeList 中
              this.siftDateRangeTypeList.push(...siftDateRangeTypeList);
            }
            this.$forceUpdate();
          }
        }
      },
    },
  },
  data() {
    return {
      oldSiftDateRangeTypeList: [],
      // 过滤后的方案
      filteredDefaultScheme: [],
      //筛选input
      siftInput: "",
      checkAll: false,
      checkedSiftList: [],
      siftList: [],
      siftDateRangeTypeList: [],
      isIndeterminate: true,
      // 时间选择器
      pickerOptions: {
        shortcuts: [
          {
            text: "今天",
            onClick: this.handleShortcutClickByNumber(0, 0),
          },
          {
            text: "昨天",
            onClick: this.handleShortcutClickByNumber(-1, -1),
          },
          {
            text: "明天",
            onClick: this.handleShortcutClickByNumber(1, 1),
          },
          {
            text: "前三天",
            onClick: this.handleShortcutClickByNumber(0, -3),
          },
          {
            text: "最近一周",
            onClick: this.handleShortcutClick("WEEK"),
          },
          {
            text: "最近一个月",
            onClick: this.handleShortcutClick("MONTH"),
          },
          {
            text: "最近三个月",
            onClick: this.handleShortcutClick("THREEMONTHS"),
          },
          {
            text: "最近半年",
            onClick: this.handleShortcutClick("SIXMONTHS"),
          },
          {
            text: "最近一年",
            onClick: this.handleShortcutClick("YEAR"),
          },
        ],
      },
      queryList: [],
      // 传进去的查询参数
      keyOptions: [],
      // 高级查询弹窗是否禁用
      isDynamicQueryDisabled: false,
      // 是否高亮高级过滤按钮
      isBtnFocus: false,
      // 是否正在搜索
      isLoading: false,
      // 时间过滤条件是否是初始化
      isInitSiftDateRangeTypeList: false,
    };
  },
  computed: {},
  methods: {
    clearDateRanges() {
      this.siftDateRangeTypeList.forEach((item) => {
        this.$set(item, "value", []);
      });
    },
    clearDateRangesTow() {
      this.siftDateRangeTypeList.forEach((item, index) => {
        // lcl 2025/07/15 输入查询条件，默认带一年时间查询条件
        if (item.columnName === "pact_date") {
          item.operate = "GE_LE";
          // item.key = "";
          const dateList = this.getStartAndEndTime("YEAR");
          let val = [
            this.parseTime(dateList[0], "{y}-{m}-{d}"),
            this.parseTime(dateList[1], "{y}-{m}-{d}"),
          ];
          this.$set(item, "value", val);
        }
      });
    },
    // 输入框回车或者失去焦点，发送请求
    handleSiftInputChange() {
      // console.log(111);
      // console.log(this.siftInput, 111);
      // this.clearDateRangesTow();
      // if (this.siftInput) {
      //   this.clearDateRangesTow(); // 输入框有值时清除日期范围
      // }
      // if (this.siftInput) {
      //   this.clearDateRangesTow(); // 输入框有值时清除日期范围
      // }
      if (!this.siftInput) {
        this.clearDateRanges(); // 输入框有值时清除日期范围 6/14之前jsq优化
      }
      this.dataChange();
    },
    // 高级过滤方法
    handleHighQuery(list) {
      // 根据list的长度，判断是否高亮按钮
      if (list?.length == 0) {
        this.isBtnFocus = false;
      } else {
        this.isBtnFocus = true;
        this.convertToDateRange(list);
      }
      // console.log("handleHighQuery", this.queryList, this.queryDateList, list);
      const filterList = this.filterEmptyItemList([
        ...this.queryList,
        ...this.queryDateList,
        ...list,
      ]);
      this.handleQuery(filterList);
    },
    // 发送请求
    handleCustomQuery() {
      // this.dataChange()
      // console.log("发送请求queryList", this.queryList);
      const filterList = this.filterEmptyItemList([
        ...this.queryList,
        ...this.queryDateList,
      ]);
      this.handleQuery(filterList);
    },
    // 全选方法
    handleCheckAllChange(val) {
      // console.log(val, this.siftList);
      this.checkedSiftList = val ? this.siftList : [];
      this.isIndeterminate = false;
    },
    // 多选框事件触发
    handleCheckedSiftListChange(value) {
      let checkedCount = value.length;
      // 是否勾选全选按钮
      this.checkAll = checkedCount === this.siftList.length;
      // 全选按钮的状态
      this.isIndeterminate =
        checkedCount > 0 && checkedCount < this.siftList.length;
    },
    dataChange() {
      this.isLoading = true;
      const list = [];
      this.checkedSiftList.map((item) => {
        list.push({
          logicalOperator: "OR",
          fieldType: "TEXT",
          field: this.options.find((item2) => item2.headerText == item)
            .columnName,
          operate: "LIKE",
          value: this.siftInput,
          filterGroup: "FAST",
        });
      });
      this.queryList = list;
      // 是否开启高级查询
      if (this.openAdvancedQuery) {
        this.handleHighQuery(this.$refs.dynamicQueryRef.getConditionList());
      } else {
        this.handleHighQuery([]);
      }
    },
    handleShortcutClick(timeRange) {
      return (picker) => {
        picker.$emit("pick", this.getStartAndEndTime(timeRange));
      };
    },
    handleShortcutClickByNumber(s, e) {
      return (picker) => {
        picker.$emit("pick", this.getStartAndEndTimeByNumber(s, e));
      };
    },
    // 获取日期事件
    getDaysDifference(timeRange) {
      switch (timeRange) {
        case "WEEK":
          return 7;
        case "MONTH":
          return 30;
        case "THREEMONTHS":
          return 90;
        case "SIXMONTHS":
          return 180;
        case "YEAR":
          return 365;
        default:
          return 30;
      }
    },
    getStartAndEndTime(timeRange) {
      const end = new Date();
      const start = new Date();
      const daysDifference = this.getDaysDifference(timeRange);
      start.setTime(start.getTime() - 3600 * 1000 * 24 * daysDifference);
      return [start, end];
    },
    /**
     * 根据天数计算开始时间和结束时间
     * @param s 开始时间 需要加的天数
     * @param e 结束时间 需要加的天数
     */
    getStartAndEndTimeByNumber(s, e) {
      const end = new Date();
      end.setDate(end.getDate() + s);
      const start = new Date();
      start.setDate(start.getDate() + e);
      return [start, end];
    },
    handleResetQuery() {
      this.siftInput = "";
      this.siftDateRangeTypeList.forEach((item) => (item.value = []));

      this.resetQuery();
    },
    // 过滤空的条件
    filterEmptyItemList(queryList) {
      // 允许为空 的 type
      let allValueEmptyType = ["IS_NULL", "IS_NOT_NULL"];
      // console.log("queryList", queryList);
      return queryList.filter(
        (item) =>
          allValueEmptyType.includes(item.operate) ||
          (item.value &&
            item.field &&
            item.fieldType &&
            item.logicalOperator &&
            item.operate)
      );
    },
    // 改变动态查询弹出框的状态
    changeDynamicQueryDisabled(isDisabled) {
      this.isDynamicQueryDisabled = isDisabled;
    },
    /**
     * 表格时间格式化
     */
    formatDate(cellValue) {
      if (cellValue == null || cellValue == "") return "";
      var date = new Date(cellValue);
      var year = date.getFullYear();
      var month =
        date.getMonth() + 1 < 10
          ? "0" + (date.getMonth() + 1)
          : date.getMonth() + 1;
      var day = date.getDate() < 10 ? "0" + date.getDate() : date.getDate();
      var hours =
        date.getHours() < 10 ? "0" + date.getHours() : date.getHours();
      var minutes =
        date.getMinutes() < 10 ? "0" + date.getMinutes() : date.getMinutes();
      var seconds =
        date.getSeconds() < 10 ? "0" + date.getSeconds() : date.getSeconds();
      return (
        year +
        "-" +
        month +
        "-" +
        day +
        " " +
        hours +
        ":" +
        minutes +
        ":" +
        seconds
      );
    },
    // 阻止冒泡
    stopPropagation(event) {
      event.stopPropagation();
    },
    // 转换时间类型为时间
    convertToDateRange(list) {
      list.forEach((item) => {
        if (dateRangeOperateList.includes(item.operate)) {
          item.operate = "GE_LE";
          const dateList = this.getStartAndEndTime(item.defaultValue);
          item.value =
            this.parseTime(dateList[0], "{y}-{m}-{d}") +
            ";" +
            this.parseTime(dateList[1], "{y}-{m}-{d}");
        }
      });
    },
    getStartAndEndTimeBy(flag) {
      switch (flag) {
        case "TODAY":
          return this.getStartAndEndTimeByNumber(0, 0);
        case "YESTERDAY":
          return this.getStartAndEndTimeByNumber(-1, -1);
        case "TOMORROW":
          return this.getStartAndEndTimeByNumber(1, 1);
        case "GTTODAY":
          return this.getStartAndEndTimeByNumber(36500, 1);
        case "GETODAY":
          return this.getStartAndEndTimeByNumber(36500, 0);
        case "THREEDAYSBEFORE":
          return this.getStartAndEndTimeByNumber(0, -3);
        case "TWODAYSBEFORE":
          return this.getStartAndEndTimeByNumber(0, -1);
        case "LASTWEEK":
          return this.getStartAndEndTime("LASTWEEK");
        case "THISMONTH":
          return this.getStartAndEndTime("THISMONTH");
        case "WEEK":
          // console.log(
          //   'this.getStartAndEndTime("WEEK")',
          //   this.getStartAndEndTime("WEEK")
          // );
          return this.getStartAndEndTime("WEEK");
        case "MONTH":
          return this.getStartAndEndTime("MONTH");
        case "THREEMONTHS":
          return this.getStartAndEndTime("THREEMONTHS");
        case "SIXMONTHS":
          return this.getStartAndEndTime("SIXMONTHS");
        case "YEAR":
          return this.getStartAndEndTime("YEAR");
        default:
          return null;
      }
    },
  },
};
</script>

<style lang="scss" scoped>
.btn-bg {
  background: #e9f4ff;
  color: #2693fc;
  border: 1px solid #bbdffe;
}
::v-deep .el-dropdown {
  .el-button {
    padding: 0px;
    border: 0px;
  }

  .el-dropdown__caret-button {
    border: 1px solid #ccc;
    padding: 7px;
    border-radius: 6px;
  }
}
::v-deep .sift-button .el-input__inner {
  height: 28px !important;
}
</style>
