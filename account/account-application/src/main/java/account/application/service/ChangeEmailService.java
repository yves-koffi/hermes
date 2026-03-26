package account.application.service;

import account.application.command.ChangeEmailCommand;
import account.application.command.SendVerifyCodeCommand;
import account.application.result.ChangeEmailResult;
import account.application.spi.AccountRepository;
import account.application.spi.AuthSessionRepository;
import account.application.usecase.ChangeEmailUseCase;
import account.application.usecase.VerificationNotificationUseCase;
import account.domain.model.AccountSecurityEventType;
import account.domain.model.Provider;
import account.domain.model.TokenType;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import shared.domain.exception.DomainConflictException;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Map;

@ApplicationScoped
public class ChangeEmailService implements ChangeEmailUseCase {

    @Inject
    CurrentAuthenticatedAccountService currentAuthenticatedAccountService;
    @Inject
    AccountRepository accountRepository;
    @Inject
    AuthSessionRepository authSessionRepository;
    @Inject
    VerificationNotificationUseCase verificationNotificationUseCase;
    @Inject
    AccountSecurityEventService accountSecurityEventService;

    @Override
    public Uni<ChangeEmailResult> execute(ChangeEmailCommand command) {
        if (command.email() == null || command.email().isBlank()) {
            return Uni.createFrom().failure(new DomainConflictException(
                    "INVALID_EMAIL",
                    "account.email.invalid",
                    Map.of()
            ));
        }

        String normalizedEmail = normalizeEmail(command.email());
        return currentAuthenticatedAccountService.requireCurrentAccount()
                .flatMap(current -> {
                    if (current.provider() != Provider.BASIC) {
                        return Uni.createFrom().failure(new DomainConflictException(
                                "ACCOUNT_PROVIDER_NOT_SUPPORTED",
                                "account.change_email.provider.not_supported",
                                Map.of("provider", current.provider().name())
                        ));
                    }
                    if (current.email().equals(normalizedEmail)) {
                        return Uni.createFrom().item(new ChangeEmailResult(
                                current.id(),
                                current.email(),
                                !current.isActivated(),
                                current.isActivated() ? null : "VERIFY_EMAIL"
                        ));
                    }

                    return accountRepository.findByEmail(normalizedEmail)
                            .flatMap(accountOpt -> {
                                if (accountOpt.isPresent() && !accountOpt.get().id().equals(current.id())) {
                                    return Uni.createFrom().failure(new DomainConflictException(
                                            "ACCOUNT_EMAIL_ALREADY_EXISTS",
                                            "account.email.already_exists",
                                            Map.of("email", command.email())
                                    ));
                                }

                                OffsetDateTime now = OffsetDateTime.now();
                                return accountRepository.save(current.changeEmail(normalizedEmail, true, now))
                                        .flatMap(saved -> authSessionRepository.revokeAllByAccountId(saved.id(), now)
                                                .flatMap(ignored -> verificationNotificationUseCase.execute(
                                                        new SendVerifyCodeCommand(saved.email(), TokenType.EMAIL_VERIFICATION_CODE)
                                                ))
                                                .flatMap(ignored -> accountSecurityEventService.record(
                                                        saved,
                                                        AccountSecurityEventType.EMAIL_CHANGED,
                                                        "Email changed; verification required"
                                                ))
                                                .replaceWith(new ChangeEmailResult(
                                                        saved.id(),
                                                        saved.email(),
                                                        true,
                                                        "VERIFY_EMAIL"
                                                )));
                            });
                });
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
