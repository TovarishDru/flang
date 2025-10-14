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

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().getType() == type;
    }

    private Token consume(TokenType type) {
        if (check(type)) {
            return advance();
        }
        Token errToken = peek();
        throw new Exception("Unexpected token type. FOUND: " + errToken + " at line: " + errToken.getLine());
    }

    private AstNode parseNode() throws Exception {
        Token curToken = tokens.get(tokenIndex);

        return switch (curToken.getType()) {
            case LPAREN -> parseParenthesizedExpr();
            case QUOTE -> parseQuoteWithoutBrackets();
            case INTEGER, REAL, NULL, BOOLEAN -> {
                advance();
                AstNode literalNode = new LiteralNode(curToken);
                yield literalNode;
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

    private AstNode parseParenthesizedExpr() throws Exception {
        consume(TokenType.LPAREN);

        Token operatorToken = peek();
        if (operatorToken.getType() == TokenType.INTEGER || operatorToken.getType() == TokenType.REAL || operatorToken.getType() == TokenType.BOOLEAN) {
            return parseLiteralList();
        } else if (operatorToken.getType() == TokenType.ATOM) {
            String operatorValue = operatorToken.getValue();
            if (globalScope.defined(operatorValue) && globalScope.find(operatorValue).getType() == NodeType.FUNC) {
                return parseFuncCall();
            } else if (globalScope.defined(operatorValue) && globalScope.find(operatorValue).getType() == NodeType.LAMBDA) {
                return parseLambdaCall();
            } else {
                return parseLiteralList();
            }
        }

        return switch (operatorToken.getValue()) {
            case "setq" -> parseSETQ();
            case "func" -> parseFUNC();
            case "cond" -> parseCOND();
            case "prog" -> parsePROG();
            case "plus", "minus", "times", "divide" -> parseOperation();
            case "head" -> parseHeadOrTail("head");
            case "tail" -> parseHeadOrTail("tail");
            case "cons" -> parseCons();
            case "while" -> parseWHILE();
            case "return" -> parseRETURN();
            case "break" -> parseBREAK();
            case "isint", "isreal", "isbool", "isnull", "isatom", "islist" -> parsePredicate();
            case "equal", "nonequal", "less", "lesseq", "greater", "greatereq" -> parseComparison();
            case "and", "or", "xor" -> parseLogicalOperator();
            case "not" -> parseNot();
            case "lambda" -> parseLambda();
            case ")" -> parseLiteralList();
            case "quote" -> parseQuote();
            case "eval" -> parseEval();
            default -> parseFuncCall();
        };
    }

    private AstNode parseQuoteWithoutBrackets() throws Exception {
        Token quoteToken = advance();
        AstNode quotedExpr = parseNode();
        AstNode quotedNode = new QuoteNode(quotedExpr);
        quotedNode.addChild(quotedExpr);
        return quotedNode;
    }
    
    private AstNode parseComparison() throws Exception {
        Token opertator = advance();
        AstNode leftElement = parseNode();
        AstNode rightElement = parseNode();
        consume(TokenType.RPAREN);

        return new ComparisonNode(opertator, leftElement, rightElement);
    }

    private AstNode parseOperation() throws Exception {
        Token operatorToken = advance();
        String operator = operatorToken.getValue();
        ArrayList<AstNode> operands = new ArrayList<>();

        while (!check(TokenType.RPAREN)) {
            AstNode expr = parseNode();
            operands.add(expr);
        }
        consume(TokenType.RPAREN);

        if (operands.size() < 2 && !(operator.equals("plus") || operator.equals("minus"))) {
            throw new Exception("ERROR: IMPOSSIBLE OPERATION at line: " + operatorToken.getLine());
        }
        AstNode operatorNode = new OperationNode(operatorToken, operands);
        return operatorNode;
    }

    private AstNode parseLiteralList() {
        ArrayList<AstNode> elements = new ArrayList<>();

        while (!check(TokenType.RPAREN)) {
            elements.add(parseNode());
        }

        Token clo = consume(TokenType.RPAREN);
        AstNode listNode = new ListNode(elements);
        return listNode;
    }

    private AstNode parseFuncCall() throws Exception {
        String functionName = peek().getValue();
        int line = peek().getLine();
        
        if (!globalScope.defined(functionName)) {
            throw new Exception("ERROR: UNDEFINED FUNCTION " + functionName + " at line " + line);
        }

        FunctionNode functionNode = (FunctionNode) globalScope.find(functionName);
        if (functionNode.getType() != NodeType.FUNC) {
            throw new Exception("ERROR: " + functionName + " IS NOT A FUNCTION at line: " + line);
        }

        ArrayList<AstNode> operands = new ArrayList<>();
        advance();

        while (!check(TokenType.RPAREN)) {
            AstNode expr = parseNode();
            operands.add(expr);
        }

        int expectedParams = ((FunctionNode) functionNode).getParameters().size();

        if (operands.size() != expectedParams) {
            throw new Exception("ERROR: INCORRECT NUMBER OF PARAMETERS FOR FUNCTION " + functionName +
                    "EXPECTED-GOT: " + expectedParams + "-" + operands.size() +
                    " at line: " + line);
        }

        consume(TokenType.RPAREN);
        return new FunctionCallNode(functionName, operands);
    }

    private AstNode parseLambdaCall() {
        String lambdaName = peek().getValue();
        int line = peek().getLine();

        if (!globalScope.defined(lambdaName)) {
            throw new Exception("ERROR: UNDEFINED LAMBDA " + lambdaName + " at line: " + line);
        }
        AstNode lambdaNode = globalScope.find(lambdaName);

        ArrayList<AstNode> operands = new ArrayList<>();
        while (!check(TokenType.RPAREN)) {
            operands.add(parseNode());
        }

        consume(TokenType.RPAREN);
        return new LambdaCallNode(lambdaName, operands);
    }
}
