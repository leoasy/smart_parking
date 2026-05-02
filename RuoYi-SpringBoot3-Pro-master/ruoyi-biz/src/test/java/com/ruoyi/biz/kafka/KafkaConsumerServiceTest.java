package com.ruoyi.biz.kafka;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.biz.domain.AiEvent;
import com.ruoyi.biz.service.impl.AiAlarmOrchestrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class KafkaConsumerServiceTest {

    private AiAlarmOrchestrator aiAlarmOrchestrator;
    private KafkaConsumerService kafkaConsumerService;

    @BeforeEach
    void setup() {
        aiAlarmOrchestrator = Mockito.mock(AiAlarmOrchestrator.class);
        kafkaConsumerService = new KafkaConsumerService(aiAlarmOrchestrator);
    }

    @Test
    void consumeAiEvent_shouldInvokeOrchestrator() {
        AiEvent event = new AiEvent();
        event.setEventId(1L);
        event.setOldStatus("FREE");
        event.setNewStatus("OCCUPIED");

        kafkaConsumerService.consumeAiEvent(JSON.toJSONString(event));

        Mockito.verify(aiAlarmOrchestrator, Mockito.times(1)).handleStatusTransition(Mockito.any(AiEvent.class));
    }
}
