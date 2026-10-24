<!--自动编码-->

<template>
  <div class="app-container">
    <FormButtons @setReaderOnly="setReaderOnly" @setForm="setForm" :form="form">
      <template slot="form">
        <el-tabs type="border-card">
          <el-tab-pane label="编码详情">
            <el-form
              class="form-box"
              ref="form"
              :model="form"
              :rules="rules"
              label-width="80px"
            >
              <el-form-item label="菜单名称" prop="functionName">
                <el-select
                  v-model="form.functionName"
                  style="width: 100%"
                  filterable
                >
                  <el-option
                    v-for="dict in menuoption"
                    :key="dict.key"
                    :label="dict.value"
                    :value="dict.key"
                  ></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="流水号长度" prop="serialLen">
                <InputNumber
                  v-model="form.serialLen"
                  placeholder="请输入流水号长度"
                  :precision="0"
                />
              </el-form-item>
              <el-form-item label="前缀" prop="prefix">
                <el-input
                  v-model="form.prefix"
                  placeholder="请输入前缀：例如YXHT[yyyy][MM][dd]"
                />
              </el-form-item>
              <el-form-item label="	是否启用编码" prop="isAutoCode">
                <el-radio-group v-model="form.isAutoCode">
                  <el-radio
                    v-for="item in radioList"
                    :key="item.value"
                    :label="item.value"
                    >{{ item.label }}
                  </el-radio>
                </el-radio-group>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="其他">
            <OtherForm :form="form" :rules="rules"></OtherForm>
          </el-tab-pane>
        </el-tabs>
      </template>
    </FormButtons>
  </div>
</template>

<script>
import {
  addMenuCoding,
  listMenuCoding,
  MenuCodingnativeSql,
  menuCodingtemplate,
} from "@/api/coding/menuCoding";
import OtherForm from "@/components/OtherForm/index.vue";
import { getDetails } from "@/api/system/common";
import { radioList } from "@/utils/variables.js";

export default {
  name: "sys_auto_code_set",
  dicts: ["b_parities_type"],
  components: {
    OtherForm,
  },
  data() {
    return {
      isReadOnly: false, // 控制表单是否只读
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        menuId: [
          { required: true, message: "菜单名称不可为空", trigger: ["blur", "change", "input"] },
        ],
        serialLen: [
          { required: true, message: "流水号长度不可为空", trigger: ["blur", "change", "input"] },
        ],
      },

      // 按钮loading
      buttonLoading: false,
      id: null,
      mode: "show",
      printButtonList: [],
      activeName: 0,

      radioList: radioList,
      menuoption: [],
    };
  },
  created() {
    this.getmenus();
  },
  dataChange(changeEchoValue, id) {
    if (id) {
      getDetails("SYS_EMPLOYEE", id).then((res) => {
        let { data } = res;
        changeEchoValue(data.name);
      });
    } else {
      // 不存在id，需要清空回显值
      changeEchoValue("");
    }
  },
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
    setForm(form, isInit) {
      this.form = form;
      this.id = form?.id;
      // this.init()
      isInit && (this.form.isAutoCode = 1);
    },
    getmenus() {
      menuCodingtemplate().then((res) => {
        this.menuoption = res.data;
      });
    },
    setReaderOnly(boolean) {
      this.isReadOnly = boolean;
    },
  },
};
</script>
<style scoped lang="scss">
</style>
