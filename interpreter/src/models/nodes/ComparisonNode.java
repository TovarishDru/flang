package models.nodes;

import java.util.ArrayList;

import models.token.Token;

public class ComparisonNode extends AstNode {
    String comparison;
	AstNode leftElement;
	AstNode rightElement;

	public ComparisonNode(Token operator, AstNode leftElement, AstNode rightElement) {
		super(NodeType.COMP, null, new ArrayList<>());
		this.comparison = operator.getValue();
		this.leftElement = leftElement;
		this.rightElement = rightElement;
        addChild(leftElement);
        addChild(rightElement);
	}

	public String getComparison() {
		return comparison;
	}

	public AstNode getLeftElement() {
		return leftElement;
	}

	public AstNode getRightElement() {
		return rightElement;
	}

	@Override
    public String toString() {
        return "ComparisonNode(" + comparison + ":left=" + 
							leftElement.toString() + ",right=" + rightElement.toString() + ")";
    }
}
