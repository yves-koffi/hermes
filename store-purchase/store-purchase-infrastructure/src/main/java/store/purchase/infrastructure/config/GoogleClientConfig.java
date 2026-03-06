package store.purchase.infrastructure.config;

import io.smallrye.config.ConfigMapping;

import java.util.Arrays;
import java.util.List;

@ConfigMapping(prefix = "google.client")
public interface GoogleClientConfig {
    String developerKey();

    String clientSecret();

    String clientId();

    String refreshToken();

    String accessToken();

    String serviceAccountPath();

    String scopes();

    String redirectUri();

    default List<String> scopesAsList() {
        if (scopes() == null || scopes().isBlank()) {
            return List.of();
        }

        return Arrays.stream(scopes().split(","))
                .map(String::trim)
                .filter(scope -> !scope.isEmpty())
                .toList();
    }
}
