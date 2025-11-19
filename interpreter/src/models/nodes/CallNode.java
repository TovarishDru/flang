package models.nodes;

import stages.Interpreter;
import java.util.ArrayList;

public class CallNode extends AstNode {
	private final AstNode callee;
	private final ArrayList<AstNode> arguments;

	public CallNode(AstNode callee, ArrayList<AstNode> arguments) {
		super(NodeType.CALL, null, new ArrayList<>());
		this.callee = callee;
		this.arguments = arguments;
		addChild(callee);
		for (AstNode arg : arguments) addChild(arg);
	}

	public AstNode getCallee() {
		return callee;
	}

	public ArrayList<AstNode> getArguments() {
		return arguments;
	}

	@Override
	public Object accept(Interpreter interpreter) {
		return interpreter.visitCallNode(this);
	}

	@Override
	public String toString() {
		return "CallNode(" + callee + ", " + arguments + ")";
	}
}

