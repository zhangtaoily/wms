<template>
  <div class="app-container">
    <FormButtons @setReaderOnly="setReaderOnly" @setForm="setForm" :form="form">
      <template slot="form">
        <el-form ref="form" :model="form" :rules="rules" label-width="80px">
          <el-row :gutter="10">
            <el-col :span="6"
              ><el-form-item label="是否被删除" prop="deleted">
                <el-radio-group v-model="form.deleted">
                  <el-radio
                    v-for="item in radioList"
                    :key="item.value"
                    :label="item.value"
                    >{{ item.label }}</el-radio
                  >
                </el-radio-group>
              </el-form-item></el-col
            >
            <el-col :span="6"
              ><el-form-item label="扩展字段1" prop="extend1">
                <el-input
                  v-model="form.extend1"
                  placeholder="请输入扩展字段1"
                /> </el-form-item
            ></el-col>
            <el-col :span="6"
              ><el-form-item label="扩展字段2" prop="extend2">
                <el-input
                  v-model="form.extend2"
                  placeholder="请输入扩展字段2"
                /> </el-form-item
            ></el-col>
            <el-col :span="6"
              ><el-form-item label="扩展字段3" prop="extend3">
                <el-input
                  v-model="form.extend3"
                  placeholder="请输入扩展字段3"
                /> </el-form-item
            ></el-col>
          </el-row>
          <el-row :gutter="10">
            <el-col :span="6"
              ><el-form-item label="扩展字段4" prop="extend4">
                <el-input
                  v-model="form.extend4"
                  placeholder="请输入扩展字段4"
                /> </el-form-item
            ></el-col>
            <el-col :span="6"
              ><el-form-item label="扩展字段5" prop="extend5">
                <el-input
                  v-model="form.extend5"
                  placeholder="请输入扩展字段5"
                /> </el-form-item
            ></el-col>
            <el-col :span="6"
              ><el-form-item label="是否在职" prop="active">
                <el-input
                  v-model="form.active"
                  placeholder="请输入是否在职"
                /> </el-form-item
            ></el-col>
            <el-col :span="6"
              ><el-form-item label="是否admin管理员用户" prop="admin">
                <el-input
                  v-model="form.admin"
                  placeholder="请输入是否admin管理员用户"
                /> </el-form-item
            ></el-col>
          </el-row>
          <el-row :gutter="10">
            <el-col :span="6"
              ><el-form-item label="生日" prop="birthday">
                <el-date-picker
                  clearable
                  v-model="form.birthday"
                  type="datetime"
                  value-format="yyyy-MM-dd HH:mm:ss"
                  placeholder="请选择生日"
                  style="width: 100%"
                >
                </el-date-picker> </el-form-item
            ></el-col>
            <el-col :span="6"
              ><el-form-item label="是否老板" prop="boss">
                <el-input
                  v-model="form.boss"
                  placeholder="请输入是否老板"
                /> </el-form-item
            ></el-col>
            <el-col :span="6"
              ><el-form-item label="部门id" prop="departmentId">
                <el-input
                  v-model="form.departmentId"
                  placeholder="请输入部门id"
                /> </el-form-item
            ></el-col>
            <el-col :span="6"
              ><el-form-item label="离职时间" prop="departureDate">
                <el-date-picker
                  clearable
                  v-model="form.departureDate"
                  type="datetime"
                  value-format="yyyy-MM-dd HH:mm:ss"
                  placeholder="请选择离职时间"
                  style="width: 100%"
                >
                </el-date-picker> </el-form-item
            ></el-col>
          </el-row>
          <el-row :gutter="10">
            <el-col :span="6"
              ><el-form-item label="邮箱" prop="email">
                <el-input
                  v-model="form.email"
                  placeholder="请输入邮箱"
                /> </el-form-item
            ></el-col>
            <el-col :span="6"
              ><el-form-item label="员工编号" prop="employeeNo">
                <el-input
                  v-model="form.employeeNo"
                  placeholder="请输入员工编号"
                /> </el-form-item
            ></el-col>
            <el-col :span="6"
              ><el-form-item label="员工等级" prop="employeeRank">
                <el-input
                  v-model="form.employeeRank"
                  placeholder="请输入员工等级"
                /> </el-form-item
            ></el-col>
            <el-col :span="6"
              ><el-form-item label="入职日期" prop="entryDate">
                <el-date-picker
                  clearable
                  v-model="form.entryDate"
                  type="datetime"
                  value-format="yyyy-MM-dd HH:mm:ss"
                  placeholder="请选择入职日期"
                  style="width: 100%"
                >
                </el-date-picker> </el-form-item
            ></el-col>
          </el-row>
          <el-row :gutter="10">
            <el-col :span="6"
              ><el-form-item label="性别" prop="gender">
                <el-input
                  v-model="form.gender"
                  placeholder="请输入性别"
                /> </el-form-item
            ></el-col>
            <el-col :span="6"
              ><el-form-item label="是否领导" prop="leader">
                <el-input
                  v-model="form.leader"
                  placeholder="请输入是否领导"
                /> </el-form-item
            ></el-col>
            <el-col :span="6"
              ><el-form-item label="直属主管id" prop="managerId">
                <el-input
                  v-model="form.managerId"
                  placeholder="请输入直属主管id"
                /> </el-form-item
            ></el-col>
            <el-col :span="6"
              ><el-form-item label="电话" prop="mobile">
                <el-input
                  v-model="form.mobile"
                  placeholder="请输入电话"
                /> </el-form-item
            ></el-col>
          </el-row>
          <el-row :gutter="10">
            <el-col :span="6"
              ><el-form-item label="姓名" prop="name">
                <el-input
                  v-model="form.name"
                  placeholder="请输入姓名"
                /> </el-form-item
            ></el-col>
            <el-col :span="6"
              ><el-form-item label="密码" prop="password">
                <el-input
                  v-model="form.password"
                  placeholder="请输入密码"
                /> </el-form-item
            ></el-col>
            <el-col :span="6"
              ><el-form-item label="用户名、登录账号" prop="username">
                <el-input
                  v-model="form.username"
                  placeholder="请输入用户名、登录账号"
                /> </el-form-item
            ></el-col>
            <el-col :span="6"
              ><el-form-item label="密级" prop="privacyLevel">
                <el-input
                  v-model="form.privacyLevel"
                  placeholder="请输入密级"
                /> </el-form-item
            ></el-col>
          </el-row>
          <el-row :gutter="10">
            <el-col :span="6"
              ><el-form-item label="用户id" prop="userId">
                <el-input
                  v-model="form.userId"
                  placeholder="请输入用户id"
                /> </el-form-item
            ></el-col>
            <el-col :span="6"
              ><el-form-item label="组织corpId" prop="corpId">
                <el-input
                  v-model="form.corpId"
                  placeholder="请输入组织corpId"
                /> </el-form-item
            ></el-col>
            <el-col :span="6"
              ><el-form-item label="是否禁用" prop="enabled">
                <el-radio-group v-model="form.enabled">
                  <el-radio
                    v-for="item in radioList"
                    :key="item.value"
                    :label="item.value"
                    >{{ item.label }}</el-radio
                  >
                </el-radio-group>
              </el-form-item></el-col
            >
            <el-col :span="6"
              ><el-form-item label="人事创建时间" prop="hrcreateTime">
                <el-date-picker
                  clearable
                  v-model="form.hrcreateTime"
                  type="datetime"
                  value-format="yyyy-MM-dd HH:mm:ss"
                  placeholder="请选择人事创建时间"
                  style="width: 100%"
                >
                </el-date-picker> </el-form-item
            ></el-col>
          </el-row>
          <el-row :gutter="10">
            <el-col :span="6"
              ><el-form-item label="人事更改时间" prop="hrmodifyTime">
                <el-date-picker
                  clearable
                  v-model="form.hrmodifyTime"
                  type="datetime"
                  value-format="yyyy-MM-dd HH:mm:ss"
                  placeholder="请选择人事更改时间"
                  style="width: 100%"
                >
                </el-date-picker> </el-form-item
            ></el-col>
            <el-col :span="6"
              ><el-form-item label="备注" prop="remark">
                <el-input
                  v-model="form.remark"
                  type="textarea"
                  placeholder="请输入内容"
                /> </el-form-item
            ></el-col>
          </el-row>
        </el-form>
      </template>
    </FormButtons>
  </div>
</template>
<script>
import { radioList } from "@/utils/variables.js";
import { save, getDetails } from "@/api/system/common";
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
        name: [{ required: true, message: "不能为空", trigger: ["blur", "change", "input"] }],
        password: [{ required: true, message: "不能为空", trigger: ["blur", "change", "input"] }],
        username: [{ required: true, message: "不能为空", trigger: ["blur", "change", "input"] }],
      },
      // 按钮loading
      buttonLoading: false,
      // 标题
      dialogTitle: "自定义标题",
      // 模块名称
      functionName: "INT_CONT_TYPE",
      // 回显的值
      echoField: "code",
      dialogValue: {
        code: "test22",
        en_name: "test",
        cn_name: "admin",
        id: "1818266111406620097",
        create_time: "2024-07-30",
      },
      dialogValue2: [
        {
          code: "test22",
          en_name: "test",
          cn_name: "admin",
          id: "1818266111407620044",
          create_time: "2024-07-30",
        },
        {
          code: "4",
          en_name: "4",
          cn_name: "admin",
          id: "18184500338146866",
          create_time: "2024-07-31",
        },
      ],
      functionName: null,
      id: null,
      mode: "show",
      printButtonList: [],
      radioList: radioList,
    };
  },
  created() {
    // this.updateTitle()
    this.init();
  },
  computed: {
    // 计算属性的 getter
    isShow: function () {
      console.log(this.mode);
      return this.mode != "add";
    },
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
    setForm(form) {
      this.form = form;
      this.id = form?.id;
      // this.init()
      this.$nextTick(() =>{
        // 清空校验
        if (this.$refs?.form?.validate) {
          this.$refs.form.clearValidate();
        }
      })
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
  },
};
</script>
