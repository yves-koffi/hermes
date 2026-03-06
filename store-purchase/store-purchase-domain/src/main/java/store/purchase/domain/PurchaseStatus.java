package store.purchase.domain;

public enum PurchaseStatus {
    PENDING("pending", PurchaseType.SUBSCRIPTION, PurchaseType.NON_SUBSCRIPTION),
    COMPLETED("completed", PurchaseType.NON_SUBSCRIPTION),
    CANCELLED("cancelled", PurchaseType.NON_SUBSCRIPTION),
    ACTIVE("active", PurchaseType.SUBSCRIPTION),
    EXPIRED("expired", PurchaseType.SUBSCRIPTION);

    private final String value;
    private final PurchaseType[] supportedTypes;

    PurchaseStatus(String value, PurchaseType... supportedTypes) {
        this.value = value;
        this.supportedTypes = supportedTypes;
    }

    public String getValue() {
        return value;
    }

    public boolean supports(PurchaseType purchaseType) {
        for (PurchaseType supportedType : supportedTypes) {
            if (supportedType == purchaseType) {
                return true;
            }
        }
        return false;
    }

    public static PurchaseStatus fromValue(String text) {
        for (PurchaseStatus status : PurchaseStatus.values()) {
            if (status.value.equalsIgnoreCase(text)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}
