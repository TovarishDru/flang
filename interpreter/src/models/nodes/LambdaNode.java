package models.nodes;

import java.util.ArrayList;
import stages.Interpreter;

public class LambdaNode extends AstNode {
    ArrayList<String> parameters;
	AstNode body;

    public LambdaNode(ArrayList<String> parameters, AstNode body) {
        super(NodeType.LAMBDA, null, new ArrayList<>());
        this.parameters = parameters;
        this.body = body;
    }

    @Override
    public Object accept(Interpreter interpreter) {
        return interpreter.visitLambdaNode(this);
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
        
        return "LambdaNode(" + stringParams + ": " + body + ")";
    }
}
