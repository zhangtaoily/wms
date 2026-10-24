<template>
  <div>
    <!-- el-input不支持鼠标双击事件，改为用div来接收该事件 -->
    <el-popover
      ref="popover"
      placement="bottom"
      width="500"
      v-model="isOpenSimpleQueryDialog"
      popper-class="simple-query-popover"
      style="padding: 0px !important"
      @show="popoverShow"
      trigger="manual"
    >
      <!-- 简单查询弹出框 -->
      <SimpleQueryDialog
        v-if="isOpenSimpleQueryDialog"
        :functionName="treeFunctionName"
        :inputValue="input"
        :openRadioSelection="openRadioSelection"
        openMode="dialog"
        @handle-dialog-submit="handleSubmit"
        :dialogName="treeFunctionName"
        :customizeConditions="finallyConditions"
      />
      <div
        @click="inputChangeHandler"
        slot="reference"
        @dblclick="inputFocusHandler"
      >
        <el-input
          :disabled="disabled"
          :placeholder="disabled || readonly ? '' : '请选择'"
          v-model="input"
          clearable
          @clear="clear"
          @focus="handleFocus"
          @blur="handleBlur"
        >
          <i
            v-if="!disabled"
            slot="suffix"
            class="el-icon-search"
            @click.stop="inputFocusHandler"
          ></i>
        </el-input>
      </div>
    </el-popover>

    <el-dialog
      style="overflow: hidden"
      :title="'请选择' + title"
      :visible.sync="dialogVisible"
      width="40%"
    >
      <DynamicTreeTest
        :key="dynamicListKey"
        ref="dynamicList"
        :handleCustomDelete="handleCustomDelete"
        :openRadioSelection="openRadioSelection"
        :functionName="functionName"
        openMode="dialog"
        @handle-dialog-submit="handleSubmit"
        :customizeConditions="finallyConditions"
        :isTree="isTree"
        @submit="submit"
        :treeFunctionName="treeFunctionName"
        :isDoubleClick="isDoubleClick"
      >
      </DynamicTreeTest>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button
          type="primary"
          v-if="!isDoubleClick"
          :disabled="disabled"
          @click="submit"
          >确 定</el-button
        >
      </span>
    </el-dialog>
  </div>
</template>

<script>
// 导入动态列表组件
import DynamicTreeTest from "@/components/DynamicTreeTest/index.vue";
import { getDetails } from "@/api/system/common";
import SimpleQueryDialog from "@/components/DynamicDialog/SimpleQueryDialog";
import _ from "lodash";
export default {
  name: "DynamicDialog",
  components: {
    DynamicTreeTest,
    SimpleQueryDialog,
  },
  props: {
    // 开启双击保存
    isDoubleClick: {
      type: Boolean,
      default: () => false,
    },
    // 保存的行数据
    rowData: {
      type: Object,
      default: () => ({}),
    },
    // 标题
    title: {
      type: String,
      required: true,
    },
    // 模块名称
    functionName: {
      type: String,
      required: true,
    },
    // 输入框回显的字段
    echoField: {
      type: String,
      default: () => "code",
    },
    // 开启单选功能
    openRadioSelection: {
      type: Boolean,
      default: true,
    },
    // v-model值
    value: [Array, Object, String],
    initValue: {
      type: String,
      default: () => null,
    },
    isReturnId: {
      type: Boolean,
      default: () => true,
    },
    // 返回字段，默认返回id
    returnField: {
      type: String,
      default: () => "id",
    },
    // 自定义搜索条件
    customizeConditions: {
      type: Array,
      default: () => [],
    },
    // 是否开启树形结构
    isTree: {
      type: Boolean,
      default: () => false,
    },
    // 树形结构的模块名
    treeFunctionName: {
      type: String,
    },
    // disabled
    disabled: {
      type: Boolean,
      default: () => false,
    },
    // readonly
    readonly: {
      type: Boolean,
      default: () => false,
    },
    // 简单模式：自动发送请求，默认关闭
    simpleMode: {
      type: Boolean,
      default: () => false,
    },
    // 多选默认返回值为字符串
    returnString: {
      type: Boolean,
      default: () => true,
    },
    // 开启默认查询
    isOpenDefaultQuery: {
      type: Boolean,
      default: () => true,
    },
    // 获取查询条件的回调函数
    conditionsCallBack: {
      type: Function,
      default: () => [],
    },
    // 打开弹窗前校验
    beforeOpenCheck: {
      type: Function,
      default: () => [],
    },
  },
  created() {},
  watch: {
    value: {
      handler: function (newVal, oldVal) {
        console.log("data-change  swatch newval", newVal);
        // if (!newVal) {
        //   // 不存在id，需要清空回显值
        //   this.changeEchoValue("");
        // }
        // // 开启简单模式自动发送数据
        // const details = this.simpleMode ? this.dataChange(newVal) : null;
        // this.$emit(
        //   "data-change",
        //   this.changeEchoValue,
        //   newVal,
        //   this.rowData,
        //   details
        // );
      },
      immediate: true,
    },
    isOpenSimpleQueryDialog: {
      handler: _.debounce(function (newVal, oldVal) {
        if (!newVal) {
          // 判断是否选择了数据
          if (!this.value) {
            // 没有选择数据就删除输入的数据
            // this.$message.error("请选择一条数据");
            this.input = null;
          }
        }
      }, 300),
    },
    // 每次打开弹窗，重新发送请求
    dialogVisible: {
      handler: function (newVal, oldVal) {
        if (newVal) {
          this.dynamicListKey = Date.now();
        }
      },
    },
    // 监听初始化的值
    initValue: {
      handler: function (newVal, oldVal) {
        console.log("initValue", this.initValue);
        this.input = newVal;
      },
      immediate: true,
    },
    // 值发生变化
    input: {
      handler: function (newVal, oldVal) {
        this.inputChangeHandler(newVal);
      },
      immediate: true,
    },
  },
  computed: {
    finallyConditions() {
      if (this.isOpenDefaultQuery) {
        return [
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
          ...this.customizeConditions,
        ];
      } else {
        return [...this.customizeConditions];
      }
    },
  },
  data() {
    return {
      dialogVisible: false,
      input: "",
      // 动态列表的key
      dynamicListKey: Date.now(),
      // 是否打开简单查询弹窗
      isOpenSimpleQueryDialog: false,
      // 记录输入框的聚焦状态
      isFocused: false,
    };
  },
  methods: {
    // input发生改变
    inputChangeHandler(value) {
      // 只有聚焦了才触发
      if (this.isFocused) {
        console.log("点击了input", value, this.isOpenSimpleQueryDialog);
        this.isOpenSimpleQueryDialog = true;
      }
    },
    // input focus
    inputFocusHandler() {
      if (!this.readonly) {
        this.dialogVisible = true;
      }
    },
    clear() {
      this.$emit("input", "");
      this.$emit("data-change", this.changeEchoValue, "", this.rowData, null);
    },
    handleCustomDelete(ids) {
      this.$message.success("自定义删除按钮" + ids);
    },
    handleCustomUpdate(id) {
      this.$message.success("自定义修改按钮" + id);
    },

    // 提交按钮方法
    submit() {
      console.log("确定");
      if (this.openRadioSelection) {
        console.log("submit");
        this.$refs.dynamicList.handleRadioDataSubmit();
      } else {
        this.$refs.dynamicList.handleMultiDataSubmit();
      }
    },
    // 处理选择的数据
    handleSubmit(data) {
      console.log("handle-dialog-submit", data);
      this.isOpenSimpleQueryDialog = false;
      if (this.openRadioSelection) {
        this.radioSubmit(data);
      } else {
        this.multiSubmit(data);
      }
    },
    // 单选处理逻辑
    radioSubmit(data) {
      // this.input = data[this.echoField];
      this.dialogVisible = false;
      this.updateValue(data);
    },
    // 多选处理逻辑
    multiSubmit(data) {
      this.input = data.map((item) => item[this.echoField]).join(",");
      this.dialogVisible = false;
      this.updateValue(data);
    },
    /**
     * 简单查询弹窗弹出时校验
     */
    async popoverShow() {
      // input为null时不显示
      if (!this.input) {
        this.isOpenSimpleQueryDialog = false;
      }
      // 调用新方法处理弹窗打开前的逻辑
      const canOpenDialog = await this.checkAndFetchConditions();
      if (!canOpenDialog) {
        this.isOpenSimpleQueryDialog = false;
      }
    },
    // 新方法：校验并获取条件
    async checkAndFetchConditions() {
      // 打开弹窗前校验，是否可以打开弹窗
      if (!this.beforeOpenCheck()) {
        return false;
      }
      // 运行过滤条件的方法，获取查询条件
      this.cbList = await this.conditionsCallBack(this.rowData);
      console.log("cbList", this.cbList);
      return true;
    },
    // 输入框聚焦时触发
    handleFocus() {
      this.isFocused = true;
    },
    // 输入框失焦时触发
    handleBlur(e) {
      this.isFocused = false;
      console.log("handleBlur", this.isFocused, this.initValue, this.input);
      if (this.input == null || this.input == "") {
        this.clear();
      } else if (this.value && this.initValue) {
        this.input = this.initValue;
      }
    },
    // 更新value
    updateValue(value) {
      let ids;
      console.log("value ----------------------:>> ", value);
      if (this.openRadioSelection) {
        if (this.isReturnId) {
          ids = value[this.returnField];
          this.$emit("input", value[this.returnField]);
        } else {
          this.$emit("input", value);
        }
      } else {
        if (this.isReturnId) {
          ids = value.map((item) => item[this.returnField]);
          // this.$emit("input", ids.join(","));
          this.$emit("input", this.returnString ? ids.join(",") : ids);
        } else {
          this.$emit("input", value);
        }
      }
      console.log("ids", ids);
      // 开启简单模式自动发送数据
      const details = this.simpleMode ? this.dataChange(ids) : value;
      console.log("details", details, value);
      this.$emit(
        "data-change",
        this.changeEchoValue,
        ids,
        this.rowData,
        details,
        true
      );
    },
    // 改变显示的值
    changeEchoValue(val) {
      this.input = val;
    },
    // 获取详情数据并且回显数据
    async dataChange(id) {
      if (id) {
        const res = await getDetails(this.functionName, id);
        const { data } = res;
        this.changeEchoValue(data[this.echoField]);
        return data;
      } else {
        return "";
      }
    },
    /**
     * 要实现点击外部关闭弹窗的功能，
     * @param event event
     */
    handleClickOutside(event) {
      const popover = this.$refs.popover.$el;
      if (popover && !popover.contains(event.target)) {
        this.isOpenSimpleQueryDialog = false;
      }
    },
    // 手动触发 data-change
    manualTriggerDataChange(newVal) {
      if (!newVal) {
        // 不存在id，需要清空回显值
        this.changeEchoValue("");
      }
      // 开启简单模式自动发送数据
      const details = this.simpleMode ? this.dataChange(newVal) : null;
      this.$emit(
        "data-change",
        this.changeEchoValue,
        newVal,
        this.rowData,
        details
      );
    },
  },
  mounted() {
    document.addEventListener("click", this.handleClickOutside);
  },
  beforeDestroy() {
    document.removeEventListener("click", this.handleClickOutside);
  },
};
</script>

<style lang="scss" scoped>
::v-deep .el-dialog__header {
  padding: 0px 10px;
  padding-top: 10px;
  margin-bottom: -10px;
  text-align: left;
}
::v-deep .el-dialog__body {
  margin: 0px !important;
  padding: 0px !important;
}
::v-deep .el-dialog:not(.is-fullscreen) {
  margin-top: 0 !important;
}
.el-input__inner,
.el-textarea__inner {
  border: 1px !important;
}
</style>
