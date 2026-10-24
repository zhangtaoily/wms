import request from '@/utils/request'

// 查询对象存储配置列表
export function listOssConfig(query) {
  return request({
    url: '/system/oss/config/list',
    method: 'get',
    params: query
  })
}

// 查询对象存储配置详细
export function getOssConfig(ossConfigId) {
  return request({
    url: '/system/oss/config/' + ossConfigId,
    method: 'get'
  })
}

// 新增对象存储配置
export function addOssConfig(data) {
  return request({
    url: '/system/oss/config',
    method: 'post',
    data: data
  })
}

// 修改对象存储配置
export function updateOssConfig(data) {
  return request({
    url: '/system/oss/config',
    method: 'put',
    data: data
  })
}

// 删除对象存储配置
export function delOssConfig(ossConfigId) {
  return request({
    url: '/system/oss/config/' + ossConfigId,
    method: 'delete'
  })
}

// 对象存储状态修改
export function changeOssConfigStatus(ossConfigId, status, configKey) {
  const data = {
    ossConfigId,
    status,
    configKey
  }
  return request({
    url: '/system/oss/config/changeStatus',
    method: 'put',
    data: data
  })
}

// 上传文件
export function getUploadFilesList(functionName, id, data) {
  return request({
    url: `/system/oss/upload/files/${functionName}/${id}`,
    method: 'get',
    data
  })
}

// 上传文件
export function uploadFiles(functionName,id,data) {
  return request({
    url: `/system/oss/upload/files/${functionName}/${id}`,
    method: 'post',
    data
  })
}

// 删除文件
export function deleteFiles(data) {
  return request({
    url: '/system/oss/delete/files',
    method: 'delete',
    data
  })
}

