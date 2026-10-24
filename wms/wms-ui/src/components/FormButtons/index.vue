<template>
  <div :class="['app-container', { 'mobile-header': isMobile }]">
    <div class="header" v-if="!notShowFormHeader">
      <p class="header-title">
        {{ customizeTitle || title }}
      </p>
      <!-- 按钮组 -->
      <el-row v-show="showButtonGroup" :gutter="5" class="button-group">
        <slot name="frontButtonSlot"></slot>
        <el-col v-if="buttonIsShow('add')" :span="1.5">
          <el-button
            size="mini"
            :disabled="buttonIsDisabled('add')"
            icon="el-icon-plus"
            @click="handleAdd"
            >新 增</el-button
          >
        </el-col>
        <el-col v-if="buttonIsShow('copy')" :span="1.5">
          <el-button
            size="mini"
            icon="el-icon-document-copy"
            @click="handleCopy"
            :disabled="buttonIsDisabled('copy') || !id"
            >复 制</el-button
          >
        </el-col>
        <el-col v-if="buttonIsShow('copy')" :span="1.5">
          <el-button
            size="mini"
            icon="el-icon-document-copy"
            @click="handleCopyAll"
            :disabled="buttonIsDisabled('copy') || !id"
            >关联复制</el-button
          >
        </el-col>
        <!-- <el-col  v-if="buttonIsShow('edit')" :span="1.5">
        <el-button
          size="mini"
          icon="el-icon-edit"
          @click="handleEdit"
          >编辑</el-button
        >
      </el-col> -->
        <el-col v-if="buttonIsShow('draft')" :span="1.5">
          <el-button
            icon="el-icon-document"
            :loading="buttonLoading"
            @click="stagingForm"
            :disabled="buttonIsDisabled('draft') || !stagingBtnIsShow"
            >暂 存</el-button
          >
        </el-col>

        <el-col v-if="buttonIsShow('save')" :span="1.5">
          <el-button
            icon="el-icon-document"
            :loading="buttonLoading"
            @click="saveForm"
            :disabled="buttonIsDisabled('save') || !saveBtnIsShow"
            >保 存</el-button
          >
        </el-col>
        <el-col v-if="buttonIsShow('submit')" :span="1.5">
          <el-button
            icon="el-icon-position"
            :loading="buttonLoading"
            @click="handleSubmit"
            :disabled="buttonIsDisabled('submit') || !submitBtnIsShow"
            >提 交</el-button
          >
        </el-col>
        <PushDownBtn
          v-if="
            buttonIsShow('push-down') &&
            showPushDownBtn &&
            detailData.pactStateDesc == '通过'
          "
          :buttonLoading="buttonLoading"
          :disabled="buttonIsDisabled('push-down') || !detailData.id"
          :functionName="functionName"
          :detailData="detailData"
        ></PushDownBtn>
        <el-col :span="1.5">
          <CheckBtn :disabled="buttonIsDisabled('check')" :form="form" />
        </el-col>
        <el-col
          v-if="cancelBtnIsShow && buttonIsShow('business-operation')"
          :span="1.5"
        >
          <BusinessOperationBtn
            v-if="isOpenCancelBtn"
            :disabled="buttonIsDisabled('business-operation')"
            :functionName="functionName"
            :showTableHeaderBtn="true"
            :hiddenButtonList="hiddenButtonList"
            :multiDataList="[form]"
          />
        </el-col>
        <!-- <el-col v-if="isOpenDocStatus && buttonIsShow('status')" :span="1.5">
          <div class="tag-group">
            <el-tag :type="currentStatus.type" effect="dark" size="medium">
              {{ form.pactStateDesc }}
            </el-tag>
          </div>
        </el-col> -->
        <el-col :span="1.5"> | </el-col>
        <el-col v-if="buttonIsShow('rollback')" :span="1.5">
          <el-button
            icon="el-icon-refresh-right"
            :loading="buttonLoading"
            @click="rollbackForm"
            :disabled="buttonIsDisabled('rollback') || !rollBackBtnIsShow"
            >撤 销</el-button
          >
        </el-col>

        <el-col v-if="buttonIsShow('deleted')" :span="1.5">
          <el-button
            :disabled="buttonIsDisabled('deleted') || !deletedBtnIsShow"
            size="mini"
            icon="el-icon-delete"
            @click="handleDelete"
            >删 除</el-button
          >
        </el-col>
        <template v-if="isBusinessDoc">
          <el-col v-if="buttonIsShow('relation-query')" :span="1.5">
            <el-button
              icon="el-icon-help"
              size="mini"
              @click="handleRelationQuery"
              :disabled="
                buttonIsDisabled('relation-query') || !relationQueryIsShow
              "
              >联查
            </el-button>
          </el-col>
          <!-- <el-col :span="1.5">
        <el-button size="mini" icon="el-icon-download" @click="handleExport"
          >导出</el-button
        >
      </el-col> -->
          <el-col v-if="buttonIsShow('print-template')" :span="1.5">
            <!-- 动态生成按钮 -->
            <el-select
              :disabled="buttonIsDisabled('print-template') || !isShow"
              v-if="printButtonList.length > 0"
              size="mini"
              placeholder="打印模版"
              @change="handlePrintSelect"
              value="打印模版"
              class="print-select"
            >
              <el-option
                v-for="item in printButtonList"
                :key="item.id"
                :label="item.buttonName"
                :value="item"
              ></el-option>
            </el-select>
          </el-col>
        </template>
      </el-row>
      <div
        v-if="isOpenDocStatus && buttonIsShow('status')"
        style="position: absolute; top: 60px; right: 50px; opacity: 0.4"
      >
        <div :class="['seal', sealColorClass]">
          <div :class="['seal-status', sealStatusClass]">
            {{ form.pactStateDesc }}
          </div>
        </div>
      </div>
    </div>
    <div :class="notShowFormHeader ? '' : 'mt-70'">
      <slot name="form"></slot>
    </div>

    <!-- 联查弹窗 -->
    <RelationQuery
      v-if="showRelationQuery"
      :functionName="functionName"
      :showRelationQuery="showRelationQuery"
      @closeDialog="showRelationQuery = false"
      :relationQueryId="relationQueryId"
      :selectedRow="selectedRow"
      :form="form"
      :isFromDetailPage="isFromDetailPage"
      :selectDetailIds="selectDetailIds"
    ></RelationQuery>
  </div>
</template>


<script>
import {
  save,
  getDetails,
  submitData,
  deleteById,
  getPrintButton,
  rollBackData,
  getInit,
  getCopyData,
  getRelevancyCopyData,
  pushDown,
  stagingData,
} from "@/api/system/common";
import { isEqual, cloneDeep } from "lodash";
import { mapGetters, mapState } from "vuex";
import qs from "qs";
import PushDownBtn from "@/components/PushDownBtn";
import { relationQueryList } from "@/utils/variables";
import BusinessOperationBtn from "../DynamicList/BusinessOperationBtn.vue";
import { checkPermi, checkRole } from "@/utils/permission"; // 权限判断函数
import CheckBtn from "@/components/Btn/CheckBtn/index.vue";
// 储存保存前的数据
let oldForm = null;
export default {
  name: "FormButtons",
  inject: ["notShowFormHeader"],
  components: {
    PushDownBtn,
    RelationQuery: () => import("@/components/RelationQuery/index.vue"),
    BusinessOperationBtn,
    CheckBtn,
  },
  props: {
    // 需要保存以及显示的form数据
    form: {
      type: Object,
      required: true,
    },
    // 显示按钮list
    // ['add','copy','save','submit','rollback','deleted','print-template','status']
    hiddenButtonList: {
      type: Array,
      default: () => [],
    },
    // 禁用按钮
    // ['add','copy','save','submit','rollback','deleted','print-template','status']
    disabledButtonList: {
      type: Array,
      default: () => [],
    },
    // 提交form之间调用的方法
    beforeAddForm: {
      type: Function,
      default: () => true,
    },
    // 提交之前执行的方法
    beforeSubmit: {
      type: Function,
      default: async () => {
        return true;
      },
    },
    // 是否打开单据状态
    isOpenDocStatus: {
      type: Boolean,
      default: () => true,
    },
    // 校验重复字段
    repeatField: {
      type: Array,
      default: () => [
        // {
        //   sonSheet: "b_cust_bank_info",
        //   paneName: "银行信息",
        //   sonFields: [
        //     { label: "银行账号", key: "account" },
        //     { label: "账户名称", key: "accountName" },
        //   ],
        // },
      ],
    },
    // 变更表单的functionName
    changeOrderFN: {
      type: String,
      default: () => null,
    },
    // form表单禁用
    toggleFormDisable: {
      type: Function,
      default: () => {},
    },
    // 子表校验是否大于0
    greaterZeroFieldList: {
      type: Array,
      default: () => [],
    },
    // 子表必填校验
    requiredCheckFieldList: {
      type: Array,
      default: () => [],
    },
    isOpenPreview: {
      type: Boolean,
      default: () => false,
    },
    // 需要校验的表
    // checkSonFieldList: [
    //     {
    //       sonSheet: "sal_cont_goods",
    //       sonName: "物料明细",
    //       sonFields: [
    //          { label: "原料明细", key: "rawMatId",required:true,greaterZero:true,repeat:true,notEmpty:true, },
    //       ],
    //       // children: [{}],
    //     },
    //   ],
    checkSonFieldList: {
      type: Array,
      default: () => [],
    },
    // 点击提交前校验
    beforeRealSubmit: {
      type: Function,
      default: () => true,
    },
    //
    customizeTitle: {
      type: String,
      default: () => null,
    },
    // 需要额外校验的表 当存在多个form需要校验时，只会校验最下方的表，导致其它form的表单校验失效
    extraForms: {
      type: Array,
      default: () => [],
    },
    // 提交提示
    submitConfirmHTML: {
      type: String,
      default: () => '是否<span style="color: #28cd6b;">提交</span>数据',
    },
    // 是否打开保存提示框
    openConfirmDialog: {
      type: Boolean,
      default: () => false,
    },
    // 提交后方法
    afterSubmit: {
      type: Function,
      default: () => {},
    },
  },
  data() {
    return {
      isFromDetailPage: true,
      selectDetailIds: [],
      isReadOnly: false, // 控制表单是否只读
      // 选择的打印模版
      selectedItem: "",
      // 按钮loading
      buttonLoading: false,
      // 模块名称
      functionName: "",
      // 当前表单的id
      id: null,
      // 页面打开模式
      mode: "show",
      // 打印模版列表
      printButtonList: [],
      // 表单状态与显示颜色对照
      documentStatusList: [
        { type: "info", label: "制单中", status: "CREATE", value: 1 },
        { type: "success", label: "通过", status: "PASS", value: 3 },
        { type: "danger", label: "不通过", status: "REJECT", value: 4 },
        { type: "warning", label: "审核中", status: "AUDIT", value: 2 },
        { type: "warning", label: "草稿", status: "DRAFT", value: 0 },
      ],
      // 保存表单状态
      currentStatus: { type: "info", label: "制单中", status: "CREATE" },
      // 表单标题
      title: "",
      isChange: false,
      diffArr: [],
      // 下推
      originalFn: "",
      pushDownParams: {},
      // id对应的明细数据
      detailData: {},
      // 显示下推按钮的函数
      shwoPushDownFns: [],
      // 展示联查弹窗
      showRelationQuery: false,
      relationQueryId: "",
      // 当前选中行
      selectedRow: "",
      // 联查列表
      relationQueryList: relationQueryList,
      // 按钮组是否显示
      showButtonGroup: false,
      // 页面权限字符串
      perms: null,
      // 所有按钮权限字符串
      allBtnPermsStrList: this.variables.allBtnPermsStrList,
      erpCode: "",
      isMobile: false,
      pactState: "",
      readonly: "",
      pactStateDesc: "",
    };
  },
  provide() {
    return {
      getFormData: this.getFormData,
    };
  },
  created() {
    window.addEventListener("resize", this.checkDeviceType); // 添加事件监听
    // 初始化获取数据
    this.init();
    // 展示头部组件，才调用方法
    if (!this.notShowFormHeader) {
      // 获取打印模版列表
      this.getPrintButtonList();
    }
    // 更新标题
    this.updateTitle();
    // 判断是否开启按钮权限
    if (this.$route.meta?.isOpenBtnPerms == 1) {
      // 获取页面权限字符串
      this.perms = this.$route.meta.perms;
    }
  },
  // 在Vue组件的mounted钩子中添加事件监听
  mounted() {
    window.addEventListener("beforeunload", this.showConfirmation, false);
  },

  // 在beforeDestroy钩子中移除事件监听
  beforeDestroy() {
    window.removeEventListener("beforeunload", this.showConfirmation, false);
    window.removeEventListener("resize", this.checkDeviceType);
  },
  computed: {
    ...mapState(["permission"]),
    checkDeviceType() {
      // 根据屏幕宽度判断是否为移动设备
      this.isMobile = window.innerWidth <= 768;
    },
    sealColorClass() {
      switch (this.form.pactState) {
        case 1:
          return "seal-info";
        case 2:
          return "seal-primary";
        case 3:
          return "seal-success";
        case 4:
          return "seal-red";
        case 0:
          return "seal-draft";
        default:
          return "seal-blue"; // 默认颜色
      }
    },
    sealStatusClass() {
      switch (this.form.pactState) {
        case 1:
          return "seal-status-info";
        case 2:
          return "seal-status-primary";
        // 添加更多状态和对应的类
        case 3:
          return "seal-status-success";
        case 4:
          return "seal-status-red";
        case 0:
          return "seal-status-draft";
        // 添加更多状态和对应的类
        default:
          return "seal-status-blue"; // 默认颜色
      }
    },
    // 计算属性的 getter
    isShow: function () {
      return this.mode != "add";
    },
    // 暂存表单
    stagingBtnIsShow: function () {
      console.log(this.currentStatus, "this.currentStatus 666");

      return this.currentStatus.status == "DRAFT";
    },
    saveBtnIsShow: function () {
      return (
        this.currentStatus.status == "CREATE" ||
        this.currentStatus.status == "DRAFT" ||
        this.currentStatus.status == "REJECT"
      );
    },
    submitBtnIsShow: function () {
      return (
        (this.form.id && this.currentStatus.status == "CREATE") ||
        this.currentStatus.status == "REJECT"
      );
    },
    rollBackBtnIsShow: function () {
      // admin 可以在审核中撤销单据
      return (
        this.currentStatus.status == "PASS" ||
        this.currentStatus.status == "AUDIT"
        // (checkRole(["admin"]) ? this.currentStatus.status == "AUDIT" : false)
      );
    },
    deletedBtnIsShow: function () {
      if (!this.form?.id) {
        return false;
      }
      return (
        this.currentStatus.status == "CREATE" ||
        this.currentStatus.status == "DRAFT" ||
        this.currentStatus.status == "REJECT"
      );
    },
    //  显示下推按钮
    showPushDownBtn() {
      const { functionName } = this.$route.query;
      console.log("functionName :>> ", functionName);
      return this.shwoPushDownFns.includes(functionName);
    },
    //  显示审批按钮
    // 联查按钮显示
    relationQueryIsShow() {
      return (
        this.currentStatus.status == "CREATE" ||
        this.currentStatus.status == "PASS" ||
        this.currentStatus.status == "REJECT" ||
        this.currentStatus.status == "AUDIT"
      );
    },
    // 取消按钮显示
    cancelBtnIsShow() {
      const isShow = this.currentStatus.status == "PASS";
      this.$store.dispatch("form/changeCurrentStatus", this.currentStatus);
      return isShow;
    },
    // 是否打开取消按钮
    isOpenCancelBtn() {
      if (
        this.variables.cancelBtnFunctionNameList.includes(this.functionName)
      ) {
        return true;
      } else {
        return false;
      }
    },
    // 是否打开关闭按钮
    isOpenClosedBtn() {
      return this.variables.closedBtnFunctionNameList.includes(
        this.functionName
      );
    },
    // 是否是业务单据
    isBusinessDoc() {
      return this.$route.meta.isBusinessDoc == 1;
    },
  },
  watch: {
    "form.pactStateDesc": function (newVal, oldVal) {
      if (newVal == "通过") {
        this.updateTitle(newVal);
      } else {
        this.updateTitle();
      }
    },
    "form.readonly": function (newVal, oldVal) {
      if (newVal == 1) {
        this.$emit("toggleFormDisable", true);
      } else {
        this.$emit("toggleFormDisable", false);
      }
    },
    "form.pactState": function (newVal, oldVal) {
      // console.log("form.status changed from", oldVal, "to", newVal);
      // 在这里可以执行其他操作
      let item = this.documentStatusList.find(
        (item) => item.value == (newVal || 0)
      );
      console.log("item 111", item);
      if (!item) {
        item = {
          type: "info",
          label: this.form.pactStateDesc,
          status: this.form.pactState,
        };
      }
      this.currentStatus = item;
      // 制单中、审核通过都只能查看
      // if (newVal == 3 || newVal == 2) {
      //   this.$emit("toggleFormDisable", true);
      // } else {
      //   this.$emit("toggleFormDisable", false);
      // }
    },
    "form.id": function (newVal, oldVal) {
      // console.log("formId", newVal);
      this.id = newVal;
    },
    "form.erpCode": function (newVal, oldVal) {
      // console.log("formId", newVal);
      this.erpCode = newVal;
    },
    "form.erpId": function (newVal, oldVal) {
      // console.log("formId", newVal);
      this.erpId = newVal;
    },
    mode: function (newVal, oldVal) {
      // console.log("mode", newVal);
      this.$emit("mode-change", newVal);
    },
    // 监听页面权限字符串，生成按钮权限
    perms(newVal, oldVal) {
      if (newVal == null || this.openMode == "dialog") {
        return;
      }
      this.allBtnPermsStrList.forEach((item) => {
        const perms = `${newVal}:${item}`;
        if (!this.checkPermi([perms])) {
          this.hiddenButtonList.push(item);
        }
      });
      this.$store.dispatch("form/hiddenButtonList", this.hiddenButtonList);
    },
  },
  methods: {
    checkPermi,
    checkRole,
    // 点击了打印按钮
    handlePrintSelect(item) {
      if (
        item.buttonName == "总厂箱单打印" ||
        item.buttonName == "总厂码单打印" ||
        item.buttonName == "总厂箱单打印(三重量)"
      ) {
        if (this.erpCode) {
          window.open(
            item.linkUrl + "&fbillno=" + this.erpCode + "&__pi__=false",
            "_blank"
          );
        }
      } else if (item.buttonName == "厚源箱单打印") {
        if (this.erpId) {
          window.open(item.linkUrl + "&fid=" + this.erpId, "_blank");
        }
      } else if (item.buttonName == "厚源码单打印") {
        if (this.erpId) {
          window.open(
            item.linkUrl + "&fid=" + this.erpId + "&flx=XSCKD&__pi__=false",
            "_blank"
          );
        }
      } else {
        window.open(item.linkUrl + "&pact_id=" + this.id, "_blank");
      }
    },
    init() {
      // this.form = {};
      this.id = null;
      let { functionName, id, mode, detailId } = this.$route.query;

      this.functionName = functionName;
      this.id = id;
      // console.log("mode", mode);
      this.mode = mode;
      if (mode == "show") {
        this.$emit("setReaderOnly", true);
      } else {
        this.$emit("setReaderOnly", false);
      }
      // console.log("init", mode);

      if (mode != "add") {
        if (mode == "copy") {
          this.getFormCopyData(functionName, id, detailId);
        } else if (mode == "copyAll") {
          this.getFormCopyAllData(functionName, id, detailId);
        } else if (mode == "change") {
          // console.log("this.changeOrderFN", this.changeOrderFN);
          this.getChangeOrderData();
        } else if (mode == "pushDown") {
          let obj = qs.parse(this.$route.query);
          // 数组参数需要解析
          this.originalFn = obj.originalFn;
          this.pushDownParams = obj.pushDownParams;
          this.pushDownParams.forEach((el) => {
            if (!Array.isArray(el.detailsId)) {
              el.detailsId = [el.detailsId];
            }
          });
          this.getPushDownData();
        } else {
          this.getData("init");
        }
      } else {
        this.getInitData();
      }
    },
    getInitData() {
      getInit(this.functionName)
        .then((res) => {
          if (res?.data) {
            let dataOne = res.data;
            this.$emit("setForm", { ...dataOne }, true);
          }
        })
        .finally(() => {
          this.showButtonGroup = true;
        });
    },
    getFormCopyData(functionName, id, detailId) {
      // console.log("deleteById", detailId);
      if (detailId) {
        // 判断传入的参数是否为数组
        if (!Array.isArray(detailId)) {
          // 如果不是数组，将其转换为包含单个元素的数组
          detailId = [detailId];
        }
        detailId = { detailId };
      }
      getCopyData(functionName, id, detailId)
        .then((res) => {
          console.log("复制数据：", res.data);
          this.$emit("setForm", res.data);
        })
        .finally(() => {
          this.showButtonGroup = true;
        });
    },
    getFormCopyAllData(functionName, id, detailId) {
      // console.log("deleteById", detailId);
      if (detailId) {
        // 判断传入的参数是否为数组
        if (!Array.isArray(detailId)) {
          // 如果不是数组，将其转换为包含单个元素的数组
          detailId = [detailId];
        }
        detailId = { detailId };
      }

      getRelevancyCopyData(functionName, id, detailId)
        .then((res) => {
          // console.log("复制数据：", res.data);
          this.$emit("setForm", res.data);
        })
        .finally(() => {
          this.showButtonGroup = true;
        });
    },
    /** 暂存按钮 */
    async stagingForm() {
      this.$modal
        .confirmHTML(
          '是否<span style="color: #28cd6b;">暂存</span>数据',
          "确认"
        )
        .then(() => {
          this.buttonLoading = true;
          console.log(this.form, "777");
          console.log("【第二条打印】你当前暂存的数据form:", this.form);
          const data = { data: this.form };

          stagingData(this.functionName, data)
            .then((res) => {
              console.log(this.functionName, res.code);
              this.$modal.msgSuccess("暂存成功");
              // 列表回显
              this.updateList();
              this.$emit("setForm", res.data);
              this.openPage("edit", res.data.id);
              this.detailData = res.data;
            })
            .finally(() => {
              this.buttonLoading = false;
            });
        })
        .catch((err) => {
          console.log("err :>> ", err); // this.$modal.msgSuccess("取消提交", err);
        });
    },
    /** 保存按钮 */
    async saveForm() {
      this.$modal
        .confirmHTML(
          '是否<span style="color: #28cd6b;">保存</span>数据',
          "确认"
        )
        .then(async () => {
          if (this.beforeSubmit) {
            let flag = await this.beforeSubmit(this.form);
            // 若保存前校验不通过 不能往下执行
            if (!flag) return;
          }

          // 额外表单校验
          if (this.extraForms.length) {
            let isAllValidForm = await this.validateExtraForms();
            if (!isAllValidForm) {
              this.$message.error("表单校验未通过");
              return;
            }
          }
          console.log("valid",  this.$parent.$refs)

          this.$parent.$refs["form"].validate(async (valid) => {
            if (valid) {
              const isAdd = this.form.id == null;
              this.buttonLoading = true;
              const data = { data: this.form };
              console.log("我当前保存的this.form的一级属性,",this.form);

              if (isAdd) {
                this.beforeAddForm && this.beforeAddForm(data);
              }
              // 后端自动生成对应的时间，后端接收会报错
              // let deleteKeys = ["createTime",  "auditTime"];
              // deleteKeys.forEach((key) => this.form[key] && delete this.form[key]);
              // console.log(this.repeatField, "🚀🚀🚀🚀🚀🚀");

              // 多个选择框 重复校验
              if (this.repeatField.length) {
                for (let i = 0; i < this.repeatField.length; i++) {
                  let targetSheet = this.repeatField[i].sonSheet;
                  let sunSheet = this.repeatField[i]?.sunSheet;
                  let targetPane = this.repeatField[i].paneName;
                  let targetField = this.repeatField[i].sonFields;
                  let targetRequired = this.repeatField[i].isRequired;

                  // 表为空时 不让提交
                  if (
                    (!this.form[targetSheet] ||
                      !this.form[targetSheet].length) &&
                    targetRequired
                  ) {
                    this.buttonLoading = false;
                    return this.$message.warning(
                      `${targetPane}表格为空，请填写值后再提交`
                    );
                  }
                  // 删除空行
                  // this.deleteEmptyRow(targetSheet, "", sunSheet);

                  if (this.form[targetSheet] && this.form[targetSheet].length) {
                    // 校验子表重复字段
                    let sonAllValues = {};
                    this.form[targetSheet].forEach((el) => {
                      targetField.forEach(({ key, label }) => {
                        if (!sonAllValues[label]) sonAllValues[label] = [];
                        sonAllValues[label].push(el[key]);
                      });
                    });
                    let sonUniqueValues = {};
                    Object.keys(sonAllValues).forEach(
                      (key) =>
                        (sonUniqueValues[key] = [...new Set(sonAllValues[key])])
                    );

                    let repeatLabels = [];
                    Object.keys(sonAllValues).forEach((key) => {
                      if (
                        sonAllValues[key].length != sonUniqueValues[key].length
                      ) {
                        repeatLabels.push(key);
                      }
                    });
                    if (repeatLabels.length > 0) {
                      this.buttonLoading = false;
                      return this.$message.warning(
                        `${targetPane} - ${repeatLabels.join(
                          "，"
                        )} 存在重复数据，请核对后再提交`
                      );
                    }
                  }
                }
              }
              // console.log(this.greaterZeroFieldList.length);
              // 子表校验是否大于0
              if (this.greaterZeroFieldList.length > 0) {
                let errMsg = "";
                // 遍历需要校验的数组
                this.greaterZeroFieldList.forEach((sonObj, index) => {
                  // 判断是否存在对应子表
                  if (this.form[sonObj.sonSheet]) {
                    // 遍历当前表需要校验的字段
                    this.greaterZeroFieldList[index].sonFields.forEach(
                      (sonField) => {
                        // 遍历子表list
                        this.form[sonObj.sonSheet].forEach((sonItem, i) => {
                          // 校验填入的是否为数字
                          // let isNumber =
                          //   !isNaN(parseFloat(sonItem[sonField.key])) &&
                          //   isFinite(sonItem[sonField.key]);
                          // if (!isNumber) {
                          //   return (errMsg = `第${i + 1}行${
                          //     sonField.label
                          //   }字段填入的不是数字，请重新填写后再提交`);
                          // }
                          // 字段小于0就报错
                          if (sonItem[sonField.key] <= 0) {
                            return (errMsg = `第${i + 1}行${
                              sonField.label
                            }字段需要大于0，请重新填写后再提交`);
                          }
                        });
                      }
                    );
                  }
                });
                this.buttonLoading = false;
                if (errMsg != "") {
                  return this.$message.warning(errMsg);
                }
              }
              // 子表必填校验
              if (this.requiredCheckFieldList.length > 0) {
                let errMsg = "";
                // 遍历需要校验的数组
                this.requiredCheckFieldList.forEach((sonObj, index) => {
                  // 判断是否存在对应子表
                  if (this.form[sonObj.sonSheet]) {
                    // 遍历当前表需要校验的字段
                    this.requiredCheckFieldList[index].sonFields.forEach(
                      (sonField) => {
                        // 遍历子表list
                        this.form[sonObj.sonSheet].forEach((sonItem, i) => {
                          // console.log(sonField.label, sonItem[sonField.key]);
                          // 字段不存在并且为空字符串就报错
                          if (
                            sonItem[sonField.key] &&
                            sonItem[sonField.key] == ""
                          ) {
                            return (errMsg = `第${i + 1}行${
                              sonField.label
                            }字段不能为空，请重新填写后再提交`);
                          }
                        });
                      }
                    );
                  }
                });
                this.buttonLoading = false;
                if (errMsg != "") {
                  return this.$message.warning(errMsg);
                }
              }
              save(this.functionName, data)
                .then((res) => {
                  console.log(this.functionName, res.code);
                  if (this.openConfirmDialog && res.code == 206) {
                    // 弹出提示框
                    this.confirmDialog(res.msg, data, isAdd);
                  } else {
                    if (isAdd) {
                      this.$modal.msgSuccess("新增成功");
                    } else {
                      this.$modal.msgSuccess("保存成功");
                    }
                    // 列表回显
                    this.updateList();
                    this.$emit("setForm", res.data);
                    this.openPage("edit", res.data.id);
                    this.detailData = res.data;
                  }
                })
                .catch((err) => {
                  console.log("save error:", err);
                  this.$message.error("保存失败");
                })
                .finally(() => {
                  this.buttonLoading = false;
                });
            } else {
              // 获取详细的错误信息
              const errField = this.$parent.$refs.form.fields.find(
                (field) => field.validateState === "error"
              );
              return this.$message.error(`${errField.validateMessage}`);
              // this.$message.error("表单校验未通过");
            }
          });
        })
        .catch((err) => {
          console.log("err :>> ", err);
          // this.$modal.msgSuccess("取消提交", err);
        });
    },

    // 大于0
    greaterZero(sonObj) {
      let msg = "";
      sonObj.sonFields.forEach((sonField) => {
        // 遍历子表list
        this.form[sonObj.sonSheet].some((sonItem, i) => {
          // 字段存在并且小于0就报错
          if (sonItem[sonField.key] && sonItem[sonField.key] <= 0) {
            msg = `第${i + 1}行${
              sonField.label
            }字段需要大于0，请重新填写后再提交`;
            return;
          }
        });
      });
      return msg;
    },
    // 必填
    requiredCheck(sonObj) {
      let msg = "";
      sonObj.sonFields.forEach((sonField) => {
        // 遍历子表list
        this.form[sonObj.sonSheet].some((sonItem, i) => {
          // 字段不存在并且为空字符串就报错
          if (sonItem[sonField.key] && sonItem[sonField.key] == "") {
            msg = `第${i + 1}行${
              sonField.label
            }字段不能为空，请重新填写后再提交`;
            return;
          }
        });
      });
      return msg;
    },
    // 删除空行
    deleteEmptyRow(sonTableName, grandsonTableName, sunTableName) {
      // console.log(sunTableName);
      // console.log("🚀🚀🚀🚀🚀🚀🚀🚀🚀🚀🚀🚀🚀🚀🚀🚀🚀🚀");
      if (sunTableName) {
        this.form[sonTableName] = this.form[sonTableName].filter((el) => {
          const keys = Object.keys(el);
          const hasSortIndex = keys.includes("sortIndex");
          const hasSunTableName = keys.includes(sunTableName);
          // console.log(111);
          // 保留的行：不满足以下条件的行
          // - 只有一个键且该键是 "sortIndex"
          // - 或者具有两个键（一个是 "sortIndex"，另一个是 `sunTableName`），且 `sunTableName` 对应的值是一个空数组
          return (
            !(keys.length === 1 && hasSortIndex) &&
            !(
              keys.length === 2 &&
              hasSortIndex &&
              hasSunTableName &&
              Array.isArray(el[sunTableName]) &&
              el[sunTableName].length === 0
            )
          );
        });
      } else {
        // console.log(222);
        this.form[sonTableName] = this.form[sonTableName].filter((el) => {
          return (
            Object.keys(el).length != 1 ||
            !Object.keys(el).includes("sortIndex")
          );
        });
      }

      // console.log("deleteEmptyRow", this.form[sonTableName]);
    },
    // 是否重复
    isRepeatField(sonObj) {
      let msg = "";
      sonObj.sonFields.some((sonField) => {
        let sonFieldSet = new Set();
        // 遍历子表list
        this.form[sonObj.sonSheet].forEach((sonItem, i) => {
          sonFieldSet.add(sonItem[sonField.key]);
        });
        // console.log(
        //   "isRepeatField",
        //   sonFieldSet,
        //   sonFieldSet.size,
        //   this.form[sonObj.sonSheet].length
        // );
        if (sonFieldSet.size != this.form[sonObj.sonSheet].length) {
          msg = `${sonObj.sonName}表，${sonField.label}字段存在重复项`;
          return;
        }
      });
      return msg;
    },
    /** 提交按钮 */
    submitForm() {
      this.$parent.$refs["form"].validate((valid) => {
        if (valid) {
          // console.log("valid", valid);
          this.buttonLoading = true;
          const data = { data: this.form };
          submitData(this.functionName, this.form.id, data)
            .then((res) => {
              if (this.openConfirmDialog && res.code == 206) {
                // 弹出提示框
                this.submitConfirmDialog(res.msg, this.form.id, data);
              } else {
                this.getData();
                // 列表回显
                this.updateList();

                this.loading = false;
                this.afterSubmit();
                this.$modal.msgSuccess("提交成功");
              }
            })
            .catch((err) => {
              console.log("err :>> ", err);
              // this.$modal.msgError("提交失败", err);
            })
            .finally(() => {
              this.loading = false;
              this.buttonLoading = false;
            });
        } else {
          this.$message.error("表单校验未通过");
        }
      });
    },
    /** 检查反审核权限（CRM 专属硬编码校验已移除，统一走权限模型） */
    checkRollbackPermission() {
      return false;
    },

    /** 反审核按钮 */
    rollbackForm() {
      // 权限校验
      if (this.checkRollbackPermission()) {
        this.$modal.msgError("您没有权限执行此操作");
        return;
      }
      this.$modal
        .confirmHTML(
          '是否<span style="color: #28cd6b;">撤销</span>数据',
          "确定"
        )
        .then(() => {
          this.buttonLoading = true;
          const data = { data: this.form };
          rollBackData(this.functionName, this.form.id)
            .then((res) => {
              if ((this.functionName, this.form.id != null)) {
                this.$modal.msgSuccess("反审核成功");
                // this.$emit("setForm", res.data);
                // isRollBack = true
                this.getData(null, true);
                // 列表回显
                this.updateList();
              }
            })
            .finally(() => {
              this.buttonLoading = false;
            });
        })
        .catch((err) => {
          console.log("err :>> ", err); // this.$modal.msgSuccess("取消提交", err);
        });
    },
    updateTitle(newVal) {
      let title = this.$route.meta.title;
      const mode = this.$route.query.mode;
      if (newVal) {
        title = title + "-" + newVal;
        document.title = title;
        this.title = title;
        return;
      }
      if (mode == "add" || mode == "pushDown") {
        title = title + "-新增";
      } else if (mode == "edit") {
        title = title + "-编辑";
      } else if (mode == "change") {
        title = title + "-新增";
      } else {
        title = title + "-查看";
      }
      // 修改页面标题
      document.title = title;
      this.title = title;
    },


    getData(desc = "", isRollBack = false) {
      console.log(12);

      if (this.id) {
        getDetails(this.functionName, this.id)
          .then((res) => {
            console.log("当前不是add,copy根据也query的id去获取主表需要的数据",res);
            this.$emit("setForm", res.data, false, desc);
            this.detailData = res.data;
          })
          .finally(() => {
            this.showButtonGroup = true;
          });
      }
    },
    // 获取变更单原单数据
    getChangeOrderData() {
      if (this.id) {
        getDetails(this.changeOrderFN, this.id)
          .then((res) => {
            // 变更过来的单据需要将单据的状态变为制单中
            res.data.pactState = 0;
            res.data.pactStateDesc = "草稿";
            res.data.readonly = 0;
            let aData = {
              ...res.data,
              pactState: 0,
              pactStateDesc: "草稿",
              readonly: 0,
            };
            this.$emit("setForm", aData, true);
          })
          .finally(() => {
            this.showButtonGroup = true;
          });
      }
    },
    getPrintButtonList() {
      getPrintButton(this.functionName).then((res) => {
        // this.form = res.data;
        this.printButtonList = res.data;
      });
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.openPage("add");
      this.init();
    },
    /** 修改按钮操作 */
    handleUpdate() {
      // this.openPage("edit");
      // this.init();
      this.mode = "edit";
      this.$emit("setReaderOnly", false);
    },
    openPage(mode, id, detailId) {
      this.mode = mode;
      let modeMap = {
        add: "新增",
        edit: "编辑",
        show: "查看",
        copy: "复制",
        copyAll: "关联复制",
      };
      let str = modeMap[mode];
      // 打开页签
      let { path, query, meta } = this.$route;
      const params = {
        functionName: query.functionName,
        mode,
        id: id || this.id,
      };
      if (mode == "add") {
        // delete params.id;
        return window.open(
          `${path}?functionName=${query.functionName}&mode=add`,
          "_blank"
        );
      }
      // console.log("params", params);
      // console.log("query", this.$route);
      this.$tab.openPage(`${meta.title}-${str}`, path, params);
    },
    handleCopy() {
      console.log("点击了复制按钮", this.id);
      this.$modal
        .confirmHTML(
          '是否<span style="color: #28cd6b;">复制</span>数据',
          "确认"
        )
        .then(() => {
          this.openPage("copy", this.id);
          this.getFormCopyData(this.functionName, this.id, null);
        })
        .catch((err) => {
          console.log("err :>> ", err); // this.$modal.msgSuccess("取消提交", err);
        });
    },
    handleCopyAll() {
      console.log("点击了关联复制", this.id);
      this.$modal
        .confirmHTML(
          '是否<span style="color: #28cd6b;">关联复制</span>数据',
          "确认"
        )
        .then(() => {
          this.openPage("copyAll", this.id);
          this.getFormCopyAllData(this.functionName, this.id, null);
        })
        .catch((err) => {
          console.log("err :>> ", err); // this.$modal.msgSuccess("取消提交", err);
        });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const id = this.id;
      this.$modal
        .confirmHTML('是否确认<span style="color: red;">删除</span>该条数据')
        .then(() => {
          this.loading = true;
          return deleteById(this.functionName, id);
        })
        .then(() => {
          this.loading = false;
          // 列表回显
          this.updateList();
          // 关闭页面
          window.close();
        })
        .catch((err) => {})
        .finally(() => {
          this.loading = false;
        });
    },
    compareDiff(obj, isFromChildArr = false) {
      // 从子/孙组件执行方法时 会清空数组导致存放数据不齐
      !isFromChildArr && (this.diffArr = []);

      Object.keys(obj).forEach((key) => {
        let value = obj[key];
        if (Array.isArray(value)) {
          value.forEach((item) => this.compareDiff(item, true));
        } else {
          if (value != this.oldForm[key]) {
            this.diffArr.push({ key, value, oldVal: this.oldForm[key] });
          }
        }
      });
      // console.log("this.diffArr :>> ", this.diffArr);

      if (
        this.diffArr.length === 1 &&
        this.diffArr[0].key == "regCapital" &&
        Number(this.diffArr[0].value) == Number(this.diffArr[0].oldVal)
      ) {
        return true;
      } else {
        return false;
      }
    },
    // 提交按钮操作
    handleSubmit() {
      const isChange = !isEqual(this.oldForm, cloneDeep(this.form));
      // 用户修改了数据，则先保存，才能提交
      if (isChange) {
        const h = this.$createElement;
        this.$msgbox({
          title: "提示",
          message: h("p", null, [
            h("span", null, "当前页面数据有修改，请先保存"),
          ]),
          confirmButtonText: "确定",
        })
          .then((res) => {})
          .catch((err) => {});
      } else {
        // 提交前校验
        let flag = this.beforeRealSubmit();
        if (flag) {
          const id = this.id;
          if (
            this.submitConfirmHTML ==
            '是否<span style="color: #28cd6b;">提交</span>数据'
          ) {
            this.loading = true;
            return this.submitForm();
          } else {
            this.$modal
              .confirmHTML(this.submitConfirmHTML, "提交")
              .then(() => {
                this.loading = true;
                return this.submitForm();
              })
              .catch((err) => {
                console.log("err :>> ", err);
                this.$modal.msgSuccess("取消提交", err);
              });
          }
        }
      }
    },
    // 根据传入的name，判断按钮是否显示
    buttonIsShow(name) {
      return !this.hiddenButtonList.includes(name);
    },
    // 根据传入的name，判断按钮是否显示
    buttonIsDisabled(name) {
      return this.disabledButtonList.includes(name);
    },
    updateList(functionName) {
      // 在页面A中设置localStorage的值
      // console.log("needUpdatedFunctionName");
      localStorage.setItem("needUpdatedFunctionName", "");
      localStorage.setItem("needUpdatedFunctionName", functionName || this.functionName);
    },
    // 设置oldForm
    setOldForm(oldForm) {
      this.oldForm = cloneDeep(oldForm);
      // console.log("this.oldForm", this.oldForm);
    },
    showConfirmation(event) {
      // console.log(event);
      if (!isEqual(this.oldForm, cloneDeep(this.form))) {
        const message = "你确定要离开吗？";
        event.returnValue = message; // 标准的跨浏览器兼容性处理
        return message; // 为了某些浏览器的兼容性而返回信息
      }
    },
    // 额外表单校验
    async validateExtraForms() {
      let allPromise = [];

      this.extraForms.forEach((formName) => {
        let validateForm = new Promise((resolve, reject) => {
          this.$parent.$refs[formName].validate((valid) => {
            if (valid) {
              resolve();
            } else {
              reject();
            }
          });
        });

        allPromise.push(validateForm);
      });

      let results = await Promise.allSettled(allPromise);
      // console.log("results :>> ", results);
      let hasInvalid = results.find((el) => el.status == "rejected");
      if (hasInvalid) {
        return false;
      } else {
        return true;
      }
    },
    // 获取下推数据
    getPushDownData() {
      pushDown(this.originalFn, this.functionName, this.pushDownParams)
        .then((res) => {
          if (res?.data) {
            console.log("下推的订单，当前获取的数据res",res);
            console.log("下推的订单，当前获取的数据res.data.isFast",res.data.isFast);
            res.data.readonly = 0;

            this.$emit("setForm", res.data);
            this.detailData = res.data;
            console.log("this.detailData.id :>> ", this.detailData.id);
          }
        })
        .finally(() => {
          this.showButtonGroup = true;
        });
    },
    // 联查
    handleRelationQuery() {
      let multipleSelection = this.$store.state.form.multipleSelection;
      // 当前单据
      let currentPact = this.relationQueryList.find(
        (el) => el.currentPact.function == this.functionName
      );
      let sonSheet = currentPact.currentPact.sonSheet;
      // 在收款单里面没有子表，所以这里需要做一些处理
      if (this.functionName == "FI_RECORD") {
        this.isFromDetailPage = false;
        this.selectedRow = [this.detailData];
        this.relationQueryId = [
          this.detailData.main_id
            ? this.detailData.main_id
            : this.detailData.id,
        ];
        this.selectDetailIds = [
          this.detailData.id ? this.detailData.id : this.detailData.main_id,
        ];
      } else if (this.functionName == "FI_RBILL") {
        this.isFromDetailPage = false;
        this.selectedRow = [this.detailData];
        console.log(this.detailData, " this.selectedRow");

        this.relationQueryId = [
          this.detailData.main_id
            ? this.detailData.main_id
            : this.detailData.id,
        ];
        this.selectDetailIds = [
          this.detailData.id ? this.detailData.id : this.detailData.main_id,
        ];
      } else {
        this.isFromDetailPage = true;
        this.selectedRow = this.detailData[sonSheet];
        // 数据未加载完成（getDetails 未返回）时，不打开弹窗，避免联查条件为空导致全量查询
        if (!this.selectedRow?.length) {
          this.$message.warning("单据数据尚未加载完成，请稍后再点击联查");
          return;
        }
        this.relationQueryId = this.detailData[sonSheet]?.map((el) => el.id);

        // this.selectedRow = multipleSelection;  联查选中的子表数据
        // this.relationQueryId = multipleSelection?.map((el) => el.id); 联查选中的子表数据
      }

      // this.relationQueryId = this.detailData.main_id || this.detailData.id;

      this.showRelationQuery = true;
    },
    // 提示弹窗
    confirmDialog(message, data, isAdd) {
      this.$modal
        .confirmHTML(message)
        .then(() => {
          data.params = { isContinueShipping: "1" };
          save(this.functionName, data).then((res) => {
            console.log(this.functionName, res.code);
            if (res.code == 200) {
              if (isAdd) {
                this.$modal.msgSuccess("新增成功");
              } else {
                this.$modal.msgSuccess("保存成功");
              }
              // 列表回显
              this.updateList();
              this.$emit("setForm", res.data);
              this.openPage("edit", res.data.id);
              this.detailData = res.data;
            } else {
              this.$message.error("保存失败", res.message);
            }
          });
        })
        .then(() => {})
        .catch(() => {})
        .finally(() => {});
    },
    // 提示弹窗
    submitConfirmDialog(message, id, data) {
      this.$modal
        .confirmHTML(message)
        .then(() => {
          data.params = { isContinueShipping: "1" };
          this.loading = true;
          this.buttonLoading = true;
          submitData(this.functionName, id, data)
            .then((res) => {
              if (res.code == 200) {
                this.getData();
                // 列表回显
                this.updateList();
                this.$modal.msgSuccess("提交成功");
              } else {
                this.$message.error("保存失败", res.message);
              }
            })
            .finally(() => {
              this.loading = false;
              this.buttonLoading = false;
            });
        })
        .then(() => {})
        .catch(() => {})
        .finally(() => {});
    },
    // 变更历史按钮获取数据
    getSelectedItem() {
      return this.form;
    },
    // getFormData
    getFormData() {
      return this.form;
    },
  },
};
</script>

<style lang="scss" scoped>
.mobile-header {
  .header {
    height: 90px;
    .header-title {
      display: none;
    }
    .button-group {
      flex-wrap: wrap;
      ::v-deep .el-col {
        line-height: normal;
      }
    }
  }
}
.header {
  position: fixed;
  top: 0;
  left: 0px;
  width: 100%;
  height: 60px;
  z-index: 999;
  background: white;
  padding: 0 3%;
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15) !important;
  display: flex;
  justify-content: space-between;
  align-content: center;
  .button-group {
    display: flex;
    justify-content: center;
    align-content: center;
    ::v-deep .el-col {
      line-height: 60px;
    }
  }
}
.header-title {
  font-size: 20px;
  font-weight: 500;
  height: 100%;
  line-height: 20px;
}

::v-deep {
  .el-form-item.is-required:not(.is-no-asterisk)
    .el-form-item__label-wrap
    > .el-form-item__label:before,
  .el-form-item.is-required:not(.is-no-asterisk) > .el-form-item__label:before {
    display: none;
  }
  .el-form-item.is-required:not(.is-no-asterisk)
    .el-form-item__label-wrap
    > .el-form-item__label:before,
  .el-form-item.is-required:not(.is-no-asterisk) > .el-form-item__label::after {
    content: "*";
    color: #f56c6c;
    margin-left: 4px;
  }
  .el-form-item__label {
    font-size: 12px;
    font-weight: normal;
  }
}
.seal {
  width: 80px;
  height: 80px;
  transform: rotate(-45deg);
  color: #00c293;
  display: grid;
  place-content: center;
  text-align: center;
  font-size: 12px;
  border: 2px dashed #00c293;
  outline: 6px solid #00c293;
  outline-offset: 5px;
  -webkit-border-radius: 50%; /* 用于WebKit内核的浏览器（如旧版Safari、Chrome等） */
  -moz-border-radius: 50%; /* 用于Mozilla内核的浏览器（如旧版Firefox） */
  border-radius: 50%;
  box-shadow: 0 0 5px 5px rgba(180, 180, 180, 0.2);
}
.seal-status {
  font-weight: 900;
  font-size: 16px;
  padding: 2px 5px;
  border: 2px solid #00c293;
  border-radius: 5px;
}
.seal-green {
  color: #00c293;
  border: 2px dashed #00c293;
  outline: 6px solid #00c293;
}

.seal-status-green {
  border-color: #00c293;
}

/* 驳回状态 */
.seal-red {
  color: #ff0000;
  border: 2px dashed #ff0000;
  outline: 6px solid #ff0000;
}

.seal-status-red {
  border-color: #ff0000;
}

/* 其他状态 */
.seal-blue {
  color: #1890ff;
  border: 2px dashed #1890ff;
  outline: 6px solid #1890ff;
}

.seal-status-blue {
  border-color: #1890ff;
}

/* 通过状态*/
.seal-success {
  color: #32cd32;
  border: 2px dashed #32cd32;
  outline: 6px solid #32cd32;
}
.seal-status-success {
  border-color: #32cd32;
}
/* 制单中状态*/
.seal-info {
  color: #bdbdbd;
  border: 2px dashed #bdbdbd;
  outline: 6px solid #bdbdbd;
}
.seal-status-info {
  border-color: #bdbdbd;
}
/* 草稿状态*/
.seal-draft {
  color: #d85e07;
  border: 2px dashed #d85e07;
  outline: 6px solid #d85e07;
}
.seal-status-draft {
  border-color: #d85e07;
}
/* 审核中状态*/
.seal-primary {
  color: #ffa500;
  border: 2px dashed #ffa500;
  outline: 6px solid #ffa500;
}
.seal-status-primary {
  border-color: #ffa500;
}
.mt-70 {
  margin-top: 70px;
}
::v-deep .print-select {
  .el-input__inner {
    width: 100px;
  }
  .el-input__icon {
    height: 28.5px;
  }
  .el-scrollbar__wrap {
    overflow: none;
  }
}
</style>
