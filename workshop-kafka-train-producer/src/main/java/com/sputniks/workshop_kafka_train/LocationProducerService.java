package com.sputniks.workshop_kafka_train;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.sputniks.workshop_kafka_train.avro.TrainPosition;

import java.util.Random;

@Service
@EnableScheduling
public class LocationProducerService {

    private static final Logger log = LoggerFactory.getLogger(LocationProducerService.class);
    private static final String TOPIC_NAME = "train-locations";

    // A list of train IDs to simulate
    private static final String[] TRAINS = {"T-81A", "T-45B", "T-99C"};
    private final Random random = new Random();

    @Autowired
    private KafkaTemplate<String, TrainPosition> kafkaTemplate;

    @Scheduled(fixedRate = 2000)
    public void sendTrainPosition() {
        String trainId = TRAINS[random.nextInt(TRAINS.length)];

        TrainPosition position = TrainPosition.newBuilder()
        .setTrainId(trainId)
        .setSpeedKph(random.nextInt(120) + 30)
        .setEngineTemperature(85.0 + random.nextDouble() * 10) // * This field is not in the conusmer!
        // * Nonethelss, it is included in the Avro schema with a default value.
        // * This means the consumer can still process it without issues.
        .build();

        // * Send the message. The trainId is used as the message key.
        // * Kafka guarantees that all messages with the same key go to the same partition.
        // * This is crucial for ordering events for a specific train.
        kafkaTemplate.send(TOPIC_NAME, position.getTrainId().toString(), position);

        log.info("Sent position update for train {}: {}", position.getTrainId(), position);
    }
}