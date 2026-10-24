<template>
  <div class="filter-header">
    <el-popover
      popper-class="filter-header-popover"
      placement="bottom"
      width="280"
      ref="popover"
      trigger="manual"
      v-model="showPanel"
      @show="handleVisibleChange"
    >
      <!-- 右下角过滤图标 -->
      <span v-show="showIcon" slot="reference" ref="trigger" class="filter-icon" @click="togglePanel">
      <svg-icon icon-class="filter" style="width: 1.4em; height: 1.4em;"/>
    </span>

      <!-- 弹出框 -->
      <div v-if="showPanel" class="filter-panel">
        <div class="filter-search">
          <el-input type="text" v-model="searchText" placeholder="搜索" clearable>
            <el-button slot="append" icon="el-icon-search"></el-button>
          </el-input>
        </div>

        <div class="filter-options">
          <label>
            <input type="checkbox" v-model="selectAll" @change="toggleAll"/>
            全选
          </label>
          <div
            v-for="item in filteredOptions"
            :key="item"
          >
            <label>
              <input
                type="checkbox"
                :value="item"
                v-model="selected"
                @change="handleChange"
              />
              {{ item }}
            </label>
          </div>
        </div>

        <div class="filter-summary">
          已选 {{ selected.length }} 项
        </div>

        <div class="filter-actions">
          <el-button
            class="btn-confirm" type="primary" size="medium" @click="confirm"
          >确定
          </el-button>
          <el-button
            class="btn-clear" size="medium" @click="clear"
          >清除
          </el-button>
        </div>
      </div>
    </el-popover>
  </div>
</template>

<script>
/**
 * 1.列表过滤器组件
 * 2.需要保存状态，输入框的状态
 * 3.如果选中的值，需要显示icon，否则隐藏，鼠标经过列表label框会显示icon
 * 4.对列表列数据进行分组，可以多选，第一个是全选 框
 * 5.需要显示‘已选择几项’
 * 6.有确认和清除按钮
 * 7.icon固定在右下角，点击后显示过滤器弹出框组件
 */
export default {
  name: 'TableHeaderFilter',
  props: {
    options: {
      type: Array,
      required: true
    },
    column: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      // 是否显示icon
      showIcon: false,
      showPanel: false,
      searchText: '',
      selected: [],
      selectAll: false
    }
  },
  computed: {
    filteredOptions() {
      return this.options.filter((item) =>
        String(item).toLowerCase().includes(String(this.searchText).toLowerCase())
      )
    }
  },
  watch: {
    selected(val) {
      this.selectAll = val.length === this.filteredOptions.length
      this.showIcon = val.length > 0
    },
    filteredOptions(val) {
      this.selectAll = val.length > 0 && val.every((item) => this.selected.includes(item))
    }
  },
  methods: {
    togglePanel() {
      this.showPanel = !this.showPanel
      if (this.showPanel) {
        // 添加全局点击事件监听
        this.$nextTick(() => {
          setTimeout(() => {
            document.addEventListener('click', this.handleClickOutside)
          }, 0)
        })
      } else {
        document.removeEventListener('click', this.handleClickOutside)
      }
    },
    toggleAll() {
      if (this.selectAll) {
        this.selected = [...this.filteredOptions]
      } else {
        this.selected = []
      }
    },
    handleChange() {
      this.selectAll = this.filteredOptions.every((item) => this.selected.includes(item))
    },
    confirm() {
      this.showPanel = false
      this.$emit('confirm', this.column, this.selected)
    },
    clear() {
      this.selected = []
      this.selectAll = false
      this.showPanel = false
      this.$emit('confirm', this.column, [])
    },
    handleClickOutside(event) {
      const popoverEl = document.querySelector('.filter-header-popover')
      const triggerEl = this.$refs.trigger

      if (
        !(popoverEl && popoverEl.contains(event.target)) &&
        !triggerEl.contains(event.target)
      ) {
        this.showPanel = false
        document.removeEventListener('click', this.handleClickOutside)
      }
    },
    handleVisibleChange() {
      // 弹窗刚刚打开
      this.$emit('visible-change', true)
    }
  },
  beforeDestroy() {
    // 组件销毁前移除事件监听
    document.removeEventListener('click', this.handleClickOutside)
  }
}
</script>

<style lang="scss" scoped>
.filter-header {
  position: absolute;
  bottom: 0;
  right: 7px;
  display: inline-block;
}

.filter-icon {
  cursor: pointer;
}

.filter-panel {
  width: 260px;
  background: #fff;
  padding: 10px;
  z-index: 1000;
  min-height: 150px;
}

.filter-search {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  position: relative;
}

.filter-search input {
  width: 100%;
  padding: 5px 25px 5px 5px;
  box-sizing: border-box;
}

.search-icon {
  position: absolute;
  right: 5px;
  top: 5px;
}

.filter-options {
  max-height: 160px;
  overflow-y: auto;
  margin-bottom: 10px;
}

.filter-summary {
  font-size: 12px;
  color: #888;
  margin-bottom: 10px;
}

.filter-actions {
  display: flex;
  justify-content: space-between;
  gap: 10px; /* 两个按钮之间的间距，根据需要调整 */
  margin-top: 10px; /* 视情况增加间距 */
}

.filter-actions .el-button {
  flex: 1;
  text-align: center;
  height: 30px;
}

.btn-confirm {
  background-color: #3b9cff;
  color: white;
  border: none;
  padding: 5px 10px;
  border-radius: 4px;
}

.btn-clear {
  background-color: #f5f5f5;
  color: #333;
  border: none;
  padding: 5px 10px;
  border-radius: 4px;
}
</style>
