import axios from "axios";
import { Notification, MessageBox, Message, Loading } from "element-ui";
import store from "@/store";
import { getToken } from "@/utils/auth";
import errorCode from "@/utils/errorCode";
import { tansParams, blobValidate } from "@/utils/ruoyi";
import cache from "@/plugins/cache";
import { saveAs } from "file-saver";

let downloadLoadingInstance;
// 是否显示重新登录
export let isRelogin = { show: false };

// 请求取消管理
const pendingRequests = new Map();

// 生成请求唯一标识
function generateRequestKey(config) {
  const { method, url, params, data } = config;
  return [method, url, JSON.stringify(params || {}), JSON.stringify(data || {})].join('&');
}

axios.defaults.headers["Content-Type"] = "application/json;charset=utf-8";
// 对应国际化资源文件后缀
axios.defaults.headers["Content-Language"] = "zh_CN";
// 创建axios实例
const service = axios.create({
  // axios中请求配置有baseURL选项，表示请求URL公共部分
  baseURL: process.env.VUE_APP_BASE_API,
  // 超时
  timeout: 600000,
});

// request拦截器
service.interceptors.request.use(
  (config) => {
    // 是否需要设置 token
    const isToken = (config.headers || {}).isToken === false;
    // 是否需要防止数据重复提交
    const isRepeatSubmit = (config.headers || {}).repeatSubmit === true;
    if (getToken() && !isToken) {
      config.headers["Authorization"] = "Bearer " + getToken(); // 让每个请求携带自定义token 请根据实际情况自行修改
    }
    // get请求映射params参数
    if (config.method === "get" && config.params) {
      let url = config.url + "?" + tansParams(config.params);
      url = url.slice(0, -1);
      config.params = {};
      config.url = url;
    }

    // 为每个请求创建 AbortController（支持取消）
    const controller = new AbortController();
    config.signal = controller.signal;
    const key = generateRequestKey(config);

    // 如果存在相同的请求，取消之前的（仅对非 GET 请求生效，GET 请求是幂等的，不应取消）
    if (config.method !== 'get' && pendingRequests.has(key)) {
      pendingRequests.get(key).abort();
      pendingRequests.delete(key);
    }
    pendingRequests.set(key, controller);
    config._requestKey = key;

    // if (
    //   !isRepeatSubmit &&
    //   (config.method === "post" || config.method === "put")
    // ) {
    //   const requestObj = {
    //     url: config.url,
    //     data:
    //       typeof config.data === "object"
    //         ? JSON.stringify(config.data)
    //         : config.data,
    //     time: new Date().getTime(),
    //   };
    //   const sessionObj = cache.session.getJSON("sessionObj");
    //   if (
    //     sessionObj === undefined ||
    //     sessionObj === null ||
    //     sessionObj === ""
    //   ) {
    //     cache.session.setJSON("sessionObj", requestObj);
    //   } else {
    //     const s_url = sessionObj.url; // 请求地址
    //     const s_data = sessionObj.data; // 请求数据
    //     const s_time = sessionObj.time; // 请求时间
    //     const interval = 100; // 间隔时间(ms)，小于此时间视为重复提交
    //     // const interval = 0; // 间隔时间(ms)，小于此时间视为重复提交
    //     if (
    //       s_data === requestObj.data &&
    //       requestObj.time - s_time < interval &&
    //       s_url === requestObj.url
    //     ) {
    //       const message = "数据正在处理，请勿重复提交";
    //       console.warn(`[${s_url}]: ` + message);
    //       return Promise.reject(new Error(message));
    //     } else {
    //       cache.session.setJSON("sessionObj", requestObj);
    //     }
    //   }
    // }
    return config;
  },
  (error) => {
    console.log(error);
    Promise.reject(error);
  }
);

// 响应拦截器
service.interceptors.response.use(
  (res) => {
    // 清理已完成的请求
    const key = res.config._requestKey;
    if (key) {
      pendingRequests.delete(key);
    }

    // 未设置状态码则默认成功状态
    const code = res.data.code || 200;
    // 获取错误信息
    const msg = errorCode[code] || res.data.msg || errorCode["default"];
    // 二进制数据则直接返回
    if (
      res.request.responseType === "blob" ||
      res.request.responseType === "arraybuffer"
    ) {
      return res.data;
    }
    if (code === 401) {
      if (!isRelogin.show) {
        isRelogin.show = true;
        MessageBox.confirm(
          "登录状态已过期，您可以继续留在该页面，或者重新登录",
          "系统提示",
          {
            confirmButtonText: "重新登录",
            cancelButtonText: "取消",
            type: "warning",
          }
        )
          .then(() => {
            isRelogin.show = false;
            store.dispatch("LogOut").then(() => {
              location.href = process.env.VUE_APP_CONTEXT_PATH + "index";
            });
          })
          .catch(() => {
            isRelogin.show = false;
          });
      }
      return Promise.reject("无效的会话，或者会话已过期，请重新登录。");
    } else if (code === 500) {
      Message({ message: msg, type: "error" });
      return Promise.reject(new Error(msg));
    } else if (code === 601) {
      Message({ message: msg, type: "warning" });
      return Promise.reject("error");
    } else if (code !== 200 && code !== 206) {
      Notification.error({ title: msg });
      return Promise.reject("error");
    } else {
      return res.data;
    }
  },
  (error) => {
    // 清理已取消/失败的请求
    if (error.config && error.config._requestKey) {
      pendingRequests.delete(error.config._requestKey);
    }

    // 如果是主动取消，不显示错误提示
    if (axios.isCancel(error) || error.name === 'CanceledError') {
      console.log('请求已取消:', error.message);
      return Promise.reject({ canceled: true });
    }

    console.log("err" + error);
    let { message } = error;
    if (message == "Network Error") {
      message = "后端接口连接异常";
    } else if (message.includes("timeout")) {
      message = "系统接口请求超时";
    } else if (message.includes("Request failed with status code")) {
      message = "系统接口" + message.substr(message.length - 3) + "异常";
    }
    Message({ message: message, type: "error", duration: 5 * 1000 });
    return Promise.reject(error);
  }
);

// 通用下载方法
export function download(url, params, filename, config) {
  downloadLoadingInstance = Loading.service({
    text: "正在下载数据，请稍候",
    spinner: "el-icon-loading",
    background: "rgba(0, 0, 0, 0.7)",
  });
  return service
    .post(url, params, {
      transformRequest: [
        (params) => {
          return tansParams(params);
        },
      ],
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      responseType: "blob",
      ...config,
    })
    .then(async (data) => {
      const isBlob = blobValidate(data);
      if (isBlob) {
        const blob = new Blob([data]);
        saveAs(blob, filename);
      } else {
        const resText = await data.text();
        const rspObj = JSON.parse(resText);
        const errMsg =
          errorCode[rspObj.code] || rspObj.msg || errorCode["default"];
        Message.error(errMsg);
      }
      downloadLoadingInstance.close();
    })
    .catch((r) => {
      // 下载请求被取消时不显示错误
      if (!r || !r.canceled) {
        console.error(r);
        Message.error("下载文件出现错误，请联系管理员！");
      }
      downloadLoadingInstance.close();
    });
}

// 取消指定请求
export function cancelRequest(config) {
  const key = generateRequestKey(config);
  const controller = pendingRequests.get(key);
  if (controller) {
    controller.abort();
    pendingRequests.delete(key);
  }
}

// 取消所有进行中的请求
export function cancelAllRequests() {
  pendingRequests.forEach(controller => controller.abort());
  pendingRequests.clear();
}

export default service;
