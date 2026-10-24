export default {
  bind(el, binding, vnode) {
  },
  update(el, binding, vnode, prevVnode) {
    const redFields = binding.value;
    if (!redFields) return;
    const columns = el.querySelectorAll(
      ".el-table__header-wrapper .el-table__cell"
    );
    columns.forEach((column) => {
      const label = column.querySelector(".cell");
      if (label) {
        let fieldName = label.innerText.trim();
        fieldName = fieldName.replace("*", "").trim();
        if (redFields.includes(fieldName)) {
          column.classList.add("red-label");
        } else {
          column.classList.remove("red-label");
        }
      }
    });
  },
  unbind(el) {
    const columns = el.querySelectorAll(
      ".el-table__header-wrapper .el-table__cell"
    );
    columns.forEach((column) => {
      column.classList.remove("red-label");
    });
  },
};
