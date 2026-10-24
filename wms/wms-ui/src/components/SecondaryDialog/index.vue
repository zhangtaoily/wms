<template>
  <!-- 添加或修改客户联系人对话框 -->
  <el-dialog :title="title" :visible.sync="open" width="1000px" append-to-body>
    <el-form
      ref="form"
      :inline="true"
      :rules="rules"
      :model="form"
      label-width="100px"
      class="form-secondary-box"
    >
      <el-row>
        <el-col :span="8">
          <el-form-item label="客户编码">
            <span>{{ parentForm.code || "" }}</span>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="客户名称">
            <span>{{ parentForm.cnName || "" }}</span>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="客户简称">
            <span>{{ parentForm.sName || "" }}</span>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="8">
          <el-form-item label="联系人编号" prop="code">
            <el-input v-model="form.code" placeholder="请输入联系人编号" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="联系人名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入联系人名称" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="别名简称" prop="sName">
            <el-input v-model="form.sName" placeholder="请输入别名简称" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="8">
          <el-form-item label="性别" prop="pactState">
            <el-radio-group v-model="form.sex" size="medium">
              <el-radio :label="1">男</el-radio>
              <el-radio :label="0">女</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="电话" prop="tel">
            <el-input v-model="form.tel" placeholder="请输入电话" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="移动电话" prop="mTel">
            <el-input v-model="form.mTel" placeholder="请输入移动电话" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="8">
          <el-form-item label="公司邮箱" prop="eMail">
            <el-input v-model="form.eMail" placeholder="请输入公司邮箱" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="岗位职务" prop="job">
            <el-input v-model="form.job" placeholder="请输入岗位职务" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="24">
          <el-form-item label="备注" prop="remark">
            <el-input
              v-model="form.remark"
              type="textarea"
              border
              :rows="2"
              placeholder="请输入内容"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button @click="cancel">取 消</el-button>
      <el-button :loading="buttonLoading" type="primary" @click="submitForm"
        >确 定</el-button
      >
    </div>
  </el-dialog>
</template>

  <script>
import { save, submitData } from "@/api/system/common";
import { formatDate } from "@/utils";

export default {
  name: "LinkMan",
  props: {
    // 来自父组件的值
    parentForm: {
      type: Object,
      default: () => {},
    },
  },
  data() {
    return {
      // 按钮loading
      buttonLoading: false,
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 客户联系人表格数据
      linkManList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        name: [
          { required: true, message: "联系人名称不能为空", trigger: ["blur", "change", "input"] },
        ],
      },
    };
  },

  methods: {
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        id: undefined,
        code: undefined,
        pactDate: undefined,
        pactState: undefined,
        tel: undefined,
        name: undefined,
        sName: undefined,
        sex: undefined,
        mTel: undefined,
        eMail: undefined,
        job: undefined,
        createBy: undefined,
        createTime: undefined,
        updateBy: undefined,
        updateTime: undefined,
        auditor: undefined,
        auditTime: undefined,
        syncTime: undefined,
        remark: undefined,
        unit: undefined,
      };
      this.resetForm("form");
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加客户联系人";
    },
    created() {},
    /** 提交按钮 */
    submitForm() {
      let custInfo = this.parentForm; // 从主页传过来的总form信息
      // let data = {
      //   ...this.form,
      //   pactDate: formatDate(new Date(), "date"),
      //   pactState: 1,
      //   // b_cust_linkman: [
      //   //   {
      //   //     custId: custInfo?.custId,
      //   //     code: custInfo?.code,
      //   //     cnName: custInfo?.cnName,
      //   //     sName: custInfo?.sName,
      //   //   },
      //   // ],
      // };

      let data = {
        data: {
          ...this.form,
          pactDate: formatDate(new Date(), "date"),
          pactState: 1,
        },
      };

      this.$refs["form"].validate((valid) => {
        this.buttonLoading = true;
        if (valid) {
          save("B_LINK_MAN", data)
            .then((res) => {
              const { data } = res;
              // 将用户刚添加的联系人变为 通过状态
              submitData("B_LINK_MAN", data.id).then(() => {});

              let newLinkMan = {
                custId: this.parentForm.custId,
                linkmanId: data.id,
                id: data.id,
                isWorking: 1,
                job: data.job,
                tel: data.tel,
                m_tel: data.mTel,
                sex_code: data.sex,
                name: data.name,
                remark: data.remark,
              };

              this.$emit("backwardFun", newLinkMan);
              this.$modal.msgSuccess("新增成功");

              this.open = false;
            })
            .finally(() => {
              this.buttonLoading = false;
            });
        } else {
          this.$message.error("表单校验未通过");
          this.buttonLoading = false;
        }
      });
    },
  },
};
</script>
