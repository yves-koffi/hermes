package image.server.application.usecase;

import io.smallrye.mutiny.Uni;

public interface PreviewImageUseCase {
    Uni<byte[]> execute(
            String subPath,
            Integer w,
            Integer h,
            boolean crop,
            int q,
            String fmt,
            boolean upscale
    );
}
