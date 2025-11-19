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

	public SymbolTable getGlobalScope() {
		return globalScope;
	}

	public AstNode parseAst() throws Exception {
		ArrayList<AstNode> instructions = new ArrayList<>();

		while (!isAtEnd()) {
			AstNode instruction = parseNode();
			instructions.add(instruction);
		}

		return new ProgNode(instructions);
	}


	private Token peek() throws Exception {
		if (isAtEnd()) {
			throw new Exception("ERROR: UNEXPECTED END OF INPUT");
		}
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

	private boolean check(TokenType type) throws Exception {
		if (isAtEnd()) return false;
		return peek().getType() == type;
	}

	private Token consume(TokenType expected) throws Exception {
		if (isAtEnd()) {
			throw new Exception("ERROR: UNEXPECTED END OF INPUT, EXPECTED " + expected);
		}
		Token token = tokens.get(tokenIndex);
		if (token.getType() != expected) {
			throw new Exception("ERROR: EXPECTED " + expected + " BUT FOUND " +
					token.getValue() + " at line " + token.getLine());
		}
		tokenIndex++;
		return token;
	}

	private AstNode parseNode() throws Exception {
		Token curToken = peek();

		return switch (curToken.getType()) {
			case LPAREN -> parseParenthesizedExpr();
			case QUOTE -> parseQuoteWithoutBrackets();

			case INTEGER, REAL, NULL, BOOLEAN -> {
				advance();
				yield new LiteralNode(curToken);
			}

			case ATOM -> {
				advance();
				if (localScope.defined(curToken.getValue())) {
					yield new AtomNode(curToken);
				} else {
					throw new Exception("ERROR: UNDEFINED VARIABLE " +
							curToken.getValue() + " at line " + curToken.getLine());
				}
			}

			case PLUS, MINUS, TIMES, DIVIDE -> {
				advance();
				yield new AtomNode(curToken);
			}

			case LESS, LESSEQ, GREATER, GREATEREQ, EQUAL, NONEQUAL -> parseComparison();

			default -> throw new Exception("ERROR: UNEXPECTED TOKEN: " +
					curToken.getValue() + " at line " + curToken.getLine());
		};
	}


	private AstNode parseQuoted() throws Exception {
		if (check(TokenType.LPAREN)) {
			return parseQuotedList();
		}

		Token t = advance();
		switch (t.getType()) {
			case INTEGER, REAL, NULL, BOOLEAN -> {
				return new LiteralNode(t);
			}

			case ATOM,
				 PLUS, MINUS, TIMES, DIVIDE,
				 LESS, LESSEQ, GREATER, GREATEREQ, EQUAL, NONEQUAL, ISBOOL -> {
				return new AtomNode(t);
			}

			case QUOTE -> {
				AstNode inner = parseQuoted();
				return new QuoteNode(inner);
			}

			default -> throw new Exception(
					"ERROR: invalid token in quoted form: " + t.getValue() + " at line: " + t.getLine()
			);
		}
	}

	private AstNode parseQuotedList() throws Exception {
		consume(TokenType.LPAREN);
		ArrayList<AstNode> elements = new ArrayList<>();
		while (!isAtEnd() && !check(TokenType.RPAREN)) {
			elements.add(parseQuoted());
		}
		if (isAtEnd()) {
			throw new Exception("ERROR: MISSING ')' IN QUOTED LIST");
		}
		consume(TokenType.RPAREN);
		return new ListNode(elements);
	}

	private AstNode parseParenthesizedExpr() throws Exception {
		consume(TokenType.LPAREN);

		Token operatorToken = peek();

		if (operatorToken.getType() == TokenType.INTEGER
				|| operatorToken.getType() == TokenType.REAL
				|| operatorToken.getType() == TokenType.BOOLEAN) {
			return parseLiteralList();
		}

		String op = operatorToken.getValue();

		return switch (op) {
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
			case "lambda" -> parseLambda();           // <--
			case ")" -> parseLiteralList();
			case "(" -> {
				AstNode node = parseParenthesizedExpr();
				consume(TokenType.RPAREN);
				yield node;
			}
			case "quote" -> parseQuote();
			case "eval" -> parseEval();

			default -> {
				if (operatorToken.getType() == TokenType.ATOM
						&& globalScope.defined(op)
						&& globalScope.find(op).getType() == NodeType.FUNC) {
					yield parseFuncCall();
				} else {
					yield parseLiteralList();
				}
			}
		};
	}

	private AstNode parseQuoteWithoutBrackets() throws Exception {
		advance(); // QUOTE (')
		AstNode form = parseQuoted();
		return new QuoteNode(form);
	}

	private AstNode parseQuote() throws Exception {
		advance(); // "quote"
		AstNode form = parseQuoted();
		consume(TokenType.RPAREN);
		return new QuoteNode(form);
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

		while (!isAtEnd() && !check(TokenType.RPAREN)) {
			AstNode expr = parseNode();
			if (expr.getType() == NodeType.LITERAL &&
					(((LiteralNode) expr).getTokenType() == TokenType.BOOLEAN)) {
				throw new Exception("ERROR: IMPOSSIBLE OPERATION at line: " + operatorToken.getLine());
			}
			operands.add(expr);
		}

		if (isAtEnd()) {
			throw new Exception("ERROR: MISSING ')' AFTER OPERATION " + operator +
					" at line: " + operatorToken.getLine());
		}

		consume(TokenType.RPAREN);

		if (operands.size() != 2) {
			throw new Exception("ERROR: IMPOSSIBLE OPERATION at line: " + operatorToken.getLine());
		}

		return new OperationNode(operatorToken, operands);
	}

	private AstNode parseLiteralList() throws Exception {
		ArrayList<AstNode> elements = new ArrayList<>();

		while (!isAtEnd() && !check(TokenType.RPAREN)) {
			elements.add(parseNode());
		}

		if (isAtEnd()) {
			throw new Exception("ERROR: MISSING ')' IN LIST");
		}

		consume(TokenType.RPAREN);
		return new ListNode(elements);
	}

	private AstNode parseFuncCall() throws Exception {
		String functionName = peek().getValue();
		int line = peek().getLine();

		if (!globalScope.defined(functionName)) {
			throw new Exception("ERROR: UNDEFINED FUNCTION " + functionName + " at line " + line);
		}

		AstNode fnNode = globalScope.find(functionName);
		if (fnNode == null || fnNode.getType() != NodeType.FUNC) {
			throw new Exception("ERROR: " + functionName + " IS NOT A FUNCTION at line: " + line);
		}
		FunctionNode functionNode = (FunctionNode) fnNode;

		advance(); // consume name

		ArrayList<AstNode> operands = new ArrayList<>();
		while (!isAtEnd() && !check(TokenType.RPAREN)) {
			operands.add(parseNode());
		}

		if (isAtEnd()) {
			throw new Exception("ERROR: MISSING ')' IN FUNCTION CALL " + functionName +
					" at line: " + line);
		}

		int expectedParams = functionNode.getParameters() == null ? 0 : functionNode.getParameters().size();
		if (operands.size() != expectedParams) {
			throw new Exception("ERROR: INCORRECT NUMBER OF PARAMETERS FOR FUNCTION " + functionName +
					" EXPECTED-GOT: " + expectedParams + "-" + operands.size() + " at line: " + line);
		}

		consume(TokenType.RPAREN);
		return new FunctionCallNode(functionName, operands);
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
		while (!isAtEnd() && !check(TokenType.RPAREN)) {
			String p = consume(TokenType.ATOM).getValue();
			params.add(p);
			localScope.define(p, null);
		}
		if (isAtEnd()) {
			throw new Exception("ERROR: MISSING ')' IN PARAMETER LIST FOR FUNCTION " + functionName);
		}
		consume(TokenType.RPAREN);

		ArrayList<AstNode> bodyExprs = new ArrayList<>();
		while (!isAtEnd() && !check(TokenType.RPAREN)) {
			bodyExprs.add(parseNode());
		}
		if (isAtEnd()) {
			throw new Exception("ERROR: MISSING ')' IN BODY OF FUNCTION " + functionName);
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

		AstNode defaultAction = null;
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
		while (!isAtEnd() && !check(TokenType.RPAREN)) {
			String name = consume(TokenType.ATOM).getValue();
			localScope.define(name, null);
		}
		if (isAtEnd()) {
			throw new Exception("ERROR: MISSING ')' IN PROG VAR LIST");
		}
		consume(TokenType.RPAREN);

		ArrayList<AstNode> statements = new ArrayList<>();
		while (!isAtEnd() && !check(TokenType.RPAREN)) {
			statements.add(parseNode());
		}
		if (isAtEnd()) {
			throw new Exception("ERROR: MISSING ')' IN PROG BODY");
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
		while (!isAtEnd() && !check(TokenType.RPAREN)) {
			body.add(parseNode());
		}
		if (isAtEnd()) {
			throw new Exception("ERROR: MISSING ')' IN WHILE BODY");
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
		while (!isAtEnd() && !check(TokenType.RPAREN)) {
			String p = consume(TokenType.ATOM).getValue();
			params.add(p);
			localScope.define(p, null);
		}
		if (isAtEnd()) {
			throw new Exception("ERROR: MISSING ')' IN LAMBDA PARAMETER LIST");
		}
		consume(TokenType.RPAREN);

		AstNode body = parseNode();

		consume(TokenType.RPAREN);

		localScope = prev;

		LambdaNode lambdaBody = new LambdaNode(params, body);

		if (!isAtEnd() && peek().getType() != TokenType.RPAREN) {
			return parseLambdaCall(lambdaBody);
		}

		return lambdaBody;
	}

	private AstNode parseLambdaCall(LambdaNode lambdaBody) throws Exception {
		ArrayList<AstNode> args = new ArrayList<>();

		while (!isAtEnd() && !check(TokenType.RPAREN)) {
			args.add(parseNode());
		}
		if (isAtEnd()) {
			throw new Exception("ERROR: MISSING ')' IN LAMBDA CALL");
		}

		lambdaBody.setArguments(args);
		return lambdaBody;
	}
}
