package account.infrastructure.api.mapper;

import account.application.command.LoginCommand;
import account.application.command.RegisterCommand;
import account.application.command.SocialCredentialCommand;
import account.application.command.UpdateAccountCommand;
import account.application.result.AuthResult;
import account.application.result.AccountDetails;
import account.application.result.AccountSummary;
import account.domain.model.PhoneNumber;
import account.infrastructure.api.dto.AccountResponse;
import account.infrastructure.api.dto.AuthResponse;
import account.infrastructure.api.dto.LoginRequest;
import account.infrastructure.api.dto.RegisterRequest;
import account.infrastructure.api.dto.SocialAuthRequest;
import account.infrastructure.api.dto.UpdateAccountRequest;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.OffsetDateTime;

@ApplicationScoped
public class AccountRequestMapper {

    public LoginCommand toCommand(LoginRequest request) {
        return new LoginCommand(request.email(), request.password());
    }

    public RegisterCommand toCommand(RegisterRequest request) {
        return new RegisterCommand(
                request.name(),
                request.email(),
                request.password(),
                toPhoneNumber(request.prefix(), request.number())
        );
    }

    public UpdateAccountCommand toCommand(UpdateAccountRequest request) {
        return new UpdateAccountCommand(
                request.name(),
                toPhoneNumber(request.prefix(), request.number())
        );
    }

    public SocialCredentialCommand toCommand(SocialAuthRequest request) {
        return new SocialCredentialCommand(
                request.provider(),
                request.providerId(),
                request.displayName(),
                request.email(),
                request.photoUrl()
        );
    }

    public AuthResponse toResponse(AuthResult result) {
        return new AuthResponse(
                result.accessToken(),
                result.refreshToken(),
                result.tokenType(),
                result.accessExpiresIn(),
                result.refreshExpiresIn()
        );
    }

    public AccountResponse toResponse(AccountSummary summary) {
        return new AccountResponse(
                summary.name(),
                null,
                null,
                summary.provider() == null ? null : summary.provider().name(),
                null,
                summary.activatedAt() != null,
                null
        );
    }

    public AccountResponse toResponse(AccountDetails details) {
        return new AccountResponse(
                details.name(),
                details.email(),
                null,
                details.provider() == null ? null : details.provider().name(),
                details.avatarUrl(),
                details.activatedAt() != null,
                toEpochSeconds(details.createdAt())
        );
    }

    private PhoneNumber toPhoneNumber(String prefix, String number) {
        if (prefix == null && number == null) {
            return null;
        }
        return new PhoneNumber(prefix, number);
    }

    private Integer toEpochSeconds(OffsetDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return (int) dateTime.toEpochSecond();
    }
}
