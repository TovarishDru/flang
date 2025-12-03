package stages;

import models.nodes.*;
import models.token.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Semanter {

    public void validate(AstNode root) throws Exception {
        if (root == null) return;
        checkNode(root, new ArrayDeque<>());
    }

    public AstNode optimize(AstNode root) {
        if (root == null) return null;
        root = constantFold(root);
        return root;
    }

    private String pathString(Deque<Integer> path) {
        if (path.isEmpty()) return "/";
        StringBuilder sb = new StringBuilder();
        for (Integer i : path) {
            sb.append('/').append(i);
        }
        return sb.toString();
    }

    private void checkNode(AstNode node, Deque<Integer> path) throws Exception {
        if (node == null) return;

        NodeType kind = node.getType();

        switch (kind) {
            case OPERATION -> {
                checkOperationNode((OperationNode) node, path);
            }
            case LOGICALOP -> {
                checkLogicalNode((LogicalNode) node, path);
            }
            case COMP -> {
                checkComparisonNode((ComparisonNode) node, path);
            }
            case HEAD -> {
                checkHeadNode((HeadNode) node, path);
            }
            case TAIL -> {
                checkTailNode((TailNode) node, path);
            }
            case CONS -> {
                checkConsNode((ConsNode) node, path);
            }
            case WHILE -> {
                checkWhileNode((WhileNode) node, path);
            }
            case COND -> {
                checkCondNode((CondNode) node, path);
            }
            default -> {
                List<AstNode> kids = node.getChildren();
                if (kids != null) {
                    for (int i = 0; i < kids.size(); i++) {
                        path.addLast(i);
                        checkNode(kids.get(i), path);
                        path.removeLast();
                    }
                }
            }
        }
    }

    private void checkOperationNode(OperationNode node, Deque<Integer> path) throws Exception {
        List<AstNode> kids = node.getChildren();
        if (kids == null || kids.size() != 2) {
            return;
        }

        for (int i = 0; i < kids.size(); i++) {
            path.addLast(i);
            AstNode child = kids.get(i);

            checkNode(child, path);

            if (isList(child)) {
                throw new Exception(
                        "SEMANTIC ERROR: ARITHMETIC OPERATOR '" + node.getOperator() +
                                "' CANNOT TAKE LIST ARGUMENT at " + pathString(path)
                );
            }

            if (child.getType() == NodeType.LITERAL && isNumericLiteral(child)) {
                throw new Exception(
                        "SEMANTIC ERROR: arithmetic operator '" + node.getOperator() +
                                "' expects numeric arguments, got literal of type " +
                                ((LiteralNode) child).getTokenType() +
                                " at " + pathString(path)
                );
            }

            path.removeLast();
        }
    }

    private void checkLogicalNode(LogicalNode node, Deque<Integer> path) throws Exception {
        List<AstNode> kids = node.getChildren();
        if (kids == null || kids.size() != 2) {
            return;
        }

        for (int i = 0; i < kids.size(); i++) {
            path.addLast(i);
            AstNode child = kids.get(i);

            checkNode(child, path);

            if (isList(child)) {
                throw new Exception(
                        "SEMANTIC ERROR: LOGICAL OPERATOR '" + node.getOperator() +
                                "' CANNOT TAKE LIST ARGUMENT at " + pathString(path)
                );
            }

            if (child.getType() == NodeType.LITERAL) {
                if (isBoolLiteral(child) && isNumericLiteral(child)) {
                    throw new Exception(
                            "SEMANTIC ERROR: LOGICAL OPERATOR '" + node.getOperator() +
                                    "' EXPECTS BOOLEAN OR NUMERIC ARGUMENT, got LITERAL of type " +
                                    ((LiteralNode) child).getTokenType() +
                                    " at " + pathString(path)
                    );
                }
            }

            path.removeLast();
        }
    }

    private void checkComparisonNode(ComparisonNode node, Deque<Integer> path) throws Exception {
        List<AstNode> kids = node.getChildren();
        if (kids == null || kids.size() != 2) {
            return;
        }

        for (int i = 0; i < kids.size(); i++) {
            path.addLast(i);
            AstNode child = kids.get(i);

            checkNode(child, path);

            if (isList(child)) {
                throw new Exception(
                        "SEMANTIC ERROR: COMPARISON '" + node.getComparison() +
                                "' CANNOT BE APPLIED TO LIST at " + pathString(path)
                );
            }

            path.removeLast();
        }
    }

    private void checkHeadNode(HeadNode node, Deque<Integer> path) throws Exception {
        AstNode arg = node.getListExpr();
        path.addLast(0);

        checkNode(arg, path);
        if (arg.getType() == NodeType.LITERAL && !isList(arg)) {
            throw new Exception(
                    "SEMANTIC ERROR: HEAD ARGUMENT MUST BE A LIST, got LITERAL of type " +
                            ((LiteralNode) arg).getTokenType() +
                            " at " + pathString(path)
            );
        }
        path.removeLast();
    }

    private void checkTailNode(TailNode node, Deque<Integer> path) throws Exception {
        AstNode arg = node.getListExpr();
        path.addLast(0);

        checkNode(arg, path);

        if (arg.getType() == NodeType.LITERAL && !isList(arg)) {
            throw new Exception(
                    "SEMANTIC ERROR: TAIL ARGUMENT MUST BE A LIST, got LITERAL of type " +
                            ((LiteralNode) arg).getTokenType() +
                            " at " + pathString(path)
            );
        }

        path.removeLast();
    }

    private void checkConsNode(ConsNode node, Deque<Integer> path) throws Exception {
        List<AstNode> kids = node.getChildren();
        if (kids == null || kids.size() != 2) return;

        path.addLast(0);
        checkNode(kids.get(0), path);
        path.removeLast();

        AstNode tail = kids.get(1);
        path.addLast(1);

        checkNode(tail, path);

        if (tail.getType() == NodeType.LITERAL && !isList(tail)) {
            throw new Exception(
                    "SEMANTIC ERROR: CONS SECOND ARGUMENT MUST BE A LIST, got LITERAL of type " +
                            ((LiteralNode) tail).getTokenType() +
                            " at " + pathString(path)
            );
        }
        path.removeLast();
    }

    private void checkWhileNode(WhileNode node, Deque<Integer> path) throws Exception {
        AstNode cond = node.getCondition();
        List<AstNode> body = node.getBody();

        path.addLast(0);
        checkNode(cond, path);

        if (isList(cond)) {
            throw new Exception(
                    "SEMANTIC ERROR: WHILE CONDITION MUST BE BOOLEAN, got LIST at " +
                            pathString(path)
            );
        }

        if (cond.getType() == NodeType.LITERAL &&
                isBoolLiteral(cond)) {
            throw new Exception(
                    "SEMANTIC ERROR: WHILE CONDITION MUST BE BOOLEAN, got LITERAL of type " +
                            ((LiteralNode) cond).getTokenType() +
                            " at " + pathString(path)
            );
        }

        path.removeLast();

        if (body != null) {
            for (int i = 0; i < body.size(); i++) {
                path.addLast(i + 1);
                checkNode(body.get(i), path);
                path.removeLast();
            }
        }
    }

    private void checkCondNode(CondNode node, Deque<Integer> path) throws Exception {
        List<AstNode> kids = node.getChildren();
        if (kids == null || kids.isEmpty()) return;

        AstNode cond = kids.get(0);
        path.addLast(0);

        checkNode(cond, path);

        if (isList(cond)) {
            throw new Exception(
                    "SEMANTIC ERROR: COND CONDITION MUST BE BOOLEAN, got LIST at " +
                            pathString(path)
            );
        }

        if (cond.getType() == NodeType.LITERAL &&
                isBoolLiteral(cond)) {
            throw new Exception(
                    "SEMANTIC ERROR: cond condition must be boolean, got literal of type " +
                            ((LiteralNode) cond).getTokenType() +
                            " at " + pathString(path)
            );
        }

        path.removeLast();

        for (int i = 1; i < kids.size(); i++) {
            path.addLast(i);
            checkNode(kids.get(i), path);
            path.removeLast();
        }
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

        if (kind == NodeType.COND && kids != null && !kids.isEmpty()) {
            Boolean cond = asBoolLiteral(kids.get(0));
            AstNode thenB  = kids.size() >= 2 ? kids.get(1) : null;
            AstNode elseB  = kids.size() >= 3 ? kids.get(2) : null;

            if (Boolean.TRUE.equals(cond) && thenB != null) {
                optResult = Optional.of(thenB);
            } else if (Boolean.FALSE.equals(cond)) {
                if (elseB != null) {
                    optResult = Optional.of(elseB);
                }
            }
        }

        // unary NOT
        else if (kind == NodeType.NOT && kids != null && kids.size() == 1) {
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
        else if (kind == NodeType.LOGICALOP && kids != null && kids.size() >= 2) {
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

    private boolean isList(AstNode n) {
        if (n == null) return false;
        NodeType k = n.getType();

        if (k == NodeType.LIST) return true;

        if (k == NodeType.QUOTE) {
            AstNode inner = ((QuoteNode) n).getQuotedExpr();
            return inner != null && inner.getType() == NodeType.LIST;
        }
        return false;
    }

    private boolean isNumericLiteral(AstNode n) {
        if (n == null || n.getType() != NodeType.LITERAL) return true;
        TokenType tt = ((LiteralNode) n).getTokenType();
        return tt != TokenType.INTEGER && tt != TokenType.REAL;
    }

    private boolean isBoolLiteral(AstNode n) {
        if (n == null || n.getType() != NodeType.LITERAL) return true;
        return ((LiteralNode) n).getTokenType() != TokenType.BOOLEAN;
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
        TokenType tt = lit.getTokenType();
        String v = lit.getValue();

        if (tt == TokenType.BOOLEAN) {
            return "true".equalsIgnoreCase(v);
        }

        if (tt == TokenType.INTEGER || tt == TokenType.REAL) {
            try {
                double d = Double.parseDouble(v);
                return d != 0.0;
            } catch (NumberFormatException ignored) {
                return null;
            }
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
        if (node instanceof LogicalNode log) return log.getOperator();
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
