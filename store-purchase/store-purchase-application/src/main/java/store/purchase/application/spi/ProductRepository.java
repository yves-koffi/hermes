package store.purchase.application.spi;

import io.smallrye.mutiny.Uni;
import store.purchase.application.command.CreateProductCommand;
import store.purchase.application.command.UpdateProductCommand;
import store.purchase.application.result.ProductSummary;
import store.purchase.application.result.ProduitDetails;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Uni<ProduitDetails> save(CreateProductCommand command);

    Uni<Optional<ProduitDetails>> findByExternalProductId(String externalProductId);

    Uni<List<ProductSummary>> findAll();

    Uni<ProduitDetails> update(UpdateProductCommand command);
}
