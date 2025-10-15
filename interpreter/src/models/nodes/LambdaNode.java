package models.nodes;

import java.util.ArrayList;

public class LambdaNode extends AstNode {
    ArrayList<String> parameters;
	AstNode body;

    public LambdaNode(ArrayList<String> parameters, AstNode body) {
        super(NodeType.LAMBDA, null, new ArrayList<>());
        this.parameters = parameters;
        this.body = body;
    }

    public ArrayList<String> getParameters() {
		return parameters;
	}

	public AstNode getBody() {
		return body;
	}

    @Override
    public String toString() {
        return "QuoteNode(" + quotedExpr.toString() + ")";
    }
}
