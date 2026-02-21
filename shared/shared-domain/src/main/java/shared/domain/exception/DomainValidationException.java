package shared.domain.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import java.util.*;

/**
 * 422 métier : violation d'une règle de validation du domaine.
 * <p>
 * Champs :
 * - code        : SCREAMING_SNAKE_CASE stable (ex. RTP_AMOUNT_REQUIRED)
 * - messageKey  : clé i18n (ex. rtp.amount.required)
 * - message     : message humain optionnel (souvent null côté domaine)
 * - context     : données utiles au debug/UI (ex. {"endToEndId":"...", "field":"amount"})
 * <p>
 * Recommandation : privilégier (code + messageKey + context) au niveau domaine.
 * Le mapper REST (Problem+JSON) traduira en HTTP 422.
 */
public class DomainValidationException extends DomainException {
    private static final Response.StatusType UNPROCESSABLE_ENTITY_STATUS = new Response.StatusType() {
        @Override
        public int getStatusCode() {
            return 422;
        }

        @Override
        public Response.Status.Family getFamily() {
            return Response.Status.Family.CLIENT_ERROR;
        }

        @Override
        public String getReasonPhrase() {
            return "Unprocessable Entity";
        }
    };

    /**
     * Constructeur verbeux (tous champs)
     */
    public DomainValidationException(String code, String messageKey, String message, Map<String, Object> context) {
        super(UNPROCESSABLE_ENTITY_STATUS, code, messageKey, message, context, null);
    }

    /**
     * Surcharge pratique sans message humain
     */
    public DomainValidationException(String code, String messageKey, Map<String, Object> context) {
        super(UNPROCESSABLE_ENTITY_STATUS, code, messageKey, null, context, null);
    }

    /**
     * Surcharge minimale (pas de contexte)
     */
    public DomainValidationException(String code, String messageKey) {
        super(UNPROCESSABLE_ENTITY_STATUS, code, messageKey, null, Collections.emptyMap(), null);
    }

    /**
     * Fabrique concise
     */
    public static DomainValidationException of(String code, String messageKey, Map<String, Object> context) {
        return new DomainValidationException(code, messageKey, context);
    }

    /* ==== (Optionnel) aides génériques pour les validations courantes ====
       NB: si tu utilises des enums de codes par BC (RtpCode, AliasCode, ...),
           préfère leurs codes dédiés plutôt que ces génériques. */

    /**
     * Champ requis manquant
     */
    public static DomainValidationException requiredField(String fieldName) {
        return new DomainValidationException(
                "VALIDATION_REQUIRED",
                "validation.required",
                Map.of("field", fieldName)
        );
    }

    /**
     * Champ invalide (valeur rejetée + raison)
     */
    public static DomainValidationException invalidField(String fieldName, Object rejectedValue, String reason) {
        return new DomainValidationException(
                "VALIDATION_INVALID",
                "validation.invalid",
                Map.of("field", fieldName, "rejected", rejectedValue, "reason", reason)
        );
    }

    /**
     * Construit une DomainValidationException à partir d'un ensemble de violations Bean Validation.
     * <p>
     * - code       : VALIDATION_CONSTRAINTS
     * - messageKey : validation.constraints
     * - message    : message de la première violation (pratique pour logs rapides)
     * - context    : { errors: [ {field, message, template, invalidValue}, ... ], errorCount, fields }
     */
    public static DomainValidationException fromViolations(Set<? extends ConstraintViolation<?>> violations) {
        if (violations == null || violations.isEmpty()) {
            return new DomainValidationException("VALIDATION_CONSTRAINTS", "validation.constraints");
        }

        // Liste détaillée des erreurs
        List<Map<String, Object>> errors = violations.stream()
                .map(v -> Map.<String, Object>of(
                        "field", propertyPath(v),
                        "message", safeMessage(v.getMessage()),
                        "template", v.getMessageTemplate(),
                        "invalidValue", safeValue(v.getInvalidValue())
                ))
                .toList();

        // Champs (unique) pour aide UI
        List<String> fields = errors.stream()
                .map(m -> Objects.toString(m.get("field"), ""))
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();

        Map<String, Object> context = new LinkedHashMap<>();
        context.put("errors", errors);
        context.put("errorCount", errors.size());
        context.put("fields", fields);

        // Message court = 1ère violation (utile pour logs/trace)
        String firstMsg = errors.isEmpty() ? null : Objects.toString(errors.getFirst().get("message"), null);

        return new DomainValidationException(
                "VALIDATION_CONSTRAINTS",
                "validation.constraints",
                firstMsg,
                context
        );
    }

    /**
     * Surcharge pratique depuis une ConstraintViolationException.
     */
    public static DomainValidationException fromViolations(ConstraintViolationException ex) {
        if (ex == null) {
            return new DomainValidationException("VALIDATION_CONSTRAINTS", "validation.constraints");
        }
        return fromViolations(ex.getConstraintViolations());
    }

    /* ==== Helpers privés ==== */

    private static String propertyPath(ConstraintViolation<?> v) {
        if (v == null || v.getPropertyPath() == null) return "";
        // Exemple: "handle.cmd.linkingKey.value" → garde le path tel quel
        return v.getPropertyPath().toString();
    }

    private static Object safeValue(Object invalidValue) {
        if (invalidValue == null) return null;
        try {
            // Évite d'inclure de gros objets ou secrets (à raffiner si besoin)
            String s = Objects.toString(invalidValue);
            return s.length() > 256 ? s.substring(0, 256) + "…" : s;
        } catch (Exception e) {
            return "<unprintable>";
        }
    }

    private static String safeMessage(String msg) {
        if (msg == null) return null;
        // Trim simple pour propreté
        String t = msg.trim();
        return t.isEmpty() ? null : t;
    }
}
