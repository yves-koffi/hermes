package life.ping.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import life.ping.application.usecase.ConnectorUseCase;

@ApplicationScoped
public class ConnectorService implements ConnectorUseCase {

    @Override
    public Output handle(Input in) {
        // TODO: implement use case
        return null;
    }
}
