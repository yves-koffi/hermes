package account.infrastructure.api;

import account.application.result.AccountVerificationResult;
import account.application.result.VerifyCodeSentResult;
import account.application.usecase.VerificationNotificationUseCase;
import account.application.usecase.CheckVerifyEmailUseCase;
import account.infrastructure.api.dto.ResendVerificationRequestDto;
import account.infrastructure.api.dto.VerifyEmailRequestDto;
import account.infrastructure.api.mapper.AccountRequestMapper;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.jboss.resteasy.reactive.RestResponse;

public class VerificationEmailResource {
    @Inject
    CheckVerifyEmailUseCase checkVerifyEmailUseCase;
    @Inject
    VerificationNotificationUseCase verificationNotificationUseCase;
    @Inject
    AccountRequestMapper accountRequestMapper;

    @POST
    @Path("verify-email")
    public Uni<RestResponse<AccountVerificationResult>> verifyEmail(VerifyEmailRequestDto request) {
        return checkVerifyEmailUseCase.execute(accountRequestMapper.toCommand(request))
                .map(RestResponse::ok);
    }

    @POST
    @Path("resend-verification")
    public Uni<RestResponse<VerifyCodeSentResult>> resendVerification(ResendVerificationRequestDto request) {
        return verificationNotificationUseCase.execute(accountRequestMapper.toCommand(request))
                .map(RestResponse::ok);
    }
}
