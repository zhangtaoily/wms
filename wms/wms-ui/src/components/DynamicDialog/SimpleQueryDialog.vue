<template>
  <div class="simple-query-dialog">
    <!-- 筛选组件 -->
    <el-row :gutter="5">
      <el-col v-show="false" :span="1.5">
        <!-- 字段管理 -->
        <field-management
          :key="fieldKey"
          :options="options"
          :style="
            requestParams.length > 5
              ? 'padding: 0px;margin-right: 5px;margin-left: 16px;margin-top:10px;'
              : 'padding: 0px;margin-right: 5px;'
          "
          @setColumns="getColumns"
        />
      </el-col>
    </el-row>
    <!-- 列表 -->
    <el-row>
      <!-- 右侧列表展示 -->
      <!-- 单选列表 -->
      <el-table
        v-if="true"
        :key="datekey"
        ref="singleTable"
        v-loading="loading"
        :data="dataList"
        :header-cell-style="{
          height: 'auto',
          padding: '2px 0',
        }"
        :row-class-name="getRowClassName"
        :row-key="getRowKey"
        border
        class="table-container"
        :height="tableHeight"
        @current-change="handleCurrentChange"
        @row-click="handleRowClick"
        @row-dblclick="handleRowDblClick"
      >
        <el-table-column align="center" label="序号" type="index" width="55" />
        <el-table-column
          v-for="item in finallyColumnsList"
          :key="item.id"
          :align="isTableColumnAlign(item)"
          :fixed="isTableColumnFixed(item)"
          :label="item.headerText"
          :prop="item.columnName"
          :sortable="isTableColumnSortable(item)"
          :width="processWidth(item.columnWidth)"
          class="textLeft"
          min-width="50"
          show-overflow-tooltip
        >
          <template slot="header" slot-scope="scoped">
            <span>{{ scoped.column.label }}</span>
            <br />
            <el-input
              class="header-filter-input"
              v-if="showHeaderFilter"
              :key="item.columnName"
              type="text"
              @change="handleEnter(item.columnName)"
              v-model="filterInputList[item.columnName]"
              @clear="handleEnter(item.columnName)"
              clearable
            ></el-input>
          </template>
          <template slot-scope="scoped">
            <el-popover
              v-if="item.popover == 1"
              placement="bottom"
              width="200"
              trigger="click"
              :content="scoped.row[item.columnName] + ''"
              style="max-height: 500px; overflow: scroll"
            >
              <div
                slot="reference"
                :class="{ 'is-deficit': scoped.row[item.columnName] < 0 }"
              >
                {{ scoped.row[item.columnName] }}
              </div>
            </el-popover>
            <template v-else>
              <div :class="{ 'is-deficit': scoped.row[item.columnName] < 0 }">
                {{ scoped.row[item.columnName] }}
              </div>
            </template>
          </template>
        </el-table-column>
      </el-table>
      <!-- 多选列表 -->
      <el-table
        v-else
        :key="datekey2"
        ref="table"
        v-loading="loading"
        :data="dataList"
        :header-cell-style="{ height: 'auto', padding: '2px 0' }"
        :row-class-name="getRowClassName"
        :row-key="getRowKey"
        border
        class="table-container"
        height="calc(100vh - 220px)"
        @selection-change="handleSelectionChange"
        @row-click="handleRowClick"
        @row-dblclick="handleRowDblClick"
      >
        <el-table-column align="center" type="selection" width="55" />
        <el-table-column
          v-for="item in finallyColumnsList"
          :key="item.id"
          :align="isTableColumnAlign(item)"
          :fixed="isTableColumnFixed(item)"
          :label="item.headerText"
          :prop="item.columnName"
          :sortable="isTableColumnSortable(item)"
          :width="item.columnWidth"
          min-width="50"
          show-overflow-tooltip
        >
        </el-table-column>
      </el-table>
    </el-row>
    <div :gutter="5" class="footer">
      <SiftButton
        ref="siftButton"
        :handleQuery="handleQuery"
        :options="queryOptions"
        :resetQuery="resetQuery"
        :onlyAcceptConditions="true"
        :openAdvancedQuery="false"
        :inputValue="inputValue"
        :openSearchButton="false"
        :openSearchInput="false"
        :defaultScheme="defaultScheme"
      />
      <!-- 分页 -->
      <pagination
        v-show="total > 0"
        :limit.sync="queryParams.pageSize"
        :page.sync="queryParams.pageNum"
        :total="total"
        @pagination="getList"
        :layout="'total,prev,next,jumper'"
        :autoScroll="false"
      />
      <!-- <slot name="buttons"></slot> -->
    </div>
  </div>
</template>

<script>
import { getDictDataList } from "@/api/system/dict/data";
import { radioList } from "@/utils/variables.js";
import columnManagement from "@/components/columnManagement/index.vue";
import FieldManagement from "@/components/QueryForm/FieldManagement";
import DynamicQuery from "@/components/DynamicQuery/index.vue";
import SiftButton from "@/components/SiftButton/index.vue";
import { getFieldConfigList, getTableList } from "@/api/system/common";

const isTrue = {
  NO: 0,
  YES: 1,
};
export default {
  name: "DynamicList",
  components: {
    FieldManagement,
    DynamicQuery,
    SiftButton,
    columnManagement,
  },
  props: {
    // 是否开启单选功能
    openRadioSelection: {
      type: Boolean,
      default: () => true,
    },
    // 模块名称
    functionName: {
      type: String,
      required: true,
    },
    // 弹窗名称
    dialogName: {
      type: String,
      default: () => null,
    },
    // 打开组件的模式 list dialog
    openMode: {
      type: String,
      default: "list",
    },
    // 自定义搜索
    // 例子：[{
    //     "logicalOperator": "OR",
    //     "fieldType": "TEXT",
    //     "field": "purpose_id",
    //     "operate": "LIKE",
    //     "value": "11"
    // }]
    // 自定义搜索条件
    customizeConditions: {
      type: Array,
      default: () => [],
    },
    // 在弹窗模式下是否单击行返回数据
    singleClickReturnData: {
      type: Boolean,
      default: () => false,
    },
    // table的高度
    tableHeight: {
      type: String,
      default: () => `258`,
    },
    // 打开数据列表时 已选择的数据设置为勾选状态
    showSelectRows: {
      type: [Array, String],
      default: () => [],
    },
    // 接受的 搜索值
    inputValue: {
      type: String,
      default: () => null,
    },
    // 显示表头过滤输入框
    showHeaderFilter: {
      type: Boolean,
      default: () => true,
    },
  },
  data() {
    return {
      // 存放搜索条件
      filters: [],
      // 存放一份列表数据
      _dataList: [],
      // 存放过滤输入框中的值
      filterInputList: [],
      dicts: [
        "t_date_range",
        "field_type",
        "field_formatter_type",
        "field_align_type",
      ],
      radioList: radioList,
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 表格数据
      dataList: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 20,
      },
      requestParams: [],
      datekey: Date.now(),
      datekey2: Date.now(),
      paginationkey: Date.now(),
      fieldKey: Date.now(),
      // 列表最终显示的字段
      // finallyColumnsList: [],
      // 接受后端的字段配置，字段显示配置
      options: [],
      // 接受后端的字段配置，查询配置
      queryOptions: [],
      // 默认查询方案
      defaultScheme: [],
      transfers: [],
      optionList: [],
      queryList: [],
      // 存放多选数据
      multiDataList: [],
      // 存放单选数据
      radioData: null,
      dictDataList: {},
      // 在回显已选择的多选框时，toggleRowSelection会触发handleSelectionChange，导致报错，用变量来阻断此时的事件
      echoFlag: true,
    };
  },
  watch: {},
  created() {
    // this.getDictData();
    // 获取传入的模块名称
    this.queryParams.functionName = this.functionName;
    // 获取字段配置数据
    this.getFieldConfig();
    this.optionList = Array.from(this.options, (obj) => ({ ...obj }));
    this.rightcheckchange();
  },
  computed: {
    // 列表最终显示的字段
    finallyColumnsList: function () {
      const list = this.options.filter(
        (item) => item.columnWidth > 0 && item.isShow == 1
      );
      this.datekey = Date.now();
      this.datekey2 = Date.now();
      return list;
    },
    // 总共有几页数据
    // totalPageNum: function () {
    //   return Math.ceil(this.total / this.queryParams.pageSize);
    // },
    // 返回过滤条件
    filterInputCondition() {
      return this.filters.map((item) => ({
        logicalOperator: "AND",
        fieldType: "TEXT",
        field: item.field,
        operate: "LIKE",
        value: item.value,
        filterGroup: "ADVANCE",
      }));
    },
  },
  methods: {
    // 获取选中的第一个item
    getSelectedItem() {
      return this.dataList.find((item) => item.id == this.ids[0]);
    },
    // 获取选中的item，id列表
    getSelectedDetailsIdList() {
      if (this.dataList[0]?.main_id) {
        return this.multiDataList.map((item) => item.id);
      } else {
        return [];
      }
    },
    setCurrent(row) {
      this.$refs.singleTable.setCurrentRow(row);
    },
    handleCurrentChange(val) {},
    getRowClassName({ row, rowIndex }) {
      if (this.ids.includes(row.id)) {
        return "selected-row-color";
      }
      return "";
    },
    // 行单击方法
    handleRowClick(row, column, event) {
      if (true) {
        this.radioData = row;
        console.log(row, this.ids, this.ids.includes(row.id));
        console.log("openMode", this.openMode);

        // 实现双击功能
        if (this.ids.includes(row.id) || this.singleClickReturnData) {
          // 如果是弹出框模式，并且是单选，需要触发方法
          if (this.openMode == "dialog") {
            if (this.openRadioSelection) {
              this.$emit("handle-dialog-submit", row);
            } else {
              this.$emit("handle-dialog-submit", [row]);
            }
          }
          this.setCurrent(null);
          this.ids = [];
          this.single = this.ids.length !== 1;
          this.multiple = !this.ids.length;
        } else {
          this.ids = [row.id];
          this.single = this.ids.length !== 1;
          this.multiple = !this.ids.length;
        }
      } else {
        const selectedRows = this.$refs.table.selection;
        const isRowSelected = selectedRows.some(
          (selectedRow) => selectedRow.id === row.id
        );
        this.$refs.table.toggleRowSelection(row, !isRowSelected);
      }
    },
    getRowKey(row) {
      return row.main_id + row.id;
    },
    // 行双击方法
    handleRowDblClick(row, column, event) {
      console.log("双击行", row, column, event);
    },
    finallyColumns2() {
      let list = this.columns.filter((item) =>
        this.checkList.includes(item.headerText)
      );
      //这里更新了datekey ，组件就会刷新
      this.datekey = Date.now();
      this.paginationkey = Date.now();
      // this.finallyColumnsList = list.filter((item) => item.columnWidth > 0);
      // console.log("this.finallyColumnsList :>> ", this.finallyColumnsList);
      return list;
    },
    // 子组件筛选条件返回
    getColumns(columns, checkList) {
      this.columns = columns;
      this.checkList = checkList;
      this.finallyColumns2();
    },
    rightcheckchange() {
      this.requestParams = [];
      for (let i = 0; i < this.transfers.length; i++) {
        for (let j = 0; j < this.options.length; j++) {
          if (this.transfers[i] === this.options[j].name) {
            this.requestParams.push(this.options[j]);
          }
        }
      }
    },
    getList: _.debounce(function (addConditions) {
      // console.log("this.queryParams.conditions", this.queryParams.conditions);
      this.loading = true;
      // console.log(this.queryParams);

      // 创建一个新的条件数组副本
      const conditions = [...(this.queryParams.conditions || [])]; // 如果this.queryParams.conditions是undefined或null，则默认为空数组

      // 自定义查询条件
      if (
        Array.isArray(this.customizeConditions) &&
        this.customizeConditions.length > 0
      ) {
        conditions.push(...this.customizeConditions);
      }

      // 表头查询条件
      if (
        Array.isArray(this.filterInputCondition) &&
        this.filterInputCondition.length > 0
      ) {
        conditions.push(...this.filterInputCondition);
      }
      // 发送请求
      getTableList({
        functionName: this.functionName,
        pageNum: this.queryParams.pageNum,
        pageSize: this.queryParams.pageSize,
        dialogName: this.dialogName,
        conditions,
      })
        .then((response) => {
          // 处理响应
          this.total = response.total;
          this.options = response.headers;
          this.fieldKey = Date.now();

          // this.dataList = this.isTree
          //   ? this.handleTree(response.rows, "id", "parent_id")
          //   : response.rows;
          this.dataList = response.rows;
          this._dataList = response.rows;

          // 将已选择的数据状态变为已勾选
          if (this.showSelectRows.length) {
            this.$nextTick(() => {
              this.echoFlag = false;
              this.multiDataList = [];

              this.showSelectRows.forEach((el) => {
                let selectRow = this.dataList.find((item) => el == item.id);
                this.$refs.table.toggleRowSelection(selectRow, true);

                this.multiDataList.push(selectRow);
              });

              this.ids = this.multiDataList.map((item) => item.id);
              this.single = this.multiDataList.length !== 1;
              this.multiple = !this.multiDataList.length;

              this.echoFlag = true;
            });
          }
        })
        .finally(() => {
          this.loading = false;
          this.$refs.siftButton.isLoading = false;
        });
    }),
    getFieldConfig() {
      getFieldConfigList(
        this?.dialogName || this.queryParams.functionName
      ).then((res) => {
        this.queryOptions = res.data.fieldConfigList;
        this.defaultScheme = res.data.defaultScheme;
        this.fieldKey = Date.now();
      });
    },
    /** 搜索按钮操作 */
    handleQuery(conditions) {
      // console.log("搜索按钮操作", conditions);
      this.queryParams.pageNum = 1;
      this.queryParams.conditions = [];
      if (conditions) {
        this.queryParams.conditions = conditions;
      }
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      if (!this.echoFlag) return;
      this.multiDataList = selection;

      this.ids = selection.map((item) => item.id);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    // 处理多选数据返回
    handleMultiDataSubmit() {
      this.$emit("handle-dialog-submit", this.multiDataList);
    },
    // 处理单选返回
    handleRadioDataSubmit() {
      this.$emit("handle-dialog-submit", this.radioData);
    },
    processWidth(num) {
      // num'%'结尾
      if (typeof num === "string" && num.endsWith("%")) {
        // 如果是，直接返回
        return num;
      } else {
        // 如果不是，将其转换为字符串（如果它还不是），并拼接'px'
        return `${num}px`;
      }
    },
    // 获取ids
    getIds() {
      let idLit = [];
      this.ids.forEach((id) => {
        const findItem = this.dataList.find((item) => item.id == id);
        idLit.push(findItem?.main_id || findItem?.id || null);
      });
      return idLit;
    },
    // 返回字段是否排序
    isTableColumnSortable(item) {
      if (isTrue.YES == item.columnSort) {
        return true;
      } else {
        return false;
      }
    },
    // 返回字段是否是固定列
    isTableColumnFixed(item) {
      if (isTrue.YES == item.fixedColumn) {
        return true;
      } else {
        return false;
      }
    },
    // 返回字段对齐方式
    isTableColumnAlign(item) {
      const textAlign = {
        LEFT: 0,
        CENTER: 1,
        RIGHT: 2,
      };
      if (item.align == textAlign.LEFT) {
        return "left";
      } else if (item.align == textAlign.CENTER) {
        return "center";
      } else if (item.align == textAlign.RIGHT) {
        return "right";
      } else {
        return "left";
      }
    },
    // 获取全部数据字典数据
    getDictData() {
      this.dictList = getDictDataList(this.dicts)
        .then((res) => {
          // console.log("dictList", res);
          this.dictDataList = res.data;
        })
        .catch((err) => {
          this.$message.error("获取数据字段数据失败");
        });
    },
    // 第一行input输入框的搜索事件
    handleEnter: _.debounce(function (field) {
      console.log("handleEnter出发了");
      const value = this.filterInputList[field];
      // 如果为空，删除查询条件
      if (!value) {
        this.filters = this.filters.filter((item) => item.field != field);
      } else {
        // 保存查询条件
        this.filters = [...this.filters, { field, value }];
      }
      // 重新构建查询条件
      this.getList();

      // 过滤当前页
      // this.dataList = this._dataList.filter((item) => {
      //   return this.filters.every((filter) => {
      //     return (
      //       item[filter.field] &&
      //       String(item[filter.field]).includes(String(filter.value))
      //     );
      //   });
      // });
    }),
  },
};
</script>
<style lang="scss" scoped>
.simple-query-dialog {
  position: relative;
}
::v-deep .footer {
  position: absolute;
  bottom: 0px;
  width: 100%;
  background: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  .pagination-container .el-pagination {
    right: 6px;
    top: -8px;
  }
}
.table-container {
  margin-bottom: 50px;
}
::v-deep .page-select {
  .el-input__inner {
    width: 75px;
  }
  .el-icon-circle-close {
    display: none !important;
  }
}
::v-deep .header-filter-input {
  .el-input__inner {
    padding: 0px;
  }
  .el-input__validateIcon {
    display: none;
  }
}
</style>
