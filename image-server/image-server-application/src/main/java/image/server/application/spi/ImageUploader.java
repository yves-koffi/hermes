package image.server.application.spi;

import io.smallrye.mutiny.Uni;
import org.jboss.resteasy.reactive.multipart.FileUpload;


public interface ImageUploader {
     Uni<String> upload(
            FileUpload file,
            String folder
    );
    Uni<byte[]>  serveImage(
            String subPath,
            Integer w,
            Integer h,
            boolean crop,
            int q,
            String fmt,
            boolean upscale
    );
    Uni<Void> deleteImage(String subPath);
}
