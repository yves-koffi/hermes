package store.purchase.infrastructure.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Represents a request body for verifying a purchase.
 */
public record VerifyPurchaseRequest(
        @NotBlank(message = "userId must not be blank")
        String userId,

        @NotBlank(message = "source must not be blank")
        String source,

        @NotBlank(message = "productId must not be blank")
        String productId,

        @NotBlank(message = "verificationData must not be blank")
        String verificationData
) {}