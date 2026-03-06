package store.purchase.infrastructure.scheduler;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Heartbeat (mise à jour last_seen_at + retour du status).envoyer un notification push
 */
@ApplicationScoped
public class PullPurchaseScheduler {


    @Scheduled(
            identity = "pull-purchase-scheduler",
            cron = "0 0 * * * ?",
            concurrentExecution = Scheduled.ConcurrentExecution.PROCEED
    )
    public void run() {

    }
}
