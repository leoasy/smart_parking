package com.ruoyi.biz.kafka;

import com.alibaba.fastjson.JSON;
import com.ruoyi.biz.domain.AiEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducerService {

    private static final String AI_EVENT_TOPIC = "ai-event-topic";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendAiEvent(AiEvent event) {
        String message = JSON.toJSONString(event);
        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                kafkaTemplate.send(AI_EVENT_TOPIC, message).get();
                log.info("[KafkaProducer] Event sent: eventId={}, topic={}", event.getEventId(), AI_EVENT_TOPIC);
                return;
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                log.error("[KafkaProducer] Send interrupted: eventId={}", event.getEventId(), ex);
                return;
            } catch (ExecutionException ex) {
                if (attempt == maxRetries) {
                    log.error("[KafkaProducer] All {} retries failed: eventId={}, cause={}",
                        maxRetries, event.getEventId(), ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
                } else {
                    log.warn("[KafkaProducer] Retry {}/{} failed: eventId={}", attempt, maxRetries, event.getEventId());
                    try { Thread.sleep(1000L * attempt); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); return; }
                }
            }
        }
    }
}
