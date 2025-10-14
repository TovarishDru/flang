package models.nodes;

import java.util.ArrayList;

public class LambdaNode extends AstNode {
    ArrayList<String> parameters;
	AstNode body;

    public LambdaNode(ArrayList<String> parameters, AstNode body) {
        this.parameters = parameters;
        this.body = body;
        super(NodeType.LAMBDA, null, null);
    }

    public ArrayList<String> getParameters() {
		return parameters;
	}

	public AstNode getBody() {
		return body;
	}
}
