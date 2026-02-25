package image.server.infrastructure.adapter;

import image.server.application.spi.ImageUploader;
import image.server.infrastructure.config.ImageConfig;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.NotSupportedException;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;


/**
 * Service métier pour la gestion des images du serveur.
 * <p>
 * Ce service centralise :
 * - la validation des entrées (taille, format, dimensions, chemins),
 * - l'écriture des originaux sur disque,
 * - la génération et la mise en cache de variantes redimensionnées,
 * - la suppression d'un original et de ses dérivés de cache.
 */
@ApplicationScoped
public class ImageServerAdapter implements ImageUploader {

    private static final Logger LOG = Logger.getLogger(ImageServerAdapter.class);

    @Inject
    ImageConfig config;

    private volatile Path uploadBaseDir;
    private volatile Path cacheBaseDir;

    // -------------------------------------------------------------------------
    // Initialisation
    // -------------------------------------------------------------------------

    /**
     * Crée les répertoires utilisés par le service si nécessaire.
     *
     * @throws IOException si la création des dossiers échoue
     */
    public void ensureDirectories() throws IOException {
        uploadBaseDir = ensureWritableDirectory(config.uploadBaseDir(), "uploads");
        cacheBaseDir = ensureWritableDirectory(config.cacheDir(), "cache");
    }

    // -------------------------------------------------------------------------
    // Upload
    // -------------------------------------------------------------------------

    /**
     * Valide puis stocke un fichier envoyé en multipart.
     * <p>
     * Règles appliquées :
     * - fichier obligatoire et non vide,
     * - taille maximale via la configuration,
     * - extension dans la liste autorisée (et présente),
     * - dossier cible assaini pour éviter la traversée de chemins.
     *
     * @param file   fichier multipart reçu
     * @param folder sous-dossier logique de destination (optionnel, peut être null)
     * @return réponse HTTP 201 avec l'URL relative en cas de succès, 400 sinon
     */
    @Override
    public Uni<String> upload(FileUpload file, String folder) {
        return runBlocking(() -> uploadBlocking(file, folder));
    }

    private String uploadBlocking(FileUpload file, String folder) throws IOException {

        // --- Validation du fichier ---
        if (file == null || file.size() == 0)
            throw new BadRequestException("File is empty.");
        if (file.size() > config.maxFileSize())
            throw new BadRequestException("File exceeds maximum size of " + (config.maxFileSize() / (1024 * 1024)) + " MB.");

        // CORRECTIF : getExtension() ne doit pas renvoyer "jpg" silencieusement
        String originalName = file.fileName();
        if (originalName == null || !originalName.contains("."))
            throw new BadRequestException("File has no extension.");

        String ext = getExtension(originalName);
        if (ext.isBlank())
            throw new BadRequestException("Unable to determine file extension.");
        if (!config.allowedExtensions().contains(ext.toLowerCase()))
            throw new BadRequestException("Unsupported type: " + ext + ". Allowed: " + config.allowedExtensions());

        // CORRECTIF : folder peut être null
        folder = sanitizeFolder(folder);
        if (folder == null)
            throw new BadRequestException("Invalid folder path.");

        // --- Stockage ---
        String storedName = UUID.randomUUID() + "." + ext.toLowerCase();
        Path uploadBase = uploadBaseDir();
        Path targetDir = folder.isBlank()
                ? uploadBase
                : uploadBase.resolve(folder).normalize();
        Path targetFile = targetDir.resolve(storedName);

        Files.createDirectories(targetDir);
        Files.copy(file.uploadedFile(), targetFile, StandardCopyOption.REPLACE_EXISTING);

        String url = folder.isBlank() ? storedName : folder + "/" + storedName;
        LOG.infof("Image uploaded: %s", url);

        return url;
    }

    // -------------------------------------------------------------------------
    // Serve
    // -------------------------------------------------------------------------

    /**
     * Sert l'image originale ou une version transformée.
     * <p>
     * Comportement :
     * - sans {@code w}/{@code h} : renvoie l'original,
     * - avec {@code w}/{@code h} : cherche en cache, sinon génère puis persiste la variante.
     * <p>
     * Sécurité :
     * - bloque les chemins invalides ({@code ..}),
     * - vérifie que les chemins résolus restent dans les dossiers autorisés.
     *
     * @param subPath chemin relatif de l'image (peut contenir des sous-dossiers)
     * @param w       largeur cible en pixels (optionnelle)
     * @param h       hauteur cible en pixels (optionnelle)
     * @param crop    active le recadrage centré si largeur et hauteur sont fournies
     * @param q       qualité de sortie (normalisée entre 1 et 100)
     * @param fmt     format de sortie demandé (normalisé en jpg/png/webp)
     * @param upscale autorise ou non l'agrandissement au-delà de la taille source
     * @return réponse HTTP binaire de l'image, avec headers de cache
     */
    @Override
    public Uni<byte[]> serveImage(
            String subPath,
            Integer w,
            Integer h,
            boolean crop,
            int q,
            String fmt,
            boolean upscale
    ) {
        return runBlocking(() -> serveImageBlocking(subPath, w, h, crop, q, fmt, upscale));
    }

    private byte[] serveImageBlocking(
            String subPath,
            Integer w,
            Integer h,
            boolean crop,
            int q,
            String fmt,
            boolean upscale
    ) throws IOException {

        // --- Validation des paramètres ---
        if (subPath == null || subPath.isBlank() || subPath.contains("..")) {
            LOG.warnf("Rejected path traversal attempt: %s", subPath);
            throw new BadRequestException();
        }
        if (w != null && (w < 1 || w > config.maxDimension()))
            throw new BadRequestException("Width out of range.");
        if (h != null && (h < 1 || h > config.maxDimension()))
            throw new BadRequestException("Height out of range.");

        q   = Math.min(100, Math.max(1, q));
        fmt = sanitizeFormat(fmt);

        boolean hasResize = (w != null || h != null);

        // pour éviter une IOException si le dossier n'existe pas encore.
        Path uploadBase = uploadBaseDir();

        // --- Servir l'original ---
        if (!hasResize) {
            Path target = uploadBase.resolve(subPath).normalize();
            if (!target.startsWith(uploadBase)) {
                LOG.warnf("Forbidden path access attempt: %s", subPath);
                throw new ForbiddenException();
            }
            if (!Files.exists(target) || !Files.isRegularFile(target))
                throw new NotFoundException();

            return Files.readAllBytes(target);
        }

        // --- Servir une variante redimensionnée ---
        Path cacheBase = cacheBaseDir();
        String requestedCacheKey = buildCacheKey(subPath, w, h, crop, q, fmt);
        Path requestedCachePath = cacheBase.resolve(requestedCacheKey).normalize();

        if (!requestedCachePath.startsWith(cacheBase)) {
            LOG.warnf("Forbidden cache path attempt: %s", requestedCacheKey);
            throw new ForbiddenException();
        }

        if (Files.exists(requestedCachePath)) {
            return Files.readAllBytes(requestedCachePath);
        }

        // Vérification de la source
        Path sourceTarget = uploadBase.resolve(subPath).normalize();
        if (!sourceTarget.startsWith(uploadBase)) {
            LOG.warnf("Forbidden source path attempt: %s", subPath);
            throw new ForbiddenException();
        }
        if (!Files.exists(sourceTarget) || !Files.isRegularFile(sourceTarget))
            throw new NotFoundException();

        Integer effectiveW = w;
        Integer effectiveH = h;


        // pour éviter qu'une seule dimension capped ne produise l'autre hors limite.
        if (!upscale) {
            BufferedImage sourceImage = ImageIO.read(sourceTarget.toFile());
            if (sourceImage == null) {
                LOG.errorf("Cannot read source image (unsupported format or corrupted): %s", subPath);
                throw new NotSupportedException();
            }
            int srcW = sourceImage.getWidth();
            int srcH = sourceImage.getHeight();
            if (effectiveW != null) effectiveW = Math.min(effectiveW, srcW);
            if (effectiveH != null) effectiveH = Math.min(effectiveH, srcH);
        }

        String effectiveCacheKey = buildCacheKey(subPath, effectiveW, effectiveH, crop, q, fmt);
        Path effectiveCachePath = cacheBase.resolve(effectiveCacheKey).normalize();
        if (!effectiveCachePath.startsWith(cacheBase)) {
            LOG.warnf("Forbidden effective cache path attempt: %s", effectiveCacheKey);
            throw new ForbiddenException();
        }
        if (!effectiveCachePath.equals(requestedCachePath) && Files.exists(effectiveCachePath)) {
            return Files.readAllBytes(effectiveCachePath);
        }

        // --- Génération de la variante ---
        ByteArrayOutputStream out     = new ByteArrayOutputStream();
        Thumbnails.Builder<File> builder = Thumbnails.of(sourceTarget.toFile());

        if (effectiveW != null && effectiveH != null) {
            builder.size(effectiveW, effectiveH);
            if (crop) builder.crop(Positions.CENTER);
            else      builder.keepAspectRatio(true);
        } else if (effectiveW != null) {
            builder.width(effectiveW);
        } else if (effectiveH != null) {
            builder.height(effectiveH);
        } else {
            builder.scale(1.0);
        }

        builder.outputQuality(q / 100.0)
                .outputFormat(fmt)
                .toOutputStream(out);

        byte[] imageBytes = out.toByteArray();

        Files.createDirectories(effectiveCachePath.getParent());
        Path tmp = effectiveCachePath.resolveSibling(effectiveCachePath.getFileName() + ".tmp");
        try {
            Files.write(tmp, imageBytes);
            Files.move(tmp, effectiveCachePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            Files.deleteIfExists(tmp);
            LOG.errorf(e, "Failed to write cache file: %s", effectiveCachePath);
            // On continue : on renvoie quand même l'image générée en mémoire.
        }

        LOG.debugf("Cache miss — variant generated: %s", effectiveCacheKey);
        return imageBytes;
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------

    /**
     * Supprime un original et toutes ses déclinaisons en cache.
     *
     * @param subPath chemin relatif de l'image à supprimer
     * @return 204 si suppression effectuée, 404 si absent, 400/403 en cas de chemin invalide
     */
    @Override
    public Uni<Void> deleteImage(String subPath) {
        return runBlocking(() -> {
            deleteImageBlocking(subPath);
            return null;
        });
    }

    private void deleteImageBlocking(String subPath) throws IOException {
        if (subPath == null || subPath.isBlank() || subPath.contains("..")) {
            LOG.warnf("Rejected invalid delete path: %s", subPath);
            throw new BadRequestException();
        }

        Path uploadBase = uploadBaseDir();
        Path target     = uploadBase.resolve(subPath).normalize();
        if (!target.startsWith(uploadBase)) {
            LOG.warnf("Forbidden delete attempt: %s", subPath);
            throw new ForbiddenException();
        }

        if (!Files.exists(target))
            throw new NotFoundException();

        Files.delete(target);
        LOG.infof("Original deleted: %s", subPath);

        // Purge du cache : préfixe déterministe identique à buildCacheKey
        String cachePrefix = buildCacheKeyBase(subPath);
        Path   cacheBase   = cacheBaseDir();
        if (Files.exists(cacheBase)) {
            try (var stream = Files.walk(cacheBase)) {
                stream.filter(Files::isRegularFile)
                        .filter(p -> p.getFileName().toString().startsWith(cachePrefix))
                        .forEach(p -> {
                            try {
                                Files.delete(p);
                                LOG.debugf("Cache variant deleted: %s", p);
                            } catch (IOException e) {
                                LOG.warnf(e, "Could not delete cache file: %s", p);
                            }
                        });
            }
        }
    }

    // -------------------------------------------------------------------------
    // Méthodes privées
    // -------------------------------------------------------------------------

    private <T> Uni<T> runBlocking(ThrowingSupplier<T> supplier) {
        return Uni.createFrom()
                .item(() -> {
                    try {
                        return supplier.get();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    private Path uploadBaseDir() throws IOException {
        if (uploadBaseDir == null) {
            ensureDirectories();
        }
        return uploadBaseDir;
    }

    private Path cacheBaseDir() throws IOException {
        if (cacheBaseDir == null) {
            ensureDirectories();
        }
        return cacheBaseDir;
    }

    private Path ensureWritableDirectory(String configuredDir, String fallbackSuffix) throws IOException {
        Path configured = Path.of(configuredDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(configured);
            return configured;
        } catch (IOException configuredError) {
            Path fallback = Path.of(
                    System.getProperty("java.io.tmpdir"),
                    "hermes-image-server",
                    fallbackSuffix
            ).toAbsolutePath().normalize();

            LOG.warnf(
                    configuredError,
                    "Directory not writable: %s. Falling back to: %s",
                    configured,
                    fallback
            );
            Files.createDirectories(fallback);
            return fallback;
        }
    }

    /**
     * Construit la partie "base" de la clé de cache (sans les paramètres de transformation).
     * <p>
     * Utilisée à la fois par {@link #buildCacheKey} et par {@link #deleteImage}
     * pour garantir que le préfixe de suppression est cohérent avec le nom des fichiers.
     *
     * @param subPath chemin logique de l'image source
     * @return base de la clé de cache
     */
    private String buildCacheKeyBase(String subPath) {
        String base = subPath.replace('/', '_');
        int lastDot = base.lastIndexOf('.');
        return lastDot > 0 ? base.substring(0, lastDot) : base;
    }

    /**
     * Construit une clé de cache déterministe pour une variante d'image.
     * <p>
     * La clé intègre le chemin logique et tous les paramètres de transformation
     * qui influencent le résultat.
     */
    private String buildCacheKey(String subPath, Integer w, Integer h, boolean crop, int q, String fmt) {
        return buildCacheKeyBase(subPath)
                + "_w" + (w != null ? w : "x")
                + "_h" + (h != null ? h : "x")
                + "_crop" + (crop ? "1" : "0")
                + "_q" + q
                + "." + fmt;
    }

    /**
     * Extrait l'extension d'un nom de fichier.
     * Renvoie une chaîne vide si aucune extension n'est détectable,
     * afin que l'appelant puisse rejeter le fichier explicitement.
     */
    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        String ext = filename.substring(filename.lastIndexOf('.') + 1);
        return ext.isBlank() ? "" : ext;
    }

    /**
     * Normalise et assainit le nom de sous-dossier.
     *
     * @param folder valeur brute (peut être null)
     * @return chaîne assainie, ou {@code null} si invalide (ex. path traversal)
     */
    private String sanitizeFolder(String folder) {
        if (folder == null) return "";
        folder = folder.replaceAll("^[/\\\\]+|[/\\\\]+$", "");
        if (folder.contains("..")) return null;
        return folder;
    }

    /**
     * Normalise le format de sortie accepté par le service.
     * Toute valeur non reconnue est rabattue vers {@code jpg}.
     */
    private String sanitizeFormat(String fmt) {
        if (fmt == null) return "jpg";
        return switch (fmt.toLowerCase()) {
            case "png"  -> "png";
            case "webp" -> "webp";
            default     -> "jpg";
        };
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}
