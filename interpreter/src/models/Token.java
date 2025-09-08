package interpreter.src.models;

public class Token {
    private TokenType type;
    private String value;

    Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    public TokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
