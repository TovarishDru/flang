package models.nodes;

import java.util.ArrayList;

public class FunctionNode extends AstNode {
    String functionName;
	ArrayList<String> parameters;
	AstNode body;

    public FunctionNode(String functionName, ArrayList<String> parameters, AstNode body) {
		super(NodeType.FUNC, null, new ArrayList<>());
        this.functionName = functionName;
        this.parameters = parameters;
        this.body = body;
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

	@Override
    public String toString() {
		String stringParams = String.join(",", parameters);

        return "FunctionNode(" + stringParams + ")";
    }
}
