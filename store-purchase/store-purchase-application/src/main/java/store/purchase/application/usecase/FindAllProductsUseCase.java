package store.purchase.application.usecase;

import io.smallrye.mutiny.Uni;
import store.purchase.application.result.ProductSummary;

import java.util.List;

public interface FindAllProductsUseCase {
    Uni<List<ProductSummary>> handle();
}
