package business.card.infrastructure.api;

import business.card.application.usecase.FindImageUseCase;
import business.card.domain.model.RecordFilter;
import business.card.application.usecase.PullUseCase;
import business.card.application.usecase.PushUseCase;
import business.card.application.usecase.SyncUseCase;
import business.card.application.usecase.UploadImageUseCase;
import business.card.infrastructure.api.mapper.BusinessCardRequestMapper;
import business.card.infrastructure.api.dto.BusinessCardRequest;
import business.card.infrastructure.api.dto.BusinessCardResponse;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.util.List;
import java.util.UUID;

@Path("business-card")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
class BusinessCardResource {

    private final PullUseCase pullUseCase;
    private final PushUseCase pushUseCase;
    private final SyncUseCase syncUseCase;
    private final UploadImageUseCase uploadImageUseCase;
    private final FindImageUseCase findImageUseCase;
    private final BusinessCardRequestMapper businessCardRequestMapper;

    @GET
    @Path("pull")
    public Uni<RestResponse<List<BusinessCardResponse>>> pull(
            @QueryParam("record")
            @DefaultValue("") String deleted
    ) {
        RecordFilter filter = toPullFilter(deleted);
        return pullUseCase.execute(filter)
                .map(businessCardRequestMapper::toResponses)
                .map(RestResponse::ok);
    }

    @POST
    @Path("push")
    public Uni<RestResponse<Void>> push(BusinessCardRequest request) {
        return pushUseCase
                .execute(List.of(businessCardRequestMapper.toCommand(request)))
                .map(RestResponse::ok);
    }


    @POST
    @Path("sync")
    public Uni<List<BusinessCardResponse>> sync(List<BusinessCardRequest> requests) {
        return syncUseCase.execute(businessCardRequestMapper.toCommands(requests))
                .map(businessCardRequestMapper::toResponses);
    }

    @GET
    @Path("upload/image/{uuid}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<String>> uploadImage(
            @PathParam("uuid") UUID uuid,
            @RestForm("avatar") FileUpload file,
            @RestForm("folder")
            @DefaultValue("") String folder
    ) {
        return uploadImageUseCase.execute(file, folder, uuid)
                .map(RestResponse::ok);
    }

    @GET
    @Path("image/{uuid}")
    public Uni<RestResponse<String>> image(
            @PathParam("uuid") UUID uuid
    ) {
        return findImageUseCase.execute(uuid)
                .map(RestResponse::ok);
    }

    private RecordFilter toPullFilter(String value) {
        if (value == null || value.isBlank()) {
            return RecordFilter.RECORD;
        }

        return switch (value.trim().toLowerCase()) {
            case "deleted" -> RecordFilter.DELETED;
            case "all", "*" -> RecordFilter.ALL;
            default -> RecordFilter.RECORD;
        };
    }
}
