package store.purchase.application.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import store.purchase.application.result.ProduitDetails;
import store.purchase.application.spi.ProductRepository;
import store.purchase.application.usecase.FindProductByExternalProductIdUseCase;

import java.util.Optional;

@ApplicationScoped
public class FindProductByExternalProductIdService implements FindProductByExternalProductIdUseCase {
    private static final Logger LOG = Logger.getLogger(FindProductByExternalProductIdService.class);

    @Inject
    ProductRepository productRepository;

    @Override
    public Uni<Optional<ProduitDetails>> handle(String externalProductId) {
        LOG.infof("Fetching product with externalProductId=%s", externalProductId);
        return productRepository.findByExternalProductId(externalProductId)
                .invoke(product -> LOG.debugf(
                        "Product lookup for externalProductId=%s found=%s",
                        externalProductId,
                        product.isPresent()
                ))
                .onFailure().invoke(throwable ->
                        LOG.errorf(throwable, "Failed to fetch product with externalProductId=%s", externalProductId));
    }
}
