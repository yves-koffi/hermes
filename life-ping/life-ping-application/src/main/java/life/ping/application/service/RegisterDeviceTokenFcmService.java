package life.ping.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import life.ping.application.usecase.RegisterDeviceTokenFcmUseCase;

@ApplicationScoped
public class RegisterDeviceTokenFcmService implements RegisterDeviceTokenFcmUseCase {

    @Override
    public void handle(Input in) {
        // TODO: implement use case
    }
}
