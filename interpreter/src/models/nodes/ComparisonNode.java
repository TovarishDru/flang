package models.nodes;

import models.token.Token;

public class ComparisonNode extends AstNode {
    String comparison;
	AstNode leftElement;
	AstNode rightElement;

	public ComparisonNode(Token operator, AstNode leftElement, AstNode rightElement) {
		this.comparison = operator.getValue();
		this.leftElement = leftElement;
		this.rightElement = rightElement;
        super(NodeType.COMP, null, null);
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
}
