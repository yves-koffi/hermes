package account.application.service;

import account.application.command.RegisterCommand;
import account.application.result.RegisterDetails;
import account.application.spi.AccountRepository;
import account.application.usecase.RegisterUseCase;
import account.domain.model.Account;
import account.domain.model.Provider;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import shared.domain.exception.DomainConflictException;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class RegisterService implements RegisterUseCase {

    @Inject
    AccountRepository accountRepository;

    @Override
    public Uni<RegisterDetails> execute(RegisterCommand command) {
        return accountRepository.findByEmail(command.email())
                .flatMap(accountOpt -> {
                    if (accountOpt.isPresent()) {
                        return Uni.createFrom().failure(
                                new DomainConflictException(
                                        "ACCOUNT_EMAIL_ALREADY_EXISTS",
                                        "account.email.already_exists",
                                        Map.of("email", command.email())
                                )
                        );
                    }

                    Account account = new Account(
                            UUID.randomUUID(),
                            command.name(),
                            command.email(),
                            command.phoneNumber(),
                            BcryptUtil.bcryptHash(command.password()),
                            null,
                            null,
                            Provider.BASIC,
                            null,
                            OffsetDateTime.now(),
                            OffsetDateTime.now()
                    );

                    return accountRepository.save(account)
                            .map(saved -> new RegisterDetails(saved.id(), true));
                });
    }
}
