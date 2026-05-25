import request from '@/utils/request'

// 查询AI推理事件列表
export function listAiEvent(query) {
  return request({
    url: '/biz/AiEvent/list',
    method: 'get',
    params: query
  })
}

// 查询AI推理事件详细
export function getAiEvent(eventId) {
  return request({
    url: '/biz/AiEvent/' + eventId,
    method: 'get'
  })
}

// 新增AI推理事件
export function addAiEvent(data) {
  return request({
    url: '/biz/AiEvent',
    method: 'post',
    data: data
  })
}

// 修改AI推理事件
export function updateAiEvent(data) {
  return request({
    url: '/biz/AiEvent',
    method: 'put',
    data: data
  })
}

// 删除AI推理事件
export function delAiEvent(eventId) {
  return request({
    url: '/biz/AiEvent/' + eventId,
    method: 'delete'
  })
}
