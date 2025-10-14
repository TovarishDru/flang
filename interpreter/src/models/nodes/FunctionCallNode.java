package models.nodes;

import java.utils.ArrayList;

import models.nodes.FunctionNode;
import models.nodes.NodeType;

public class FunctionCallNode extends AstNode {
    String functionName;
	ArrayList<AstNode> parameters;

    public FunctionCallNode(FunctionNode node) {
		this.functionName = node.getFunctionName();
		this.parameters = node.getParameters();
        super(NodeType.FUNCCALL, null, null);
	}

    public String getFunctionName() {
		return functionName;
	}

	public ArrayList<ASTNode> getParameters() {
		return parameters;
	}
}
