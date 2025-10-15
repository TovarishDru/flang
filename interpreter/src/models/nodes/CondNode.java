package models.nodes;

import java.util.ArrayList;

public class CondNode extends AstNode {
	private final AstNode condition;
	private final AstNode action;
	private final AstNode defaultAction;

	public CondNode(AstNode condition, AstNode action, AstNode defaultAction) {
		super(NodeType.COND, null, new ArrayList<>());
		this.condition = condition;
		this.action = action;
		this.defaultAction = defaultAction;
		addChild(condition);
		addChild(action);
		if (defaultAction != null) addChild(defaultAction);
	}

	public AstNode getCondition() { return condition; }
	public AstNode getAction() { return action; }
	public AstNode getDefaultAction() { return defaultAction; }
}
