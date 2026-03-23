package account.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "auth_sessions")
public class AuthSessionEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @Column(name = "account_id", nullable = false)
    public UUID accountId;

    @Column(name = "refresh_token_hash", nullable = false, unique = true, length = 255)
    public String refreshTokenHash;

    @Column(name = "expiry_date", nullable = false)
    public OffsetDateTime expiryDate;

    @Column(name = "ip_address", length = 64)
    public String ipAddress;

    @Column(name = "user_agent", length = 512)
    public String userAgent;

    @Column(name = "rotated_from_session_id")
    public UUID rotatedFromSessionId;

    @Column(name = "last_used_at")
    public OffsetDateTime lastUsedAt;

    @Column(name = "revoked_at")
    public OffsetDateTime revokedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    public OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    public OffsetDateTime updatedAt;
}
