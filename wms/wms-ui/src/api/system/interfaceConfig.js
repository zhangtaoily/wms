import request from "@/utils/request";

// 查询字段配置表列表
export function getPreviewData(configId) {
  return request({
    url: `/system/interfaceConfig/preview/${configId}`,
    method: "get",
  });
}
// 批量推送
export function batchSend(functionName, ids) {
  return request({
    url: `/common/table/muli/${functionName}`,
    method: "post",
    data: ids ,
  });
}
// 批量推送(条件)
export function batchSendCondition(functionName, ids,worktiming) {
  return request({
    url: `/common/table/muliCondition/${functionName}`,
    method: "post",
    data:{ids,worktiming },
  });
}
// 全量推送
export function allSend(functionName) {
  return request({
    url: `/common/table/all/${functionName}`,
    method: "patch",
  });
}
// 重新发送
export function reSend(functionName,id) {
  return request({
    url: `/common/table/reSend/${functionName}/${id}`,
    method: "patch",
  });
}
