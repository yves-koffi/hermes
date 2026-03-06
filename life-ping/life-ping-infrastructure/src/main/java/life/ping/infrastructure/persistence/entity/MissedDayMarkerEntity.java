package life.ping.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "missed_day_markers",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_missed_day_marker_account_local_date", columnNames = {"account_id", "local_date"})
        },
        indexes = {
                @Index(name = "idx_missed_day_marker_account_id", columnList = "account_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissedDayMarkerEntity {
    @Id
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "local_date", nullable = false)
    private LocalDate localDate;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
