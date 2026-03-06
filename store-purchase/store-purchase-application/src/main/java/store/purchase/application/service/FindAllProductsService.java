package store.purchase.application.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import store.purchase.application.result.ProductSummary;
import store.purchase.application.spi.ProductRepository;
import store.purchase.application.usecase.FindAllProductsUseCase;

import java.util.List;

@ApplicationScoped
public class FindAllProductsService implements FindAllProductsUseCase {
    private static final Logger LOG = Logger.getLogger(FindAllProductsService.class);

    @Inject
    ProductRepository productRepository;

    @Override
    public Uni<List<ProductSummary>> handle() {
        LOG.info("Fetching all products");
        return productRepository.findAll()
                .invoke(products -> LOG.debugf("Fetched %d products", products.size()))
                .onFailure().invoke(throwable -> LOG.error("Failed to fetch all products", throwable));
    }
}
