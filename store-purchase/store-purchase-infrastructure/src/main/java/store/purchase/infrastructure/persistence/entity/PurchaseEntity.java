package store.purchase.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import store.purchase.domain.IapSource;
import store.purchase.domain.PurchaseStatus;
import store.purchase.domain.PurchaseType;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "purchases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Convert(converter = IapSourceConverter.class)
    @Column(name = "iap_source", nullable = false)
    private IapSource iapSource;

    @Column(name = "order_id", nullable = false, unique = true)
    private String orderId;

    @Column(name = "external_product_id", nullable = false)
    private String externalProductId;

    @Column(name = "purchase_date", nullable = false)
    private String purchaseDate;

    @Column(name = "expiry_date")
    private String expiryDate;

    @Convert(converter = PurchaseTypeConverter.class)
    @Column(name = "purchase_type", nullable = false)
    private PurchaseType purchaseType;

    @Convert(converter = PurchaseStatusConverter.class)
    @Column(name = "status", nullable = false)
    private PurchaseStatus status;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
