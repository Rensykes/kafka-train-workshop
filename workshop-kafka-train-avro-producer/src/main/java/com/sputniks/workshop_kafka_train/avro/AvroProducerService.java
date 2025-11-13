package com.sputniks.workshop_kafka_train.avro;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Demonstrates Schema Registry resilience by:
 * 1. Producing data with V1 schema initially
 * 2. Switching to V2 schema after some time
 * 3. Generating alerts with proper enum validation
 * 
 * This shows how consumers can handle schema evolution seamlessly.
 */
@Service
@EnableScheduling
public class AvroProducerService {

    private static final Logger log = LoggerFactory.getLogger(AvroProducerService.class);
    private static final String POSITION_TOPIC = "train-locations-avro";
    private static final String ALERT_TOPIC = "train-alerts-avro";
    
    private static final String[] TRAINS = {"T-AVR1", "T-AVR2", "T-AVR3"};
    private final Random random = new Random();
    
    // Toggle between V1 and V2 schema after 30 seconds to demonstrate evolution
    private boolean useV2Schema = false;
    private long startTime = System.currentTimeMillis();
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Send train position data - alternates between V1 and V2 schemas
     */
    @Scheduled(fixedRate = 2000)
    public void sendTrainPosition() {
        // Switch to V2 schema after 30 seconds
        if (!useV2Schema && (System.currentTimeMillis() - startTime) > 30000) {
            useV2Schema = true;
            log.info("🔄 SCHEMA EVOLUTION: Now using evolved schema with additional fields!");
        }
        
        String trainId = TRAINS[random.nextInt(TRAINS.length)];
        double latitude = 48.85 + random.nextDouble() * 0.1;
        double longitude = 2.35 + random.nextDouble() * 0.1;
        int speedKph = random.nextInt(120) + 30;
        
            TrainPosition position;
        
        if (useV2Schema) {
            // V2 schema with timestamp and fuel level
                position = TrainPosition.newBuilder()
                .setTrainId(trainId)
                .setLatitude(latitude)
                .setLongitude(longitude)
                .setSpeedKph(speedKph)
                .setTimestamp(System.currentTimeMillis())
                .setFuelLevel(random.nextInt(101)) // 0-100%
                .build();
            
            log.info("📤 [V2] Sent position for {}: speed={}kph, fuel={}%", 
                trainId, speedKph, position.getFuelLevel());
        } else {
            // V1 schema - basic fields only (timestamp=0, fuelLevel=null via defaults)
                position = TrainPosition.newBuilder()
                .setTrainId(trainId)
                .setLatitude(latitude)
                .setLongitude(longitude)
                .setSpeedKph(speedKph)
                .build();
            
            log.info("📤 [V1] Sent position for {}: speed={}kph", trainId, speedKph);
        }
        
        kafkaTemplate.send(POSITION_TOPIC, trainId, position);
        
        // Generate alerts for slow trains
        if (speedKph < 60) {
            sendMaintenanceAlert(trainId, speedKph);
        }
        
        // Generate low fuel alerts in V2 mode
        if (useV2Schema && position.getFuelLevel() != null && position.getFuelLevel() < 20) {
            sendLowFuelAlert(trainId, position.getFuelLevel());
        }
    }

    /**
     * Send maintenance alert when train is going too slow
     */
    private void sendMaintenanceAlert(String trainId, int speedKph) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("currentSpeed", String.valueOf(speedKph));
        metadata.put("threshold", "60");
        
        TrainAlert alert = TrainAlert.newBuilder()
            .setAlertId(UUID.randomUUID().toString())
            .setTrainId(trainId)
            .setAlertType(AlertType.MAINTENANCE)
            .setSeverity(speedKph < 40 ? Severity.HIGH : Severity.MEDIUM)
            .setMessage(String.format("Train %s operating below normal speed: %d kph", trainId, speedKph))
            .setTimestamp(System.currentTimeMillis())
            .setMetadata(metadata)
            .build();
        
        kafkaTemplate.send(ALERT_TOPIC, trainId, alert);
        log.warn("⚠️  ALERT: Maintenance needed for {} (speed: {} kph)", trainId, speedKph);
    }
    
    /**
     * Send low fuel alert (only available in V2 schema)
     */
    private void sendLowFuelAlert(String trainId, int fuelLevel) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("fuelLevel", String.valueOf(fuelLevel));
        metadata.put("threshold", "20");
        
        TrainAlert alert = TrainAlert.newBuilder()
            .setAlertId(UUID.randomUUID().toString())
            .setTrainId(trainId)
            .setAlertType(AlertType.LOW_FUEL)
            .setSeverity(fuelLevel < 10 ? Severity.CRITICAL : Severity.HIGH)
            .setMessage(String.format("Train %s fuel level critical: %d%%", trainId, fuelLevel))
            .setTimestamp(System.currentTimeMillis())
            .setMetadata(metadata)
            .build();
        
        kafkaTemplate.send(ALERT_TOPIC, trainId, alert);
        log.warn("🔋 ALERT: Low fuel for {} ({}%)", trainId, fuelLevel);
    }
}
