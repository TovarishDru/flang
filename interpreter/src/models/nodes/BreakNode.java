package models.nodes;

import java.util.ArrayList;
import stages.Interpreter;

public class BreakNode extends AstNode {
	public BreakNode() {
		super(NodeType.BREAK, null, new ArrayList<>());
	}

	@Override
    public Object accept(Interpreter interpreter) {
        return interpreter.visitBreakNode(this);
    }

	@Override
    public String toString() {
        return "BreakNode()";
    }
}
