package com.sputniks.workshop_kafka_train;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
class DashboardListenerTest {

    @Container
    static final KafkaContainer kafka =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.3.2"));

    @DynamicPropertySource
    static void overrideKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @SpyBean
    private DashboardListener dashboardListener;

    @Test
    void shouldReceiveMessageAndProcessIt() {
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        KafkaTemplate<String, TrainPosition> template = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerProps));

        // Create the test data
        TrainPosition testPosition = new TrainPosition("TEST-002", 51.0, 11.0, 120);

        // --- Act: Send a message using our test template ---
        template.send("train-locations", testPosition.trainId(), testPosition);

        // --- Assert: Verify that our listener's 'listen' method was called ---
        ArgumentCaptor<TrainPosition> captor = ArgumentCaptor.forClass(TrainPosition.class);
        verify(dashboardListener, timeout(5000)).listen(captor.capture(), anyString(), anyInt());
        
        TrainPosition receivedPosition = captor.getValue();
        assertEquals("TEST-002", receivedPosition.trainId());
        assertEquals(120, receivedPosition.speedKph());
    }
}