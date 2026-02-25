package business.card.infrastructure.persistence.entity;

import business.card.domain.model.BusinessCardType;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "business_cards")
public class BusinessCardEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @Column(name = "uid", nullable = false)
    public String uid;

    @Column(name = "account_id", nullable = false)
    public UUID accountId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw", columnDefinition = "jsonb")
    public JsonNode raw;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 16)
    public BusinessCardType type = BusinessCardType.MAIN;

    @Column(length = 255, name = "avatar_url")
    public String avatarUrl;

    @Column(name = "soft_deleted_at")
    public OffsetDateTime softDeletedAt;

    @Column(name = "save_at", nullable = false, insertable = false, updatable = false)
    public OffsetDateTime saveAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    public OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    public OffsetDateTime updatedAt;
}