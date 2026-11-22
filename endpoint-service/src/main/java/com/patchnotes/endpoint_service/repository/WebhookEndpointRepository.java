package com.patchnotes.endpoint_service.repository;

import com.patchnotes.endpoint_service.entity.EndpointStatus;
import com.patchnotes.endpoint_service.entity.WebhookEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface WebhookEndpointRepository extends JpaRepository<WebhookEndpoint, UUID> {

    // Ye query Delivery Worker use karega: "Is eventType ko sunne wale saare ACTIVE endpoints lao"
    @Query("SELECT e FROM WebhookEndpoint e JOIN e.subscribedEvents s WHERE s = :eventType AND e.status = :status")
    List<WebhookEndpoint> findActiveEndpointsByEventType(
            @Param("eventType") String eventType,
            @Param("status") EndpointStatus status
    );
}