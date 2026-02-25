package life.ping.infrastructure.scheduler;


import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Déclencher l'alerte si missed_streak >= max_missed_days (anti-dup via emergency_alert).
 * Optionnellement applique une règle de "grâce" basée sur last_seen_at.
 */
@ApplicationScoped
public class TriggerEmergencyAlertScheduler {

    @Scheduled(
            identity = "trigger-emergency-alert",
            cron = "0 0 * * * ?",
            concurrentExecution = Scheduled.ConcurrentExecution.SKIP
    )
    public void run() {

    }
}
