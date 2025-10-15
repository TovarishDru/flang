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

    private Token consume(TokenType type) throws Exception {
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

    private AstNode parseLiteralList() throws Exception {
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

    private AstNode parseLambdaCall() throws Exception{
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

    private AstNode parseSETQ() throws Exception {
        advance();
        String name = consume(TokenType.ATOM).getValue();
        AstNode value = parseNode();
        consume(TokenType.RPAREN);

        localScope.define(name, value);

        return new SetqNode(name, value);
    }

    private AstNode parseFUNC() throws Exception {
        advance();

        SymbolTable prev = localScope;
        localScope = new SymbolTable(prev);

        String functionName = consume(TokenType.ATOM).getValue();

        FunctionNode placeholder = new FunctionNode(functionName, new ArrayList<>(), null);
        globalScope.define(functionName, placeholder);
        localScope.define(functionName, placeholder);

        consume(TokenType.LPAREN);
        ArrayList<String> params = new ArrayList<>();
        while (!check(TokenType.RPAREN)) {
            String p = consume(TokenType.ATOM).getValue();
            params.add(p);
            localScope.define(p, null);
        }
        consume(TokenType.RPAREN);

        ArrayList<AstNode> bodyExprs = new ArrayList<>();
        while (!check(TokenType.RPAREN)) {
            bodyExprs.add(parseNode());
        }
        consume(TokenType.RPAREN);

        AstNode body = new ProgNode(bodyExprs);

        localScope = prev;

        FunctionNode fn = new FunctionNode(functionName, params, body);

        globalScope.define(functionName, fn);
        localScope.define(functionName, fn);

        return fn;
    }

    private AstNode parseCOND() throws Exception {
        advance();

        AstNode condition = parseNode();
        AstNode action = parseNode();

        AstNode defaultAction = null; // optional else
        if (!check(TokenType.RPAREN)) {
            defaultAction = parseNode();
        }

        consume(TokenType.RPAREN);
        return new CondNode(condition, action, defaultAction);
    }

    private AstNode parsePROG() throws Exception {
        advance();

        SymbolTable prev = localScope;
        localScope = new SymbolTable(prev);

        consume(TokenType.LPAREN);
        while (!check(TokenType.RPAREN)) {
            String name = consume(TokenType.ATOM).getValue();
            localScope.define(name, null);
        }
        consume(TokenType.RPAREN);

        ArrayList<AstNode> statements = new ArrayList<>();
        while (!check(TokenType.RPAREN)) {
            statements.add(parseNode());
        }
        consume(TokenType.RPAREN);

        localScope = prev;

        return new ProgNode(statements);
    }

    private AstNode parseHeadOrTail(String which) throws Exception {
        advance();
        AstNode listExpr = parseNode();
        consume(TokenType.RPAREN);

        if ("head".equals(which)) {
            return new HeadNode(listExpr);
        } else {
            return new TailNode(listExpr);
        }
    }

    private AstNode parseCons() throws Exception {
        advance();
        AstNode item = parseNode();
        AstNode list = parseNode();
        consume(TokenType.RPAREN);
        return new ConsNode(item, list);
    }

    private AstNode parseWHILE() throws Exception {
        advance();
        consume(TokenType.LPAREN);
        AstNode condition = parseNode();
        ArrayList<AstNode> body = new ArrayList<>();
        while (!check(TokenType.RPAREN)) {
            body.add(parseNode());
        }
        consume(TokenType.RPAREN);
        return new WhileNode(condition, body);
    }

    private AstNode parseRETURN() throws Exception {
        advance();
        AstNode value = parseNode();
        consume(TokenType.RPAREN);
        return new ReturnNode(value);
    }

    private AstNode parseBREAK() throws Exception {
        advance();
        consume(TokenType.RPAREN);
        return new BreakNode();
    }

    private AstNode parsePredicate() throws Exception {
        Token op = advance(); // isint|isreal|isbool|isnull|isatom|islist
        AstNode arg = parseNode();
        consume(TokenType.RPAREN);
		if (op == null) {
			throw new Exception("ERROR: Expected predicate parameter"); //TODO Check on test case and delete maybe
		}
        return new PredicateNode(op.getValue(), arg);
    }

    private AstNode parseQuote() throws Exception {
        advance();
        AstNode quoted = parseNode();
        consume(TokenType.RPAREN);
        AstNode q = new QuoteNode(quoted);
        q.addChild(quoted);
        return q;
    }

    private AstNode parseEval() throws Exception {
        advance();
        AstNode expr = parseNode();
        consume(TokenType.RPAREN);
        return new EvalNode(expr);
    }

    private AstNode parseLogicalOperator() throws Exception {
        Token op = advance();
        AstNode left = parseNode();
        AstNode right = parseNode();
        consume(TokenType.RPAREN);
        return new LogicalNode(op.getValue(), left, right);
    }

    private AstNode parseNot() throws Exception {
        advance();
        AstNode arg = parseNode();
        consume(TokenType.RPAREN);
        return new NotNode(arg);
    }

    private AstNode parseLambda() throws Exception {
        advance();

        SymbolTable prev = localScope;
        localScope = new SymbolTable(prev);

        ArrayList<String> params = new ArrayList<>();
        consume(TokenType.LPAREN);
        while (!check(TokenType.RPAREN)) {
            String p = consume(TokenType.ATOM).getValue();
            params.add(p);
            localScope.define(p, null);
        }
        consume(TokenType.RPAREN);

        AstNode body = parseNode();

        consume(TokenType.RPAREN);

        localScope = prev;

        return new LambdaNode(params, body);
    }
}
