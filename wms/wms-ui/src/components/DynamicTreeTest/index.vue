<template>
  <div class="app-container">
    <!-- 列表 -->
    <el-row :gutter="20">
      <!-- 左侧树形筛选 -->
      <el-col v-if="isTree" :span="24">
        <el-input
          v-model="treeFilterInput"
          placeholder="请输入分组名称"
          clearable
          size="mini"
          prefix-icon="el-icon-search"
          style="
            border: 0px none !important;
            margin-top: 15px;
            margin-bottom: 15px;
          "
        />
        <div class="tree-wrap">
          <!-- 树形组件 -->
          <!-- :data="treeOptions" -->
          <el-tree
            ref="tree"
            :default-expanded-keys="['0']"
            :expand-on-click-node="false"
            :filter-node-method="filterNode"
            :load="loadNode"
            :props="treeProps"
            highlight-current
            lazy
            node-key="id"
            @node-click="handleNodeClick"
            :key="treeKey"
          />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getDictDataList } from "@/api/system/dict/data";
import { radioList } from "@/utils/variables.js";
import FieldManagement from "@/components/QueryForm/FieldManagement";
import DynamicQuery from "@/components/DynamicQuery/index.vue";
import SiftButton from "@/components/SiftButton/index.vue";
import { getPrintButton, rollBackData } from "@/api/system/common";
import qs from "qs";
import {
  getFieldConfigList,
  getTableList,
  deleteById,
} from "@/api/system/common";

const isTrue = {
  NO: 0,
  YES: 1,
};
export default {
  name: "DynamicTreeTest",
  components: {
    FieldManagement,
    DynamicQuery,
    SiftButton,
  },
  props: {
    // 是否开启双击保存
    isDoubleClick: {
      type: Boolean,
      default: () => false,
    },
    // 显示按钮list
    // ['add','copy','pushDown','delete','update','export','print']
    hiddenButtonList: {
      type: Array,
      default: () => ["pushDown"],
    },
    // 自定义添加按钮事件
    handleCustomAdd: {
      type: Function,
      default: null,
    },
    // 自定义修改按钮事件
    handleCustomUpdate: {
      type: Function,
      default: null,
    },
    // 自定义删除按钮事件
    handleCustomDelete: {
      type: Function,
      default: null,
    },
    // 是否开启单选功能
    openRadioSelection: {
      type: Boolean,
      default: () => true,
    },
    buttons: {
      type: Array, // button数据
      default: () => [],
    },
    // 模块名称
    functionName: {
      type: String,
      required: true,
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
    isTree: {
      type: Boolean,
      default: () => false,
    },
    // 树形默认Props
    treeProps: {
      type: Object,
      default: () => ({
        children: "children",
        label: "cn_name",
        isLeaf: "leaf",
      }),
    },
    // 树形结构的模块名
    treeFunctionName: {
      type: String,
      default: () => "",
    },
    // 树形结构主键字段
    treePrimaryKey: { type: String, default: () => "id" },
    // 外键字段（列表里面，关联树的id）
    foreignKey: { type: String, default: () => "parent_id" },
    // 树形结构外键字段（树的上级id）
    treeForeignKey: { type: String, default: () => "parent_id" },
    // 树形的打开链接
    treePath: {
      type: String,
    },
    // 左侧树的默认查询参数 只有在本身页面为分组（如物料分组）时不做筛选
    treeDefaultParams: {
      type: Array,
      default: () => [
        {
          logicalOperator: "AND",
          fieldType: "TEXT",
          field: "pact_state",
          operate: "EQ",
          value: "通过",
          filterGroup: "ADVANCE",
        },
        {
          logicalOperator: "AND",
          fieldType: "TEXT",
          field: "enabled",
          operate: "EQ",
          value: "是",
          filterGroup: "ADVANCE",
        },
      ],
    },
    // 在弹窗模式下是否单击行返回数据
    singleClickReturnData: {
      type: Boolean,
      default: () => false,
    },
    // table的高度
    tableHeight: {
      type: String,
      default: () => `calc(100vh - 220px)`,
    },
  },
  provide() {
    return {
      mark: "Member", //特定标识,根据业务模块不同,传输的标识也不同,标识由后端定义(或者字典维护)
    };
  },
  data() {
    return {
      dicts: [
        "t_date_range",
        "field_type",
        "field_formatter_type",
        "field_align_type",
      ],
      radioList: radioList,
      // 按钮loading
      buttonLoading: false,
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
      selectedItem: null,
      datekey: Date.now(),
      datekey2: Date.now(),
      paginationkey: Date.now(),
      fieldKey: Date.now(),
      // 列表最终显示的字段
      finallyColumnsList: [],
      // 接受后端的字段配置，字段显示配置
      options: [],
      // 接受后端的字段配置，查询配置
      queryOptions: [],
      transfers: [],
      optionList: [],
      queryList: [],
      // 存放多选数据
      multiDataList: [],
      // 打印按钮列表
      printButtonList: [],
      // 树形过滤input
      treeFilterInput: "",
      // 树形数据
      treeOptions: [],
      // 存放单选数据
      radioData: null,
      // 存放树形节点，选中的数据
      selectedTreeItem: null,
      dictDataList: {},
      treeKey: Date.now(),

      //双击保存
      nodeCount: 0,
      preNodeId: null,
      curNodeId: null,
      nodeTimer: null,
    };
  },
  watch: {
    // 根据名称筛选部门树
    treeFilterInput(val) {
      this.$refs.tree.filter(val);
    },
  },
  created() {
    this.getDictData();

    // 获取传入的模块名称
    this.queryParams.functionName = this.functionName;
    // 获取字段配置数据
    this.getFieldConfig();
    this.optionList = Array.from(this.options, (obj) => ({ ...obj }));
    this.rightcheckchange();
    // 获取打印按钮数据
    this.getPrintButtonList();
    // 初始化监听localStorage
    this.addStorageEventListener();
  },

  beforeDestroy() {
    this.removeStorageEventListener();
  },
  computed: {
    rollBackBtnIsShow: function () {
      if (!this.single) {
        return true;
      }
      return (
        this.dataList.find((item) => item.id == this.ids[0])?.status == "PASS"
      );
    },
  },
  methods: {
    loadNode(node, resolve) {
      if (!this.treeFunctionName) {
        this.$message.error("请输入树形结构的模块名称");
        return resolve([]);
      }
      // 初始化顶级节点
      if (node.level === 0) {
        const topLevel = [
          {
            cn_name: "全部",
            code: "all",
            id: "0",
          },
        ];
        resolve(topLevel);
      }
      // 获取其他节点
      if (node.level > 0) {
        this.getTree(node.data.id).then((res) => {
          resolve(res);
        });
      }
    },
    /** 查询树结构 */
    getTree(value) {
      let initTreeParams = {
        pageNum: 1,
        pageSize: 999,
        functionName: this.treeFunctionName,
        conditions: [
          {
            logicalOperator: "AND",
            fieldType: "TEXT",
            field: this.treeForeignKey,
            operate: "EQ",
            value: value,
            filterGroup: "ADVANCE",
          },
          ...this.treeDefaultParams,
        ],
      };
      return getTableList(initTreeParams).then((response) => {
        return response.rows;
      });
    },
    // 节点单击事件
    handleNodeClick(data) {
      this.selectedTreeItem = data;
      // console.log("handleNodeClick data :>> ", data);
      // console.log(
      //   "handleNodeClick this.treePrimaryKey :>> ",
      //   this.treePrimaryKey
      // );
      let conditions = [];
      if (data?.id != "0") {
        conditions = [
          {
            logicalOperator: "AND",
            fieldType: "TEXT",
            field: this.foreignKey,
            operate: "EQ",
            value: data[this.treePrimaryKey],
            filterGroup: "ADVANCE",
          },
        ];
      }
      // 保存选中的ID值
      console.log("ffff", data);

      this.radioData = data;
      // this.getList(conditions);
      //this.$emit("handle-dialog-submit", data);

      // 实现双击保存
      if (this.isDoubleClick) {
        this.nodeCount++;
        if (this.preNodeId && this.nodeCount >= 2) {
          this.curNodeId = data.id;
          this.nodeCount = 0;
          if (this.curNodeId == this.preNodeId) {
            //第一次点击的节点和第二次点击的节点id相同
            // 调用父保存方法
            this.$emit("submit");

            this.curNodeId = null;
            this.preNodeId = null;
            return;
          }
        }
        this.preNodeId = data.id;
        this.nodeTimer = setTimeout(() => {
          //300ms内没有第二次点击就把第一次点击的清空
          this.preNodeId = null;
          this.nodeCount = 0;
        }, 300);
      }
    },
    // 筛选节点
    filterNode(value, data) {
      if (!value) return true;
      return data.cn_name.indexOf(value) !== -1;
    },
    addStorageEventListener() {
      window.addEventListener("storage", this.handleStorageChange);
    },
    removeStorageEventListener() {
      window.removeEventListener("storage", this.handleStorageChange);
    },
    handleStorageChange(event) {
      if (event.key === "needUpdatedFunctionName") {
        if (event.newValue != "" && event.newValue == this.functionName) {
          console.log("数据更新，需要发送请求");
          this.getList();
        }
        if (event.newValue != "" && event.newValue == this.treeFunctionName) {
          console.log("树形组件，数据更新，需要发送请求");
          // this.resetChecked();
          console.log(this.treeKey, "this.treeKey ");
          this.treeKey = Date.now();
        }
      }
    },
    getSelectedItem() {
      return this.dataList.find((item) => item.id == this.ids[0]);
    },
    /** 反审核按钮 */
    rollbackForm() {
      if (!this.single) {
        this.buttonLoading = true;
        // 寻找主表id
        const obj = this.dataList.find((item) => item.id == this.ids[0]);
        rollBackData(this.functionName, obj?.main_id || obj.id)
          .then((res) => {
            this.$modal.msgSuccess("反审核成功");
            // this.$emit("setForm", res.data);
            this.getList();
          })
          .finally(() => {
            this.buttonLoading = false;
          });
      }
    },
    // 根据传入的name，判断按钮是否显示
    buttonIsShow(name) {
      return !this.hiddenButtonList.includes(name);
    },
    // 获取打印按钮list
    getPrintButtonList() {
      getPrintButton(this.functionName).then((res) => {
        // this.form = res.data;
        this.printButtonList = res.data;
      });
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
    // 处理多选数据返回
    handleMultiDataSubmit() {
      this.$emit("handle-dialog-submit", this.multiDataList);
    },
    // 处理单选返回
    handleRadioDataSubmit() {
      this.$emit("handle-dialog-submit", this.radioData);
    },
    // 行单击方法
    handleRowClick(row, column, event) {
      if (this.openRadioSelection) {
        this.radioData = row;
        console.log(row, this.ids, this.ids.includes(row.id));
        console.log("openMode", this.openMode);

        // 实现双击功能
        if (this.ids.includes(row.id) || this.singleClickReturnData) {
          // 如果是弹出框模式，并且是单选，需要触发方法
          if (this.openMode == "dialog" && this.openRadioSelection) {
            this.$emit("handle-dialog-submit", row);
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
      if (this.openMode == "list") {
        let viweMode =
          row.pact_state == "通过" || row.pact_state == "审核中"
            ? "show"
            : "edit";
        this.openPage(viweMode, row.main_id || row.id);
      }
    },
    finallyColumns2() {
      let list = this.columns.filter((item) =>
        this.checkList.includes(item.headerText)
      );
      //这里更新了datekey ，组件就会刷新
      this.datekey = Date.now();
      this.paginationkey = Date.now();
      this.finallyColumnsList = list.filter((item) => item.columnWidth > 0);
      console.log("this.finallyColumnsList :>> ", this.finallyColumnsList);
      return list;
    },
    // 子组件筛选条件返回
    getColumns(columns, checkList) {
      this.columns = columns;
      this.checkList = checkList;
      this.finallyColumns2();
    },
    //搜索
    upSearch(val) {
      //调用子组件查询列表方法
      this.queryParams = val;
      this.queryParams.pageNum = this.queryForm.pageNum;
      this.queryParams.pageSize = this.queryForm.pageSize;
      this.$emit("templete-list", this.queryParams);
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
    cellStyleForm() {
      if (this.requestParams.length + 1 > 6) {
        return "mpc-section-zkaw";
      } else {
        return "mpc-section";
      }
    },
    // 点击了打印按钮
    handlePrintSelect(item) {
      if (!this.single) {
        window.open(item.linkUrl, "_blank");
      } else {
        this.$message.warning("请选择一条数据");
      }
    },
    getList(addConditions) {
      console.log("this.queryParams.conditions", this.queryParams.conditions);
      this.loading = true;
      console.log(this.queryParams);

      // 创建一个新的条件数组副本
      const conditions = [...(this.queryParams.conditions || [])]; // 如果this.queryParams.conditions是undefined或null，则默认为空数组

      // 自定义查询条件
      if (
        Array.isArray(this.customizeConditions) &&
        this.customizeConditions.length > 0
      ) {
        conditions.push(...this.customizeConditions);
      }

      // 添加额外的查询条件
      if (Array.isArray(addConditions) && addConditions.length > 0) {
        conditions.push(...addConditions);
      }

      // 发送请求
      getTableList({
        functionName: this.functionName,
        pageNum: this.queryParams.pageNum,
        pageSize: this.queryParams.pageSize,
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
        })
        .finally(() => {
          this.loading = false;
          // this.$refs.siftButton.isLoading = false;
        });
    },
    getFieldConfig() {
      getFieldConfigList(this.queryParams.functionName).then((response) => {
        this.queryOptions = response.data;
        this.fieldKey = Date.now();
      });
    },
    /** 搜索按钮操作 */
    handleQuery(conditions) {
      console.log("搜索按钮操作", conditions);
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
      this.multiDataList = selection;
      this.ids = selection.map((item) => item.id);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
      console.log("多选框选中数据");
    },
    /** 新增按钮操作 */
    handleAdd() {
      if (this.handleCustomAdd) {
        this.handleCustomAdd();
      } else {
        this.openPage("add");
      }
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      if (this.handleCustomUpdate) {
        this.handleCustomUpdate(row.id || this.ids);
      } else {
        const item = this.getSelectedItem();
        this.openPage("edit", item?.main_id || item.id);
      }
    },
    openPage(mode, id, detailId) {
      let modeMap = {
        add: "新增",
        edit: "编辑",
        show: "查看",
        copy: "复制",
      };
      let str = modeMap[mode];
      // 打开页签
      let { path, query, meta } = this.$route;
      const params = {
        functionName: query.functionName,
        mode,
        id: this.ids && this.ids[0],
      };
      if (id) {
        params.id = id;
        if (mode == "copy") {
          params.detailId = detailId;
        }
      }

      if (mode == "add") {
        delete params.id;
      }
      // 格式化url
      let path2 =
        path +
        "/form?" +
        decodeURIComponent(qs.stringify(params)).replace(/\[([0-9]+)\]=/g, "=");
      window.open(path2, "_blank");
      // this.$tab.openPage(`${meta.title}-${str}`, path + "/form", params);
    },
    // 清空树形筛选条件
    resetChecked() {
      this.handleNodeClick(null);
      this.$refs.tree.setCurrentKey(null);
      // this.$refs.tree.setCheckedKeys(null);
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
    // 树形组件-添加按钮
    addTreeHandler() {
      this.treeOpenUrlHandler("add");
    },
    // 树形组件-编辑按钮
    editTreeHandler() {
      this.treeOpenUrlHandler("edit");
    },
    // 树形组件-删除按钮
    deleteTreeHandler() {
      if (this.selectedTreeItem) {
        console.log(this.selectedTreeItem);
        const id = this.selectedTreeItem.id;
        this.$modal
          .confirmHTML('是否确认<span style="color: red;">删除</span>该条数据')
          .then(() => {
            // return deleteById("RAW_MATERIAL_GROUP", id);
            return deleteById(this.treeFunctionName, id);
          })
          .then(() => {
            this.$refs.tree.remove(this.selectedTreeItem);
            this.$modal.msgSuccess("删除成功");
          })
          .catch(() => {})
          .finally(() => {});
      } else {
        this.$message.warning("请选择树形节点");
      }
    },
    // 根据模式处理树形组件按钮
    treeOpenUrlHandler(mode) {
      console.log(this.selectedTreeItem);
      if (this.selectedTreeItem || mode == "add") {
        const params = {
          functionName: this.treeFunctionName,
          mode,
          id: this.selectedTreeItem?.id || "",
        };
        let path = this.treePath + "?" + qs.stringify(params);
        window.open(path, "_blank");
      } else {
        this.$message.warning("请选择树形节点");
      }
    },
    // 获取全部数据字典数据
    getDictData() {
      this.dictList = getDictDataList(this.dicts)
        .then((res) => {
          console.log("dictList", res);
          this.dictDataList = res.data;
        })
        .catch((err) => {
          this.$message.error("获取数据字段数据失败");
        });
    },
  },
};
</script>
<style lang="scss" scoped>
.head-container {
  display: flex;
  height: 30px;
  justify-content: center;
  align-content: center;
}

.button-group {
  margin-bottom: 5px;
}

.table-container {
  height: calc(100vh - 220px);
  overflow: scroll;
}

.pagination-container {
  margin: 0px !important;
  padding: 0px !important;
}

::v-deep .el-input__inner,
.el-textarea__inner,
.el-input {
  border: 1px solid #dcdfe6 !important;
  background-color: white !important;
  border-color: #dcdfe6 !important;
}

::v-deep .el-table th.el-table__cell > .cell {
  white-space: nowrap;
  text-align: center;
}

::v-deep .el-table--enable-row-transition .el-table__body td.el-table__cell {
  // text-align: left;
}

.tree-box {
  border: 1px solid #ccc;
  height: calc(100vh - 220px);
  overflow: scroll;
}

.fixed-field {
  ::v-deep .el-table th.el-table__cell.is-leaf,
  .el-table td.el-table__cell {
    padding: 10px 0px;
  }
}

::v-deep .el-radio {
  margin-right: 10px !important;
}

.required {
  color: red;
  margin-left: 3px;
}
::v-deep .el-dialog__body {
  padding: 0 20px !important;
}
::v-deep .el-dialog__body .el-table .cell {
  text-align: center;
}
.tree-wrap {
  border: 1px solid #dcdfe6;
  height: 40vh;
  overflow: auto;
}
</style>
