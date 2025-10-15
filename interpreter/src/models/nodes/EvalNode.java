package models.nodes;

import java.util.ArrayList;

public class EvalNode extends AstNode {
	private final AstNode expr;

	public EvalNode(AstNode expr) {
		super(NodeType.EVAL, null, new ArrayList<>());
		this.expr = expr;
		addChild(expr);
	}

	public AstNode getExpr() { return expr; }
}
