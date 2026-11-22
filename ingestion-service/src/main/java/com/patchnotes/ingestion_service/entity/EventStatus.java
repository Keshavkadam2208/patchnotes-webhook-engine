package com.patchnotes.ingestion_service.entity;

public enum EventStatus {
    RECEIVED,   // Database me save ho gaya, abhi queue me jana baaki hai
    PUBLISHED   // RabbitMQ me successfully bhej diya gaya
}
