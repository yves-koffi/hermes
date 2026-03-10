package account.infrastructure.persistence.entity;

import account.domain.model.Provider;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "accounts")
public class AccountEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @Column(nullable = false, length = 255)
    public String name;

    @Column(nullable = false, unique = true, length = 120)
    public String email;

    @Column(length = 208, name = "avatar_url")
    public String avatarUrl;

    @Column(length = 255, name = "provider_id")
    public String providerId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "prefix", column = @Column(name = "prefix", length = 8)),
            @AttributeOverride(name = "number", column = @Column(name = "number", length = 16))
    })
    public PhoneNumberEntity phoneNumberEntity;

    @Column(length = 255, name = "password")
    public String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 24)
    public Provider provider = Provider.GOOGLE;

    @Column(name = "activated_at")
    public OffsetDateTime activatedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    public OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    public OffsetDateTime updatedAt;

}
