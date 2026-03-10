package account.infrastructure.api.dto;

public record UpdateAccountRequest(
        String name,
        String prefix,
        String number
) {
}
