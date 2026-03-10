package store.purchase.infrastructure.api;

import com.apple.itunes.storekit.verification.VerificationException;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import store.purchase.infrastructure.api.dto.AppleNotificationRequest;
import store.purchase.infrastructure.store.app.store.AppleNotificationHandler;

@Path("/apple")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AppleNotificationsResource {

    @Inject
    AppleNotificationHandler handler;

    @POST
    @Path("/receive/{appleId}/notifications")
    public Uni<Response> receive(
            @PathParam("appleId") Long appleId,
            AppleNotificationRequest req) {
        if (req == null || req.signedPayload() == null || req.signedPayload().isBlank()) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST).build());
        }
        try {

            return handler.processNotification(req.signedPayload(), appleId)
                    .onItem().transform(it -> Response.ok().build());

        }  catch (Exception e) {

            return Uni.createFrom().item(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());

        }
    }
}
