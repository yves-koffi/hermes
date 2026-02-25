package life.ping.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import life.ping.application.usecase.UpdateUserSettingsUseCase;

@ApplicationScoped
public class UpdateUserSettingsService implements UpdateUserSettingsUseCase {

    @Override
    public void handle(Input in) {
        // TODO: implement use case
    }
}
