package stages;

import models.nodes.*;
import models.token.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Semanter {

    public interface Typed {
        Object getDataType();
    }

    public static class SemanticException extends Exception {
        public SemanticException(String message) {
            super(message);
        }
    }

    private final NodeType arithmeticKind;
    private final NodeType logicalKind;
    private final NodeType comparisonKind;


    public Semanter() {
        this.arithmeticKind = NodeType.OPERATION;
        this.logicalKind = NodeType.LOGICALOP;
        this.comparisonKind = NodeType.COMP;
    }

    private static SemanticException error(String fmt, Object... args) {
        return new SemanticException(String.format(fmt, args));
    }

    private static String pathString(Deque<Integer> path) {
        if (path.isEmpty()) return "/";
        StringBuilder sb = new StringBuilder();
        for (Integer i : path) sb.append('/').append(i);
        return sb.toString();
    }

    public void optimize(AstNode root) {
        if (root == null) return;
        root = performOptimizations(root);
    }

    private AstNode performOptimizations(AstNode root) {
        root = constantFold(root);
        root = simplifyConditionals(root);
        return root;
    }

    private AstNode constantFold(AstNode node) {
        if (node == null) return null;

        List<AstNode> kids = node.getChildren();
        if (kids != null) {
            for (int i = 0; i < kids.size(); i++) {
                kids.set(i, constantFold(kids.get(i)));
            }
        }

        NodeType kind = node.getType();
        Optional<AstNode> optResult = Optional.empty();

        // unary NOT
        if (kind == NodeType.NOT && kids != null && kids.size() == 1) {
            Boolean b = asBoolLiteral(kids.get(0));
            if (b != null) optResult = Optional.of(makeBoolLiteral(!b));
        }

        // arithmetic
        else if (kind == NodeType.OPERATION && kids != null && kids.size() == 2) {
            Number L = asNumberLiteral(kids.get(0));
            Number R = asNumberLiteral(kids.get(1));
            if (L != null && R != null) {
                TokenType op = readOperatorType(node);
                if (op == null) op = mapWordToType(readOperatorWord(node));
                double l = L.doubleValue(), r = R.doubleValue();
                if (op == TokenType.PLUS) optResult = Optional.of(makeNumberLiteral(l + r));
                if (op == TokenType.MINUS) optResult = Optional.of(makeNumberLiteral(l - r));
                if (op == TokenType.TIMES) optResult = Optional.of(makeNumberLiteral(l * r));
                if (op == TokenType.DIVIDE) {
                    if (r != 0.0) optResult = Optional.of(makeNumberLiteral(l / r));
                }
            }
        }

        // logical
        else if (kind == NodeType.LOGICALOP && kids != null && kids.size() == 2) {
            TokenType op = readOperatorType(node);
            if (op == null) op = mapWordToType(readOperatorWord(node));
            Boolean LB = asBoolLiteral(kids.get(0));
            Boolean RB = asBoolLiteral(kids.get(1));
            if (op == TokenType.AND) {
                if (Boolean.FALSE.equals(LB)) optResult = Optional.of(makeBoolLiteral(false));
                if (Boolean.TRUE.equals(LB) && RB != null) optResult = Optional.of(makeBoolLiteral(RB));
                if (Boolean.TRUE.equals(RB) && LB != null) optResult = Optional.of(makeBoolLiteral(LB));
                if (LB != null && RB != null) optResult = Optional.of(makeBoolLiteral(LB && RB));
            } else if (op == TokenType.OR) {
                if (Boolean.TRUE.equals(LB)) optResult = Optional.of(makeBoolLiteral(true));
                if (Boolean.FALSE.equals(LB) && RB != null) optResult = Optional.of(makeBoolLiteral(RB));
                if (Boolean.FALSE.equals(RB) && LB != null) optResult = Optional.of(makeBoolLiteral(LB));
                if (LB != null && RB != null) optResult = Optional.of(makeBoolLiteral(LB || RB));
            } else if (op == TokenType.XOR) {
                if (LB != null && RB != null) optResult = Optional.of(makeBoolLiteral(LB ^ RB));
            }
        }

        // comparisons
        else if (kind == NodeType.COMP && kids != null && kids.size() == 2) {
            TokenType op = readOperatorType(node);
            if (op == null) op = mapWordToType(readOperatorWord(node));

            Number Ln = asNumberLiteral(kids.get(0));
            Number Rn = asNumberLiteral(kids.get(1));
            if (Ln != null && Rn != null) {
                double l = Ln.doubleValue(), r = Rn.doubleValue();
                if (op == TokenType.EQUAL) optResult = Optional.of(makeBoolLiteral(l == r));
                if (op == TokenType.NONEQUAL) optResult = Optional.of(makeBoolLiteral(l != r));
                if (op == TokenType.LESS) optResult = Optional.of(makeBoolLiteral(l < r));
                if (op == TokenType.LESSEQ) optResult = Optional.of(makeBoolLiteral(l <= r));
                if (op == TokenType.GREATER) optResult = Optional.of(makeBoolLiteral(l > r));
                if (op == TokenType.GREATEREQ) optResult = Optional.of(makeBoolLiteral(l >= r));
            }

            Boolean Lb = asBoolLiteral(kids.get(0));
            Boolean Rb = asBoolLiteral(kids.get(1));
            if (Lb != null && Rb != null) {
                if (op == TokenType.EQUAL) optResult = Optional.of(makeBoolLiteral(Objects.equals(Lb, Rb)));
                if (op == TokenType.NONEQUAL) optResult = Optional.of(makeBoolLiteral(!Objects.equals(Lb, Rb)));
            }
        }

        if (optResult.isPresent()) {
            System.out.println("Successfully found optimization for: " + node.toString() + ". Reduced to: " + optResult.get().toString());
            return optResult.get();
        }

        return node;
    }

    private AstNode simplifyConditionals(AstNode node) {
        if (node == null) return null;

        List<AstNode> kids = node.getChildren();
        if (kids != null) {
            for (int i = 0; i < kids.size(); i++) {
                kids.set(i, simplifyConditionals(kids.get(i)));
            }
        }

        if (node.getType() == NodeType.COND && kids != null && !kids.isEmpty()) {
            // assume [cond, thenBranch, elseBranch?]
            Boolean cond = asBoolLiteral(kids.get(0));
            AstNode thenB = kids.size() >= 2 ? kids.get(1) : null;
            AstNode elseB = kids.size() >= 3 ? kids.get(2) : null;

            if (Boolean.TRUE.equals(cond) && thenB != null) return thenB;
            if (Boolean.FALSE.equals(cond)) {
                if (elseB != null) return elseB;
                // no else -> drop whole cond (or return an explicit empty node if you have one)
            }
        }
        return node;
    }

    private Number asNumberLiteral(AstNode n) {
        if (n == null || n.getType() != NodeType.LITERAL) return null;
        LiteralNode lit = (LiteralNode) n;
        String v = lit.getValue();
        try {
            if (lit.getTokenType() == TokenType.INTEGER) return Long.valueOf(v);
            if (lit.getTokenType() == TokenType.REAL) return Double.valueOf(v);
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    private Boolean asBoolLiteral(AstNode n) {
        if (n == null || n.getType() != NodeType.LITERAL) return null;
        LiteralNode lit = (LiteralNode) n;
        if (lit.getTokenType() == TokenType.BOOLEAN) {
            return "true".equalsIgnoreCase(lit.getValue());
        }
        return null;
    }

    private AstNode makeLiteral(TokenType type, String value) {
        Token tok = new Token(type, value, -1);
        return new LiteralNode(tok);
    }

    private AstNode makeBoolLiteral(boolean v) {
        return makeLiteral(TokenType.BOOLEAN, v ? "true" : "false");
    }

    private AstNode makeNumberLiteral(double v) {
        if (Math.rint(v) == v) return makeLiteral(TokenType.INTEGER, String.valueOf((long) v));
        return makeLiteral(TokenType.REAL, String.valueOf(v));
    }

    private TokenType readOperatorType(AstNode node) {
        if (node instanceof OperationNode op) {
            try {
                return (TokenType) OperationNode.class
                        .getMethod("getOperatorType")
                        .invoke(op);
            } catch (Exception ignored) {
            }
        }
        if (node instanceof ComparisonNode cmp) {
            try {
                return (TokenType) ComparisonNode.class
                        .getMethod("getComparisonType")
                        .invoke(cmp);
            } catch (Exception ignored) {
            }
        }
        return null; // will fallback to word
    }

    private String readOperatorWord(AstNode node) {
        if (node instanceof OperationNode op) return op.getOperator();      // "plus"/"and"/...
        if (node instanceof ComparisonNode cmp) return cmp.getComparison();  // "less"/"equal"/...
        return null;
    }

    private TokenType mapWordToType(String w) {
        if (w == null) return null;
        switch (w) {
            case "plus":
                return TokenType.PLUS;
            case "minus":
                return TokenType.MINUS;
            case "times":
                return TokenType.TIMES;
            case "divide":
                return TokenType.DIVIDE;
            case "and":
                return TokenType.AND;
            case "or":
                return TokenType.OR;
            case "xor":
                return TokenType.XOR;
            case "not":
                return TokenType.NOT;
            case "equal":
                return TokenType.EQUAL;
            case "nonequal":
                return TokenType.NONEQUAL;
            case "less":
                return TokenType.LESS;
            case "lesseq":
                return TokenType.LESSEQ;
            case "greater":
                return TokenType.GREATER;
            case "greatereq":
                return TokenType.GREATEREQ;
            default:
                return null;
        }
    }
}
