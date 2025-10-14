package models.nodes;

import models.token.Token;
import models.token.TokenType;

public class LiteralNode extends AstNode {
    private String value;
    private TokenType type;

    public LiteralNode(Token token) {
        this.value = token.getValue();
        this.type = token.getType();
        super(NodeType.LITERAL, null, null);
    }
}