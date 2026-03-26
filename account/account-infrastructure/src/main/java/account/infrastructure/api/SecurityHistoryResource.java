package account.infrastructure.api;

import account.application.usecase.SecurityHistoryUseCase;
import account.infrastructure.api.dto.AccountSecurityEventResponseDto;
import account.infrastructure.api.mapper.AccountRequestMapper;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.List;

@Path("security-history")
public class SecurityHistoryResource {

    @Inject
    SecurityHistoryUseCase securityHistoryUseCase;
    @Inject
    AccountRequestMapper accountRequestMapper;

    @GET
    @Path("")
    public Uni<RestResponse<List<AccountSecurityEventResponseDto>>> securityHistory() {
        return securityHistoryUseCase.execute()
                .map(results -> results.stream().map(accountRequestMapper::toResponse).toList())
                .map(RestResponse::ok);
    }
}
