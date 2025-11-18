package models.nodes;

import java.util.ArrayList;

import models.token.Token;
import models.token.TokenType;
import stages.Interpreter;

public class LiteralNode extends AstNode {
    private String value;
    private TokenType type;

    public LiteralNode(Token token) {
        super(NodeType.LITERAL, null, new ArrayList<>());
        this.value = token.getValue();
        this.type = token.getType();
    }

    public LiteralNode(String value, TokenType type) {
        super(NodeType.LITERAL, null, new ArrayList<>());
        this.value = value;
        this.type = type;
    }

    @Override
    public Object accept(Interpreter interpreter) {
        return interpreter.visitLiteralNode(this);
    }

    public String getValue() {
        return value;
    }

    public TokenType getTokenType() {
        return type;
    }

    @Override
    public String toString() {
        return "LiteralNode(" + value + ")";
    }
}
