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

    public Object InterpretProgNode(ProgNode progNode) {
        Object result = null;

        for (AstNode childNode : progNode.getChildren()) {
            if (childNode instanceof ProgNode) {
                Interpreter localInterpreter = new Interpreter(symbolTable, false);
                result = localInterpreter.InterpretProgNode(childNode);
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
        try {
            return visit(symbolTable.find(atomNode.getValue()));
        } catch (Exception e) {
            throw new RuntimeException("ERROR: " + e.getMessage());
        }
    }

    public Object visitLiteralNode(LiteralNode literalNode) {
        String value = literalNode.getValue();

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

        switch (predicate) {
            case "isint" -> {
				if (predicateNode.getElement().accept(this) instanceof Integer) {
					return true;
				} else return false;
			}
			case "isreal" -> {
				if (predicateNode.getElement().accept(this) instanceof Double ||
						predicateNode.getElement().accept(this) instanceof Integer) {
					return true;
				} else return false;
			}
			case "isbool" -> {
				if (predicateNode.getElement().accept(this) instanceof Boolean) {
					return true;
				} else return false;
			}
			case "isnull" -> {
				if (predicateNode.getElement() instanceof NullNode) {
					return true;
				} else return false;
			}
			case "islist" -> {
				if (predicateNode.getElement() instanceof ConsNode) {
					return true;
				} else return false;
			}
        }

        Object value = visit(predicateNode.getArgument());
		return value != null;
    }

    private Number evalOperation(String operator, List<Object> operands) {
        List<Double> numericOperands = operands.stream()
				.map(o -> ((Number) o).doubleValue())
				.toList();

        switch (oprator) {
            case "plus" -> {
                Double result = numericOperands.stream().mapToDouble(Double::doubleValue).sum();

                if (isInteger(result)) {
                    result (Integer) result;
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

	public Object visitConditionNode(CondNode condNode) {

        AstNode condition = condNode.getCondition();
        AstNode action = condNode.getAction();
        AstNode defaultAction = condNode.getDefaultAction();

		if ((boolean) condition.accept(this)) {
			return action.accept(this);
		} else try {
			return defaultAction.accept(this);
		} catch (NullPointerException e) {
			return null;
		}
	}

	public Object visitWhileNode(WhileNode whileNode) {
		boolean breakflag = false;
		while ((boolean) visit(whileNode.getCondition())) {
			if (breakflag) break;
			for (ASTNode node : whileNode.getBody()) {
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

	public Object visitNullNode(NullNode nullNode) {
		return null;
	}

	public Object visitBoolNode(BooleanNode booleanNode) {
		return booleanNode.getValue();
	}

	public Object visitNotNode(NotNode notNode) {
		Object value = visit(notNode.getElement());
		if (value instanceof boolean) {
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

	public Object visitLogicalOperationNode(LogicalOperationNode logicalNode) {
		Object left = visit(logicalNode.getChildren().get(0));
		Object right = visit(logicalNode.getChildren().get(1));
		String operator = logicalNode.getOperator();

		if (left instanceof boolean && right instanceof boolean) {
			boolean l = (boolean) left;
			boolean r = (boolean) right;
			return switch (operator) {
				case "and" -> l && r;
				case "or" -> l || r;
				case "xor" -> (l || r) && !(l && r);
				case "nor" -> !(l || r);
				case "nand" -> !(l && r);
				case "xnor" -> !((l || r) && !(l && r));
				default -> throw new RuntimeException("ERROR: UNKNOWN LOGICAL OPERATOR");
			};
		}
		throw new RuntimeException("ERROR: UNKNOWN LOGICAL OPERATOR " + operator);
	}

	public Object visitConsNode(ConsNode consNode) {
		Object head = visit(consNode.getHead());
		List<Object> tail = (List<Object>) visit(consNode.getTail());
		List<Object> result = new ArrayList<>();
		result.add(head);
		result.addAll(tail);
		return result;
	}

    public Object visitFunctionCallNode(FunctionCallNode functionCallNode) {
        try {
            Object function = symbolTable.find(functionCallNode.getFunctionName());
            SymbolTable functionTable = new SymbolTable(symbolTable);
            ArrayList<AstNode> parametersValues = functionCallNode.getParameters();
            ArrayList<String> parametersNames;

            if (function instanceof LambdaNode) {
                LambdaNode function = (LambdaNode) function;
                parametersNames = function.getParameters();

                for (int i = 0; i < parametersNames.size(); i++) {
                    Object parameterValue = visit(parametersValues.get(i));
                    functionTable.define(parametersNames.get(i), new LiteralNode(parameterValue.toString()));
                }

                Interpreter funcInterpreter = new Interpreter(functionTable, false);
                return funcInterpreter.visit(function.getBody());
            } else if (function instanceof FunctionNode) {
                LambdaNode function = (FunctionNode) function;
                parametersNames = function.getParameters();

                for (int i = 0; i < parametersNames.size(); i++) {
                    Object parameterValue = visit(parametersValues.get(i));
                    functionTable.define(parametersNames.get(i), new LiteralNode(parameterValue.toString()));
                }

                Interpreter funcInterpreter = new Interpreter(functionTable, false);
                return funcInterpreter.visit(function.getBody());
            }
        } catch (Exception e) {
			throw new RuntimeException("ERROR: IN FUNCTION CALL " + e.getMessage());
		}
		return null;
    }
}
