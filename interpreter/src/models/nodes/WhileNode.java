package models.nodes;

import java.util.ArrayList;

public class WhileNode extends AstNode {
	private final AstNode condition;
	private final ArrayList<AstNode> body;

	public WhileNode(AstNode condition, ArrayList<AstNode> body) {
		super(NodeType.WHILE, null, new ArrayList<>());
		this.condition = condition;
		this.body = body;
		addChild(condition);
		for (AstNode b : body) addChild(b);
	}

	@Override
    public Object accept(Interpreter interpreter) {
        return interpreter.visitWhileNode(this);
    }

	public AstNode getCondition() { return condition; }
	public ArrayList<AstNode> getBody() { return body; }

	@Override
    public String toString() {
        return "WhileNode(" + condition.toString() + ":" + body.toString() + ")";
    }
}
