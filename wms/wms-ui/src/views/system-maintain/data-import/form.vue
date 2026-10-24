<template>
  <div class="app-container">
    <FormButtons
      ref="formButtonsRef"
      @setForm="setForm"
      :form="form"
      :isOpenDocStatus="false"
    >
      <template slot="form">
        <el-form ref="form" :model="form" label-width="80px">
          <el-tabs type="card">
            <el-tab-pane class="form-box" label="基础资料">
              <el-form-item label="单号" prop="code">
                <el-input disabled v-model="form.code" type="text" />
              </el-form-item>
              <el-form-item label="是否更新" prop="isUpdate">
                <YNSelect v-model="form.isUpdate" disabled />
              </el-form-item>
              <el-form-item label="创建人" prop="createName">
                <el-input
                  v-model="form.createName"
                  type="text"
                  placeholder=""
                  disabled
                />
              </el-form-item>
              <el-form-item label="创建时间" prop="createTime">
                <el-date-picker
                  clearable
                  v-model="form.createTime"
                  type="datetime"
                  value-format="yyyy-MM-dd HH:mm:ss"
                  placeholder=""
                  style="width: 100%"
                  disabled
                >
                </el-date-picker>
              </el-form-item>
              <el-form-item style="width: 100%" label="结果" prop="result">
                <el-input class="result" disabled v-model="form.result" type="textarea" />
              </el-form-item>
            </el-tab-pane>
          </el-tabs>
        </el-form>
      </template>
    </FormButtons>
  </div>
</template>
<script>
export default {
  name: "Data-import-form",
  components: {},
  data() {
    return {
      // 参数预览
      previewJson: {},
      // 表单参数
      form: {},
      id: null,
      mode: "show",
      selectedSonTableIndex: -1,
    };
  },
  created() {},
  computed: {},
  methods: {
    // 设置form的数据
    setForm(form) {
      this.form = form;
      this.id = form?.id;
      this.$nextTick(() => {
        this.$refs.formButtonsRef.setOldForm(this.form);
        // 清空校验
        if (this.$refs?.form?.validate) {
          this.$refs.form.clearValidate();
        }
      });
    },
  },
};
</script>
<style lang="scss" scoped>
::v-deep .sub-table .el-form-item--mini.el-form-item {
  margin: 0px;
}
.result{
  ::v-deep .el-textarea__inner{
    height:500px;
  }
}
</style>
