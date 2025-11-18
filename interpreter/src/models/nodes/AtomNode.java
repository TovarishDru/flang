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
    public Object accept(Interpreter interpreter) {
        return interpreter.visitAtomNode(this);
    }

    public TokenType getTokenType() {
        return this.type;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return "AtomNode(" + value + ")";
    }
}
