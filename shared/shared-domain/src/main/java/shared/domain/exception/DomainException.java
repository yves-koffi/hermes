package shared.domain.exception;

import io.quarkiverse.resteasy.problem.HttpProblem;
import jakarta.ws.rs.core.Response;

import java.util.Collections;
import java.util.Map;

/**
 * Base des exceptions de domaine (DDD) utilisées dans la couche « Domain ».
 * <p>
 * Cette classe transporte un triplé <b>(code, messageKey, context)</b> afin de séparer :
 * <ul>
 *   <li><b>code</b> — identifiant stable en SCREAMING_SNAKE_CASE
 *       (ex. {@code TRANSFER_INVALID_STATE}, {@code ALIAS_POLICY_DENIED}) pour
 *       l'interfaçage et l'analytics côté clients ;</li>
 *   <li><b>messageKey</b> — clé i18n (ex. {@code transfer.invalid_state}) destinée
 *       aux traducteurs/clients REST ;</li>
 *   <li><b>context</b> — carte de détails (clé/valeur) utile au debug, au support ou à l'UI
 *       (ex. {@code {"endToEndId":"...", "currentState":"INITIATED"}}).</li>
 * </ul>
 *
 * <h3>Recommandations d'usage</h3>
 * <ul>
 *   <li>Au niveau <i>domaine</i>, privilégier <b>(code + messageKey + context)</b> et laissez
 *       souvent <code>message</code> à {@code null}. La traduction finale sera faite au niveau API.</li>
 *   <li>Exposez ces exceptions via un mapper REST (ex. Problem+JSON) qui décidera du statut HTTP :
 *     <ul>
 *       <li>Validation : 422 Unprocessable Entity</li>
 *       <li>Politique/refus : 403 Forbidden</li>
 *       <li>Conflit/état/optimistic locking : 409 Conflict</li>
 *       <li>Idempotence : 409 Conflict (ou 422 selon politique)</li>
 *       <li>Ressource absente : 404 Not Found</li>
 *     </ul>
 *   </li>
 *   <li>Le champ <code>context</code> peut contenir des identifiants techniques (e2e, txId, policy, etc.).
 *       Évitez d'y mettre des données sensibles ou PII non nécessaires.</li>
 *   <li>Si possible, passez une carte <i>immuable</i> (ex. {@code Collections.unmodifiableMap(...)})
 *       pour préserver l'immutabilité logique.</li>
 * </ul>
 *
 * <h3>Arborescence typique</h3>
 * <p>
 * Spécialisez cette classe pour représenter des familles sémantiques :
 * <ul>
 *   <li>{@code DomainValidationException} (422)</li>
 *   <li>{@code DomainPolicyException} (403)</li>
 *   <li>{@code DomainConflictException} (409)</li>
 *   <li>{@code DomainIdempotencyException} (409)</li>
 * </ul>
 * </p>
 */
public abstract class DomainException extends HttpProblem {

    /**
     * Code stable, ex. {@code TRANSFER_INVALID_STATE}.
     */
    private final String code;

    /**
     * Clé i18n, ex. {@code transfer.invalid_state}.
     */
    private final String messageKey;

    /**
     * Contexte additionnel pour le debug et l'UI
     * (ex. {@code {"endToEndId":"...", "field":"amount"}}).
     */
    private final Map<String, Object> context;

    /**
     * Constructeur protégé utilisé par les exceptions concrètes.
     *
     * @param code       code stable (SCREAMING_SNAKE_CASE), ex. {@code TRANSFER_INVALID_STATE}
     * @param messageKey clé i18n, ex. {@code transfer.invalid_state}
     * @param message    message humain optionnel (souvent {@code null} côté domaine)
     * @param context    contexte additionnel (peut être {@code null} ou vide)
     * @param cause      cause technique (stack d'origine) éventuellement attachée
     */
    protected DomainException(
            Response.StatusType status,
            String code,
            String messageKey,
            String message,
            Map<String, Object> context,
            Throwable cause) {
        super(HttpProblem.builder()
                .withStatus(status)
                .withTitle(status.getReasonPhrase())
                .withDetail(message)
                .with("code", code)
                .with("messageKey", messageKey)
                .with("context", context == null ? Collections.emptyMap() : context));

        this.code = code;
        this.messageKey = messageKey;
        this.context = context == null ? Collections.emptyMap() : context;
        if (cause != null) {
            initCause(cause);
        }
    }

    /**
     * Retourne le code stable (SCREAMING_SNAKE_CASE).
     */
    public String getCode() {
        return code;
    }

    /**
     * Retourne la clé i18n (dot.case), ex. {@code transfer.invalid_state}.
     */
    public String getMessageKey() {
        return messageKey;
    }

    /**
     * Retourne le contexte additionnel (détails utiles pour la résolution de problème).
     */
    public Map<String, Object> getContext() {
        return context;
    }
}
