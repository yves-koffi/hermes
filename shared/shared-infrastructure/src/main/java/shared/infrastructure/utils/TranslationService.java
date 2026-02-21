package shared.infrastructure.utils;


import jakarta.enterprise.context.ApplicationScoped;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


@ApplicationScoped
public class TranslationService {

    private static final String BUNDLE_NAME = "messages";

    /**
     * Traduction simple selon une locale.
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
     * Traduction en utilisant la locale par défaut (souvent FR).
     */
    public String translate(String messageKey) {
        return translate(messageKey, Locale.forLanguageTag("fr"));
    }

    /**
     * Gère les locales null / incohérentes.
     */
    private Locale safeLocale(Locale locale) {
        return (locale == null) ? Locale.getDefault() : locale;
    }
}

