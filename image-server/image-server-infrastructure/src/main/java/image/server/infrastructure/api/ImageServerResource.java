package image.server.infrastructure.api;

import image.server.application.usecase.DeleteImageUseCase;
import image.server.application.usecase.PreviewImageUseCase;
import image.server.application.usecase.UploadImageUseCase;
import image.server.infrastructure.adapter.ImageServerAdapter;
import image.server.domain.UploadResult;
import image.server.infrastructure.config.ImageConfig;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;

@Path("/${image.route-prefix:images}")
public class ImageServerResource {

    @Inject
    PreviewImageUseCase previewImageUseCase;

    @Inject
    DeleteImageUseCase deleteImageUseCase;

    @Inject
    UploadImageUseCase uploadImageUseCase;

    @Inject
    ImageServerAdapter imageServerAdapter;

    @Inject
    ImageConfig imageConfig;

    void onStart(@Observes StartupEvent event) throws IOException {
        imageServerAdapter.ensureDirectories();
    }

    /**
     * GET /images/{path: chemin vers le fichier, supporte les sous-dossiers}
     * <p>
     * Exemples :
     * GET /images/photo.jpg
     * GET /images/avatars/alice.png
     * GET /images/products/shoes/red.webp?w=400&h=300&crop=true
     * <p>
     * Stratégie de cache disque :
     * - Sans params resize → sert l'original depuis uploads/
     * - Avec params resize → vérifie cache/ d'abord, génère + sauvegarde si absent
     * <p>
     * Query params :
     * w       (int)    – largeur cible en px        (max 5000)
     * h       (int)    – hauteur cible en px        (max 5000)
     * crop    (bool)   – recadrage centré exact w×h (défaut: false → fit)
     * q       (int)    – qualité 1-100              (défaut: 85)
     * fmt     (string) – format de sortie (appliqué uniquement si resize) : jpg | png | webp
     * upscale (bool)   – autorise l'agrandissement  (défaut: false)
     */
    @GET
    @Path("/{path: .+}")
    public Uni<Response> serveImage(
            @RestPath("path") String subPath,
            @RestQuery("w") Integer w,
            @RestQuery("h") Integer h,
            @RestQuery("crop") @DefaultValue("false") boolean crop,
            @RestQuery("q") @DefaultValue("85") int q,
            @RestQuery("fmt") @DefaultValue("jpg") String fmt,
            @RestQuery("upscale") @DefaultValue("false") boolean upscale
    ) {
        return previewImageUseCase.execute(subPath, w, h, crop, q, fmt, upscale)
                .map(bytes -> {
                    String outputFmt = (w == null && h == null)
                            ? sanitizeFormat(getExtension(subPath))
                            : sanitizeFormat(fmt);
                    return buildImageResponse(bytes, outputFmt);
                })
                .onFailure(WebApplicationException.class)
                .recoverWithItem(t -> ((WebApplicationException) t).getResponse());
    }

    /**
     * DELETE /images/{path}
     * Supprime l'original et tous ses variants cachés.
     */
    @GET
    @Path("/{path: .+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> deleteImage(
            @RestPath String path
    )  {
        return deleteImageUseCase.execute(path)
                .replaceWith(Response.noContent().build())
                .onFailure(WebApplicationException.class)
                .recoverWithItem(t -> ((WebApplicationException) t).getResponse());
    }

    @GET
    @Path("/upload")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Uni<Response> upload(
            @RestForm("file") FileUpload file,
            @RestForm("folder") @DefaultValue("") String folder
    ) {
        return uploadImageUseCase.execute(file, folder)
                .map(url -> Response.status(Response.Status.CREATED)
                        .entity(new UploadResult(url, null))
                        .type(MediaType.APPLICATION_JSON)
                        .build())
                .onFailure(WebApplicationException.class)
                .recoverWithItem(t -> ((WebApplicationException) t).getResponse());
    }


    private Response buildImageResponse(byte[] bytes, String outputFmt) {
        return Response.ok(bytes)
                .type(mediaTypeFor(outputFmt))
                .header("Cache-Control", "public, max-age=" + cacheSeconds() + ", immutable")
                .header("ETag", '"' + sha1(bytes) + '"')
                .build();
    }

    private long cacheSeconds() {
        return TimeUnit.DAYS.toSeconds(imageConfig.cacheDays());
    }

    private String sha1(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            return HexFormat.of().formatHex(md.digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 not available", e);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        String ext = filename.substring(filename.lastIndexOf('.') + 1);
        return ext.isBlank() ? "" : ext;
    }

    private String sanitizeFormat(String fmt) {
        if (fmt == null) return "jpg";
        return switch (fmt.toLowerCase()) {
            case "png"  -> "png";
            case "webp" -> "webp";
            default     -> "jpg";
        };
    }

    private String mediaTypeFor(String fmt) {
        return switch (fmt) {
            case "png"  -> "image/png";
            case "webp" -> "image/webp";
            default     -> "image/jpeg";
        };
    }
}
