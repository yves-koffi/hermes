package life.ping.infrastructure.notification;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import life.ping.application.spi.EmergencyEmailSender;
import life.ping.domain.model.Account;
import life.ping.domain.model.EmergencyContact;

import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class EmergencyEmailSenderImpl implements EmergencyEmailSender {
    private static final String SUBJECT = "SOS urgent - Life Ping";

    @Inject
    ReactiveMailer reactiveMailer;

    @Override
    public Uni<Void> sendSosAlert(Account account, List<EmergencyContact> contacts, Instant requestedAt) {
        List<Mail> mails = contacts.stream()
                .filter(contact -> contact.email() != null && !contact.email().isBlank())
                .map(contact -> Mail.withText(
                        contact.email(),
                        SUBJECT,
                        buildBody(account, contact, requestedAt)
                ))
                .toList();

        if (mails.isEmpty()) {
            return Uni.createFrom().voidItem();
        }

        return reactiveMailer.send(mails.toArray(Mail[]::new));
    }

    private String buildBody(Account account, EmergencyContact contact, Instant requestedAt) {
        String displayName = account.userName() == null || account.userName().isBlank()
                ? account.appUuid()
                : account.userName();

        return """
                Bonjour %s,

                Une demande SOS vient d'etre declenchee depuis Life Ping.

                Utilisateur: %s
                Identifiant: %s
                Fuseau horaire: %s
                Horodatage: %s

                Merci de contacter cette personne des que possible.
                """.formatted(
                contact.name() == null || contact.name().isBlank() ? "contact d'urgence" : contact.name(),
                displayName,
                account.id(),
                account.timezone(),
                requestedAt
        );
    }
}
