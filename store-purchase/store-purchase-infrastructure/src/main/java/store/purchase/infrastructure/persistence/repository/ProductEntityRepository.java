package store.purchase.infrastructure.persistence.repository;

import com.apple.itunes.storekit.model.Platform;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import store.purchase.infrastructure.persistence.entity.ProductEntity;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ProductEntityRepository implements PanacheRepositoryBase<ProductEntity, UUID> {

    public Uni<Optional<ProductEntity>> findByAppleId(Long appleId) {
        return find("appAppleId = ?1 AND platform = ?", appleId, Platform.APPLE)
                .firstResult()
                .map(Optional::ofNullable);
    }
}
