package models.nodes;

import java.util.ArrayList;

import models.nodes.FunctionNode;
import models.nodes.NodeType;

public class FunctionCallNode extends AstNode {
    String functionName;
	ArrayList<AstNode> parameters;

    public FunctionCallNode(String functionName, ArrayList<AstNode> parameters) {
		this.functionName = functionName;
		this.parameters = parameters;
        super(NodeType.FUNCCALL, null, null);
	}

    public String getFunctionName() {
		return functionName;
	}

	public ArrayList<AstNode> getParameters() {
		return parameters;
	}
}
