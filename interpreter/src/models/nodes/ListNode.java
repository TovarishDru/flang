package models.nodes;

import models.nodes.AstNode;
import models.nodes.NodeType;

import java.util.ArrayList;
import java.util.stream.Collectors;
import stages.Interpreter;


public class ListNode extends AstNode {
    ArrayList<AstNode> elements;

    public ListNode(ArrayList<AstNode> elements) {
		super(NodeType.LIST, null, elements);
		this.elements = elements;
	}
	
	@Override
    public Object accept(Interpreter interpreter) {
        return interpreter.visitListNode(this);
    }

	public ArrayList<AstNode> getElements() {
		return this.elements;
	}

	@Override
    public String toString() {
        String elementsStr = elements.stream()
				.map(element -> element.toString())
				.collect(Collectors.joining(","));
		return "ListNode(" + elementsStr + ")";
    }
}
