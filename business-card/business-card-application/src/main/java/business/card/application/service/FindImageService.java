package business.card.application.service;

import business.card.application.usecase.FindImageUseCase;
import business.card.application.spi.BusinessCardRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import shared.domain.exception.DomainNotFoundException;

import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class FindImageService implements FindImageUseCase {

    @Inject
    BusinessCardRepository repository;

    @Override
    public Uni<String> execute(UUID businessCardId) {
        return repository.findById(businessCardId)
                .flatMap(cardOpt -> {
                    if (cardOpt.isEmpty() || cardOpt.get().avatarUrl() == null) {
                        return Uni.createFrom().failure(
                                new DomainNotFoundException(
                                        "BUSINESS_CARD_IMAGE_NOT_FOUND",
                                        "business_card.image.not_found",
                                        Map.of("id", businessCardId)
                                )
                        );
                    }
                    return Uni.createFrom().item(cardOpt.get().avatarUrl());
                });
    }
}
