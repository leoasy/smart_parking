import request from '@/utils/request'

// 查询车位ROI标注列表
export function listParkingRoi(query) {
  return request({
    url: '/biz/ParkingRoi/list',
    method: 'get',
    params: query
  })
}

// 查询车位ROI标注详细
export function getParkingRoi(parkingRoiId) {
  return request({
    url: '/biz/ParkingRoi/' + parkingRoiId,
    method: 'get'
  })
}

// 新增车位ROI标注
export function addParkingRoi(data) {
  return request({
    url: '/biz/ParkingRoi',
    method: 'post',
    data: data
  })
}

// 修改车位ROI标注
export function updateParkingRoi(data) {
  return request({
    url: '/biz/ParkingRoi',
    method: 'put',
    data: data
  })
}

// 删除车位ROI标注
export function delParkingRoi(parkingRoiId) {
  return request({
    url: '/biz/ParkingRoi/' + parkingRoiId,
    method: 'delete'
  })
}
