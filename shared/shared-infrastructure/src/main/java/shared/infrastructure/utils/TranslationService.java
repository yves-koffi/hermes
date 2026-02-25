package shared.infrastructure.utils;


import jakarta.enterprise.context.ApplicationScoped;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * Service de traduction i18n basé sur des {@link ResourceBundle}.
 * <p>
 * Les clés introuvables ne provoquent pas d'erreur bloquante:
 * la clé brute est renvoyée en fallback.
 */
@ApplicationScoped
public class TranslationService {

    private static final String BUNDLE_NAME = "messages";

    /**
     * Traduit une clé de message pour une locale donnée.
     *
     * @param messageKey clé i18n à résoudre
     * @param locale locale cible; une locale sûre est utilisée si null
     * @return la traduction résolue, ou la clé brute si absente du bundle
     */
    public String translate(String messageKey, Locale locale) {
        if (messageKey == null) {
            return null;
        }

        try {
            ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, safeLocale(locale));
            return bundle.getString(messageKey);
        } catch (MissingResourceException ex) {
            return messageKey; // fallback vers la clé brute
        }
    }

    /**
     * Traduit une clé en utilisant la locale applicative par défaut.
     *
     * @param messageKey clé i18n à résoudre
     * @return la traduction correspondante, ou la clé brute en fallback
     */
    public String translate(String messageKey) {
        return translate(messageKey, Locale.forLanguageTag("fr"));
    }

    /**
     * Normalise la locale pour éviter les accès ResourceBundle avec valeur nulle.
     *
     * @param locale locale demandée
     * @return la locale fournie, ou la locale JVM par défaut si null
     */
    private Locale safeLocale(Locale locale) {
        return (locale == null) ? Locale.getDefault() : locale;
    }
}
