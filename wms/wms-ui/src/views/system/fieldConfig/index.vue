<template>
  <div class="app-container">
    <el-form
      :model="queryParams"
      ref="queryForm"
      size="small"
      :inline="true"
      v-show="showSearch"
      label-width="68px"
    >
      <el-form-item label="模块名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入模块名称"
          clearable
          style="width: 160px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="功能组" prop="moduleGroup">
        <el-input
          v-model="queryParams.moduleGroup"
          placeholder="请输入从属功能组"
          clearable
          style="width: 160px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="列标题" prop="headerText">
        <el-input
          v-model="queryParams.headerText"
          placeholder="请输入列标题"
          clearable
          style="width: 160px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          icon="el-icon-search"
          size="mini"
          @click="handleQuery"
          >搜索</el-button
        >
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery"
          >重置</el-button
        >
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['SYS_FieldConfig']"
          >新增</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['SYS_FieldConfig']"
          >修改</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['SYS_FieldConfig']"
          >删除</el-button
        >
      </el-col>
      <right-toolbar
        :showSearch.sync="showSearch"
        @queryTable="getList"
      ></right-toolbar>
    </el-row>

    <el-table
      v-loading="loading"
      :data="fieldConfigList"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="编号" prop="id" width="70" />
      <el-table-column
        label="模块名称"
        prop="name"
        :show-overflow-tooltip="true"
        width="120"
      />
      <el-table-column
        label="从属功能组"
        prop="moduleGroup"
        :show-overflow-tooltip="true"
        width="110"
      />
      <el-table-column
        label="列名"
        prop="columnName"
        :show-overflow-tooltip="true"
        width="130"
      />
      <el-table-column
        label="列标题"
        prop="headerText"
        :show-overflow-tooltip="true"
      />
      <el-table-column label="排序" prop="sortIndex" width="70" align="center" />
      <el-table-column
        label="字段类型"
        prop="fieldType"
        width="90"
        align="center"
      />
      <el-table-column label="搜索" align="center" width="70">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.search === 1" type="success" size="mini"
            >是</el-tag
          >
          <el-tag v-else type="info" size="mini">否</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="显示" align="center" width="70">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.isShow === 1" type="success" size="mini"
            >是</el-tag
          >
          <el-tag v-else type="info" size="mini">否</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="PDA显示" align="center" width="80">
        <template slot-scope="scope">
          <span v-if="scope.row.mobileShow === 1">是</span>
          <span v-else>否</span>
        </template>
      </el-table-column>
      <el-table-column label="扫码输入" align="center" width="80">
        <template slot-scope="scope">
          <span v-if="scope.row.scanInput === 1">是</span>
          <span v-else>否</span>
        </template>
      </el-table-column>
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        width="160"
      >
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        align="center"
        width="140"
        class-name="small-padding fixed-width"
      >
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['SYS_FieldConfig']"
            >修改</el-button
          >
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['SYS_FieldConfig']"
            >删除</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改字段配置对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="640px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="模块名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入模块名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="从属功能组" prop="moduleGroup">
              <el-input v-model="form.moduleGroup" placeholder="请输入从属功能组" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="列名" prop="columnName">
              <el-input v-model="form.columnName" placeholder="请输入列名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="列标题" prop="headerText">
              <el-input v-model="form.headerText" placeholder="请输入列标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="SQL表达式" prop="sqlColumn">
              <el-input v-model="form.sqlColumn" placeholder="可留空：默认按列名在主表解析" />
              <div class="el-form-item__tip" style="font-size: 11px; color: #909399; line-height: 1.5">
                推荐留空或只填列名（驼峰/下划线均可，如 warehouseName 或 warehouse_name），引擎自动在主表解析；
                高级写法（表名.列 / CASE WHEN / 子查询）原样支持
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="字段类型" prop="fieldType">
              <el-select
                v-model="form.fieldType"
                placeholder="请选择字段类型"
                clearable
                style="width: 100%"
              >
                <el-option
                  v-for="item in fieldTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="排序" prop="sortIndex">
              <el-input-number
                v-model="form.sortIndex"
                controls-position="right"
                :min="0"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="列宽" prop="columnWidth">
              <el-input v-model="form.columnWidth" placeholder="如 120px" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="对齐" prop="align">
              <el-select
                v-model="form.align"
                placeholder="对齐方式"
                clearable
                style="width: 100%"
              >
                <el-option label="左" :value="1" />
                <el-option label="中" :value="2" />
                <el-option label="右" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="参与搜索" prop="search">
              <el-radio-group v-model="form.search">
                <el-radio :label="1">是</el-radio>
                <el-radio :label="0">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="动态列" prop="dynamic">
              <el-radio-group v-model="form.dynamic">
                <el-radio :label="1">是</el-radio>
                <el-radio :label="0">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="是否显示" prop="isShow">
              <el-radio-group v-model="form.isShow">
                <el-radio :label="1">是</el-radio>
                <el-radio :label="0">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="8">
            <el-form-item label="PDA显示" prop="mobileShow">
              <el-radio-group v-model="form.mobileShow">
                <el-radio :label="1">是</el-radio>
                <el-radio :label="0">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="扫码输入" prop="scanInput">
              <el-radio-group v-model="form.scanInput">
                <el-radio :label="1">是</el-radio>
                <el-radio :label="0">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="PDA列序" prop="mobileSeq">
              <el-input-number v-model="form.mobileSeq" :min="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listFieldConfig,
  getFieldConfig,
  addFieldConfig,
  updateFieldConfig,
  delFieldConfig,
} from "@/api/system/fieldConfig";

export default {
  name: "FieldConfig",
  data() {
    return {
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
      // 字段配置表格数据
      fieldConfigList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 字段类型选项
      fieldTypeOptions: [
        { value: "text", label: "文本" },
        { value: "number", label: "数字" },
        { value: "date", label: "日期" },
        { value: "datetime", label: "日期时间" },
        { value: "select", label: "下拉" },
      ],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 20,
        name: undefined,
        moduleGroup: undefined,
        headerText: undefined,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        name: [
          {
            required: true,
            message: "模块名称不能为空",
            trigger: ["blur", "change"],
          },
        ],
        columnName: [
          {
            required: true,
            message: "列名不能为空",
            trigger: ["blur", "change"],
          },
        ],
        headerText: [
          {
            required: true,
            message: "列标题不能为空",
            trigger: ["blur", "change"],
          },
        ],
      },
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询字段配置列表 */
    getList() {
      this.loading = true;
      listFieldConfig(this.queryParams).then((response) => {
        this.fieldConfigList = response.rows;
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
        id: undefined,
        name: undefined,
        moduleGroup: undefined,
        columnName: undefined,
        headerText: undefined,
        sortIndex: 0,
        columnWidth: undefined,
        sqlColumn: undefined,
        search: 0,
        dynamic: 0,
        fieldType: undefined,
        align: 1,
        isShow: 1,
        mobileShow: 0,
        scanInput: 0,
        mobileSeq: undefined,
      };
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
      this.ids = selection.map((item) => item.id);
      this.single = selection.length != 1;
      this.multiple = !selection.length;
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加字段配置";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids;
      getFieldConfig(id).then((response) => {
        this.form = response.data;
        this.open = true;
        this.title = "修改字段配置";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate((valid) => {
        if (valid) {
          if (this.form.id != undefined) {
            updateFieldConfig(this.form).then((response) => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addFieldConfig(this.form).then((response) => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal
        .confirm('是否确认删除字段配置编号为"' + ids + '"的数据项？')
        .then(function () {
          return delFieldConfig(ids);
        })
        .then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        })
        .catch(() => {});
    },
  },
};
</script>
