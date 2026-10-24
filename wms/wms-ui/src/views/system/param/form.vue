<!--参数列表-->

<template>
  <div class="app-container">
    <FormButtons
      @setReaderOnly="setReaderOnly"
      @setForm="setForm"
      @toggleFormDisable="toggleFormDisable"
      ref="formButtons"
      :form="form"
    >
      <template slot="form">
        <el-tabs type="border-card">
          <el-tab-pane label="基础资料">
            <el-form
              class="form-box"
              ref="form"
              :model = "form"
              :rules = "rules"
              label-width="80px"
              :disabled = "isFormDisabled"
            >
              <el-form-item label="单据编号" prop="code">
                <el-input
                  v-model="form.code"
                  type="text"
                />
              </el-form-item>
              <el-form-item label="名称" prop="name">
                <el-input
                  v-model="form.name"
                  type="text"
                />
              </el-form-item>
              <el-form-item label="单据日期" prop="pactDate">
                <el-date-picker
                clearable
                v-model="form.pactDate"
                type="date"
                value-format="yyyy-MM-dd"
                >
                </el-date-picker>
              </el-form-item>
              <el-form-item label="参数" prop="param">
                <el-input
                  v-model="form.param"
                  type="text"
                />
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
                  @data-change="dataChangeForORG"
                  :initValue="form.orgCnName"
                />
              </el-form-item>
              <el-form-item style="width: 100%" label="备注" prop="remark">
                <el-input v-model="form.remark" type="textarea" />
              </el-form-item>
              <el-tab-pane label="其他">
                <OtherForm :form="form" :rules="rules" />
              </el-tab-pane>
            </el-form>
          </el-tab-pane>

        </el-tabs>

        <el-tabs style="margin-top: 20px" type = "card">
          <el-tab-pane label="明细信息">
            <subTable
              class="sub-table"
              :isOpenSort = 'true'
              :tableName="'sys_param_detail'"
              :subData.sync ="form.sys_param_detail"
              :disabled="isFormDisabled"
              :rules="sonRules"
              >
              <template #table-column>
                <el-table-column label="下限数量" prop="lowQty">
                  <template slot-scope="scope">
                    <InputNumber
                      :readonly="isFormDisabled"
                      :controls="false"
                      v-model="scope.row.lowQty"
                      :precision="2"
                    ></InputNumber>
                  </template>
                </el-table-column>
                <el-table-column label="上限数量" prop="upQty">
                  <template slot-scope="scope">
                    <InputNumber
                      :readonly="isFormDisabled"
                      :controls="false"
                      v-model="scope.row.upQty"
                      :precision="2"
                    ></InputNumber>
                  </template>
                </el-table-column>
                <el-table-column label="值" prop="value">
                  <template slot-scope="scope">
                    <InputNumber
                      :readonly="isFormDisabled"
                      :controls="false"
                      v-model="scope.row.value"
                      :precision="2"
                      :rules="sonRules.value"
                    ></InputNumber>
                  </template>
                </el-table-column>
                <el-table-column label="绝对值" prop="absValue">
                  <template slot-scope="scope">
                    <InputNumber
                      :readonly="isFormDisabled"
                      :controls="false"
                      v-model="scope.row.absValue"
                      :precision="2"
                    ></InputNumber>
                  </template>
                </el-table-column>
                <el-table-column label="备注" prop="remark" width="120">
                  <template slot-scope="scope">
                    <el-input
                      v-model="scope.row.remark"
                      :disabled="isFormDisabled"
                    />
                  </template>
                </el-table-column>
              </template>
            </subTable>

          </el-tab-pane>
        </el-tabs>
      </template>

    </FormButtons>
  </div>

</template>

<script>

import {radioList} from "@/utils/variables";
import OtherForm from "@/components/OtherForm/index.vue";

export default {
  name:"sys-param",
  components: {OtherForm},
  dicts :[
    "data_type",
  ],
  comments:{},
  data(){
    return{
      // 参数预览
      previewJson: {},
      isReadOnly: true, // 控制表单是否只读
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        param: [
          {required: true, message: "参数不能为空", trigger: ["blur", "change", "input"]},
        ],
      },
      // 子表表单校验
      sonRules: {
        value: [
          { required: true, message: "值不能为空", trigger: ["blur", "change", "input"]},
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
  methods: {
    // 设置form的数据
    setForm(form, isInit = false) {
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
    // 组织发生变化
    dataChangeForORG(changeEchoValue, id, row, details) {
      console.log("销售组织发生变化", changeEchoValue, id, row, details);
      if (details) {
        details.then((res) => {
          this.$nextTick(() => {
            // 销售组织发生变化，携带本位币
            this.$set(this.form, "orgCnName", res.cn_name);
          });
        });
      } else {
            this.$set(this.form, "orgCnName", null);
      }
    },
  }
}
</script>

<style scoped lang="scss">

</style>
