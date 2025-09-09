package models;

import java.util.HashMap;
import java.util.Map;

public enum TokenType {
    LPAREN,
    RPAREN,
    ASSIGN,
    SEMICOLON,
    DOT,

    FUNC,
    LAMBDA,
    WHILE,
    FOR,
    RANGE,
    RETURN,
    BREAK,
    CONTINUE,
    IF,
    ELSE,
    ELSEIF,

    PLUS,
    MINUS,
    TIMES,
    DIVIDE,
    MODULO,
    POWER,
    INC,
    DEC,

    INTEGER,
    REAL,
    BOOLEAN,
    STRING,
    CHAR,
    NULL,

    EQUAL,
    NON_EQUAL,
    LESS,
    LESS_EQUAL,
    GREATER,
    GREATER_EQUAL,

    IS_INT,
    IS_REAL,
    IS_BOOLEAN,
    IS_STRING,
    IS_CHAR,
    IS_NULL,

    AND,
    OR,
    XOR,
    NOT,

    LIST,
    DICT,
    OBJECT,
    APPEND,

    IDENTIFIER;

    private static final Map<String, TokenType> keywords = new HashMap<>();
    static {
        // keywords
        keywords.put("func", FUNC);
        keywords.put("lambda", LAMBDA);
        keywords.put("while", WHILE);
        keywords.put("for", FOR);
        keywords.put("range", RANGE);
        keywords.put("return", RETURN);
        keywords.put("break", BREAK);
        keywords.put("continue", CONTINUE);
        keywords.put("if", IF);
        keywords.put("else", ELSE);
        keywords.put("elseif", ELSEIF);

        keywords.put("true", BOOLEAN);
        keywords.put("false", BOOLEAN);
        keywords.put("null", NULL);

        // type checks
        keywords.put("is_int", IS_INT);
        keywords.put("is_real", IS_REAL);
        keywords.put("is_boolean", IS_BOOLEAN);
        keywords.put("is_string", IS_STRING);
        keywords.put("is_char", IS_CHAR);
        keywords.put("is_null", IS_NULL);

        // logical operators
        keywords.put("and", AND);
        keywords.put("or", OR);
        keywords.put("xor", XOR);
        keywords.put("not", NOT);

        // collections
        keywords.put("list", LIST);
        keywords.put("dict", DICT);
        keywords.put("object", OBJECT);
        keywords.put("append", APPEND);
    }
    private static final Map<String, TokenType> operators = new HashMap<>();
    static {
        // operators & delimiters
        operators.put("=", ASSIGN);
        operators.put("==", EQUAL);
        operators.put("!=", NON_EQUAL);
        operators.put("<", LESS);
        operators.put("<=", LESS_EQUAL);
        operators.put(">", GREATER);
        operators.put(">=", GREATER_EQUAL);

        operators.put("+", PLUS);
        operators.put("-", MINUS);
        operators.put("*", TIMES);
        operators.put("/", DIVIDE);
        operators.put("%", MODULO);
        operators.put("^", POWER);

        operators.put("++", INC);
        operators.put("--", DEC);

        operators.put("(", LPAREN);
        operators.put(")", RPAREN);
        operators.put(";", SEMICOLON);
        operators.put(".", DOT);
    }

    public static TokenType fromString(String s) {
        if (keywords.containsKey(s)) {
            return keywords.get(s);
        }
        if (operators.containsKey(s)) {
            return operators.get(s);
        }
        if (s.matches("^-?\\d+$")) {
            return INTEGER;
        }
        if (s.matches("^-?\\d+\\.\\d+$")) {
            return REAL;
        }
        if (s.startsWith("\"") && s.endsWith("\"")) {
            return STRING;
        }
        if (s.startsWith("'") && s.endsWith("'") && s.length() == 3) {
            return CHAR;
        }
        return IDENTIFIER;
    }
}
