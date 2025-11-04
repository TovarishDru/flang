package models.nodes;

import java.util.ArrayList;

import models.token.Token;
import models.token.TokenType;

public class LiteralNode extends AstNode {
    private String value;
    private TokenType type;

    public LiteralNode(Token token) {
        super(NodeType.LITERAL, null, new ArrayList<>());
        this.value = token.getValue();
        this.type = token.getType();
    }

    public TokenType getTokenType() {
        return this.type;
    }

    @Override
    public String toString() {
        return "LiteralNode(" + value + ")";
    }
}