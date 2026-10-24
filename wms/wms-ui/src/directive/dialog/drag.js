/**
* v-dialogDrag 弹窗拖拽
* Copyright (c) 浙江云梭纺织股份有限公司
*/

export default {
  bind(el, binding, vnode, oldVnode) {
    const value = binding.value
    if (value == false) return
    // 获取拖拽内容头部
    const dialogHeaderEl = el.querySelector('.el-dialog__header');
    const dragDom = el.querySelector('.el-dialog');
    dialogHeaderEl.style.cursor = 'move';
    // 获取原有属性 ie dom元素.currentStyle 火狐谷歌 window.getComputedStyle(dom元素, null);
    const sty = dragDom.currentStyle || window.getComputedStyle(dragDom, null);
    dragDom.style.position = 'absolute';
    dragDom.style.marginTop = 0;
    let width = dragDom.style.width;
    if (width.includes('%')) {
      width = +document.body.clientWidth * (+width.replace(/\%/g, '') / 100);
    } else {
      width = +width.replace(/\px/g, '');
    }
    dragDom.style.left = `${(document.body.clientWidth - width) / 2}px`;

    // 定义鼠标移动处理器（保存引用以便清理）
    el._mouseMoveHandler = (e) => {
      // 通过事件委托，计算移动的距离 （开始拖拽至结束拖拽的距离）
      const l = e.clientX - el._disX;
      const t = e.clientY - el._disY;
      let finallyL = l + el._styL
      let finallyT = t + el._styT
      // 移动当前元素
      dragDom.style.left = `${finallyL}px`;
      dragDom.style.top = `${finallyT}px`;
    };

    // 定义鼠标释放处理器
    el._mouseUpHandler = () => {
      document.removeEventListener('mousemove', el._mouseMoveHandler);
      document.removeEventListener('mouseup', el._mouseUpHandler);
    };

    // 鼠标按下事件
    dialogHeaderEl.onmousedown = (e) => {
      // 鼠标按下，计算当前元素距离可视区的距离 (鼠标点击位置距离可视窗口的距离)
      el._disX = e.clientX - dialogHeaderEl.offsetLeft;
      el._disY = e.clientY - dialogHeaderEl.offsetTop;

      // 获取到的值带px 正则匹配替换
      let styL, styT;

      // 注意在ie中 第一次获取到的值为组件自带50% 移动之后赋值为px
      if (sty.left.includes('%')) {
        styL = +document.body.clientWidth * (+sty.left.replace(/\%/g, '') / 100);
        styT = +document.body.clientHeight * (+sty.top.replace(/\%/g, '') / 100);
      } else {
        styL = +sty.left.replace(/\px/g, '');
        styT = +sty.top.replace(/\px/g, '');
      }
      el._styL = styL;
      el._styT = styT;

      // 添加事件监听器（使用 addEventListener）
      document.addEventListener('mousemove', el._mouseMoveHandler);
      document.addEventListener('mouseup', el._mouseUpHandler);
    }
  },

  // 新增 unbind 钩子 - 清理事件监听器
  unbind(el) {
    // 清理事件监听器
    if (el._mouseMoveHandler) {
      document.removeEventListener('mousemove', el._mouseMoveHandler);
      delete el._mouseMoveHandler;
    }
    if (el._mouseUpHandler) {
      document.removeEventListener('mouseup', el._mouseUpHandler);
      delete el._mouseUpHandler;
    }
    // 清理保存的状态
    delete el._disX;
    delete el._disY;
    delete el._styL;
    delete el._styT;
  }
};
