import request from '@/utils/request'

// 查询模块权限树（组别→模块）
export function moduleTreeselect() {
  return request({
    url: '/system/module/treeselect',
    method: 'get'
  })
}

// 按角色ID查询模块权限树及已勾选模块ID
export function roleModuleTreeselect(roleId) {
  return request({
    url: '/system/module/roleModuleTreeselect/' + roleId,
    method: 'get'
  })
}
