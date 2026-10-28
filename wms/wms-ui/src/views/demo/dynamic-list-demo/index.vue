<template>
  <div class="app-container">
    <!-- :handleCustomAdd="handleCustomAdd" -->
    <!-- :handleCustomUpdate="handleCustomUpdate" -->
    <!-- :handleCustomDelete="handleCustomDelete" -->
    <!-- :openRadioSelection="openRadioSelection" -->
    <!-- :hiddenButtonList="['add','copy','pushDown','delete','update','export','print']" -->
    <DynamicList
      :disabledButtonList="finallyDisabledButtonList"
      ref="dynamicList"
      :openRadioSelection="false"
      :showSummary="showSummary"
      :functionName="functionName"
      :dialogName="dialogName"
      :changeOrderFN="changeOrderFN"
      :changeOrderPath="changeOrderPath"
      :hiddenButtonList="hiddenButtonList"
      :customizeConditions="customizeConditions"
    >
    </DynamicList>
  </div>
</template>

<script>
import DynamicList from "@/components/DynamicList/index.vue";
import { getDetails } from "@/api/system/common";
import dictData from "@/components/DictData";

export default {
  name: "DynamicListPage",
  computed: {
    dictData() {
      return dictData;
    },
    // 变更单的functionName
    changeOrderFN() {
      if (this.functionName && this.changeOrderMap[this.functionName]) {
        return this.changeOrderMap[this.functionName].changeOrderFN;
      } else {
        return null;
      }
    },
    // 变更单的路径
    changeOrderPath() {
      if (this.functionName && this.changeOrderMap[this.functionName]) {
        return this.changeOrderMap[this.functionName].changeOrderPath;
      } else {
        return null;
      }
    },
    finallyDisabledButtonList() {
      return [...this.disabledButtonList, ...this.customDisableBtnList];
    },
  },
  components: {
    DynamicList,
  },
  props: {
    //推送按钮
    options: {
      type: Array,
      default: () => [],
    }, // 禁用按钮
    // ['add','copy','pushDown','delete','update','export','print']
    disabledButtonList: {
      type: Array,
      default: () => [],
    },
  },

  data() {
    return {
      // 开启单选功能
      // openRadioSelection:true,
      functionName: null,
      dialogName: null,
      // 隐藏按钮列表
      hiddenButtonList: [],
      // 变更Map
      changeOrderMap: this.variables.changeOrderMap,
      // 显示取消按钮的页面
      cancelBtnFunctionNameList: this.variables.cancelBtnFunctionNameList,
      // 显示关闭按钮的页面
      closedBtnFunctionNameList: this.variables.closedBtnFunctionNameList,
      // 禁用按钮
      customDisableBtnList: [],
      // 自定义查询条件
      customizeConditions: [],
      showSummary: false, // 是否显示合计
      // 选中要修改的数据
      form: {},
      disabled: true,
      dialogTableVisible: false,
      mainId: null,
      unwatchCount: null,
    };
  },
  mounted() {
    this.unwatchCount = this.$watch(
      () => {
        return this.$refs.dynamicList.getSelectedList();
      },
      (val) => {
        console.log(val, "val 发货通知");
        if (val.length !== 1) {
          this.disabled = true;
        } else {
          if (val[0].pact_state == "通过") {
            this.disabled = false;
          }
          this.form = val[0];
          this.mainId = val[0].main_id;
        }
      }
    );
  },
  created() {
    console.log("demo-list-created");
    this.functionName = this.$route.query.functionName || this.$route.meta.perms;
    this.dialogName = this.$route.query.dialogName;
    // 不在map中说明不需要变更按钮
    if (!this.changeOrderMap[this.functionName]) {
      this.hiddenButtonList.push("change");
    }
    if (!this.cancelBtnFunctionNameList.includes(this.functionName)) {
      this.hiddenButtonList.push("cancel");
    }
    if (!this.closedBtnFunctionNameList.includes(this.functionName)) {
      this.hiddenButtonList.push("close");
    }
    if (this.functionName == "PP_RECORD") {
      this.hiddenButtonList.push("add");
      this.hiddenButtonList.push("copy");
    }
    // 隐藏变更单新增复制按钮
    if (
      [
        "SAL_CONT_C",
        "SAL_INT_CONT_C",
        "SAL_ORDER_C",
        "SAL_PRDNOTICE_C",
      ].includes(this.functionName)
    ) {
      this.hiddenButtonList.push("add");
      this.hiddenButtonList.push("copy");
      this.hiddenButtonList.push("rollback");
    }

    console.log("hiddenButtonList", this.hiddenButtonList);
    if (
      [
        "SAL_ORG_OUTSTOCK",
        "SAL_ORG_RESTOCK",
        "FI_RECORD",
        "WH_MATINV",
      ].includes(this.functionName)
    ) {
      const disabledButtonList = [
        "add",
        "copy",
        "pushDown",
        "delete",
        "update",
        "export",
        "print",
      ];
      this.customDisableBtnList.push(...disabledButtonList);
    }
    if (["SAL_TRANSPORT"].includes(this.functionName)) {
      const hiddenButtonList = [
        "add",
        "copy",
        "push-down",
        "delete",
        "update",
        "export",
        "change",
        "print-template",
      ];
      this.hiddenButtonList.push(...hiddenButtonList);
    }
    if (
      [
        "TECH_REVIEW_SAL_ORDER",
        "TECH_REVIEW_SAL_PRDNOTICE",
        "PLAN_REVIEW_SAL_ORDER",
        "PLAN_REVIEW_SAL_PRDNOTICE",
      ].includes(this.dialogName)
    ) {
      const hiddenButtonList = [
        "add",
        "copy",
        "push-down",
        "delete",
        "update",
        "export",
        "change",
        "print-template",
      ];
      this.hiddenButtonList.push(...hiddenButtonList);
    }
    if (["SAL_RESTOCK"].includes(this.functionName)) {
      const disabledButtonList = [
        "add",
        "copy",
        "delete",
        "update",
        "export",
        "print",
      ];
      this.customDisableBtnList.push(...disabledButtonList);
    }
    if (["FI_AR"].includes(this.functionName)) {
      const disabledButtonList = [
        "add",
        "copy",
        "pushDown",
        "update",
        "delete",
      ];
      this.customDisableBtnList.push(...disabledButtonList);
    }
    if (["FI_RBILL"].includes(this.functionName)) {
      const disabledButtonList = [
        "add",
        "copy",
        "pushDown",
        "update",
        "delete",
      ];
      this.customDisableBtnList.push(...disabledButtonList);
    }
    if (["FI_INVRECORD"].includes(this.functionName)) {
      const disabledButtonList = [
        "add",
        "copy",
        "pushDown",
        "update",
        "delete",
      ];
      this.customDisableBtnList.push(...disabledButtonList);
    }
    if (["FI_CHECK_ACCOUNT"].includes(this.functionName)) {
      const disabledButtonList = ["copy", "pushDown", "update"];
      this.customDisableBtnList.push(...disabledButtonList);
    }

    if (
      ["PLAN_REVIEW_SAL_ORDER", "PLAN_REVIEW_SAL_PRDNOTICE"].includes(
        this.dialogName
      )
    ) {
      const conditions = [
        {
          field: "is_tech_review",
          fieldType: "NUMBER",
          logicalOperator: "AND",
          operate: "EQ",
          value: "1",
          filterGroup: "ADVANCE",
        },
        // {
        //   field: "is_plan_review",
        //   fieldType: "NUMBER",
        //   logicalOperator: "AND",
        //   operate: "EQ",
        //   value: "0",
        //   filterGroup: "ADVANCE",
        // },
        {
          field: "pact_state",
          fieldType: "TEXT",
          logicalOperator: "AND",
          operate: "EQ",
          value: "通过",
          filterGroup: "ADVANCE",
        },
      ];
      this.customizeConditions = conditions;
    }
    if (
      ["TECH_REVIEW_SAL_ORDER", "TECH_REVIEW_SAL_PRDNOTICE"].includes(
        this.dialogName
      )
    ) {
      const conditions = [
        // {
        //   field: "is_tech_review",
        //   fieldType: "NUMBER",
        //   logicalOperator: "OR",
        //   operate: "EQ",
        //   value: "0",
        //   filterGroup: "FAST",
        // },
        // {
        //   field: "is_plan_review",
        //   fieldType: "NUMBER",
        //   logicalOperator: "OR",
        //   operate: "EQ",
        //   value: "0",
        //   filterGroup: "FAST",
        // },
        {
          field: "pact_state",
          fieldType: "TEXT",
          logicalOperator: "AND",
          operate: "EQ",
          value: "通过",
          filterGroup: "ADVANCE",
        },
      ];
      this.customizeConditions = conditions;
    }
    // 是否显示合计
    this.showSummary = [
      "SAL_INT_CONT",
      "SAL_CONT",
      "SAL_ORDER",
      "SAL_PRDNOTICE",
      "SAL_NOTICE",
      "PUR_APPLY",
      "SAL_OUTSTOCK",
      "SAL_RENOTICE",
      "SAL_RESTOCK",
    ].includes(this.functionName);
    console.log(this.customizeConditions, "customizeConditions");
  },
  methods: {
    stopWatching() {
      if (this.unwatchCount) {
        this.unwatchCount(); // 手动取消监听
      }
    },
    // 修改字段
    setFieldValue() {
      let list = this.$refs.dynamicList.getSelectedList();
      console.log(list, "list");
      if (list.length == 1) {
        getDetails(this.functionName, list[0].id).then((res) => {
          this.form = res.data;
        });
        // this.form = list[0];
      } else if (list.lengtg == 0) {
        console.log("请先选中一条数据");
      } else {
        console.log("选择单条数据");
      }
    },

    /**
     * 处理修改字段的逻辑
     * */
    handleClose(done) {
      this.$confirm("确认关闭？")
        .then((_) => {
          done();
        })
        .catch((_) => {});
    },
  },
  beforeDestroy() {
    this.stopWatching(); // 在组件销毁前取消监听
  },
};
</script>
<style lang="scss" scoped></style>
