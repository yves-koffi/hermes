package account.infrastructure.persistence.entity;

import account.domain.model.AccountSecurityEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "account_security_events")
public class AccountSecurityEventEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @Column(name = "account_id", nullable = false)
    public UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 48)
    public AccountSecurityEventType eventType;

    @Column(name = "detail", length = 255)
    public String detail;

    @Column(name = "ip_address", length = 64)
    public String ipAddress;

    @Column(name = "occurred_at", nullable = false)
    public OffsetDateTime occurredAt;
}
