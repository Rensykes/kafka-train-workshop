package com.sputniks.workshop_kafka_train;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@SpringBootApplication
@EnableKafkaStreams
public class WorkshopKafkaTrainStreamsAnalysisApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkshopKafkaTrainStreamsAnalysisApplication.class, args);
	}

}
