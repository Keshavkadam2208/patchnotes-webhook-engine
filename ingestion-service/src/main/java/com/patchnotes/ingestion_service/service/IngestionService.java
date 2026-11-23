package com.patchnotes.ingestion_service.service;

import com.patchnotes.ingestion_service.config.RabbitMQConfig;
import com.patchnotes.ingestion_service.dto.IngestEventResponse;
import com.patchnotes.ingestion_service.dto.WebhookEventMessage;
import com.patchnotes.ingestion_service.entity.EventStatus;
import com.patchnotes.ingestion_service.entity.IncomingEvent;
import com.patchnotes.ingestion_service.repository.IncomingEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionService {

    private final IncomingEventRepository eventRepository;
    private final StringRedisTemplate redisTemplate;
    private final RabbitTemplate rabbitTemplate;

    private static final String REDIS_KEY_PREFIX = "idempotency:";
    private static final Duration IDEMPOTENCY_TTL = Duration.ofHours(24);

    @Transactional
    public IngestEventResponse ingestEvent(String idempotencyKey, String eventType, String rawPayload) {
        String redisKey = REDIS_KEY_PREFIX + idempotencyKey;

        // 1. Redis Atomic Check (SETNX with 24h TTL)
        Boolean isNewRequest = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, "PROCESSING", IDEMPOTENCY_TTL);

        if (Boolean.FALSE.equals(isNewRequest)) {
            log.warn("Duplicate event blocked by Redis for key: {}", idempotencyKey);
            return new IngestEventResponse(
                    null,
                    idempotencyKey,
                    "DUPLICATE_IGNORED",
                    "Event already accepted and processed"
            );
        }

        try {
            // 2. Database me save karna (Source of Truth)
            IncomingEvent event = IncomingEvent.builder()
                    .idempotencyKey(idempotencyKey)
                    .eventType(eventType)
                    .payload(rawPayload)
                    .status(EventStatus.RECEIVED)
                    .build();

            IncomingEvent savedEvent = eventRepository.save(event);

            // 3. RabbitMQ me message publish karna
            WebhookEventMessage message = new WebhookEventMessage(
                    savedEvent.getId(),
                    savedEvent.getIdempotencyKey(),
                    savedEvent.getEventType(),
                    savedEvent.getPayload(),
                    1, // First attempt
                    Instant.now()
            );

            String routingKey = "webhook.event." + eventType;
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, routingKey, message);

            // 4. Status update to PUBLISHED
            savedEvent.setStatus(EventStatus.PUBLISHED);
            eventRepository.save(savedEvent);

            log.info("Event {} published to RabbitMQ with routingKey {}", savedEvent.getId(), routingKey);

            return new IngestEventResponse(
                    savedEvent.getId(),
                    savedEvent.getIdempotencyKey(),
                    "ACCEPTED",
                    "Event queued for webhook delivery"
            );

        } catch (DataIntegrityViolationException ex) {
            // Layer 2 Defense: Agar Redis miss hua par DB unique constraint ne pakad liya
            log.warn("Duplicate event blocked by DB constraint for key: {}", idempotencyKey);
            return new IngestEventResponse(
                    null,
                    idempotencyKey,
                    "DUPLICATE_IGNORED",
                    "Event already exists in database"
            );
        } catch (Exception ex) {
            // Agar DB ya RabbitMQ fail hua, to Redis key delete karo taaki client retry kar sake!
            redisTemplate.delete(redisKey);
            throw ex;
        }
    }
}