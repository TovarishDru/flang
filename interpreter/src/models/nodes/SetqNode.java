package models.nodes;

import java.util.ArrayList;

public class SetqNode extends AstNode {
	private final String name;
	private final AstNode value;

	public SetqNode(String name, AstNode value) {
		super(NodeType.SETQ, null, new ArrayList<>());
		this.name = name;
		this.value = value;
		addChild(value);
	}

	public String getName() {
		return name;
	}

	public AstNode getValue() {
		return value;
	}

	@Override
    public String toString() {
        return "SetqNode(" + name.toString() + ":" + value.toString() + ")";
    }
}
