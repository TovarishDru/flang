package models.nodes;

import java.util.ArrayList;

import models.token.Token;
import models.token.TokenType;

public class AtomNode extends AstNode {
    private String value;
    private TokenType type;

    public AtomNode(Token token) {
        super(NodeType.ATOM, null, new ArrayList<>());
        this.value = token.getValue();
        this.type = token.getType();
    }

    @Override
    public String toString() {
        return "AtomNode(" + value + ")";
    }
}
