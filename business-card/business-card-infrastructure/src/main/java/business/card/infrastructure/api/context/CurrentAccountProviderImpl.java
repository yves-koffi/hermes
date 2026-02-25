package business.card.infrastructure.api.context;

import business.card.application.spi.CurrentAccountProvider;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import shared.domain.exception.DomainValidationException;

import java.util.UUID;

@RequestScoped
public class CurrentAccountProviderImpl implements CurrentAccountProvider {

    @Context
    HttpHeaders httpHeaders;

    @Override
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
}
