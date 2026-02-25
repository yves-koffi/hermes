package business.card.infrastructure.persistence.entity;

import business.card.domain.model.Provider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "accounts")
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public UUID id;

    @Column(nullable = false, length = 255)
    public String name;

    @Column(nullable = false, unique = true, length = 120)
    public String email;

    @Column(length = 255,name = "avatar_url")
    public String avatarUrl;

    @Column(length = 24,name = "google_id")
    public String googleId;

    @Column(length = 24,name = "apple_id")
    public String appleId;

    @Enumerated(EnumType.STRING)
    public Provider provider = Provider.GOOGLE;

    @Column(name = "activated_at")
    public OffsetDateTime activatedAt;

    @Column(name = "created_at",insertable = false, updatable = false)
    public OffsetDateTime createdAt;

    @Column(name = "updated_at",insertable = false)
    public OffsetDateTime updatedAt;


}