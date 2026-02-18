package org.ft.wallet.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="wallet_inbox_event")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InboxEvent {

    @Id
    private UUID eventId;

    private LocalDateTime processedAt;
}
