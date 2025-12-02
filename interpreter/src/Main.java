import models.nodes.AstNode;
import models.token.Token;
import stages.Interpreter;
import stages.Lexer;
import stages.Parser;
import stages.Semanter;

import java.util.ArrayList;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    private static final String RESET = "\u001B[0m";
    private static final String CYAN = "\u001B[36m";
    private static final String BOLD_YELLOW = "\u001B[93m";
    private static final String RED = "\u001B[31m";

    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            try {
                String content = new String(Files.readAllBytes(Paths.get(args[i])));

                System.out.println(BOLD_YELLOW + "Processing file: " + args[i] + RESET);
                Lexer lexer = new Lexer(content);
                lexer.parseTokens();
                ArrayList<Token> tokens = lexer.getTokens();

                System.out.println("\nTokenization result:");
                writeTokens(tokens, args[i]);

                Parser parser = new Parser(tokens);
                AstNode ast = parser.parseAst();
                System.out.println("\nParsing result:");
                printAst(ast, "", true);

                System.out.println("\nOptimization logs:");
                Semanter semanter = new Semanter();
                ast = semanter.optimize(ast);

                System.out.println("\nTree after optimization:");
                printAst(ast, "", true);

                Interpreter interpreter = new Interpreter(parser.getGlobalScope(), true);
                System.out.println("\nInterpreting result:");
                ast.accept(interpreter);
            } catch (Exception e) {
                System.out.println(RED + "Error while processing file " + args[i] + ": " + e + RESET);
            }
        }
    }

    private static void writeTokens(ArrayList<Token> tokens, String fileName) {
        System.out.println("The parsing result for file " + fileName + " is:");
        int prevLine = -1;
        StringBuilder lineBuilder = new StringBuilder();

        for (Token token : tokens) {
            if (token.getLine() != prevLine && prevLine != -1) {
                System.out.println(lineBuilder.toString().trim());
                lineBuilder.setLength(0);
            }

            String value = token.getValue();
            if ("\n".equals(value)) value = "\\n";

            lineBuilder.append(token.getType()).append("(").append(value).append(") ");

            prevLine = token.getLine();
        }

        if (lineBuilder.length() > 0) {
            System.out.println(lineBuilder.toString().trim());
        }
    }

    private static void printAst(AstNode node, String prefix, boolean isTail) {
        if (node == null) return;

        System.out.println(prefix + (isTail ? "└── " : "├── ") + CYAN + node.toString() + RESET);

        var children = node.getChildren();
        for (int i = 0; i < children.size(); i++) {
            printAst(children.get(i), prefix + (isTail ? "    " : "│   "), i == children.size() - 1);
        }
    }
}
