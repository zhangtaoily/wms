<template>
  <div class="app-container">
    <DynamicListPage
      :disabledButtonList="disabledButtonList"
      :openRadioSelection="false"
    />
  </div>
</template>

<script>
import DynamicListPage from '@/views/demo/dynamic-list-demo/index'

export default {
  name: "SalOrgStockList",
  components: {
    DynamicListPage,
  },

  data() {
    return {
      disabledButtonList: [
        "add",
        "copy",
        "pushDown",
        "delete",
        "update",
        "export",
        "print",
      ],
      
    };
  },
  created() {
  },
  methods: {
  },
};
</script>
<style lang="scss" scoped>
</style>

