package account.application.service;

import account.application.command.RegisterCommand;
import account.application.command.SendVerifyCodeCommand;
import account.application.mapper.AccountCommandMapper;
import account.application.result.RegisterResult;
import account.application.spi.AccountRepository;
import account.application.usecase.VerificationNotificationUseCase;
import account.domain.model.Account;
import account.domain.model.PhoneNumber;
import account.domain.model.Provider;
import account.domain.model.TokenType;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegisterServiceTest {

    @Test
    void should_require_email_verification_when_requested() {
        InMemoryAccountRepository accountRepository = new InMemoryAccountRepository();
        RecordingVerificationNotificationUseCase verificationUseCase = new RecordingVerificationNotificationUseCase();

        RegisterService service = new RegisterService();
        service.accountRepository = accountRepository;
        service.verificationNotificationUseCase = verificationUseCase;
        service.accountCommandMapper = new AccountCommandMapper();

        RegisterResult result = service.execute(new RegisterCommand(
                "John",
                "john@example.com",
                "secret",
                new PhoneNumber("+225", "0700000000"),
                true
        )).await().indefinitely();

        assertTrue(result.verificationRequired());
        assertEquals("VERIFY_EMAIL", result.nextStep());
        assertNotNull(accountRepository.savedAccount);
        assertNull(accountRepository.savedAccount.activatedAt());
        assertNotNull(verificationUseCase.command);
        assertEquals("john@example.com", verificationUseCase.command.email());
        assertEquals(TokenType.EMAIL_VERIFICATION_CODE, verificationUseCase.command.type());
    }

    @Test
    void should_activate_account_immediately_when_email_verification_is_not_required() {
        InMemoryAccountRepository accountRepository = new InMemoryAccountRepository();
        RecordingVerificationNotificationUseCase verificationUseCase = new RecordingVerificationNotificationUseCase();

        RegisterService service = new RegisterService();
        service.accountRepository = accountRepository;
        service.verificationNotificationUseCase = verificationUseCase;
        service.accountCommandMapper = new AccountCommandMapper();

        RegisterResult result = service.execute(new RegisterCommand(
                "John",
                "john@example.com",
                "secret",
                new PhoneNumber("+225", "0700000000"),
                false
        )).await().indefinitely();

        assertFalse(result.verificationRequired());
        assertNull(result.nextStep());
        assertNotNull(accountRepository.savedAccount);
        assertNotNull(accountRepository.savedAccount.activatedAt());
        assertNull(verificationUseCase.command);
    }

    private static final class InMemoryAccountRepository implements AccountRepository {
        private Account savedAccount;

        @Override
        public Uni<Account> save(Account account) {
            this.savedAccount = account;
            return Uni.createFrom().item(account);
        }

        @Override
        public Uni<Optional<Account>> findById(UUID id) {
            return Uni.createFrom().item(Optional.empty());
        }

        @Override
        public Uni<Optional<Account>> findByEmail(String email) {
            return Uni.createFrom().item(Optional.empty());
        }

        @Override
        public Uni<List<Account>> findAll() {
            return Uni.createFrom().item(List.of());
        }

        @Override
        public Uni<Void> deleteById(UUID id) {
            return Uni.createFrom().voidItem();
        }
    }

    private static final class RecordingVerificationNotificationUseCase implements VerificationNotificationUseCase {
        private SendVerifyCodeCommand command;

        @Override
        public Uni<account.application.result.VerifyCodeSentResult> execute(SendVerifyCodeCommand command) {
            this.command = command;
            return Uni.createFrom().item(new account.application.result.VerifyCodeSentResult(
                    true,
                    "EMAIL",
                    command.type(),
                    OffsetDateTime.now().plusMinutes(10)
            ));
        }
    }
}
