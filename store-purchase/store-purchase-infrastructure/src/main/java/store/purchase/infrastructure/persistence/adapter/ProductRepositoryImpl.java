package store.purchase.infrastructure.persistence.adapter;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import store.purchase.application.command.CreateProductCommand;
import store.purchase.application.command.UpdateProductCommand;
import store.purchase.application.result.ProductSummary;
import store.purchase.application.result.ProduitDetails;
import store.purchase.application.spi.ProductRepository;
import store.purchase.infrastructure.persistence.mapper.ProductMapper;
import store.purchase.infrastructure.persistence.repository.ProductEntityRepository;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ProductRepositoryImpl implements ProductRepository {
    private final ProductEntityRepository productEntityRepository;
    private final ProductMapper productMapper;

    @Inject
    public ProductRepositoryImpl(
            ProductEntityRepository productEntityRepository,
            ProductMapper productMapper
    ) {
        this.productEntityRepository = productEntityRepository;
        this.productMapper = productMapper;
    }

    @Override
    public Uni<ProduitDetails> save(CreateProductCommand command) {
        return productEntityRepository.find("externalProductId", command.externalProductId()).firstResult()
                .chain(existingEntity -> {
                    if (existingEntity == null) {
                        var newEntity = productMapper.toEntity(command);
                        return productEntityRepository.persistAndFlush(newEntity)
                                .replaceWith(newEntity);
                    }

                    productMapper.updateEntity(
                            new UpdateProductCommand(
                                    existingEntity.getId(),
                                    command.type(),
                                    command.platform(),
                                    command.externalProductId(),
                                    command.packageName(),
                                    command.bundleId(),
                                    command.appAppleId()
                            ),
                            existingEntity
                    );
                    return productEntityRepository.flush().replaceWith(existingEntity);
                })
                .map(productMapper::toDetails);
    }

    @Override
    public Uni<Optional<ProduitDetails>> findByExternalProductId(String externalProductId) {
        return productEntityRepository.find("externalProductId", externalProductId).firstResult()
                .map(entity -> entity == null
                        ? Optional.empty()
                        : Optional.of(productMapper.toDetails(entity)));
    }

    @Override
    public Uni<List<ProductSummary>> findAll() {
        return productEntityRepository.listAll()
                .map(entities -> entities.stream().map(productMapper::toSummary).toList());
    }

    @Override
    public Uni<ProduitDetails> update(UpdateProductCommand command) {
        return productEntityRepository.find("externalProductId", command.externalProductId()).firstResult()
                .chain(existingEntity -> {
                    if (existingEntity == null) {
                        return Uni.createFrom().failure(
                                new IllegalArgumentException(
                                        "Product not found for externalProductId: " + command.externalProductId()
                                )
                        );
                    }

                    productMapper.updateEntity(command, existingEntity);
                    return productEntityRepository.flush().replaceWith(existingEntity);
                })
                .map(productMapper::toDetails);
    }
}
