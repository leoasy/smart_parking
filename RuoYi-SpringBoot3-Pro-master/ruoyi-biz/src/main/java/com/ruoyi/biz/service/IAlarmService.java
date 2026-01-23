package com.ruoyi.biz.service;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.biz.domain .Alarm;
import com.ruoyi.biz.vo.AlarmVo;

/**
 * 告警记录Service接口
 *
 * @author aoshiyue
 * @date 2026-01-08
 */
public interface IAlarmService extends IService<Alarm> {
    public IPage<Alarm> pageAlarm(Page<Alarm> page, QueryWrapper<Alarm> queryWrapper);

    /**
     * 分页查询告警记录，关联事件、摄像头、车位信息
     * @param page 分页对象
     * @param params 查询参数 (例如 AlarmVo，包含可能的 beginTriggerTime, endTriggerTime 等)
     * @return 分页结果，包含关联信息
     */
    IPage<AlarmVo> selectPageAlarmListWithDetails(Page<AlarmVo> page, AlarmVo params); // 添加新方法声明


    /**
     * 查询告警记录
     *
     * @param alarmId 告警记录主键
     * @return 告警记录
     */
    public Alarm selectAlarmByAlarmId(Long alarmId);

    /**
     * 新增告警记录
     *
     * @param alarm 告警记录
     * @return 结果
     */
    public int insertAlarm(Alarm alarm);

    /**
     * 修改告警记录
     *
     * @param alarm 告警记录
     * @return 结果
     */
    public int updateAlarm(Alarm alarm);

    /**
     * 批量删除告警记录
     *
     * @param alarmIds 需要删除的告警记录主键集合
     * @return 结果
     */
    public int deleteAlarmByAlarmIds(Long[] alarmIds);

    /**
     * 删除告警记录信息
     *
     * @param alarmId 告警记录主键
     * @return 结果
     */
    public int deleteAlarmByAlarmId(Long alarmId);

    /**
     * 导入告警记录数据
     *
     * @param list            数据列表
     * @param isUpdateSupport 是否更新支持，如果已存在，则进行更新数据
     * @param operName        操作用户
     * @return 结果
     */
    public String importAlarm(
    List<Alarm> list, int titleNum, Boolean
    isUpdateSupport,
    String operName);

    /**
     * 根据事件ID查询未处理告警
     */
    Alarm selectUnhandledByEventId(Long eventId);

    /**
     * 自动消除告警（OCCUPIED → FREE）
     */
    int autoClearByEventId(Long eventId);

}
