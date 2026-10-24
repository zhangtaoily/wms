export default {
  bind(el, binding, vnode) {
    const redFields = binding.value;
    if (!redFields) return;
    const formItems = el.querySelectorAll(".el-form-item");
    formItems.forEach((item) => {
      const label = item.querySelector(".el-form-item__label");
      if (label) {
        const fieldName = label.getAttribute("for"); // 获取 for 属性的值
        if (redFields.includes(fieldName)) {
          item.classList.add("red-label");
        }
      }
    });
  },
  // 在绑定元素的父组件
  // 及他自己的所有子节点都更新后调用
  update(el, binding, vnode, prevVnode) {
    const redFields = binding.value;
    if (!redFields) return;
    const formItems = el.querySelectorAll(".el-form-item");
    formItems.forEach((item) => {
      const label = item.querySelector(".el-form-item__label");
      if (label) {
        const fieldName = label.getAttribute("for"); // 获取 for 属性的值
        if (redFields.includes(fieldName)) {
          item.classList.add("red-label");
        } else {
          item.classList.remove("red-label");
        }
      }
    });
  },
  unbind(el) {
    const formItems = el.querySelectorAll(".el-form-item");
    formItems.forEach((item) => {
      item.classList.remove("red-label");
    });
  },
};
