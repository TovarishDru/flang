package stages;

import models.Token;
import models.TokenType;

import java.util.ArrayList;

public class Lexer {
    private ArrayList<Token> tokens;
    private String input;
    private int pos;
    private int line;
    private int length;

    public Lexer(String input) {
        this.input = input;
        pos = 0;
        line = 0;
        length = input.length();
        tokens = new ArrayList<>();
    }

    public ArrayList<Token> getTokens() {
        return tokens;
    }

    public void parseTokens() throws Exception {
        while (pos < length) {
            char cur = input.charAt(pos);

            if (cur == '(' || cur == ')' || cur == '\'' || cur == '\n') {
                String curString = String.valueOf(cur);
                tokens.add(new Token(TokenType.fromString(curString), curString, line));
                if (cur == '\n') {
                    line++;
                }
                pos++;
            } else {
                if (Character.isWhitespace(cur)) {
                    pos++;
                } else if (Character.isDigit(cur) || cur == '+' || cur == '-') {
                    tokens.add(parseNumber());
                } else if (Character.isLetter(cur)) {
                    tokens.add(parseKeyword());
                } else {
                    throw new Exception("Unknown character '" + cur + "' at line " + line);
                }
            }
        }
    }

    private Token parseKeyword() {
        StringBuilder builder = new StringBuilder();
        while (pos < length) {
            char cur = input.charAt(pos);
            if (Character.isLetterOrDigit(cur) || cur == '_') {
                builder.append(cur);
                pos++;
            } else {
                break;
            }
        }
        String keyword = builder.toString();
        return new Token(TokenType.fromString(keyword), keyword, line);
    }

    private Token parseNumber() throws Exception {
        StringBuilder builder = new StringBuilder();

        while (pos < length) {
            char cur = input.charAt(pos);
            if (Character.isDigit(cur) || cur == '.' || cur == '-' || cur == '+') {
                if (cur != '+') {
                    builder.append(cur);
                }
                pos++;
            } else {
                break;
            }
        }

        if (pos < length && Character.isLetter(input.charAt(pos))) {
            throw new Exception("Unexpected character '" + input.charAt(pos) + "' at line " + line);
        }

        String numberString = builder.toString();
        return new Token(TokenType.fromString(numberString), numberString, line);
    }
}
