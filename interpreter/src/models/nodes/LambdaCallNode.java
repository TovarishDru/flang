package models.nodes;

import java.util.ArrayList;

public class LambdaCallNode extends AstNode {
    private String lambdaName;
	private ArrayList<AstNode> parameters;

    public LambdaCallNode(String lambdaName, ArrayList<AstNode> parameters) {
        this.lambdaName = lambdaName;
        this.parameters = parameters;
        super(NodeType.LAMBDACALL, null, null);
    }
    
    public String getLambdaName() {
		return lambdaName;
	}

	public ArrayList<AstNode> getParameters() {
		return parameters;
	}
}
