package models.nodes;

import stages.Interpreter;
import java.util.ArrayList;

public class RuntimeLiteralNode extends AstNode {
	private final Object value;

	public RuntimeLiteralNode(Object value) {
		super(NodeType.LITERAL, null, new ArrayList<>());
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public Object accept(Interpreter interpreter) {
		return value;
	}

	@Override
	public String toString() {
		return "RuntimeLiteral(" + value + ")";
	}
}
