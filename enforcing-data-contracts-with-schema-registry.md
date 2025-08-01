
# 🚉 Enforcing Data Contracts with Schema Registry and Avro

In the previous stage of the workshop, our services communicated using raw JSON. This is simple to start with, but can be brittle in a real-world system. What happens if one service changes the structure of a message? How do we prevent breaking changes from crashing our downstream consumers?

This is where **Confluent Schema Registry** comes in. It acts as a centralized, versioned "rulebook" for all the data flowing through Kafka. We will now refactor our services to use Schema Registry with the **Apache Avro** data format to create a more robust and efficient system.

## 🌟 Key Benefits

* ✅ **Schema Enforcement:** Prevents invalid data from entering the system.
* 🔄 **Safe Schema Evolution:** Supports backward-compatible changes.
* 📦 **Efficiency:** Avro’s binary format reduces message size.

---

## 🧱 Step 1: Update the Environment

Add the Schema Registry service via `docker-compose.yml`.

```bash
# 🛑 Stop everything
docker-compose down

# 🔁 Start all services, including Schema Registry
docker-compose up -d --build
```

📍 Visit [http://localhost:8081](http://localhost:8081) to check it's running. You should see `{}`.

---

## 🏗️ Step 2: Refactor the Producer (`workshop-kafka-train-producer`)

Our source of truth is now an Avro schema (`.avsc` file).

### 🔍 What to Check

- `src/main/resources/avro/TrainPosition.avsc`: Defines the schema.
- `pom.xml`: Includes `kafka-avro-serializer` and `avro-maven-plugin`.
- `application.yml`: Uses `KafkaAvroSerializer` and `schema.registry.url`.
- `LocationProducerService.java`: Uses generated `TrainPosition` class.

### ▶️ To Run:

```bash
cd workshop-kafka-train
mvn spring-boot:run
```

📡 The first message registers the schema and subsequent ones are compact binary payloads.

---

## 🖥️ Step 3: Refactor the Consumer (`workshop-kafka-train-consumer-dashboard`)

### 🔍 What to Check

- `pom.xml` and `application.yml`: Use `KafkaAvroDeserializer` and `specific.avro.reader: true`.
- `DashboardListener.java`: Expects deserialized `TrainPosition` objects.

### ▶️ To Run

```bash
# Start one instance
docker-compose up -d --scale dashboard-consumer=1

# Follow logs
docker-compose logs -f dashboard-consumer
```

✅ You should see AVRO messages logged.

---

## 🧪 Part 4: The Power of Schema Evolution

### 🛠️ Scenario: Add Engine Temperature

1. Stop the producer (`Ctrl+C`).
2. Edit `TrainPosition.avsc`, add:
```json
{ "name": "engineTemperature", "type": "double", "default": 90.0 }
```
3. Update `LocationProducerService.java` to set this field.
4. Restart producer:
```bash
mvn spring-boot:run
```

### 🎉 Result

- 🆕 Schema v2 is registered.
- ✅ Consumer (using v1) keeps working — ignores the new field.

This demonstrates **true decoupling**: 🚀 deploy producers independently without breaking consumers.
