import request from '@/utils/request'

// 查询告警记录列表
export function listAlarm(query) {
  return request({
    url: '/biz/Alarm/list',
    method: 'get',
    params: query
  })
}

// 查询告警记录详细
export function getAlarm(alarmId) {
  return request({
    url: '/biz/Alarm/' + alarmId,
    method: 'get'
  })
}

// 新增告警记录
export function addAlarm(data) {
  return request({
    url: '/biz/Alarm',
    method: 'post',
    data: data
  })
}

// 修改告警记录
export function updateAlarm(data) {
  return request({
    url: '/biz/Alarm',
    method: 'put',
    data: data
  })
}

// 删除告警记录
export function delAlarm(alarmId) {
  return request({
    url: '/biz/Alarm/' + alarmId,
    method: 'delete'
  })
}

// 批量删除告警记录
export function batchDeleteAlarm(alarmIds) {
  return request({
    url: '/biz/Alarm/batch',
    method: 'delete',
    data: alarmIds
  })
}

// 批量确认告警记录
export function batchConfirmAlarm(alarmIds) {
  return request({
    url: '/biz/Alarm/confirm',
    method: 'put',
    data: alarmIds
  })
}

// 确认告警记录（单个）
export function confirmAlarm(alarmId) {
  return request({
    url: '/biz/Alarm/confirm/' + alarmId,
    method: 'put'
  })
}
