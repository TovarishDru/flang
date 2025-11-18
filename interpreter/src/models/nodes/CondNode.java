package models.nodes;

import java.util.ArrayList;
import stages.Interpreter;


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

	@Override
    public Object accept(Interpreter interpreter) {
        return interpreter.visitCondNode(this);
    }

	public AstNode getCondition() { return condition; }
	public AstNode getAction() { return action; }
	public AstNode getDefaultAction() { return defaultAction; }

	@Override
    public String toString() {
		if (defaultAction != null) {
			return "CondNode(" + condition.toString() + "?" + action.toString() + ":" + defaultAction.toString() + ")";
		} else {
			return "CondNode(" + condition.toString() + "?" + action.toString() + ")";
		}   
    }
}
