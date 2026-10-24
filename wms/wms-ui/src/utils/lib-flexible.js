export function flexible(window, document, flag) {
  // 存储事件监听器引用
  let domContentLoadedListener = null;
  let pageShowListener = null;

  // 调整 body 字体大小
  var docEl = document.documentElement;
  var dpr = window.devicePixelRatio || 1;

  // 清理所有事件监听器
  function cleanupListeners() {
    // 移除 DOMContentLoaded 监听
    if (domContentLoadedListener) {
      document.removeEventListener("DOMContentLoaded", domContentLoadedListener);
      domContentLoadedListener = null;
    }

    // 移除 pageshow 监听
    if (pageShowListener) {
      window.removeEventListener("pageshow", pageShowListener);
      pageShowListener = null;
    }
  }

  // 设置 body 字体大小（重构版）
  function setBodyFontSize() {
    if (document.body) {
      document.body.style.fontSize = 16 * dpr + "px";
    }
  }

  // 清理模式
  if (!flag) {
    // 移除样式
    docEl.style.removeProperty("font-size");
    if (document.body) {
      document.body.style.removeProperty("font-size");
    }

    // 移除可能添加的 hairlines 类
    docEl.classList.remove("hairlines");

    // 清理所有事件监听器
    cleanupListeners();
    return;
  }

  // 设置模式 - 先清理旧监听器
  cleanupListeners();

  // 智能设置 body 字体大小
  if (document.readyState === "complete" || document.readyState === "interactive") {
    setBodyFontSize();
  } else {
    domContentLoadedListener = setBodyFontSize;
    document.addEventListener("DOMContentLoaded", domContentLoadedListener);
  }

  // 设置 rem 单位
  function setRemUnit() {
    var rem = docEl.clientWidth / 24;
    docEl.style.fontSize = rem + "px";
  }

  setRemUnit();

  // 页面显示时重置 rem 单位
  pageShowListener = function (e) {
    if (e.persisted) {
      setRemUnit();
    }
  };
  window.addEventListener("pageshow", pageShowListener);

  // 检测 0.5px 支持
  if (dpr >= 2) {
    // 使用 IIFE 封装避免变量泄露
    (function detectHairline() {
      var fakeBody = document.createElement("body");
      var testElement = document.createElement("div");

      testElement.style.border = ".5px solid transparent";
      fakeBody.appendChild(testElement);
      docEl.appendChild(fakeBody);

      if (testElement.offsetHeight === 1) {
        docEl.classList.add("hairlines");
      }

      docEl.removeChild(fakeBody);
    })();
  }

  // 返回清理函数，供外部调用
  return cleanupListeners;
}
// export function flexible(window, document, flag) {
//   // adjust body font size
//   var docEl = document.documentElement;
//   var dpr = window.devicePixelRatio || 1;
//   function setBodyFontSize() {
//     if (document.body) {
//       document.body.style.fontSize = 16 * dpr + "px";
//     } else {
//       document.addEventListener("DOMContentLoaded", setBodyFontSize);
//     }
//   }
//   if (!flag) {
//     docEl.style.removeProperty("font-size");
//     document.body.style.removeProperty("font-size");
//     // window.removeEventListener("resize", setBodyFontSize);
//     return;
//   }
//   setBodyFontSize();

//   function setRemUnit() {
//     var rem = docEl.clientWidth / 24;
//     docEl.style.fontSize = rem + "px";
//   }

//   setRemUnit();

//   // reset rem unit on page resize
//   // window.addEventListener("resize", setRemUnit);
//   window.addEventListener("pageshow", function (e) {
//     if (e.persisted) {
//       setRemUnit();
//     }
//   });

//   // detect 0.5px supports
//   if (dpr >= 2) {
//     var fakeBody = document.createElement("body");
//     var testElement = document.createElement("div");
//     testElement.style.border = ".5px solid transparent";
//     fakeBody.appendChild(testElement);
//     docEl.appendChild(fakeBody);
//     if (testElement.offsetHeight === 1) {
//       docEl.classList.add("hairlines");
//     }
//     docEl.removeChild(fakeBody);
//   }
// }
