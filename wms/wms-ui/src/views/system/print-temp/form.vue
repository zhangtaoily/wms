<!--打印模板-->
<template>
  <div class="app-container">
    <FormButtons
      ref="formButtons"
      @setReaderOnly="setReaderOnly"
      @setForm="setForm"
      @toggleFormDisable="toggleFormDisable"
      :form="form">
      <template slot="form">
        <el-tabs  type="card">

          <el-tab-pane label="基础资料">
            <el-form
            class="form-box"
            ref="form"
            :model = "form"
            :rules="rules"
            label-width="80px"
            :disabled="isFormDisabled"

            >
              <el-form-item label="单据编号" prop="code">
                <el-input
                  v-model="form.code"
                  type="text"
                  placeholder="请输入内容"
                />
              </el-form-item>
              <el-form-item label="中文名称" prop="cnName">
                <el-input v-model="form.cnName" placeholder="请输入" />
              </el-form-item>
              <el-form-item label="英文名称" prop="enName">
                <el-input v-model="form.enName" placeholder="请输入" />
              </el-form-item>
              <el-form-item label="组织" prop="orgId">
                <DynamicDialog
                  :disabled="isFormDisabled"
                  title="组织"
                  functionName="ORG"
                  echoField="cnName"
                  :simpleMode="true"
                  :openRadioSelection="true"
                  v-model="form.orgId"
                  :initValue="form.orgCnName"
                />
              </el-form-item>
              <el-form-item label="是否启用" prop="enabled">
                <el-radio-group
                  v-model="form.enabled"

                >
                  <el-radio
                    v-for="item in radioList"
                    :key="item.value"
                    :label="item.value"
                  >{{ item.label }}</el-radio
                  >
                </el-radio-group>
              </el-form-item>
              <el-form-item label="是否英文打印" prop="isEnPrint">
                <el-radio-group
                  v-model="form.isEnPrint"
                >
                  <el-radio
                    v-for="item in radioList"
                    :key="item.value"
                    :label="item.value"
                  >{{ item.label }}</el-radio
                  >
                </el-radio-group>
              </el-form-item>
              <el-form-item style="width:100%;" label="备注"  prop="remark">
                <el-input
                  v-model="form.remark"
                  type="textarea"
                  placeholder="请输入"
                />
              </el-form-item>

            </el-form>
          </el-tab-pane>

          <el-tab-pane label="其他">
            <OtherForm :form="form" :rules="rules" :dictDataList="dictDataList" />
          </el-tab-pane>
            <el-tab-pane label="接口日志">
              <InterfaceLogList functionName="B_PRINT_TEMP" />
            </el-tab-pane>
        </el-tabs>
      </template>
      <el-tabs type="">
      </el-tabs>
    </FormButtons>

  </div>
</template>

<script>

import {radioList} from "@/utils/variables";
import OtherForm from "@/components/OtherForm/index.vue";

export default {
  name:"print-temp",
  components: {OtherForm},
  dicts :[
    "data_type",
  ],
  comments:{},
  data(){
    return{
      dictDataList:{},
      // 参数预览
      previewJson: {},
      isReadOnly: true, // 控制表单是否只读
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        cnname: [
          {required: true, message: "中文名称不能为空", trigger: ["blur", "change", "input"]},
        ],
        orgId: [
          {required: true, message: "组织不能为空", trigger: "change"},
        ],

      },
      // 按钮loading
      buttonLoading: false,

      radioList: radioList,
      // 标题
      dialogTitle: "自定义标题",
      // 模块名称
      functionName: null,
      id: null,
      mode: "show",
      printButtonList: [],
      isFormDisabled: false,
    }
  },
  created() {},
  computed: {},
  methods: {
    // 设置form的数据
    setForm(form,isInit = false){
      this.form = form;
      this.id = form?.id;
      console.log(this.isFirst);
      // 默认赋值启用
      isInit && (this.form.enabled = 1);
      this.$nextTick(() => {
        console.log("nextTick oldForm", this.form);
        this.$refs.formButtons.setOldForm(this.form);
      });
    },
    setReaderOnly(boolean) {
      this.isReadOnly = boolean;
    },

    // 更改form 是否禁用
    toggleFormDisable(isDisable) {
      console.log("isPreview watch toggleFormDisable", isDisable);
      this.isFormDisabled = isDisable;
      console.log(this.$refs.SubTable);
    },
  },
}

</script>

<style scoped lang="scss">

</style>
