import models.nodes.AstNode;
import models.token.Token;
import stages.Lexer;
import stages.Parser;
import stages.Semanter;

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
                writeTokens(tokens, args[i]);

                Parser parser = new Parser(tokens);
                AstNode ast = parser.parseAst();
                printAst(ast, 0);

                Semanter semanter = new Semanter();
                semanter.analyze(ast);
            } catch (Exception e) {
                System.out.println("Error while processing file " + args[i] + ": " + e.toString());
            }
        }
    }

    private static void writeTokens(ArrayList<Token> tokens, String fileName) {
        System.out.println("The parsing result for file " + fileName + " is:");
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

    private static void printAst(AstNode node, int depth) {
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            indent.append("  ");
        }
        System.out.println(indent + node.toString());

        for (AstNode child : node.getChildren()) {
            printAst(child, depth + 1);
        }
    }
}
