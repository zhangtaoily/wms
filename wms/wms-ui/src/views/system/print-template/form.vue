<template>
  <div class="app-container">
    <FormButtons ref="formButtons" :isOpenDocStatus="false" @setReaderOnly="setReaderOnly" @setForm="setForm" :form="form">
      <template slot="form">
        <el-form
          ref="form"
          :model="form"
          :rules="rules"
          label-width="80px"
          class="form-box"
        >
          <el-form-item label="模块表名" prop="modelName">
            <el-input
              :disabled="isReadOnly"
              v-model="form.modelName"
              placeholder="请输入模块表名"
            />
          </el-form-item>
          <el-form-item label="链接地址" prop="linkUrl">
            <el-input
              :disabled="isReadOnly"
              v-model="form.linkUrl"
              placeholder="请输入链接地址"
            />
          </el-form-item>
          <el-form-item label="按钮名称" prop="buttonName">
            <el-input
              :disabled="isReadOnly"
              v-model="form.buttonName"
              placeholder="请输入按钮名称"
            />
          </el-form-item>

          <el-form-item label="是否启用" prop="enabled">
            <el-radio-group v-model="form.enabled">
              <el-radio
                v-for="item in radioList"
                :key="item.value"
                :label="item.value"
                :disabled="isReadOnly"
                >{{ item.label }}</el-radio
              >
            </el-radio-group>
          </el-form-item>
        </el-form>
      </template>
    </FormButtons>
  </div>
</template>

<script>
import { getDetails } from "@/api/system/common";
import { radioList } from "@/utils/variables.js";

export default {
  name: "FormPage",
  dicts: ["sys_enabled"],
  components: {},
  data() {
    return {
      isReadOnly: false, // 控制表单是否只读
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        id: [{ required: true, message: "不能为空", trigger: ["blur", "change", "input"] }],
        code: [{ required: true, message: "编码不能为空", trigger: ["blur", "change", "input"] }],
        enabled: [
          { required: true, message: "是否启用不能为空", trigger: ["blur", "change", "input"]},
        ],
        cnName: [{ required: true, message: "名称不能为空", trigger: ["blur", "change", "input"] }],
        enName: [
          { required: true, message: "英文名称不能为空", trigger: ["blur", "change", "input"] },
        ],
      },
      // 模块名称
      functionName: "INT_CONT_TYPE",

      id: null,
      mode: "show",
      radioList: radioList,
    };
  },
  created() {
    this.init();
  },
  methods: {
    init() {
      this.form = {};
      this.id = null;
      let { functionName, id, mode } = this.$route.query;
      this.functionName = functionName;
      this.id = id;
      this.mode = mode;
      if (mode == "show") {
        this.isReadOnly = true;
      } else {
        this.isReadOnly = false;
      }
      if (mode != "add") {
        this.getData();
      }
    },
    getData() {
      if (this.id) {
        getDetails(this.functionName, this.id).then((res) => {
          this.form = res.data;
        });
      }
    },
    setReaderOnly(boolean) {
      this.isReadOnly = boolean;
    },
    setForm(form, isInit = false) {
      this.form = form;
      this.id = form?.id;
      // 默认赋值启用
      isInit && (this.form.enabled = 1);
      this.$nextTick(() => {
        console.log("nextTick oldForm", this.form);
        this.$refs.formButtons.setOldForm(this.form);
      });
    },
  },
};
</script>
