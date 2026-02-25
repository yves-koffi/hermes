package image.server.infrastructure.scheduler;


import image.server.infrastructure.adapter.CacheCleanupAdapter;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CacheCleanupScheduler {

    @Inject
    CacheCleanupAdapter cacheCleanupAdapter;

    /**
     * Tourne toutes les nuits à minuit.
     * Supprime les fichiers du dossier cache/ dont la date de dernière
     * modification est plus ancienne que image.cache-days jours.
     * Le cron est configurable via :
     * image.cleanup-cron=0 0 * * * ?   (défaut : toutes les heures)
     */
    @Scheduled(
            identity = "image-cache-cleanup",
            cron = "${image.cleanup-cron:0 0 * * * ?}",
            concurrentExecution = Scheduled.ConcurrentExecution.SKIP
    )
    public void run() {
        cacheCleanupAdapter.evictImageExpired();
    }
}
