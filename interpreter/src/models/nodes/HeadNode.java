package models.nodes;

import java.util.ArrayList;

public class HeadNode extends AstNode {
	private final AstNode listExpr;

	public HeadNode(AstNode listExpr) {
		super(NodeType.HEAD, null, new ArrayList<>());
		this.listExpr = listExpr;
		addChild(listExpr);
	}

	public AstNode getListExpr() { return listExpr; }

	@Override
    public String toString() {
        return "HeadNode(" + listExpr.toString() +")";
    }
}
