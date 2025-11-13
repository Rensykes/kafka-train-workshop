package com.sputniks.workshop_kafka_train.avro;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Demonstrates Schema Registry resilience by consuming messages that may be:
 * 1. V1 schema (basic fields)
 * 2. V2 schema (with additional timestamp and fuelLevel)
 * 
 * The consumer handles both versions gracefully thanks to Schema Registry's
 * compatibility checking and Avro's built-in support for schema evolution.
 */
@Service
public class AvroConsumerService {

    private static final Logger log = LoggerFactory.getLogger(AvroConsumerService.class);

    /**
     * Consumes train position data with schema evolution support
     * 
     * RESILIENCE FEATURES:
     * - Automatically handles V1 and V2 schemas
     * - Schema Registry ensures compatibility
     * - No code changes needed when schema evolves (if compatible)
     * - Avro handles missing/extra fields transparently
     */
    @KafkaListener(topics = "train-locations-avro", groupId = "avro-consumer-group")
    public void consumeTrainPosition(ConsumerRecord<String, Object> record) {
        String trainId = record.key();
        Object value = record.value();
        
        // The same class handles both V1 (without optional fields) and V2 (with optional fields)
        if (value instanceof TrainPosition) {
            TrainPosition position = (TrainPosition) value;
            
            // Check if this has the V2 fields (timestamp and fuelLevel)
            boolean hasV2Fields = position.getTimestamp() != 0 || position.getFuelLevel() != null;
            
            if (hasV2Fields) {
                // V2 format - has additional fields
                String fuelInfo = position.getFuelLevel() != null 
                    ? String.format(", fuel=%d%%", position.getFuelLevel())
                    : ", fuel=N/A";
                
                log.info("📥 [V2] Received: train={}, speed={}kph{}, timestamp={}", 
                    trainId, 
                    position.getSpeedKph(),
                    fuelInfo,
                    position.getTimestamp());
                
                // Example: Additional processing for V2 features
                if (position.getFuelLevel() != null && position.getFuelLevel() < 30) {
                    log.warn("⚠️  Low fuel warning for {}: {}%", trainId, position.getFuelLevel());
                }
            } else {
                // V1 format - basic fields only
                log.info("📥 [V1] Received: train={}, speed={}kph, lat={}, lon={}", 
                    trainId, 
                    position.getSpeedKph(),
                    position.getLatitude(),
                    position.getLongitude());
            }
        } else {
            log.warn("⚠️  Unknown schema type: {}", value.getClass().getName());
        }
    }

    /**
     * Consumes train alerts with enum validation
     * 
     * RESILIENCE FEATURES:
     * - Enums prevent invalid alert types at the schema level
     * - Type-safe consumption with generated classes
     * - Metadata map allows flexible extension without schema changes
     */
    @KafkaListener(topics = "train-alerts-avro", groupId = "avro-consumer-group")
    public void consumeTrainAlert(ConsumerRecord<String, TrainAlert> record) {
        TrainAlert alert = record.value();
        
        String emoji = switch (alert.getSeverity()) {
            case CRITICAL -> "🚨";
            case HIGH -> "⚠️";
            case MEDIUM -> "⚡";
            case LOW -> "ℹ️";
        };
        
        log.info("{} ALERT [{}] {} - Train: {}, Message: {}", 
            emoji,
            alert.getSeverity(),
            alert.getAlertType(),
            alert.getTrainId(),
            alert.getMessage());
        
        // Log metadata if present
        if (alert.getMetadata() != null && !alert.getMetadata().isEmpty()) {
            log.debug("   Metadata: {}", alert.getMetadata());
        }
        
        // Handle critical alerts specially
        if (alert.getSeverity() == Severity.CRITICAL) {
            log.error("🚨 CRITICAL ALERT requires immediate action: {}", alert.getMessage());
            // In a real system, this would trigger automated responses
        }
    }
}
