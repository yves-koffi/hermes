package image.server.application.usecase;

import io.smallrye.mutiny.Uni;
import org.jboss.resteasy.reactive.multipart.FileUpload;

public interface UploadImageUseCase {
    Uni<String> execute(
            FileUpload file,
            String folder
    );
}
