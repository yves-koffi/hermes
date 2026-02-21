package shared.domain.exception;

import jakarta.ws.rs.core.Response;

import java.util.Map;

/**
 * Exception de domaine spécifique aux échecs d'authentification.
 * <p>
 * Correspond généralement aux statuts HTTP :
 * <ul>
 *     <li><b>401 Unauthorized</b> : Identifiants invalides, token manquant/expiré.</li>
 *     <li><b>403 Forbidden</b> : Compte désactivé, banni ou non vérifié (selon politique de sécurité).</li>
 * </ul>
 */
public class AuthenticationException extends DomainException {

    // --- Codes Stables (Standardisés pour les clients API) ---
    public static final String CODE_INVALID_CREDENTIALS = "AUTH_INVALID_CREDENTIALS";
    public static final String CODE_ACCOUNT_DISABLED = "AUTH_ACCOUNT_DISABLED";
    public static final String CODE_ACCOUNT_LOCKED = "AUTH_ACCOUNT_LOCKED";
    public static final String CODE_ACCOUNT_UNVERIFIED = "AUTH_ACCOUNT_UNVERIFIED";

    /**
     * Constructeur générique.
     */
    protected AuthenticationException(Response.StatusType status, String code, String messageKey, String message, Map<String, Object> context) {
        super(status, code, messageKey, message, context, null);
    }

    // --- Factory Methods (Pour une utilisation fluide dans le Service) ---

    /**
     * Cas : Mot de passe incorrect ou utilisateur inconnu.
     * <p>
     * Note de sécurité : On utilise souvent le même message pour "User not found"
     * et "Wrong password" pour éviter l'énumération des utilisateurs.
     */
    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException(
                Response.Status.UNAUTHORIZED,
                CODE_INVALID_CREDENTIALS,
                "auth.invalid_credentials",
                "Identifiants incorrects",
                Map.of() // Pas de contexte pour éviter de fuiter des infos sensibles
        );
    }

    /**
     * Cas : Le compte existe mais le booléen `isActive` est à false (ou softDeleted).
     */
    public static AuthenticationException accountDisabled(String username) {
        return new AuthenticationException(
                Response.Status.FORBIDDEN,
                CODE_ACCOUNT_DISABLED,
                "auth.account_disabled",
                "Le compte est désactivé",
                Map.of("username", username) // Contexte utile pour les logs/support
        );
    }

    /**
     * Cas : Le compte est bloqué (ex: trop de tentatives échouées).
     */
    public static AuthenticationException accountLocked(String username, long lockoutDurationSeconds) {
        return new AuthenticationException(
                Response.Status.FORBIDDEN,
                CODE_ACCOUNT_LOCKED,
                "auth.account_locked",
                "Le compte est temporairement verrouillé",
                Map.of(
                        "username", username,
                        "retryAfterSeconds", lockoutDurationSeconds
                )
        );
    }
    /**
     * Cas : L'inscription est faite mais l'email n'a pas été validé.
     */
    public static AuthenticationException accountUnverified(String username) {
        return new AuthenticationException(
                Response.Status.FORBIDDEN,
                CODE_ACCOUNT_UNVERIFIED,
                "auth.account_unverified",
                "L'adresse email du compte n'a pas encore été vérifiée",
                Map.of("username", username)
        );
    }
}
