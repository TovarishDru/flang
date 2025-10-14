package models.nodes;

import models.token.Token;
import models.token.TokenType;

public class AtomNode extends AstNode {
    private String value;
    private TokenType type;

    public AtomNode(Token token) {
        this.value = token.getValue();
        this.type = token.getType();
        super(NodeType.ATOM, null, null);
    }
}
