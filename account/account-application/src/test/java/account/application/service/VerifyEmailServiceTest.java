package account.application.service;

import account.application.command.VerifyAccountCommand;
import account.application.result.AccountVerificationResult;
import account.application.spi.AccountRepository;
import account.application.spi.AccountSecurityEventRepository;
import account.application.spi.HashTokenRepository;
import account.domain.model.AccountSecurityEvent;
import account.domain.model.Account;
import account.domain.model.HashToken;
import account.domain.model.PhoneNumber;
import account.domain.model.Provider;
import account.domain.model.TokenType;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VerifyEmailServiceTest {

    @Test
    void should_increment_verification_metric_when_email_is_verified() {
        OneTimeTokenService oneTimeTokenService = new OneTimeTokenService();
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        AccountMetrics metrics = new AccountMetrics();
        metrics.meterRegistry = registry;

        UUID accountId = UUID.randomUUID();
        String rawToken = "verify-token";
        HashToken token = new HashToken(
                UUID.randomUUID(),
                oneTimeTokenService.hash(rawToken),
                TokenType.EMAIL_VERIFICATION_LINK,
                accountId,
                OffsetDateTime.now().plusHours(1),
                "127.0.0.1",
                null,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        InMemoryAccountRepository accountRepository = new InMemoryAccountRepository(account(accountId));
        InMemoryHashTokenRepository hashTokenRepository = new InMemoryHashTokenRepository(token);

        CheckVerifyEmailService service = new CheckVerifyEmailService();
        service.hashTokenRepository = hashTokenRepository;
        service.accountRepository = accountRepository;
        service.oneTimeTokenService = oneTimeTokenService;
        service.accountMetrics = metrics;
        service.accountSecurityEventService = noOpSecurityEventService();

        AccountVerificationResult result = service.execute(
                new VerifyAccountCommand(rawToken, TokenType.EMAIL_VERIFICATION_LINK)
        ).await().indefinitely();

        assertEquals(accountId, result.accountId());
        assertTrue(result.verified());
        assertNotNull(result.verifiedAt());
        assertNotNull(accountRepository.savedAccount);
        assertEquals(1.0, registry.get("account.email_verification.success").counter().count());
        assertEquals(token.id(), hashTokenRepository.deletedTokenId);
    }

    private static Account account(UUID accountId) {
        OffsetDateTime now = OffsetDateTime.now();
        return new Account(
                accountId,
                "John",
                "john@example.com",
                new PhoneNumber("+225", "0700000000"),
                "hashed-password",
                null,
                null,
                Provider.BASIC,
                null,
                null,
                now,
                now
        );
    }

    private static AccountSecurityEventService noOpSecurityEventService() {
        AccountSecurityEventService service = new AccountSecurityEventService();
        service.accountSecurityEventRepository = new NoOpAccountSecurityEventRepository();
        service.context = () -> null;
        return service;
    }

    private static final class InMemoryAccountRepository implements AccountRepository {
        private Account account;
        private Account savedAccount;

        private InMemoryAccountRepository(Account account) {
            this.account = account;
        }

        @Override
        public Uni<Account> save(Account account) {
            this.savedAccount = account;
            this.account = account;
            return Uni.createFrom().item(account);
        }

        @Override
        public Uni<Optional<Account>> findById(UUID id) {
            return Uni.createFrom().item(Optional.ofNullable(account));
        }

        @Override
        public Uni<Optional<Account>> findByEmail(String email) {
            return Uni.createFrom().item(Optional.empty());
        }

        @Override
        public Uni<List<Account>> findAll() {
            return Uni.createFrom().item(account == null ? List.of() : List.of(account));
        }

        @Override
        public Uni<Void> deleteById(UUID id) {
            return Uni.createFrom().voidItem();
        }
    }

    private static final class InMemoryHashTokenRepository implements HashTokenRepository {
        private final HashToken token;
        private UUID deletedTokenId;

        private InMemoryHashTokenRepository(HashToken token) {
            this.token = token;
        }

        @Override
        public Uni<HashToken> save(HashToken hashToken) {
            return Uni.createFrom().item(hashToken);
        }

        @Override
        public Uni<Optional<HashToken>> findById(UUID id) {
            return Uni.createFrom().item(Optional.of(token));
        }

        @Override
        public Uni<Optional<HashToken>> findByHashToken(String hashToken) {
            if (token.hashToken().equals(hashToken)) {
                return Uni.createFrom().item(Optional.of(token));
            }
            return Uni.createFrom().item(Optional.empty());
        }

        @Override
        public Uni<List<HashToken>> findByAccountId(UUID accountId) {
            return Uni.createFrom().item(List.of(token));
        }

        @Override
        public Uni<Void> deleteById(UUID id) {
            this.deletedTokenId = id;
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

    private static final class NoOpAccountSecurityEventRepository implements AccountSecurityEventRepository {

        @Override
        public Uni<AccountSecurityEvent> save(AccountSecurityEvent event) {
            return Uni.createFrom().item(event);
        }

        @Override
        public Uni<List<AccountSecurityEvent>> findRecentByAccountId(UUID accountId, int limit) {
            return Uni.createFrom().item(List.of());
        }
    }
}
