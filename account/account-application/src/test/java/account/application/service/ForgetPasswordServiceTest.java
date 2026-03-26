package account.application.service;

import account.application.command.ForgetPasswordCommand;
import account.application.event.EmailDispatchEvent;
import account.application.result.ForgetPasswordResult;
import account.application.spi.AccountRepository;
import account.application.spi.HashTokenRepository;
import account.application.spi.NotificationByEmailDispatchPublisher;
import account.domain.model.Account;
import account.domain.model.HashToken;
import account.domain.model.PhoneNumber;
import account.domain.model.Provider;
import account.domain.model.TokenType;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;
import shared.application.context.ExecutionContext;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ForgetPasswordServiceTest {

    @Test
    void should_increment_reset_metric_when_request_is_accepted() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        AccountMetrics metrics = new AccountMetrics();
        metrics.meterRegistry = registry;

        InMemoryHashTokenRepository hashTokenRepository = new InMemoryHashTokenRepository();
        RecordingEmailDispatchPublisher emailDispatchPublisher = new RecordingEmailDispatchPublisher();

        ForgetPasswordService service = new ForgetPasswordService();
        service.accountRepository = new InMemoryAccountRepository(account());
        service.hashTokenRepository = hashTokenRepository;
        service.context = () -> new ExecutionContext(null, "127.0.0.1", "fr");
        service.oneTimeTokenService = new OneTimeTokenService();
        service.accountMetrics = metrics;
        service.emailDispatchPublisher = emailDispatchPublisher;

        ForgetPasswordResult result = service.execute(
                new ForgetPasswordCommand("john@example.com", TokenType.PASSWORD_RESET_LINK)
        ).await().indefinitely();

        assertTrue(result.accepted());
        assertEquals("EMAIL", result.deliveryChannel());
        assertEquals(TokenType.PASSWORD_RESET_LINK, result.tokenType());
        assertNotNull(result.expiresAt());
        assertNotNull(hashTokenRepository.savedToken);
        assertEquals(TokenType.PASSWORD_RESET_LINK, hashTokenRepository.savedToken.tokenType());
        assertNotNull(emailDispatchPublisher.event);
        assertEquals("john@example.com", emailDispatchPublisher.event.recipient());
        assertEquals(TokenType.PASSWORD_RESET_LINK, emailDispatchPublisher.event.tokenType());
        assertEquals("fr", emailDispatchPublisher.event.language());
        assertNotNull(emailDispatchPublisher.event.tokenValue());
        assertFalse(emailDispatchPublisher.event.tokenValue().isBlank());
        assertEquals(1.0, registry.get("account.password_reset.requested").counter().count());
    }

    private static Account account() {
        OffsetDateTime now = OffsetDateTime.now();
        return new Account(
                UUID.randomUUID(),
                "John",
                "john@example.com",
                new PhoneNumber("+225", "0700000000"),
                "hashed-password",
                null,
                null,
                Provider.BASIC,
                now,
                null,
                now,
                now
        );
    }

    private static final class InMemoryAccountRepository implements AccountRepository {
        private final Account account;

        private InMemoryAccountRepository(Account account) {
            this.account = account;
        }

        @Override
        public Uni<Account> save(Account account) {
            return Uni.createFrom().item(account);
        }

        @Override
        public Uni<Optional<Account>> findById(UUID id) {
            return Uni.createFrom().item(Optional.of(account));
        }

        @Override
        public Uni<Optional<Account>> findByEmail(String email) {
            return Uni.createFrom().item(Optional.of(account));
        }

        @Override
        public Uni<List<Account>> findAll() {
            return Uni.createFrom().item(List.of(account));
        }

        @Override
        public Uni<Void> deleteById(UUID id) {
            return Uni.createFrom().voidItem();
        }
    }

    private static final class InMemoryHashTokenRepository implements HashTokenRepository {
        private HashToken savedToken;

        @Override
        public Uni<HashToken> save(HashToken hashToken) {
            this.savedToken = hashToken;
            return Uni.createFrom().item(hashToken);
        }

        @Override
        public Uni<Optional<HashToken>> findById(UUID id) {
            return Uni.createFrom().item(Optional.empty());
        }

        @Override
        public Uni<Optional<HashToken>> findByHashToken(String hashToken) {
            return Uni.createFrom().item(Optional.empty());
        }

        @Override
        public Uni<List<HashToken>> findByAccountId(UUID accountId) {
            return Uni.createFrom().item(List.of());
        }

        @Override
        public Uni<Void> deleteById(UUID id) {
            return Uni.createFrom().voidItem();
        }

        @Override
        public Uni<Void> deleteByAccountIdAndTokenTypes(UUID accountId, List<TokenType> tokenTypes) {
            return Uni.createFrom().voidItem();
        }

        @Override
        public Uni<Void> deleteByAccountId(UUID accountId) {
            return Uni.createFrom().voidItem();
        }
    }

    private static final class RecordingEmailDispatchPublisher implements NotificationByEmailDispatchPublisher {
        private EmailDispatchEvent event;

        @Override
        public Uni<Void> publish(EmailDispatchEvent event) {
            this.event = event;
            return Uni.createFrom().voidItem();
        }
    }
}
