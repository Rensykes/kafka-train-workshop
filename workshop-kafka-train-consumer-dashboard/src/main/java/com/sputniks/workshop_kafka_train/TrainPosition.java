// ! This class is used across multiple services in the workshop.
// ! In a real-world scenario, it should be placed in a shared module or library 
// ! to avoid duplication and ensure consistency.

package com.sputniks.workshop_kafka_train;

public record TrainPosition(
    String trainId,
    double latitude,
    double longitude,
    int speedKph
) {}