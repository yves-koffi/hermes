package shared.infrastructure;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "jwt")
public interface TokenValidityProperties {
    Long accessTokenValidity();

    Long refreshTokenValidity();
}