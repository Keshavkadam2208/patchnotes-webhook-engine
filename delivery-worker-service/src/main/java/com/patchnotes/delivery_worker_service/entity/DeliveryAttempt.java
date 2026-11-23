package com.patchnotes.delivery_worker_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "delivery_attempts", indexes = {
        @Index(name = "idx_event_id", columnList = "eventId"),
        @Index(name = "idx_endpoint_id", columnList = "endpointId"),
        @Index(name = "idx_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class DeliveryAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID eventId;

    @Column(nullable = false)
    private UUID endpointId;

    @Column(nullable = false)
    private String eventType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Column(nullable = false)
    private int attemptNumber;

    private Integer httpStatus; //200, 500, ya 0(agr network timeout/connection refused hua to)

    @Column(columnDefinition = "TEXT")
    private String responseBody; //servers reply or error msg

    private long latencyMs;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    @CreationTimestamp
    private Instant attemptedAt;
}
