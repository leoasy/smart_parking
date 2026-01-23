package com.ruoyi.biz.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.biz.domain .AiEvent;

/**
 * AI推理事件Service接口
 *
 * @author aoshiyue
 * @date 2026-01-08
 */
public interface IAiEventService extends IService<AiEvent> {

    public IPage<AiEvent> pageAiEvent(Page<AiEvent> page, QueryWrapper<AiEvent> queryWrapper);

    /**
     * 查询AI推理事件
     *
     * @param eventId AI推理事件主键
     * @return AI推理事件
     */
    public AiEvent selectAiEventByEventId(Long eventId);

    /**
     * 新增AI推理事件
     *
     * @param aiEvent AI推理事件
     * @return 结果
     */
    public int insertAiEvent(AiEvent aiEvent);

    /**
     * 修改AI推理事件
     *
     * @param aiEvent AI推理事件
     * @return 结果
     */
    public int updateAiEvent(AiEvent aiEvent);

    /**
     * 批量删除AI推理事件
     *
     * @param eventIds 需要删除的AI推理事件主键集合
     * @return 结果
     */
    public int deleteAiEventByEventIds(Long[] eventIds);

    /**
     * 删除AI推理事件信息
     *
     * @param eventId AI推理事件主键
     * @return 结果
     */
    public int deleteAiEventByEventId(Long eventId);

    /**
     * 导入AI推理事件数据
     *
     * @param list            数据列表
     * @param isUpdateSupport 是否更新支持，如果已存在，则进行更新数据
     * @param operName        操作用户
     * @return 结果
     */
    public String importAiEvent(
    List<AiEvent> list, int titleNum, Boolean
    isUpdateSupport,
    String operName);

    Map<String, Object> dashboard();
}
