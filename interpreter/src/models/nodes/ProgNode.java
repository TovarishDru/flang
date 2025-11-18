package models.nodes;

import java.util.ArrayList;
import java.util.stream.Collectors;

import stages.Interpreter;

public class ProgNode extends AstNode {
    public ProgNode(ArrayList<AstNode> children) {
        super(NodeType.PROG, null, children);
    }

    @Override
    public Object accept(Interpreter interpreter) {
        return interpreter.visitProgNode(this);
    }

    @Override
    public String toString() {
        String statementsStr = children.stream()
				.map(statement -> statement.toString())
				.collect(Collectors.joining(","));
		return "ProgNode(" + statementsStr + ")";
    }
}