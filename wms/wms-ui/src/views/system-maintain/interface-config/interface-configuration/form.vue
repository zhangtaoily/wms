<template>
  <div class="app-container">
    <FormButtons
      :isOpenDocStatus="false"
      @setReaderOnly="setReaderOnly"
      @setForm="setForm"
      :form="form"
      :requiredCheckFieldList="requiredCheckFieldList"
    >
      <template slot="form">
        <el-tabs v-model="activeName" type="card">
          <el-tab-pane label="基础资料">
            <el-form
              class="form-box"
              ref="form"
              :model="form"
              :rules="rules"
              label-width="80px"
            >
              <el-form-item label="业务单据" prop="modelId">
                <el-select
                  filterable
                  v-model="form.modelId"
                  placeholder="请选择"
                  clearable
                >
                  <el-option
                    v-for="dict in dict.type.model_type"
                    :key="dict.value"
                    :label="dict.label"
                    :value="dict.value"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="分发模式" prop="sendType">
                <el-select
                  filterable
                  v-model="form.sendType"
                  placeholder="请选择"
                  @change="sendTypeChange"
                  clearable
                >
                  <el-option
                    v-for="dict in dict.type.send_type"
                    :key="dict.value"
                    :label="dict.label"
                    :value="dict.value"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="触发条件" prop="workTiming">
                <el-select
                  filterable
                  v-model="form.workTiming"
                  placeholder="请选择"
                  clearable
                >
                  <el-option
                    v-for="dict in dict.type.work_timing"
                    :key="dict.value"
                    :label="dict.label"
                    :value="dict.value"
                  />
                </el-select>
              </el-form-item>
              <el-form-item style="width: 100%" label="备注" prop="remark">
                <el-input
                  v-model="form.remark"
                  type="textarea"
                  placeholder="请输入内容"
                />
              </el-form-item>
            </el-form>
          </el-tab-pane>
          <el-tab-pane label="其他">
            <OtherForm :form="form" :rules="rules" :dictDataList="dictDataList" />
          </el-tab-pane>
        </el-tabs>
        <el-tabs v-if="activeName == 0" style="margin-top: 20px" type="card">
          <el-tab-pane label="接口明细">
            <SubTable
              :isOpenSort="true"
              v-if="form.sendType"
              :subData.sync="form.sys_businessflow_config_detail"
              :tableName="'sys_businessflow_config_detail'"
            >
              <template #table-column>
                <el-table-column
                  v-if="form.sendType == '1'"
                  label="组织来源"
                  prop="rawMatId"
                >
                  <template slot-scope="scope">
                    <el-select
                      filterable
                      v-model="scope.row.orgSource"
                      placeholder="请选择"
                      clearable
                    >
                      <el-option
                        v-for="dict in dict.type.org_source"
                        :key="dict.value"
                        :label="dict.label"
                        :value="dict.value"
                      />
                    </el-select>
                  </template>
                </el-table-column>

                <el-table-column
                  v-if="form.sendType == '2'"
                  label="接受方组织"
                  prop="configId"
                >
                  <template slot-scope="scope">
                    <DynamicDialog
                      title="接受方组织"
                      functionName="SYS_INTERFACE_CFG"
                      :openRadioSelection="true"
                      @data-change="dataChangeForOrgId"
                      v-model="scope.row.configId"
                      :init-value="scope.row.orgName"
                      :row-data="scope.row"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="接受方系统" prop="systemId">
                  <template slot-scope="scope">
                    <el-select
                      filterable
                      v-model="scope.row.systemId"
                      placeholder="请选择"
                      clearable
                    >
                      <el-option
                        v-for="dict in dict.type.interface_sys"
                        :key="dict.value"
                        :label="dict.label"
                        :value="dict.value"
                      />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="用途" prop="purposeId">
                  <template slot-scope="scope">
                    <el-select
                      filterable
                      v-model="scope.row.purposeId"
                      placeholder="请选择"
                      clearable
                    >
                      <el-option
                        v-for="dict in dict.type.sys_purpose"
                        :key="dict.value"
                        :label="dict.label"
                        :value="dict.value"
                      />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="执行条件" prop="conditionSql">
                  <template slot-scope="scope">
                    <el-input
                      v-model="scope.row.conditionSql"
                      placeholder="请输入执行条件"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="启用" prop="enabled">
                  <template slot-scope="scope">
                    <el-radio-group v-model="scope.row.enabled">
                      <el-radio
                        v-for="item in radioList"
                        :key="item.value"
                        :label="item.value"
                        >{{ item.label }}</el-radio
                      >
                    </el-radio-group>
                  </template>
                </el-table-column>
              </template>
            </SubTable>
          </el-tab-pane>
        </el-tabs>
      </template>
    </FormButtons>
  </div>
</template>
<script>
import { getDetails } from "@/api/system/common";
import OtherForm from "@/components/OtherForm/index.vue";
import { radioList } from "@/utils/variables.js";
export default {
  name: "MatGP",
  dicts: [
    "send_type",
    "work_timing",
    "model_type",
    "org_source",
    "interface_sys",
    "sys_purpose",
  ],
  components: { OtherForm },
  data() {
    return {
      dictDataList:{},
      radioList: radioList,
      isReadOnly: false, // 控制表单是否只读
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        modelId: [
          { required: true, message: "业务单据名不能为空", trigger: ["blur", "change", "input"] },
        ],
        sendType: [
          { required: true, message: "分发类型不能为空", trigger: ["blur", "change", "input"] },
        ],
        workTiming: [
          { required: true, message: "触发条件不能为空", trigger: ["blur", "change", "input"] },
        ],
      },
      requiredCheckFieldList: [
        {
          sonSheet: "sys_businessflow_config_detail",
          paneName: "配置明细",
          sonFields: [
            { label: "接收方系统", key: "systemId" },
            { label: "接收方组织", key: "orgId" },
            { label: "用途", key: "purposeId" },
          ],
        },
      ],
      // 按钮loading
      buttonLoading: false,
      // 标题
      dialogTitle: "自定义标题",
      // 模块名称
      functionName: null,
      id: null,
      activeName: 0,
    };
  },
  created() {},
  computed: {},
  watch: {
    form: {
      handler: (newVal, oldVal) => {
        console.log(newVal, oldVal);
      },
      immediate: true,
      deep: true,
    },
  },
  methods: {
    setForm(form) {
      console.log(form, "form");
      this.form = form;
      this.id = form?.id;
      this.$nextTick(() => {
        this.$refs.formButtons.setOldForm(this.form);
        // 清空校验
        if (this.$refs?.form?.validate) {
          this.$refs.form.clearValidate();
        }
      });
    },
    setReaderOnly(boolean) {
      this.isReadOnly = boolean;
    },
    dataChangeForOrgId(changeEchoValue, id, row) {
      if (id) {
        getDetails("SYS_INTERFACE_CFG", id).then((res) => {
          let { data } = res;
          changeEchoValue(data.orgName);
          console.log(data, "res");
          this.$set(row, "orgId", data["organizationId"]);
          this.$set(row, "purposeId", data["purposeId"]);
          this.$set(row, "systemId", data["systemId"]);
          console.log(row.orgId);
          // for (const key in data) {
          //   if (data.hasOwnProperty(key)) {
          //     if (key !== 'orgId') {
          //       // 如果不是 'code'，则使用 this.$set 更新 row 的对应属性

          //     }
          //   }
          // }
        });
      } else {
        // 不存在id，需要清空回显值
        changeEchoValue("");
      }
    },
    sendTypeChange() {
      this.form.sys_businessflow_config_detail = [];
    },
    sfeeChange(e) {
      console.log(Number(e));
      return Number(e);
    },
  },
};
</script>

<style scoped>
.el-input-number {
  width: 100% !important;
}
.form-box >>> .el-input-number .el-input__inner {
  text-align: left !important;
}
</style>
