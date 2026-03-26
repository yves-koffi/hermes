package account.infrastructure.persistence.entity;

import account.domain.model.TokenType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "hash_tokens")
public class HashTokenEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @Column(name = "hash_token", nullable = false, unique = true, length = 255)
    public String hashToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", length = 16)
    public TokenType tokenType = TokenType.EMAIL_VERIFICATION_CODE;

    @Column(name = "account_id", nullable = false)
    public UUID accountId;

    @Column(name = "expiry_date", nullable = false)
    public OffsetDateTime expiryDate;

    @Column(name = "ip_address", length = 32)
    public String ipAddress;

    @Column(name = "revoked_at")
    public OffsetDateTime revokedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    public OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    public OffsetDateTime updatedAt;
}
