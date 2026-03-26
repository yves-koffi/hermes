package account.application.service;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AccountMetrics {

    @Inject
    MeterRegistry meterRegistry;

    public void recordLoginSucceeded() {
        meterRegistry.counter("account.login.success").increment();
    }

    public void recordLoginFailed() {
        meterRegistry.counter("account.login.failure").increment();
    }

    public void recordPasswordResetRequested() {
        meterRegistry.counter("account.password_reset.requested").increment();
    }

    public void recordEmailVerificationSucceeded() {
        meterRegistry.counter("account.email_verification.success").increment();
    }
}
