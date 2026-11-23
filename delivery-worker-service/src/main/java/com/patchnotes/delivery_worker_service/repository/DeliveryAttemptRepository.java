package com.patchnotes.delivery_worker_service.repository;

import com.patchnotes.delivery_worker_service.entity.DeliveryAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.List;

public interface DeliveryAttemptRepository extends JpaRepository<DeliveryAttempt,UUID> {
    List<DeliveryAttempt> findByEventIdOrderByAttemptNumberAsc(UUID eventId);
}
