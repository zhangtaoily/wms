<template>
  <el-select
    :disabled="disabled"
    class="closeSelect"
    @change="handleCommand"
    :value="'关闭/反关闭'"
    placeholder="关闭/反关闭"
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
  batchClose,
  batchReClose,
  batchCloseDetails,
  batchReCloseDetails,
} from "@/api/system/common";
export default {
  name: "closeBtn",
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
  methods: {
    // 明细关闭
    close() {
      if (!this.multiDataList || this.multiDataList.length <= 0) {
        return this.$message.warning("请选择数据");
      }
      const detailId = this.multiDataList.map((item) => item.id);
      console.log(this.functionName, this.multiDataList, detailId);
      batchCloseDetails(this.functionName, detailId).then((res) => {
        console.log(res);
        this.$message.success(res.msg);
        this.modifyDataState(1);
      });
    },
    // 处理逻辑
    handleCommand(command) {
      this.$modal
        .confirmHTML('是否确认<span style="color: red;">关闭/反关闭</span>数据')
        .then(() => {
          switch (command) {
            case "close":
              this.close();
              break;
            case "reClose":
              this.reClose();
              break;
            case "closeTable":
              this.closeTable();
              break;
            case "reCloseTable":
              this.reCloseTable();
              break;
            default:
              this.$message.warning("未知的方法");
              break;
          }
        })
        .catch((err) => {});
    },
    // 明细反关闭
    reClose() {
      if (!this.multiDataList || this.multiDataList.length <= 0) {
        return this.$message.warning("请选择数据");
      }
      const detailId = this.multiDataList.map((item) => item.id);
      console.log(this.functionName, this.multiDataList, detailId);
      batchReCloseDetails(this.functionName, detailId).then((res) => {
        console.log(res);
        this.$message.success(res.msg);
        this.modifyDataState(0);
      });
    },
    // 表头关闭
    closeTable() {
      if (!this.multiDataList || this.multiDataList.length <= 0) {
        return this.$message.warning("请选择数据");
      }
      const ids = this.multiDataList.map((item) => item?.main_id || item.id);
      batchClose(this.functionName, ids).then((res) => {
        console.log(res);
        this.$message.success(res.msg);
        this.modifyDataState(1);
      });
    },
    // 表头反关闭
    reCloseTable() {
      if (!this.multiDataList || this.multiDataList.length <= 0) {
        return this.$message.warning("请选择数据");
      }
      const ids = this.multiDataList.map((item) => item?.main_id || item.id);
      batchReClose(this.functionName, ids).then((res) => {
        console.log(res);
        this.$message.success(res.msg);
        this.modifyDataState(0);
      });
    },
    // 修改数据关闭取消状态
    modifyDataState(status) {
      // 详情页面，表头取消需要刷新页面
      if (this.showTableHeaderBtn) {
        window.location.reload();
      } else {
        this.multiDataList.forEach((item) => {
          this.$set(item, "is_closed_detail", status);
          this.$set(item, "isClosed", status);
          this.$set(item, "is_closed_name", status == 0 ? "正常" : "手动关闭");
        });
      }
    },
    btnIsDisabled(value) {
      switch (value) {
        case "close":
          return this.goodsClosedBtnDisable;
        case "reClose":
          return this.goodsReClosedBtnDisable;
        case "closeTable":
          return this.closedBtnDisable;
        case "reCloseTable":
          return this.reClosedBtnDisable;
        default:
          this.$message.warning("未知的方法");
          break;
      }
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
  watch: {
    multiDataList: {
      handler: function (newVal, oldVal) {
        this.goodsClosedBtnDisable = false;
        this.goodsReClosedBtnDisable = false;
        this.closedBtnDisable = false;
        this.reClosedBtnDisable = false;
        if (!newVal) {
          return;
        }
        if (this.showTableGoodsBtn) {
          newVal.find((item) => {
            // 明细关闭- 有一个不为正常 按钮变灰
            if (item.isClosed != 0) {
              this.goodsClosedBtnDisable = true;
            }
            // 明细反关闭 有一个不为手动关闭 按钮变灰
            if (item.isClosed != 2) {
              this.goodsReClosedBtnDisable = true;
            }
            return this.goodsClosedBtnDisable && this.goodsReClosedBtnDisable;
          });
        } else if (this.showTableHeaderBtn) {
          newVal.find((item) => {
            // 单据关闭- 有一个不为正常 按钮变灰
            if (item.isClosed != 0) {
              this.closedBtnDisable = true;
            }
            // 单据反关闭 有一个不为手动关闭 按钮变灰
            if (item.isClosed != 2) {
              this.reClosedBtnDisable = true;
            }
            return this.reClosedBtnDisable && this.closedBtnDisable;
          });
        } else {
          // is_closed_detail
          // is_cancel_detail
          // is_cancel
          // is_closed
          newVal.find((item) => {
            // 明细关闭- 有一个不为正常 按钮变灰
            if (item.is_closed_detail != 0) {
              this.goodsClosedBtnDisable = true;
            }
            // 明细反关闭 有一个不为手动关闭 按钮变灰
            if (item.is_closed_detail != 2) {
              this.goodsReClosedBtnDisable = true;
            }

            // 单据关闭- 有一个不为正常 按钮变灰
            if (item.is_closed != 0) {
              this.closedBtnDisable = true;
            }
            // 单据反关闭 有一个不为手动关闭 按钮变灰
            if (item.is_closed != 2) {
              this.reClosedBtnDisable = true;
            }
            console.log(
              this.reClosedBtnDisable,
              this.closedBtnDisable,
              this.goodsClosedBtnDisable,
              this.goodsReClosedBtnDisable
            );
            return (
              this.reClosedBtnDisable &&
              this.closedBtnDisable &&
              this.goodsClosedBtnDisable &&
              this.goodsReClosedBtnDisable
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
          value: "close",
          label: "关 闭",
        },
        {
          value: "reClose",
          label: "反关闭",
        },
        {
          value: "closeTable",
          label: "单据关闭",
        },
        {
          value: "reCloseTable",
          label: "单据反关闭",
        },
      ],
      // 表头展示
      headerOptions: [
        {
          value: "closeTable",
          label: "关 闭",
        },
        {
          value: "reCloseTable",
          label: "反关闭",
        },
      ],
      // 明细展示
      goodsOptions: [
        {
          value: "close",
          label: "关 闭",
        },
        {
          value: "reClose",
          label: "反关闭",
        },
      ],
      // 明细关闭
      goodsClosedBtnDisable: false,
      // 明细反关闭
      goodsReClosedBtnDisable: false,
      // 单据关闭
      closedBtnDisable: false,
      // 单据反关闭
      reClosedBtnDisable: false,
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
.closeSelect {
  ::v-deep .el-input__inner {
    width: 110px;
  }
  ::v-deep .el-input__icon {
    height: 28.5px;
  }
}
</style>
