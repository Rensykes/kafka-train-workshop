package com.sputniks.workshop_kafka_train;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DashboardListener {

    private static final Logger log = LoggerFactory.getLogger(DashboardListener.class);
    
    // * This consumer is in a DIFFERENT group.
    // * This means it gets its own, independent copy of all messages.
    @KafkaListener(topics = "train-locations", groupId = "dashboard-group")
    public void listen(TrainPosition position) {
        log.info("[Dashboard] Received position for train {}: Speed {} kph at ({}, {})",
                position.trainId(),
                position.speedKph(),
                position.latitude(),
                position.longitude()
        );
    }
}