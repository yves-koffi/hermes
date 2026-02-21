package shared.domain.exception;

import jakarta.ws.rs.core.Response;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 409 métier : <b>conflit de ressource ou d'état</b>.
 * <p>
 * Utiliser lorsque l'action demandée entre en conflit avec :
 * <ul>
 *   <li>l'état courant d'une ressource (ex. transition illégale vs état actuel) ;</li>
 *   <li>une contrainte d'unicité (doublon) ;</li>
 *   <li>une version/jeton d'optimistic locking ;</li>
 *   <li>une dépendance bloquante (contrainte de relation).</li>
 * </ul>
 *
 * <p><b>Champs :</b></p>
 * <ul>
 *   <li><code>code</code>        : identifiant stable en SCREAMING_SNAKE_CASE (ex. <code>RESOURCE_ALREADY_EXISTS</code>)</li>
 *   <li><code>messageKey</code>  : clé i18n (ex. <code>conflict.resource.already_exists</code>)</li>
 *   <li><code>message</code>     : message humain optionnel (souvent null côté domaine)</li>
 *   <li><code>context</code>     : données utiles au debug/UI (ex. {"resource":"alias","id":"...","field":"..."})</li>
 * </ul>
 *
 * <p><b>Recommandation :</b> mapper cette exception en HTTP <code>409 Conflict</code>.
 * Au niveau domaine, privilégier (<code>code</code> + <code>messageKey</code> + <code>context</code>);
 * le mapper REST (Problem+JSON) choisira le statut et la traduction finale.</p>
 */

public class DomainConflictException extends DomainException {

    /** Constructeur verbeux (tous champs). */
    public DomainConflictException(String code, String messageKey, String message, Map<String, Object> context) {
        super(Response.Status.CONFLICT, code, messageKey, message, context, null);
    }

    /** Surcharge pratique sans message humain. */
    public DomainConflictException(String code, String messageKey, Map<String, Object> context) {
        super(Response.Status.CONFLICT, code, messageKey, null, context, null);
    }

    /** Surcharge minimale (pas de contexte). */
    public DomainConflictException(String code, String messageKey) {
        super(Response.Status.CONFLICT, code, messageKey, null, Collections.emptyMap(), null);
    }

    /** Fabrique concise. */
    public static DomainConflictException of(String code, String messageKey, Map<String, Object> context) {
        return new DomainConflictException(code, messageKey, context);
    }

    /* =====================================================================
       =                            Fabriques                               =
       ===================================================================== */

    /**
     * Conflit : la ressource existe déjà (unicité).
     * @param resource  type de ressource (ex. "alias", "transfer_plan")
     * @param id        identifiant ou valeur candidate (ex. clé d'alias)
     */
    public static DomainConflictException resourceAlreadyExists(String resource, String id) {
        Map<String, Object> ctx = new HashMap<>();
        if (resource != null) ctx.put("resource", resource);
        if (id != null)       ctx.put("id", id);
        return new DomainConflictException(
                "RESOURCE_ALREADY_EXISTS",
                "conflict.resource.already_exists",
                ctx
        );
    }

    /**
     * Conflit : violation d'une contrainte d'unicité (champ/valeur).
     * @param field  nom du champ (ex. "endToEndId", "localAlias")
     * @param value  valeur en conflit
     */
    public static DomainConflictException uniqueConstraintViolation(String field, Object value) {
        Map<String, Object> ctx = new HashMap<>();
        if (field != null) ctx.put("field", field);
        if (value != null) ctx.put("value", value);
        return new DomainConflictException(
                "UNIQUE_CONSTRAINT_VIOLATION",
                "conflict.unique_violation",
                ctx
        );
    }

    /**
     * Conflit de version (optimistic locking).
     * @param resource        type de ressource
     * @param id              identifiant de la ressource
     * @param expectedVersion version attendue
     * @param actualVersion   version courante en base
     */
    public static DomainConflictException versionConflict(String resource,
                                                          String id,
                                                          Number expectedVersion,
                                                          Number actualVersion) {
        Map<String, Object> ctx = new HashMap<>();
        if (resource != null)       ctx.put("resource", resource);
        if (id != null)             ctx.put("id", id);
        if (expectedVersion != null)ctx.put("expectedVersion", expectedVersion);
        if (actualVersion != null)  ctx.put("actualVersion", actualVersion);
        return new DomainConflictException(
                "VERSION_CONFLICT",
                "conflict.version",
                ctx
        );
    }

    /**
     * Conflit d'état (transition impossible).
     * @param resource     type de ressource
     * @param id           identifiant
     * @param currentState état courant
     * @param requiredState état attendu/nécessaire (optionnel)
     */
    public static DomainConflictException stateConflict(String resource,
                                                        String id,
                                                        String currentState,
                                                        String requiredState) {
        Map<String, Object> ctx = new HashMap<>();
        if (resource != null)     ctx.put("resource", resource);
        if (id != null)           ctx.put("id", id);
        if (currentState != null) ctx.put("currentState", currentState);
        if (requiredState != null)ctx.put("requiredState", requiredState);
        return new DomainConflictException(
                "STATE_CONFLICT",
                "conflict.state",
                ctx
        );
    }

    /**
     * Conflit dû à une dépendance (ex. ressource liée non compatible).
     * @param resource    type de ressource cible
     * @param id          identifiant
     * @param dependency  nom de la dépendance (ex. "transfer", "alias")
     * @param reason      raison synthétique (ex. "dependent_in_irrevocable_state")
     */
    public static DomainConflictException dependencyConflict(String resource,
                                                             String id,
                                                             String dependency,
                                                             String reason) {
        Map<String, Object> ctx = new HashMap<>();
        if (resource != null)   ctx.put("resource", resource);
        if (id != null)         ctx.put("id", id);
        if (dependency != null) ctx.put("dependency", dependency);
        if (reason != null)     ctx.put("reason", reason);
        return new DomainConflictException(
                "DEPENDENCY_CONFLICT",
                "conflict.dependency",
                ctx
        );
    }
}
