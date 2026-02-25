package life.ping.infrastructure.api;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("life-ping")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LifePingResource {

    @GET
    @Path("devices/auth")
    public Uni<String> deviceAuth() {
        return Uni.createFrom().item("Hello LifePing");
    }

    @GET
    @Path("devices/transfert")
    public Uni<String> transfert() {
        return Uni.createFrom().item("Hello LifePing");
    }

    @GET
    @Path("devices/status")
    public Uni<String> status() {
        return Uni.createFrom().item("Hello LifePing");
    }

    @GET
    @Path("devices/reset")
    public Uni<String> reset() {
        return Uni.createFrom().item("Hello LifePing");
    }

    @POST
    @Path("devices/checkins/sync")
    public Uni<String> checkinSync() {
        return Uni.createFrom().item("Hello LifePing");
    }

    @POST
    @Path("devices/checkins")
    public Uni<String> checkins() {
        return Uni.createFrom().item("Hello LifePing");
    }

    @GET
    @Path("/devices/register-token")
    public Uni<String> registerTokenFCM() {
        return Uni.createFrom().item("Hello LifePing");
    }

    @GET
    @Path("/devices/emergency-contact")
    public Uni<String> emergencyContact() {
        return Uni.createFrom().item("Hello LifePing");
    }


}
