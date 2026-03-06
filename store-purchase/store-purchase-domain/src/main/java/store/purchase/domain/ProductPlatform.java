package store.purchase.domain;

public enum ProductPlatform {
    IOS("ios"),
    ANDROID("android"); // N'oubliez pas le point-virgule si vous avez des membres ou des méthodes après les constantes.

    private final String value; // Champ pour stocker la valeur de la chaîne

    // Constructeur de l'enum
    ProductPlatform(String value) {
        this.value = value;
    }

    // Méthode pour obtenir la valeur associée
    public String getValue() {
        return value;
    }

    // Optionnel: Méthode statique pour obtenir un ProductPlatform à partir de sa chaîne de valeur
    public static ProductPlatform fromValue(String text) {
        for (ProductPlatform platform : ProductPlatform.values()) {
            if (platform.value.equalsIgnoreCase(text)) {
                return platform;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}