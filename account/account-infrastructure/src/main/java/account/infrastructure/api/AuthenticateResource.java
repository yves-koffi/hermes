package account.infrastructure.api;

import account.application.usecase.LoginUseCase;
import account.application.usecase.LogoutAllSessionsUseCase;
import account.application.usecase.LogoutUseCase;
import account.application.usecase.RefreshSessionUseCase;
import account.infrastructure.api.dto.AuthResponseDto;
import account.infrastructure.api.dto.LoginRequestDto;
import account.infrastructure.api.dto.LogoutRequestDto;
import account.infrastructure.api.dto.RefreshTokenRequestDto;
import account.infrastructure.api.mapper.AccountRequestMapper;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;


@Path("auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthenticateResource {

    @Inject
    LoginUseCase loginUseCase;
    @Inject
    RefreshSessionUseCase refreshSessionUseCase;
    @Inject
    LogoutUseCase logoutUseCase;
    @Inject
    LogoutAllSessionsUseCase logoutAllSessionsUseCase;
    @Inject
    AccountRequestMapper accountRequestMapper;

    @POST
    @Path("login")
    public Uni<RestResponse<AuthResponseDto>> login(LoginRequestDto request) {
        return loginUseCase.execute(accountRequestMapper.toCommand(request))
                .map(accountRequestMapper::toResponse)
                .map(RestResponse::ok);
    }

    @POST
    @Path("refresh")
    public Uni<RestResponse<AuthResponseDto>> refresh(RefreshTokenRequestDto request) {
        return refreshSessionUseCase.execute(accountRequestMapper.toCommand(request))
                .map(accountRequestMapper::toResponse)
                .map(RestResponse::ok);
    }

    @POST
    @Path("logout")
    public Uni<RestResponse<Void>> logout(LogoutRequestDto request) {
        return logoutUseCase.execute(accountRequestMapper.toCommand(request))
                .replaceWith(RestResponse.noContent());
    }

    @POST
    @Path("logout-all")
    public Uni<RestResponse<Void>> logoutAll() {
        return logoutAllSessionsUseCase.execute()
                .replaceWith(RestResponse.noContent());
    }
}
