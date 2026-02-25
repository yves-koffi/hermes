package life.ping.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import life.ping.application.usecase.GetUserStatusUseCase;

@ApplicationScoped
public class GetUserStatusService implements GetUserStatusUseCase {

    @Override
    public Output handle(Input in) {
        // TODO: implement use case
        return null;
    }
}
