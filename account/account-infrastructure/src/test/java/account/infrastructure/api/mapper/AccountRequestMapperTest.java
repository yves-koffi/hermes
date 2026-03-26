package account.infrastructure.api.mapper;

import account.application.command.RegisterCommand;
import account.infrastructure.api.dto.RegisterRequestDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountRequestMapperTest {

    @Test
    void should_map_required_verify_email_from_register_request() {
        AccountRequestMapper mapper = new AccountRequestMapper();

        RegisterCommand command = mapper.toCommand(new RegisterRequestDto(
                "John",
                "john@example.com",
                "secret",
                "+225",
                "0700000000",
                true
        ));

        assertEquals("John", command.name());
        assertEquals("john@example.com", command.email());
        assertTrue(command.requiredVerifyEmail());
    }
}
