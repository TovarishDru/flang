package models.nodes;

import java.util.ArrayList;
import java.util.stream.Collectors;

import models.token.TokenType;
import models.token.Token;

public class OperationNode extends AstNode {
	String operator;
	ArrayList<AstNode> operands;
	TokenType type;

	public OperationNode(Token operator, ArrayList<AstNode> operands) {
		super(NodeType.OPERATION, null, operands);
		this.operator = operator.getValue();
		this.type = operator.getType();
		this.operands = operands;
	}

	public String getOperator() {
		return operator;
	}

	public TokenType getOperatorType() {
		return type;
	}

	public ArrayList<AstNode> getOperands() {
		return operands;
	}

	@Override
	public String toString() {
		String operandsStr = operands.stream()
				.map(operand -> operand.toString())
				.collect(Collectors.joining(", "));
		return "OperationNode(" + operator + ":" + operandsStr + ")";
	}
}
