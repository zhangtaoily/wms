export default {
  bind(el, binding, vnode) {},
  update(el, binding, vnode, prevVnode) {
    const redFields = binding.value;
    if (!redFields) return;
    const columns = el.querySelectorAll(
      ".el-table__header-wrapper .el-table__cell"
    );
    // 创建一个Map来存储field中每个item的索引，提高查找效率
    const fieldIndexMap = new Map();
    columns.forEach((column, index) => {
      const label = column.querySelector(".cell");
      if (label) {
        let fieldName = label.innerText.trim();
        // 去除星号并再次去除前后空格
        fieldName = fieldName.replace("*", "").trim(); 
        fieldIndexMap.set(fieldName, index);
      }
    });
    const rows = el.querySelectorAll(".el-table__row");
    if (!rows || rows.length == 0) {
      return;
    }
    redFields.forEach((fieldRows, i) => {
      const fieldItems = rows[i].querySelectorAll(".el-table__cell");
      fieldRows.forEach((item) => {
        // 从Map中获取item的索引
        const index = fieldIndexMap.get(item);

        // 如果找到了索引，则添加类名
        if (index !== undefined && index < fieldItems.length) {
          fieldItems[index].classList.add("table-red-label");
        }
      });
    });
  },
  unbind(el) {},
};
