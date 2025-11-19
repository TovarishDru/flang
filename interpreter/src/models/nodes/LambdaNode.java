package models.nodes;

import java.util.ArrayList;
import stages.Interpreter;

public class LambdaNode extends AstNode {
    ArrayList<String> parameters;
    AstNode body;
    ArrayList<AstNode> arguments;

    public LambdaNode(ArrayList<String> parameters, AstNode body) {
        super(NodeType.LAMBDA, null, new ArrayList<>());
        this.parameters = parameters;
        this.body = body;
        this.arguments = new ArrayList<>();
    }

    public ArrayList<String> getParameters() {
        return parameters;
    }

    public AstNode getBody() {
        return body;
    }

    public ArrayList<AstNode> getArguments() {
        return arguments;
    }

    public void setArguments(ArrayList<AstNode> arguments) {
        this.arguments = arguments;
    }

    @Override
    public Object accept(Interpreter interpreter) {
        return interpreter.visitLambdaNode(this);
    }

    @Override
    public String toString() {
        String stringParams = String.join(",", parameters);
        return "LambdaNode(" + stringParams + ": " + body + ")";
    }
}
