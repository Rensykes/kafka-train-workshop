# Schema Registry Testing and Management Scripts

## List all registered schemas
curl http://localhost:8081/subjects

## Get versions of the train position schema
curl http://localhost:8081/subjects/train-locations-avro-value/versions

## Get the latest train position schema
curl http://localhost:8081/subjects/train-locations-avro-value/versions/latest | ConvertFrom-Json | Select-Object -ExpandProperty schema | ConvertFrom-Json

## Get versions of the train alert schema
curl http://localhost:8081/subjects/train-alerts-avro-value/versions

## Get the latest train alert schema
curl http://localhost:8081/subjects/train-alerts-avro-value/versions/latest | ConvertFrom-Json | Select-Object -ExpandProperty schema | ConvertFrom-Json

## Check global compatibility level
curl http://localhost:8081/config

## Set compatibility to BACKWARD (default)
# curl -X PUT -H "Content-Type: application/vnd.schemaregistry.v1+json" --data '{"compatibility":"BACKWARD"}' http://localhost:8081/config

## Test compatibility of a new schema (example)
# curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" `
#   --data '{"schema":"{\"type\":\"record\",\"name\":\"TrainPositionV3\",\"fields\":[{\"name\":\"trainId\",\"type\":\"string\"}]}"}' `
#   http://localhost:8081/compatibility/subjects/train-locations-avro-value/versions/latest

## Delete a schema subject (BE CAREFUL!)
# curl -X DELETE http://localhost:8081/subjects/train-locations-avro-value

## Get schema by ID
# curl http://localhost:8081/schemas/ids/1
