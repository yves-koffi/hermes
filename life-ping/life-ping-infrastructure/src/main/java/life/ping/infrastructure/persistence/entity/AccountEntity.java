package life.ping.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(
        name = "app_accounts",
        indexes = {
                @Index(name = "idx_app_user_platform", columnList = "device_platform"),
                @Index(name = "idx_app_user_updated_at", columnList = "updated_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "app_uuid", nullable = false, unique = true)
    private String appUuid;

    @Column(name = "device_unique_id", nullable = false)
    private String deviceUniqueId;

    @Column(name = "device_model", nullable = false)
    private String deviceModel;

    @Column(name = "device_platform", nullable = false)
    private String devicePlatform;

    @Column(name = "timezone", nullable = false)
    private String timezone;

    @Column(name = "callback_time")
    private LocalTime callbackTime;

    @Column(name = "check_in_frequency", nullable = false)
    private Integer checkInFrequency;

    @Column(name = "threshold_period", nullable = false)
    private Integer thresholdPeriod;

    @Column(name = "last_checkin_at")
    private LocalDateTime lastCheckinAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
