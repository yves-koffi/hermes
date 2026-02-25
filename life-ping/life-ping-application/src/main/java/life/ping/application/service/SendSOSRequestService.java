package life.ping.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import life.ping.application.usecase.SendSOSRequestUseCase;

@ApplicationScoped
public class SendSOSRequestService implements SendSOSRequestUseCase {

    @Override
    public Output handle(Input in) {
        // TODO: implement use case
        return null;
    }
}
