package account.infrastructure.api;


import account.application.usecase.LoginUseCase;
import account.application.usecase.CurrentAccountUseCase;
import account.infrastructure.api.dto.AccountResponse;
import account.infrastructure.api.dto.LoginRequest;
import account.infrastructure.api.mapper.AccountRequestMapper;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;

@Path("account")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class AccountResource {

    @Inject
    LoginUseCase loginUseCase;
    @Inject
    CurrentAccountUseCase currentAccountUseCase;
    @Inject
    AccountRequestMapper accountRequestMapper;

    @POST
    @Path("login")
    public Uni<RestResponse<AccountResponse>> login(LoginRequest request) {
//        return loginUseCase.execute(accountRequestMapper.toCommand(request))
//                .map(accountRequestMapper::toResponse)
//                .map(RestResponse::ok);
        return Uni.createFrom().item(null);
    }

    @GET
    @Path("current")
    public Uni<RestResponse<AccountResponse>> current() {
        return currentAccountUseCase.execute()
                .map(accountRequestMapper::toResponse)
                .map(RestResponse::ok);
    }
}
