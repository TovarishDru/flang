package models.nodes;

import java.util.ArrayList;
import java.util.stream.Collectors;

import models.nodes.FunctionNode;
import models.nodes.NodeType;
import stages.Interpreter;

public class FunctionCallNode extends AstNode {
    String functionName;
	ArrayList<AstNode> parameters;

    public FunctionCallNode(String functionName, ArrayList<AstNode> parameters) {
		super(NodeType.FUNCCALL, null, new ArrayList<>());
		this.functionName = functionName;
		this.parameters = parameters;
	}

	@Override
    public Object accept(Interpreter interpreter) {
        return interpreter.visitFunctionCallNode(this);
    }

    public String getFunctionName() {
		return functionName;
	}

	public ArrayList<AstNode> getParameters() {
		return parameters;
	}

	@Override
    public String toString() {
		String stringParams = parameters.stream().map(element -> element.toString()).collect(Collectors.joining(","));

        return "FunctionCallNode(" + functionName + ":" + stringParams + ")";
    }
}
