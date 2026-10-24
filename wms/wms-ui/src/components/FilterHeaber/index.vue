<template>
  <div style="display: inline-block" @click.stop>
    <el-popover
      ref="popover"
      placement="bottom"
      :title="column.label"
      trigger="click"
    >
      文件内容
      <span ref="reference"> {{ column.label }} </span>
    </el-popover>
  </div>
</template>

<script>
export default {
  name: "FilterHeaber",
  components: {},
  props: {
    column: {
      type: Object,
      defalut: () => {},
    },
  },
  data() {
    return {};
  },
  mounted() {
    console.log(column, "column");
  },
  computed: {},
  methods: {},
};
</script>

<style>
</style>
