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

import java.math.BigDecimal;
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

        // 场景1: FREE -> OCCUPIED，创建告警（去掉预检查，直接插入依赖数据库唯一键去重）
        if ("FREE".equals(oldStatus) && "OCCUPIED".equals(newStatus)) {
            Alarm alarm = new Alarm();
            alarm.setEventId(event.getEventId());
            alarm.setCameraId(event.getCameraId());
            alarm.setSlotId(event.getSlotId());
            alarm.setAlarmType(AlarmConstants.Type.PARKING_OCCUPIED);
            alarm.setAlarmLevel(calculateAlarmLevel(event.getConfidence()));
            alarm.setAlarmStatus(AlarmConstants.Status.UNHANDLED);
            alarm.setImageUrl(event.getImageUrl());
            alarm.setTriggerTime(new Date());

            try {
                alarmService.insertAlarm(alarm);
                log.info("[AiAlarmOrchestrator] 创建告警: eventId={}, slotId={}, cameraId={}, level={}",
                        eventId, slotId, cameraId, alarm.getAlarmLevel());
            } catch (DuplicateKeyException ex) {
                // 并发情况下，另一线程已插入，直接忽略
                log.debug("[AiAlarmOrchestrator] 告警已存在(并发去重), eventId={}", eventId);
            } catch (Exception ex) {
                log.error("[AiAlarmOrchestrator] 创建告警异常: eventId={}", eventId, ex);
                throw ex;
            }
        }

        // 场景2: OCCUPIED -> FREE，关闭该车位所有未处理告警
        if ("OCCUPIED".equals(oldStatus) && "FREE".equals(newStatus)) {
            QueryWrapper<Alarm> query = new QueryWrapper<>();
            query.eq("slot_id", slotId)
                .eq("alarm_status", AlarmConstants.Status.UNHANDLED);
            var alarms = alarmService.list(query);
            if (alarms != null && !alarms.isEmpty()) {
                for (Alarm a : alarms) {
                    a.setAlarmStatus(AlarmConstants.Status.RECOVERED);
                    alarmService.updateAlarm(a);
                }
                log.info("[AiAlarmOrchestrator] 自动关闭告警: slotId={}, 数量={}", slotId, alarms.size());
            } else {
                log.debug("[AiAlarmOrchestrator] 无待关闭告警: slotId={}", slotId);
            }
        }
    }

    /**
     * 根据置信度动态计算告警级别
     * 置信度 >= 0.95 -> URGENT
     * 置信度 >= 0.85 -> HIGH
     * 置信度 >= 0.70 -> MEDIUM
     * 其他        -> LOW
     */
    private String calculateAlarmLevel(BigDecimal confidence) {
        if (confidence == null) {
            return AlarmConstants.Level.MEDIUM;
        }
        double conf = confidence.doubleValue();
        if (conf >= 0.95) {
            return AlarmConstants.Level.URGENT;
        } else if (conf >= 0.85) {
            return AlarmConstants.Level.HIGH;
        } else if (conf >= 0.70) {
            return AlarmConstants.Level.MEDIUM;
        } else {
            return AlarmConstants.Level.LOW;
        }
    }
}
