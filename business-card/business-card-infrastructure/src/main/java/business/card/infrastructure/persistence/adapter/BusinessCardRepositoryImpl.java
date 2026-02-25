package business.card.infrastructure.persistence.adapter;

import business.card.domain.model.BusinessCard;
import business.card.domain.model.BusinessCardType;
import business.card.domain.model.BusinessCardPatch;
import business.card.application.spi.BusinessCardRepository;
import business.card.infrastructure.persistence.entity.BusinessCardEntity;
import business.card.infrastructure.persistence.mapper.BusinessCardMapper;
import business.card.infrastructure.persistence.repository.BusinessCardEntityRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class BusinessCardRepositoryImpl implements BusinessCardRepository {

    @Inject
    BusinessCardEntityRepository businessCardEntityRepository;
    @Inject
    BusinessCardMapper businessCardMapper;

    @Override
    public Uni<BusinessCard> save(BusinessCard businessCard) {
        BusinessCardEntity entity = businessCardMapper.toEntity(businessCard);
        return businessCardEntityRepository.getSession()
                .flatMap(session -> session.merge(entity))
                .map(businessCardMapper::toDomain);
    }

    @Override
    public Uni<Optional<BusinessCard>> findById(UUID id) {
        return businessCardEntityRepository.findById(id)
                .map(entity -> Optional.ofNullable(businessCardMapper.toDomain(entity)));
    }

    @Override
    public Uni<Optional<BusinessCard>> findByUid(String uid) {
        return businessCardEntityRepository.find("uid", uid).firstResult()
                .map(entity -> Optional.ofNullable(businessCardMapper.toDomain(entity)));
    }

    @Override
    public Uni<Optional<BusinessCard>> findMainByUid(String uid) {
        return businessCardEntityRepository.find("uid = ?1 and type = ?2", uid, BusinessCardType.MAIN)
                .firstResult()
                .map(entity -> Optional.ofNullable(businessCardMapper.toDomain(entity)));
    }

    @Override
    public Uni<List<BusinessCard>> findByAccountId(UUID accountId) {
        return businessCardEntityRepository.find("accountId", accountId).list()
                .map(entities -> entities.stream().map(businessCardMapper::toDomain).toList());
    }

    @Override
    public Uni<List<BusinessCard>> findAll() {
        return businessCardEntityRepository.listAll()
                .map(entities -> entities.stream().map(businessCardMapper::toDomain).toList());
    }

    @Override
    public Uni<Integer> updateMainByUidPartial(String uid, BusinessCardPatch patch) {
        List<Object> params = new ArrayList<>();
        StringBuilder setClause = new StringBuilder();
        int index = 1;

        if (patch.raw() != null) {
            setClause.append("raw = ?").append(index++);
            params.add(patch.raw());
        }
        if (patch.type() != null) {
            if (setClause.length() > 0) {
                setClause.append(", ");
            }
            setClause.append("type = ?").append(index++);
            params.add(patch.type());
        }
        if (patch.saveAt() != null) {
            if (setClause.length() > 0) {
                setClause.append(", ");
            }
            setClause.append("saveAt = ?").append(index++);
            params.add(patch.saveAt().atOffset(ZoneOffset.UTC));
        }

        if (setClause.length() == 0) {
            return Uni.createFrom().item(0);
        }

        params.add(uid);
        params.add(BusinessCardType.MAIN);

        String query = setClause + " where uid = ?" + index++ + " and type = ?" + index;
        return businessCardEntityRepository.update(query, params.toArray());
    }

    @Override
    public Uni<Integer> updateNonMainSoftDeletedByUid(String uid, OffsetDateTime softDeletedAt) {
        return businessCardEntityRepository.update(
                "softDeletedAt = ?1 where uid = ?2 and type <> ?3",
                softDeletedAt,
                uid,
                BusinessCardType.MAIN
        );
    }

    @Override
    public Uni<Void> deleteById(UUID id) {
        return businessCardEntityRepository.deleteById(id).replaceWithVoid();
    }
}
