package shared.infrastructure.context;

import io.quarkus.vertx.web.RouteFilter;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import shared.application.context.ExecutionContext;

import java.util.UUID;

@ApplicationScoped
public class ExecutionContextFilter {

    private static final String ACCOUNT_ID_HEADER = "X-Account-Id";
    private static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";
    private static final String REAL_IP_HEADER = "X-Real-IP";
    private static final String UNKNOWN_IP = "unknown";

    @Inject
    RequestContextImpl context;

    @RouteFilter(2)
    void filter(RoutingContext rc) {
        String accountIdHeader = rc.request().getHeader(ACCOUNT_ID_HEADER);
        UUID accountId = null;
        if (accountIdHeader != null && !accountIdHeader.isBlank()) {
            try {
                accountId = UUID.fromString(accountIdHeader.trim());
            } catch (IllegalArgumentException ignored) {
            }
        }

        String ip = extractIp(rc);
        context.setExecutionContext(new ExecutionContext(accountId, ip));
        rc.next();
    }

    private String extractIp(RoutingContext rc) {
        String forwardedFor = rc.request().getHeader(FORWARDED_FOR_HEADER);
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String clientIp = forwardedFor.split(",")[0].trim();
            if (!clientIp.isBlank()) {
                return clientIp;
            }
        }

        String realIp = rc.request().getHeader(REAL_IP_HEADER);
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        if (rc.request().remoteAddress() != null && rc.request().remoteAddress().hostAddress() != null) {
            return rc.request().remoteAddress().hostAddress();
        }
        return UNKNOWN_IP;
    }
}
