package business.card.application.service;

import business.card.application.result.BusinessCardDetails;
import business.card.application.spi.CurrentAccountProvider;
import business.card.domain.model.RecordFilter;
import business.card.application.usecase.PullUseCase;
import business.card.domain.model.BusinessCard;
import business.card.application.spi.BusinessCardRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.ZoneOffset;
import java.util.List;

@ApplicationScoped
public class PullService implements PullUseCase {

    @Inject
    BusinessCardRepository repository;
    @Inject
    CurrentAccountProvider currentAccountProvider;


    @Override
    public Uni<List<BusinessCardDetails>> execute(RecordFilter filter) {
        RecordFilter effectiveFilter = filter == null ? RecordFilter.RECORD : filter;
        return currentAccountProvider.getCurrentAccountId()
                .flatMap(repository::findByAccountId)
                .map(cards -> cards.stream()
                        .filter(card -> matchesFilter(card, effectiveFilter))
                        .map(this::toDetails)
                        .toList());
    }

    private boolean matchesFilter(BusinessCard card, RecordFilter filter) {
        return switch (filter) {
            case RECORD -> !card.isDeleted();
            case DELETED -> card.isDeleted();
            case ALL -> true;
        };
    }

    private BusinessCardDetails toDetails(BusinessCard card) {
        return new BusinessCardDetails(
                card.id(),
                card.avatarUrl(),
                card.raw(),
                card.type() != null ? card.type().name().toLowerCase() : null,
                card.isDeleted(),
                card.saveAt() != null ? card.saveAt().toEpochSecond(ZoneOffset.UTC) : null,
                card.uid(),
                "sync"
        );
    }
}
