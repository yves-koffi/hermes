package shared.infrastructure.exceptionMapper;

import io.quarkiverse.resteasy.problem.HttpProblem;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.WebApplicationException;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import shared.domain.exception.DomainException;
import shared.infrastructure.utils.TranslationService;

import java.net.URI;

public class ProblemExceptionMappers {
    private static final Logger LOGGER = Logger.getLogger(ProblemExceptionMappers.class);

    @Inject
    UriInfo uriInfo;

    @Inject TranslationService translationService;

    @ServerExceptionMapper
    public Response map(DomainException ex) {
        int statusCode = ex.getStatusCode();
        String messageKey = ex.getMessageKey();

        HttpProblem rebuilt = HttpProblem.builder(ex)
                .withType(ex.getType() != null ? ex.getType() : URI.create("about:blank"))
                .withTitle(ex.getTitle())
                .withStatus(statusCode)
                .withDetail(translationService.translate(messageKey))
                .withInstance(instanceUri())
                .build();

        LOGGER.warnf("%s - %s", ex.getCode(), messageKey);

        return Response.status(statusCode)
                .type(HttpProblem.MEDIA_TYPE)
                .entity(rebuilt)
                .build();
    }

    @ServerExceptionMapper
    public Response map(WebApplicationException ex) {
        int statusCode = ex.getResponse() != null
                ? ex.getResponse().getStatus()
                : Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        String code = "HTTP_" + statusCode;
        String messageKey = "http." + statusCode;
        Response.Status status = Response.Status.fromStatusCode(statusCode);

        HttpProblem problem = HttpProblem.builder()
                .withType(URI.create("about:blank"))
                .withTitle(status != null ? status.getReasonPhrase() : "HTTP Error")
                .withStatus(statusCode)
                .withDetail(translationService.translate(messageKey))
                .withInstance(instanceUri())
                .with("code", code)
                .with("messageKey", messageKey)
                .build();

        LOGGER.warnf("HTTP exception mapped: code=%s, messageKey=%s", code, messageKey);

        return Response.status(statusCode)
                .type(HttpProblem.MEDIA_TYPE)
                .entity(problem)
                .build();
    }

    @ServerExceptionMapper
    public Response map(Throwable ex) {
        int statusCode = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        String messageKey = "error.internal";

        HttpProblem problem = HttpProblem.builder()
                .withType(URI.create("about:blank"))
                .withTitle(Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .withStatus(statusCode)
                .withDetail(translationService.translate(messageKey))
                .withInstance(instanceUri())
                .with("code", "INTERNAL_SERVER_ERROR")
                .with("messageKey", messageKey)
                .build();

        LOGGER.error("Unhandled exception mapped to Problem", ex);

        return Response.status(statusCode)
                .type(HttpProblem.MEDIA_TYPE)
                .entity(problem)
                .build();
    }

    private URI instanceUri() {
        String path = uriInfo != null ? uriInfo.getPath() : "/";
        if (!path.startsWith("/")) path = "/" + path;
        return URI.create(path);
    }
}
