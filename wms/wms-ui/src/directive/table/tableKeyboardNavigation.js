/*
 * @Author: lcl Liu15649960054@163.com
 * @Date: 2025-07-10 10:20:54
 * @LastEditors: lcl Liu15649960054@163.com
 * @LastEditTime: 2025-07-16 15:27:48
 * @FilePath: \wms\wms-ui\src\directive\table\tableKeyboardNavigation.js
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
export default {
  inserted(el, binding, vnode) {
    const tableBody = el.querySelector('.el-table__body-wrapper')
    if (!tableBody) return

    // 当前聚焦的单元格信息 {rowIndex, columnIndex}
    let currentCell = null

    // 键盘事件处理
    const handleKeyDown = (event) => {
      console.log(event, "event");
      // ArrowRight 向右
      // ArrowLeft 向左 "ArrowRight", "ArrowLeft"
      if (!['Enter', 'ArrowDown', 'ArrowUp',].includes(event.key) || !currentCell) return
      event.preventDefault()

      const { rowIndex, columnIndex } = currentCell
      console.log(columnIndex, "columnIndex");
      const tableData = vnode.componentInstance?.data || []
      let newRowIndex = rowIndex

      // 确定目标行
      if (event.key === 'ArrowUp') {
        newRowIndex = Math.max(0, rowIndex - 1)
      } else { // Enter 或 ArrowDown
        newRowIndex = Math.min(tableData.length - 1, rowIndex + 1)
      }

      // 如果行没有变化则不处理
      if (newRowIndex === rowIndex) return

      // 获取目标行的单元格
      const rows = tableBody.querySelectorAll('.el-table__body tbody tr')
      const targetRow = rows[newRowIndex]
      if (!targetRow) return

      const cells = targetRow.querySelectorAll('.el-table__cell')
      // 确保columnIndex在有效范围内
      const targetIndex = Math.min(columnIndex, cells.length - 1)
      const targetCell = cells[targetIndex]

      if (!targetCell) return

      const input = targetCell.querySelector('input')
      if (input) {
        // 更新当前焦点信息
        currentCell = {
          rowIndex: newRowIndex,
          columnIndex
        }
        input.focus()

        // 滚动到可见区域
        const inputRect = input.getBoundingClientRect()
        const tableRect = tableBody.getBoundingClientRect()
        if (inputRect.bottom > tableRect.bottom) {
          // 向下滚动
          tableBody.scrollTop += inputRect.bottom - tableRect.bottom + 10
        } else if (inputRect.top < tableRect.top) {
          // 向上滚动
          tableBody.scrollTop -= tableRect.top - inputRect.top + 10
        }
      }
    }

    // 点击事件处理
    const handleClick = (event) => {
      const cell = event.target.closest('.el-table__cell')
      if (!cell) return

      const row = cell.closest('tr')
      const rows = tableBody.querySelectorAll('.el-table__body tbody tr')
      const rowIndex = Array.from(rows).indexOf(row)

      if (rowIndex === -1) return

      // 通过单元格在行中的位置确定列索引
      const cells = row.querySelectorAll('.el-table__cell')
      const columnIndex = Array.from(cells).indexOf(cell)

      if (columnIndex !== -1) {
        currentCell = { rowIndex, columnIndex }
      }
    }

    tableBody.addEventListener('keydown', handleKeyDown)
    tableBody.addEventListener('click', handleClick)

    // 清理
    vnode.context.$once('hook:beforeDestroy', () => {
      tableBody.removeEventListener('keydown', handleKeyDown)
      tableBody.removeEventListener('click', handleClick)
    })
  }
}
