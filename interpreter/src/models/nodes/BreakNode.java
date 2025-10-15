package models.nodes;

import java.util.ArrayList;

public class BreakNode extends AstNode {
	public BreakNode() {
		super(NodeType.BREAK, null, new ArrayList<>());
	}

	@Override
    public String toString() {
        return "BreakNode()";
    }
}
