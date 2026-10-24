import request from '@/utils/request'

// 查询系统设置列表
export function listSetting(query) {
  return request({
    url: '/system/setting/list',
    method: 'get',
    params: query
  })
}

// 查询系统设置详细
export function getSetting(id) {
  return request({
    url: '/system/setting/' + id,
    method: 'get'
  })
}

// 新增系统设置
export function addSetting(data) {
  return request({
    url: '/system/setting',
    method: 'post',
    data: data
  })
}

// 修改系统设置
export function updateSetting(data) {
  return request({
    url: '/system/setting',
    method: 'put',
    data: data
  })
}

// 删除系统设置
export function delSetting(id) {
  return request({
    url: '/system/setting/' + id,
    method: 'delete'
  })
}
