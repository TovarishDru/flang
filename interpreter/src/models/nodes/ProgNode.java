package models.nodes;

import java.util.ArrayList;

public class ProgNode extends AstNode {

    public ProgNode(ArrayList<AstNode> children) {
        super(NodeType.PROG, null, children);
    }
}