<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['coding:menuCoding:add']"
          >新增</el-button
        >
      </el-col>
    </el-row>

    <el-table border v-loading="loading" :data="menuCodingList">
      <el-table-column label="模块" align="center" prop="functionName" />
      <el-table-column label="预览" align="center" prop="preview" />
      <el-table-column label="日期" align="center" prop="codingTime" />
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改菜单编码对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="模块" prop="menuId">
          <el-select v-model="form.functionName" 亲舒服 style="width: 100%">
            <el-option
              v-for="dict in menuoption"
              :key="dict.key"
              :label="dict.value"
              :value="dict.key"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-divider></el-divider>
        <el-form-item
          v-for="(item, index) in select"
          :label="index == 1 ? 'sql' : '前缀' + (index + 1)"
          :key="index"
        >
          <el-input
            v-model="sql"
            @change="getspl($event, item)"
            placeholder="请输入"
            v-if="index == 1"
          />
          <div style="display: flex" v-else>
            <el-select v-model="item.codingType" style="width: 160px">
              <el-option
                v-for="dict in dict.type.sys_coding_type"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              ></el-option>
            </el-select>
            <span style="width: 50px; text-align: center">长度</span>
            <el-input
              style="width: 50px"
              v-model="item.codingLength"
              @change="lengthChange($event, item)"
              :disabled="item.codingType == 2"
            ></el-input>
            <el-input
              style="width: 100px"
              v-model="item.codingPrefix"
              v-if="item.codingType == 1"
              :disabled="item.codingType == 1"
            ></el-input>
            <el-input
              style="width: 100px"
              v-model="item.codingPrefix"
              v-if="item.codingType == 3"
              :maxlength="item.codingLength"
            ></el-input>
            <el-select
              v-model="item.codingPrefix"
              v-if="item.codingType == 2"
              @change="datelength($event, item)"
              style="width: 100px"
              clearable
            >
              <el-option
                v-for="dict in dict.type.sys_date_type"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              ></el-option>
            </el-select>
          </div>
        </el-form-item>
        <el-form-item label="预览" prop="remark">
          <span>{{ form.preview }}</span>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button :loading="buttonLoading" type="primary" @click="submitForm"
          >确 定</el-button
        >
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  addMenuCoding,
  listMenuCoding,
  MenuCodingnativeSql,
  menuCodingtemplate,
} from "@/api/coding/menuCoding";
import { getDicts } from "@/api/system/dict/data";

export default {
  name: "MenuCoding",
  dicts: ["sys_date_type", "sys_coding_type"],
  data() {
    return {
      coding: "", //预览编码
      number: 0,
      form: {
        functionName: "",
        preview: "",
      },
      select: [
        { codingType: "", codingLength: "", codingPrefix: "" },
        { codingType: "", codingLength: "", codingPrefix: "" },
        { codingType: "", codingLength: "", codingPrefix: "" },
        { codingType: "", codingLength: "", codingPrefix: "" },
      ],
      sql: "", //页面输入sql 字符
      menuoption: [],
      //前缀选项
      // selectOption:[
      //   {id:1,label:'流水号'},
      //   {id:2,label:'日期'},
      //   {id:3,label:'固定值'},
      // ],
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
      // 菜单编码表格数据
      menuCodingList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 50,
      },
      aaa: [],
      // 表单校验
      rules: {
        functionName: [
          {
            required: true,
            message: "模块不能为空",
            trigger: ["blur", "change", "input"],
          },
        ],
      },
    };
  },
  created() {
    this.getList();
    this.getmenus(); //获取菜单下拉
    this.getDicts(); //获取流水号长度
  },
  watch: {
    select: {
      handler(oldval, newval) {
        let string = "";
        newval.forEach((el) => {
          if (el.codingType === "2") {
            string += this.gettime(el.codingPrefix) + "";
          } else {
            string += el.codingPrefix + "";
          }
        });
        this.form.preview = string;
      },
      deep: true,
    },
  },
  methods: {
    lengthChange(e, item) {
      // console.log(e ,item)
      let a = "1".padStart(e, "0");
      console.log(a);
      if (item.codingType == "1") {
        this.$set(item, "codingPrefix", a);
      }
      console.log(this.select);
    },
    //获取日期的长度
    datelength(e, item) {
      console.log(item);
      this.$set(item, "codingLength", e.length);
    },
    getspl(e, item) {
      let param = {
        nativeSql: e,
      };
      MenuCodingnativeSql(param).then((res) => {
        console.log(res);
        this.$set(item, "codingPrefix", res.data);
      });
    },

    getDicts() {
      getDicts("sys_code_length").then((res) => {
        console.log(res);
        this.number = parseInt(res.data[0].dictLabel);
      });
    },
    getmenus() {
      menuCodingtemplate().then((res) => {
        this.menuoption = res.data;
      });
    },
    gettime(format) {
      if (format) {
        const date = new Date();
        const year = date.getFullYear();
        const month = ("0" + (date.getMonth() + 1)).slice(-2);
        const day = ("0" + date.getDate()).slice(-2);
        return format
          .replace("yyyy", year)
          .replace("MM", month)
          .replace("dd", day);
      } else {
        return "";
      }
    },
    /** 查询菜单编码列表 */
    getList() {
      this.loading = true;
      listMenuCoding(this.queryParams).then((response) => {
        this.menuCodingList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        functionName: "",
        preview: "",
      };
      this.select = [
        { codingType: "", codingLength: "", codingPrefix: "" },
        { codingType: "", codingLength: "", codingPrefix: "" },
        { codingType: "", codingLength: "", codingPrefix: "" },
        { codingType: "", codingLength: "", codingPrefix: "" },
      ];
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map((item) => item.codingId);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "新增自动编码";
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate((valid) => {
        if (valid) {
          this.buttonLoading = true;
          this.form.sysMenuCodingAttachmentsList = this.select;
          addMenuCoding(this.form)
            .then((response) => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            })
            .finally(() => {
              this.buttonLoading = false;
            });
        }
      });
    },
  },
};
</script>


