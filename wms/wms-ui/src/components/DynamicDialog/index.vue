<template>
  <div>
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
        :functionName="functionName"
        :inputValue="input"
        :openRadioSelection="openRadioSelection"
        openMode="dialog"
        @handle-dialog-submit="handleSubmit"
        :dialogName="dialogName"
        :customizeConditions="finallyConditions"
      />
      <!-- el-input不支持鼠标双击事件，改为用div来接收该事件 -->
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
          :readonly="isReadOnly"
          @focus="handleFocus"
          @blur="handleBlur"
          class="query-input"
        >
          <i
            v-if="!disabled"
            slot="suffix"
            class="el-icon-search"
            :style="{ background: disabled ? 'transparent' : '#fff' }"
            @click.stop="inputFocusHandler"
          ></i>
        </el-input>
      </div>
    </el-popover>


    <el-dialog
      style="overflow: hidden"
      :title="'请选择' + title"
      :visible.sync="dialogVisible"
      width="80%"
      :modal="modal"
      modal-append-to-body
      append-to-body
      show-close
      v-dialogDrag
      v-dialogDragWidth
      v-dialogDragHeight
    >

      <DynamicList
        :key="dynamicListKey"
        ref="dynamicList"
        :handleCustomDelete="handleCustomDelete"
        :openRadioSelection="openRadioSelection"
        :functionName="functionName"
        :dialogName="dialogName"
        openMode="dialog"
        @handle-dialog-submit="handleSubmit"
        :customizeConditions="finallyConditions"
        :validateRadioSelection="validateRadioSelection"
        :isTree="isTree"
        :treeFunctionName="treeFunctionName"
        :treePath="treePath"
        :foreignKey="foreignKey"
        :treeLikeFieldArray="treeLikeFieldArray"
        :isSecondaryDialog="isSecondaryDialog"
        :reserveSelection="reserveSelection"
        :echoRowDataList="echoRowDataList"
        :showHeaderFilter="true"
        :needCompareKeys="needCompareKeys"
        @setBtnDisable="setBtnDisable"
      >
      </DynamicList>



      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button
          type="primary"
          :disabled="disabled || uplookDisabled"
          @click="submit"
          >确 定</el-button
        >
      </span>
    </el-dialog>



  </div>
</template>




<script>
// 导入动态列表组件
import DynamicList from "@/components/DynamicList/index.vue";
import { getDetails } from "@/api/system/common";
import SimpleQueryDialog from "./SimpleQueryDialog";
import _ from "lodash";
export default {
  name: "DynamicDialog",
  components: {
    DynamicList,
    SimpleQueryDialog,
  },
  props: {
    isSecondaryDialog: {
      type: Boolean,
      default: false,
    },
    // 保存的行数据
    parentRowData: {
      type: Object,
      default: () => ({}),
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
    // 弹窗名称
    dialogName: {
      type: String,
      default: () => null,
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
    value: [Array, Object, String, Number],
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
    treePath: {
      type: String,
      default: () => "",
    },
    // 打开数据列表时 已选择的数据设置为勾选状态
    reserveSelection: {
      type: Boolean,
      default: () => false,
    },
    // 回显的数据
    echoRowDataList: {
      type: [Array, String],
      default: () => [],
    },
    // 获取查询条件的回调函数
    conditionsCallBack: {
      type: Function,
      default: () => [],
    },
    // el-input框是否只读
    isReadOnly: {
      type: Boolean,
      default: () => false,
    },
    // 打开弹窗前校验
    beforeOpenCheck: {
      type: Function,
      default: () => [],
    },
    // 是否需要遮罩层
    modal: {
      type: Boolean,
      default: () => true,
    },
    // 上选 多选时,需要数据中的key对应的值一致才能选择
    needCompareKeys: {
      type: Array,
      default: () => [],
    },
    // 树形模糊查询的字段
    // [{'key':'code','value':'code'}]
    treeLikeFieldArray: {
      type: Array,
      default: () => null,
    },
    // 外键字段（列表里面，关联树的id）
    foreignKey: { type: String, default: () => "parent_id" },
    // 单选弹框保存前的校验
    validateRadioSelection: {
      type: Function,
      default: null,
    },
  },
  created() {},
  watch: {
    value: {
      handler: function (newVal, oldVal) {},
      immediate: true,
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
        // console.log("initValue", this.initValue);
        this.input = newVal;
      },
      immediate: true,
    },
    // 值发生变化
    input: {
      handler: function (newVal, oldVal) {
        if (newVal != null && newVal != undefined) {
          // 只去除前面的空白字符
          this.input = newVal.replace(/^\s+/, "");
        }
        this.inputChangeHandler(newVal);
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
          ...this.cbList,
        ];
      } else {
        return [...this.customizeConditions, ...this.cbList];
      }
    },
  },
  data() {
    return {
      dialogVisible: false,
      input: "",
      // 动态列表的key
      dynamicListKey: Date.now(),
      // 存放回调函数返回的查询条件
      cbList: [],
      // 记录输入框的聚焦状态
      isFocused: false,
      // 是否打开简单查询弹窗
      isOpenSimpleQueryDialog: false,
      // 上选 多选禁用
      uplookDisabled: false,
    };
  },
  methods: {
    async inputFocusHandler() {
      // 调用新方法处理弹窗打开前的逻辑
      const canOpenDialog = await this.checkAndFetchConditions();
      if (!canOpenDialog) {
        return;
      }
      // 显示弹窗
      this.dialogVisible = !this.disabled;
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
    clear() {
      this.$emit("input", null);
      this.$emit("data-change", this.changeEchoValue, "", this.rowData, null);
    },
    // handleCustomAdd() {
    //   this.$message.success("自定义添加按钮");
    // },
    handleCustomDelete(ids) {
      this.$message.success("自定义删除按钮" + ids);
    },
    handleCustomUpdate(id) {
      this.$message.success("自定义修改按钮" + id);
    },
    // 提交按钮方法
    submit() {
      if (this.openRadioSelection) {
        // console.log("submit");
        this.$refs.dynamicList.handleRadioDataSubmit();
      } else {
        this.$refs.dynamicList.handleMultiDataSubmit();
      }
    },
    // 处理选择的数据
    handleSubmit(data) {
      console.log("multiSubmit000", data);
      this.isOpenSimpleQueryDialog = false;
      if (this.openRadioSelection) {
        this.radioSubmit(data);
      } else {
        this.multiSubmit(data);
      }
    },
    // 处理选择的数据
    handleSimpleQuerySubmit(data) {
      this.isOpenSimpleQueryDialog = false;
      this.radioSubmit(data);
    },
    // 单选处理逻辑
    radioSubmit(data) {
      // 单选弹框保存前的校验
      if (this.validateRadioSelection) {
        if (!this.validateRadioSelection(data)) {
          return;
        }
      }
      if (!this.simpleMode) {
        this.input = data[this.echoField];
      }
      this.dialogVisible = false;
      this.updateValue(data, true);
    },
    // 多选处理逻辑
    multiSubmit(data) {
      // 过滤掉 null 和 undefined
      const filteredData = data.filter(
        (item) =>
          item[this.echoField] !== null && item[this.echoField] !== undefined
      );
      // 使用 Set 去重
      const uniqueData = [
        ...new Set(filteredData.map((item) => item[this.echoField])),
      ];
      // 拼接成一个字符串
      this.input = uniqueData.join(",");
      this.dialogVisible = false;
      this.updateValue(data, false);
    },
    /**
     * 更新value
     * @param value 选中的数据
     * @param isRadio 是否为单选
     */
    updateValue(value, isRadio) {
      let ids;
      // console.log("value ----------------------:>> ", value);
      if (isRadio) {
        if (this.isReturnId) {
          console.log(
            "value[this.returnField]",
            value,
            value[this.returnField]
          );
          ids = value[this.returnField];
          this.$emit("input", value[this.returnField]);
        } else {
          this.$emit("input", value);
        }
      } else {
        if (this.isReturnId) {
          ids = value.map((item) => item[this.returnField]);
          // this.$emit("input", ids.join(","));
          // console.log("ids------------ :>> ", ids);
          this.$emit("input", this.returnString ? ids.join(",") : ids);
        } else {
          this.$emit("input", value);
        }
      }

      // console.log("ids", ids);
      // 开启简单模式自动发送数据
      const details = this.simpleMode ? this.dataChange(ids) : value;
      // console.log("details", details, value);
      this.$emit(
        "data-change",
        this.changeEchoValue,
        ids,
        this.rowData,
        details,
        true
      );
      // 如果父组件存在clearValidate方法，则调用 例如 el-form-item
      if (this.$parent?.clearValidate) {
        this.$parent?.clearValidate();
      } else if (this.$refs.popover?.$parent?.$parent?.validate) {
        this.$refs.popover?.$parent?.$parent?.validate();
      }
      // this.$refs.popover?.$parent?.$parent?.validate();
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
    // input发生改变
    inputChangeHandler(value) {
      // 只有聚焦了才触发
      if (this.isFocused) {
        console.log("点击了input", value, this.isOpenSimpleQueryDialog);
        this.isOpenSimpleQueryDialog = true;
      }
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
    // 上选比较选中数据的key 决定是否禁用按钮
    setBtnDisable(flag) {
      this.uplookDisabled = flag;
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
::v-deep .query-input {
  .el-input__suffix {
    // line-height: 45px;
    // top:25%;
  }
}
</style>
