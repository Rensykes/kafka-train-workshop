#!/bin/bash
# Schema Registry Testing and Management Scripts

echo "=== Schema Registry Management ==="
echo ""

echo "1. List all registered schemas:"
curl -s http://localhost:8081/subjects | jq
echo ""

echo "2. Get versions of train-locations-avro schema:"
curl -s http://localhost:8081/subjects/train-locations-avro-value/versions | jq
echo ""

echo "3. Get latest train-locations-avro schema:"
curl -s http://localhost:8081/subjects/train-locations-avro-value/versions/latest | jq '.schema | fromjson'
echo ""

echo "4. Get versions of train-alerts-avro schema:"
curl -s http://localhost:8081/subjects/train-alerts-avro-value/versions | jq
echo ""

echo "5. Get latest train-alerts-avro schema:"
curl -s http://localhost:8081/subjects/train-alerts-avro-value/versions/latest | jq '.schema | fromjson'
echo ""

echo "6. Check global compatibility level:"
curl -s http://localhost:8081/config | jq
echo ""

echo "=== Schema Registry is ready! ==="
