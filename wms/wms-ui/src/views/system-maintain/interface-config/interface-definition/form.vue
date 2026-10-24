<template>
  <div class="app-container">
    <FormButtons
      ref="formButtonsRef"
      @setForm="setForm"
      :form="form"
      :isOpenDocStatus="false"
    >
      <template slot="form">
        <el-form ref="form" :model="form" :rules="rules" label-width="80px">
          <el-tabs v-model="activeName" type="card">
            <el-tab-pane class="form-box" label="基础资料">
              <el-form-item label="接口名称" prop="interfaceName">
                <el-input
                  v-model="form.interfaceName"
                  type="text"
                  placeholder="请输入内容"
                />
              </el-form-item>
              <el-form-item label="用途" prop="purposeId">
                <el-select
                  v-model="form.purposeId"
                  placeholder="请选择用途"
                  filterable
                >
                  <el-option
                    v-for="dict in dictDataList.sys_purpose"
                    :key="dict.dictValue"
                    :label="dict.dictLabel"
                    :value="dict.dictValue"
                  ></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="提供方组织" prop="organizationId">
                <el-select
                  filterable
                  v-model="form.organizationId"
                  placeholder="请选择提供方组织"
                >
                  <el-option
                    v-for="dict in dictDataList.interface_org"
                    :key="dict.dictValue"
                    :label="dict.dictLabel"
                    :value="Number(dict.dictValue)"
                  ></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="提供方系统" prop="systemId">
                <el-select
                  filterable
                  v-model="form.systemId"
                  placeholder="请选择提供方系统"
                >
                  <el-option
                    v-for="dict in dictDataList.interface_sys"
                    :key="dict.dictValue"
                    :label="dict.dictLabel"
                    :value="dict.dictValue"
                  ></el-option>
                </el-select>
              </el-form-item>
              <el-form-item style="width: 100%" label="地址" prop="url">
                <el-input
                  v-model="form.url"
                  type="text"
                  placeholder="请输入内容"
                />
              </el-form-item>
              <el-form-item style="width: 100%" label="备注" prop="remark">
                <el-input
                  v-model="form.remark"
                  type="textarea"
                  placeholder="请输入内容"
                />
              </el-form-item>
            </el-tab-pane>
            <el-tab-pane label="其他">
              <OtherForm :form="form" :rules="rules" :dictDataList="dictDataList" />
            </el-tab-pane>
          </el-tabs>
          <el-tabs v-if="activeName == 0" style="margin-top: 20px" type="card">
            <el-tab-pane label="请求头参数">
              <SubTable
                class="sub-table"
                :isOpenSort="true"
                :subData.sync="form.sys_interface_config_head"
                :tableName="'sys_interface_config_head'"
              >
                <template #table-column>
                  <el-table-column
                    :render-header="addRedStar"
                    label="参数名"
                    prop="itemName"
                    width="200px"
                  >
                    <template slot-scope="scope">
                      <el-form-item
                        :prop="`sys_interface_config_head.${scope.$index}.itemName`"
                        :rules="headTableRules.itemName"
                      >
                        <el-input
                          v-model="scope.row.itemName"
                          placeholder="请输入"
                        />
                      </el-form-item>
                    </template>
                  </el-table-column>
                  <el-table-column
                    :render-header="addRedStar"
                    label="值"
                    prop="itemValue"
                    width="250px"
                  >
                    <template slot-scope="scope">
                      <el-form-item
                        :prop="`sys_interface_config_head.${scope.$index}.itemValue`"
                        :rules="headTableRules.itemValue"
                      >
                        <el-input
                          v-model="scope.row.itemValue"
                          placeholder="请输入"
                      /></el-form-item>
                    </template>
                  </el-table-column>
                </template>
              </SubTable>
            </el-tab-pane>
            <el-tab-pane label="请求参数">
              <SubTable
                :isOpenSort="true"
                :subData.sync="form.sys_interface_config_node"
                :selectedSonTableIndex.sync="selectedSonTableIndex"
                :tableName="'sys_interface_config_node'"
              >
                <template #table-column>
                  <el-table-column
                    label="参数节点名称"
                    prop="nodeName"
                    width="200px"
                    :render-header="addRedStar"
                  >
                    <template slot-scope="scope">
                      <el-form-item
                        :prop="`sys_interface_config_node.${scope.$index}.nodeName`"
                        :rules="nodeTableRules.nodeName"
                      >
                        <el-input
                          v-model="scope.row.nodeName"
                          placeholder="请输入参数节点名称"
                      /></el-form-item>
                    </template>
                  </el-table-column>
                  <el-table-column
                    label="上级节点"
                    prop="parentNode"
                    width="200px"
                  >
                    <template slot-scope="scope">
                      <el-input
                        v-model="scope.row.parentNode"
                        placeholder="请输入上级节点"
                      />
                    </template>
                  </el-table-column>
                  <el-table-column
                    label="本地数据节点"
                    prop="dataNode"
                    width="200px"
                    :render-header="addRedStar"
                  >
                    <template slot-scope="scope">
                      <el-form-item
                        :prop="`sys_interface_config_node.${scope.$index}.dataNode`"
                        :rules="nodeTableRules.dataNode"
                      >
                        <el-input
                          v-model="scope.row.dataNode"
                          placeholder="请输入本地数据节点"
                      /></el-form-item>
                    </template>
                  </el-table-column>
                  <el-table-column
                    label="节点类型"
                    prop="dataType"
                    width="200px"
                    :render-header="addRedStar"
                  >
                    <template slot-scope="scope">
                      <el-form-item
                        :prop="`sys_interface_config_node.${scope.$index}.dataType`"
                        :rules="nodeTableRules.dataType"
                      >
                        <el-select
                          filterable
                          v-model="scope.row.dataType"
                          placeholder="请选择节点类型"
                        >
                          <el-option
                            v-for="dict in dictDataList.node_type"
                            :key="dict.dictValue"
                            :label="dict.dictLabel"
                            :value="dict.dictValue"
                          ></el-option> </el-select
                      ></el-form-item>
                    </template>
                  </el-table-column>
                </template>
              </SubTable>

              <SubTable
                v-if="selectedSonTableIndex >= 0"
                :isOpenSort="true"
                :subData.sync="
                  form.sys_interface_config_node[selectedSonTableIndex]
                    .sys_interface_config_node_parm
                "
                :tableName="'sys_interface_config_node_parm'"
              >
                <template #table-column>
                  <el-table-column
                    :render-header="addRedStar"
                    label="参数名"
                    prop="parmKey"
                    width="200px"
                  >
                    <template slot-scope="scope">
                      <el-form-item
                        :prop="`sys_interface_config_node.${selectedSonTableIndex}.sys_interface_config_node_parm.${scope.$index}.parmKey`"
                        :rules="nodeTableRules.parmKey"
                      >
                        <el-input
                          v-model="scope.row.parmKey"
                          placeholder="请输入参数名" /></el-form-item
                    ></template>
                  </el-table-column>
                  <el-table-column
                    label="数据类型"
                    prop="dataType"
                    width="200px"
                    :render-header="addRedStar"
                  >
                    <template slot-scope="scope">
                      <el-form-item
                        :prop="`sys_interface_config_node.${selectedSonTableIndex}.sys_interface_config_node_parm.${scope.$index}.dataType`"
                        :rules="nodeTableRules.dataType"
                      >
                        <el-select
                          filterable
                          v-model="scope.row.dataType"
                          placeholder="数据类型"
                        >
                          <el-option
                            v-for="dict in dictDataList.data_type"
                            :key="dict.dictValue"
                            :label="dict.dictLabel"
                            :value="Number(dict.dictValue)"
                          ></el-option> </el-select
                      ></el-form-item>
                    </template>
                  </el-table-column>
                  <el-table-column label="赋值公式" prop="dataFun">
                    <template slot-scope="scope">
                      <el-input
                        v-model="scope.row.dataFun"
                        type="text"
                        placeholder="请输入内容赋值公式"
                    /></template>
                  </el-table-column>
                  <el-table-column label="备注" prop="remark">
                    <template slot-scope="scope">
                      <el-input
                        v-model="scope.row.remark"
                        type="textarea"
                    /></template>
                  </el-table-column>
                </template>
              </SubTable>
            </el-tab-pane>
            <el-tab-pane label="参数预览">
              <json-viewer
                :value="previewJson"
                :expand-depth="5"
                copyable
                boxed
                sort
              ></json-viewer>
            </el-tab-pane>
          </el-tabs>
        </el-form>
      </template>
    </FormButtons>
  </div>
</template>
<script>
import { getPreviewData } from "@/api/system/interfaceConfig";
import JsonViewer from "vue-json-viewer";
import OtherForm from "@/components/OtherForm/index.vue";
import { getDictDataList } from "@/api/system/dict/data";
import { headerAddRedStar } from "@/utils/validatorUtils";
export default {
  name: "InterfaceDefinition",
  components: {
    JsonViewer,
    OtherForm,
  },
  data() {
    return {
      // 参数预览
      previewJson: {},
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        interfaceName: [
          { required: true, message: "接口名称不能为空", trigger: ["blur", "change", "input"] },
        ],
        purposeId: [
          { required: true, message: "用途不能为空", trigger: ["blur", "change", "input"] },
        ],
        organizationId: [
          { required: true, message: "提供方组织不能为空", trigger: ["blur", "change", "input"] },
        ],
        systemId: [
          { required: true, message: "提供方系统不能为空", trigger: ["blur", "change", "input"] },
        ],
        url: [{ required: true, message: "地址不能为空", trigger: ["blur", "change", "input"] }],
      },
      // 请求头参数表单校验
      headTableRules: {
        itemName: [
          { required: true, message: "参数名不能为空", trigger: ["blur", "change", "input"] },
        ],
        itemValue: [
          { required: true, message: "参数值不能为空", trigger: ["blur", "change", "input"] },
        ],
      },
      // 请求参数节点表单校验
      nodeTableRules: {
        nodeName: [
          { required: true, message: "参数节点名称不能为空", trigger: ["blur", "change", "input"] },
        ],
        dataNode: [
          { required: true, message: "本地数据节点不能为空", trigger: ["blur", "change", "input"] },
        ],
        dataType: [
          { required: true, message: "节点类型不能为空", trigger: ["blur", "change", "input"] },
        ],
      },
      // 请求参数节点表单校验
      parmTableRules: {
        parmKey: [
          { required: true, message: "参数名称不能为空", trigger: ["blur", "change", "input"] },
        ],
        dataType: [
          { required: true, message: "数据类型不能为空", trigger: ["blur", "change", "input"] },
        ],
      },
      id: null,
      mode: "show",
      selectedSonTableIndex: -1,
      // 用来判断是否第一进入页面
      isFirst: true,
      activeName: 0,
      // 数据字典列表
      dicts: [
        "interface_org",
        "interface_sys",
        "node_type",
        "sys_purpose",
        "data_type",
      ],
      // 存储
      dictDataList: [],
      // 表头生成必填校验红星样式
      addRedStar: headerAddRedStar,
    };
  },
  created() {
    // 获取数据字段数据S
    this.getDictData();
  },
  computed: {},
  methods: {
    // 发送请求获取预览数据
    getPreview() {
      getPreviewData(this.id).then((res) => {
        console.log("getPreview", res);
        this.previewJson = res.data;
      });
    },
    // 设置form的数据
    setForm(form) {
      this.form = form;
      this.id = form?.id;
      console.log(this.isFirst);
      if (this.isFirst && this.id) {
        this.getPreview();
        this.isFirst = false;
      }
      this.$nextTick(() => {
        this.$refs.formButtonsRef.setOldForm(this.form);
        // 清空校验
        if (this.$refs?.form?.validate) {
          this.$refs.form.clearValidate();
        }
      });
    },

    // 获取全部数据字典数据
    getDictData() {
      this.dictList = getDictDataList(this.dicts)
        .then((res) => {
          this.dictDataList = res.data;
        })
        .catch((err) => {
          this.$message.error("获取数据字段数据失败");
        });
    },
  },
};
</script>
<style lang="scss" scoped>
// ::v-deep .form-box  .el-input__inner{
// border: 0 !important;
// }
::v-deep .sub-table .el-form-item--mini.el-form-item {
  margin: 0px;
}
</style>
