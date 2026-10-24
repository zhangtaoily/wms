<template>
  <el-form ref="form" :model="user" :rules="rules" label-width="80px">
    <el-form-item label="用户昵称" prop="nickName">
      <el-input v-model="user.nickName" maxlength="30" />
    </el-form-item>
    <el-form-item label="手机号码" prop="phonenumber">
      <el-input v-model="user.phonenumber" maxlength="11" />
    </el-form-item>
    <el-form-item label="邮箱" prop="email">
      <el-input v-model="user.email" maxlength="50" />
    </el-form-item>
    <el-form-item label="销售员" prop="salerId">
      <DynamicDialog
        :customizeConditions="customizeConditions3"
        title="销售员"
        functionName="SALER"
        echoField="empName"
        :simpleMode="true"
        :openRadioSelection="true"
        v-model="user.salerId"
        @data-change="dataChangeForSalerId"
        :initValue="user.salerName"
      />
    </el-form-item>
    <!-- @data-change="dataChangeForSalerId" -->
    <el-form-item label="销售组织" prop="salOrgId">
      <DynamicDialog
        :customizeConditions="customizeConditions3"
        title="销售组织"
        functionName="ORG"
        echoField="cnName"
        :openRadioSelection="true"
        v-model="user.salOrgId"
        :initValue="user.salOrgCnName"
        @data-change="dataChangeForOrganization"
      />
    </el-form-item>
    <el-form-item label="备货组织" prop="prdOrgId">
      <DynamicDialog
        :customizeConditions="customizeConditions3"
        :disabled="disabled"
        title="备货组织"
        functionName="ORG"
        echoField="cn_name"
        :simpleMode="false"
        :openRadioSelection="true"
        v-model="user.prdOrgId"
        :initValue="user.prdOrgCnName"
        @data-change="dataChangeForOrganization"
      />
    </el-form-item>
    <el-form-item label="合并code">
      <el-radio-group v-model="user.isListMergeCode">
        <el-radio :label="1">是</el-radio>
        <el-radio :label="0">否</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item label="性别">
      <el-radio-group v-model="user.sex">
        <el-radio label="0">男</el-radio>
        <el-radio label="1">女</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" size="mini" @click="submit">保存</el-button>
      <el-button type="danger" size="mini" @click="close">关闭</el-button>
    </el-form-item>
  </el-form>
</template>

<script>
import { updateUserProfile } from "@/api/system/user";
import { getDetails } from "@/api/system/common";
export default {
  props: {
    user: {
      type: Object,
    },
  },
  data() {
    return {
      customizeConditions3: [
        {
          logicalOperator: "AND",
          fieldType: "TEXT",
          field: "is_sal",
          operate: "EQ",
          value: "是",
          filterGroup: "FAST",
        },
      ],
      // 表单校验
      rules: {
        nickName: [
          {
            required: true,
            message: "用户昵称不能为空",
            trigger: ["blur", "change", "input"],
          },
        ],
        email: [
          {
            required: true,
            message: "邮箱地址不能为空",
            trigger: ["blur", "change", "input"],
          },
          {
            type: "email",
            message: "请输入正确的邮箱地址",
            trigger: ["blur", "change"],
          },
        ],
        phonenumber: [
          {
            required: true,
            message: "手机号码不能为空",
            trigger: ["blur", "change", "input"],
          },
          {
            pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/,
            message: "请输入正确的手机号码",
            trigger: ["blur", "change", "input"],
          },
        ],
      },
    };
  },
  methods: {
    // 销售员发生变化
    dataChangeForSalerId(changeEchoValue, id, row, details) {
      if (details) {
        details.then((res) => {
          // 销售员发生变化，携带销售部门
          let { data } = res;
          changeEchoValue(data.salerName);
        });
      } else {
        changeEchoValue("");
      }
    },
    // 销售组织发生变化
    dataChangeForOrganization(changeEchoValue, id, row) {
      if (id) {
        getDetails("ORG", id).then((res) => {
          let { data } = res;
          changeEchoValue(data.cnName);
        });
      } else {
        changeEchoValue("");
      }
    },
    submit() {
      this.$refs["form"].validate((valid) => {
        if (valid) {
          updateUserProfile(this.user).then((response) => {
            this.$modal.msgSuccess("修改成功");
          });
        }
      });
    },
    close() {
      this.$tab.closePage();
    },
  },
};
</script>
