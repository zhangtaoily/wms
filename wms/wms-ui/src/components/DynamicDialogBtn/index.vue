<template>
  <div class="el-button" style="border: 0px; padding: 0px">
    <template v-if="!isImg">
      <el-dropdown
        :disabled="disabled"
        v-if="options.length > 1"
        @command="handleDropdownClick"
      >
        <el-button type="primary">
          + 添 加<i class="el-icon-arrow-down el-icon--right"></i>
        </el-button>

        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item
            :disabled="menuDisabledList.includes(item.value)"
            style="width: 100px; font-size: 14px; padding: 6px"
            v-for="item in options"
            :key="item.value"
            :command="item.value"
            >{{ item.label }}</el-dropdown-item
          >
        </el-dropdown-menu>
      </el-dropdown>
      <el-button
        v-else
        :type="btnType"
        :disabled="disabled"
        @click="handleDropdownClick(options[0].value)"
        >{{ btnLabel }}</el-button
      >
    </template>
    <img
      v-else
      @click="handleDropdownClick(options[0].value)"
      style="float: right; width: 0.4rem; height: 0.4rem; cursor: pointer"
      src="@/assets/images/add.png"
      alt=""
      class="icon"
    />
    <el-dialog
      :title="'请选择' + title"
      :visible.sync="dialogVisible"
      :model="false"
      width="60%"
      v-dialogDrag
      v-dialogResize
    >
      <DynamicList
        v-if="functionName"
        :key="dynamicListKey"
        ref="dynamicList"
        :openRadioSelection="openRadioSelection"
        :functionName="functionName"
        :dialogName="dialogName"
        openMode="dialog"
        @handle-dialog-submit="handleSubmit"
        :customizeConditions="finallyConditions"
        :isTree="isTree"
        :treeFunctionName="treeFunctionName"
        :form="form"
        :isSecondaryDialog="isSecondaryDialog"
        showHeaderFilter
        :reserveSelection="true"
        :echoRowDataList="echoRowDataList"
        :extraDefaultSchemeCallBack="extraDefaultSchemeCallBack"
        :showPageSizeSelect="showPageSizeSelect"
        @size-state-change="handleSizeStateChange"
      >
      </DynamicList>
      <span slot="footer" class="dialog-footer">
        <!-- 页面大小选择器（位于页脚最左侧，状态与 DynamicList 保持同步） -->
        <div v-if="showPageSizeSelect" class="page-size-select-box">
          <template v-if="sizeState.dialogSizeType == 'custom'">
            <el-input
              v-model="sizeState.customDialogWidth"
              size="mini"
              class="page-size-input"
              placeholder="宽度"
              @change="handleFooterCustomSizeChange"
            >
              <template slot="append">%</template>
            </el-input>
            <el-input
              v-model="sizeState.customDialogHeight"
              size="mini"
              class="page-size-input"
              placeholder="高度"
              @change="handleFooterCustomSizeChange"
            >
              <template slot="append">vh</template>
            </el-input>
          </template>
          <el-select
            v-model="sizeState.dialogSizeType"
            size="mini"
            class="page-size-select"
            placeholder="页面大小"
            @change="handleFooterDialogSizeChange"
          >
            <el-option
              v-for="item in dialogSizeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </div>
        <span class="footer-btn-group">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" :disabled="disabled" @click="submit"
            >确 定</el-button
          >
        </span>
      </span>
    </el-dialog>
  </div>
</template>

<script>
// 导入动态列表组件
import DynamicList from "@/components/DynamicList/index.vue";
export default {
  name: "DynamicDialogBtn",
  components: {
    DynamicList,
  },
  props: {
    // 是否使用图片当按钮
    isImg: {
      type: Boolean,
      default: () => false,
    },
    // 是否开启单选功能
    openRadioSelection: {
      type: Boolean,
      default: () => false,
    },
    // 标题
    // title: {
    //   type: String,
    //   required: true,
    // },
    btnLabel: {
      type: String,
      default: () => "+ 添 加",
    },
    btnType: {
      type: String,
      default: () => "primary",
    },
    // 是否二级弹窗
    isSecondaryDialog: {
      type: Boolean,
      default: false,
    },
    form: {
      type: Object,
      default: () => {},
    },
    // 自定义搜索条件
    customizeConditions: {
      type: Array,
      default: () => [],
    },
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
    // 选单选项
    options: {
      type: Array,
      default: () => [],
    },
    // 开启默认查询
    isOpenDefaultQuery: {
      type: Boolean,
      default: () => true,
    },
    // 行数据
    rowData: {
      type: Object,
      default: () => null,
    },
    // 需要禁用的menu名称列表
    menuDisabledList: {
      type: Array,
      default: () => [],
    },
    // 传入的自定义默认查询条件
    extraDefaultSchemeCallBack: {
      type: Function,
      default: () => []
    },
    // 是否显示弹窗页面大小选择器（用于调整弹窗宽高）
    showPageSizeSelect: {
      type: Boolean,
      default: true
    }
  },
  created() {},
  computed: {
    finallyConditions() {
      // console.log("isOpenDefaultQuery", this.isOpenDefaultQuery);
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
            field: "is_unlock",
            operate: "EQ",
            value: "0",
            filterGroup: "ADVANCE",
          },
          ...this.customizeConditions,
          ...this.condition,
        ];
      } else {
        return [...this.customizeConditions, ...this.condition];
      }
    },
  },
  watch: {
    // 每次打开弹窗，重新发送请求
    dialogVisible: {
      handler: function (newVal, oldVal) {
        if (newVal) {
          this.dynamicListKey = Date.now();
        }
      },
    },
  },
  data() {
    return {
      dialogVisible: false,
      // 选中的模块名
      functionName: "",
      // 动态列表的key
      dynamicListKey: Date.now(),
      title: "",
      // 过滤条件
      condition: "",
      // 弹窗名称
      dialogName: "",
      // 打开数据列表时 已选择的数据设置为勾选状态
      reserveSelection: false,
      // 回显的数据
      echoRowDataList: [],
      // 页面大小选择器状态（与 DynamicList 内部状态保持同步）
      sizeState: {
        dialogSizeType: "default",
        customDialogWidth: "",
        customDialogHeight: "",
      },
      // 页面大小下拉选项
      dialogSizeOptions: [
        { label: "默认(60%)", value: "default" },
        { label: "中(75%)", value: "medium" },
        { label: "大(90%)", value: "large" },
        { label: "全屏(98%)", value: "full" },
        { label: "自定义", value: "custom" },
      ],
    };
  },
  methods: {
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
      console.log("处理选择的数据", data);
      this.dialogVisible = false;
      this.updateValue(data);
    },
    // DynamicList 页面大小状态同步（弹窗重新打开时，DynamicList 会根据弹窗当前尺寸回传状态）
    handleSizeStateChange(state) {
      Object.assign(this.sizeState, state);
    },
    // 页脚页面大小下拉框选择事件（委托给 DynamicList 处理，由其实际调整弹窗宽高）
    handleFooterDialogSizeChange(val) {
      const list = this.$refs.dynamicList;
      if (!list) return;
      list.customDialogWidth = this.sizeState.customDialogWidth;
      list.customDialogHeight = this.sizeState.customDialogHeight;
      list.handleDialogSizeChange(val);
    },
    // 页脚自定义宽高输入事件（委托给 DynamicList 处理）
    handleFooterCustomSizeChange() {
      const list = this.$refs.dynamicList;
      if (!list) return;
      list.customDialogWidth = this.sizeState.customDialogWidth;
      list.customDialogHeight = this.sizeState.customDialogHeight;
      list.handleCustomSizeChange();
    },
    // 更新value
    updateValue(value) {
      this.$emit("data-change", value, this.functionName);
    },
    // 处理选单按钮单击事件
    handleDropdownClick(command) {
      if (command == "MANUALLY_ADD") {
        // console.log("手动新增");
        const option = this.options.find((item) => item.value == command);
        if (option?.verify) {
          const res = option.verify();
          if (res != true) {
            return this.$message.warning(res);
          }
        }
        this.$emit("data-change", null, command);
      } else {
        const option = this.options.find((item) => item.value == command);
        // 调用按钮校验方法
        if (option?.verify) {
          const res = option.verify();
          if (res != true) {
            return this.$message.warning(res);
          }
        }
        // 获取筛选条件
        if (option?.condition) {
          this.condition = option.condition();
          // console.log("this.condition ", this.condition);
        }
        this.dialogVisible = true;
        this.functionName = command;
        this.reserveSelection = option?.reserveSelection || false;
        this.echoRowDataList = option.echoRowDataList
          ? option?.echoRowDataList()
          : [];
        console.log("options", option);
        this.dialogName = option.dialogName;
        this.title = option?.title || "";
      }
    },
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
/* 页脚：左侧页面大小选择器 + 右侧操作按钮 */
.dialog-footer {
  display: flex;
  align-items: center;
}
.footer-btn-group {
  margin-left: auto;
}
.page-size-select-box {
  display: flex;
  align-items: center;
}
.page-size-select {
  width: 110px;
}
.page-size-input {
  width: 115px;
  margin-right: 5px;
}
</style>
