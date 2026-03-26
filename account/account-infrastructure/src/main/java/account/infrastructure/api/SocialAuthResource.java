package account.infrastructure.api;

import account.application.usecase.SocialAuthUseCase;
import account.infrastructure.api.dto.AuthResponseDto;
import account.infrastructure.api.dto.SocialAuthRequestDto;
import account.infrastructure.api.mapper.AccountRequestMapper;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;

@Path("social")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SocialAuthResource {

    @Inject
    AccountRequestMapper accountRequestMapper;
    @Inject
    SocialAuthUseCase socialAuthUseCase;

    @POST
    @Path("login")
    public Uni<RestResponse<AuthResponseDto>> socialAuth(SocialAuthRequestDto request) {
        return socialAuthUseCase.execute(accountRequestMapper.toCommand(request))
                .map(accountRequestMapper::toResponse)
                .map(RestResponse::ok);
    }
}
