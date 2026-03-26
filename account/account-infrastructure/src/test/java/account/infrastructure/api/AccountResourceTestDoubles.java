package account.infrastructure.api;

import account.application.result.AccountDetails;
import account.application.result.AccountSecurityEventResult;
import account.application.result.AccountVerificationResult;
import account.application.result.AuthResult;
import account.application.result.ChangeEmailResult;
import account.application.result.CheckTokenResult;
import account.application.result.ForgetPasswordResult;
import account.application.result.PasswordResetResult;
import account.application.result.RegisterResult;
import account.application.result.VerifyCodeSentResult;
import account.application.usecase.ChangePasswordUseCase;
import account.application.usecase.ChangeEmailUseCase;
import account.application.usecase.CheckTokenUseCase;
import account.application.usecase.CurrentAccountUseCase;
import account.application.usecase.DeactivateAccountUseCase;
import account.application.usecase.DeleteAccountUseCase;
import account.application.usecase.ForgetPasswordUseCase;
import account.application.usecase.LoginUseCase;
import account.application.usecase.RegisterUseCase;
import account.application.usecase.ResetPasswordUseCase;
import account.application.usecase.SecurityHistoryUseCase;
import account.application.usecase.UpdateAccountUseCase;
import account.application.usecase.VerificationNotificationUseCase;
import account.application.usecase.CheckVerifyEmailUseCase;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Produces;

import java.util.List;

@ApplicationScoped
public class AccountResourceTestDoubles {

    static volatile AuthResult loginResponse;
    static volatile RegisterResult registerResponse;
    static volatile ChangeEmailResult changeEmailResponse;
    static volatile ForgetPasswordResult forgetPasswordResponse;
    static volatile PasswordResetResult resetPasswordResponse;
    static volatile AccountVerificationResult verifyEmailResponse;
    static volatile VerifyCodeSentResult resendVerificationResponse;
    static volatile CheckTokenResult checkTokenResponse;
    static volatile AccountDetails currentResponse;
    static volatile List<AccountSecurityEventResult> securityHistoryResponse;

    @Produces
    @Alternative
    @Priority(1)
    LoginUseCase loginUseCase() {
        return command -> Uni.createFrom().item(loginResponse);
    }

    @Produces
    @Alternative
    @Priority(1)
    RegisterUseCase registerUseCase() {
        return command -> Uni.createFrom().item(registerResponse);
    }

    @Produces
    @Alternative
    @Priority(1)
    ForgetPasswordUseCase forgetPasswordUseCase() {
        return command -> Uni.createFrom().item(forgetPasswordResponse);
    }

    @Produces
    @Alternative
    @Priority(1)
    ResetPasswordUseCase resetPasswordUseCase() {
        return command -> Uni.createFrom().item(resetPasswordResponse);
    }

    @Produces
    @Alternative
    @Priority(1)
    CheckVerifyEmailUseCase verifyEmailUseCase() {
        return command -> Uni.createFrom().item(verifyEmailResponse);
    }

    @Produces
    @Alternative
    @Priority(1)
    VerificationNotificationUseCase verificationNotificationUseCase() {
        return command -> Uni.createFrom().item(resendVerificationResponse);
    }

    @Produces
    @Alternative
    @Priority(1)
    CheckTokenUseCase checkTokenUseCase() {
        return command -> Uni.createFrom().item(checkTokenResponse);
    }

    @Produces
    @Alternative
    @Priority(1)
    UpdateAccountUseCase updateAccountUseCase() {
        return command -> Uni.createFrom().voidItem();
    }

    @Produces
    @Alternative
    @Priority(1)
    CurrentAccountUseCase currentAccountUseCase() {
        return () -> Uni.createFrom().item(currentResponse);
    }

    @Produces
    @Alternative
    @Priority(1)
    ChangePasswordUseCase changePasswordUseCase() {
        return command -> Uni.createFrom().voidItem();
    }

    @Produces
    @Alternative
    @Priority(1)
    ChangeEmailUseCase changeEmailUseCase() {
        return command -> Uni.createFrom().item(changeEmailResponse);
    }

    @Produces
    @Alternative
    @Priority(1)
    DeactivateAccountUseCase deactivateAccountUseCase() {
        return () -> Uni.createFrom().voidItem();
    }

    @Produces
    @Alternative
    @Priority(1)
    DeleteAccountUseCase deleteAccountUseCase() {
        return () -> Uni.createFrom().voidItem();
    }

    @Produces
    @Alternative
    @Priority(1)
    SecurityHistoryUseCase securityHistoryUseCase() {
        return () -> Uni.createFrom().item(securityHistoryResponse == null ? List.of() : securityHistoryResponse);
    }
}
