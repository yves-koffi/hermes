package shared.domain.model;


public enum TokenType {
    ACCESS_TOKEN("ACCESS"),
    REFRESH_TOKEN("REFRESH");
    public final String value;

    TokenType(String value) {
        this.value = value;
    }
}
