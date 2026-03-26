package account.application.service;

import account.application.command.UpdateAccountCommand;
import account.application.spi.AccountRepository;
import account.application.usecase.UpdateAccountUseCase;
import account.domain.model.PhoneNumber;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import shared.domain.exception.DomainConflictException;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Implémentation du use case de mise à jour du profil courant.
 *
 * Le service relit le compte du contexte, fusionne les données modifiables reçues dans la
 * commande avec les données existantes puis persiste la nouvelle version. Il ne modifie pas
 * les éléments liés à l'authentification comme l'email, le provider ou le mot de passe.
 */
@ApplicationScoped
public class UpdateAccountService implements UpdateAccountUseCase {

    @Inject
    CurrentAuthenticatedAccountService currentAuthenticatedAccountService;
    @Inject
    AccountRepository accountRepository;

    @Override
    public Uni<Void> execute(UpdateAccountCommand command) {
        if (command.name() != null && command.name().isBlank()) {
            return Uni.createFrom().failure(
                    new DomainConflictException(
                            "INVALID_ACCOUNT_NAME",
                            "account.name.invalid",
                            Map.of()
                    )
            );
        }
        if (command.phoneNumber() != null && !isPhoneNumberValid(command.phoneNumber())) {
            return Uni.createFrom().failure(
                    new DomainConflictException(
                            "INVALID_PHONE_NUMBER",
                            "account.phone.invalid",
                            Map.of()
                    )
            );
        }

        return currentAuthenticatedAccountService.requireCurrentAccount()
                .flatMap(current -> accountRepository.save(
                        current.withProfile(
                                command.name() == null ? current.name() : command.name().trim(),
                                command.phoneNumber() == null ? current.phoneNumber() : command.phoneNumber(),
                                command.avatarUrl() == null ? current.avatarUrl() : normalizeAvatarUrl(command.avatarUrl()),
                                OffsetDateTime.now()
                        )
                ).replaceWithVoid());
    }

    private boolean isPhoneNumberValid(PhoneNumber phoneNumber) {
        return phoneNumber.prefix() != null
                && !phoneNumber.prefix().isBlank()
                && phoneNumber.number() != null
                && !phoneNumber.number().isBlank();
    }

    private String normalizeAvatarUrl(String avatarUrl) {
        if (avatarUrl == null) {
            return null;
        }
        String trimmed = avatarUrl.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
