package com.weyland.starter.synthetichumancorestarter.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AuditMode auditMode;
    private final String topic;

    public AuditService(
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${synthetichuman.audit.mode:CONSOLE}") String auditMode,
            @Value("${synthetichuman.audit.kafka-topic:android-audit}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.auditMode = AuditMode.valueOf(auditMode.toUpperCase());
        this.topic = topic;
    }

    public void audit(String message) {
        if (auditMode == AuditMode.KAFKA) {
            kafkaTemplate.send(topic, message);
        } else {
            logger.info("[AUDIT] {}", message);
        }
    }
}