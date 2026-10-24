import { parseTime } from "./ruoyi";
import _ from "lodash";

/**
 * 表格时间格式化
 */
export function formatDate(cellValue, type = "datetime") {
  if (cellValue == null || cellValue == "") return "";
  var date = new Date(cellValue);
  var year = date.getFullYear();
  var month =
    date.getMonth() + 1 < 10
      ? "0" + (date.getMonth() + 1)
      : date.getMonth() + 1;
  var day = date.getDate() < 10 ? "0" + date.getDate() : date.getDate();
  var hours = date.getHours() < 10 ? "0" + date.getHours() : date.getHours();
  var minutes =
    date.getMinutes() < 10 ? "0" + date.getMinutes() : date.getMinutes();
  var seconds =
    date.getSeconds() < 10 ? "0" + date.getSeconds() : date.getSeconds();
  if (type === "datetime") {
    return (
      year +
      "-" +
      month +
      "-" +
      day +
      " " +
      hours +
      ":" +
      minutes +
      ":" +
      seconds
    );
  }
  if (type === "date") {
    return year + "-" + month + "-" + day;
  }
}

// string类型的数字保留几位小数 "99.000" => 99.00
export const formatNumDecimal = (num, keepDecimal = 2) => {
  console.log(
    "Number(num).toFixed(keepDecimal) :>> ",
    Number(num).toFixed(keepDecimal)
  );
  return num ? Number(num).toFixed(keepDecimal) : num;
};

/**
 * @param {number} time
 * @param {string} option
 * @returns {string}
 */
export function formatTime(time, option) {
  if (("" + time).length === 10) {
    time = parseInt(time) * 1000;
  } else {
    time = +time;
  }
  const d = new Date(time);
  const now = Date.now();

  const diff = (now - d) / 1000;

  if (diff < 30) {
    return "刚刚";
  } else if (diff < 3600) {
    // less 1 hour
    return Math.ceil(diff / 60) + "分钟前";
  } else if (diff < 3600 * 24) {
    return Math.ceil(diff / 3600) + "小时前";
  } else if (diff < 3600 * 24 * 2) {
    return "1天前";
  }
  if (option) {
    return parseTime(time, option);
  } else {
    return (
      d.getMonth() +
      1 +
      "月" +
      d.getDate() +
      "日" +
      d.getHours() +
      "时" +
      d.getMinutes() +
      "分"
    );
  }
}

/**
 * @param {string} url
 * @returns {Object}
 */
export function getQueryObject(url) {
  url = url == null ? window.location.href : url;
  const search = url.substring(url.lastIndexOf("?") + 1);
  const obj = {};
  const reg = /([^?&=]+)=([^?&=]*)/g;
  search.replace(reg, (rs, $1, $2) => {
    const name = decodeURIComponent($1);
    let val = decodeURIComponent($2);
    val = String(val);
    obj[name] = val;
    return rs;
  });
  return obj;
}

/**
 * @param {string} input value
 * @returns {number} output value
 */
export function byteLength(str) {
  // returns the byte length of an utf8 string
  let s = str.length;
  for (var i = str.length - 1; i >= 0; i--) {
    const code = str.charCodeAt(i);
    if (code > 0x7f && code <= 0x7ff) s++;
    else if (code > 0x7ff && code <= 0xffff) s += 2;
    if (code >= 0xdc00 && code <= 0xdfff) i--;
  }
  return s;
}

/**
 * @param {Array} actual
 * @returns {Array}
 */
export function cleanArray(actual) {
  const newArray = [];
  for (let i = 0; i < actual.length; i++) {
    if (actual[i]) {
      newArray.push(actual[i]);
    }
  }
  return newArray;
}

/**
 * @param {Object} json
 * @returns {Array}
 */
export function param(json) {
  if (!json) return "";
  return cleanArray(
    Object.keys(json).map((key) => {
      if (json[key] === undefined) return "";
      return encodeURIComponent(key) + "=" + encodeURIComponent(json[key]);
    })
  ).join("&");
}

/**
 * @param {string} url
 * @returns {Object}
 */
export function param2Obj(url) {
  const search = decodeURIComponent(url.split("?")[1]).replace(/\+/g, " ");
  if (!search) {
    return {};
  }
  const obj = {};
  const searchArr = search.split("&");
  searchArr.forEach((v) => {
    const index = v.indexOf("=");
    if (index !== -1) {
      const name = v.substring(0, index);
      const val = v.substring(index + 1, v.length);
      obj[name] = val;
    }
  });
  return obj;
}

/**
 * @param {string} val
 * @returns {string}
 */
export function html2Text(val) {
  const div = document.createElement("div");
  div.innerHTML = val;
  return div.textContent || div.innerText;
}

/**
 * Merges two objects, giving the last one precedence
 * @param {Object} target
 * @param {(Object|Array)} source
 * @returns {Object}
 */
export function objectMerge(target, source) {
  if (typeof target !== "object") {
    target = {};
  }
  if (Array.isArray(source)) {
    return source.slice();
  }
  Object.keys(source).forEach((property) => {
    const sourceProperty = source[property];
    if (typeof sourceProperty === "object") {
      target[property] = objectMerge(target[property], sourceProperty);
    } else {
      target[property] = sourceProperty;
    }
  });
  return target;
}

/**
 * @param {HTMLElement} element
 * @param {string} className
 */
export function toggleClass(element, className) {
  if (!element || !className) {
    return;
  }
  let classString = element.className;
  const nameIndex = classString.indexOf(className);
  if (nameIndex === -1) {
    classString += "" + className;
  } else {
    classString =
      classString.substr(0, nameIndex) +
      classString.substr(nameIndex + className.length);
  }
  element.className = classString;
}

/**
 * @param {string} type
 * @returns {Date}
 */
export function getTime(type) {
  if (type === "start") {
    return new Date().getTime() - 3600 * 1000 * 24 * 90;
  } else {
    return new Date(new Date().toDateString());
  }
}

/**
 * @param {Function} func
 * @param {number} wait
 * @param {boolean} immediate
 * @return {*}
 */
export function debounce(func, wait, immediate) {
  let timeout, args, context, timestamp, result;

  const later = function () {
    // 据上一次触发时间间隔
    const last = +new Date() - timestamp;

    // 上次被包装函数被调用时间间隔 last 小于设定时间间隔 wait
    if (last < wait && last > 0) {
      timeout = setTimeout(later, wait - last);
    } else {
      timeout = null;
      // 如果设定为immediate===true，因为开始边界已经调用过了此处无需调用
      if (!immediate) {
        result = func.apply(context, args);
        if (!timeout) context = args = null;
      }
    }
  };

  return function (...args) {
    context = this;
    timestamp = +new Date();
    const callNow = immediate && !timeout;
    // 如果延时不存在，重新设定延时
    if (!timeout) timeout = setTimeout(later, wait);
    if (callNow) {
      result = func.apply(context, args);
      context = args = null;
    }

    return result;
  };
}

/**
 * This is just a simple version of deep copy
 * Has a lot of edge cases bug
 * If you want to use a perfect deep copy, use lodash's _.cloneDeep
 * @param {Object} source
 * @returns {Object}
 */
export function deepClone(source) {
  if (!source && typeof source !== "object") {
    throw new Error("error arguments", "deepClone");
  }
  const targetObj = source.constructor === Array ? [] : {};
  Object.keys(source).forEach((keys) => {
    if (source[keys] && typeof source[keys] === "object") {
      targetObj[keys] = deepClone(source[keys]);
    } else {
      targetObj[keys] = source[keys];
    }
  });
  return targetObj;
}

/**
 * @param {Array} arr
 * @returns {Array}
 */
export function uniqueArr(arr) {
  return Array.from(new Set(arr));
}

/**
 * @returns {string}
 */
export function createUniqueString() {
  const timestamp = +new Date() + "";
  const randomNum = parseInt((1 + Math.random()) * 65536) + "";
  return (+(randomNum + timestamp)).toString(32);
}

/**
 * Check if an element has a class
 * @param {HTMLElement} elm
 * @param {string} cls
 * @returns {boolean}
 */
export function hasClass(ele, cls) {
  return !!ele.className.match(new RegExp("(\\s|^)" + cls + "(\\s|$)"));
}

/**
 * Add class to element
 * @param {HTMLElement} elm
 * @param {string} cls
 */
export function addClass(ele, cls) {
  if (!hasClass(ele, cls)) ele.className += " " + cls;
}

/**
 * Remove class from element
 * @param {HTMLElement} elm
 * @param {string} cls
 */
export function removeClass(ele, cls) {
  if (hasClass(ele, cls)) {
    const reg = new RegExp("(\\s|^)" + cls + "(\\s|$)");
    ele.className = ele.className.replace(reg, " ");
  }
}

export function makeMap(str, expectsLowerCase) {
  const map = Object.create(null);
  const list = str.split(",");
  for (let i = 0; i < list.length; i++) {
    map[list[i]] = true;
  }
  return expectsLowerCase ? (val) => map[val.toLowerCase()] : (val) => map[val];
}

export const exportDefault = "export default ";

export const beautifierConf = {
  html: {
    indent_size: "2",
    indent_char: " ",
    max_preserve_newlines: "-1",
    preserve_newlines: false,
    keep_array_indentation: false,
    break_chained_methods: false,
    indent_scripts: "separate",
    brace_style: "end-expand",
    space_before_conditional: true,
    unescape_strings: false,
    jslint_happy: false,
    end_with_newline: true,
    wrap_line_length: "110",
    indent_inner_html: true,
    comma_first: false,
    e4x: true,
    indent_empty_lines: true,
  },
  js: {
    indent_size: "2",
    indent_char: " ",
    max_preserve_newlines: "-1",
    preserve_newlines: false,
    keep_array_indentation: false,
    break_chained_methods: false,
    indent_scripts: "normal",
    brace_style: "end-expand",
    space_before_conditional: true,
    unescape_strings: false,
    jslint_happy: true,
    end_with_newline: true,
    wrap_line_length: "110",
    indent_inner_html: true,
    comma_first: false,
    e4x: true,
    indent_empty_lines: true,
  },
};

// 首字母大小
export function titleCase(str) {
  return str.replace(/( |^)[a-z]/g, (L) => L.toUpperCase());
}

// 下划转驼峰
export function camelCase(str) {
  return str.replace(/_[a-z]/g, (str1) => str1.substr(-1).toUpperCase());
}

export function isNumberStr(str) {
  return /^[+-]?(0|([1-9]\d*))(\.\d+)?$/g.test(str);
}
/**
 * @description 将url请求参数转为json格式
 * @param url
 * @returns {{}|any}
 */
export function paramObj(url) {
  const search = url.split("?")[1];
  if (!search) {
    return {};
  }
  return JSON.parse(
    '{"' +
    decodeURIComponent(search)
      .replace(/"/g, '\\"')
      .replace(/&/g, '","')
      .replace(/=/g, '":"')
      .replace(/\+/g, " ") +
    '"}'
  );
}
/**
 * @description 父子关系的数组转换成树形结构数据
 * @param data
 * @returns {*}
 */
export function translateDataToTree(data) {
  const parent = data.filter(
    (value) => value.parentId === "undefined" || value.parentId === null
  );
  const children = data.filter(
    (value) => value.parentId !== "undefined" && value.parentId !== null
  );
  const translator = (parent, children) => {
    parent.forEach((parent) => {
      children.forEach((current, index) => {
        if (current.parentId === parent.id) {
          const temp = JSON.parse(JSON.stringify(children));
          temp.splice(index, 1);
          translator([current], temp);
          typeof parent.children !== "undefined"
            ? parent.children.push(current)
            : (parent.children = [current]);
        }
      });
    });
  };
  translator(parent, children);
  return parent;
}

/**
 * @description 树形结构数据转换成父子关系的数组
 * @param data
 * @returns {[]}
 */
export function translateTreeToData(data) {
  const result = [];
  data.forEach((item) => {
    const loop = (data) => {
      result.push({
        id: data.id,
        name: data.name,
        parentId: data.parentId,
      });
      const child = data.children;
      if (child) {
        for (let i = 0; i < child.length; i++) {
          loop(child[i]);
        }
      }
    };
    loop(item);
  });
  return result;
}

/**
 * @description 10位时间戳转换年月日时分秒
 * @param time
 * @returns {string}
 */
export function tenBitTimestamp(time) {
  const date = new Date(time * 1000);
  const y = date.getFullYear();
  let m = date.getMonth() + 1;
  m = m < 10 ? "" + m : m;
  let d = date.getDate();
  d = d < 10 ? "" + d : d;
  let h = date.getHours();
  h = h < 10 ? "0" + h : h;
  let minute = date.getMinutes();
  let second = date.getSeconds();
  minute = minute < 10 ? "0" + minute : minute;
  second = second < 10 ? "0" + second : second;
  return y + "年" + m + "月" + d + "日 " + h + ":" + minute + ":" + second; //组合
}
/**
 * @description 10位时间戳转换时分秒
 * @param time
 * @returns {string}
 */
export function tenBitTimestampHms(time) {
  const date = new Date(time * 1000);
  let h = date.getHours();
  h = h < 10 ? "0" + h : h;
  let minute = date.getMinutes();
  let second = date.getSeconds();
  minute = minute < 10 ? "0" + minute : minute;
  second = second < 10 ? "0" + second : second;
  return h + ":" + minute + ":" + second; //组合
}

/**
 * @description 13位时间戳转换
 * @param time
 * @returns {string}
 */
export function thirteenBitTimestamp(time) {
  const date = new Date(time / 1);
  const y = date.getFullYear();
  let m = date.getMonth() + 1;
  m = m < 10 ? "" + m : m;
  let d = date.getDate();
  d = d < 10 ? "" + d : d;
  let h = date.getHours();
  h = h < 10 ? "0" + h : h;
  let minute = date.getMinutes();
  let second = date.getSeconds();
  minute = minute < 10 ? "0" + minute : minute;
  second = second < 10 ? "0" + second : second;
  return y + "年" + m + "月" + d + "日 " + h + ":" + minute + ":" + second; //组合
}

/**
 * @description 获取随机id
 * @param length
 * @returns {string}
 */
export function uuid(length = 32) {
  const num = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
  let str = "";
  for (let i = 0; i < length; i++) {
    str += num.charAt(Math.floor(Math.random() * num.length));
  }
  return str;
}

/**
 * @description m到n的随机数
 * @param m
 * @param n
 * @returns {number}
 */
export function random(m, n) {
  return Math.floor(Math.random() * (m - n) + n);
}

/**
 * @description addEventListener
 * @type {function(...[*]=)}
 */
export const on = (function () {
  return function (element, event, handler, useCapture = false) {
    if (element && event && handler) {
      element.addEventListener(event, handler, useCapture);
    }
  };
})();

/**
 * @description removeEventListener
 * @type {function(...[*]=)}
 */
export const off = (function () {
  return function (element, event, handler, useCapture = false) {
    if (element && event) {
      element.removeEventListener(event, handler, useCapture);
    }
  };
})();

/**
 * @description 数组打乱
 * @param array
 * @returns {*}
 */
export function shuffle(array) {
  let m = array.length,
    t,
    i;
  while (m) {
    i = Math.floor(Math.random() * m--);
    t = array[m];
    array[m] = array[i];
    array[i] = t;
  }
  return array;
}
/**
 * @description ele表格序号
 * @param number
 * @returns {string}
 */
export function indexMethod(index) {
  return index + 1;
}
/**
 * @description 获取当前时间并打印
 * @param null
 * @returns {string}
 */
export function getCurrentTime() {
  var _this = this;
  let yy = new Date().getFullYear();
  let mm = new Date().getMonth() + 1;
  let dd = new Date().getDate();
  let hh = new Date().getHours();
  let mf =
    new Date().getMinutes() < 10
      ? "0" + new Date().getMinutes()
      : new Date().getMinutes();
  let ss =
    new Date().getSeconds() < 10
      ? "0" + new Date().getSeconds()
      : new Date().getSeconds();
  return (_this.gettime =
    yy + "-" + mm + "-" + dd + " " + hh + ":" + mf + ":" + ss);
}
/**
 * @description 数组里对象根据某个值(包含姓名、字母、数字)排序
 * @param null
 * @returns {array}
 */
export function compare(data) {
  let chineseChars = [],
    chars = [],
    list = [];
  data.forEach((item) => {
    // 判断是否为中文
    if (/^[\u4e00-\u9fa5]*$/.test(item.nickname.charAt(0))) {
      chineseChars.push(item); // 姓名首字符为中文的
    } else {
      chars.push(item); // 姓名首字符非中文的（字母，数字）
    }
  });
  chars.sort((a, b) => a.nickname.charCodeAt(0) - b.nickname.charCodeAt(0));
  chineseChars.sort((a, b) => a.nickname.localeCompare(b.nickname));
  list = chars.concat(chineseChars); // list为最终想要的数组
  return list;
}
/**
 * 获取字符串中两个点中间的数据
 *
 * 例：// 测试函数
const str = "al_order_goods.0.color";
const numberBetweenDots = extractNumberBetweenDots(str);
console.log(numberBetweenDots); // 输出: 0
 */
export function extractNumberBetweenDots(str) {
  // 使用正则表达式匹配两个点之间的数字
  // \. 匹配点（`.` 在正则表达式中是特殊字符，所以需要用反斜杠`\`进行转义）
  // (\d+) 匹配一个或多个数字，并捕获它们
  const match = str.match(/\.(\d+)\./);

  // 如果找到了匹配项，则返回第一个捕获组（即两个点之间的数字）
  // match[1] 是因为 match[0] 是整个匹配项，而 match[1] 是第一个（也是在这个情况下唯一的）捕获组
  if (match) {
    return match[1];
  }

  // 如果没有找到匹配项，返回一个默认值或者null
  return null;
}

/**
 * 传入id列表，返回查询条件
 * @param {String} value id用逗号分隔的字符串
 */
export function getIdListConditions(value) {
  return [
    {
      field: "id",
      value,
      operate: "IN",
      fieldType: "TEXT",
      logicalOperator: "AND",
      filterGroup: "ADVANCE",
    },
  ];
}

// 深度对比两个对象并找出不同项
export function findDifferences(obj1, obj2) {
  const keys1 = Object.keys(obj1);
  const keys2 = Object.keys(obj2);
  const allKeys = _.union(keys1, keys2);

  const differences = {};

  allKeys.forEach((key) => {
    if (!_.isEqual(obj1[key], obj2[key])) {
      differences[key] = {
        value1: obj1[key],
        value2: obj2[key],
      };
    }
  });

  return differences;
}

// 深度对比两个对象并找出不同项,返回列表
export function findDifferencesList(obj1, obj2) {
  const keys1 = Object.keys(obj1);
  const keys2 = Object.keys(obj2);
  const allKeys = _.union(keys1, keys2);

  const differences = [];

  allKeys.forEach((key) => {
    if (!_.isEqual(obj1[key], obj2[key])) {
      differences.push(key);
    }
  });

  return differences;
}

export function updateList(functionName) {
  console.log(functionName, "2323");

  localStorage.setItem("needUpdatedFunctionName", "");
  localStorage.setItem("needUpdatedFunctionName", functionName);
}
