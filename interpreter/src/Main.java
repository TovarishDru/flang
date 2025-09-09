import models.Token;
import stages.Lexer;

import java.util.ArrayList;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            try {
                String content = new String(Files.readAllBytes(Paths.get(args[i])));
                Lexer lexer = new Lexer(content);
                lexer.parseTokens();
                ArrayList<Token> tokens = lexer.getTokens();
                writeTokens(tokens);
            } catch (Exception e) {
                System.out.println("Error while processing file " + args[i] + ": " + e.toString());
            }
        }
    }

    private static void writeTokens(ArrayList<Token> tokens) {
        System.out.println("The pasring result is:");
        int prevLine = -1;
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.getLine() != prevLine && prevLine > 0) {
                System.out.println();
            }
            if (i != tokens.size() - 1) {
                System.out.println(token.getType() + "(" + token.getValue() + ") ");
            } else {
                System.out.println(token.getType() + "(" + token.getValue() + ")");
            }
        }
    }
}
