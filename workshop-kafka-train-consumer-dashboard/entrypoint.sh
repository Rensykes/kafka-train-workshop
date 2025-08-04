#!/bin/sh

# * THIS SCRIPT SHOULDNT BE NEEDED ANYMORE, BUT JUST IN CASE WE KEEP IT FOR NOW
# * It waits for the topics to be ready before starting the Spring Boot application.
# * This is a workaround for the issue where the application starts before the topics are created.
# * Ideally, the application should be able to handle this gracefully without needing this script.
# * In case of need, change the sleep time to a higher value if the topics take longer to be created.

# Exit immediately if a command exits with a non-zero status.
set -e

echo "--- Waiting for 15 seconds to give Kafka and kafka-init time to complete ---"
sleep 0

echo "--- Wait finished. Starting the Spring Boot application ---"

exec java -jar /app/app.jar