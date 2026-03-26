package account.infrastructure.api.mapper;

import account.application.command.ChangePasswordCommand;
import account.application.command.ChangeEmailCommand;
import account.application.command.CheckTokenCommand;
import account.application.command.ForgetPasswordCommand;
import account.application.command.LoginCommand;
import account.application.command.LogoutCommand;
import account.application.command.RefreshSessionCommand;
import account.application.command.RegisterCommand;
import account.application.command.ResetPasswordCommand;
import account.application.command.SendVerifyCodeCommand;
import account.application.command.SocialCredentialCommand;
import account.application.command.UpdateAccountCommand;
import account.application.command.VerifyAccountCommand;
import account.application.result.AuthResult;
import account.application.result.ChangeEmailResult;
import account.application.result.AccountSecurityEventResult;
import account.application.result.AccountDetails;
import account.application.result.AccountSummary;
import account.domain.model.PhoneNumber;
import account.infrastructure.api.dto.ChangePasswordRequestDto;
import account.infrastructure.api.dto.ChangeEmailRequestDto;
import account.infrastructure.api.dto.ChangeEmailResponseDto;
import account.infrastructure.api.dto.CheckTokenRequestDto;
import account.infrastructure.api.dto.AccountSecurityEventResponseDto;
import account.infrastructure.api.dto.ForgotPasswordRequestDto;
import account.infrastructure.api.dto.AccountResponseDto;
import account.infrastructure.api.dto.AuthResponseDto;
import account.infrastructure.api.dto.LoginRequestDto;
import account.infrastructure.api.dto.LogoutRequestDto;
import account.infrastructure.api.dto.RefreshTokenRequestDto;
import account.infrastructure.api.dto.RegisterRequestDto;
import account.infrastructure.api.dto.ResendVerificationRequestDto;
import account.infrastructure.api.dto.ResetPasswordRequestDto;
import account.infrastructure.api.dto.SocialAuthRequestDto;
import account.infrastructure.api.dto.UpdateAccountRequestDto;
import account.infrastructure.api.dto.VerifyEmailRequestDto;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.OffsetDateTime;

@ApplicationScoped
public class AccountRequestMapper {

    public LoginCommand toCommand(LoginRequestDto request) {
        return new LoginCommand(request.email(), request.password());
    }

    public RefreshSessionCommand toCommand(RefreshTokenRequestDto request) {
        return new RefreshSessionCommand(request.refreshToken());
    }

    public LogoutCommand toCommand(LogoutRequestDto request) {
        return new LogoutCommand(request.refreshToken());
    }

    public RegisterCommand toCommand(RegisterRequestDto request) {
        return new RegisterCommand(
                request.name(),
                request.email(),
                request.password(),
                toPhoneNumber(request.prefix(), request.number()),
                request.requiredVerifyEmail()
        );
    }

    public UpdateAccountCommand toCommand(UpdateAccountRequestDto request) {
        return new UpdateAccountCommand(
                request.name(),
                toPhoneNumber(request.prefix(), request.number()),
                request.avatarUrl()
        );
    }

    public ChangeEmailCommand toCommand(ChangeEmailRequestDto request) {
        return new ChangeEmailCommand(request.email());
    }

    public ChangePasswordCommand toCommand(ChangePasswordRequestDto request) {
        return new ChangePasswordCommand(
                request.currentPassword(),
                request.newPassword(),
                request.confirmNewPassword()
        );
    }

    public ForgetPasswordCommand toCommand(ForgotPasswordRequestDto request) {
        return new ForgetPasswordCommand(request.email(), request.type());
    }

    public ResetPasswordCommand toCommand(ResetPasswordRequestDto request) {
        return new ResetPasswordCommand(
                request.token(),
                request.newPassword(),
                request.confirmNewPassword()
        );
    }

    public VerifyAccountCommand toCommand(VerifyEmailRequestDto request) {
        return new VerifyAccountCommand(request.token(), request.type());
    }

    public SendVerifyCodeCommand toCommand(ResendVerificationRequestDto request) {
        return new SendVerifyCodeCommand(request.email(), request.type());
    }

    public CheckTokenCommand toCommand(CheckTokenRequestDto request) {
        return new CheckTokenCommand(request.token(), request.type());
    }

    public SocialCredentialCommand toCommand(SocialAuthRequestDto request) {
        return new SocialCredentialCommand(
                request.provider(),
                request.providerId(),
                request.displayName(),
                request.email(),
                request.photoUrl()
        );
    }

    public AuthResponseDto toResponse(AuthResult result) {
        return new AuthResponseDto(
                result.accountId(),
                result.verified(),
                result.accessToken(),
                result.refreshToken(),
                result.tokenType(),
                result.accessExpiresIn(),
                result.refreshExpiresIn(),
                result.accessExpiresAt(),
                result.refreshExpiresAt()
        );
    }

    public AccountResponseDto toResponse(AccountSummary summary) {
        return new AccountResponseDto(
                summary.id() == null ? null : summary.id().toString(),
                summary.name(),
                null,
                null,
                null,
                summary.provider() == null ? null : summary.provider().name(),
                null,
                summary.activatedAt() != null,
                null,
                null,
                null
        );
    }

    public AccountResponseDto toResponse(AccountDetails details) {
        return new AccountResponseDto(
                details.id() == null ? null : details.id().toString(),
                details.name(),
                details.email(),
                details.phoneNumber() == null ? null : details.phoneNumber().prefix(),
                details.phoneNumber() == null ? null : details.phoneNumber().number(),
                details.provider() == null ? null : details.provider().name(),
                details.avatarUrl(),
                details.activatedAt() != null,
                details.disabledAt() != null,
                toIsoString(details.createdAt()),
                toIsoString(details.updatedAt())
        );
    }

    public ChangeEmailResponseDto toResponse(ChangeEmailResult result) {
        return new ChangeEmailResponseDto(
                result.accountId() == null ? null : result.accountId().toString(),
                result.email(),
                result.verificationRequired(),
                result.nextStep()
        );
    }

    public AccountSecurityEventResponseDto toResponse(AccountSecurityEventResult result) {
        return new AccountSecurityEventResponseDto(
                result.id() == null ? null : result.id().toString(),
                result.eventType() == null ? null : result.eventType().name(),
                result.detail(),
                result.ipAddress(),
                toIsoString(result.occurredAt())
        );
    }

    private PhoneNumber toPhoneNumber(String prefix, String number) {
        if ((prefix == null || prefix.isBlank()) && (number == null || number.isBlank())) {
            return null;
        }
        return new PhoneNumber(
                prefix == null ? null : prefix.trim(),
                number == null ? null : number.trim()
        );
    }

    private String toIsoString(OffsetDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toString();
    }
}
