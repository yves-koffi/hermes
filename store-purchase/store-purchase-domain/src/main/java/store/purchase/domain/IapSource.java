package store.purchase.domain;

public enum IapSource {
    GOOGLE_PLAY("google_play"),
    APP_STORE("app_store");

    private final String value; // Champ pour stocker la valeur associée

    // Constructeur de l'enum
    IapSource(String value) {
        this.value = value;
    }

    // Méthode pour obtenir la valeur associée
    public String getValue() {
        return value;
    }

    public static IapSource fromValue(String text) {
        for (IapSource b : IapSource.values()) {
            if (b.value.equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}