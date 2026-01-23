import request from '@/utils/request'

// 查询摄像头设备列表
export function listDevCamera(query) {
  return request({
    url: '/biz/DevCamera/list',
    method: 'get',
    params: query
  })
}

// 查询摄像头设备详细
export function getDevCamera(cameraId) {
  return request({
    url: '/biz/DevCamera/' + cameraId,
    method: 'get'
  })
}

// 新增摄像头设备
export function addDevCamera(data) {
  return request({
    url: '/biz/DevCamera',
    method: 'post',
    data: data
  })
}

// 修改摄像头设备
export function updateDevCamera(data) {
  return request({
    url: '/biz/DevCamera',
    method: 'put',
    data: data
  })
}

// 删除摄像头设备
export function delDevCamera(cameraId) {
  return request({
    url: '/biz/DevCamera/' + cameraId,
    method: 'delete'
  })
}
