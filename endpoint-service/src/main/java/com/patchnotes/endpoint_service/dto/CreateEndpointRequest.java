package com.patchnotes.endpoint_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.hibernate.validator.constraints.URL;

import java.util.Set;

public record CreateEndpointRequest(
        @NotBlank(message = "Client name is required")
        String clientName,

        @NotBlank(message = "Target URL is required")
        @URL(message = "Must be a valid HTTP/HTTPS URL")
        String targetUrl,

        @NotEmpty(message = "At least one event type subscription is required")
        Set<String> subscribedEvents
) {}