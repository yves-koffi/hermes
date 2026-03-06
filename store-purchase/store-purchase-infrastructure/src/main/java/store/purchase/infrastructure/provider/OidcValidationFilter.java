package store.purchase.infrastructure.provider;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory; // Ou JacksonFactory si vous préférez Jackson pour cette partie
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Provider // Indique à JAX-RS que c'est un fournisseur (comme un filtre)
@ApplicationScoped // Rendre le filtre un bean géré par CDI
public class OidcValidationFilter implements ContainerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(OidcValidationFilter.class);

    @ConfigProperty(name = "quarkus.oidc.audience")
    String audience; // Injecte l'audience configurée dans application.properties

    private GoogleIdTokenVerifier verifier;

    // Cache pour les jetons déjà validés afin d'éviter la revérification coûteuse
    private LoadingCache<String, Boolean> validatedTokensCache;

    @PostConstruct
    void init() {
        if (audience == null || audience.trim().isEmpty()) {
            throw new IllegalStateException("La propriété 'quarkus.oidc.audience' doit être configurée dans application.properties.");
        }

        // Initialisation du vérificateur de jetons Google OIDC
        // Utilise NetHttpTransport (pour les requêtes HTTP) et GsonFactory (pour le parsing JSON)
        // L'audience doit correspondre à l'URL de votre service
        verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(audience))
                .build();

        // Initialisation du cache pour les jetons validés
        validatedTokensCache = CacheBuilder.newBuilder()
                .maximumSize(1000) // Nombre max de jetons à cacher
                .expireAfterWrite(5, TimeUnit.MINUTES) // Expiration après 5 minutes
                .build(new CacheLoader<String, Boolean>() {
                    @Override
                    public Boolean load(String token) {
                        // Cette méthode est appelée si le jeton n'est pas dans le cache
                        // La validation réelle se fait dans la méthode filter()
                        // Pour le cache, on se base sur la validation réussie ou échouée
                        return false; // Valeur par défaut, non utilisée directement
                    }
                });

        logger.info("OidcValidationFilter initialisé avec l'audience: {}", audience);
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Appliquer ce filtre uniquement à votre endpoint Pub/Sub push
        // Vous pouvez ajuster le chemin si votre endpoint push n'est pas exactement "/pubsub/push"
        if (!requestContext.getUriInfo().getPath().contains("/pubsub/push")) {
            return;
        }

        String authorizationHeader = requestContext.getHeaderString("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            logger.warn("Tentative d'accès non autorisé sur '{}': En-tête Authorization manquant ou mal formaté.", requestContext.getUriInfo().getPath());
            abortWithUnauthorized(requestContext, "Missing or malformed Authorization header");
            return;
        }

        String idToken = authorizationHeader.substring(7); // Supprime "Bearer "

        try {
            // Vérifier si le jeton est déjà dans le cache comme validé
            if (validatedTokensCache.asMap().containsKey(idToken) && validatedTokensCache.get(idToken)) {
                logger.debug("Jeton OIDC trouvé dans le cache et validé pour la requête sur '{}'.", requestContext.getUriInfo().getPath());
                return; // Le jeton est valide, laisser passer la requête
            }

            // Valider le jeton OIDC
            GoogleIdToken googleIdToken = verifier.verify(idToken);

            if (googleIdToken == null) {
                logger.warn("Validation du jeton OIDC échouée (null) pour la requête sur '{}'.", requestContext.getUriInfo().getPath());
                throw new SecurityException("Jeton OIDC invalide.");
            }

            // Optionnel : Vérifier l'émetteur (issuer)
            String issuer = googleIdToken.getPayload().getIssuer();
            if (!"https://accounts.google.com".equals(issuer) && !"https://securetoken.google.com".equals(issuer)) {
                logger.warn("Émetteur OIDC invalide pour la requête sur '{}': {}", requestContext.getUriInfo().getPath(), issuer);
                throw new SecurityException("Émetteur OIDC invalide.");
            }

            // Si toutes les validations passent, ajouter au cache comme valide
            validatedTokensCache.put(idToken, true);
            logger.debug("Jeton OIDC validé avec succès pour la requête sur '{}'.", requestContext.getUriInfo().getPath());

        } catch (GeneralSecurityException | IOException | ExecutionException e) {
            logger.error("Échec de la validation du jeton OIDC pour la requête sur '{}': {}", requestContext.getUriInfo().getPath(), e.getMessage());
            abortWithUnauthorized(requestContext, "Invalid OIDC token: " + e.getMessage());
        } catch (Exception e) { // Attraper d'autres exceptions imprévues
            logger.error("Erreur inattendue lors de la validation du jeton OIDC pour la requête sur '{}': {}", requestContext.getUriInfo().getPath(), e.getMessage());
            abortWithUnauthorized(requestContext, "Unexpected error during OIDC token validation");
        }
    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext, String message) {
        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                .entity("Unauthorized: " + message)
                .build());
    }
}