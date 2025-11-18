package models.nodes;

import java.util.ArrayList;

public class ConsNode extends AstNode {
	private final AstNode item;
	private final AstNode list;

	public ConsNode(AstNode item, AstNode list) {
		super(NodeType.CONS, null, new ArrayList<>());
		this.item = item;
		this.list = list;
		addChild(item);
		addChild(list);
	}

	@Override
    public Object accept(Interpreter interpreter) {
        return interpreter.visitConsNode(this);
    }

	public AstNode getItem() { return item; }
	public AstNode getList() { return list; }

	@Override
    public String toString() {
        return "ConsNode(" + item.toString() + ":" + list.toString() + ")";
    }
}
