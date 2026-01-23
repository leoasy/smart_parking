package com.ruoyi.biz.service.impl;

import com.ruoyi.biz.domain.Alarm;
import com.ruoyi.biz.domain.DevCamera;
import com.ruoyi.biz.domain.ParkingSlot;
import com.ruoyi.biz.mapper.AlarmMapper;
import com.ruoyi.biz.mapper.DevCameraMapper;
import com.ruoyi.biz.mapper.ParkingSlotMapper;
import com.ruoyi.biz.service.IAlarmService;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.*;

import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.bean.BeanValidators;
import com.ruoyi.common.exception.ServiceException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.mapper.AiEventMapper;
import com.ruoyi.biz.domain.AiEvent;
import com.ruoyi.biz.service.IAiEventService;
import com.ruoyi.common.constant.AlarmConstants;

import jakarta.validation.Validator;

/**
 * AI推理事件Service业务层处理
 *
 * @author aoshiyue
 * @date 2026-01-08
 */
@Service
@RequiredArgsConstructor
public class AiEventServiceImpl extends ServiceImpl<AiEventMapper,AiEvent> implements IAiEventService {
    @Autowired
    private final IAlarmService alarmService;
    private final AiEventMapper aiEventMapper;
    private final ParkingSlotMapper parkingSlotMapper;
    private final  AlarmMapper alarmMapper;
    private final DevCameraMapper devCameraMapper;
    protected final Validator validator;

    @Override
    public String importAiEvent(
    List<AiEvent> list, int titleNum, Boolean
    isUpdateSupport,
    String operName)

    {
        if (StringUtils.isNull(list) || list.size() == 0) {
            throw new ServiceException("导入数据不能为空！" );
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            AiEvent aiEvent =list.get(i);
            try {
                QueryWrapper<AiEvent> queryWrapper = new QueryWrapper<>();
                List<AiEvent> checkList = new ArrayList<>(); //aiEventMapper.selectList(queryWrapper);
                if (checkList.size() == 0) {
                    BeanValidators.validateWithException(validator, aiEvent);
                    insertAiEvent(aiEvent);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、记录" + (i + titleNum + 2) + "：" + aiEvent.getEventId() + " 导入成功")
                    ;
                } else if (isUpdateSupport) {
                    BeanValidators.validateWithException(validator, aiEvent);
                    aiEvent.setEventId(checkList.get(0).getEventId());
                    updateAiEvent(aiEvent);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、记录" + (i + titleNum + 2) + " 更新成功")
                    ;
                } else {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "、记录" + (i + titleNum + 2) + " 已存在")
                    ;
                }
            } catch (Exception e) {
                failureNum++;
                String msg = "<br/>" + failureNum + "、记录" + (i + titleNum + 2) + " 导入失败：";
                failureMsg.append(msg + e.getMessage());
            }
        }
        if (failureNum > 0) {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：" );
            throw new ServiceException(failureMsg.toString());
        } else {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：" );
        }
        return successMsg.toString();
    }

    @Override
    public IPage<AiEvent> pageAiEvent(Page<AiEvent> page, QueryWrapper<AiEvent> queryWrapper) {
        return aiEventMapper.selectPage(page, queryWrapper);
    }

    /**
     * 查询AI推理事件
     *
     * @param eventId AI推理事件主键
     * @return AI推理事件
     */
    @Override
    public AiEvent selectAiEventByEventId(Long eventId) {
        return aiEventMapper.selectById(eventId);
    }

    /**
     * 新增AI推理事件
     *
     * @param aiEvent AI推理事件
     * @return 结果
     */
//    @Override
//    public int insertAiEvent(AiEvent aiEvent) {
//        aiEvent.setUserId(SecurityUtils.getUserId());
//        aiEvent.setDeptId(SecurityUtils.getDeptId());
//        aiEvent.setCreateBy(SecurityUtils.getUsername());
//        aiEvent.setCreateTime(DateUtils.getNowDate());
//            return aiEventMapper.insert(aiEvent);
//    }
    @Override
    public int insertAiEvent(AiEvent aiEvent) {
        aiEvent.setCreateTime(DateUtils.getNowDate());

        // 获取当前用户的ID和部门ID
        Long userId;
        Long deptId;
        String username;

        try {
            userId = SecurityUtils.getUserId();
            deptId = SecurityUtils.getDeptId();
            username = SecurityUtils.getUsername();
        } catch (Exception e) {
            // 本地测试 / 无登录态兜底
            userId = 1L;  // 默认的用户ID（根据实际情况）
            deptId = 1L;  // 默认的部门ID（根据实际情况）
            username = "system";  // 默认为系统用户
        }

        aiEvent.setUserId(userId);
        aiEvent.setDeptId(deptId);
        aiEvent.setCreateBy(username);

        // 1️⃣ 插入事件
        int rows = aiEventMapper.insert(aiEvent);



        // 2️⃣ 首次事件：FREE → OCCUPIED 才可能告警
        handleAlarm(aiEvent);

        return rows;

        // 插入AI推理事件
//        return aiEventMapper.insert(aiEvent);
    }


    /**
     * 修改AI推理事件
     *
     * @param aiEvent AI推理事件
     * @return 结果
     */
    @Override
    public int updateAiEvent(AiEvent aiEvent) {
        // 1️⃣ 查旧事件
        AiEvent oldEvent = aiEventMapper.selectById(aiEvent.getEventId());

        // 2️⃣ 更新
        aiEvent.setUpdateTime(DateUtils.getNowDate());
        aiEvent.setUpdateBy(SecurityUtils.getUsername());
        int rows = aiEventMapper.updateById(aiEvent);

        // 3️⃣ 状态变化 → 告警
        handleAlarm(aiEvent);

        return rows;
//        return aiEventMapper.updateById(aiEvent);
    }

    /**
     * 批量删除AI推理事件
     *
     * @param eventIds 需要删除的AI推理事件主键
     * @return 结果
     */
    @Override
    public int deleteAiEventByEventIds(Long[] eventIds) {
        return aiEventMapper.deleteByIds(Arrays.asList(eventIds));
    }

    /**
     * 删除AI推理事件信息
     *
     * @param eventId AI推理事件主键
     * @return 结果
     */
    @Override
    public int deleteAiEventByEventId(Long eventId) {
        return aiEventMapper.deleteById(eventId);
    }

    /**
     * 根据 AI 事件状态变化处理告警
     */
    private void handleAlarm(AiEvent event) {

        String oldStatus = event.getOldStatus();
        String newStatus = event.getNewStatus();
        Long eventId = event.getEventId();

        // FREE → OCCUPIED：生成告警
        if ("FREE".equals(oldStatus) && "OCCUPIED".equals(newStatus)) {

            QueryWrapper<Alarm> qw = new QueryWrapper<>();
            qw.eq("event_id", eventId)
                    .eq("alarm_status", AlarmConstants.Status.UNHANDLED)
                    .last("limit 1");

            Alarm exist = alarmService.getOne(qw);
            if (exist == null) {
                Alarm alarm = new Alarm();
                alarm.setEventId(event.getEventId());
                alarm.setCameraId(event.getCameraId());
                alarm.setSlotId(event.getSlotId());
                alarm.setAlarmType(AlarmConstants.Type.PARKING_OCCUPIED);
                alarm.setAlarmLevel(AlarmConstants.Level.MEDIUM);
                alarm.setAlarmStatus(AlarmConstants.Status.UNHANDLED);
                alarm.setTriggerTime(new Date());
                alarmService.insertAlarm(alarm);
            }
        }

        // OCCUPIED → FREE：消警
        if ("OCCUPIED".equals(oldStatus) && "FREE".equals(newStatus)) {

            QueryWrapper<Alarm> qw = new QueryWrapper<>();
            qw.eq("event_id", eventId)
                    .eq("alarm_status", AlarmConstants.Status.UNHANDLED);

            Alarm alarm = alarmService.getOne(qw);
            if (alarm != null) {
                alarm.setAlarmStatus(AlarmConstants.Status.HANDLED);
                alarmService.updateAlarm(alarm);
            }
        }
    }

    public Map<String, Object> dashboard() {
        Map<String, Object> map = new HashMap<>();

        long totalSlots = parkingSlotMapper.selectCount(null);
        long occupiedSlots = parkingSlotMapper.selectCount(
                new QueryWrapper<ParkingSlot>().eq("slot_status", "OCCUPIED")
        );
        long freeSlots = totalSlots - occupiedSlots;

        long todayAlarms = alarmMapper.selectCount(
                new QueryWrapper<Alarm>().ge("trigger_time", LocalDate.now())
        );

        long cameraOnline = devCameraMapper.selectCount(
                new QueryWrapper<DevCamera>().eq("camera_status", "ONLINE")
        );
        long cameraOffline = devCameraMapper.selectCount(
                new QueryWrapper<DevCamera>().eq("camera_status", "OFFLINE")
        );

        List<AiEvent> events = aiEventMapper.selectList(
                new QueryWrapper<AiEvent>().orderByDesc("event_time").last("limit 10")
        );
        map.put("totalSlots", totalSlots);
        map.put("occupiedSlots", occupiedSlots);
        map.put("freeSlots", freeSlots);
        map.put("todayAlarms", todayAlarms);

        map.put("cameraOnline", cameraOnline);
        map.put("cameraOffline", cameraOffline);

        map.put("days", List.of("Mon","Tue","Wed","Thu","Fri","Sat","Sun"));
        map.put("alarmCounts", List.of(2,5,3,6,4,7,1));

        map.put("events", events);

        return map;
    }


}
