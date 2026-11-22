package com.patchnotes.endpoint_service.controller;

import com.patchnotes.endpoint_service.dto.CreateEndpointRequest;
import com.patchnotes.endpoint_service.dto.EndpointResponse;
import com.patchnotes.endpoint_service.entity.EndpointStatus;
import com.patchnotes.endpoint_service.service.EndpointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/endpoints")
@RequiredArgsConstructor
public class EndpointController {

    private final EndpointService endpointService;

    @PostMapping
    public ResponseEntity<EndpointResponse> createEndpoint(@Valid @RequestBody CreateEndpointRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(endpointService.registerEndpoint(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EndpointResponse> getEndpoint(@PathVariable UUID id) {
        return ResponseEntity.ok(endpointService.getEndpointById(id));
    }

    @PatchMapping("/{id}/pause")
    public ResponseEntity<EndpointResponse> pauseEndpoint(@PathVariable UUID id) {
        return ResponseEntity.ok(endpointService.updateStatus(id, EndpointStatus.PAUSED));
    }

    @PatchMapping("/{id}/resume")
    public ResponseEntity<EndpointResponse> resumeEndpoint(@PathVariable UUID id) {
        return ResponseEntity.ok(endpointService.updateStatus(id, EndpointStatus.ACTIVE));
    }

    // Internal API jo Delivery Worker call karega
    @GetMapping("/subscribers")
    public ResponseEntity<List<EndpointResponse>> getActiveSubscribers(@RequestParam String eventType) {
        return ResponseEntity.ok(endpointService.getActiveSubscribers(eventType));
    }
}
