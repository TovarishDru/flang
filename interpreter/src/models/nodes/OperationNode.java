package models.nodes;

import java.util.ArrayList;

import models.token.TokenType;
import models.token.Token;

public class OperationNode extends AstNode {
    String operator;
	ArrayList<AstNode> operands;

	public OperationNode(Token operator, ArrayList<AstNode> operands) {
		this.operator = operator.getValue();
		this.operands = operands;
		super(operands.get(0).getType(), null, operands);
	}

	public String getOperator() {
		return operator;
	}

	public ArrayList<AstNode> getOperands() {
		return operands;
	}
}
