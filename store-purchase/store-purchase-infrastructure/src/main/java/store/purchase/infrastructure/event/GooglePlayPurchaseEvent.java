package store.purchase.infrastructure.event;

import store.purchase.application.result.PlayNotification;

public record GooglePlayPurchaseEvent(
        String messageId,
        String subscription,
        PlayNotification notificationData
) {
}
