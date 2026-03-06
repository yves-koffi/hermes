package store.purchase.infrastructure.persistence.adapter;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import store.purchase.application.spi.PurchaseRepository;
import store.purchase.domain.Purchase;
import store.purchase.infrastructure.persistence.mapper.PurchaseMapper;
import store.purchase.infrastructure.persistence.repository.PurchaseEntityRepository;

import java.util.Optional;

@ApplicationScoped
public class PurchaseRepositoryImpl implements PurchaseRepository {
    private final PurchaseEntityRepository purchaseEntityRepository;
    private final PurchaseMapper purchaseMapper;

    @Inject
    public PurchaseRepositoryImpl(
            PurchaseEntityRepository purchaseEntityRepository,
            PurchaseMapper purchaseMapper
    ) {
        this.purchaseEntityRepository = purchaseEntityRepository;
        this.purchaseMapper = purchaseMapper;
    }

    @Override
    public Uni<Purchase> createOrUpdate(Purchase purchase) {
        return purchaseEntityRepository.find("orderId", purchase.orderId()).firstResult()
                .chain(existingEntity -> {
                    if (existingEntity == null) {
                        var newEntity = purchaseMapper.toEntity(purchase);
                        return purchaseEntityRepository.persistAndFlush(newEntity)
                                .replaceWith(newEntity);
                    }

                    purchaseMapper.updateEntity(purchase, existingEntity);
                    return purchaseEntityRepository.flush().replaceWith(existingEntity);
                })
                .map(purchaseMapper::toDomain);
    }

    @Override
    public Uni<Optional<Purchase>> findByOrderId(String orderId) {
        return purchaseEntityRepository.find("orderId", orderId).firstResult()
                .map(entity -> entity == null
                        ? Optional.empty()
                        : Optional.of(purchaseMapper.toDomain(entity)));
    }
}
