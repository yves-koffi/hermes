package business.card.application.spi;

import business.card.domain.model.BusinessCard;
import io.smallrye.mutiny.Uni;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BusinessCardRepository {

    Uni<BusinessCard> save(BusinessCard businessCard);

    Uni<Optional<BusinessCard>> findById(UUID id);

    Uni<Optional<BusinessCard>> findByUid(String uid);

    Uni<Optional<BusinessCard>> findMainByUid(String uid);

    Uni<List<BusinessCard>> findByAccountId(UUID accountId);

    Uni<List<BusinessCard>> findAll();

    Uni<Integer> updateMainByUidPartial(String uid, business.card.domain.model.BusinessCardPatch patch);

    Uni<Integer> updateNonMainSoftDeletedByUid(String uid, OffsetDateTime softDeletedAt);

    Uni<Void> deleteById(UUID id);
}
