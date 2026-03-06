package store.purchase.application.usecase;

import io.smallrye.mutiny.Uni;
import store.purchase.application.result.ProduitDetails;

import java.util.Optional;

public interface FindProductByExternalProductIdUseCase {
    Uni<Optional<ProduitDetails>> handle(String externalProductId);
}
