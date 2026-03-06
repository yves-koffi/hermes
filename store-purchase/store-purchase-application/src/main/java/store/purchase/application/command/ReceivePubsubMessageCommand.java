package store.purchase.application.command;

import store.purchase.application.result.PlayNotification;

public record ReceivePubsubMessageCommand(
        String messageId,
        String subscription,
        PlayNotification notificationData
) {
}
