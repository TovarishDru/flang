package interpreter.src.stages;

import java.util.ArrayList;

import interpreter.src.models.Token;

public class Lexer {
    private ArrayList<Token> tokens;
    private String input;
    private int pos;
    private int line;
    private int length;

    Lexer(String input) {
        this.input = input;
        pos = 0;
        line = 0;
        length = input.length();
    }

    // main parsing method. Detects only simple cases as
    // parentheses, newline characters, etc. Calls
    // parseNumber or parseKeyword for more complicated cases
    public void parseTokens() {

    }

    private Token parseKeyword() {

    }

    private Token parseNumber() {
        
    }
}
