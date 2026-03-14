package account.application.service;

import account.application.command.CheckTokenCommand;
import account.application.result.CheckTokenDetails;
import account.application.spi.HashTokenRepository;
import account.application.usecase.CheckTokenUseCase;
import account.domain.model.HashToken;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;

@ApplicationScoped
public class CheckTokenService implements CheckTokenUseCase {

    @Inject
    HashTokenRepository hashTokenRepository;

    @Override
    public Uni<CheckTokenDetails> execute(CheckTokenCommand command) {
        if (command.token() == null || command.token().isBlank()) {
            return Uni.createFrom().item(new CheckTokenDetails(false));
        }

        String hashToken = hash(command.token());
        return hashTokenRepository.findByHashToken(hashToken)
                .map(tokenOpt -> new CheckTokenDetails(isValid(tokenOpt.orElse(null), command)));
    }

    private boolean isValid(HashToken token, CheckTokenCommand command) {
        if (token == null) {
            return false;
        }
        if (token.revokedAt() != null) {
            return false;
        }
        if (command.type() != null && token.tokenType() != command.type()) {
            return false;
        }
        return !token.expiryDate().isBefore(OffsetDateTime.now());
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
