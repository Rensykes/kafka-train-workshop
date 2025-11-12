#!/bin/sh

# Exit immediately if a command exits with a non-zero status.
set -e

echo "--- Waiting for 15 seconds to give Kafka and kafka-init time to complete ---"
sleep 15

echo "--- Wait finished. Starting the Spring Boot application ---"

exec java -jar /app/app.jar