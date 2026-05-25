import axios from "axios";
import {
  ElNotification,
  ElMessageBox,
  ElMessage,
  ElLoading,
} from "element-plus";
import { getToken } from "@/utils/auth";
import errorCode from "@/utils/errorCode";
import { tansParams, blobValidate } from "@/utils/ruoyi";
import cache from "@/plugins/cache";
import { saveAs } from "file-saver";
import useUserStore from "@/store/modules/user";

let downloadLoadingInstance;
// 是否显示重新登录
export let isRelogin = { show: false };

axios.defaults.headers["Content-Type"] = "application/json;charset=utf-8";
// 创建axios实例
const service = axios.create({
  // axios中请求配置有baseURL选项，表示请求URL公共部分
  baseURL: import.meta.env.VITE_APP_BASE_API,
  // 超时
  timeout: 600000,
});

// request拦截器
service.interceptors.request.use(
  (config) => {
    // 是否需要设置 token
    const isToken = (config.headers || {}).isToken === false;
    // 是否需要防止数据重复提交
    const isRepeatSubmit = (config.headers || {}).repeatSubmit === false;
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
    if (
      !isRepeatSubmit &&
      (config.method === "post" || config.method === "put")
    ) {
      const requestObj = {
        url: config.url,
        data:
          typeof config.data === "object"
            ? JSON.stringify(config.data)
            : config.data,
        time: new Date().getTime(),
      };
      const requestSize = Object.keys(JSON.stringify(requestObj)).length; // 请求数据大小
      const limitSize = 5 * 1024 * 1024; // 限制存放数据5M
      if (requestSize >= limitSize) {
        console.warn(
          `[${config.url}]: ` +
            "请求数据大小超出允许的5M限制，无法进行防重复提交验证。"
        );
        return config;
      }
      const sessionObj = cache.session.getJSON("sessionObj");
      if (
        sessionObj === undefined ||
        sessionObj === null ||
        sessionObj === ""
      ) {
        cache.session.setJSON("sessionObj", requestObj);
      } else {
        const s_url = sessionObj.url; // 请求地址
        const s_data = sessionObj.data; // 请求数据
        const s_time = sessionObj.time; // 请求时间
        const interval = 1000; // 间隔时间(ms)，小于此时间视为重复提交
        if (
          s_data === requestObj.data &&
          requestObj.time - s_time < interval &&
          s_url === requestObj.url
        ) {
          const message = "数据正在处理，请勿重复提交";
          console.warn(`[${s_url}]: ` + message);
          return Promise.reject(new Error(message));
        } else {
          cache.session.setJSON("sessionObj", requestObj);
        }
      }
    }
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
      // 401 未授权，跳转登录
      if (!isRelogin.show) {
        isRelogin.show = true;
        ElMessageBox.confirm(
          "登录状态已过期，您可以继续留在该页面，或者重新登录",
          "系统提示",
          {
            confirmButtonText: "重新登录",
            cancelButtonText: "取消",
            type: "warning",
            closeOnClickModal: false, // 禁止点击遮罩关闭
          }
        )
          .then(() => {
            isRelogin.show = false;
            useUserStore()
              .logOut()
              .then(() => {
                // 使用 window.location 确保完整页面刷新，清除所有状态
                window.location.href = "/admin/index";
                window.location.reload();
              })
              .catch(() => {
                window.location.href = "/admin/index";
                window.location.reload();
              });
          })
          .catch(() => {
            isRelogin.show = false;
          });
      }
      return Promise.reject(new Error("登录已过期，请重新登录"));
    } else if (code === 500) {
      // 500 服务器错误
      ElNotification.error({
        title: "服务器错误",
        message: msg,
        duration: 5000,
      });
      return Promise.reject(new Error(msg));
    } else if (code === 403) {
      // 403 无权限访问
      ElNotification.warning({
        title: "权限不足",
        message: "您没有权限访问该资源，请联系管理员",
        duration: 5000,
      });
      return Promise.reject(new Error("无权限访问"));
    } else if (code === 404) {
      // 404 资源不存在
      ElNotification.warning({
        title: "资源不存在",
        message: msg || "请求的资源未找到",
        duration: 5000,
      });
      return Promise.reject(new Error("资源不存在"));
    } else if (code === 502 || code === 503 || code === 504) {
      // 502 503 504 网关错误
      ElNotification.error({
        title: "服务暂时不可用",
        message: "服务器正在维护，请稍后再试",
        duration: 5000,
      });
      return Promise.reject(new Error("服务暂时不可用"));
    } else if (code === 601) {
      // 601 业务警告（如重复提交、业务校验失败等）
      ElMessage({ message: msg, type: "warning" });
      return Promise.reject(new Error(msg));
    } else if (code !== 200) {
      ElNotification.error({ title: msg });
      return Promise.reject(new Error(msg));
    } else {
      return Promise.resolve(res.data);
    }
  },
  (error) => {
    // 详细记录错误信息，便于调试
    console.error("请求错误:", error);

    let { message } = error;
    let errorType = "error";
    let errorTitle = "请求失败";

    // 网络错误处理
    if (!error.response) {
      // 无响应，表示网络连接问题
      if (error.code === "ECONNREFUSED") {
        message = "服务器拒绝连接，请检查服务器是否运行";
      } else if (error.code === "ERR_NETWORK") {
        message = "网络连接异常，请检查您的网络设置";
      } else if (message === "Network Error") {
        message = "后端接口连接异常，请检查服务器是否启动";
      } else if (error.code === "ETIMEDOUT" || message.includes("timeout")) {
        message = "系统接口请求超时，请稍后重试";
        errorType = "warning";
        errorTitle = "请求超时";
      } else {
        message = "网络连接失败，请检查网络状态";
      }
      ElNotification[errorType]({
        title: errorTitle,
        message: message,
        duration: 5000,
      });
      return Promise.reject(error);
    }

    // HTTP 状态码错误处理
    const status = error.response?.status;
    switch (status) {
      case 400:
        message = "请求参数错误，请检查输入内容";
        errorTitle = "请求错误";
        break;
      case 401:
        // 401 在响应拦截器前端已处理，这里仅作兜底
        message = "登录已过期，请重新登录";
        if (!isRelogin.show) {
          isRelogin.show = true;
          ElMessageBox.confirm(message, "会话超时", {
            confirmButtonText: "重新登录",
            cancelButtonText: "取消",
            type: "warning",
          })
            .then(() => {
              isRelogin.show = false;
              window.location.href = "/admin/index";
              window.location.reload();
            })
            .catch(() => {
              isRelogin.show = false;
            });
        }
        break;
      case 403:
        message = "无权限访问该资源";
        errorTitle = "权限不足";
        break;
      case 404:
        message = "请求的资源不存在";
        errorTitle = "未找到";
        break;
      case 405:
        message = "请求方法不被允许";
        errorTitle = "方法不支持";
        break;
      case 408:
        message = "请求超时，请稍后重试";
        errorTitle = "请求超时";
        errorType = "warning";
        break;
      case 500:
        message = "服务器内部错误，请联系管理员";
        errorTitle = "服务器错误";
        break;
      case 501:
        message = "功能未实现";
        errorTitle = "未实现";
        break;
      case 502:
        message = "网关错误，服务暂时不可用";
        errorTitle = "网关错误";
        break;
      case 503:
        message = "服务暂时不可用，请稍后重试";
        errorTitle = "服务不可用";
        break;
      case 504:
        message = "网关超时，请稍后重试";
        errorTitle = "网关超时";
        break;
      default:
        if (message.includes("timeout")) {
          message = "系统接口请求超时";
          errorTitle = "请求超时";
          errorType = "warning";
        } else if (message.includes("Request failed with status code")) {
          message = `系统接口${status}异常`;
        }
    }

    ElMessage({ message: message, type: errorType, duration: 5000 });
    return Promise.reject(error);
  }
);

// 通用下载方法
export function download(url, params, filename, config) {
  downloadLoadingInstance = ElLoading.service({
    text: "正在下载数据，请稍候",
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
        ElMessage.error(errMsg);
      }
      downloadLoadingInstance.close();
    })
    .catch((r) => {
      console.error(r);
      ElMessage.error("下载文件出现错误，请联系管理员！");
      downloadLoadingInstance.close();
    });
}

export default service;
