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

		return switch (predicate) {
			case "isint" -> value instanceof Integer;
			case "isreal" -> value instanceof Double || value instanceof Integer;
			case "isbool" -> value instanceof Boolean;
			case "isnull" -> value == null;
			case "islist" -> value instanceof java.util.List<?>;
			case "isatom" -> !(value instanceof java.util.List<?>);
			default -> value != null;
		};
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

	// because we simplified it in semanter
	public Object visitCondNode(CondNode condNode) {
		java.util.List<AstNode> kids = condNode.getChildren();

		AstNode condition;
		AstNode thenBranch;
		AstNode elseBranch;

		if (kids != null && !kids.isEmpty()) {
			condition   = kids.get(0);
			thenBranch  = kids.size() >= 2 ? kids.get(1) : null;
			elseBranch  = kids.size() >= 3 ? kids.get(2) : null;
		} else {
			condition   = condNode.getCondition();
			thenBranch  = condNode.getAction();
			elseBranch  = condNode.getDefaultAction();
		}

		Object condVal = visit(condition);
		if (!(condVal instanceof Boolean)) {
			throw new RuntimeException("ERROR: COND CONDITION IS NOT BOOLEAN");
		}

		if ((Boolean) condVal) {
			return thenBranch != null ? visit(thenBranch) : null;
		} else {
			return elseBranch != null ? visit(elseBranch) : null;
		}
	}

	public Object visitWhileNode(WhileNode whileNode) {
		boolean breakflag = false;
		while ((boolean) visit(whileNode.getCondition())) {
			if (breakflag) break;
			for (AstNode node : whileNode.getBody()) {
				visit(node);
				if (node instanceof BreakNode) {
					breakflag = true;
					break;
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
		if (value instanceof Boolean) {
			return !(boolean) value;
		}
		throw new RuntimeException("ERROR: EXPECTED BOOLEAN FOR NOT");
	}

	public Object visitComparisonNode(ComparisonNode comparisonNode) {
		double left = ((Number) visit(comparisonNode.getLeftElement())).doubleValue();
		double right = ((Number) visit(comparisonNode.getRightElement())).doubleValue();

		return switch (comparisonNode.getComparison()) {
			case "equal" -> left == right;
			case "nonequal" -> left != right;
			case "less" -> left < right;
			case "lesseq" -> left <= right;
			case "greater" -> left > right;
			case "greatereq" -> left >= right;
			default -> throw new RuntimeException("ERROR: UNKNOWN COMPARISON OPERATOR");
		};
	}

	public Object visitLogicalNode(LogicalNode logicalNode) {
		Object leftVal = visit(logicalNode.getChildren().get(0));
		Object rightVal = visit(logicalNode.getChildren().get(1));
		String operator = logicalNode.getOperator();

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

	public Object visitConsNode(ConsNode consNode) {
		Object head = visit(consNode.getItem());
		Object tailVal = visit(consNode.getList());

		List<Object> result = new ArrayList<>();
		result.add(head);
		if (tailVal instanceof List<?> tailList) {
			result.addAll(tailList);
		} else if (tailVal != null) {
			throw new RuntimeException("ERROR: CONS TAIL IS NOT A LIST");
		}

		return result;
	}

	public Object visitFunctionCallNode(FunctionCallNode functionCallNode) {
		String funcName = functionCallNode.getFunctionName();

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

		ArrayList<AstNode> argExprs = functionCallNode.getParameters();

		if (argExprs.size() < paramNames.size()) {
			throw new RuntimeException("ERROR: TOO FEW ARGUMENTS FOR " + funcName);
		}
		if (argExprs.size() > paramNames.size()) {
			throw new RuntimeException("ERROR: TOO MANY ARGUMENTS FOR " + funcName);
		}

		SymbolTable functionTable = new SymbolTable(symbolTable);

		for (int i = 0; i < paramNames.size(); i++) {
			String paramName = paramNames.get(i);
			AstNode argAst = argExprs.get(i);
			functionTable.define(paramName, argAst);
		}

		Interpreter funcInterpreter = new Interpreter(functionTable, false);
		return funcInterpreter.visit(body);
	}

	public Object visitListNode(ListNode listNode) {
		List<Object> values = new ArrayList<>();
		for (AstNode element : listNode.getElements()) {
			values.add(visit(element));
		}
		return values;
	}

	public Object visitQuoteNode(QuoteNode quoteNode) {
		return quoteNode.getQuotedExpr();
	}

	public Object visitEvalNode(EvalNode evalNode) {
		Object evalResult = visit(evalNode.getExpr());
		if (evalResult instanceof AstNode) {
			return visit((AstNode) evalResult);
		}
		throw new RuntimeException("ERROR: UNEXPECTED ARGUMENT FOR EVAL");
	}

	public Object visitReturnNode(ReturnNode returnNode) {
		return visit(returnNode.getValue());
	}

	public Object visitBreakNode(BreakNode breakNode) {
		return true;
	}

	public Object visitHeadNode(HeadNode headNode) {
		Object value = visit(headNode.getListExpr());
		if (!(value instanceof List<?> list)) {
			throw new RuntimeException("ERROR: HEAD EXPECTED LIST");
		}
		if (list.isEmpty()) {
			throw new RuntimeException("ERROR: EMPTY LIST");
		}
		return list.get(0);
	}

	public Object visitTailNode(TailNode tailNode) {
		Object value = visit(tailNode.getListExpr());
		if (!(value instanceof List<?> list)) {
			throw new RuntimeException("ERROR: TAIL EXPECTED LIST");
		}
		if (list.isEmpty()) {
			throw new RuntimeException("ERROR: EMPTY LIST");
		}
		return list.subList(1, list.size());
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
		if (node.getArguments() == null || node.getArguments().isEmpty()) {
			return node;
		}

		SymbolTable localTable = new SymbolTable(symbolTable);

		ArrayList<String> params = node.getParameters();
		ArrayList<AstNode> args = node.getArguments();

		for (int i = 0; i < params.size(); i++) {
			String name = params.get(i);
			AstNode argAst = (i < args.size()) ? args.get(i) : null;
			localTable.define(name, argAst);
		}

		Interpreter inner = new Interpreter(localTable, false);
		return inner.visit(node.getBody());
	}
}
