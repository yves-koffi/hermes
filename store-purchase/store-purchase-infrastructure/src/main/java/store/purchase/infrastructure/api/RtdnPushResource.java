package store.purchase.infrastructure.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import store.purchase.application.result.PlayNotification;
import store.purchase.domain.PubsubMessage;
import store.purchase.domain.PushRequest;
import store.purchase.infrastructure.event.GooglePlayPurchaseEvent;

@Path("/pubsub")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.TEXT_PLAIN)
public class RtdnPushResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(RtdnPushResource.class);
    private static final String SUCCESS_MESSAGE = "Message processed successfully";
    private static final String GENERIC_ERROR_MESSAGE = "Error processing message";

    @Inject
    ObjectMapper mapper;
    @Inject
    Event<GooglePlayPurchaseEvent> googlePlayPurchaseEvent;

    @POST
    @Path("/push")
    public Uni<Response> receivePubsubMessage(PushRequest request) {
        if (request == null || request.getMessage() == null) {
            LOGGER.warn("Requete Pub/Sub invalide: corps ou message manquant.");
            return Uni.createFrom().item(badRequest("Bad Request: Missing message"));
        }

        PubsubMessage pubsubMessage = request.getMessage();
        String messageId = pubsubMessage.getMessageId();
        String subscription = request.getSubscription();

        try {
            PlayNotification notificationData = readNotification(pubsubMessage);

            LOGGER.info("Message Pub/Sub recu sur l'abonnement '{}' avec l'ID '{}'.", subscription, messageId);

            googlePlayPurchaseEvent.fireAsync(new GooglePlayPurchaseEvent(messageId, subscription, notificationData));

            return Uni.createFrom().item(Response.ok().build());

        } catch (IllegalArgumentException | JsonProcessingException e) {
            LOGGER.warn("Payload Pub/Sub invalide pour le message ID '{}': {}", messageId, e.getMessage());
            return Uni.createFrom().item(badRequest("Bad Request: Invalid message payload"));
        } catch (Exception e) {
            LOGGER.error("Erreur generale lors de la reception du message Pub/Sub ID '{}'.", messageId, e);
            return Uni.createFrom().item(serverError(Response.Status.INTERNAL_SERVER_ERROR, "General error receiving message"));
        }
    }

    private PlayNotification readNotification(PubsubMessage pubsubMessage) throws JsonProcessingException {
        String decodedData = pubsubMessage.decodeData();
        if (decodedData == null || decodedData.isBlank()) {
            throw new IllegalArgumentException("Le contenu 'data' du message Pub/Sub est vide.");
        }

        return mapper.readValue(decodedData, PlayNotification.class);
    }

    private Response badRequest(String message) {
        return Response.status(Response.Status.BAD_REQUEST).entity(message).build();
    }

    private Response serverError(Response.Status status, String message) {
        return Response.status(status).entity(message).build();
    }
}
