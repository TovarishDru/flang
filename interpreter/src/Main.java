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
        System.out.println("The parsing result is:");
        int prevLine = -1;
        StringBuilder lineBuilder = new StringBuilder();

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);

            if (token.getLine() != prevLine && prevLine != -1) {
                System.out.println(lineBuilder.toString().trim());
                lineBuilder.setLength(0);
            }

            String value = token.getValue();
            if ("\n".equals(value)) {
                value = "\\n";
            }

            lineBuilder.append(token.getType())
                    .append("(")
                    .append(value)
                    .append(") ");

            prevLine = token.getLine();
        }

        if (lineBuilder.length() > 0) {
            System.out.println(lineBuilder.toString().trim());
        }
    }

}
