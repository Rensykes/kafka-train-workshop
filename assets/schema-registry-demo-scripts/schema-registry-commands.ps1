
# Schema Registry Testing and Management Scripts (PowerShell)

# NOTE: When using PowerShell, curl may be an alias for Invoke-WebRequest.
# If you want the raw curl binary (bundled with Git/Curl for Windows), call it explicitly as "curl.exe".

Write-Host "=== Schema Registry Management (PowerShell) ===`n"

Write-Host "1) List all registered schema subjects"
curl.exe http://localhost:8081/subjects | ConvertFrom-Json | ConvertTo-Json -Depth 10

Write-Host "`n2) Get versions of the train position schema"
curl.exe http://localhost:8081/subjects/train-locations-avro-value/versions | ConvertFrom-Json | ConvertTo-Json -Depth 10

Write-Host "`n3) Get the latest train position schema (parsed)"
# The Schema Registry returns a JSON object with a 'schema' string. Parse twice to get the Avro JSON.
($schemaResp = curl.exe http://localhost:8081/subjects/train-locations-avro-value/versions/latest | ConvertFrom-Json).schema | ConvertFrom-Json | ConvertTo-Json -Depth 20

Write-Host "`n4) Get versions of the train alert schema"
curl.exe http://localhost:8081/subjects/train-alerts-avro-value/versions | ConvertFrom-Json | ConvertTo-Json -Depth 10

Write-Host "`n5) Get the latest train alert schema (parsed)"
($schemaResp = curl.exe http://localhost:8081/subjects/train-alerts-avro-value/versions/latest | ConvertFrom-Json).schema | ConvertFrom-Json | ConvertTo-Json -Depth 20

Write-Host "`n6) Check global compatibility level"
curl.exe http://localhost:8081/config | ConvertFrom-Json | ConvertTo-Json -Depth 10

Write-Host "`n7) Set compatibility to BACKWARD (example)"
Write-Host "# Example (uncomment to run):"
Write-Host "# curl.exe -X PUT -H 'Content-Type: application/vnd.schemaregistry.v1+json' --data '{\"compatibility\":\"BACKWARD\"}' http://localhost:8081/config"

Write-Host "`n8) Test compatibility of a new schema (example)"
# Example: a reader-only schema that only contains trainId (BACKWARD-compatible because reader ignores extra writer fields)
$compatTest = '{"schema":"{\"type\":\"record\",\"name\":\"TrainPosition\",\"doc\":\"V3: Reader-only schema with only trainId (string). BACKWARD-compatible because reader ignores extra writer fields (timestamp, fuelLevel) via Avro resolution.\",\"fields\":[{\"name\":\"trainId\",\"type\":\"string\",\"doc\":\"Unique identifier for the train (kept)\"}]}"}'
Write-Host "# Compatibility test payload:\n$compatTest`n"
curl.exe -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" --data $compatTest http://localhost:8081/compatibility/subjects/train-locations-avro-value/versions/latest | ConvertFrom-Json | ConvertTo-Json -Depth 5

Write-Host "`n9) Test incompatible change example (changing a field type)"
$badTest = '{"schema":"{\"type\":\"record\",\"name\":\"TrainPosition\",\"doc\":\"V3: This schema changes type of trainId from string->int. Incompatible because changing field types breaks reader/writer resolution and may break existing consumers.\",\"fields\":[{\"name\":\"trainId\",\"type\":\"int\",\"doc\":\"Unique identifier for the train (type changed to int)\"}]}"}'
Write-Host "# Incompatible test payload:\n$badTest`n"
curl.exe -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" --data $badTest http://localhost:8081/compatibility/subjects/train-locations-avro-value/versions/latest | ConvertFrom-Json | ConvertTo-Json -Depth 5

Write-Host "`n10) Register (push) a compatible new schema version (example)"
$newSchema = '{"schema":"{\"type\":\"record\",\"name\":\"TrainPosition\",\"doc\":\"V3: Add passengerCount (nullable).\",\"fields\":[{\"name\":\"trainId\",\"type\":\"string\"},{\"name\":\"latitude\",\"type\":\"double\"},{\"name\":\"longitude\",\"type\":\"double\"},{\"name\":\"speedKph\",\"type\":\"int\"},{\"name\":\"timestamp\",\"type\":\"long\",\"default\":0},{\"name\":\"fuelLevel\",\"type\": [\"null\",\"int\"],\"default\":null},{\"name\":\"passengerCount\",\"type\": [\"null\",\"int\"],\"default\":null}]}"}'
Write-Host "# Register payload preview:\n$newSchema`n"
curl.exe -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" --data $newSchema http://localhost:8081/subjects/train-locations-avro-value/versions | ConvertFrom-Json | ConvertTo-Json -Depth 5

Write-Host "`n11) Get schema by ID (example id=1)"
curl.exe http://localhost:8081/schemas/ids/1 | ConvertFrom-Json | ConvertTo-Json -Depth 10

Write-Host "`n12) Delete a schema subject (BE CAREFUL!)"
Write-Host "# Example (uncomment to run): curl.exe -X DELETE http://localhost:8081/subjects/train-locations-avro-value"

Write-Host "`n=== End of Schema Registry commands ==="
