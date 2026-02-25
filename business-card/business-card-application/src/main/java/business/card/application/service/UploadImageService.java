package business.card.application.service;

import business.card.application.usecase.UploadImageUseCase;
import business.card.domain.model.BusinessCard;
import business.card.application.spi.BusinessCardRepository;
import image.server.application.spi.ImageUploader;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import shared.domain.exception.DomainNotFoundException;

import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class UploadImageService implements UploadImageUseCase {

    @Inject
    BusinessCardRepository repository;
    @Inject
    ImageUploader imageUploader;

    @Override
    public Uni<String> execute(
            FileUpload file,
            String folder,
            UUID businessCardId
    ) {
        return imageUploader.upload(
                file,
                folder
        ).flatMap(path -> repository.findById(businessCardId)
                .flatMap(cardOpt -> {
                    if (cardOpt.isEmpty()) {
                        return Uni.createFrom().failure(
                                new DomainNotFoundException(
                                        "BUSINESS_CARD_NOT_FOUND",
                                        "business_card.not_found",
                                        Map.of("id",businessCardId)
                                )
                        );
                    }
                    return repository.save(getBusinessCard(path, cardOpt.get()))
                            .map(BusinessCard::avatarUrl);
                }));

    }

    private static BusinessCard getBusinessCard(String path, BusinessCard cardOpt) {
        return new BusinessCard(
                cardOpt.id(),
                cardOpt.uid(),
                cardOpt.accountId(),
                cardOpt.raw(),
                cardOpt.type(),
                path,
                cardOpt.softDeletedAt(),
                cardOpt.saveAt(),
                cardOpt.createdAt(),
                cardOpt.updatedAt()
        );
    }
}
