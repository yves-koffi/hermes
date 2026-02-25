package life.ping.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import life.ping.application.usecase.SyncCheckInsUseCase;

@ApplicationScoped
public class SyncCheckInsService implements SyncCheckInsUseCase {

    @Override
    public Output handle(Input in) {
        // TODO: implement use case
        return null;
    }
}
