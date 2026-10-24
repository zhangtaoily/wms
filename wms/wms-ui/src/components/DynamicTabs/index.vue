<template>
  <el-tabs type="border-card" class="tabs">
    <el-tab-pane v-for="(item, index) in paneList" :key="item" :label="item">
      <slot :name="'pane' + (index + 1)"></slot>
    </el-tab-pane>
  </el-tabs>
</template>

<script>
export default {
  props: {
    paneList: {
      type: Array,
      default: () => [],
    },
  },
};
</script>

<style scoped lang="scss">
.tabs {
  margin-top: 20px;
}
</style>
