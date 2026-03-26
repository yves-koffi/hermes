package account.application.service;

import account.application.command.LoginCommand;
import account.application.result.AuthResult;
import account.application.spi.AccountRepository;
import account.domain.model.Account;
import account.domain.model.PhoneNumber;
import account.domain.model.Provider;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;
import shared.domain.exception.AuthenticationException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginServiceTest {

    @Test
    void should_increment_success_metric_when_login_succeeds() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        AccountMetrics metrics = metrics(registry);

        LoginService service = new LoginService();
        service.accountRepository = new InMemoryAccountRepository(account(
                UUID.randomUUID(),
                "john@example.com",
                BcryptUtil.bcryptHash("secret"),
                OffsetDateTime.now()
        ));
        service.authSessionTokenService = new StubAuthSessionTokenService(
                new AuthResult(
                        UUID.randomUUID(),
                        true,
                        "access",
                        "refresh",
                        "Bearer",
                        120,
                        10080,
                        OffsetDateTime.now().plusMinutes(120),
                        OffsetDateTime.now().plusDays(7)
                )
        );
        service.accountMetrics = metrics;

        AuthResult result = service.execute(new LoginCommand("john@example.com", "secret"))
                .await().indefinitely();

        assertEquals("access", result.accessToken());
        assertEquals(1.0, registry.get("account.login.success").counter().count());
        assertEquals(null, registry.find("account.login.failure").counter());
    }

    @Test
    void should_increment_failure_metric_when_login_is_refused() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        AccountMetrics metrics = metrics(registry);

        LoginService service = new LoginService();
        service.accountRepository = new InMemoryAccountRepository(account(
                UUID.randomUUID(),
                "john@example.com",
                BcryptUtil.bcryptHash("secret"),
                OffsetDateTime.now()
        ));
        service.authSessionTokenService = new StubAuthSessionTokenService(
                new AuthResult(
                        UUID.randomUUID(),
                        true,
                        "access",
                        "refresh",
                        "Bearer",
                        120,
                        10080,
                        OffsetDateTime.now().plusMinutes(120),
                        OffsetDateTime.now().plusDays(7)
                )
        );
        service.accountMetrics = metrics;

        assertThrows(AuthenticationException.class, () ->
                service.execute(new LoginCommand("john@example.com", "bad-secret"))
                        .await().indefinitely()
        );

        assertEquals(1.0, registry.get("account.login.failure").counter().count());
    }

    private static AccountMetrics metrics(SimpleMeterRegistry registry) {
        AccountMetrics metrics = new AccountMetrics();
        metrics.meterRegistry = registry;
        return metrics;
    }

    private static Account account(UUID id, String email, String password, OffsetDateTime activatedAt) {
        OffsetDateTime now = OffsetDateTime.now();
        return new Account(
                id,
                "John",
                email,
                new PhoneNumber("+225", "0700000000"),
                password,
                null,
                null,
                Provider.BASIC,
                activatedAt,
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
            return Uni.createFrom().item(Optional.ofNullable(account));
        }

        @Override
        public Uni<Optional<Account>> findByEmail(String email) {
            if (account != null && account.email().equals(email)) {
                return Uni.createFrom().item(Optional.of(account));
            }
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

    private static final class StubAuthSessionTokenService extends AuthSessionTokenService {
        private final AuthResult authResult;

        private StubAuthSessionTokenService(AuthResult authResult) {
            this.authResult = authResult;
        }

        @Override
        public Uni<AuthResult> issueTokens(Account account, UUID rotatedFromSessionId) {
            return Uni.createFrom().item(authResult);
        }
    }
}
