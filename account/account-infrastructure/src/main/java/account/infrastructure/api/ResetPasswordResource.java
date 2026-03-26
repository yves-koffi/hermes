package account.infrastructure.api;

import account.application.result.ForgetPasswordResult;
import account.application.result.PasswordResetResult;
import account.application.usecase.ForgetPasswordUseCase;
import account.application.usecase.ResetPasswordUseCase;
import account.infrastructure.api.dto.ForgotPasswordRequestDto;
import account.infrastructure.api.dto.ResetPasswordRequestDto;
import account.infrastructure.api.mapper.AccountRequestMapper;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.jboss.resteasy.reactive.RestResponse;

@Path("forgot-password")
public class ResetPasswordResource {

    @Inject
    ForgetPasswordUseCase forgetPasswordUseCase;
    @Inject
    ResetPasswordUseCase resetPasswordUseCase;
    @Inject
    AccountRequestMapper accountRequestMapper;

    @POST
    @Path("")
    public Uni<RestResponse<ForgetPasswordResult>> forgotPassword(ForgotPasswordRequestDto request) {
        return forgetPasswordUseCase.execute(accountRequestMapper.toCommand(request))
                .map(RestResponse::ok);
    }

    @POST
    @Path("")
    public Uni<RestResponse<PasswordResetResult>> resetPassword(ResetPasswordRequestDto request) {
        return resetPasswordUseCase.execute(accountRequestMapper.toCommand(request))
                .map(RestResponse::ok);
    }
}
