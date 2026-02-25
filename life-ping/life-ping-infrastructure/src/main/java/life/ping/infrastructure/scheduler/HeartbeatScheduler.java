package life.ping.infrastructure.scheduler;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Heartbeat (mise à jour last_seen_at + retour du status).envoyer un notification push
 */
@ApplicationScoped
public class HeartbeatScheduler {


    @Scheduled(
            identity = "heartbeat-cache-cleanup",
            cron = "0 0 * * * ?",
            concurrentExecution = Scheduled.ConcurrentExecution.SKIP
    )
    public void run() {

    }
}
