package account.infrastructure.api;


import account.application.usecase.ChangePasswordUseCase;
import account.application.usecase.ChangeEmailUseCase;
import account.application.usecase.CheckTokenUseCase;
import account.application.usecase.DeactivateAccountUseCase;
import account.application.usecase.DeleteAccountUseCase;
import account.application.result.RegisterResult;
import account.application.result.CheckTokenResult;
import account.application.usecase.SecurityHistoryUseCase;
import account.application.usecase.RegisterUseCase;
import account.application.usecase.UpdateAccountUseCase;
import account.application.usecase.CurrentAccountUseCase;
import account.infrastructure.api.dto.CheckTokenRequestDto;
import account.infrastructure.api.dto.ChangeEmailRequestDto;
import account.infrastructure.api.dto.ChangeEmailResponseDto;
import account.infrastructure.api.dto.ChangePasswordRequestDto;
import account.infrastructure.api.dto.AccountResponseDto;
import account.infrastructure.api.dto.AccountSecurityEventResponseDto;
import account.infrastructure.api.dto.RegisterRequestDto;
import account.infrastructure.api.dto.UpdateAccountRequestDto;
import account.infrastructure.api.mapper.AccountRequestMapper;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;

@Path("account")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccountResource {

    @Inject
    RegisterUseCase registerUseCase;
    @Inject
    UpdateAccountUseCase updateAccountUseCase;
    @Inject
    CurrentAccountUseCase currentAccountUseCase;
    @Inject
    ChangePasswordUseCase changePasswordUseCase;
    @Inject
    ChangeEmailUseCase changeEmailUseCase;

    @Inject
    CheckTokenUseCase checkTokenUseCase;
    @Inject
    DeactivateAccountUseCase deactivateAccountUseCase;
    @Inject
    DeleteAccountUseCase deleteAccountUseCase;
    @Inject
    AccountRequestMapper accountRequestMapper;



    @POST
    @Path("")
    public Uni<RestResponse<RegisterResult>> register(RegisterRequestDto request) {
        return registerUseCase.execute(accountRequestMapper.toCommand(request))
                .map(RestResponse::ok);
    }

    @GET
    @Path("current")
    public Uni<RestResponse<AccountResponseDto>> current() {
        return currentAccountUseCase.execute()
                .map(accountRequestMapper::toResponse)
                .map(RestResponse::ok);
    }

    @PUT
    @Path("")
    public Uni<RestResponse<Void>> update(UpdateAccountRequestDto request) {
        return updateAccountUseCase.execute(accountRequestMapper.toCommand(request))
                .replaceWith(RestResponse.noContent());
    }

    @POST
    @Path("deactivate")
    public Uni<RestResponse<Void>> deactivate() {
        return deactivateAccountUseCase.execute()
                .replaceWith(RestResponse.noContent());
    }

    @DELETE
    @Path("")
    public Uni<RestResponse<Void>> delete() {
        return deleteAccountUseCase.execute()
                .replaceWith(RestResponse.noContent());
    }

    @POST
    @Path("change-password")
    public Uni<RestResponse<Void>> changePassword(ChangePasswordRequestDto request) {
        return changePasswordUseCase.execute(accountRequestMapper.toCommand(request))
                .replaceWith(RestResponse.noContent());
    }

    @POST
    @Path("change-email")
    public Uni<RestResponse<ChangeEmailResponseDto>> changeEmail(ChangeEmailRequestDto request) {
        return changeEmailUseCase.execute(accountRequestMapper.toCommand(request))
                .map(accountRequestMapper::toResponse)
                .map(RestResponse::ok);
    }

    @POST
    @Path("check-token")
    public Uni<RestResponse<CheckTokenResult>> checkToken(CheckTokenRequestDto request) {
        return checkTokenUseCase.execute(accountRequestMapper.toCommand(request))
                .map(RestResponse::ok);
    }

}
