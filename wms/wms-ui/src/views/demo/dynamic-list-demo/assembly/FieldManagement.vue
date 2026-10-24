<template>
  <el-dropdown class="ml-10 mr-10" trigger="hover">
    <el-tooltip effect="dark" content="字段管理" placement="top">
      <el-button type="primary" plain size="mini">字段管理</el-button>
    </el-tooltip>
    <el-dropdown-menu slot="dropdown">
      <el-row :gutter="5" type="flex" class="row-flex">
        <el-col :span="6">
          <el-checkbox :indeterminate="isIndeterminate" v-model="checkAll" @change="handleCheckAllChange">
            勾选您要显示的字段
          </el-checkbox>
        </el-col>
        <el-divider />
        <el-checkbox-group v-if="options.length > 0" v-model="checkList" @change="handleCheckedChange">
          <zn-draggable v-bind="dragOptions" :list="options" class="zn-draggable" v-model="options" @sort="dragSort">
            <el-col :span="24" class="checkbox-group-col" v-for="item in options" :key="item.id">
              <!-- <zn-icon icon="drag-drop-line" /> -->
              <el-checkbox :label="item.headerText">
                {{ item.headerText }}
              </el-checkbox>
            </el-col>
          </zn-draggable>
        </el-checkbox-group>
      </el-row>
    </el-dropdown-menu>
  </el-dropdown>
</template>

<script>
import ZnDraggable from 'vuedraggable'
export default {
  name: 'fieldManagement',
  components: {
    ZnDraggable,
  },
  inject: ['mark'],
  props: {
    options: {
      type: Array, // table数据
      default: () => [],
    },
  },
  data() {
    return {
      checkAll: false,
      checkList: [],
      // options: [],
      isIndeterminate: true,
    }
  },
  computed: {
    dragOptions() {
      return {
        animation: 600,
        group: 'description',
      }
    },
  },
  watch: {},
  created() {
    this.getHeader()
  },
  mounted() { },
  methods: {
    dragSort(e) {
      // e.newIndex 为draggable插件给的值
      this.options[e.newIndex].sortIndex = e.newIndex + 1;

      // 重新排序
      this.options.map((item, index) => {
        item.sortIndex = index + 1;
      })
      //对checkList进行排序
      let sortCheck = [];
      this.options.map((item) => {
        if (this.checkList.includes(item.headerText)) {
          sortCheck.push(item)
        }
      })
      let checks = []
      sortCheck.map((item) => {
        checks.push(item.headerText)
      })
      this.$emit('setColumns', this.options, checks)
    },

    // 字段管理的接口是统一的 , 只有业务模块的mark标识不同 , 所以请求就写在了组件里
    async getHeader() {

      this.options.map((item) => {
        // if (item.isShow == true) {
          this.checkList.push(item.headerText)
        // }
      })
      this.checkAll = this.checkList.length === this.options.length
      this.isIndeterminate = this.checkList.length > 0 && this.checkList.length < this.options.length
      this.$emit('setColumns', this.options, this.checkList)
    },
    // 操纵全选
    handleCheckAllChange(val) {
      if (val) {
        this.options.map((item) => {
          this.checkList.push(item.headerText)
        })
      } else {
        this.checkList = []
      }
      this.isIndeterminate = false
      // 向父组件发送数据
      this.$emit('setColumns', this.options, this.checkList)
    },
    // 单个数据选中
    handleCheckedChange(value) {
      let checkedCount = value.length
      this.checkAll = checkedCount === this.options.length
      this.isIndeterminate =
        checkedCount > 0 && checkedCount < this.options.length
      // 向父组件发送数据
      this.$emit('setColumns', this.options, this.checkList)
    },
  },
}
</script>

<style lang="scss" scoped>
.row-flex {
  padding: 15px;
  display: flex;
  flex-direction: column;
}

.zn-draggable {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
}

.checkbox-group-col {
  max-height: 100px;
  overflow-y: auto;

  .el-checkbox {
    padding: 3px 0;
    width: 100%;
    margin-right: 0;

  }
}

.icon-class:hover {
  opacity: 0.5;
  transition: 1s;
  transform: scale(1.3);
}
</style>
