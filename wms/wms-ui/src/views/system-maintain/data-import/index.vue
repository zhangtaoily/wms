<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="24" :xs="24">
        <el-form
          :model="queryParams"
          ref="queryForm"
          size="small"
          :inline="true"
          v-show="showSearch"
          class="search-form"
          :rules="rules"
          style="    text-align: center;"
        >
          <!-- <el-form-item label="模块" prop="key">
            <el-select
              filterable
              v-model="queryParams.key"
              placeholder="请选择"
              clearable
            >
              <el-option
                v-for="dict in dictDataList.data_import_module"
                :key="dict.dictValue"
                :label="dict.dictLabel"
                :value="dict.dictValue"
              ></el-option>
            </el-select>
          </el-form-item> -->
          <el-form-item label="意向合同/销售合同单号" prop="code">
            <el-input
              v-model="queryParams.code"
              placeholder="请输入"
              clearable
            />
          </el-form-item>
          <el-form-item label="重复单号是否更新" prop="isUpdate">
            <el-switch
              v-model="queryParams.isUpdate"
              :active-value="1"
              :inactive-value="0"
            >
            </el-switch>
          </el-form-item>
          <!-- <el-form-item label="是否导入下游数据" prop="isSyncDownstream">
            <el-switch
              v-model="queryParams.isSyncDownstream"
              :active-value="1"
              :inactive-value="0"
            >
            </el-switch>
          </el-form-item> -->
        </el-form>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <DataSyncProgress :getQueryParams="getQueryParams" />
    </el-row>
  </div>
</template>

<script>
import DataSyncProgress from "@/components/DataSyncProgress/index.vue";
import { getDictDataList } from "@/api/system/dict/data";
export default {
  name: "Data-import",
  components: {
    DataSyncProgress,
  },
  created() {
    // 获取数据字段数据
    this.getDictData();
  },
  data() {
    return {
      // 显示搜索条件
      showSearch: true,
      // 查询参数
      queryParams: {
        key: null,
        code: null,
        isUpdate: 0,
        // isSyncDownstream: 0,
      },
      // 数据字典
      dicts: ["data_import_module"],
      dictDataList: [],
      rules: {
        key: [{ required: true, message: "模块不能为空", trigger: ["blur", "change", "input"]}],
        code: [{ required: true, message: "单号不能为空", trigger: ["blur", "change", "input"]}],
      },
    };
  },
  methods: {
    getQueryParams() {
      let queryParams;
      this.$refs["queryForm"].validate((valid) => {
        if (valid) {
          console.log("queryParams", this.queryParams);
          queryParams = this.queryParams;
        }
      });
      return queryParams;
    },
    // 获取全部数据字典数据
    getDictData() {
      return getDictDataList(this.dicts).then((res) => {
        this.setDictData(res.data);
        return res;
      });
    },
    // 修改数据字典
    setDictData(data) {
      this.dictDataList = data;
    },
  },
};
</script>

<style>
</style>