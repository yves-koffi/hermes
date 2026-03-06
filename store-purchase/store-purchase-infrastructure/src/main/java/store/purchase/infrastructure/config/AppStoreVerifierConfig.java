package store.purchase.infrastructure.config;

import com.apple.itunes.storekit.model.Environment;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@ConfigMapping(prefix = "store.purchase.app-store")
public interface AppStoreVerifierConfig {
    @WithDefault("SANDBOX")
    String environment();

    @WithDefault("false")
    boolean enableOnlineChecks();

    @WithDefault("true")
    boolean enableApiLookup();

    @WithDefault("")
    String issuerId();

    @WithDefault("")
    String keyId();

    @WithDefault("")
    String privateKeyPath();

    @WithDefault("")
    @WithName("paths")
    String getRawPaths();

    default Environment parsedEnvironment() {
        try {
            return Environment.valueOf(environment().trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid App Store environment: " + environment(), exception);
        }
    }
    /**
     * Méthode par défaut pour obtenir l'ensemble des chemins de certificats.
     * @return Un ensemble non modifiable de chaînes de caractères représentant les chemins.
     */
    default Set<String> getCertificatePaths() {
        return Arrays.stream(getRawPaths().split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank()).collect(Collectors.toUnmodifiableSet());
    }

}
