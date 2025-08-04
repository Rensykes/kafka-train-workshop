package com.sputniks.workshop_kafka_train;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class DashboardListener {

    private static final Logger log = LoggerFactory.getLogger(DashboardListener.class);
    
    // * This consumer is in a DIFFERENT group.
    // * This means it gets its own, independent copy of all messages.
    // * The 'id' here will be used as a prefix in the logs to distinguish consumer instances.
    @KafkaListener(id = "dashboard-consumer", topics = "train-locations", groupId = "dashboard-group")
    public void listen(
            TrainPosition position,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {

        log.info("[Partition {}] Received position for train key {}: Speed {} kph",
                partition,
                key,
                position.speedKph()
        );
    }
}