<!--
 * @Author: lcl Liu15649960054@163.com
 * @Date: 2025-07-17 16:50:34
 * @LastEditors: lcl Liu15649960054@163.com
 * @LastEditTime: 2025-08-14 12:38:20
 * @FilePath: \web2\wms-ui\src\components\FilterScheme\FilterSchemeBtn.vue
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
-->
<template>
  <div class="filter-scheme-btn-box">
    <el-button
      size="mini"
      icon="el-icon-setting"
      style="margin-right: 5px"
      @click="filterSchemeBtnHandler"
      >过滤方案</el-button
    >
    <!-- 弹窗 -->
    <el-dialog
      width="60%"
      title="过滤方案管理"
      :visible.sync="dialogVisible"
      class="filter-scheme-btn-dialog"
      append-to-body
    >
      <el-row>
        <el-col :span="6" class="dialog-left-box">
          <!-- 方案列表 -->
          <div class="button-group">
            <el-tree
              v-loading="listLoading"
              class="scheme-list-tree"
              :data="schemeList"
              ref="tree"
              node-key="id"
              @node-click="nodeClickHandler"
              highlight-current
              :render-content="renderContent"
            >
            </el-tree>
            <el-button
              :loading="loading"
              @click="addSchemeListDialogVisible = true"
            >
              添加
            </el-button>
          </div>
        </el-col>
        <el-col :span="18" class="dialog-right-box">
          <el-form
            :disabled="isFormDisabled"
            :model="form"
            :rules="rules"
            ref="form"
          >
            <div class="form-header-box">
              <el-form-item label="方案名称" prop="schemeName">
                <el-input v-model="form.schemeName"></el-input>
              </el-form-item>
              <el-form-item property="isDefault" width="80">
                <el-checkbox
                  label="默认查询"
                  :true-label="1"
                  :false-label="0"
                  v-model="form.isDefault"
                ></el-checkbox>
              </el-form-item>
              <el-form-item property="isShow" width="80">
                <el-checkbox
                  label="放置列表"
                  :true-label="1"
                  :false-label="0"
                  v-model="form.isShow"
                ></el-checkbox>
              </el-form-item>
              <el-form-item property="isShare" width="80">
                <el-checkbox
                  label="共享方案"
                  :true-label="1"
                  :false-label="0"
                  v-model="form.isShare"
                ></el-checkbox>
              </el-form-item>
            </div>
            <!-- 筛选条件 -->
            <SubTable
              :disabled="isFormDisabled"
              ref="subTableRef"
              :isOpenSort="true"
              :tableName="subTableName"
              :subData.sync="form[subTableName]"
              :selectedSonTableIndex.sync="selectedSonTableIndex"
              class="sub-table"
              :initObj="{ logicalOperator: 'AND', operate: 'EQ' }"
            >
              <template #table-column>
                <el-table-column
                  width="50"
                  label="且/或"
                  prop="logicalOperator"
                >
                  <template slot-scope="scope">
                    <el-select
                      v-model="scope.row.logicalOperator"
                      placeholder="请选择"
                      clearable
                    >
                      <el-option
                        v-for="item in [
                          { key: '且', val: 'AND' },
                          { key: '或', val: 'OR' },
                        ]"
                        :key="item.val"
                        :label="item.key"
                        :value="item.val"
                      ></el-option>
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="字段" prop="fieldConfigId">
                  <template slot-scope="scope">
                    <el-select
                      v-model="scope.row.fieldConfigId"
                      placeholder="请选择"
                      clearable
                      filterable
                      @change="fieldConfigIdChange(scope.row)"
                    >
                      <el-option
                        v-for="item in options"
                        :key="item.id"
                        :label="item.headerText"
                        :value="item.id"
                      ></el-option>
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column width="100" label="运算符" prop="operate">
                  <template slot-scope="scope">
                    <el-select
                      @change="operateChange(scope.row)"
                      v-model="scope.row.operate"
                      placeholder="请选择"
                      clearable
                    >
                      <el-option
                        v-for="item in getOperate(scope.row.fieldType)"
                        :key="item.val"
                        :label="item.key"
                        :value="item.val"
                      ></el-option>
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="值" prop="value">
                  <template slot-scope="scope">
                    <el-date-picker
                      :disabled="datePickerDisabled(scope.row.operate)"
                      v-if="
                        scope.row.fieldType &&
                        scope.row.fieldType.startsWith('DATE')
                      "
                      v-model="scope.row.value"
                      type="datetime"
                      placeholder="日期"
                      format="yyyy-MM-dd HH:mm:ss"
                      value-format="yyyy-MM-dd HH:mm:ss"
                      clearable
                      class="no-icon-date-picker"
                    >
                    </el-date-picker>
                    <el-input v-else v-model="scope.row.value"></el-input>
                  </template>
                </el-table-column>
              </template>
            </SubTable>
          </el-form>
          <div class="dialog-right-box-bottom">
            <el-button
              :disabled="isFormDisabled"
              size="small"
              type="primary "
              :loading="loading"
              @click="saveHandler"
              >保存本方案</el-button
            >
            <el-button
              :disabled="isFormDisabled"
              size="small"
              :loading="loading"
              @click="resetHandler"
              >重 置</el-button
            >
            <el-button
              :disabled="isFormDisabled"
              size="small"
              :loading="loading"
              @click="deletedHandler"
              >删除本方案</el-button
            >
          </div>
        </el-col>
      </el-row>
      <div slot="footer" class="dialog-footer">
        <el-button size="small" type="primary" @click="query">
          查 询
        </el-button>
        <el-button size="small" @click="dialogVisible = false">取 消</el-button>
      </div>
    </el-dialog>
    <!-- 新增列表弹窗 -->
    <el-dialog
      width="300px"
      title="新增方案"
      :visible.sync="addSchemeListDialogVisible"
      append-to-body
    >
      <el-form :model="schemeNameForm" ref="schemeNameFormRef">
        <el-form-item
          label="方案名称"
          prop="schemeName"
          :rules="rules.schemeName"
        >
          <el-input
            v-model="schemeNameForm.schemeName"
            autocomplete="off"
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="addSchemeListDialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="loading" @click="addHandler"
          >确 定</el-button
        >
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  save,
  getTableList,
  deleteById,
  getDetails,
} from "@/api/system/common";
import {
  allOperateList,
  operateMap,
  dateRangeOperateList,
} from "@/utils/variables.js";
export default {
  name: "FilterSchemeBtn",
  props: {
    // 接受可以过滤字段的参数
    options: {
      type: Array, // table数据
      default: () => [],
    },
    // 模块名称
    functionName: {
      type: String,
      default: () => null,
    },
  },
  data() {
    return {
      subTableName: "sys_filter_scheme_detail",
      selectedSonTableIndex: -1,
      // 过滤方案管理弹窗
      dialogVisible: false,
      // 表单数据
      form: {},
      // 添加方案表单
      schemeNameForm: {},
      // 方案列表数据
      schemeList: [],
      // 新增列表弹窗
      addSchemeListDialogVisible: false,
      // 按钮加载
      loading: false,
      // 列表加载
      listLoading: false,
      allOperateList: allOperateList,
      operateMap: operateMap,
      // 模块名称
      SYS_FILTER_SCHEME: this.variables.SYS_FILTER_SCHEME,
      // 表单校验
      rules: {
        schemeName: [
          {
            required: true,
            message: "方案名称不能为空",
            trigger: ["blur", "change"],
          },
        ],
      },
    };
  },
  computed: {
    isFormDisabled() {
      return !Boolean(this.form.id);
    },
  },
  methods: {
    // 点击过滤方案按钮
    filterSchemeBtnHandler() {
      // 发送请求获取列表
      this.getList().then(() => {
        this.$nextTick(() => {
          this.selectTreeRow(0);
        });
      });
      // 打开弹窗
      this.dialogVisible = true;
    },
    // 点击列表方法
    nodeClickHandler(data) {
      getDetails(this.SYS_FILTER_SCHEME, data.id)
        .then((res) => {
          this.form = res.data;
        })
        .finally(() => {});
    },
    // 添加
    addHandler() {
      this.$refs.schemeNameFormRef.validate((valid) => {
        if (valid) {
          this.loading = true;
          const data = {
            data: {
              functionName: this.functionName,
              schemeName: this.schemeNameForm.schemeName,
              userId: this.$store.state.user.id,
              isDefault: 0,
              isShow: 1,
              isShare: 0,
            },
          };
          save(this.SYS_FILTER_SCHEME, data)
            .then((res) => {
              this.$modal.msgSuccess("新增成功");
              this.addSchemeListDialogVisible = false;
              this.getList().then(() => {
                this.$nextTick(() => {
                  this.selectTreeRow(this.schemeList.length - 1);
                });
              });
              this.$refs.schemeNameFormRef.resetFields();
            })
            .finally(() => {
              this.loading = false;
            });
        } else {
          console.log("error submit!!");
          return false;
        }
      });
    },
    // 保存
    saveHandler() {
      if (this.form.createBy != this.$store.state.user.id) {
        this.$modal.msgError("共享方案不能修改");
        return;
      }
      this.$refs.form.validate((valid) => {
        if (valid) {
          this.loading = true;
          const data = {
            data: this.form,
          };
          save(this.SYS_FILTER_SCHEME, data)
            .then((res) => {
              this.getList().then(() => {
                this.$nextTick(() => {
                  this.$refs.tree.setCurrentKey(this.form.id);
                });
              });
              this.$modal.msgSuccess("保存成功");
            })
            .finally(() => {
              this.loading = false;
            });
        } else {
          console.log("error submit!!");
          return false;
        }
      });
    },
    // 复制
    copyHandler() {},
    // 删除
    deletedHandler() {
      this.$modal
        .confirmHTML('是否确认<span style="color: red;">删除</span>该方案')
        .then(() => {
          this.loading = true;
          return deleteById(this.SYS_FILTER_SCHEME, this.form.id);
        })
        .then(() => {
          this.form = {};
          this.loading = false;
          this.getList().then(() => {
            this.$nextTick(() => {
              this.selectTreeRow(0);
            });
          });
          this.$modal.msgSuccess("删除成功");
        })
        .catch(() => {})
        .finally(() => {
          this.loading = false;
        });
    },
    // 重置
    resetHandler() {
      this.$set(this.form, this.subTableName, []);
    },
    /**
     * 获取列表
     * @param isSelectFirstNode 是否选中第一个树🌲结点
     */
    async getList() {
      this.listLoading = true;
      let conditions = [
        {
          field: "functionName",
          value: this.functionName,
          operate: "EQ",
          fieldType: "TEXT",
          logicalOperator: "AND",
          filterGroup: "ADVANCE",
        },
        {
          field: "isShare",
          value: 1,
          operate: "EQ",
          fieldType: "TEXT",
          logicalOperator: "AND",
          filterGroup: "ADVANCE",
        },
        {
          field: "functionName",
          value: this.functionName,
          operate: "EQ",
          fieldType: "TEXT",
          logicalOperator: "OR",
          filterGroup: "ADVANCE",
        },
        {
          field: "userId",
          value: this.$store.state.user.id,
          operate: "EQ",
          fieldType: "TEXT",
          logicalOperator: "AND",
          filterGroup: "ADVANCE",
        },
      ];
      try {
        const response = await getTableList({
          functionName: this.SYS_FILTER_SCHEME,
          pageNum: 1,
          pageSize: 999,
          conditions,
        });
        this.schemeList = response.rows;
        // if(isSelectFirstNode){
        //   this.selectTreeRow(0)
        // }else{
        //   this.selectTreeRow(this.schemeList.length)
        // }
        console.log("listChange", this.schemeList);
        // 如果分享的方案，并且当前用户不是创建者，不是能是默认方案
        this.schemeList.forEach((item) => {
          if (item.isShare == 1 && item.userId != this.$store.state.user.id) {
            item.isDefault = 0;
          }
        })
        this.$emit("listChange", this.schemeList);
      } catch (error) {
        // 被后续相同请求取代而取消的，不视为错误（数据由后发出的请求返回）
        if (error && error.canceled) return;
        console.error("Error fetching list:", error);
      } finally {
        this.listLoading = false;
      }
      return this.schemeList;
    },
    // 选中树节点
    selectTreeRow(index) {
      this.$nextTick(() => {
        if (this.schemeList.length > 0 && this.$refs.tree) {
          const firstNode = this.$refs.tree.getNode(this.schemeList[index].id);
          if (firstNode) {
            this.$refs.tree.setCurrentKey(this.schemeList[index].id);
            this.nodeClickHandler(firstNode.data, firstNode, this.$refs.tree);
          }
        }
      });
    },
    // 渲染树节点内容
    renderContent(h, { node, data, store }) {
      let label = data.schemeName;
      if (data.isDefault === 1) {
        label += "（默认方案）";
      }
      if (data.isShare === 1 && data.userId != this.$store.state.user.id) {
        label += `（${data.createBy}）`;
      }
      let style = {};
      if (data.isDefault === 1) {
        style.color = "#409EFF";
      }
      return h("span", { style }, label);
    },
    // 查询
    query() {
      this.dialogVisible = false;
      this.$emit("setDefaultScheme", this.form);
    },
    // 字段发生改变
    fieldConfigIdChange(row) {
      const selectedId = row.fieldConfigId;
      const selectedItem = this.options.find((item) => item.id == selectedId);
      if (selectedItem) {
        this.$set(row, "columnName", selectedItem.columnName);
        this.$set(row, "fieldType", selectedItem.fieldType);
      }
    },
    // 获取操作符
    getOperate(type) {
      if (!type || (!type.startsWith("DATE") && !this.operateMap[type]))
        return this.allOperateList;
      return type.startsWith("DATE")
        ? this.operateMap.DATE
        : this.operateMap[type];
    },
    // 时间选择框禁用
    datePickerDisabled(operate) {
      return dateRangeOperateList.includes(operate);
    },
    operateChange(row) {
      // 如果是时间默认为-，否则置空规范时间选择器类型
      if (this.datePickerDisabled(row?.operate)) {
        this.$set(row, "value", "-");
      } else {
        this.$set(row, "value", "");
      }
    },
  },
};
</script>

<style lang="scss" scoped>
::v-deep .button-group {
  height: 500px;
  display: flex;
  justify-content: flex-start;
  align-items: center;
  flex-direction: column;
  column-gap: 20;
  .el-button {
    margin: 0px;
    margin-top: 10px;
    width: 80%;
  }
}
.curd-btn-box {
  display: flex;
  justify-content: center;
  align-items: center;
  border-bottom: 1px solid #ccc;
}
::v-deep .filter-scheme-btn-dialog {
  .el-dialog__header {
    padding: 3px;
    background: #f2f2f2;
    padding-bottom: 10px;
  }
  .el-dialog__body {
    padding: 0px !important;
  }
}
::v-deep .form-header-box {
  display: flex;
  justify-content: flex-start;
  align-content: center;
  column-gap: 20px;
  padding: 5px;
  padding-bottom: 14px;
  .el-form-item {
    display: flex;
    margin-bottom: 0px;
  }
}
.dialog-left-box {
  border-right: 1px solid #ccc;
}
.dialog-right-box {
  padding-left: 10px;
  height: 100%;
  padding-right: 10px;
  .dialog-right-box-bottom {
    margin-top: 10px;
    width: 100%;
    display: flex;
    justify-content: flex-end;
  }
}
.scheme-list-tree {
  width: 100%;
  text-align: center;
}
::v-deep .el-table__cell .el-date-editor {
  padding-left: 0px !important;
}
::v-deep .no-icon-date-picker {
  padding-left: 0px !important;
  .el-input__prefix {
    display: none;
  }
}
</style>
