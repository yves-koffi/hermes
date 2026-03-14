package account.infrastructure.api;


import account.application.result.RegisterDetails;
import account.application.usecase.RegisterUseCase;
import account.application.usecase.SocialAuthUseCase;
import account.application.usecase.UpdateAccountUseCase;
import account.application.usecase.LoginUseCase;
import account.application.usecase.CurrentAccountUseCase;
import account.infrastructure.api.dto.AccountResponse;
import account.infrastructure.api.dto.AuthResponse;
import account.infrastructure.api.dto.LoginRequest;
import account.infrastructure.api.dto.RegisterRequest;
import account.infrastructure.api.dto.SocialAuthRequest;
import account.infrastructure.api.dto.UpdateAccountRequest;
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
    LoginUseCase loginUseCase;
    @Inject
    SocialAuthUseCase socialAuthUseCase;
    @Inject
    RegisterUseCase registerUseCase;
    @Inject
    UpdateAccountUseCase updateAccountUseCase;
    @Inject
    CurrentAccountUseCase currentAccountUseCase;
    @Inject
    AccountRequestMapper accountRequestMapper;

    @POST
    @Path("login")
    public Uni<RestResponse<AuthResponse>> login(LoginRequest request) {
        return loginUseCase.execute(accountRequestMapper.toCommand(request))
                .map(accountRequestMapper::toResponse)
                .map(RestResponse::ok);
    }

    @POST
    @Path("social/login")
    public Uni<RestResponse<AuthResponse>> socialAuth(SocialAuthRequest request) {
        return socialAuthUseCase.execute(accountRequestMapper.toCommand(request))
                .map(accountRequestMapper::toResponse)
                .map(RestResponse::ok);
    }

    @POST
    @Path("register")
    public Uni<RestResponse<RegisterDetails>> register(RegisterRequest request) {
        return registerUseCase.execute(accountRequestMapper.toCommand(request))
                .map(RestResponse::ok);
    }

    @PUT
    public Uni<RestResponse<Void>> update(UpdateAccountRequest request) {
        return updateAccountUseCase.execute(accountRequestMapper.toCommand(request))
                .replaceWith(RestResponse.noContent());
    }

    @GET
    @Path("current")
    public Uni<RestResponse<AccountResponse>> current() {
        return currentAccountUseCase.execute()
                .map(accountRequestMapper::toResponse)
                .map(RestResponse::ok);
    }
}
