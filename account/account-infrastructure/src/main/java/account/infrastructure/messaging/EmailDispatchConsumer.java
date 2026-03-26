package account.infrastructure.messaging;

import account.application.event.EmailDispatchEvent;
import account.domain.model.RenderedEmail;
import account.infrastructure.email.EmailContentFactory;
import account.domain.model.TokenType;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MailerName;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class EmailDispatchConsumer {

    private static final Logger LOGGER = Logger.getLogger(EmailDispatchConsumer.class);

    @Inject
    @MailerName("account")
    ReactiveMailer mailer;
    @Inject
    EmailContentFactory emailContentFactory;

    @ConsumeEvent(value = EmailDispatchEvent.ADDRESS)
    public Uni<Void> onEmailDispatch(JsonObject payload) {
        EmailDispatchEvent event = new EmailDispatchEvent(
                payload.getString("recipient"),
                TokenType.valueOf(payload.getString("tokenType")),
                payload.getString("language"),
                payload.getString("tokenValue")
        );

        try {
            RenderedEmail rendered = emailContentFactory.render(event);
            return mailer.send(Mail.withHtml(event.recipient(), rendered.subject(), rendered.html()))
                    .onFailure().invoke(error -> LOGGER.errorv(
                            error,
                            "event=email_dispatch_failed recipient={0} tokenType={1}",
                            event.recipient(),
                            event.tokenType()
                    ));
        } catch (RuntimeException error) {
            LOGGER.errorv(
                    error,
                    "event=email_dispatch_failed recipient={0} tokenType={1}",
                    event.recipient(),
                    event.tokenType()
            );
            return Uni.createFrom().voidItem();
        }
    }
}
