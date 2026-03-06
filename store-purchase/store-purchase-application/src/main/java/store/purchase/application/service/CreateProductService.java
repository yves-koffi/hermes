package store.purchase.application.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import store.purchase.application.command.CreateProductCommand;
import store.purchase.application.result.ProduitDetails;
import store.purchase.application.spi.ProductRepository;
import store.purchase.application.usecase.CreateProductUseCase;

@ApplicationScoped
public class CreateProductService implements CreateProductUseCase {
    private static final Logger LOG = Logger.getLogger(CreateProductService.class);

    @Inject
    ProductRepository productRepository;

    @Override
    public Uni<ProduitDetails> handle(CreateProductCommand command) {
        LOG.infof("Creating product with externalProductId=%s", command.externalProductId());
        return productRepository.save(command)
                .invoke(savedProduct -> LOG.debugf("Product created with externalProductId=%s", savedProduct.externalProductId()))
                .onFailure().invoke(throwable ->
                        LOG.errorf(throwable, "Failed to create product with externalProductId=%s", command.externalProductId()));
    }
}
