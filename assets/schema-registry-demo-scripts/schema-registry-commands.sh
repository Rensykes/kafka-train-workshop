#!/bin/bash
# Schema Registry Testing and Management Scripts (POSIX)

set -euo pipefail

echo "=== Schema Registry Management ==="

echo "1) List all registered schema subjects"
curl -s http://localhost:8081/subjects | jq

echo "\n2) Get versions of train-locations-avro schema"
curl -s http://localhost:8081/subjects/train-locations-avro-value/versions | jq

echo "\n3) Get latest train-locations-avro schema (parsed)"
curl -s http://localhost:8081/subjects/train-locations-avro-value/versions/latest | jq '.schema | fromjson'

echo "\n4) Get versions of train-alerts-avro schema"
curl -s http://localhost:8081/subjects/train-alerts-avro-value/versions | jq

echo "\n5) Get latest train-alerts-avro schema (parsed)"
curl -s http://localhost:8081/subjects/train-alerts-avro-value/versions/latest | jq '.schema | fromjson'

echo "\n6) Check global compatibility level"
curl -s http://localhost:8081/config | jq

echo "\n7) Set compatibility to BACKWARD (example)"
echo "# curl -X PUT -H 'Content-Type: application/vnd.schemaregistry.v1+json' --data '{\"compatibility\":\"BACKWARD\"}' http://localhost:8081/config"

echo "\n8) Test compatibility of a new schema (example)"
compatTest='{"schema":"{\"type\":\"record\",\"name\":\"TrainPosition\",\"doc\":\"V3: Reader-only schema with only trainId (string). BACKWARD-compatible because reader ignores extra writer fields (timestamp, fuelLevel) via Avro resolution.\",\"fields\":[{\"name\":\"trainId\",\"type\":\"string\",\"doc\":\"Unique identifier for the train (kept)\"}]}"}'
echo "Compatibility test payload: $compatTest"
curl -s -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" --data "$compatTest" http://localhost:8081/compatibility/subjects/train-locations-avro-value/versions/latest | jq

echo "\n9) Test incompatible change example (changing a field type)"
badTest='{"schema":"{\"type\":\"record\",\"name\":\"TrainPosition\",\"doc\":\"V3: This schema changes type of trainId from string->int. Incompatible because changing field types breaks reader/writer resolution and may break existing consumers.\",\"fields\":[{\"name\":\"trainId\",\"type\":\"int\",\"doc\":\"Unique identifier for the train (type changed to int)\"}]}"}'
echo "Incompatible test payload: $badTest"
curl -s -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" --data "$badTest" http://localhost:8081/compatibility/subjects/train-locations-avro-value/versions/latest | jq

echo "\n10) Register (push) a compatible new schema version (example)"
newSchema='{"schema":"{\"type\":\"record\",\"name\":\"TrainPosition\",\"doc\":\"V3: Add passengerCount (nullable).\",\"fields\":[{\"name\":\"trainId\",\"type\":\"string\"},{\"name\":\"latitude\",\"type\":\"double\"},{\"name\":\"longitude\",\"type\":\"double\"},{\"name\":\"speedKph\",\"type\":\"int\"},{\"name\":\"timestamp\",\"type\":\"long\",\"default\":0},{\"name\":\"fuelLevel\",\"type\": [\"null\",\"int\"],\"default\":null},{\"name\":\"passengerCount\",\"type\": [\"null\",\"int\"],\"default\":null}]}"}'
echo "Register payload preview: $newSchema"
curl -s -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" --data "$newSchema" http://localhost:8081/subjects/train-locations-avro-value/versions | jq

echo "\n11) Get schema by ID (example id=1)"
curl -s http://localhost:8081/schemas/ids/1 | jq

echo "\n12) Delete a schema subject (BE CAREFUL!)"
echo "# curl -X DELETE http://localhost:8081/subjects/train-locations-avro-value"

echo "\n=== Schema Registry is ready! ==="
