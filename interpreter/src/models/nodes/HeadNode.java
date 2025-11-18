package models.nodes;

import java.util.ArrayList;
import stages.Interpreter;

public class HeadNode extends AstNode {
	private final AstNode listExpr;

	public HeadNode(AstNode listExpr) {
		super(NodeType.HEAD, null, new ArrayList<>());
		this.listExpr = listExpr;
		addChild(listExpr);
	}

	@Override
    public Object accept(Interpreter interpreter) {
        return interpreter.visitHeadNode(this);
    }

	public AstNode getListExpr() { return listExpr; }

	@Override
    public String toString() {
        return "HeadNode(" + listExpr.toString() +")";
    }
}
