package com.patchnotes.endpoint_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import javax.annotation.processing.Generated;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "webhook_endpoints")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookEndpoint {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String clientName;

    @Column(nullable = false)
    private String targetUrl;

    @Column(nullable = false)
    private String secretKey; //for HMAC signing

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EndpointStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "endpoint_subscriptions", joinColumns = @JoinColumn(name = "endpoint_id"))
    @Column(name = "event_type")
    @Builder.Default
    private Set<String> subscribedEvents = new HashSet<>();

    @CreationTimestamp
    private Instant createdAt;

}
