package image.server.application.service;

import image.server.application.spi.ImageUploader;
import image.server.application.usecase.DeleteImageUseCase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class DeleteImageService implements DeleteImageUseCase {

    @Inject
    ImageUploader imageUploader;

    @Override
    public Uni<Void> execute(String subPath) {
        return imageUploader.deleteImage(subPath);
    }
}
