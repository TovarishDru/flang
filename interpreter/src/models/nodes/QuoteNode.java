package models.nodes;

public class QuoteNode extends AstNode {
    private AstNode quotedExpr;

	public QuoteNode(AstNode quotedExpr) {
		this.quotedExpr = quotedExpr;
		super(NodeType.QUOTE, null, null);
	}

	public AstNode getQuotedExpr() {
		return quotedExpr;
	}
}
