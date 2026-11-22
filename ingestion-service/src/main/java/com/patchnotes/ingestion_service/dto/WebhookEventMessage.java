package com.patchnotes.ingestion_service.dto;

import java.time.Instant;
import java.util.UUID;

public record WebhookEventMessage(
        UUID eventId,
        String idempotencykey,
        String eventType,
        String payload,
        int attempCount,
        Instant createdAt
) {

}
