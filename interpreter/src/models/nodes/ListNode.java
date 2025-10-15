package models.nodes;

import models.nodes.AstNode;
import models.nodes.NodeType;

import java.util.ArrayList;
import java.util.stream.Collectors;


public class ListNode extends AstNode {
    ArrayList<AstNode> elements;

    public ListNode(ArrayList<AstNode> elements) {
		this.elements = elements;
		super(NodeType.LIST, null, elements);
	}

	@Override
    public String toString() {
        String elementsStr = elements.stream()
				.map(element -> element.accept(this))
				.collect(Collectors.joining(","));
		return "ListNode(" + elementsStr + ")";
    }
}
