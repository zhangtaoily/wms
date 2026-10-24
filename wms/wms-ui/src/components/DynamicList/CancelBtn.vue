<template>
  <el-select
    :disabled="disabled"
    class="cancelSelect"
    @change="handleCommand"
    :value="'取消/反取消'"
    placeholder="取消/反取消"
  >
    <el-option
      v-for="item in options"
      :key="item.value"
      :label="item.label"
      :value="item.value"
      :disabled="btnIsDisabled(item.value)"
    >
    </el-option>
  </el-select>
</template>

<script>
import {
  batchCancel,
  batchReCancel,
  batchCancelDetails,
  batchReCancelDetails,
} from "@/api/system/common";
export default {
  name: "cancelBtn",
  props: {
    functionName: {
      type: String,
    },
    multiDataList: {
      type: Array,
    },
    disabled: {
      type: Boolean,
      default: () => {},
    },
    // 详情页面表头按钮
    showTableHeaderBtn: {
      type: Boolean,
      default: () => false,
    },
    // 详情页面子表按钮
    showTableGoodsBtn: {
      type: Boolean,
      default: () => false,
    },
  },
  computed: {
    options() {
      if (this.showTableGoodsBtn) {
        return this.goodsOptions;
      } else if (this.showTableHeaderBtn) {
        return this.headerOptions;
      } else {
        return this.listOptions;
      }
    },
  },
  methods: {
    // 明细取消
    cancel() {
      if (!this.multiDataList || this.multiDataList.length <= 0) {
        return this.$message.warning("请选择数据");
      }
      const detailId = this.multiDataList.map((item) => item.id);
      console.log(this.functionName, this.multiDataList, detailId);
      batchCancelDetails(this.functionName, detailId).then((res) => {
        console.log(res);
        this.$message.success(res.msg);
        this.modifyDataState(1);
      });
    },
    // 处理逻辑
    handleCommand(command) {
      this.$modal
        .confirmHTML('是否确认<span style="color: red;">取消/反取消</span>数据')
        .then(() => {
          switch (command) {
            case "cancel":
              this.cancel();
              break;
            case "reCancel":
              this.reCancel();
              break;
            case "cancelTable":
              this.cancelTable();
              break;
            case "reCancelTable":
              this.reCancelTable();
              break;
            default:
              this.$message.warning("未知的方法" + command);
              break;
          }
        })
        .catch((err) => {});
    },
    // 明细反取消
    reCancel() {
      if (!this.multiDataList || this.multiDataList.length <= 0) {
        return this.$message.warning("请选择数据");
      }
      const detailId = this.multiDataList.map((item) => item.id);
      console.log(this.functionName, this.multiDataList, detailId);
      batchReCancelDetails(this.functionName, detailId).then((res) => {
        console.log(res);
        this.$message.success(res.msg);
        this.modifyDataState(0);
      });
    },
    // 表头取消
    cancelTable() {
      if (!this.multiDataList || this.multiDataList.length <= 0) {
        return this.$message.warning("请选择数据");
      }
      const ids = this.multiDataList.map((item) => item?.main_id || item.id);
      batchCancel(this.functionName, ids).then((res) => {
        console.log(res);
        this.$message.success(res.msg);
        this.modifyDataState(1);
      });
    },
    // 表头反取消
    reCancelTable() {
      if (!this.multiDataList || this.multiDataList.length <= 0) {
        return this.$message.warning("请选择数据");
      }
      const ids = this.multiDataList.map((item) => item?.main_id || item.id);
      batchReCancel(this.functionName, ids).then((res) => {
        console.log(res);
        this.$message.success(res.msg);
        this.modifyDataState(0);
      });
    },
    // 修改数据取消取消状态
    modifyDataState(status) {
      // 详情页面，表头取消需要刷新页面
      if (this.showTableHeaderBtn) {
        window.location.reload();
      } else {
        this.multiDataList.forEach((item) => {
          this.$set(item, "is_cancel_detail", status);
          this.$set(item, "isCancel", status);
          this.$set(item, "is_cancel_name", status == 0 ? "正常" : "取消");
        });
      }
      // this.multiDataList.forEach((item) => {
      //   this.$set(item, "is_cancel_detail", status);
      //   this.$set(item, "isCancel", status);
      //   this.$set(item, "is_cancel_name", status == 0 ? "正常" : "取消");
      // });
    },
    btnIsDisabled(value) {
      switch (value) {
        case "cancel":
          return this.goodsCancelBtnDisable;
        case "reCancel":
          return this.goodsReCancelBtnDisable;
        case "cancelTable":
          return this.cancelBtnDisable;
        case "reCancelTable":
          return this.reCancelBtnDisable;
        default:
          this.$message.warning("未知的方法" + value);
          break;
      }
    },
  },
  watch: {
    multiDataList: {
      handler: function (newVal, oldVal) {
        this.goodsCancelBtnDisable = false;
        this.goodsReCancelBtnDisable = false;
        this.cancelBtnDisable = false;
        this.reCancelBtnDisable = false;
        if (!newVal) {
          return;
        }
        if (this.showTableGoodsBtn) {
          newVal.find((item) => {
            console.log("isCancel", item.isCancel);
            // 明细取消- 有一个不为正常 按钮变灰
            if (item.isCancel != 0) {
              this.goodsCancelBtnDisable = true;
            }
            // 明细反取消 有一个不为取消 按钮变灰
            if (item.isCancel != 1) {
              this.goodsReCancelBtnDisable = true;
            }
            return this.goodsCancelBtnDisable && this.goodsReCancelBtnDisable;
          });
        } else if (this.showTableHeaderBtn) {
          newVal.find((item) => {
            // 单据取消- 有一个不为正常 按钮变灰
            if (item.isCancel != 0) {
              this.cancelBtnDisable = true;
            }
            // 单据反取消 有一个不为取消 按钮变灰
            if (item.isCancel != 1) {
              this.reCancelBtnDisable = true;
            }
            return this.reCancelBtnDisable && this.cancelBtnDisable;
          });
        } else {
          // is_cancel_detail
          // is_cancel_detail
          // is_cancel
          // is_cancel
          newVal.find((item) => {
            // 明细取消- 有一个不为正常 按钮变灰
            if (item.is_cancel_detail != 0) {
              this.goodsCancelBtnDisable = true;
            }
            // 明细反取消 有一个不为取消 按钮变灰
            if (item.is_cancel_detail != 1) {
              this.goodsReCancelBtnDisable = true;
            }

            // 单据取消- 有一个不为正常 按钮变灰
            if (item.is_cancel != 0) {
              this.cancelBtnDisable = true;
            }
            // 单据反取消 有一个不为取消 按钮变灰
            if (item.is_cancel != 1) {
              this.reCancelBtnDisable = true;
            }
            return (
              this.reCancelBtnDisable &&
              this.cancelBtnDisable &&
              this.goodsCancelBtnDisable &&
              this.goodsReCancelBtnDisable
            );
          });
        }
      },
      immediate: true,
    },
  },
  data() {
    return {
      // 列表展示
      listOptions: [
        {
          value: "cancel",
          label: "取 消",
        },
        {
          value: "reCancel",
          label: "反取消  ",
        },
        {
          value: "cancelTable",
          label: "单据取消",
        },
        {
          value: "reCancelTable",
          label: "单据反取消  ",
        },
      ],
      // 表头展示
      headerOptions: [
        {
          value: "cancelTable",
          label: "取 消",
        },
        {
          value: "reCancelTable",
          label: "反取消  ",
        },
      ],
      // 明细展示
      goodsOptions: [
        {
          value: "cancel",
          label: "取 消",
        },
        {
          value: "reCancel",
          label: "反取消  ",
        },
      ],
      // 明细取消
      goodsCancelBtnDisable: false,
      // 明细反取消
      goodsReCancelBtnDisable: false,
      // 单据取消
      cancelBtnDisable: false,
      // 单据反取消
      reCancelBtnDisable: false,
    };
  },
};
</script>

<style lang="scss" scoped>
.el-dropdown {
  vertical-align: top;
}
.el-dropdown + .el-dropdown {
  margin-left: 15px;
}
.el-icon-arrow-down {
  font-size: 12px;
}
.cancelSelect {
  ::v-deep .el-input__inner {
    width: 110px;
  }
  ::v-deep .el-input__icon {
    height: 28.5px;
  }
}
</style>
