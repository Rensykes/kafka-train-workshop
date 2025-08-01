# Kafka for Train Transportation: A Multi-Language Workshop

Welcome to the Connected Train Platform workshop!

In this workshop, you will build a multi-service, multi-language event-driven system using Apache Kafka. We will simulate a fleet of trains sending real-time location data, process this data to generate insights, and visualize it on a live dashboard.

This project demonstrates core Kafka concepts like:
- Producers and Consumers
- Topics, Partitions, and Consumer Groups
- Language Interoperability (Java & Python)
- Stateful Stream Processing with Kafka Streams
- Real-time data visualization with WebSockets

**Note on Architecture:** For the sake of simplicity in this workshop, we used the same package structure across all Java applications to make the serialized objects (like `TrainLocation`) available across all services. In a real-world scenario, you should handle shared data models differently by:
- Creating a separate shared library/JAR for common data models
- Using schema registries (like Confluent Schema Registry) with Avro or JSON Schema
- Implementing proper versioning strategies for your data contracts
- Following microservice principles where each service owns its data model

## System Architecture

Our system consists of several independent microservices that communicate through Kafka:

```
┌─────────────────────────┐    ┌─────────────────────┐    ┌─────────────────────────┐
│ workshop-kafka-train    │    │                     │    │    train-locations      │
│ (TrainLocationSimulator)│───►│       KAFKA         │───►│       (Topic)           │
│    (Java Producer)      │    │                     │    └─────────────┬───────────┘
└─────────────────────────┘    └─────────────────────┘                  │
                                                                         │ (Consumes)
                                                                         │
                               ┌─────────────────────────────────────────┼─────────────┐
                               │                                         │             │
                               │                                         ▼             │
┌──────────────────────────────┴─────────────────────────┐    ┌─────────────────────────┐
│        kafka-workshop-train-python                     │    │ workshop-kafka-train-   │
│         (Maintenance Alerter)                          │    │    streams-analysis     │
└────────────────────────────────────────────────────────┘    │    (Java Streams)      │
                                                               └─────────────┬───────────┘
                                                                             │ (Produces)
                                                                             │
┌─────────────────────┐    ┌─────────────────────────┐                     ▼
│                     │    │  train-speed-averages   │◄────────────────────┘
│       KAFKA         │───►│       (Topic)           │
│                     │    └─────────────┬───────────┘
└─────────────────────┘                  │ (Consumes)
                                         │
┌────────────────────────────────────────┼─────────────────────────────────────┐
│              YOUR WEB BROWSER          │                                     │
│      (http://localhost:8085/index.html)│◄──────(WebSocket)──────────────────┐│
└────────────────────────────────────────┼─────────────────────────────────────┘│
                                         │                                      │
                                         └──────────────────────────────────────┼┘
                                                                                │
                                         ┌─────────────────────────────────────┼┐
                                         │   workshop-kafka-train-websocket    ││
                                         │   (Java Consumer + Web Server)      ││
                                         └─────────────────────────────────────┼┘
                                                                               │
                                         ┌─────────────────────────────────────┘
```

## Prerequisites

Before you begin, please ensure you have the following software installed on your system:

- **Docker & Docker Compose:** To run Kafka and Zookeeper locally.
- **Java JDK 17+:** All Java services are built with Java 17.
- **Apache Maven:** To build and run the Java projects.
- **Python 3.8+ & pip:** For the Python maintenance service.
- **A Git client:** To clone the repository.

---

## Part 1: Setting Up The Environment

This part is the foundation for everything. We will start the Kafka infrastructure and create the "channels" (topics) for our services to communicate.

### Step 1. Start Kafka and Zookeeper

Navigate to the root directory of the workshop (where `docker-compose.yml` is located) and run the following command in your terminal:

```bash
docker-compose up -d
```

This command will download the necessary images and start two containers in the background: zookeeper and kafka. You can check that they are running with `docker ps`.

### Step 2. Create the Kafka Topics

Our services need two topics to communicate:
- `train-locations`: For raw position data from the trains.
- `train-speed-averages`: For the processed average speed results.

Execute the following commands from your terminal to create them. These commands run a tool inside the kafka Docker container.

```bash
# Create the topic for raw train locations with 3 partitions
docker exec kafka kafka-topics --create \
  --topic train-locations \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1

# Create the topic for our analysis results
docker exec kafka kafka-topics --create \
  --topic train-speed-averages \
  --bootstrap-server localhost:9092 \
  --partitions 1 \
  --replication-factor 1
```

Your Kafka environment is now ready!

## Part 2: Running the Microservices

Now we will start each microservice one by one. It is recommended to use a separate terminal window for each service so you can see its logs.

### Service 1: The Producer (workshop-kafka-train)

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

You should see log messages indicating that train positions are being sent, like `Sent position update for train T-81A....`

### Service 2: The Stream Processor (workshop-kafka-train-streams-analysis)

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

This service will run quietly in the background. It doesn't produce much console output unless there's an error. Its job is to continuously transform data between the two topics.

### Service 3: The WebSocket Dashboard (workshop-kafka-train-websocket)

This is the DashboardWebApp. This service has two roles:
- It acts as a Kafka consumer, listening to the `train-speed-averages` topic.
- It acts as a WebSocket server, pushing any new data it receives from Kafka directly to connected web browsers.

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

After a few seconds (waiting for the first 10-second window to complete), you should see train cards appear and their average speed updating in real-time on the webpage!

### Service 4: The Python Alerter (kafka-workshop-train-python-service)

This Python script demonstrates language interoperability and the power of consumer groups. It listens to the same `train-locations` topic as our Streams application, but for a different purpose: to alert if a train is slowing down (speed is lesser than 60). It uses a different group-id, so it gets a full, independent copy of all messages.

**To Run:**

1. Open a new terminal.
2. Navigate to the project directory:
   ```bash
   cd kafka-workshop-train-python-service
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

## Summary: Running The Full System

You should now have 4 terminal windows running your microservices, plus your web browser showing the live dashboard. This completes the full, end-to-end data pipeline.

## Shutting Down

- To stop each microservice, go to its terminal window and press `Ctrl + C`.
- To stop the Kafka and Zookeeper containers, run the following command from the root directory:
  ```bash
  docker-compose down
  ```

## Troubleshooting

**Error: ListenerExecutionFailedException... Cannot convert from [java.lang.String]**

This is a classic configuration issue. It means your consumer application's `.yml` file is not correctly configured to use the JsonDeserializer. Ensure the configuration is under `spring.kafka.consumer` and not `spring.kafka.streams`, and that the `value-deserializer` and `spring.json.*` properties are correctly set.

**Error: Connection to node -1 (localhost/127.0.0.1:9092) could not be established. Broker may not be available.**

This means your application cannot connect to Kafka. Check that your Docker containers are running with `docker ps`. If not, run `docker-compose up -d`.

**Error: Port is already in use**

One of the Java services is trying to start on a port that is already occupied. Check the `server.port` property in the `src/main/resources/application.yml` file of the failing service and change it to a free port. The `workshop-kafka-train-websocket` service uses port 8085 by default.

**The Python script gives an error.**

- Make sure you have installed the dependency with `pip install kafka-python`.
- Verify Kafka is running and accessible at `localhost:9092`.

**PowerShell execution policy error on Windows:**

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

---

## Workshop Architecture Notes

📌 **Recommended Conclusion for Production Systems**

Since you're preparing a **realistic workshop**, the **best solution** for production environments would be:

🔁 **Align packages between producer and consumer** → Put `TrainPosition` in a common module imported by both services.

For real-world applications, consider:
- Creating a shared Maven module with its own `pom.xml`
- Implementing a multi-module project structure
- Using schema registries for better data contract management
- Implementing proper versioning strategies for your shared models