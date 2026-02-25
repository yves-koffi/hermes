package business.card.application.usecase;

import io.smallrye.mutiny.Uni;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.util.UUID;

public interface UploadImageUseCase {
    Uni<String> execute(FileUpload file, String folder, UUID businessCardId);
}
