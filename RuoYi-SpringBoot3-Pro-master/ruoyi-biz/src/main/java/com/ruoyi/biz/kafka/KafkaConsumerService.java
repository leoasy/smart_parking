package com.ruoyi.biz.kafka;

import com.alibaba.fastjson.JSON;
import com.ruoyi.biz.domain.AiEvent;
import com.ruoyi.biz.service.impl.AiAlarmOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final AiAlarmOrchestrator aiAlarmOrchestrator;

    @KafkaListener(topics = "ai-event-topic", groupId = "parking-group", autoStartup = "${spring.kafka.consumer.auto-startup:true}")
    public void consumeAiEvent(String message) {
        AiEvent event = null;
        try {
            event = JSON.parseObject(message, AiEvent.class);
        } catch (Exception ex) {
            log.error("[KafkaConsumer] JSON解析失败, raw={}", message, ex);
            return;
        }
        if (event == null) {
            log.warn("[KafkaConsumer] 空事件, raw={}", message);
            return;
        }
        try {
            log.info("[KafkaConsumer] 收到事件: eventId={}, slotId={}, {}->{}",
                event.getEventId(), event.getSlotId(), event.getOldStatus(), event.getNewStatus());
            aiAlarmOrchestrator.handleStatusTransition(event);
        } catch (Exception ex) {
            log.error("[KafkaConsumer] 处理事件失败: eventId={}", event.getEventId(), ex);
        }
    }
}
