package models.nodes;

import java.util.ArrayList;

public class FunctionNode extends AstNode {
    String functionName;
	ArrayList<String> parameters;
	AstNode body;

    public FunctionNode(String functionName, ArrayList<String> parameters, AstNode body) {
        this.functionName = functionName;
        this.parameters = parameters;
        this.body = body;
        super(NodeType.FUNC, null, null);
    }

    public String getFunctionName() {
		return functionName;
	}

	public ArrayList<String> getParameters() {
		return parameters;
	}

	public AstNode getBody() {
		return body;
	}
}
