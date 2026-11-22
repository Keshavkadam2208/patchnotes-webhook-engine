package com.patchnotes.endpoint_service.service;

import com.patchnotes.endpoint_service.dto.CreateEndpointRequest;
import com.patchnotes.endpoint_service.dto.EndpointResponse;
import com.patchnotes.endpoint_service.entity.EndpointStatus;
import com.patchnotes.endpoint_service.entity.WebhookEndpoint;
import com.patchnotes.endpoint_service.repository.WebhookEndpointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EndpointService {

    private final WebhookEndpointRepository repository;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Transactional
    public EndpointResponse registerEndpoint(CreateEndpointRequest request) {
        WebhookEndpoint endpoint = WebhookEndpoint.builder()
                .clientName(request.clientName())
                .targetUrl(request.targetUrl())
                .secretKey(generateSecretKey())
                .status(EndpointStatus.ACTIVE)
                .subscribedEvents(request.subscribedEvents())
                .build();

        WebhookEndpoint saved = repository.save(endpoint);
        return mapToResponse(saved);
    }

    @Transactional
    public EndpointResponse updateStatus(UUID id, EndpointStatus newStatus) {
        WebhookEndpoint endpoint = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Endpoint not found with id: " + id));
        endpoint.setStatus(newStatus);
        return mapToResponse(repository.save(endpoint));
    }

    @Transactional(readOnly = true)
    public List<EndpointResponse> getActiveSubscribers(String eventType) {
        return repository.findActiveEndpointsByEventType(eventType, EndpointStatus.ACTIVE)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EndpointResponse getEndpointById(UUID id) {
        return repository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Endpoint not found with id: " + id));
    }

    // Production-grade 256-bit (32 bytes) secret key generator with prefix like Stripe ("whsec_...")
    private String generateSecretKey() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return "whsec_" + HexFormat.of().formatHex(bytes);
    }

    private EndpointResponse mapToResponse(WebhookEndpoint e) {
        return new EndpointResponse(
                e.getId(),
                e.getClientName(),
                e.getTargetUrl(),
                e.getSecretKey(),
                e.getStatus(),
                e.getSubscribedEvents(),
                e.getCreatedAt()
        );
    }
}