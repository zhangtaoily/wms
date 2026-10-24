import request from '@/utils/request'

// 查询字段配置表列表
export function getFieldConfigList(name) {
  return request({
    url: `/system/field/config/list/${name}`,
    method: "get"
  });
}

// 查询字段配置表列表
export function listFieldConfig(query) {
  return request({
    url: '/system/field/config/list',
    method: 'get',
    params: query
  })
}

// 查询字段配置表详细
export function getFieldConfig(id) {
  return request({
    url: '/system/field/config/' + id,
    method: 'get'
  })
}

// 新增字段配置表
export function addFieldConfig(data) {
  return request({
    url: '/system/field/config',
    method: 'post',
    data: data
  })
}

// 修改字段配置表
export function updateFieldConfig(data) {
  return request({
    url: '/system/field/config',
    method: 'put',
    data: data
  })
}

// 删除字段配置表
export function delFieldConfig(id) {
  return request({
    url: '/system/field/config/' + id,
    method: 'delete'
  })
}
