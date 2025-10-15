package models.nodes;

import models.nodes.NodeType;

import java.util.ArrayList;

public abstract class AstNode {
    protected NodeType type;
    protected AstNode parent;
    protected ArrayList<AstNode> children;

    public AstNode(NodeType type, AstNode parent, ArrayList<AstNode> children) {
        this.type = type;
        this.parent = parent;
        this.children = children;
    }

    public NodeType getType() {
        return type;
    }

    public void addChild(AstNode child) {
        children.add(child);
    }

    @Override
    public String toString() {
        return "AstNode()";
    }
}
