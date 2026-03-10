package store.purchase.infrastructure.api.dto;

import store.purchase.application.result.PlayNotification;

public record ReceivePubsubMessageRequest(
        String messageId,
        String subscription,
        PlayNotification notificationData
) {
}
