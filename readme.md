# 🚂 Kafka for Train Transportation: A Multi-Language Workshop

Welcome to the Connected Train Platform workshop! 🎉

In this workshop, you will build a multi-service, multi-language event-driven system using Apache Kafka. We will simulate a fleet of trains sending real-time location data, process this data to generate insights, and visualize it on a live dashboard.

This project demonstrates core Kafka concepts like:
- 📤 Producers and Consumers
- 📁 Topics, Partitions, and Consumer Groups
- ⚖️ Partition Rebalancing
- 🌐 Language Interoperability (Java & Python)
- 🔄 Stateful Stream Processing with Kafka Streams
- 📊 Real-time data visualization with WebSockets
- 🛡️ **Schema Registry and Avro for Data Resilience**

**⚠️ Note on Architecture:** For the sake of simplicity in this workshop, we used the same package structure across all Java applications to make the serialized objects (like `TrainPosition`) available across all services. In a real-world scenario, you should handle shared data models differently by:
- 📦 Creating a separate shared library/JAR for common data models
- 🗄️ **Using schema registries (like Confluent Schema Registry) with Avro or JSON Schema** ⭐ See Part 3 below!
- 🔄 Implementing proper versioning strategies for your data contracts
- 🏗️ Following microservice principles where each service owns its data model

## 🏗️ System Architecture

Our system consists of several independent microservices that communicate through Kafka:

![Light mode](assets/img/diagram-light.drawio.png#gh-light-mode-only)
![Dark mode](assets/img/diagram-dark.drawio.png#gh-dark-mode-only)

## 📋 Prerequisites

Before you begin, please ensure you have the following software installed on your system:

- 🐳 **Docker & Docker Compose:** To run Kafka and Zookeeper locally.
- ☕ **Java JDK 17+:** All Java services are built with Java 17.
- 🔨 **Apache Maven:** To build and run the Java projects.
- 🐍 **Python 3.8+ & pip:** For the Python maintenance service.
- 📂 **A Git client:** To clone the repository.

---

## 🐳 Docker Compose Options
This workshop can be started entirely using `docker-compose up -d`, which will launch all services including the Kafka broker, topic initialization, and all microservices. For more flexible deployment options:

### Start just Kafka infrastructure:
`docker-compose -f .\docker-compose-kafka.yml up -d`
This launches only Zookeeper, Kafka broker, and Kafka UI, allowing you to run the applications manually as explained in the workshop instructions.

### Start consumer services with scaling:
```bash
docker-compose -f .\docker-compose-consumer.yml up -d --scale dashboard-consumer=3 # This will trigger the recreation of all the containers
docker-compose up -d --scale dashboard-consumer=3 dashboard-consumer # This will trigger the recreation of only the containers that we want to scale
```

This starts the consumer services (including the scalable dashboard consumer) which you can then scale up or down to demonstrate partition rebalancing. The number of consumer instances can be adjusted by changing the --scale parameter.

The full `docker-compose.yml` combines all these services for a complete one-command startup of the entire workshop environment.

## 🎯 Part 1: Setting Up The Environment

This part is the foundation for everything. We will start the Kafka infrastructure and create the "channels" (topics) for our services to communicate.

### 🚀 Step 1. Start Kafka and Zookeeper

Navigate to the root directory of the workshop (where `docker-compose.yml` is located) and run the following command in your terminal:

```bash
docker-compose up -d
```

This command will download the necessary images and start several containers in the background: zookeeper, kafka, kafka-ui, and automatically creates the required topics. You can check that they are running with `docker ps`.

**Kafka UI Dashboard**
Once the containers are running, you can access the Kafka UI web interface at:
```
http://localhost:8099
```
This provides a visual interface to monitor topics, partitions, consumer groups, and messages in real-time! 📈

### ✅ Step 2. Verify Topics Creation

The topics are now created automatically when you run `docker-compose up`. You can verify they were created successfully using these commands:

```bash
# List all topics
docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list

# Get detailed information about the train-locations topic
docker exec kafka kafka-topics --describe --topic train-locations --bootstrap-server localhost:9092

# Get detailed information about the train-speed-averages topic  
docker exec kafka kafka-topics --describe --topic train-speed-averages --bootstrap-server localhost:9092
```

You should see that `train-locations` has 3 partitions and `train-speed-averages` has 1 partition. 🎯

Your Kafka environment is now ready! 🎊

## 🏃‍♂️ Part 2: Running the Microservices

Now we will start each microservice one by one. It is recommended to use a separate terminal window for each service so you can see its logs. 📺

### 🎲 Service 1: The Producer (workshop-kafka-train)

This is the TrainLocationSimulator. It's a Java Spring Boot application that generates fake train location data and sends it to the `train-locations` topic every 2 seconds.

**To Run:**

1. Open a new terminal.
2. Navigate to the project directory:
   ```bash
   cd workshop-kafka-train
   ```
3. Run the application using Maven:
   ```bash
   mvn spring-boot:run
   ```

You should see log messages indicating that train positions are being sent, like `Sent position update for train T-81A....` 🚂

### ⚡ Service 2: The Stream Processor (workshop-kafka-train-streams-analysis)

This is our SpeedAnalysisStream application. It's a Kafka Streams application that reads from `train-locations`, calculates the average speed for each train over a 10-second window, and writes the result to the `train-speed-averages` topic.

**To Run:**

1. Open a new terminal.
2. Navigate to the project directory:
   ```bash
   cd workshop-kafka-train-streams-analysis
   ```
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

This service will run quietly in the background. It doesn't produce much console output unless there's an error. Its job is to continuously transform data between the two topics. 🔄

### 📊 Service 3: The WebSocket Dashboard (workshop-kafka-train-websocket)

This is the DashboardWebApp. This service has two roles:
- 📥 It acts as a Kafka consumer, listening to the `train-speed-averages` topic.
- 🌐 It acts as a WebSocket server, pushing any new data it receives from Kafka directly to connected web browsers.

**To Run:**

1. Open a new terminal.
2. Navigate to the project directory:
   ```bash
   cd workshop-kafka-train-websocket
   ```
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```
4. Once it has started, open your web browser and navigate to:
   ```
   http://localhost:8085/index.html
   ```

After a few seconds (waiting for the first 10-second window to complete), you should see train cards appear and their average speed updating in real-time on the webpage! 🎉

### 🐍 Service 4: The Python Alerter (workshop-kafka-train-python-service)

This Python script demonstrates language interoperability and the power of consumer groups. It listens to the same `train-locations` topic as our Streams application, but for a different purpose: to alert if a train is slowing down (speed is lesser than 60). It uses a different group-id, so it gets a full, independent copy of all messages.

**To Run:**

1. Open a new terminal.
2. Navigate to the project directory:
   ```bash
   cd workshop-kafka-train-python-service
   ```
3. Create and activate a Python virtual environment:
   ```bash
   # Create virtual environment
   python -m venv kafka-workshop
   
   # Activate virtual environment (Windows PowerShell)
   .\kafka-workshop\Scripts\Activate.ps1
   
   # Activate virtual environment (Windows Command Prompt)
   kafka-workshop\Scripts\activate.bat
   
   # Activate virtual environment (macOS/Linux)
   source kafka-workshop/bin/activate
   ```
4. Install the required Python library:
   ```bash
   pip install kafka-python
   ```
5. Run the script:
   ```bash
   python maintenance_alerter.py
   ```

Now, watch this terminal. Whenever the simulator generates a position with `speedKph < 60`, a `[MAINTENANCE ALERT]` will appear here, while the other services continue their work unaffected.

## 🚀 Testing Kafka Partition Rebalancing

### 📈 Service 5: The Dashboard Consumer (Demonstrates Partition Rebalancing)

This service is now included in the Docker Compose setup and demonstrates one of Kafka's most powerful features: **automatic partition rebalancing**. 

The `dashboard-consumer` service listens to the `train-locations` topic (which has 3 partitions) and logs which partition each message comes from. This is perfect for demonstrating how Kafka distributes work across multiple consumer instances.

### 🧪 Testing Partition Rebalancing

**🎯 Key Concept:** When you have multiple consumers in the same consumer group, Kafka automatically distributes the topic partitions among them. When consumers join or leave the group, Kafka triggers a "rebalance" to redistribute the partitions fairly.

**To test this:**

1. **Start with a single consumer instance:**
   ```bash
   docker-compose up -d
   ```
   
   Watch the logs of the dashboard consumer:
   ```bash
   docker-compose logs -f dashboard-consumer
   ```
   
   You'll see it consuming from all 3 partitions (0, 1, and 2).

2. **Scale up to 3 consumer instances:**
   ```bash
   docker-compose up --build -d --scale dashboard-consumer=3
   ```
   
   Watch the logs:
   ```bash
   docker-compose logs -f dashboard-consumer
   ```
   
   **What you'll observe:**
   - ⚖️ Kafka triggers a rebalance
   - 🎯 Each consumer instance now gets assigned to exactly 1 partition
   - 🚀 The workload is now distributed across 3 consumer instances
   - ✅ Each message is processed by only one consumer (no duplicates)

3. **Scale down to 2 consumer instances:**
   ```bash
   docker-compose up -d --scale dashboard-consumer=2
   ```
   
   **What you'll observe:**
   - 🔄 Another rebalance occurs
   - ⚖️ One consumer gets 2 partitions, the other gets 1 partition
   - ✅ Messages continue to be processed without loss

4. **Scale back to 1 consumer:**
   ```bash
   docker-compose up -d --scale dashboard-consumer=1
   ```
   
   The single remaining consumer takes over all 3 partitions again.

### Monitoring Rebalancing with Kafka UI

You can visually monitor the rebalancing process using the Kafka UI at `http://localhost:8099`: 🎯

1. Go to **Consumer Groups** → **dashboard-group** 👥
2. Watch the **Partition Assignment** change as you scale consumers up and down ⚖️
3. Monitor the **Lag** and **Offset** information for each partition 📈

### 📋 Understanding the Logs

In the dashboard consumer logs, you'll see messages like:
```
[Partition 0] Received position for train key T-123: Speed 75 kph
[Partition 1] Received position for train key T-456: Speed 82 kph
[Partition 2] Received position for train key T-789: Speed 91 kph
```

The `[Partition X]` prefix shows which partition the message came from, helping you understand the distribution of work. 🎯

## 🎊 Summary: Running The Full System

You should now have multiple services running:
- 🐳 Kafka infrastructure (Docker containers)
- 🚂 Train location simulator (Java producer)
- ⚡ Stream processor for speed analysis (Kafka Streams)
- 📊 WebSocket dashboard (Java consumer + web server)
- 🐍 Python maintenance alerter (Python consumer)
- 📈 Dashboard consumer service (demonstrating rebalancing)

Plus monitoring tools:
- 🎨 Kafka UI web interface at `http://localhost:8099`
- 📊 Live train dashboard at `http://localhost:8085/index.html`

## 🛠️ Useful Commands for Monitoring

```bash
# List all topics
docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list

# Describe a specific topic
docker exec kafka kafka-topics --describe --topic train-locations --bootstrap-server localhost:9092

# List consumer groups
docker exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --list

# Describe a consumer group
docker exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group dashboard-group

# View container logs
docker-compose logs -f dashboard-consumer
docker-compose logs -f kafka

# Scale services
docker-compose up -d --scale dashboard-consumer=3 # This will trigger the recreation of all the containers
docker-compose up -d --scale dashboard-consumer=3 dashboard-consumer # This will trigger the recreation of only the containers that we want to scale

```

## 🛑 Shutting Down

- To stop each non-Docker microservice, go to its terminal window and press `Ctrl + C`. ⌨️
- To stop all Docker containers, run the following command from the root directory:
  ```bash
  docker-compose down
  ```

## 🔧 Troubleshooting

**❌ Error: ListenerExecutionFailedException... Cannot convert from [java.lang.String]**

This is a classic configuration issue. It means your consumer application's `.yml` file is not correctly configured to use the JsonDeserializer. Ensure the configuration is under `spring.kafka.consumer` and not `spring.kafka.streams`, and that the `value-deserializer` and `spring.json.*` properties are correctly set.

**❌ Error: Connection to node -1 (localhost/127.0.0.1:9092) could not be established. Broker may not be available.**

This means your application cannot connect to Kafka. Check that your Docker containers are running with `docker ps`. If not, run `docker-compose up -d`.

**❌ Error: Port is already in use**

One of the Java services is trying to start on a port that is already occupied. Check the `server.port` property in the `src/main/resources/application.yml` file of the failing service and change it to a free port. The `workshop-kafka-train-websocket` service uses port 8085 by default.

**🐍 The Python script gives an error.**

- Make sure you have installed the dependency with `pip install kafka-python`. 📦
- Verify Kafka is running and accessible at `localhost:9092`. ✅

**💻 PowerShell execution policy error on Windows:**

If you get an error like "L'esecuzione di script è disabilitata nel sistema in uso" when trying to activate the virtual environment, you have several options:

1. **Temporary solution (recommended):** Open PowerShell as administrator and run:
   ```powershell
   Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope Process
   ```
   This allows script execution only for the current session.

2. **Alternative activation methods:**
   ```bash
   # Use Command Prompt instead of PowerShell
   kafka-workshop\Scripts\activate.bat
   
   # Or use Python directly
   python -m venv kafka-workshop --prompt kafka-workshop
   ```

3. **Permanent solution (use with caution):**
   ```powershell
   Set-ExecutionPolicy RemoteSigned -Scope CurrentUser
   ```

**🐳 Issues with scaling dashboard consumers:**

- Make sure you're running the `docker-compose up --scale` command from the root directory where the `docker-compose.yml` file is located. 📁
- If containers fail to start due to port conflicts, check the dashboard consumer service configuration - it should not expose any specific ports since it's only a consumer. ⚠️

---

## 🛡️ Part 3: Schema Registry and Avro for Data Resilience

This section demonstrates how Schema Registry and Apache Avro provide production-grade resilience for evolving data schemas without downtime.

### 🎯 What is Schema Registry?

Schema Registry is a centralized service that:
- Stores and versions your data schemas
- Validates schema compatibility before registration
- Ensures producers and consumers can evolve independently
- Prevents breaking changes from entering your system

### 🔧 Components

When you run `docker-compose up`, the following Schema Registry components are started:

1. **Schema Registry** (port 8081): Central schema repository
2. **Avro Producer** (`workshop-kafka-train-avro-producer`): Demonstrates schema evolution
3. **Avro Consumer** (`workshop-kafka-train-avro-consumer`): Handles multiple schema versions

### 🚀 Running the Schema Registry Example

The Avro services are included in the main `docker-compose.yml`:

```bash
# Start everything including Schema Registry
docker-compose up -d

# Watch the Avro producer logs (schema evolution happens after 30 seconds)
docker-compose logs -f avro-producer

# Watch the Avro consumer logs (handles both V1 and V2 schemas)
docker-compose logs -f avro-consumer
```

Or run manually for development:

```bash
# Terminal 1: Start the Avro producer
cd workshop-kafka-train-avro-producer
mvn spring-boot:run

# Terminal 2: Start the Avro consumer
cd workshop-kafka-train-avro-consumer
mvn spring-boot:run
```

### 📊 What Happens?

1. **First 30 seconds**: Producer sends data using **V1 schema** (basic fields)
2. **After 30 seconds**: Producer automatically switches to **V2 schema** (adds timestamp + fuel level)
3. **Consumer**: Handles both versions seamlessly, no errors, no downtime!

### 🔍 Monitoring Schemas

Check registered schemas:
```bash
# List all schema subjects
curl http://localhost:8081/subjects

# Get schema versions for train positions
curl http://localhost:8081/subjects/train-locations-avro-value/versions

# View specific schema
curl http://localhost:8081/subjects/train-locations-avro-value/versions/1 | jq
```

You can also view schemas in Kafka UI at `http://localhost:8099` under the "Schema Registry" tab.

### 🧰 Schema Registry: Testing & Management (scripts)

There are two helper scripts included for convenience:

- `assets/schema-registry-demo-scripts/schema-registry-commands.ps1` (PowerShell)
- `assets/schema-registry-demo-scripts/schema-registry-commands.sh` (POSIX shell, requires `jq`)

They provide quick commands to list subjects, inspect versions, parse the latest Avro schema, check/set compatibility, test candidate schemas for compatibility, register a new schema version, get a schema by id, and (commented) delete a subject.

Quick PowerShell examples (Windows):

```powershell
# List subjects
curl.exe http://localhost:8081/subjects | ConvertFrom-Json

# Get latest train-locations schema (parsed)
($s = curl.exe http://localhost:8081/subjects/train-locations-avro-value/versions/latest | ConvertFrom-Json).schema | ConvertFrom-Json

# Test compatibility of a candidate schema (POST)
curl.exe -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" --data '{"schema":"<AVRO_JSON_AS_STRING>"}' http://localhost:8081/compatibility/subjects/train-locations-avro-value/versions/latest
```

Quick macOS / Linux examples:

```bash
# List subjects (requires jq)
curl -s http://localhost:8081/subjects | jq

# Get latest train-locations schema (parsed)
curl -s http://localhost:8081/subjects/train-locations-avro-value/versions/latest | jq '.schema | fromjson'

# Test compatibility of a candidate schema (POST)
curl -s -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" --data '{"schema":"<AVRO_JSON_AS_STRING>"}' http://localhost:8081/compatibility/subjects/train-locations-avro-value/versions/latest | jq
```

Notes:

- On Windows PowerShell, `curl` may be an alias for `Invoke-WebRequest`; use `curl.exe` to call the real curl binary.
- The shell script uses `jq` to pretty-print JSON; install `jq` if you don't already have it.


### 💡 Key Resilience Features Demonstrated

| Feature | How It Helps | Example in Workshop |
|---------|-------------|---------------------|
| **Schema Evolution** | Add new fields without breaking consumers | V2 adds `timestamp` and `fuelLevel` |
| **Backward Compatibility** | New consumers read old data | Consumer handles V1 messages with defaults |
| **Forward Compatibility** | Old consumers read new data | V1 consumer ignores extra V2 fields |
| **Type Safety** | Enums prevent invalid values | `AlertType` must be MAINTENANCE, LOW_FUEL, etc. |
| **Auto-validation** | Bad schemas rejected at registration | Try to register incompatible schema - it fails! |
| **Self-documentation** | Schema serves as contract | Each field has `doc` explaining its purpose |

### 🧪 Try These Experiments

**Experiment 1: Schema Evolution in Action**
1. Start producer and consumer
2. Watch logs for `[V1]` messages
3. After 30 seconds, see `🔄 SCHEMA EVOLUTION` message
4. Watch logs for `[V2]` messages with fuel data
5. Consumer handles both versions without restart!

**Experiment 2: View Schema Versions**
```bash
# See how many schema versions exist
curl http://localhost:8081/subjects/train-locations-avro-value/versions

# Get the latest schema
curl http://localhost:8081/subjects/train-locations-avro-value/versions/latest
```

**Experiment 3: Test Compatibility**
The producer automatically validates compatibility. Try modifying schemas in `avro-schemas/` and restarting - incompatible changes will be rejected!

### 📚 Schema Files

All Avro schemas are in the `avro-schemas/` directory:

- `train-position-v1.avsc` - Initial schema with basic fields
- `train-position-v2.avsc` - Evolved schema with timestamp and fuel level
- `train-alert.avsc` - Alert schema with enums for type safety

Each schema includes:
- Field documentation
- Type definitions
- Default values for backward compatibility

### 🎓 Learning Outcomes

After completing this section, you'll understand:

1. **Why Schema Registry is essential** for production Kafka systems
2. **How Avro enables schema evolution** without breaking changes
3. **Backward vs Forward compatibility** and when to use each
4. **Type safety benefits** of strongly-typed schemas vs plain JSON
5. **Zero-downtime deployments** with schema versioning
6. **Best practices** for evolving data contracts

### 📖 Detailed Documentation

For a comprehensive guide on resilience patterns, compatibility modes, and best practices, see:
**[SCHEMA_REGISTRY_RESILIENCE.md](SCHEMA_REGISTRY_RESILIENCE.md)**

This document includes:
- Detailed resilience explanations
- Schema evolution strategies
- Compatibility testing examples
- Production best practices
- Troubleshooting guide

---

## 🏗️ Workshop Architecture Notes

📌 **Recommended Conclusion for Production Systems**

Since you're preparing a **realistic workshop**, the **best solution** for production environments would be:

🔁 **Use Schema Registry with Avro** (demonstrated in Part 3) → This provides:
- Automatic schema validation and versioning
- Zero-downtime schema evolution
- Type safety and documentation
- Protection against breaking changes

Alternative approaches:
- 📦 **Shared library/JAR** → Put `TrainPosition` in a common module imported by both services
- 🗄️ **JSON Schema** → Similar to Avro but with JSON instead of binary format

For real-world applications, consider:
- 📦 Creating a shared Maven module with its own `pom.xml`
- 🏗️ Implementing a multi-module project structure
- 🗄️ **Using Schema Registry for better data contract management** (see Part 3 above)
- 🔄 Implementing proper versioning strategies for your shared models

## 🎯 Key Learning Outcomes

After completing this workshop, participants will understand:

1. **📚 Kafka Fundamentals:** Topics, partitions, producers, and consumers
2. **👥 Consumer Groups:** How multiple consumers can share workload automatically
3. **⚖️ Partition Rebalancing:** Kafka's automatic load balancing when consumers join/leave
4. **🌐 Language Interoperability:** Java and Python services working together through Kafka
5. **⚡ Stream Processing:** Real-time data transformation with Kafka Streams
6. **📊 Monitoring:** Using both command-line tools and web interfaces to monitor Kafka clusters
7. **📈 Scalability:** How to scale consumer applications horizontally
8. **🛡️ Schema Registry & Avro:** Production-grade data resilience and schema evolution

## 🌟 Summary

This workshop demonstrates a complete Kafka ecosystem:
- ✅ Multiple programming languages (Java & Python)
- ✅ Real-time stream processing
- ✅ WebSocket-based visualization
- ✅ Horizontal scalability with consumer groups
- ✅ **Schema Registry for production-ready data management**
- ✅ Docker-based deployment

**Next Steps:**
- Explore the Schema Registry examples in Part 3
- Experiment with different schema evolution strategies
- Try modifying the Avro schemas and see how Schema Registry validates them
- Read [SCHEMA_REGISTRY_RESILIENCE.md](SCHEMA_REGISTRY_RESILIENCE.md) for advanced patterns