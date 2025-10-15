package models.nodes;

import java.util.ArrayList;

public class ReturnNode extends AstNode {
	private final AstNode value;

	public ReturnNode(AstNode value) {
		super(NodeType.RETURN, null, new ArrayList<>());
		this.value = value;
		addChild(value);
	}

	public AstNode getValue() { return value; }
}
