package business.card.application.service;

import business.card.application.command.BusinessCardCommand;
import business.card.application.result.BusinessCardDetails;
import business.card.domain.model.BusinessCardPatch;
import business.card.application.spi.BusinessCardRepository;
import business.card.application.spi.CurrentAccountProvider;
import business.card.application.usecase.PushUseCase;
import business.card.domain.model.BusinessCard;
import business.card.domain.model.BusinessCardStatus;
import business.card.domain.model.BusinessCardType;
import com.fasterxml.jackson.databind.node.TextNode;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PushService implements PushUseCase {

    @Inject
    BusinessCardRepository businessCardRepository;
    @Inject
    CurrentAccountProvider currentAccountProvider;

    @Override
    public Uni<Void> execute(List<BusinessCardCommand> commands) {
        return apply(commands);
    }

    public Uni<Void> apply(List<BusinessCardCommand> commands) {
        if (commands == null || commands.isEmpty()) {
            return Uni.createFrom().voidItem();
        }

        return Multi.createFrom().iterable(commands)
                .onItem().transformToUniAndConcatenate(this::applyOne)
                .collect().asList().replaceWithVoid();
    }

    public Uni<Optional<BusinessCardDetails>> apply(BusinessCardCommand cmd) {
        return applyOne(cmd);
    }

    private Uni<Optional<BusinessCardDetails>> applyOne(BusinessCardCommand cmd) {
        if (cmd == null || cmd.uid() == null || cmd.uid().isBlank() || cmd.status() == null) {
            return Uni.createFrom().item(Optional.empty());
        }

        return switch (cmd.status()) {
            case INSERT -> handleInsert(cmd);
            case UPDATE -> handleUpdate(cmd);
            case BIN -> handleBin(cmd);
            case DESTROY -> handleDestroy(cmd);
        };
    }

    private Uni<Optional<BusinessCardDetails>> handleInsert(BusinessCardCommand cmd) {
        return businessCardRepository.findByUid(cmd.uid())
                .flatMap(existing -> {
                    if (existing.isPresent()) {
                        BusinessCard cardToSave = mergeForInsert(existing.get(), cmd);
                        return businessCardRepository.save(cardToSave)
                                .map(saved -> Optional.of(toDetails(saved, BusinessCardStatus.INSERT)));
                    }
                    return currentAccountProvider.getCurrentAccountId()
                            .flatMap(accountId -> businessCardRepository.save(buildNewCard(cmd, accountId)))
                            .map(saved -> Optional.of(toDetails(saved, BusinessCardStatus.INSERT)));
                });
    }

    private Uni<Optional<BusinessCardDetails>> handleUpdate(BusinessCardCommand cmd) {
        BusinessCardPatch patch = buildPatch(cmd);
        return businessCardRepository.updateMainByUidPartial(cmd.uid(), patch)
                .flatMap(updated -> {
                    if (updated == 0) {
                        return Uni.createFrom().item(Optional.empty());
                    }
                    return businessCardRepository.findMainByUid(cmd.uid())
                            .map(existing -> existing.map(card -> toDetails(card, BusinessCardStatus.UPDATE)));
                });
    }

    private Uni<Optional<BusinessCardDetails>> handleBin(BusinessCardCommand cmd) {
        if (cmd.bin() == null) {
            return Uni.createFrom().item(Optional.empty());
        }
        OffsetDateTime deletedAt = cmd.bin() == 1 ? OffsetDateTime.now(ZoneOffset.UTC) : null;
        return businessCardRepository.updateNonMainSoftDeletedByUid(cmd.uid(), deletedAt)
                .map(ignored -> Optional.empty());
    }

    private Uni<Optional<BusinessCardDetails>> handleDestroy(BusinessCardCommand cmd) {
        return businessCardRepository.updateNonMainSoftDeletedByUid(cmd.uid(), OffsetDateTime.now(ZoneOffset.UTC))
                .map(ignored -> Optional.empty());
    }

    private BusinessCard mergeForInsert(BusinessCard existing, BusinessCardCommand cmd) {
        OffsetDateTime softDeletedAt = resolveSoftDeletedAt(existing.softDeletedAt(), cmd.bin());
        return new BusinessCard(
                existing.id(),
                cmd.uid(),
                existing.accountId(),
                parseRaw(cmd.raw(), existing.raw()),
                parseType(cmd.type(), existing.type()),
                existing.avatarUrl(),
                softDeletedAt,
                parseSaveAt(cmd.saveAt(), existing.saveAt()),
                existing.createdAt(),
                LocalDateTime.now(ZoneOffset.UTC)
        );
    }

    private BusinessCard buildNewCard(BusinessCardCommand cmd, UUID accountId) {
        OffsetDateTime softDeletedAt = resolveSoftDeletedAt(null, cmd.bin());
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        return new BusinessCard(
                UUID.randomUUID(),
                cmd.uid(),
                accountId,
                parseRaw(cmd.raw(), null),
                parseType(cmd.type(), BusinessCardType.MAIN),
                null,
                softDeletedAt,
                parseSaveAt(cmd.saveAt(), now),
                now,
                now
        );
    }

    private BusinessCardPatch buildPatch(BusinessCardCommand cmd) {
        return new BusinessCardPatch(
                cmd.raw() != null ? TextNode.valueOf(cmd.raw()) : null,
                parseTypeNullable(cmd.type()),
                cmd.saveAt() != null ? parseSaveAt(cmd.saveAt(), null) : null
        );
    }

    private OffsetDateTime resolveSoftDeletedAt(OffsetDateTime existing, Integer bin) {
        if (bin == null) {
            return existing;
        }
        return bin == 1 ? OffsetDateTime.now(ZoneOffset.UTC) : null;
    }

    private com.fasterxml.jackson.databind.JsonNode parseRaw(String value, com.fasterxml.jackson.databind.JsonNode fallback) {
        if (value == null) {
            return fallback;
        }
        return TextNode.valueOf(value);
    }

    private BusinessCardType parseType(String value, BusinessCardType fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return BusinessCardType.valueOf(value.toUpperCase());
    }

    private BusinessCardType parseTypeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return BusinessCardType.valueOf(value.toUpperCase());
    }

    private LocalDateTime parseSaveAt(Long value, LocalDateTime fallback) {
        if (value == null) {
            return fallback;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(value), ZoneOffset.UTC);
    }

    private BusinessCardDetails toDetails(BusinessCard card, BusinessCardStatus status) {
        return new BusinessCardDetails(
                card.id(),
                card.avatarUrl(),
                card.raw(),
                card.type() != null ? card.type().name().toLowerCase() : null,
                card.isDeleted(),
                card.saveAt() != null ? card.saveAt().toEpochSecond(ZoneOffset.UTC) : null,
                card.uid(),
                status != null ? status.name().toLowerCase() : null
        );
    }
}
