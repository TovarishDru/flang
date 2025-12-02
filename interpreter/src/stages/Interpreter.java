package stages;

import models.nodes.*;
import models.symbol_table.SymbolTable;

import java.util.ArrayList;
import java.util.List;

public class Interpreter {
	private final SymbolTable symbolTable;
	private final boolean globalScope;

	private Object visit(AstNode node) {
		return node.accept(this);
	}

	public Interpreter(SymbolTable symbolTable, boolean globalScope) {
		this.symbolTable = symbolTable;
		this.globalScope = globalScope;
	}

	public Object visitProgNode(ProgNode progNode) {
		Object result = null;

		for (AstNode childNode : progNode.getChildren()) {
			if (childNode instanceof ProgNode) {
				Interpreter localInterpreter = new Interpreter(symbolTable, false);
				result = localInterpreter.visitProgNode((ProgNode) childNode);
			} else {
				result = visit(childNode);
			}

			if (result instanceof ReturnNode rn) {
				Object value = visit(rn.getValue());

				if (globalScope && value != null) {
					System.err.println(value);
				}
				return value;
			}

			if (globalScope && result != null) {
				System.err.println(result);
			}
		}

		return result;
	}

	public Object visitAtomNode(AtomNode atomNode) {
		String name = atomNode.getValue();
		AstNode bound = symbolTable.find(name);

		if (bound == null) {
			throw new RuntimeException("ERROR: UNDEFINED VARIABLE " + name);
		}

		if (bound == atomNode) {
			throw new RuntimeException("ERROR: SELF-REFERENTIAL VARIABLE " + name);
		}

		if (bound instanceof RuntimeLiteralNode rl) {
			return rl.getValue();
		}

		if (bound instanceof FunctionNode || bound instanceof LambdaNode) {
			return bound;
		}

		return visit(bound);
	}

	public Object visitLiteralNode(LiteralNode literalNode) {
		String value = literalNode.getValue();

		switch (value) {
			case "true" -> {
				return Boolean.TRUE;
			}
			case "false" -> {
				return Boolean.FALSE;
			}
			case "null" -> {
				return null;
			}
		}

		try {
			if (value.contains(".")) {
				return Double.parseDouble(value);
			} else {
				return Integer.parseInt(value);
			}
		} catch (NumberFormatException e) {
			return value;
		}
	}


	public Object visitOperationNode(OperationNode operationNode) {
		String operator = operationNode.getOperator();
		ArrayList<Object> evaluatedOperands = new ArrayList<>();

		for (AstNode operand : operationNode.getChildren()) {
			evaluatedOperands.add(visit(operand));
		}

		return evalOperation(operator, evaluatedOperands);
	}

	public Object visitPredicateNode(PredicateNode predicateNode) {
		String predicate = predicateNode.getPredicate();
		Object value = visit(predicateNode.getArgument());
		return evalPredicate(predicate, value);
	}

	public Object visitCondNode(CondNode condNode) {
		java.util.List<AstNode> kids = condNode.getChildren();

		AstNode condition;
		AstNode thenBranch;
		AstNode elseBranch;

		if (kids != null && !kids.isEmpty()) {
			condition = kids.get(0);
			thenBranch = kids.size() >= 2 ? kids.get(1) : null;
			elseBranch = kids.size() >= 3 ? kids.get(2) : null;
		} else {
			condition = condNode.getCondition();
			thenBranch = condNode.getAction();
			elseBranch = condNode.getDefaultAction();
		}

		Object condVal = visit(condition);
		if (!(condVal instanceof Boolean)) {
			throw new RuntimeException("ERROR: COND CONDITION IS NOT BOOLEAN");
		}

		if ((Boolean) condVal) {
			return evalCondBranch(thenBranch);
		} else {
			return evalCondBranch(elseBranch);
		}
	}

	public Object visitWhileNode(WhileNode whileNode) {
		while ((boolean) visit(whileNode.getCondition())) {
			for (AstNode node : whileNode.getBody()) {
				Object result = visit(node);

				if (result instanceof BreakNode) {
					return null;
				}
				if (result instanceof ReturnNode) {
					return result;
				}
			}
		}
		return null;
	}

	public Object visitFunctionNode(FunctionNode functionNode) {
		symbolTable.define(functionNode.getFunctionName(), functionNode);
		return null;
	}

	public Object visitNotNode(NotNode notNode) {
		Object value = visit(notNode.getArgument());
		return evalNot(value);
	}

	public Object visitComparisonNode(ComparisonNode comparisonNode) {
		Object leftVal = visit(comparisonNode.getLeftElement());
		Object rightVal = visit(comparisonNode.getRightElement());
		String op = comparisonNode.getComparison();
		return evalComparison(op, leftVal, rightVal);
	}

	public Object visitLogicalNode(LogicalNode logicalNode) {
		Object leftVal = visit(logicalNode.getChildren().get(0));
		Object rightVal = visit(logicalNode.getChildren().get(1));
		String operator = logicalNode.getOperator();
		return evalLogical(operator, leftVal, rightVal);
	}


	private boolean asBoolean(Object value, String side) {
		if (value instanceof Boolean b) {
			return b;
		}
		if (value instanceof String s) {
			if (s.equals("true") || s.equals("false")) {
				return Boolean.parseBoolean(s);
			}
		}
		throw new RuntimeException(
				"ERROR: LOGICAL " + side + " OPERAND IS NOT BOOLEAN: " + value
		);
	}


	public Object visitListNode(ListNode listNode) {
		List<Object> values = new ArrayList<>();
		for (AstNode element : listNode.getElements()) {
			values.add(visit(element));
		}
		return values;
	}

	public Object visitQuoteNode(QuoteNode quoteNode) {
		return evalQuoted(quoteNode.getQuotedExpr());
	}

	private Object evalQuoted(AstNode node) {
		switch (node) {
			case ListNode listNode -> {
				List<Object> result = new ArrayList<>();
				for (AstNode elem : listNode.getElements()) {
					result.add(evalQuoted(elem));
				}
				return result;
			}
			case LiteralNode lit -> {
				return visitLiteralNode(lit);
			}
			case AtomNode atom -> {
				return atom.getValue();
			}
			case QuoteNode q -> {
				return evalQuoted(q.getQuotedExpr());
			}
			case null, default -> {
				return node;
			}
		}
	}

	public Object visitEvalNode(EvalNode evalNode) {
		Object value = visit(evalNode.getExpr());
		return evalValue(value);
	}

	private Object evalValue(Object value) {
		if (value instanceof AstNode ast) {
			return visit(ast);
		}

		if (value instanceof List<?> list) {
			return evalListAsProgram(list);
		}
		return value;
	}

	private Object lookupAtomValue(String name) {
		AstNode bound = symbolTable.find(name);
		if (bound == null) {
			throw new RuntimeException("ERROR: UNDEFINED VARIABLE " + name);
		}
		return visit(bound);
	}


	private Object evalListAsProgram(List<?> list) {
		if (list.isEmpty()) {
			return null;
		}

		Object head = list.get(0);

		String funcName;

		if (head instanceof String s) {
			funcName = s;
		} else if (head instanceof AstNode ast) {
			Object fnValue = visit(ast);
			if (fnValue instanceof String s2) {
				funcName = s2;
			} else {
				throw new RuntimeException("Cannot eval list: invalid head " + fnValue);
			}
		} else {
			throw new RuntimeException("Cannot eval list: invalid head " + head);
		}

		List<Object> args = new ArrayList<>();

		for (int i = 1; i < list.size(); i++) {
			Object arg = list.get(i);

			if (arg instanceof List<?> subList) {
				if (!subList.isEmpty() && subList.get(0) instanceof String) {
					arg = evalListAsProgram(subList);
				} else {
				}
			} else if (arg instanceof AstNode astArg) {
				arg = visit(astArg);
			} else if (arg instanceof String atomName) {
				arg = lookupAtomValue(atomName);
			}
			args.add(arg);
		}

		return applyFunctionOrSpecialForm(funcName, args);
	}

	private void checkArity(String funcName, List<Object> args, int expected) {
		if (args.size() != expected) {
			throw new RuntimeException(
					"ERROR: " + funcName + " EXPECTS " + expected + " ARGUMENT(S)"
			);
		}
	}

	private Object applyFunctionOrSpecialForm(String funcName, List<Object> args) {
		switch (funcName) {
			case "plus", "minus", "times", "divide" -> {
				return evalOperation(funcName, args);
			}

			case "head" -> {
				checkArity(funcName, args, 1);
				return evalHead(args.get(0));
			}
			case "tail" -> {
				checkArity(funcName, args, 1);
				return evalTail(args.get(0));
			}
			case "cons" -> {
				checkArity(funcName, args, 2);
				return evalCons(args.get(0), args.get(1));
			}

			case "equal", "nonequal", "less", "lesseq", "greater", "greatereq" -> {
				checkArity(funcName, args, 2);
				return evalComparison(funcName, args.get(0), args.get(1));
			}

			case "isint", "isreal", "isbool", "isnull", "isatom", "islist" -> {
				checkArity(funcName, args, 1);
				return evalPredicate(funcName, args.get(0));
			}

			case "and", "or", "xor", "nor", "nand", "xnor" -> {
				checkArity(funcName, args, 2);
				return evalLogical(funcName, args.get(0), args.get(1));
			}
			case "not" -> {
				checkArity(funcName, args, 1);
				return evalNot(args.get(0));
			}

			case "eval" -> {
				checkArity(funcName, args, 1);
				return evalValue(args.get(0));
			}
		}

		AstNode funcNode = symbolTable.find(funcName);
		if (funcNode == null) {
			throw new RuntimeException("ERROR: UNDEFINED FUNCTION " + funcName);
		}

		ArrayList<String> paramNames;
		AstNode body;

		if (funcNode instanceof FunctionNode fn) {
			paramNames = fn.getParameters();
			body = fn.getBody();
		} else if (funcNode instanceof LambdaNode ln) {
			paramNames = ln.getParameters();
			body = ln.getBody();
		} else {
			throw new RuntimeException("ERROR: " + funcName + " is not a function or lambda");
		}

		if (args.size() < paramNames.size()) {
			throw new RuntimeException("ERROR: TOO FEW ARGUMENTS FOR " + funcName);
		}
		if (args.size() > paramNames.size()) {
			throw new RuntimeException("ERROR: TOO MANY ARGUMENTS FOR " + funcName);
		}

		SymbolTable functionTable = new SymbolTable(symbolTable);

		for (int i = 0; i < paramNames.size(); i++) {
			String paramName = paramNames.get(i);
			Object argVal = args.get(i);

			AstNode argNode;
			if (argVal instanceof AstNode ast) {
				argNode = ast;
			} else {
				argNode = new RuntimeLiteralNode(argVal);
			}
			functionTable.define(paramName, argNode);
		}

		Interpreter funcInterpreter = new Interpreter(functionTable, false);
		return funcInterpreter.visit(body);
	}


	public Object visitReturnNode(ReturnNode returnNode) {
		return returnNode;
	}

	public Object visitBreakNode(BreakNode breakNode) {
		return breakNode;
	}

	public Object visitHeadNode(HeadNode headNode) {
		Object value = visit(headNode.getListExpr());
		return evalHead(value);
	}

	public Object visitTailNode(TailNode tailNode) {
		Object value = visit(tailNode.getListExpr());
		return evalTail(value);
	}

	public Object visitConsNode(ConsNode consNode) {
		Object head = visit(consNode.getItem());
		Object tailVal = visit(consNode.getList());
		return evalCons(head, tailVal);
	}


	public Object visitSetqNode(SetqNode setqNode) {
		String name = setqNode.getName();
		AstNode rhs = setqNode.getValue();

		AstNode toStore;

		if (rhs instanceof QuoteNode) {
			toStore = rhs;
		} else {
			Object value = visit(rhs);

			if (value instanceof AstNode ast) {
				toStore = ast;
			} else {
				toStore = new RuntimeLiteralNode(value);
			}
		}

		symbolTable.define(name, toStore);
		return null;
	}


	public Object visitLambdaNode(LambdaNode node) {
		return node;
	}

	public Object visitCallNode(CallNode node) {
		Object fnValue = visit(node.getCallee());

		if (fnValue instanceof String s) {
			switch (s) {
				case "plus", "minus", "times", "divide" -> {
					ArrayList<Object> evaluatedOperands = new ArrayList<>();
					for (AstNode arg : node.getArguments()) {
						evaluatedOperands.add(visit(arg));
					}
					return evalOperation(s, evaluatedOperands);
				}
				default -> throw new RuntimeException("ERROR: expression does not evaluate to a function");
			}
		}

		ArrayList<String> paramNames;
		AstNode body;

		if (fnValue instanceof LambdaNode lambda) {
			paramNames = lambda.getParameters();
			body = lambda.getBody();
		} else if (fnValue instanceof FunctionNode func) {
			paramNames = func.getParameters();
			body = func.getBody();
		} else {
			throw new RuntimeException("ERROR: expression does not evaluate to a function");
		}

		ArrayList<AstNode> argExprs = node.getArguments();
		if (argExprs.size() != paramNames.size()) {
			throw new RuntimeException("ERROR: function expected " +
					paramNames.size() + " args, got " + argExprs.size());
		}

		SymbolTable functionTable = new SymbolTable(symbolTable);
		for (int i = 0; i < paramNames.size(); i++) {
			String paramName = paramNames.get(i);
			AstNode argAst = argExprs.get(i);

			Object argVal = visit(argAst);
			AstNode stored = (argVal instanceof AstNode ast)
					? ast
					: new RuntimeLiteralNode(argVal);

			functionTable.define(paramName, stored);
		}

		Interpreter funcInterpreter = new Interpreter(functionTable, false);
		Object result = funcInterpreter.visit(body);

		if (result instanceof ReturnNode rn) {
			return funcInterpreter.visit(rn.getValue());
		}

		return result;
	}


	private Object evalPredicate(String predicate, Object value) {
		return switch (predicate) {
			case "isint" -> value instanceof Integer;
			case "isreal" -> value instanceof Double || value instanceof Integer;
			case "isbool" -> value instanceof Boolean;
			case "isnull" -> value == null;
			case "islist" -> value instanceof java.util.List<?>;
			case "isatom" -> value instanceof String;
			default -> value != null;
		};
	}

	private Object evalComparison(String op, Object leftVal, Object rightVal) {
		if (leftVal instanceof Boolean && rightVal instanceof Boolean) {
			boolean l = (Boolean) leftVal;
			boolean r = (Boolean) rightVal;
			return switch (op) {
				case "equal" -> l == r;
				case "nonequal" -> l != r;
				default -> throw new RuntimeException(
						"ERROR: BOOLEAN COMPARISON ONLY SUPPORTS equal/nonequal, got " + op
				);
			};
		}

		double l = ((Number) leftVal).doubleValue();
		double r = ((Number) rightVal).doubleValue();

		return switch (op) {
			case "equal" -> l == r;
			case "nonequal" -> l != r;
			case "less" -> l < r;
			case "lesseq" -> l <= r;
			case "greater" -> l > r;
			case "greatereq" -> l >= r;
			default -> throw new RuntimeException("ERROR: UNKNOWN COMPARISON OPERATOR " + op);
		};
	}

	private Object evalLogical(String operator, Object leftVal, Object rightVal) {
		boolean l = asBoolean(leftVal, "LEFT");
		boolean r = asBoolean(rightVal, "RIGHT");

		return switch (operator) {
			case "and" -> l && r;
			case "or" -> l || r;
			case "xor" -> (l || r) && !(l && r);
			case "nor" -> !(l || r);
			case "nand" -> !(l && r);
			case "xnor" -> !((l || r) && !(l && r));
			default -> throw new RuntimeException("ERROR: UNKNOWN LOGICAL OPERATOR " + operator);
		};
	}

	private Object evalNot(Object value) {
		boolean v = asBoolean(value, "ARG");
		return !v;
	}

	private Object evalHead(Object value) {
		if (!(value instanceof java.util.List<?> list)) {
			throw new RuntimeException("ERROR: HEAD EXPECTED LIST");
		}
		if (list.isEmpty()) {
			throw new RuntimeException("ERROR: EMPTY LIST");
		}
		return list.get(0);
	}

	private Object evalTail(Object value) {
		if (!(value instanceof java.util.List<?> list)) {
			throw new RuntimeException("ERROR: TAIL EXPECTED LIST");
		}
		if (list.isEmpty()) {
			throw new RuntimeException("ERROR: EMPTY LIST");
		}
		return new ArrayList<>(list.subList(1, list.size()));
	}

	private Object evalCons(Object head, Object tailVal) {
		java.util.List<Object> result = new ArrayList<>();
		result.add(head);
		if (tailVal instanceof java.util.List<?> tailList) {
			result.addAll(tailList);
		} else if (tailVal != null) {
			throw new RuntimeException("ERROR: CONS TAIL IS NOT A LIST");
		}
		return result;
	}

	private Number evalOperation(String operator, List<Object> operands) {
		List<Double> numericOperands = operands.stream()
				.map(o -> ((Number) o).doubleValue())
				.toList();

		switch (operator) {
			case "plus" -> {
				double result = numericOperands.stream().mapToDouble(Double::doubleValue).sum();

				if (isInteger(result)) {
					return (int) result;
				} else {
					return result;
				}
			}
			case "minus" -> {
				double result = numericOperands.get(0);
				for (int i = 1; i < numericOperands.size(); i++) {
					result -= numericOperands.get(i);
				}
				if (isInteger(result)) {
					return (int) result;
				} else return result;
			}
			case "times" -> {
				double result = 1.0;
				for (Double operand : numericOperands) {
					result *= operand;
				}
				if (isInteger(result)) {
					return (int) result;
				} else return result;
			}
			case "divide" -> {
				double result = numericOperands.get(0);
				for (int i = 1; i < numericOperands.size(); i++) {
					double divisor = numericOperands.get(i);
					if (divisor == 0) {
						throw new RuntimeException("ERROR: DIVISION BY ZERO");
					}
					result /= divisor;
				}
				if (isInteger(result)) {
					return (int) result;
				} else return result;
			}
			default -> throw new RuntimeException("ERROR: UNKNOWN OPERATOR " + operator);
		}
	}

	private boolean isInteger(double value) {
		return value == Math.floor(value);
	}

	private Object evalCondBranch(AstNode branch) {
		if (branch == null) return null;

		if (branch instanceof AtomNode atom) {
			String name = atom.getValue();
			if (name.equals("plus") ||
					name.equals("minus") ||
					name.equals("times") ||
					name.equals("divide")) {
				return name;
			}
		}

		return visit(branch);
	}
}
