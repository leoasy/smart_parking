package com.ruoyi.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ruoyi.biz.domain.AiEvent;
import com.ruoyi.biz.domain.Alarm;
import com.ruoyi.biz.service.IAlarmService;
import com.ruoyi.common.constant.AlarmConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAlarmOrchestrator {

    private final IAlarmService alarmService;

    @Transactional(rollbackFor = Exception.class)
    public void handleStatusTransition(AiEvent event) {
        if (event == null) {
            log.warn("[AiAlarmOrchestrator] 收到空事件，跳过处理");
            return;
        }

        String oldStatus = event.getOldStatus();
        String newStatus = event.getNewStatus();
        Long eventId = event.getEventId();
        Long slotId = event.getSlotId();
        Long cameraId = event.getCameraId();

        log.info("[AiAlarmOrchestrator] 状态变更: slotId={}, {} -> {}", slotId, oldStatus, newStatus);

        // 场景1: FREE -> OCCUPIED，创建告警
        if ("FREE".equals(oldStatus) && "OCCUPIED".equals(newStatus)) {
            QueryWrapper<Alarm> query = new QueryWrapper<>();
            query.eq("event_id", eventId)
                .eq("alarm_status", AlarmConstants.Status.UNHANDLED)
                .last("limit 1");

            Alarm exist = alarmService.getOne(query);
            if (exist != null) {
                log.debug("[AiAlarmOrchestrator] 告警已存在，跳过: eventId={}", eventId);
                return;
            }

            Alarm alarm = new Alarm();
            alarm.setEventId(event.getEventId());
            alarm.setCameraId(event.getCameraId());
            alarm.setSlotId(event.getSlotId());
            alarm.setAlarmType(AlarmConstants.Type.PARKING_OCCUPIED);
            alarm.setAlarmLevel(AlarmConstants.Level.MEDIUM);
            alarm.setAlarmStatus(AlarmConstants.Status.UNHANDLED);
            alarm.setImageUrl(event.getImageUrl());
            alarm.setTriggerTime(new Date());

            try {
                alarmService.insertAlarm(alarm);
                log.info("[AiAlarmOrchestrator] 创建告警: eventId={}, slotId={}, cameraId={}", eventId, slotId, cameraId);
            } catch (DuplicateKeyException ex) {
                // 并发情况下，另一线程已插入，直接忽略
                log.debug("[AiAlarmOrchestrator] 告警已存在(并发去重), eventId={}", eventId);
            } catch (Exception ex) {
                log.error("[AiAlarmOrchestrator] 创建告警异常: eventId={}", eventId, ex);
                throw ex; // 其他异常继续抛出，事务回滚
            }
        }

        // 场景2: OCCUPIED -> FREE，关闭告警
        if ("OCCUPIED".equals(oldStatus) && "FREE".equals(newStatus)) {
            QueryWrapper<Alarm> query = new QueryWrapper<>();
            query.eq("event_id", eventId)
                .eq("alarm_status", AlarmConstants.Status.UNHANDLED);
            Alarm alarm = alarmService.getOne(query);
            if (alarm != null) {
                alarm.setAlarmStatus(AlarmConstants.Status.HANDLED);
                alarmService.updateAlarm(alarm);
                log.info("[AiAlarmOrchestrator] 自动关闭告警: eventId={}", eventId);
            } else {
                log.debug("[AiAlarmOrchestrator] 无待关闭告警: eventId={}", eventId);
            }
        }
    }
}
