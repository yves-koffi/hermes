package image.server.infrastructure.adapter;

import image.server.infrastructure.config.ImageConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service de maintenance du cache d'images.
 * <p>
 * Il supprime les fichiers expirés selon la politique de rétention,
 * puis retire les répertoires devenus vides.
 */
@ApplicationScoped
public class CacheCleanupAdapter {
    private static final Logger LOG = Logger.getLogger(CacheCleanupAdapter.class);
    @Inject
    ImageConfig config;

    /**
     * Lance un cycle complet de nettoyage du cache.
     * <p>
     * Les fichiers dont la date de modification est antérieure au seuil
     * (`now - cacheDays`) sont supprimés.
     * Un bilan (supprimés / erreurs) est journalisé à la fin.
     */
    public void evictImageExpired(){
        Path cacheRoot = Path.of(config.cacheDir());

        if (!Files.exists(cacheRoot)) {
            LOG.debug("Cache directory does not exist, skipping cleanup.");
            return;
        }

        Instant cutoff = Instant.now().minus(config.cacheDays(), ChronoUnit.DAYS);
        AtomicInteger deleted = new AtomicInteger(0);
        AtomicInteger errors  = new AtomicInteger(0);

        try (var stream = Files.walk(cacheRoot)) {
            stream
                    .filter(Files::isRegularFile)
                    .filter(path -> isOlderThan(path, cutoff))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            deleted.incrementAndGet();
                            LOG.debugf("Deleted expired cache file: %s", path);
                        } catch (IOException e) {
                            errors.incrementAndGet();
                            LOG.warnf("Failed to delete cache file %s: %s", path, e.getMessage());
                        }
                    });

            // Supprimer les dossiers vides laissés après nettoyage
            deleteEmptyDirectories(cacheRoot);

        } catch (IOException e) {
            LOG.errorf("Cache cleanup failed: %s", e.getMessage());
        }

        LOG.infof("Cache cleanup done — %d file(s) deleted, %d error(s).",
                deleted.get(), errors.get());
    }

    /**
     * Indique si un fichier est plus ancien que la date limite.
     *
     * @param path fichier à évaluer
     * @param cutoff date limite d'expiration
     * @return true si le fichier est expiré
     */
    private boolean isOlderThan(Path path, Instant cutoff) {
        try {
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            return attrs.lastModifiedTime().toInstant().isBefore(cutoff);
        } catch (IOException e) {
            LOG.warnf("Cannot read attributes of %s: %s", path, e.getMessage());
            return false;
        }
    }

    /**
     * Parcourt l'arborescence en bottom-up et supprime les dossiers vides,
     * sauf la racine du cache elle-même.
     *
     * @param root racine du cache à conserver
     * @throws IOException si l'exploration du système de fichiers échoue
     */
    private void deleteEmptyDirectories(Path root) throws IOException {
        try (var stream = Files.walk(root)) {
            stream
                    .filter(Files::isDirectory)
                    .filter(path -> !path.equals(root))
                    .sorted((a, b) -> b.compareTo(a)) // bottom-up
                    .forEach(dir -> {
                        try {
                            if (isDirectoryEmpty(dir)) {
                                Files.delete(dir);
                                LOG.debugf("Removed empty cache directory: %s", dir);
                            }
                        } catch (IOException e) {
                            LOG.warnf("Cannot remove directory %s: %s", dir, e.getMessage());
                        }
                    });
        }
    }

    /**
     * Vérifie si un dossier ne contient aucune entrée.
     *
     * @param dir dossier à inspecter
     * @return true si le dossier est vide
     * @throws IOException si la lecture du dossier échoue
     */
    private boolean isDirectoryEmpty(Path dir) throws IOException {
        try (var entries = Files.newDirectoryStream(dir)) {
            return !entries.iterator().hasNext();
        }
    }
}
