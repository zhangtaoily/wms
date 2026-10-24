<template>
  <div>
    <el-select
      :disabled="disabled"
      class="operationSelect"
      @change="handleCommand"
      :value="'业务操作'"
      placeholder="业务操作"
      v-loading="loading"
    >
      <el-option
        v-for="item in finalOptions"
        :key="item.value"
        :label="item.label"
        :value="item.value"
        :disabled="btnIsDisabled(item.value)"
      >
      </el-option>
    </el-select>
    <el-dialog append-to-body title="取消" :visible.sync="dialogFormVisible">
      <el-form :model="form" :rules="rules" ref="form">
        <el-form-item label="取消描述" :label-width="formLabelWidth">
          <el-input
            type="textarea"
            :rows="2"
            v-model="form.cancelReason"
            autocomplete="off"
          ></el-input>
        </el-form-item>
        <template v-if="showIsGf">
          <el-form-item
            label="是否产生费用"
            prop="isGf"
            :label-width="formLabelWidth"
          >
            <el-radio-group v-model="form.isGf">
              <el-radio
                v-for="item in radioList"
                :key="item.value"
                :label="item.value"
                >{{ item.label }}</el-radio
              >
            </el-radio-group>
          </el-form-item>
          <el-form-item
            label="是否通知染厂"
            prop="isNd"
            :label-width="formLabelWidth"
          >
            <el-radio-group v-model="form.isNd">
              <el-radio
                v-for="item in radioList"
                :key="item.value"
                :label="item.value"
                >{{ item.label }}</el-radio
              >
            </el-radio-group>
          </el-form-item>
          <el-form-item
            v-if="getFirstDyeMethod && getFirstDyeMethod == '2'"
            label="染色车间"
            prop="jrcj"
            :label-width="formLabelWidth"
          >
            <el-select v-model="form.jrcj" filterable placeholder="请选择">
              <el-option
                v-for="dict in getDyeWorkshop"
                :key="dict.dictValue"
                :label="dict.dictLabel"
                :value="dict.dictValue"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item
            v-if="getFirstDyeMethod && getFirstDyeMethod == '5'"
            label="染色车间"
            prop="trcj"
            :label-width="formLabelWidth"
          >
            <el-select v-model="form.trcj" filterable placeholder="请选择">
              <el-option
                v-for="dict in getDyeWorkshop"
                :key="dict.dictValue"
                :label="dict.dictLabel"
                :value="dict.dictValue"
              ></el-option>
            </el-select>
          </el-form-item>
        </template>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取 消</el-button>
        <el-button :loading="submitLoading" type="primary" @click="submit"
          >确 定</el-button
        >
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getDictDataList } from "@/api/system/dict/data";
import { radioList } from "@/utils/variables.js";
import { checkPermi, checkRole } from "@/utils/permission"; // 权限判断函数
import {
  batchCancel,
  batchReCancel,
  batchCancelDetails,
  batchReCancelDetails,
  batchClose,
  batchReClose,
  batchCloseDetails,
  batchReCloseDetails,
} from "@/api/system/common";
export default {
  // 业务操作
  name: "businessOperationBtn",
  props: {
    functionName: {
      type: String,
    },
    multiDataList: {
      type: Array,
    },
    // 子表数据
    subTableData: {
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
    // 显示按钮list
    hiddenButtonList: {
      type: Array,
      default: () => [],
    },
  },
  computed: {
    // 显示是否产生费用
    showIsGf() {
      return (
        this.functionName == "SAL_ORDER" || this.functionName == "SAL_PRDNOTICE"
      );
    },
    options() {
      if (this.showTableGoodsBtn) {
        return this.goodsOptions;
      } else if (this.showTableHeaderBtn) {
        return this.headerOptions;
      } else {
        return this.listOptions;
      }
    },
    finalOptions() {
      if (!this.hiddenButtonList || this.hiddenButtonList == [])
        return this.options;
      return this.options.filter(
        (item) => !this.hiddenButtonList.includes(item.value)
      );
    },
    getFirstDyeMethod() {
      if (!this.multiDataList) {
        return null;
      }
      console.log(
        "getFirstDyeMethod",
        this.multiDataList,
        this.showTableGoodsBtn,
        this.showTableHeaderBtn
      );
      if (this.showTableGoodsBtn) {
        return this.multiDataList[0]?.dyeMethod;
      } else if (this.showTableHeaderBtn) {
        if (this.functionName == "SAL_ORDER") {
          return (
            this.multiDataList[0]?.dyeMethod ||
            this.multiDataList[0]?.sal_order_goods[0]?.dyeMethod
          );
        } else if (this.functionName == "SAL_PRDNOTICE") {
          return (
            this.multiDataList[0]?.dyeMethod ||
            this.multiDataList[0]?.sal_prdnotice_goods[0]?.dyeMethod
          );
        } else {
          return this.multiDataList[0]?.dyeMethod;
        }
      }
      return null;
    },
    getDyeWorkshop() {
      const dyeMethod = this.getFirstDyeMethod;
      if (dyeMethod == "5") {
        // 筒染车间
        return this.dictDataList.t_tube_dyeing_workshop;
      } else if (dyeMethod == "2") {
        // 绞染车间
        return this.dictDataList.t_stranding_dyeing_workshop;
      }
    },
  },
  created() {},
  methods: {
    checkPermi,
    checkRole,
    // 销售员和跟单员不能看到
    checkSalerRole() {
      let flag = false;
      if (this.checkPermi(["admin"])) {
        flag = true;
      } else {
        flag = !this.checkRole(["inter", "saler"]);
      }
      return flag;
    },
    submit() {
      if (this.command == "cancel") {
        this.cancel();
      } else if (this.command == "cancelTable") {
        this.cancelTable();
      }
    },
    // 处理逻辑
    handleCommand(command) {
      this.command = command;
      if ("cancel" == command) {
        this.dialogFormVisible = true;
      } else if ("cancelTable" == command) {
        this.dialogFormVisible = true;
      } else {
        this.$modal
          .confirmHTML('是否确认<span style="color: red;">取消/关闭</span>数据')
          .then(() => {
            this.loading = true;
            switch (command) {
              case "reCancel":
                this.reCancel();
                break;
              case "reCancelTable":
                this.reCancelTable();
                break;
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
                this.$message.warning("未知的方法" + command);
                break;
            }
          })
          .catch((err) => {});
      }
    },
    // 明细取消
    cancel() {
      this.submitLoading = true;
      if (!this.multiDataList || this.multiDataList.length <= 0) {
        return this.$message.warning("请选择数据");
      }
      const detailId = this.multiDataList.map((item) => item.id);
      console.log(this.functionName, this.multiDataList, detailId);
      let id;
      let details = [];
      if (this.functionName == "SAL_ORDER") {
        id = this.multiDataList[0]?.main_id || this.multiDataList[0]?.orderId;
        this.multiDataList.forEach((item) => {
          details.push({
            id: item.id,
            sortIndex: item.sortIndex,
            rbNo: item.rbNo,
            sbNo: item.sbNo,
            dyeMethod: item.dyeMethod,
          });
        });
      } else if (this.functionName == "SAL_PRDNOTICE") {
        id =
          this.multiDataList[0]?.main_id || this.multiDataList[0]?.prdnoticeId;
        this.multiDataList.forEach((item) => {
          details.push({
            id: item.id,
            sortIndex: item.sortIndex,
            rbNo: item.rbNo,
            sbNo: item.sbNo,
            dyeMethod: item.dyeMethod,
          });
        });
      } else if (this.functionName == "SAL_NOTICE") {
        id = this.multiDataList[0]?.main_id || this.multiDataList[0]?.noticeId;
      } else if (this.functionName == "SAL_CONT") {
        id = this.multiDataList[0]?.main_id || this.multiDataList[0]?.contId;
      } else if (this.functionName == "SAL_INT_CONT") {
        id = this.multiDataList[0]?.main_id || this.multiDataList[0]?.intContId;
      }
      console.log("this.multiDataList", this.multiDataList);
      const cancelQueryData = {
        ...this.form,
        ids: detailId,
        id,
        details,
        dyeMethod: this.getFirstDyeMethod,
      };
      batchCancelDetails(this.functionName, cancelQueryData)
        .then((res) => {
          console.log(res);
          this.$message.success(res.msg);
          this.modifyDataState(1);
          this.dialogFormVisible = false;
        })
        .finally(() => {
          this.submitLoading = false;
        });
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
      this.submitLoading = true;
      if (!this.multiDataList || this.multiDataList.length <= 0) {
        return this.$message.warning("请选择数据");
      }
      const ids = this.multiDataList.map((item) => item?.main_id || item.id);
      const cancelQueryData = {
        ...this.form,
        id: ids[0],
        ids,
        dyeMethod: this.getFirstDyeMethod,
      };
      if (this.showIsGf) {
        const form = this.multiDataList[0];
        const details = [];
        let subTableData;
        if (this.functionName == "SAL_ORDER") {
          subTableData = form.sal_order_goods;
        } else if (this.functionName == "SAL_PRDNOTICE") {
          subTableData = form.sal_prdnotice_goods;
        }
        subTableData.forEach((item) => {
          details.push({
            id: item.id,
            sortIndex: item.sortIndex,
            rbNo: item.rbNo,
            sbNo: item.sbNo,
            dyeMethod: item.dyeMethod,
          });
        });
        cancelQueryData.details = details;
      }
      batchCancel(this.functionName, cancelQueryData)
        .then((res) => {
          console.log(res);
          this.$message.success(res.msg);
          // this.modifyDataState(1);
          this.dialogFormVisible = false;
        })
        .finally(() => {
          this.submitLoading = false;
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
        this.closedModifyDataState(2);
      });
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
        this.closedModifyDataState(0);
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
        this.closedModifyDataState(2);
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
        this.closedModifyDataState(0);
      });
    },
    // 修改数据取消取消状态
    modifyDataState(status) {
      this.loading = false;
      // 详情页面，表头取消需要刷新页面
      if (this.showTableHeaderBtn) {
        // window.location.reload();
      } else {
        this.multiDataList.forEach((item) => {
          this.$set(item, "is_cancel_detail", status);
          this.$set(item, "isCancel", status);
          this.$set(item, "is_cancel_name", status == 0 ? "正常" : "取消");
        });
      }
    },
    // 修改数据关闭状态
    closedModifyDataState(status) {
      this.loading = false;
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
        case "cancel":
          // 意向合同、销售合同不允许明细行取消。
          if (
            this.functionName == "SAL_CONT" ||
            this.functionName == "SAL_INT_CONT"
          ) {
            return true;
          }
          return this.goodsCancelBtnDisable;
        case "reCancel":
          // 意向合同、销售合同不允许明细行反取消。
          if (
            this.functionName == "SAL_CONT" ||
            this.functionName == "SAL_INT_CONT"
          ) {
            return true;
          }
          return this.goodsReCancelBtnDisable;
        case "cancelTable":
          return this.cancelBtnDisable;
        case "reCancelTable":
          return this.reCancelBtnDisable;
        case "close":
          return this.goodsClosedBtnDisable;
        case "reClose":
          return this.goodsReClosedBtnDisable;
        case "closeTable":
          return this.closedBtnDisable;
        case "reCloseTable":
          return this.reClosedBtnDisable;
        default:
          this.$message.warning("未知的方法" + value);
          break;
      }
    },
    // 获取全部数据字典数据
    getDictData() {
      getDictDataList(this.dicts)
        .then((res) => {
          this.dictDataList = res.data;
        })
        .catch((err) => {
          this.$message.error("获取数据字段数据失败");
        });
    },
  },
  watch: {
    dialogFormVisible: {
      handler: function (newVal, oldVal) {
        console.log("dialogFormVisible", newVal, this.dictDataList);
        if (this.dictDataList && this.dictDataList.length <= 0) {
          // 获取数据字段数据
          this.getDictData();
        }
      },
    },
    multiDataList: {
      handler: function (newVal, oldVal) {
        this.goodsCancelBtnDisable = false;
        this.goodsReCancelBtnDisable = false;
        this.cancelBtnDisable = false;
        this.reCancelBtnDisable = false;
        this.goodsClosedBtnDisable = false;
        this.goodsReClosedBtnDisable = false;
        this.closedBtnDisable = false;
        this.reClosedBtnDisable = false;
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
            // 明细关闭- 有一个不为正常 按钮变灰
            if (item.isClosed != 0) {
              this.goodsClosedBtnDisable = true;
            }
            // 明细反关闭 关闭字段为正常 按钮变灰
            if (item.isClosed == 0) {
              this.goodsReClosedBtnDisable = true;
            }
            return this.goodsCancelBtnDisable && this.goodsReCancelBtnDisable;
          });
        } else if (this.showTableHeaderBtn) {
          newVal.find((item) => {
            const isCancel = item?.is_cancel || item?.isCancel;
            const isClosed = item?.is_closed || item?.isClosed;
            // 单据取消- 有一个不为正常 按钮变灰
            if (isCancel != 0) {
              this.cancelBtnDisable = true;
            }
            // 单据反取消 有一个不为取消 按钮变灰
            if (isCancel != 1) {
              this.reCancelBtnDisable = true;
            }
            // 单据关闭- 有一个不为正常 按钮变灰
            if (isClosed != 0) {
              this.closedBtnDisable = true;
            }
            // 单据反关闭 有一个不为手动关闭 按钮变灰
            if (isClosed == 0) {
              this.reClosedBtnDisable = true;
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
            // 明细关闭- 有一个不为正常 按钮变灰
            if (item.is_closed_detail != 0) {
              this.goodsClosedBtnDisable = true;
            }
            // 明细反关闭 有一个不为手动关闭 按钮变灰
            if (item.is_closed_detail == 0) {
              this.goodsReClosedBtnDisable = true;
            }

            // 单据关闭- 有一个不为正常 按钮变灰
            if (item.is_closed != 0) {
              this.closedBtnDisable = true;
            }
            // 单据反关闭 有一个不为手动关闭 按钮变灰
            if (item.is_closed == 0) {
              this.reClosedBtnDisable = true;
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
      dicts: ["t_tube_dyeing_workshop", "t_stranding_dyeing_workshop"],
      // 数据字典列表
      dictDataList: [],
      // 提交Loading
      submitLoading: false,
      command: "",
      // 是否 单选
      radioList: radioList,
      dialogFormVisible: false,
      form: {
        cancelReason: "",
      },
      formLabelWidth: "120px",
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
          value: "cancelTable",
          label: "取 消",
        },
        {
          value: "reCancelTable",
          label: "反取消  ",
        },
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
          value: "cancel",
          label: "取 消",
        },
        {
          value: "reCancel",
          label: "反取消  ",
        },
        {
          value: "close",
          label: "关 闭",
        },
        {
          value: "reClose",
          label: "反关闭",
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
      // 明细关闭
      goodsClosedBtnDisable: false,
      // 明细反关闭
      goodsReClosedBtnDisable: false,
      // 单据关闭
      closedBtnDisable: false,
      // 单据反关闭
      reClosedBtnDisable: false,
      // 是否加载
      loading: false,
      rules: {
        isGf: [
          {
            required: true,
            message: "是否产生费用不能为空",
            trigger: ["blur", "change", "input"],
          },
        ],
        isNd: [
          {
            required: true,
            message: "是否通知染厂不能为空",
            trigger: ["blur", "change", "input"],
          },
        ],
      },
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
.operationSelect {
  ::v-deep .el-input__inner {
    width: 110px;
  }
  ::v-deep .el-input__icon {
    height: 28.5px;
  }
}
</style>
