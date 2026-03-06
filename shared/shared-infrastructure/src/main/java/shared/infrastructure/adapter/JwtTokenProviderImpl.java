package shared.infrastructure.adapter;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claims;
import shared.application.spi.JwtTokenProvider;
import shared.domain.model.TokenType;
import shared.infrastructure.TokenValidityProperties;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@RequestScoped
public class JwtTokenProviderImpl implements JwtTokenProvider {

    @Inject
    TokenValidityProperties properties;

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;


    @Override
    public String generateAccessToken(
            String subjectId,
            String userId,
            List<String> roles,
            Long expiresIn
    ) {
        Long expires = expiresIn == null ? properties.accessTokenValidity() : expiresIn;
        return Jwt.issuer(issuer)
                .upn(subjectId)
                .claim(Claims.kid, userId)
                .claim("type", TokenType.ACCESS_TOKEN.value)
                .groups(new HashSet<>(roles))
                .expiresIn(expires)
                .sign();
    }

    @Override
    public String generateRefreshToken(
            String hashToken,
            UUID userId,
            Long expiresIn
    ) {
        Long expires = expiresIn == null ? properties.accessTokenValidity() : expiresIn;
        return Jwt.issuer(issuer)
                .upn(hashToken)
                .claim(Claims.kid, userId)
                .claim("type", TokenType.REFRESH_TOKEN.value)
                .groups(new HashSet<>(List.of("REFRESH_TOKEN")))
                .expiresIn(expires)
                .sign();
    }
}
