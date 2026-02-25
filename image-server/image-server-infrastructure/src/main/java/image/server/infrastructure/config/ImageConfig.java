package image.server.infrastructure.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

import java.util.List;

@ConfigMapping(prefix = "image")
public interface ImageConfig {

    @WithName("upload-base-dir")
    @WithDefault("uploads")
    String uploadBaseDir();

    @WithName("cache-dir")
    @WithDefault(".cache")
    String cacheDir();

    @WithName("allowed-extensions")
    @WithDefault("jpg,jpeg,png,gif,webp,bmp")
    List<String> allowedExtensions();

    @WithName("max-file-size")
    @WithDefault("20971520")  // 20 MB
    long maxFileSize();

    @WithName("max-dimension")
    @WithDefault("5000")
    int maxDimension();

    @WithName("cache-days")
    @WithDefault("7")
    int cacheDays();

    @WithName("cleanup-cron")
    @WithDefault("0 0 * * * ?")
    String cleanupCron();
}
