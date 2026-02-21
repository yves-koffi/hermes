package shared.domain.exception;

import jakarta.ws.rs.core.Response;

import java.util.Collections;
import java.util.Map;

/**
 * 404 métier : ressource introuvable dans le domaine.
 * <p>
 * Champs :
 * - code        : SCREAMING_SNAKE_CASE (ex. ALIAS_NOT_FOUND)
 * - messageKey  : clé i18n (ex. alias.not_found)
 * - message     : message humain optionnel (exposé tel quel ou ignoré côté API)
 * - context     : carte de détails (ex. {"accountNumber":"...", "endToEndId":"..."})
 * <p>
 * Recommandation : en domaine, privilégier (code + messageKey + context).
 */
public class DomainNotFoundException extends DomainException {

    /** Constructeur verbeux (tous champs) */
    public DomainNotFoundException(String code, String messageKey, String message, Map<String, Object> context) {
        super(Response.Status.NOT_FOUND, code, messageKey, message, context, null);
    }

    /** Surcharge pratique sans message humain (utilise messageKey + context) */
    public DomainNotFoundException(String code, String messageKey, Map<String, Object> context) {
        super(Response.Status.NOT_FOUND, code, messageKey, null, context, null);
    }

    /** Surcharge minimale (pas de contexte) */
    public DomainNotFoundException(String code, String messageKey) {
        super(Response.Status.NOT_FOUND, code, messageKey, null, Collections.emptyMap(), null);
    }

    /** Fabrique utilitaire (syntaxe courte) */
    public static DomainNotFoundException of(String code, String messageKey, Map<String, Object> context) {
        return new DomainNotFoundException(code, messageKey, context);
    }
}
