package models.nodes;

import java.util.ArrayList;

public class QuoteNode extends AstNode {
    private AstNode quotedExpr;

	public QuoteNode(AstNode quotedExpr) {
		super(NodeType.QUOTE, null, new ArrayList<>());
		this.quotedExpr = quotedExpr;
	}

	public AstNode getQuotedExpr() {
		return quotedExpr;
	}

	@Override
    public String toString() {
        return "QuoteNode(" + quotedExpr.toString() + ")";
    }
}
