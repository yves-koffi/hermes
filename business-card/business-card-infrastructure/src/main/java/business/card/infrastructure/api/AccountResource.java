package business.card.infrastructure.api;

import business.card.application.usecase.AttemptUseCase;
import business.card.application.usecase.CurrentAccountUseCase;
import business.card.infrastructure.api.dto.AccountResponse;
import business.card.infrastructure.api.dto.LoginRequest;
import business.card.infrastructure.api.mapper.AccountRequestMapper;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.reactive.RestResponse;

@Path("account")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class AccountResource {

    @Inject
    AttemptUseCase attemptUseCase;
    @Inject  CurrentAccountUseCase currentAccountUseCase;
    @Inject AccountRequestMapper accountRequestMapper;

    @POST
    @Path("attempt")
    public Uni<RestResponse<AccountResponse>> attempt(LoginRequest request) {
        return attemptUseCase.execute(accountRequestMapper.toCommand(request))
                .map(accountRequestMapper::toResponse)
                .map(RestResponse::ok);
    }

    @GET
    @Path("current")
    public Uni<RestResponse<AccountResponse>> current() {
        return currentAccountUseCase.execute()
                .map(accountRequestMapper::toResponse)
                .map(RestResponse::ok);
    }
}
