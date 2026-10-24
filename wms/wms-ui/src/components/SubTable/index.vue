<template>
  <div>
    <div v-if="!notShowFormHeader && !hideButtonBox" class="button-box">
      <slot name="buttonsRight"></slot>
      <!-- 子表 -->
      <el-button
        type="primary"
        size="mini"
        @click="addRow"
        icon="el-icon-plus"
        v-if="!isOpenDialog"
        :disabled="disabled"
        >添 加</el-button
      >
      <DynamicDialogBtn
        v-else
        @data-change="dataChangeForDialog"
        :options="menuOptions"
        :customizeConditions="customizeConditions"
        :disabled="disabled"
        :form="form"
        :isSecondaryDialog="isSecondaryDialog"
        :menuDisabledList="menuDisabledList"
      />
      <el-button
        type="danger"
        size="mini"
        @click="handleDelete"
        icon="el-icon-delete"
        :disabled="buttonIsDisabled('remove')"
        >移 除</el-button
      >
      <template v-if="isOpenSort">
        <el-button
          :disabled="disabled"
          size="mini"
          @click="moveUp"
          icon="el-icon-arrow-up"
          >上 移</el-button
        >
        <el-button
          :disabled="disabled"
          size="mini"
          @click="moveDown"
          icon="el-icon-arrow-down"
          >下 移</el-button
        >
      </template>

      <el-button size="mini" @click="checkAll" icon="el-icon-circle-check"
        >全 选</el-button
      >

      <el-button
        size="mini"
        @click="reverseElection"
        icon="el-icon-refresh-left"
        >反 选</el-button
      >
      <el-button
        v-if="openCopyBtn"
        :disabled="disabled"
        size="mini"
        @click="copyRowHandle()"
        icon="el-icon-document-copy"
        >复 制</el-button
      >
      <el-button
        v-if="openFillingBtn"
        :disabled="disabled"
        size="mini"
        @click="fillingRowHandle()"
        icon="el-icon-document-copy"
        >批量填充</el-button
      >
      <div v-if="isPass" style="margin-left: 10px">
        <BusinessOperationBtn
          v-if="isOpenCancelBtn"
          :functionName="functionName"
          :showTableGoodsBtn="true"
          :hiddenButtonList="hiddenButtonList"
          :multiDataList="multipleSelection"
        />
      </div>
      <slot name="buttons"></slot>
      <div style="position: absolute; right: 16px">
        <el-button
          :disabled="false"
          size="mini"
          @click="toggleExpand"
          :icon="isExpanded ? 'el-icon-arrow-up' : 'el-icon-arrow-down'"
          >{{ isExpanded ? "收 起" : "展开全部" }}</el-button
        >
      </div>
    </div>
    <div class="resizable-table-container">
      <el-table
        v-table-keyboard-nav
        ref="subTable"
        border
        :row-class-name="tableRowClassName"
        @current-change="handleCurrentChange"
        @header-click="handleCellClick"
        @row-click="handleRowClick"
        @selection-change="handleSelectionChange"
        :data="subData"
        :header-cell-class-name="getHeaderCellClassName"
        style="width: 100%; margin-top: 5px"
        row-key="sortIndex"
        :height="tableHeight"
        :max-height="tableHeight"
        class="sub-table"
        :show-summary="showSummary"
        :summary-method="getSummaries"
        :class="{ 'is-disabled-table': disabled }"
        :header-row-style="
          () => {
            return 'line-height:30px';
          }
        "
      >
        <!-- selectable 值为true为可选用，和disabled值相反 -->
        <el-table-column
          v-if="!isOpenRadio"
          align="center"
          type="selection"
          width="55"
        />
        <!-- :selectable="() => !disabled" -->
        <el-table-column
          key="1"
          type="index"
          label="序号"
          width="55"
          align="center"
        />
        <!-- 加入宽度为1的列，解决顺序错乱的问题 -->
        <el-table-column width="1" align="center" />
        <slot name="table-column"></slot>
      </el-table>
      <div class="drag-handle" @mousedown="startResize"></div>
    </div>

    <!-- 弹窗 -->
    <el-dialog title="选择来源" :visible.sync="dialogVisible">
      <el-radio v-model="selectedRadio" label="1">备选项</el-radio>
      <el-radio v-model="selectedRadio" label="2">备选项</el-radio>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="radioConfirm">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import DynamicDialogBtn from "@/components/DynamicDialogBtn/index.vue";
import CancelBtn from "@/components/DynamicList/CancelBtn.vue";
import CloseBtn from "@/components/DynamicList/CloseBtn.vue";
import BusinessOperationBtn from "../DynamicList/BusinessOperationBtn.vue";
import { cloneDeep, cloneDeepWith } from "lodash";
import tableKeyboardNavigation from "@/directive/table/tableKeyboardNavigation";
import { relationshipsList } from "@/utils/variables";
export default {
  name: "SubTable",
  components: {
    DynamicDialogBtn,
    CancelBtn,
    CloseBtn,
    BusinessOperationBtn,
  },
  directives: {
    // 局部注册指令
    "table-keyboard-nav": tableKeyboardNavigation,
  },
  inject: {
    // 必须存在的依赖（没有会报错）
    notShowFormHeader: { default: null }, // 需要保留的强制依赖
    clearValidate: { default: null }, // 需要保留的强制依赖

    // 可选依赖（没有时会使用默认值）
    setMatIdHandler: { default: () => {} }, // 空函数
    setRawMatHandler: { default: () => {} }, // 空函数
    subTableWatch: { default: () => {} }, // 空函数
    setPrintColorIdHandler: { default: () => {} }, // 空函数
    dataChangeForCompColorId: { default: () => {} }, // 空函数
    totalCostPriceCalc: { default: () => {} }, // 空函数
  },
  props: {
    // 是否二级弹窗
    isSecondaryDialog: {
      type: Boolean,
      default: false,
    },
    form: {
      type: Object,
      default: () => {},
    },
    // 是否开启上移下移
    isOpenSort: {
      type: Boolean,
      default: () => false,
    },
    // 子表数据
    subData: {
      type: Array,
      default: () => [],
    },
    // 孙表名称
    grandsonTableName: {
      type: Array,
      default: () => [],
    },
    // 孙表复制需要删除的字段

    // 选中的index
    selectedSonTableIndex: {
      type: Number,
      default: () => -1,
    },
    // 是否显示合计
    showSummary: {
      type: Boolean,
      default: () => false,
    },
    // 参与合计的字段列表
    summaryFieldList: {
      type: Array,
      default: () => [],
    },
    // 是否开启选单按钮
    isOpenDialog: {
      type: Boolean,
      default: () => false,
    },
    // 选单按钮列表
    menuOptions: {
      type: Array,
      default: () => [],
    },
    // 自定义搜索条件
    customizeConditions: {
      type: Array,
      default: () => [],
    },
    // 是否禁用按钮
    disabled: {
      type: Boolean,
      default: () => false,
    },
    // 是否隐藏按钮区域
    hideButtonBox: {
      type: Boolean,
      default: () => false,
    },
    // 需要禁用的menu名称列表
    menuDisabledList: {
      type: Array,
      default: () => [],
    },
    // 开启单选
    isOpenRadio: {
      type: Boolean,
      default: () => false,
    },
    // 复制行需要删除的字段列表
    copyRowDeletedFields: {
      type: Array,
      default: () => [
        "id",
        "isClosed",
        "closedBy",
        "closedTime",
        "isCancel",
        "cancelBy",
        "cancelTime",
      ],
    },
    // 开启关闭取消按钮
    openCancelClosedBtn: {
      type: Boolean,
      default: () => false,
    },
    // 禁用按钮
    // []
    disabledButtonList: {
      type: Array,
      default: () => [],
    },
    // 是否开启复制按钮
    openCopyBtn: {
      type: Boolean,
      default: true,
    },
    // 是否开启批量填充按钮
    openFillingBtn: {
      type: Boolean,
      default: false,
    },
    // 初始化对象
    initObj: {
      type: Object,
      default: () => ({}),
    },
    // 可配置可批量填充的变量
    relationshipsLis: {
      type: Object,
      default: () => relationshipsList,
    },
    // 删除行前校验函数
    validateBeforeDeleteFn: {
     type: Function,
     default: null,
    },
  },
  mounted() {
    this.$nextTick(() => {
      if (this.$refs.subTable) {
        this.$refs.subTable.$el.addEventListener("keydown", this.handleKeyDown);
      }
    });
  },
  beforeDestroy() {
    if (this.$refs.subTable) {
      this.$refs.subTable.$el.removeEventListener(
        "keydown",
        this.handleKeyDown
      );
    }
  },
  beforeUpdate() {
    if (this.$refs.subTable) {
    }
  },
  data() {
    return {
      _selectedSonTableIndex: this.selectedSonTableIndex,
      multipleSelection: [], //多选框选中的数据
      // 弹窗是否显示
      dialogVisible: false,
      // 单选框的值
      selectedRadio: "",
      functionName: "",
      activeHeader: "",
      // 可拖拽高度
      tableHeight: 260, // 初始高度
      isResizing: false,
      startY: 0,
      startHeight: 0,
      isExpanded: false,

      numTotal:0,
    };
  },
  created() {
    this.functionName = this.$route.query.functionName;
  },
  computed: {
    isPass() {
      return this.$store.state.form.currentStatus?.status == "PASS";
    },
    isOpenCancelBtn() {
      return this.openCancelClosedBtn;
    },
    isOpenClosedBtn() {
      return this.openCancelClosedBtn;
    },
    hiddenButtonList() {
      return this.$store.state.form.hiddenButtonList;
    },
  },
  watch: {
    subData: {
      handler: function () {
        // console.log("subData", this.$refs.subTable);
      },
    },
  },
  methods: {
    toggleExpand() {
      this.isExpanded = !this.isExpanded;
      if (this.isExpanded) {
        this.$nextTick(() => {
          this.calculateTableHeight();
          this.topScrollClick();
        });
      } else {
        // 收起时恢复默认高度
        this.tableHeight = 260;
      }
    },
    calculateTableHeight() {
      const wrapper = this.$refs.subTable.$el.querySelector(
        ".el-table__body-wrapper"
      );
      if (!wrapper) return;

      const rows = wrapper.querySelectorAll(".el-table__row");
      const header = wrapper.previousElementSibling;
      const computedStyle = window.getComputedStyle(wrapper);

      // 计算总高度（包含表头、边框、padding等）
      const totalHeight =
        Array.from(rows).reduce((acc, row) => acc + row.offsetHeight, 0) +
        (header ? header.offsetHeight : 0) +
        parseInt(computedStyle.paddingTop) +
        parseInt(computedStyle.paddingBottom) +
        parseInt(computedStyle.borderTopWidth) +
        parseInt(computedStyle.borderBottomWidth) +
        80; // 预留安全间距

      // 设置最小高度防止异常
      this.tableHeight = Math.max(totalHeight, 180);
    },
    handleCellClick(column) {
      if (
        !this.openFillingBtn ||
        this.disabled ||
        !this.relationshipsLis[column.property]
      ) {
        return;
      }
      this.activeHeader = column.property;
    },
    // 新增动态表头类名方法
    getHeaderCellClassName({ column }) {
      return column.property === this.activeHeader ? "highlight-header" : "";
    },
    // 根据传入的name，判断按钮是否显示
    buttonIsDisabled(name) {
      return this.disabled || this.disabledButtonList.includes(name);
    },
    // 复选框选中
    handleSelectionChange(val) {
      this.multipleSelection = val;
      this.$store.commit("form/CHANGE_SUBTABLESELECTION", val);
      //向父组件传选中的值
      this.$emit("closeMain", val);
    },
    // 修改index
    setSelectedIndex(index) {
      this._selectedSonTableIndex = index;
      // console.log("_selectedSonTableIndex", this._selectedSonTableIndex);
      this.$emit("update:selectedSonTableIndex", this._selectedSonTableIndex);
    },
    handleDelete() {
      // 删除前校验
       if (this.validateBeforeDeleteFn) {
        if(!this.validateBeforeDeleteFn(this.multipleSelection)){
          return
        }
      }
      console.log(this._selectedSonTableIndex);
      this.$confirm("是否确认移除所选数据!", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          //遍历获得多选选中的index值
          this.multipleSelection.forEach((value, index) => {
            //遍历多选框获取的数据
            this.subData.forEach((v, i) => {
              //遍历数据表，任意一个属性值相同，则说明该数据被选中，其i则为索引值
              if (value.sortIndex == v.sortIndex) {
                this.$emit("deleteRowHandler", this.subData[i], i);
                this.subData.splice(i, 1);
                this.setFocusRow();
              }
            });
          });
          this.$message({
            type: "success",
            message: "移除成功!",
          });
          // 移除成功后emit
          this.$emit("deleted-handler");
          console.log("移除成功");
        })
        .catch(() => {
          this.$message({
            type: "info",
            message: "已取消移除",
          });
        });
      // this.subData.splice(this._selectedSonTableIndex, 1);
    },
    // 删除子表行
    removeRow(index) {
      if (index != null) {
        this.subData.splice(index, 1);
      } else {
        this.subData.splice(0, this.subData.length);
      }
      this.setFocusRow();
    },
    // 设置焦点行
    setFocusRow() {
      this.$nextTick(() => {
        if (this.subData?.length <= 0) {
          // 没有数据，没有焦点行
          this.setSelectedIndex(-1);
        } else {
          // 选中第一行
          this.handleRowClick(this.subData[0]);
          this.topScrollClick();
        }
      });
    },
    handleCurrentChange(val) {},
    //上移
    moveUp() {
      if (this.multipleSelection.length === 0) return;
      // 获取选中行的索引
      const selectedIndexes = this.multipleSelection.map((row) =>
        this.subData.indexOf(row)
      );
      selectedIndexes.sort((a, b) => a - b); // 从小到大排序

      // 确定目标位置（最小索引的位置的上面）
      const targetIndex = selectedIndexes[0] - 1;

      // 移除选中的行
      const rowsToMove = [];
      for (let i = selectedIndexes.length - 1; i >= 0; i--) {
        const index = selectedIndexes[i];
        rowsToMove.unshift(this.subData.splice(index, 1)[0]); // 从后到前移除
      }

      // 插入行到目标位置
      const insertIndex = Math.max(targetIndex, 0);
      this.subData.splice(insertIndex, 0, ...rowsToMove);
      this.setSelectedIndex(insertIndex);
    },
    // moveUp() {
    //   const index = this._selectedSonTableIndex;
    //   // console.log("上移", index);
    //   if (index > 0) {
    //     let upDate = this.subData[index - 1];
    //     this.subData.splice(index - 1, 1);
    //     this.subData.splice(index, 0, upDate);
    //     this.setSelectedIndex(this._selectedSonTableIndex - 1);
    //   } else {
    //     this.$message.error("已经是第一条，不可上移");
    //   }
    // },
    //下移
    moveDown() {
      if (this.multipleSelection.length === 0) return;
      // 获取选中行的索引
      const selectedIndexes = this.multipleSelection.map((row) =>
        this.subData.indexOf(row)
      );
      selectedIndexes.sort((a, b) => a - b); // 从小到大排序

      let targetIndex;
      if (selectedIndexes.length == 1) {
        targetIndex = selectedIndexes[0] + 1;
      } else {
        targetIndex = Math.min(
          selectedIndexes[selectedIndexes.length - 1],
          this.subData.length
        );
      }
      // 确定目标位置（最小索引的位置的上面）

      // const targetIndex = selectedIndexes[0] - 1;

      // 移除选中的行
      const rowsToMove = [];
      for (let i = selectedIndexes.length - 1; i >= 0; i--) {
        const index = selectedIndexes[i];
        rowsToMove.unshift(this.subData.splice(index, 1)[0]); // 从后到前移除
      }

      // 插入行到目标位置
      const insertIndex = Math.max(targetIndex, 0);
      this.subData.splice(insertIndex, 0, ...rowsToMove);
      this.setSelectedIndex(insertIndex);
    },
    // moveDown() {
    //   const index = this._selectedSonTableIndex;
    //   console.log("下移", index);
    //   if (index + 1 === this.subData.length) {
    //     this.$message.error("已经是最后一条，不可下移");
    //   } else {
    //     console.log(index);
    //     // 保存下一条数据
    //     let downDate = this.subData[index + 1];
    //     // 移除下一条数据
    //     this.subData.splice(index + 1, 1);
    //     // 增添被移除的那一条数据
    //     this.subData.splice(index, 0, downDate);
    //     this.setSelectedIndex(this._selectedSonTableIndex + 1);
    //   }
    // },
    // 通过这个方法，给每一个sonTable添加一个index
    tableRowClassName({ row, rowIndex }) {
      // 给每一个sonTable添加一个index属性
      this.$set(row, "sortIndex", rowIndex + 1);

      // 初始化样式类名
      let className = "";

      // 检查是否选中当前行
      if (this._selectedSonTableIndex === rowIndex) {
        className = "selected-row-color";

        // 根据特定条件添加额外样式
        if (row?.isCancel === 1) {
          className += " cancel-row-color";
        } else if (row?.isClosed === 1 || row?.isClosed === 2) {
          className += " closed-row-color";
        } else if (row?.isNewRow === 1) {
          className += " new-row-color";
        }
      } else if (this.multipleSelection.includes(row)) {
        // 如果在多选框选中列表中，添加"selected-all-row-color"并根据特定条件添加额外样式
        className = "selected-all-row-color";

        if (row?.isCancel === 1) {
          className += " cancel-row-color";
        } else if (row?.isClosed === 1 || row?.isClosed === 2) {
          className += " closed-row-color";
        } else if (row?.isNewRow === 1) {
          className += " new-row-color";
        }
      } else {
        // 如果未在多选框选中列表中，根据特定条件添加样式
        if (row?.isCancel === 1) {
          className = "cancel-row-color";
        } else if (row?.isClosed === 1 || row?.isClosed === 2) {
          className = "closed-row-color";
        } else if (row?.isNewRow === 1) {
          className = "new-row-color";
        }
      }

      // 返回最终的样式类名
      return className;
    },
    // 单击行事件
    handleRowClick(row, column) {
      console.log("handleRowClick", row, column, event, row.sortIndex);
      this.setSelectedIndex(row.sortIndex - 1);
      if (!this.multipleSelection.includes(row)) {
        this.$refs.subTable.toggleRowSelection(row);
      }
      const result = this.multipleSelection.filter(
        (item) => item.sortIndex != row.sortIndex
      );
      this.toggleSelection(result);

      // 专为锁库申设置，告知当前选中行的index
      this.$emit("informIndex", row.sortIndex);
      // this.$refs.subTable.toggleRowSelection(row);
      this.$emit("closeMain", row);
    },
    // 添加数据
    addRow() {
      // console.log("subData", this.subData);
      if (!this.subData) {
        this.subData = [];
      }
      this.$set(this.subData, this.subData.length, cloneDeep(this.initObj));
      this.$emit("update:subData", this.subData);
      this.$nextTick(() => {
        this.bottomScrollClick();
        this.selectedAddRow();
        // 清空校验
        this.clearValidate();
        this.$refs.subTable?.doLayout();
        this.calculateTableHeight();
      });

      // 清空每次的查询条件
      this.$emit("emptyStockQuery");
    },
    // 选中添加行
    selectedAddRow() {
      console.log("selectedAddRow");
      this.handleRowClick(this.subData[this.subData.length - 1]);
    },
    // 通过index选中行
    selectedRow(index) {
      this.handleRowClick(this.subData[index]);
    },
    // 这个方法会遍历数组中的每个对象，如果对象的键值对数量为1且键名为'sortIndex'，那么就将其从数组中移除
    removeEmptyObject(arr) {
      for (let i = 0; i < arr.length; i++) {
        if (
          Object.keys(arr[i]).length === 1 &&
          arr[i].hasOwnProperty("sortIndex")
        ) {
          arr.splice(i, 1);
          i--;
        }
      }
      return arr;
    },
    // 计算合计
    getSummaries(param) {
      const { columns, data } = param;
      const sums = [];
      columns.forEach((column, index) => {
        if (index === 0) {
          sums[index] = "合计";
          return;
        }
        // 计算元素是否参与合计
        let isSummary = false;
        for (let i = 0; i < this.summaryFieldList.length; i++) {
          if (column.property == this.summaryFieldList[i]) {
            isSummary = true;
            break;
          }
        }
        if (isSummary) {
          const values = data.map((item) => Number(item[column.property]));
          if (!values.every((value) => isNaN(value))) {
            sums[index] = values.reduce((prev, curr) => {
              const value = Number(curr);
              if (!isNaN(value)) {
                // 处理合计字段精度问题
                return parseFloat((prev + value).toFixed(2));
              } else {
                return prev;
              }
            }, 0);
            sums[index];
          } else {
            sums[index] = "N/A";
          }
        } else {
          sums[index] = "";
        }
      });
      this.$nextTick(() => {
        if (this.$refs.subTable) {
          this.$refs.subTable.doLayout();
        }
      });

      this.numTotal = sums[4];
      this.$emit('total-change',this.numTotal);




      return sums;
    },



    // 单选框确认
    radioConfirm() {
      this.dialogVisible = false;
    },
    // 获取弹窗数据
    dataChangeForDialog(details, functionName) {
      this.$emit("data-change-for-dialog", details, functionName);
      console.log("setFocusRow");
      this.$nextTick(() => {
        // 清空校验
        this.clearValidate();
        this.$refs.subTable?.doLayout();
        this.calculateTableHeight();
      });
      // setTimeout(() => {
      //   this.handleRowClick(this.subData[this.subData.length - 1]);
      // }, 0);
    },
    // 全选
    checkAll() {
      const result = this.subData.filter(
        (item) =>
          !this.multipleSelection.some(
            (sel) => sel.sortIndex === item.sortIndex
          )
      );
      this.toggleSelection(result);
    },
    // 反选
    reverseElection() {
      // this.toggleSelection(this.subData);
      this.$refs.subTable.clearSelection();
    },
    // 选中方法
    toggleSelection(rows) {
      if (rows) {
        rows.forEach((row) => {
          this.$refs.subTable.toggleRowSelection(row);
        });
      } else {
        this.$refs.subTable.clearSelection();
      }
    },
    // 表格滚动到顶部
    topScrollClick() {
      this.$nextTick(() => {
        const wrapper = this.$refs.subTable.$el.querySelector(
          ".el-table__body-wrapper"
        );
        if (wrapper) {
          wrapper.scrollTop = 0;
        }
      });
    },
    //  表格滚动到底部
    bottomScrollClick() {
      this.$nextTick(() => {
        const wrapper = this.$refs.subTable.$el.querySelector(
          ".el-table__body-wrapper"
        );
        if (wrapper) {
          wrapper.scrollTop = wrapper.scrollHeight;
        }
      });
    },
    // 批量填充
    fillingRowHandle() {
      if (!this.activeHeader) {
        this.$message.warning("请先点击要填充的列头");
        return;
      }
      if (!this.relationshipsLis[this.activeHeader]) {
        this.$message.warning("当前列不支持批量填充");
        return;
      }
      let sourceRow = {};
      this.subData.forEach((row, index) => {
        if (index == 0) {
          sourceRow = { ...row };
        } else if (this.activeHeader == "matId") {
          // 物料
          sourceRow.code = sourceRow.matCode;
          sourceRow.cnName = sourceRow.matName;
          sourceRow.saleRate = sourceRow.salRate;
          this.$set(row, this.activeHeader, sourceRow[this.activeHeader]);
          this.setMatIdHandler(row, sourceRow);
        } else if (this.activeHeader == "rawMatId") {
          // 原料
          sourceRow.name = sourceRow.rawMaterialName;
          sourceRow.code = sourceRow.rawMaterialCode;
          this.$set(row, this.activeHeader, sourceRow[this.activeHeader]);
          this.setRawMatHandler(row, sourceRow);
        } else if (
          [
            "dyeMethod",
            "lineNum",
            "qty",
            "price",
            "amount",
            "checkPrice",
            "tpFee",
            "paFee",
            "paRate",
            "raFee",
            "cFee",
            "otherFee",
            "spPrice",
            "costPrice",
            "yarnUse",
            "weightType",
            "packType",
            "cnyRate",
            "devType",
            "isFree",
            "isCb",
            "expFee",
            "busFee",
            "seaFee",
            "airFee",
            "addCostPrice",
            "totalCostPrice",
          ].includes(this.activeHeader)
        ) {
          this.$set(row, this.activeHeader, sourceRow[this.activeHeader]);
          this.subTableWatch(row, this.activeHeader);
        } else if (this.activeHeader == "printColorId") {
          // 打版色号
          this.$set(row, "printColorName", sourceRow["printColorName"]);
          this.$set(row, this.activeHeader, sourceRow[this.activeHeader]);
        } else if (this.activeHeader == "compColorId") {
          // 公司色号compColorName
          this.$set(row, "compColorName", sourceRow["compColorName"]);
          this.$set(row, this.activeHeader, sourceRow[this.activeHeader]);
        } else {
          this.$set(row, this.activeHeader, sourceRow[this.activeHeader]);
        }
      });
      this.$message.success("批量填充完成");
    },
    // 复制行
    copyRowHandle(copyData) {
      if (!copyData) {
        copyData = this.multipleSelection;
      }
      console.log("copyData", copyData);
      // 使用 map 创建选中行的副本并排除指定字段
      const rowsToCopy = copyData.map((row) => {
        const newRow = cloneDeep(row);
        this.copyRowDeletedFields.forEach((field) => {
          delete newRow[field];
        });
        // 复制处理孙表数据
        if (this.grandsonTableName && this.grandsonTableName.length > 0) {
          this.grandsonTableName.forEach((tableName) => {
            if (
              newRow[tableName.name] &&
              Array.isArray(newRow[tableName.name])
            ) {
              // 创建新的孙表数组
              let newTable = [];
              newRow[tableName.name].forEach((childRow) => {
                let newChildRow = cloneDeep(childRow);
                // 删除孙表字段
                tableName.copyDelFields.forEach((field) => {
                  delete newChildRow[field];
                });
                newTable.push(newChildRow);
              });
              newRow[tableName.name] = newTable;
            }
          });
        }
        newRow.pushedCount = 0;
        return newRow;
      });
      // 在指定位置插入复制的行

      this.subData.splice(this.subData.length, 0, ...rowsToCopy);
      this.$nextTick(() => {
        this.selectedAddRow();

        // 新增：复制行后自动计算高度
        this.calculateTableHeight();
      });
      return this.subData[this.subData.length - 1];
    },
    // 修改事件处理方法
    startResize(e) {
      this.isResizing = true;
      this.startY = e.clientY;
      this.startHeight = this.tableHeight;
      document.body.classList.add("resizing-active");
      document.addEventListener("mousemove", this.handleResize);
      document.addEventListener("mouseup", this.stopResize);
      e.preventDefault();
    },
    handleResize(e) {
      if (!this.isResizing) return;
      const deltaY = e.clientY - this.startY;
      // 正确计算高度变化：向下拖拽增加高度，向上拖拽减少
      this.tableHeight = Math.max(180, this.startHeight + deltaY);
      e.preventDefault();
    },
    stopResize() {
      this.isResizing = false;
      document.body.classList.remove("resizing-active");
      document.removeEventListener("mousemove", this.handleResize);
      document.removeEventListener("mouseup", this.stopResize);
    },
  },
};
</script>
<style scoped>
.mr-5 {
  margin-right: 5px;
}
::v-deep .el-table__body tr.current-row > td.el-table__cell {
  background: #7794f4 !important;
}
::v-deep .sub-table .el-input__inner {
  border: 0;
  border-radius: 0px;
  padding: 0px 5px;
  height: 40px;
}
::v-deep .sub-table .is-error .el-input__inner {
  border-color: #ffefef;
  background-color: #ffefef;
}
::v-deep .sub-table .is-error .el-form-item__error {
  background: white;
}
::v-deep .sub-table .is-disabled .el-input__inner {
  /* 事件穿透 */
  pointer-events: none;
}
::v-deep .sub-table .el-form-item__content {
  margin-left: 0px !important;
}
::v-deep .sub-table .is-error .el-form-item__content {
  margin-bottom: 12px !important;
}
::v-deep .el-table .cell {
  padding: 0px !important;
}
::v-deep .el-table__cell {
  text-align: center;
}
::v-deep .sub-table .el-select {
  width: 100%;
}
::v-deep .sub-table .el-select .is-disabled {
  /* 事件穿透 */
  pointer-events: none;
}
/* 禁用行的CSS样式 */
.disable-row {
  pointer-events: none;
  opacity: 0.5;
  cursor: default;
}
::v-deep .el-table th.el-table__cell > .cell {
  white-space: nowrap;
  text-align: center;
}

::v-deep .el-table__footer-wrapper tbody td.el-table__cell {
  height: 20px;
  background: white;
}
::v-deep .el-table .el-table__cell.is-leaf {
  text-align: right;
  padding: 0px 5px;
}
::v-deep .el-table .el-table__cell.is-center {
  text-align: center;
}
::v-deep .el-table .el-table__cell.is-center .cell {
  text-align: center;
}
/* 为.sub-table .el-table__body-wrapper添加滚动条样式 */
::v-deep .sub-table .el-table__body-wrapper::-webkit-scrollbar {
  width: 20px !important; /*滚动条宽度*/
  height: 20px !important; /*滚动条高度*/
  background: transparent;
}
.button-box {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  flex-wrap: wrap;
}
::v-deep .sub-table .el-table__body-wrapper {
  min-height: 120px;
}
/* 新增表头高亮样式 */
::v-deep .el-table .highlight-header {
  background-color: #7794f4 !important;
  font-weight: bold;
}

.resizable-table-container {
  position: relative;
  margin-top: 5px;
}
.drag-handle {
  position: relative;
  height: 7px;
  cursor: row-resize;
  background-color: transparent;
  margin-top: 5px; /* 可选的间距 */
}
.drag-handle:hover {
  background-color: #409eff;
}
/* 拖拽时的样式 */
body.resizing-active {
  cursor: row-resize;
  user-select: none;
}
</style>
