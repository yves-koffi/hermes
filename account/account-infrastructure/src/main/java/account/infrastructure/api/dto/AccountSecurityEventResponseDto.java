package account.infrastructure.api.dto;

public record AccountSecurityEventResponseDto(
        String id,
        String eventType,
        String detail,
        String ipAddress,
        String occurredAt
) {
}
