package store.purchase.application.usecase;

import io.smallrye.mutiny.Uni;
import store.purchase.application.command.CreateProductCommand;
import store.purchase.application.result.ProduitDetails;

public interface CreateProductUseCase {
    Uni<ProduitDetails> handle(CreateProductCommand command);
}
