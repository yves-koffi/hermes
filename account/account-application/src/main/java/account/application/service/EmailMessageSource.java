package account.application.service;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

@ApplicationScoped
public class EmailMessageSource {

    private static final String BUNDLE_NAME = "email-messages";

    public String message(String key, String language) {
        try {
            return ResourceBundle.getBundle(BUNDLE_NAME, locale(language)).getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }

    private Locale locale(String language) {
        if ("en".equalsIgnoreCase(language)) {
            return Locale.ENGLISH;
        }
        return Locale.FRENCH;
    }
}
