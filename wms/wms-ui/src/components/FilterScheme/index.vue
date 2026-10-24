<template>
  <el-row :gutter="5" class="filter-scheme-box">
    <el-col :span="1.5">
      <!-- 搜索方案按钮 -->
      <FilterSchemeBtn
        ref="filterSchemeBtnRef"
        :options="options"
        :functionName="functionName"
        @listChange="listChange"
        @setDefaultScheme="setDefaultScheme"
      />
    </el-col>
    <el-col :span="1.5">
      <!-- 搜索方案列表展示 -->
      <el-button
        v-for="item in showSchemeList"
        size="mini"
        :key="item.id"
        :class="{ 'is-selected': item.id == selectSchemeId }"
        @click="changeSelectSchemeHandler(item)"
        >{{ item.schemeName }}
        <span v-if="item.userId != $store.state.user.id"
          >({{ item.createBy }})</span
        ></el-button
      >
    </el-col>
  </el-row>
</template>

<script>
import { getDetails } from "@/api/system/common";
import FilterSchemeBtn from "./FilterSchemeBtn.vue";
export default {
  name: "FilterScheme",
  props: {
    // 接受可以过滤字段的参数
    options: {
      type: Array,
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
      // 搜索方案列表
      schemeList: [],
      // 选中的id
      selectSchemeId: null,
    };
  },
  components: { FilterSchemeBtn },
  mounted() {
    // 获取搜索方案
    this.$refs.filterSchemeBtnRef.getList();
  },
  computed: {
    // 过滤列表展示的方案
    showSchemeList() {
      return this.schemeList.filter((item) => item.isShow != 0);
    },
  },
  methods: {
    //搜索方案列表改变触发
    listChange(list) {
      this.schemeList = list;
      this.selectSchemeId = list.find((item) => item.isDefault == 1)?.id;
    },
    // 改变搜索方案
    changeSelectSchemeHandler(item) {
      if (item.id == this.selectSchemeId) {
        this.setDefaultScheme([]);
      } else {
        // 获取配置方案详情数据
        getDetails(this.variables.SYS_FILTER_SCHEME, item.id).then((res) => {
          this.setDefaultScheme(res.data);
        });
      }
    },
    // 修改查询条件
    setDefaultScheme(data) {
      // 赋值选中的id，高亮按钮
      this.selectSchemeId = data?.id;
      const detail = data["sys_filter_scheme_detail"];
      this.$emit("setDefaultScheme", detail || []);
    },
  },
};
</script>

<style lang="scss" scoped>
.filter-scheme-box {
  .is-selected {
    background-color: #409eff;
    color: white;
  }
}
</style>
