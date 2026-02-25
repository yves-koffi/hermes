package image.server.application.service;

import image.server.application.spi.ImageUploader;
import image.server.application.usecase.UploadImageUseCase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@ApplicationScoped
public class UploadImageService implements UploadImageUseCase {

    @Inject
    ImageUploader imageUploader;

    @Override
    public Uni<String> execute(FileUpload file, String folder) {
        return imageUploader.upload(file, folder);
    }
}
