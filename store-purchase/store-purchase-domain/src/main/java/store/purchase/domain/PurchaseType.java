package store.purchase.domain;

public enum PurchaseType {
    SUBSCRIPTION("subscription"),
    NON_SUBSCRIPTION("nonSubscription");

    private final String value;

    PurchaseType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PurchaseType fromValue(String text) {
        for (PurchaseType purchaseType : PurchaseType.values()) {
            if (purchaseType.value.equalsIgnoreCase(text)) {
                return purchaseType;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}
