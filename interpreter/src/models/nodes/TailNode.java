package models.nodes;

import java.util.ArrayList;

public class TailNode extends AstNode {
	private final AstNode listExpr;

	public TailNode(AstNode listExpr) {
		super(NodeType.TAIL, null, new ArrayList<>());
		this.listExpr = listExpr;
		addChild(listExpr);
	}

	public AstNode getListExpr() { return listExpr; }

	@Override
    public String toString() {
        return "TailNode(" + listExpr.toString() + ")";
    }
}
