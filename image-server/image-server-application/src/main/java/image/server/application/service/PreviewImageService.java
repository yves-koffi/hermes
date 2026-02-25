package image.server.application.service;

import image.server.application.spi.ImageUploader;
import image.server.application.usecase.PreviewImageUseCase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PreviewImageService implements PreviewImageUseCase {

    @Inject
    ImageUploader imageUploader;

    @Override
    public Uni<byte[]> execute(String subPath, Integer w, Integer h, boolean crop, int q, String fmt, boolean upscale) {
        return imageUploader.serveImage(subPath, w, h, crop, q, fmt, upscale);
    }
}
