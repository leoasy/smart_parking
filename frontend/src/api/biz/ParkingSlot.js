import request from '@/utils/request'

// 查询车位信息列表
export function listParkingSlot(query) {
  return request({
    url: '/biz/ParkingSlot/list',
    method: 'get',
    params: query
  })
}

// 查询车位信息详细
export function getParkingSlot(parkingSlotId) {
  return request({
    url: '/biz/ParkingSlot/' + parkingSlotId,
    method: 'get'
  })
}

// 新增车位信息
export function addParkingSlot(data) {
  return request({
    url: '/biz/ParkingSlot',
    method: 'post',
    data: data
  })
}

// 修改车位信息
export function updateParkingSlot(data) {
  return request({
    url: '/biz/ParkingSlot',
    method: 'put',
    data: data
  })
}

// 删除车位信息
export function delParkingSlot(parkingSlotId) {
  return request({
    url: '/biz/ParkingSlot/' + parkingSlotId,
    method: 'delete'
  })
}
