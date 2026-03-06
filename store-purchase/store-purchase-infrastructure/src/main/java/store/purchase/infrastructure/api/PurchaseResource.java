package store.purchase.infrastructure.api;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;
import store.purchase.application.result.ProduitDetails;
import store.purchase.application.usecase.FindProductByExternalProductIdUseCase;
import store.purchase.application.usecase.NonSubscriptionUseCase;
import store.purchase.application.usecase.SubscriptionUseCase;
import store.purchase.application.usecase.VerifyPurchaseUseCase;
import store.purchase.domain.ProductData;
import store.purchase.infrastructure.api.dto.PurchaseRequest;
import store.purchase.infrastructure.api.dto.VerifyPurchaseRequest;

@Path("/purchases")
@ApplicationScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PurchaseResource {
    private final SubscriptionUseCase subscriptionUseCase;
    private final NonSubscriptionUseCase nonSubscriptionUseCase;
    private final VerifyPurchaseUseCase verifyPurchaseUseCase;
    private final FindProductByExternalProductIdUseCase findProductByExternalProductIdUseCase;

    @Inject
    public PurchaseResource(
            SubscriptionUseCase subscriptionUseCase,
            NonSubscriptionUseCase nonSubscriptionUseCase,
            VerifyPurchaseUseCase verifyPurchaseUseCase,
            FindProductByExternalProductIdUseCase findProductByExternalProductIdUseCase
    ) {
        this.subscriptionUseCase = subscriptionUseCase;
        this.nonSubscriptionUseCase = nonSubscriptionUseCase;
        this.verifyPurchaseUseCase = verifyPurchaseUseCase;
        this.findProductByExternalProductIdUseCase = findProductByExternalProductIdUseCase;
    }

    @POST
    @Path("/subscription")
    public Uni<RestResponse<Boolean>> subscriptionPurchase(@Valid PurchaseRequest request) {
        return resolveProduct(request.productId())
                .chain(productData -> subscriptionUseCase.handle(
                        request.userId(),
                        productData,
                        request.verificationData()
                )).map(RestResponse::ok);
    }

    @POST
    @Path("/non-subscription")
    public Uni<RestResponse<Boolean>> nonSubscriptionPurchases(
            @Valid PurchaseRequest request
    ) {
        return resolveProduct(request.productId())
                .chain(productData -> nonSubscriptionUseCase.handle(
                        request.userId(),
                        productData,
                        request.verificationData()
                )).map(RestResponse::ok);
    }

    @POST
    @Path("/verify")
    public Uni<RestResponse<Boolean>> verifyPurchase(
            @Valid VerifyPurchaseRequest request
    ) {
        return resolveProduct(request.productId())
                .chain(productData -> verifyPurchaseUseCase.handle(
                        request.userId(),
                        productData,
                        request.verificationData()
                )).map(RestResponse::ok);
    }

    private Uni<ProductData> resolveProduct(String externalProductId) {
        return findProductByExternalProductIdUseCase.handle(externalProductId)
                .chain(product -> product
                        .map(details -> Uni.createFrom().item(toProductData(details)))
                        .orElseGet(() -> Uni.createFrom().failure(
                                new NotFoundException("Product not found for externalProductId: " + externalProductId)
                        )));
    }

    private ProductData toProductData(ProduitDetails details) {
        return new ProductData(
                details.id(),
                details.type(),
                details.platform(),
                details.externalProductId(),
                details.packageName(),
                details.bundleId(),
                details.appAppleId()
        );
    }
}
