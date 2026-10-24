<template>
  <div
  v-if="checkSalerRole()">
    <el-dropdown
      split-button
      v-if="showBtn"
      :disabled="disabled"
      :hide-timeout="500"
      @command="commandHandler"
    >
      <span @click="handleCheck(1)">检 查</span>
      <el-dropdown-menu slot="dropdown">
        <el-dropdown-item command="check">检 查</el-dropdown-item>
        <el-dropdown-item command="reCheck">反检查</el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>
  </div>
</template>

<script>
import { checkPermi, checkRole } from "@/utils/permission"; // 权限判断函数
import {updateCheckState} from "@/api/system/common.js";
export default {
  name: "CheckBtn",
  props: {
    disabled: {
      type: Boolean,
      default: () => false,
    },
    form: {
      type: Object,
      default: () => null,
    },
    getMultiDataList: {
      type: Function,
    },
  },
  created() {
    this.functionName = this.$route.query.functionName;
    if (this.showCheckBtnList.includes(this.functionName)) {
      this.showBtn = true;
    }
  },
  data() {
    return {
      // 检查Map
      showCheckBtnList: this.variables.showCheckBtnList,
      functionName: "",
      showBtn: false,
    };
  },
  methods: {
    checkPermi,
    checkRole,
    // 销售员和跟单员不能看到
    checkSalerRole(){
      let flag = false;
      if(this.checkPermi(['admin'])){
        flag = true;
      }else{
        flag = !this.checkRole(['inter','saler'])
      }
      return flag;
    },
    /** 检查按钮操作 */
    handleCheck(isCheck) {
      const string = isCheck == 1 ? "检查" : "反检查";
      this.$modal
        .confirmHTML(
          '是否<span style="color: #28cd6b;">'+string+'</span>数据',
          "确定"
        )
        .then(() => {
          let ids = new Set();
          // 判断是否审核通过
          if (this.form) {
            if (this.form.pactState != "3") {
              this.$message.warning("请选择通过的单据进行"+string);
              return false;
            }
            ids.add(this.form.id);
          } else {
            const list = this.getMultiDataList();
            let result = list.find((item) => item.pact_state != "通过");
            if (result) {
              this.$message.warning("请选择通过的单据进行"+string);
              return false;
            }
            list.forEach((item) => {
              ids.add(item?.main_id || item.id);
            });
          }
          console.log("ids", ids,isCheck);
          updateCheckState(this.functionName,isCheck, Array.from(ids)).then((res) => {
            this.$modal.msgSuccess(string + "成功");
          })
        })
        .catch((err) => {
          console.log("err :>> ", err); // this.$modal.msgSuccess("取消提交", err);
        });
    },
    // 点击下列项事件
    commandHandler(value) {
      switch (value) {
        case "check":
          this.handleCheck(1);
          break;
        case "reCheck":
          this.handleCheck(0);
          break;
        default:
          break;
      }
    },
  },
  computed: {},
};
</script>

<style></style>
