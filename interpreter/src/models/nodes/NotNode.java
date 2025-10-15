package models.nodes;

import java.util.ArrayList;

public class NotNode extends AstNode {
	private final AstNode argument;

	public NotNode(AstNode argument) {
		super(NodeType.NOT, null, new ArrayList<>());
		this.argument = argument;
		addChild(argument);
	}

	public AstNode getArgument() { return argument; }
}
