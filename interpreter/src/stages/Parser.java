package stages;

import models.nodes.*;
import models.token.*;
import models.symbol_table.SymbolTable;

import java.util.ArrayList;

public class Parser {
    private ArrayList<Token> tokens;
    private int tokenIndex;
    private SymbolTable localScope;
    private SymbolTable globalScope;

    public Parser(ArrayList<Token> tokens) {
        this.tokens = tokens;
        this.tokenIndex = 0;
        this.localScope = new SymbolTable(null);
        this.globalScope = new SymbolTable(null);
    }

    public AstNode parseAst() throws Exception {
        ArrayList<AstNode> instructions = new ArrayList<>();
        
        while (tokenIndex <= tokens.size()) {
            AstNode instruction = parseNode();
            instructions.add(instruction);
        }

        AstNode progNode = new ProgNode(instructions);

        return progNode;
    }

    private Token peek() {
        return tokens.get(tokenIndex);
    }

    private boolean isAtEnd() {
        return tokenIndex >= tokens.size();
    }
    
    private Token advance() {
        if (!isAtEnd()) {
            return tokens.get(tokenIndex++);
        }
        return null;
    }

    private AstNode parseNode() throws Exception {
        Token curToken = tokens.get(tokenIndex);

        return switch (curToken.getType()) {
            case LPAREN -> parseParenthesizedExpr();
            case QUOTE -> parseQuoteWithoutBrackets();
            case INTEGER -> {
                advance();
                AstNode intNode = new LiteralNode(curToken);
                yield intNode;
            }
            case REAL -> {
                advance();
                AstNode realNode = new LiteralNode(curToken);
                yield realNode;
            }
            case NULL -> {
                advance();
                AstNode nullNode = new LiteralNode(curToken);
                yield nullNode;
            }
            case BOOLEAN -> {
                advance();
                AstNode boolNode = new LiteralNode(curToken);
                yield boolNode;
            }
            case ATOM -> {
                advance();
                if (localScope.defined(curToken.getValue())) {
                    AstNode atomNode = new AtomNode(curToken);
                    yield atomNode;
                } else {
                    throw new Exception("ERROR: UNDEFINED VARIABLE " + curToken.getValue() + " at line " + curToken.getLine());
                }
            }
            case LESS, LESSEQ, GREATER, GREATEREQ, EQUAL, NONEQUAL -> parseComparison();
            case PLUS, MINUS, TIMES, DIVIDE -> parseOperation();
            default -> throw new Exception("ERROR: UNEXPECTED TOKEN: " + curToken + " at line " + curToken.getLine());
        };
    }

    private AstNode parseParenthesizedExpr() {
        return new AtomNode(peek());
    }

    private AstNode parseQuoteWithoutBrackets() {
        return new AtomNode(peek());
    }
    
    private AstNode parseComparison() {
        return new AtomNode(peek());
    }

    private AstNode parseOperation() {
        return new AtomNode(peek());
    }
}
