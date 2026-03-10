package shared.infrastructure.context;

import jakarta.enterprise.context.RequestScoped;
import shared.application.context.ExecutionContext;
import shared.application.context.RequestContext;

@RequestScoped
public class RequestContextImpl implements RequestContext {

    private  ExecutionContext executionContext;

    public void setExecutionContext(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

   /* @Override
    public Uni<UUID> getCurrentAccountId() {

        String accountIdHeader = httpHeaders.getHeaderString("X-Account-Id");
        if (accountIdHeader == null || accountIdHeader.isBlank()) {
            return Uni.createFrom().failure(
                    DomainValidationException.requiredField("X-Account-Id")
            );
        }

        try {
            return Uni.createFrom().item(UUID.fromString(accountIdHeader.trim()));
        } catch (IllegalArgumentException ex) {
            return Uni.createFrom().failure(
                    DomainValidationException.invalidField(
                            "X-Account-Id",
                            accountIdHeader,
                            "must be a valid UUID"
                    )
            );
        }
    }

    @Override
    public Uni<String> getIp() {
        String forwardedFor = httpHeaders.getHeaderString("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String clientIp = forwardedFor.split(",")[0].trim();
            if (!clientIp.isBlank()) {
                return Uni.createFrom().item(clientIp);
            }
        }
        String realIp = httpHeaders.getHeaderString("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return Uni.createFrom().item(realIp.trim());
        }

        return Uni.createFrom().item("unknown");
    }*/

    @Override
    public ExecutionContext getExecutionContext() {
        return executionContext;
    }
}
