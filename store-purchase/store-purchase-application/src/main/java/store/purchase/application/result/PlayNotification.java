package store.purchase.application.result;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PlayNotification(
        String version,
        String packageName,
        Long eventTimeMillis,
        OneTimeProductNotification oneTimeProductNotification,
        SubscriptionNotification subscriptionNotification,
        VoidedPurchaseNotification voidedPurchaseNotification,
        TestNotification testNotification
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OneTimeProductNotification(
            String version,
            OneTimeProductNotificationType notificationType,
            String purchaseToken,
            String sku
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SubscriptionNotification(
            String version,
            SubscriptionNotificationType notificationType,
            String purchaseToken
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record VoidedPurchaseNotification(
            String purchaseToken,
            String orderId,
            VoidedPurchaseNotificationProductType productType,
            VoidedPurchaseNotificationRefundType refundType
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TestNotification(String version) {
    }

    public enum VoidedPurchaseNotificationProductType {
        PRODUCT_TYPE_SUBSCRIPTION(1),
        PRODUCT_TYPE_ONE_TIME(2),
        UNKNOWN(-1); // Pour gérer les valeurs non reconnues

        private final int value;

        VoidedPurchaseNotificationProductType(int value) {
            this.value = value;
        }

        @JsonValue
        public int getValue() {
            return value;
        }

        @JsonCreator
        public static VoidedPurchaseNotificationProductType fromValue(int value) {
            Optional<VoidedPurchaseNotificationProductType> type = Arrays.stream(VoidedPurchaseNotificationProductType.values())
                    .filter(e -> e.value == value)
                    .findFirst();
            return type.orElse(UNKNOWN);
        }
    }

    public enum VoidedPurchaseNotificationRefundType {
        REFUND_TYPE_FULL_REFUND(1),
        REFUND_TYPE_QUANTITY_BASED_PARTIAL_REFUND(2),
        UNKNOWN(-1); // Pour gérer les valeurs non reconnues

        private final int value;

        VoidedPurchaseNotificationRefundType(int value) {
            this.value = value;
        }

        @JsonValue
        public int getValue() {
            return value;
        }

        @JsonCreator
        public static VoidedPurchaseNotificationRefundType fromValue(int value) {
            Optional<VoidedPurchaseNotificationRefundType> type = Arrays.stream(VoidedPurchaseNotificationRefundType.values())
                    .filter(e -> e.value == value)
                    .findFirst();
            return type.orElse(UNKNOWN);
        }
    }

    public enum OneTimeProductNotificationType {
        ONE_TIME_PRODUCT_PURCHASED(1),
        ONE_TIME_PRODUCT_CANCELED(2),
        UNKNOWN(-1); // Pour gérer les valeurs non reconnues

        private final int value;

        OneTimeProductNotificationType(int value) {
            this.value = value;
        }

        @JsonValue
        public int getValue() {
            return value;
        }

        @JsonCreator
        public static OneTimeProductNotificationType fromValue(int value) {
            Optional<OneTimeProductNotificationType> type = Arrays.stream(OneTimeProductNotificationType.values())
                    .filter(e -> e.value == value)
                    .findFirst();
            return type.orElse(UNKNOWN);
        }
    }

    public enum SubscriptionNotificationType {
        SUBSCRIPTION_RECOVERED(1),
        SUBSCRIPTION_RENEWED(2),
        SUBSCRIPTION_CANCELED(3),
        SUBSCRIPTION_PURCHASED(4),
        SUBSCRIPTION_ON_HOLD(5),
        SUBSCRIPTION_IN_GRACE_PERIOD(6),
        SUBSCRIPTION_RESTARTED(7),
        SUBSCRIPTION_PRICE_CHANGE_CONFIRMED(8), // OBSOLÈTE
        SUBSCRIPTION_DEFERRED(9),
        SUBSCRIPTION_PAUSED(10),
        SUBSCRIPTION_PAUSE_SCHEDULE_CHANGED(11),
        SUBSCRIPTION_REVOKED(12),
        SUBSCRIPTION_EXPIRED(13),
        SUBSCRIPTION_ITEMS_CHANGED(17),
        SUBSCRIPTION_CANCELLATION_SCHEDULED(18),
        SUBSCRIPTION_PRICE_CHANGE_UPDATED(19),
        SUBSCRIPTION_PENDING_PURCHASE_CANCELED(20),
        SUBSCRIPTION_PRICE_STEP_UP_CONSENT_UPDATED(22),

        UNKNOWN(-1); // Pour gérer les valeurs non reconnues

        private final int value;

        SubscriptionNotificationType(int value) {
            this.value = value;
        }

        @JsonValue
        public int getValue() {
            return value;
        }

        @JsonCreator
        public static SubscriptionNotificationType fromValue(int value) {
            Optional<SubscriptionNotificationType> type = Arrays.stream(SubscriptionNotificationType.values())
                    .filter(e -> e.value == value)
                    .findFirst();
            return type.orElse(UNKNOWN);
        }
    }
}
