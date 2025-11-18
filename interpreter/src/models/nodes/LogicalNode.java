package models.nodes;

import java.util.ArrayList;
import java.util.List;

import stages.Interpreter;

public class LogicalNode extends AstNode {
	private final String operator; // "and" | "or" | "xor"
	private final AstNode left;
	private final AstNode right;

	public LogicalNode(String operator, AstNode left, AstNode right) {
		super(NodeType.LOGICALOP, null, new ArrayList<>(List.of(left, right)));
		this.operator = operator;
		this.left = left;
		this.right = right;
		addChild(left);
		addChild(right);
	}

	@Override
    public Object accept(Interpreter interpreter) {
        return interpreter.visitLogicalNode(this);
    }

	public String getOperator() { return operator; }
	public AstNode getLeft() { return left; }
	public AstNode getRight() { return right; }

	 @Override
    public String toString() {
        return "AstNode(" + operator.toString() + ",left=" + left.toString() +  ",right=" + right.toString() + ")";
    }

}
