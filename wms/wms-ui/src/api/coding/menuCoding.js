import request from '@/utils/request'

// 查询菜单编码列表
export function listMenuCoding(query) {
  return request({
    url: '/coding/menuCoding/list',
    method: 'get',
    params: query
  })
}

// 查询菜单编码详细
export function getMenuCoding(codingId) {
  return request({
    url: '/coding/menuCoding/' + codingId,
    method: 'get'
  })
}

// 新增菜单编码
export function addMenuCoding(data) {
  return request({
    url: '/coding/menuCoding',
    method: 'post',
    data: data
  })
}

// 修改菜单编码
export function updateMenuCoding(data) {
  return request({
    url: '/coding/menuCoding',
    method: 'put',
    data: data
  })
}

// 删除菜单编码
export function delMenuCoding(codingId) {
  return request({
    url: '/coding/menuCoding/' + codingId,
    method: 'delete'
  })
}


//查询模块下拉

// 查询菜单编码列表
export function roleMenus() {
  return request({
    url: '/system/role/menus',
    method: 'get',
  })
}


export function template() {
  return request({
    url: '/coding/menuCoding/template',
    method: 'get',
  })
}


// 查询菜单编码列表
export function menuCodingtemplate() {
  return request({
    url: '/coding/menuCoding/template',
    method: 'get',
  })
}

// 根据sql 获取信息
export function MenuCodingnativeSql(query) {
  return request({
    url: '/coding/menuCoding/nativeSql',
    method: 'get',
    params: query
  })
}
