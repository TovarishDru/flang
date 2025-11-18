package models.nodes;

import java.util.ArrayList;

import stages.Interpreter;

public class QuoteNode extends AstNode {
    private AstNode quotedExpr;

	public QuoteNode(AstNode quotedExpr) {
		super(NodeType.QUOTE, null, new ArrayList<>());
		this.quotedExpr = quotedExpr;
	}

	@Override
    public Object accept(Interpreter interpreter) {
        return interpreter.visitQuoteNode(this);
    }

	public AstNode getQuotedExpr() {
		return quotedExpr;
	}

	@Override
    public String toString() {
        return "QuoteNode(" + quotedExpr.toString() + ")";
    }
}
