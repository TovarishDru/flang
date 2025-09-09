package models;

import java.util.HashMap;
import java.util.Map;

public enum TokenType {
    // Structural
    LPAREN,
    RPAREN,
    QUOTE,

    // Special forms
    SETQ,
    FUNC,
    LAMBDA,
    PROG,
    COND,
    WHILE,
    RETURN,
    BREAK,

    // Arithmetic
    PLUS,
    MINUS,
    TIMES,
    DIVIDE,

    // List operations
    HEAD,
    TAIL,
    CONS,

    // Comparisons
    EQUAL,
    NONEQUAL,
    LESS,
    LESSEQ,
    GREATER,
    GREATEREQ,

    // Predicates
    ISINT,
    ISREAL,
    ISBOOL,
    ISNULL,
    ISATOM,
    ISLIST,

    // Logical
    AND,
    OR,
    XOR,
    NOT,

    // Evaluator
    EVAL,

    // Literals
    INTEGER,
    REAL,
    BOOLEAN,
    NULL,

    // General identifiers
    IDENTIFIER;

    private static final Map<String, TokenType> keywords = new HashMap<>();
    static {
        // special forms
        keywords.put("quote", QUOTE);
        keywords.put("'", QUOTE);
        keywords.put("setq", SETQ);
        keywords.put("func", FUNC);
        keywords.put("lambda", LAMBDA);
        keywords.put("prog", PROG);
        keywords.put("cond", COND);
        keywords.put("while", WHILE);
        keywords.put("return", RETURN);
        keywords.put("break", BREAK);

        // arithmetic
        keywords.put("plus", PLUS);
        keywords.put("minus", MINUS);
        keywords.put("times", TIMES);
        keywords.put("divide", DIVIDE);

        // list operations
        keywords.put("head", HEAD);
        keywords.put("tail", TAIL);
        keywords.put("cons", CONS);

        // comparisons
        keywords.put("equal", EQUAL);
        keywords.put("nonequal", NONEQUAL);
        keywords.put("less", LESS);
        keywords.put("lesseq", LESSEQ);
        keywords.put("greater", GREATER);
        keywords.put("greatereq", GREATEREQ);

        // predicates
        keywords.put("isint", ISINT);
        keywords.put("isreal", ISREAL);
        keywords.put("isbool", ISBOOL);
        keywords.put("isnull", ISNULL);
        keywords.put("isatom", ISATOM);
        keywords.put("islist", ISLIST);

        // logical
        keywords.put("and", AND);
        keywords.put("or", OR);
        keywords.put("xor", XOR);
        keywords.put("not", NOT);

        // evaluator
        keywords.put("eval", EVAL);

        // literals
        keywords.put("true", BOOLEAN);
        keywords.put("false", BOOLEAN);
        keywords.put("null", NULL);

        // parentheses
        keywords.put("(", LPAREN);
        keywords.put(")", RPAREN);
    }

    public static TokenType fromString(String s) {
        if (keywords.containsKey(s)) {
            return keywords.get(s);
        }
        if (s.matches("^-?\\d+$")) {
            return INTEGER;
        }
        if (s.matches("^-?\\d+\\.\\d+$")) {
            return REAL;
        }
        return IDENTIFIER;
    }
}
