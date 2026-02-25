package life.ping.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "user_devices",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_devices_user_id_fcm_token", columnNames = {"user_id", "fcm_token"})
        },
        indexes = {
                @Index(name = "idx_user_device_user", columnList = "user_id"),
                @Index(name = "idx_user_device_last_seen", columnList = "last_seen_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDeviceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "platform", nullable = false)
    private String platform;

    @Column(name = "fcm_token", nullable = false)
    private String fcmToken;

    @Column(name = "last_seen_at", nullable = false)
    private LocalDateTime lastSeenAt;
}
