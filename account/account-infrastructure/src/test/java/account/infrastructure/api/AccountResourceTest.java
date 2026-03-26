package account.infrastructure.api;

import account.application.result.AccountDetails;
import account.application.result.AccountVerificationResult;
import account.application.result.AuthResult;
import account.application.result.CheckTokenResult;
import account.application.result.ForgetPasswordResult;
import account.application.result.PasswordResetResult;
import account.application.result.RegisterResult;
import account.application.result.VerifyCodeSentResult;
import account.domain.model.PhoneNumber;
import account.domain.model.Provider;
import account.domain.model.TokenType;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
@TestHTTPEndpoint(AccountResource.class)
class AccountResourceTest {

    @Test
    void login_should_return_auth_response() {
        AccountResourceTestDoubles.loginResponse = new AuthResult(
                UUID.fromString("33333333-3333-3333-3333-333333333333"),
                true,
                "access-token",
                "refresh-token",
                "Bearer",
                120,
                10080,
                OffsetDateTime.parse("2026-03-24T12:02:00Z"),
                OffsetDateTime.parse("2026-03-31T12:00:00Z")
        );

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "email": "john@example.com",
                          "password": "secret"
                        }
                        """)
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .body("accountId", equalTo("33333333-3333-3333-3333-333333333333"))
                .body("verified", equalTo(true))
                .body("accessToken", equalTo("access-token"))
                .body("refreshToken", equalTo("refresh-token"))
                .body("tokenType", equalTo("Bearer"))
                .body("accessExpiresAt", equalTo("2026-03-24T12:02:00Z"))
                .body("refreshExpiresAt", equalTo("2026-03-31T12:00:00Z"));
    }

    @Test
    void register_should_return_registration_details() {
        AccountResourceTestDoubles.registerResponse =
                new RegisterResult(
                        UUID.fromString("11111111-1111-1111-1111-111111111111"),
                        "john@example.com",
                        true,
                        "VERIFY_EMAIL"
                );

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "name": "John",
                          "email": "john@example.com",
                          "password": "secret",
                          "prefix": "+225",
                          "number": "0700000000"
                        }
                        """)
                .when()
                .post("/register")
                .then()
                .statusCode(200)
                .body("accountId", equalTo("11111111-1111-1111-1111-111111111111"))
                .body("email", equalTo("john@example.com"))
                .body("verificationRequired", equalTo(true))
                .body("nextStep", equalTo("VERIFY_EMAIL"));
    }

    @Test
    void forgot_password_should_return_neutral_success() {
        AccountResourceTestDoubles.forgetPasswordResponse = new ForgetPasswordResult(
                true,
                "EMAIL",
                TokenType.PASSWORD_RESET_LINK,
                OffsetDateTime.parse("2026-03-24T12:10:00Z")
        );

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "email": "john@example.com",
                          "type": "PASSWORD_RESET_LINK"
                        }
                        """)
                .when()
                .post("/forgot-password")
                .then()
                .statusCode(200)
                .body("accepted", equalTo(true))
                .body("deliveryChannel", equalTo("EMAIL"))
                .body("tokenType", equalTo("PASSWORD_RESET_LINK"))
                .body("expiresAt", equalTo("2026-03-24T12:10:00Z"));
    }

    @Test
    void reset_password_should_return_reset_timestamp() {
        AccountResourceTestDoubles.resetPasswordResponse =
                new PasswordResetResult(
                        UUID.fromString("44444444-4444-4444-4444-444444444444"),
                        true,
                        true,
                        OffsetDateTime.parse("2026-03-24T12:00:00Z")
                );

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "token": "raw-reset-token",
                          "newPassword": "new-secret",
                          "confirmNewPassword": "new-secret"
                        }
                        """)
                .when()
                .post("/reset-password")
                .then()
                .statusCode(200)
                .body("accountId", equalTo("44444444-4444-4444-4444-444444444444"))
                .body("passwordUpdated", equalTo(true))
                .body("sessionsRevoked", equalTo(true))
                .body("resetAt", equalTo("2026-03-24T12:00:00Z"));
    }

    @Test
    void verify_email_should_return_verification_timestamp() {
        AccountResourceTestDoubles.verifyEmailResponse =
                new AccountVerificationResult(
                        UUID.fromString("55555555-5555-5555-5555-555555555555"),
                        true,
                        OffsetDateTime.parse("2026-03-24T12:30:00Z")
                );

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "token": "raw-verify-token",
                          "type": "EMAIL_VERIFICATION_LINK"
                        }
                        """)
                .when()
                .post("/verify-email")
                .then()
                .statusCode(200)
                .body("accountId", equalTo("55555555-5555-5555-5555-555555555555"))
                .body("verified", equalTo(true))
                .body("verifiedAt", equalTo("2026-03-24T12:30:00Z"));
    }

    @Test
    void resend_verification_should_return_neutral_success() {
        AccountResourceTestDoubles.resendVerificationResponse = new VerifyCodeSentResult(
                true,
                "EMAIL",
                TokenType.EMAIL_VERIFICATION_CODE,
                OffsetDateTime.parse("2026-03-24T12:20:00Z")
        );

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "email": "john@example.com",
                          "type": "EMAIL_VERIFICATION_CODE"
                        }
                        """)
                .when()
                .post("/resend-verification")
                .then()
                .statusCode(200)
                .body("accepted", equalTo(true))
                .body("deliveryChannel", equalTo("EMAIL"))
                .body("tokenType", equalTo("EMAIL_VERIFICATION_CODE"))
                .body("expiresAt", equalTo("2026-03-24T12:20:00Z"));
    }

    @Test
    void check_token_should_return_token_status() {
        AccountResourceTestDoubles.checkTokenResponse = new CheckTokenResult(
                true,
                "PASSWORD_RESET_CODE",
                OffsetDateTime.parse("2026-03-24T12:40:00Z")
        );

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "token": "raw-token",
                          "type": "PASSWORD_RESET_CODE"
                        }
                        """)
                .when()
                .post("/check-token")
                .then()
                .statusCode(200)
                .body("valid", equalTo(true))
                .body("tokenType", equalTo("PASSWORD_RESET_CODE"))
                .body("expiresAt", equalTo("2026-03-24T12:40:00Z"));
    }

    @Test
    void update_should_return_no_content() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "name": "John Updated",
                          "prefix": "+225",
                          "number": "0102030405"
                        }
                        """)
                .when()
                .put()
                .then()
                .statusCode(204);
    }

    @Test
    void current_should_return_account_view() {
        AccountResourceTestDoubles.currentResponse = new AccountDetails(
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "John",
                "john@example.com",
                new PhoneNumber("+225", "0700000000"),
                "https://cdn.example.com/avatar.png",
                Provider.BASIC,
                OffsetDateTime.parse("2026-03-24T08:00:00Z"),
                null,
                OffsetDateTime.parse("2026-03-20T08:00:00Z"),
                OffsetDateTime.parse("2026-03-24T08:05:00Z")
        );

        given()
                .when()
                .get("/current")
                .then()
                .statusCode(200)
                .body("id", equalTo("22222222-2222-2222-2222-222222222222"))
                .body("name", equalTo("John"))
                .body("email", equalTo("john@example.com"))
                .body("prefix", equalTo("+225"))
                .body("number", equalTo("0700000000"))
                .body("provider", equalTo("BASIC"))
                .body("avatarUrl", equalTo("https://cdn.example.com/avatar.png"))
                .body("activated", equalTo(true))
                .body("disabled", equalTo(false))
                .body("createdAt", notNullValue())
                .body("updatedAt", notNullValue());
    }
}
