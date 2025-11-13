# Avro Schemas for Kafka Train Workshop

This directory contains the Avro schemas used for demonstrating Schema Registry and schema evolution in the Kafka Train Workshop.

## Schemas

### train-position-v1.avsc
The initial version of the TrainPosition schema with basic fields:
- trainId (string)
- latitude (double)
- longitude (double)
- speedKph (int)

### train-position-v2.avsc
An evolved version that adds new fields while maintaining backward compatibility:
- All V1 fields
- timestamp (long) - with default value for backward compatibility
- fuelLevel (nullable int) - optional field for backward compatibility

### train-alert.avsc
Schema for train alerts/notifications with enums for alert types and severity levels.

## Schema Evolution Strategy

These schemas demonstrate:
1. **Backward Compatibility**: V2 can read data written by V1 (default values for new fields)
2. **Forward Compatibility**: V1 can read data written by V2 (ignores unknown fields)
3. **Type Safety**: Enums prevent invalid alert types
4. **Documentation**: Each field includes documentation

## Usage

These schemas are registered with the Confluent Schema Registry and used by:
- `workshop-kafka-train-avro-producer`: Produces data using V1 or V2 schemas
- `workshop-kafka-train-avro-consumer`: Consumes data handling both versions
