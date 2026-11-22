package com.patchnotes.endpoint_service.dto;

import com.patchnotes.endpoint_service.entity.EndpointStatus;

import java.util.Set;
import java.util.UUID;
import java.time.Instant;

public record EndpointResponse (
        UUID id,
        String clientName,
        String targetUrl,
        String secretKey,
        EndpointStatus status,
        Set<String> subscribedEvents,
        Instant createdAt
){
}
