package life.ping.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import life.ping.application.usecase.CheckInUseCase;

@ApplicationScoped
public class CheckInService implements CheckInUseCase {

    @Override
    public Output handle(Input in) {
        // TODO: implement use case
        return null;
    }
}
