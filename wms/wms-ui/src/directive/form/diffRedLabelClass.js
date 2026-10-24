export default {
  bind(el, binding, vnode) {
    const data = binding.value;
    const originData = data[0];
    const diffData = data[1];
    console.log("data", data);
    if (!data) return;
    const formItems = el.querySelectorAll(".el-form-item");
    formItems.forEach((item) => {
      const label = item.querySelector(".el-form-item__label");
      if (label) {
        const fieldName = label.getAttribute("for"); // 获取 for 属性的值
        if (originData[fieldName] || diffData[fieldName]) {
          // 将值转换为数字进行比较，如果转换失败则直接比较字符串
          const value1 = Number(originData[fieldName]);
          const value2 = Number(diffData[fieldName]);

          if (isNaN(value1) || isNaN(value2)) {
            // 如果转换失败，直接比较字符串
            if (originData[fieldName] != diffData[fieldName]) {
              console.log(
                fieldName,
                originData[fieldName],
                diffData[fieldName]
              );
              item.classList.add("red-label");
            }
          } else {
            // 如果转换成功，比较数字
            if (value1.toFixed(4) != value2.toFixed(4)) {
              console.log(
                fieldName,
                originData[fieldName],
                diffData[fieldName]
              );
              item.classList.add("red-label");
            }
          }
        }
      }
    });
  },
  // 在绑定元素的父组件
  // 及他自己的所有子节点都更新后调用
  update(el, binding, vnode, prevVnode) {
    const data = binding.value;
    const originData = data[0];
    const diffData = data[1];
    console.log("data", data);
    if (!data) return;
    const formItems = el.querySelectorAll(".el-form-item");
    formItems.forEach((item) => {
      const label = item.querySelector(".el-form-item__label");
      if (label) {
        const fieldName = label.getAttribute("for"); // 获取 for 属性的值
        if (originData[fieldName] || diffData[fieldName]) {
          // 将值转换为数字进行比较，如果转换失败则直接比较字符串
          const value1 = Number(originData[fieldName]);
          const value2 = Number(diffData[fieldName]);

          if (isNaN(value1) || isNaN(value2)) {
            // 如果转换失败，直接比较字符串
            if (originData[fieldName] != diffData[fieldName]) {
              console.log(
                fieldName,
                originData[fieldName],
                diffData[fieldName]
              );
              item.classList.add("red-label");
            }
          } else {
            // 如果转换成功，比较数字
            if (value1.toFixed(4) != value2.toFixed(4)) {
              console.log(
                fieldName,
                originData[fieldName],
                diffData[fieldName]
              );
              item.classList.add("red-label");
            }
          }
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
