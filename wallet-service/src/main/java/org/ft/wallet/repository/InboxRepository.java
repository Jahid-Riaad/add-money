package org.ft.wallet.repository;

import org.ft.wallet.entity.InboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface InboxRepository extends JpaRepository<InboxEvent, UUID> {
}