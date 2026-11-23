package com.patchnotes.ingestion_service.dto;

import java.util.UUID;

public record IngestEventResponse(
        UUID eventId,
        String idempotencyKey,
        String status,
        String message
) {

}
