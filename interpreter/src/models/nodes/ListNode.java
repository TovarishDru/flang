package models.nodes;

import models.nodes.AstNode;
import models.nodes.NodeType;

import java.util.ArrayList;

public class ListNode extends AstNode {
    ArrayList<AstNode> elements;

    public ListNode(ArrayList<AstNode> elements) {
		this.elements = elements;
		super(NodeType.LIST, null, elements);
	}
}
