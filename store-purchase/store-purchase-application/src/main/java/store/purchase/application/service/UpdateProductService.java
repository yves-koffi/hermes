package store.purchase.application.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import store.purchase.application.command.UpdateProductCommand;
import store.purchase.application.result.ProduitDetails;
import store.purchase.application.spi.ProductRepository;
import store.purchase.application.usecase.UpdateProductUseCase;

@ApplicationScoped
public class UpdateProductService implements UpdateProductUseCase {
    private static final Logger LOG = Logger.getLogger(UpdateProductService.class);

    @Inject
    ProductRepository productRepository;

    @Override
    public Uni<ProduitDetails> handle(UpdateProductCommand command) {
        LOG.infof("Updating product with externalProductId=%s", command.externalProductId());
        return productRepository.update(command)
                .invoke(updatedProduct -> LOG.debugf("Product updated with externalProductId=%s", updatedProduct.externalProductId()))
                .onFailure().invoke(throwable ->
                        LOG.errorf(throwable, "Failed to update product with externalProductId=%s", command.externalProductId()));
    }
}
