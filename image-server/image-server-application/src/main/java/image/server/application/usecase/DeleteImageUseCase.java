package image.server.application.usecase;

import io.smallrye.mutiny.Uni;

public interface DeleteImageUseCase {
    Uni<Void> execute(String subPath);
}
