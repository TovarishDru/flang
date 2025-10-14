package models.nodes;

import java.util.ArrayList;

public class LambdaCallNode extends AstNode {
    private String lambdaName;
	private ArrayList<ASTNode> parameters;

    public LambdaCallNode(LambdaNode node) {
        this.lambdaName = node.getLambdaName();
        this.parameters = node.getParameters();
        super(NodeType.LAMBDACALL, null, null);
    }
    
    public String getLambdaName() {
		return lambdaName;
	}

	public ArrayList<AstNode> getParameters() {
		return parameters;
	}
}
