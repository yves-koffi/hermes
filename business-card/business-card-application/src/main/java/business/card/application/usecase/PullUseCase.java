package business.card.application.usecase;

import business.card.application.result.BusinessCardDetails;
import business.card.domain.model.RecordFilter;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface PullUseCase {
    default Uni<List<BusinessCardDetails>> execute() {
        return execute(RecordFilter.RECORD);
    }

    Uni<List<BusinessCardDetails>> execute(RecordFilter filter);
}
