<script>
import { TableColumn } from 'element-ui'

const renderCell = {
  slots (h, data) {
    // 接受传入的renderCell函数
    const renderCell = (h, data) => {
      return this.renderCell ? this.renderCell(data) : ''
    }
    return <div class="cell">{renderCell(h, data)}</div>
  }
  // 其他类型暂未扩展
}

export default {
  extends: TableColumn, // 继承el-table-column
  props: {
    cellType: {
      type: String,
      validator (value) {
        const valid = ['slots'].includes(value)
        !valid && console.error(`dd-table-cloumn组件不适配 ${value} 类型，适配类型有'slots'`)
        return valid
      }
    },
    renderCell: Function,
    mousedown: Function,
    mousemove: Function,
    isLastColumn: Boolean
  },
  created () {
    // 对于'expand', 'selection', 'index'三类特殊列不执行自定义渲染，使用原生的功能
    if (['expand', 'selection', 'index'].includes(this.type)) return
    if (renderCell[this.cellType]) {
      this.columnConfig.renderCell = renderCell[this.cellType].bind(this)
    }
    this.columnConfig.renderHeader = (h, { column, $index }) => {
      const headContent = this.renderHeader ? this.renderHeader(h, { column, $index }) : column.label
      return h(
        'div',
        {
          // 在.thead-cell上绑定鼠标按下和移动事件，fixed-cell用于展示禁止落点的鼠标图标
          // class: ['thead-cell', column.fixed ? 'fixed-cell' : '', this.isLastColumn ? 'last-column' : ''],
          // on: {
          //   mousedown: $event => {
          //     this.mousedown($event, column, $index)
          //   },
          //   mousemove: $event => {
          //     this.mousemove($event, column, $index)
          //   }
          // }
        },
        [
          // 添加 <a> 用于显示表头，其上添加stopPropagation()上是为了让表头的按钮点击事件禁止冒泡（否则可能会触发el-table的排序）
          column.fixed
            ? h('span', {}, [headContent])
            : h(
              'a',
              {
                on: {
                  click: e => {
                    // e.stopPropagation()
                  }
                }
              },
              [headContent]
            ),
          // 为非冻结列添加一个空标签用于显示拖动中的目标位置虚线
          column.fixed
            ? ''
            : h('span', {
              class: ['virtual']
            }),
          // 为最后一列添加自定义显示的编辑按钮
          this.isLastColumn
            ? h('span', {
              class: 'el-icon-edit',
              on: {
                click: e => {
                  e.stopPropagation()
                  this.$emit('customDisplay')
                }
              }
            })
            : ''
        ]
      )
    }
  }
}
</script>
