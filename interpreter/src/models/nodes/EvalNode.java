package models.nodes;

import java.util.ArrayList;
import stages.Interpreter;

public class EvalNode extends AstNode {
	private final AstNode expr;

	public EvalNode(AstNode expr) {
		super(NodeType.EVAL, null, new ArrayList<>());
		this.expr = expr;
		addChild(expr);
	}
	
	@Override
    public Object accept(Interpreter interpreter) {
        return interpreter.visitEvalNode(this);
    }

	public AstNode getExpr() { return expr; }

	 @Override
    public String toString() {
        return "EvalNode(" + expr.toString() + ")";
    }
}
