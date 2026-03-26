package account.infrastructure.api.dto;

public record ChangeEmailResponseDto(
        String accountId,
        String email,
        boolean verificationRequired,
        String nextStep
) {
}
