package com.patchnotes.ingestion_service.repository;

import com.patchnotes.ingestion_service.entity.IncomingEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IncomingEventRepository extends JpaRepository<IncomingEvent, UUID>
{
    //agr Redis miss ho jaye toh DB me check krne k liye
    Optional<IncomingEvent> findByIdempotencyKey(String idempotencyKey);
}
