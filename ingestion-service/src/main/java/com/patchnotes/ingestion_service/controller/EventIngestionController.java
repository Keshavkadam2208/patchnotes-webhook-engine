package com.patchnotes.ingestion_service.controller;

import com.patchnotes.ingestion_service.dto.IngestEventResponse;
import com.patchnotes.ingestion_service.service.IngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventIngestionController {
    private final IngestionService ingestionService;

    @PostMapping
    public ResponseEntity<IngestEventResponse> publishEvent(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestHeader("X-Event-Type") String eventType,
            @RequestBody String rawPayload
    )
    {
        if(idempotencyKey == null || idempotencyKey.isBlank())
        {
            return ResponseEntity.badRequest().body(
                    new IngestEventResponse(null, null, "INVALID_REQUEST", "Idempotency-Key header is required")
            );
        }
        if(eventType ==  null || eventType.isBlank())
        {
            return ResponseEntity.badRequest().body(
                    new IngestEventResponse(null, idempotencyKey, "INVALID_REQUEST", "x-Event-Type header is required")
            );
        }
        IngestEventResponse response = ingestionService.ingestEvent(idempotencyKey, eventType, rawPayload);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
