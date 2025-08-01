package com.sputniks.workshop_kafka_train;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class SpeedUpdateHandler {

    private static final Logger log = LoggerFactory.getLogger(SpeedUpdateHandler.class);
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public SpeedUpdateHandler(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(topics = "train-speed-averages", groupId = "dashboard-webapp-group")
    public void handleSpeedUpdate(TrainAverageSpeed speedUpdate) {
        log.info("Received speed update for {}: {}. Pushing to WebSocket.",
            speedUpdate.trainId(), speedUpdate.averageSpeed());

        // Send the received object to the "/topic/speed-updates" WebSocket topic
        messagingTemplate.convertAndSend("/topic/speed-updates", speedUpdate);
    }
}