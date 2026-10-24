import request from "@/utils/request";
import { getToken } from "@/utils/auth";

// 获取列表数据和显示字段数据
export function getTableList(data) {
  return request({
    url: "/common/table/list",
    method: "post",
    data: data,
  });
}

// 查询字段配置表列表，获取搜索配置信息
export function getFieldConfigList(name) {
  return request({
    url: `/system/field/config/list/${name}`,
    method: "get",
  });
}

// 通用删除接口
export function deleteById(functionName, id) {
  return request({
    url: `/common/table/${functionName}/${id}`,
    method: "delete",
  });
}

// 保存数据
export function save(functionName, data) {
  return request({
    url: `/common/table/${functionName}`,
    method: "post",
    data: data,
  });
}
// 获取详情
export function getDetails(functionName, id) {
  return request({
    url: `/common/table/${functionName}/${id}`,
    method: "get",
  });
}
// 获取详情
export function getDetailsList(functionName, data) {
  return request({
    url: `/common/table/${functionName}/batch/init`,
    method: "post",
    data,
  });
}

// 获取打印模版按钮list
export function getPrintButton(functionName) {
  return request({
    url: `/system/print/config/${functionName}`,
    method: "get",
  });
}
// 暂存数据
export function stagingData(functionName, data) {
  return request({
    url: `/common/table/staging/${functionName}`,
    method: "post",
    data: data,
  });
}
// 提交数据
export function submitData(functionName, id, data) {
  return request({
    url: `/common/table/submit/${functionName}/${id}`,
    method: "patch",
    data,
  });
}
// 反审核数据
export function rollBackData(functionName, id) {
  return request({
    url: `/common/table/roll/back/${functionName}/${id}`,
    method: "patch",
  });
}
// 重发同步（同步失败单据重新触发，后端幂等）
export function reSendData(functionName, id) {
  return request({
    url: `/common/table/reSend/${functionName}/${id}`,
    method: "patch",
  });
}
// 盘存单盘盈亏主管确认（M5 Q2：approved=true 放行并调整台账回写ERP，false 驳回）
export function confirmCountAdjust(id, approved, remark) {
  return request({
    url: `/common/table/stockCount/confirm/${id}/${approved}`,
    method: "patch",
    data: { remark: remark || "" },
  });
}
// 新增 初始化绑定
export function getInit(functionName) {
  return request({
    url: `/common/table/init/bind/${functionName}`,
    method: "get",
  });
}
// 复制
export function getCopyData(functionName, id, data) {
  return request({
    url: `/common/table/copy/${functionName}/${id}`,
    method: "post",
    data,
  });
}
// 关联复制
export function getRelevancyCopyData(functionName, id, data) {
  return request({
    url: `/common/table/copyAll/${functionName}/${id}`,
    method: "post",
    data,
  });
}

// 列配置
export function getColumnConfig(functionName) {
  return request({
    url: `/system/field/config/list/function/${functionName}/fields`,
  });
}

// 列配置保存
export function saveColumnConfig(functionName, data) {
  return request({
    url: `/common/table/${functionName}`,
    method: "post",
    data,
  });
}

// 下推
/**
 * originalFn 原下推函数
 * currentFnName 下推目标函数
 */
export function pushDown(originalFn, currentFnName, data) {
  return request({
    url: `/common/table/push/${originalFn}/${currentFnName}`,
    method: "post",
    data,
  });
}

// 关闭表单
export function closeTable(functionName, id) {
  return request({
    url: `/common/table/close/${functionName}/${id}`,
    method: "patch",
  });
}

// 批量关闭表单
export function batchClose(functionName, data) {
  return request({
    url: `/common/table/close/batch/${functionName}`,
    method: "patch",
    data,
  });
}

// 批量反关闭表单
export function batchReClose(functionName, data) {
  return request({
    url: `/common/table/re-close/batch/${functionName}`,
    method: "patch",
    data,
  });
}

// 批量关闭表单明细
export function batchCloseDetails(functionName, data) {
  return request({
    url: `/common/table/close/details/batch/${functionName}`,
    method: "patch",
    data,
  });
}

// 批量反关闭表单明细
export function batchReCloseDetails(functionName, data) {
  return request({
    url: `/common/table/re-close/details/batch/${functionName}`,
    method: "patch",
    data,
  });
}

// 批量取消表单
export function batchCancel(functionName, data) {
  return request({
    url: `/common/table/cancel/batch/${functionName}`,
    method: "patch",
    data,
  });
}

// 批量反取消表单
export function batchReCancel(functionName, data) {
  return request({
    url: `/common/table/re-cancel/batch/${functionName}`,
    method: "patch",
    data,
  });
}

// 批量取消表单明细
export function batchCancelDetails(functionName, data) {
  return request({
    url: `/common/table/cancel/details/batch/${functionName}`,
    method: "patch",
    data,
  });
}

// 批量反取消表单明细
export function batchReCancelDetails(functionName, data) {
  return request({
    url: `/common/table/re-cancel/details/batch/${functionName}`,
    method: "patch",
    data,
  });
}

// 获取变更字段列表
export function getChangeFieldList(functionName,changeType) {
  return request({
    url: `/system/change-field/list/${functionName}/${changeType}`,
    method: "get",
  });
}

// 客户360联系人添加
export function createLinkman(data) {
  return request({
    url: `/system/cust/create/linkman`,
    method: "post",
    data,
  });
}
// 客户360业务员添加
export function createSaler(data) {
  return request({
    url: `system/cust/create/saler`,
    method: "post",
    data,
  });
}
// 客户360意向合同、销售合同销、销售子合同、销售订单、生产通知单、销售出库单金额
export function getPactInfo(params) {
  return request({
    url: `/system/cust/get/pactInfo`,
    method: "get",
    params,
  });
}
// 客户360退货通知单、销售退货单金额
export function geTrePactinfo(params) {
  return request({
    url: `/system/cust/get/rePactInfo`,
    method: "get",
    params,
  });
}

// 客户360获取工厂品牌信息
export function getCustBrand(params) {
  return request({
    url: `/system/cust/get/brand`,
    method: "get",
    params,
  });
}

// 用户自定义列，恢复默认值, 删除用户对应模块下面的配置
export function resetFieldShowConfig(functionName) {
  return request({
    url: `/system/field-show/data/${functionName}`,
    method: "delete",
  });
}

// 用户自定义列，更新配置
export function updatedFieldShowConfig(functionName) {
  return request({
    url: `/system/field-show/data/${functionName}`,
    method: "post",
  });
}

// 获取客户信用额度
export function getCredit(params) {
  return request({
    url: `/system/cust/get/credit`,
    method: "get",
    params
  });
}

// 获取客户应收账款
export function getLast(params) {
  return request({
    url: `/system/cust/get/last`,
    method: "get",
    params
  });
}

// 重新反写
export function reWriteBack(id) {
  return request({
    url: `/business/write/back/${id}`,
    method: "patch",
  });
}

// 变更单获取上游数据
export function getUpstreamData(functionName, data) {
  return request({
    url: `/common/table//upstream/${functionName}`,
    method: "post",
    data,
  });
}

// 获取明细数据列表
export function getSubTableDataList(functionName, data) {
  return request({
    url: `/common/table/details/${functionName}`,
    method: "post",
    data,
  });
}

// 获取明细数据列表和表头数据
export function getDetailsWithHead(functionName, data) {
  return request({
    url: `/common/table/details/with/head/${functionName}`,
    method: "post",
    data,
  });
}

// 检查能否下推
export function checkPushDown(ids, functionName) {
  return request({
    url: `/push/down/check/${functionName}`,
    method: "post",
    data: ids,
  });
}

// 检查功能
export function updateCheckState(functionName, isCheck, data) {
  return request({
    url: `/common/table/check/${functionName}/${isCheck}`,
    method: "post",
    data,
  });
}

// 导出功能
export function exportExcel(functionName, data) {
  return handleExport(functionName, data);
}

// 导入模板下载
export const downloadImportTemplate = async (functionName) => {
  try {
    const response = await fetch(
      `${process.env.VUE_APP_BASE_API}/common/table/importTemplate/${functionName}`,
      {
        method: "GET",
        headers: {
          Authorization: "Bearer " + getToken(),
        },
      }
    );

    if (!response.ok) {
      throw new Error(`请求失败: ${response.status} ${response.statusText}`);
    }

    const blob = await response.blob();

    const contentDisposition = response.headers.get("Content-Disposition");
    const fileName = contentDisposition
      ? contentDisposition.split("filename=")[1].replace(/"/g, "")
      : `${functionName}_import_template.xlsx`;

    const url = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = decodeURIComponent(fileName);
    document.body.appendChild(link);
    link.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(link);
  } catch (error) {
    console.error("模板下载失败:", error);
    alert(`模板下载失败: ${error.message}`);
  }
};

// Excel 导入
export const importExcelData = (functionName, formData) => {
  return fetch(
    `${process.env.VUE_APP_BASE_API}/common/table/importData/${functionName}`,
    {
      method: "POST",
      headers: {
        Authorization: "Bearer " + getToken(),
      },
      body: formData,
    }
  ).then((response) => response.json());
};

const handleExport = async (functionName, data) => {
  try {
    // 1. 发送请求（保持与原接口一致）
    const response = await fetch(
      `${process.env.VUE_APP_BASE_API}/common/table/export/${functionName}`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Bearer " + getToken(),
        },
        body: JSON.stringify(data),
      }
    );

    // 2. 检查响应状态
    if (!response.ok) {
      throw new Error(`请求失败: ${response.status} ${response.statusText}`);
    }

    // 3. 处理二进制数据
    const blob = await response.blob();

    // 4. 提取文件名（如果后端返回文件名）
    const contentDisposition = response.headers.get("Content-Disposition");
    const fileName = contentDisposition
      ? contentDisposition.split("filename=")[1].replace(/"/g, "")
      : `${functionName}_export_${new Date().getTime()}.xlsx`;

    // 5. 创建下载链接
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = decodeURIComponent(fileName);
    document.body.appendChild(link);

    // 6. 触发下载
    link.click();

    // 7. 清理资源
    window.URL.revokeObjectURL(url);
    document.body.removeChild(link);
  } catch (error) {
    console.error("导出失败:", error);
    alert(`导出失败: ${error.message}`);
  }
};
