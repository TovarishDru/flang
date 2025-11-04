package stages;

import models.nodes.AstNode;
import models.nodes.AtomNode;
import models.nodes.NodeType;
import models.token.TokenType;
import models.nodes.LiteralNode;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Set;


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

    public void analyze(AstNode root) throws SemanticException {
        if (root == null) return;
        Deque<Integer> path = new ArrayDeque<>();
        traverse(root, path);
    }

    private void traverse(AstNode node, Deque<Integer> path) throws SemanticException {
        if (node == null) return;

        NodeType kind = node.getType();
        List<AstNode> children = node.getChildren();

        if (arithmeticKind == kind) {
            checkArithmetic(node, children, path);
        } else if (logicalKind == kind) {
            checkLogical(node, children, path);
        } else if (comparisonKind == kind) {
            checkComparison(node, children, path);
        }

        if (children != null) {
            for (int i = 0; i < children.size(); i++) {
                path.addLast(i);
                traverse(children.get(i), path);
                path.removeLast();
            }
        }
    }

    private void checkArithmetic(AstNode node, List<AstNode> children, Deque<Integer> path)
            throws SemanticException {
        int count = (children == null) ? 0 : children.size();
        if (count != 2) {
            throw error("ERROR: ARITHMETIC node %s must have exactly 2 operands but has %d at path %s",
                    node.getType(), count, pathString(path));
        }

        for (int i = 0; i < children.size(); i++) {
            AstNode child = children.get(i);
            if (child.getType() == NodeType.LITERAL) {
                TokenType literalType = ((LiteralNode) child).getTokenType();
                if (literalType != TokenType.BOOLEAN) {
                    continue;
                }
            }
            if (child.getType() == NodeType.ATOM) {
                TokenType atomType = ((AtomNode) child).getTokenType();
                if (atomType == TokenType.INTEGER && atomType == TokenType.REAL) {
                    continue;
                }
            }
            if (child.getType() == NodeType.FUNCCALL) {
                continue;
            }
        }
        throw error("ERROR: ARITHMETIC node %s got unexpected operands at path %s",
                        node.getType(), pathString(path));
    }

    private void checkLogical(AstNode node, List<AstNode> children, Deque<Integer> path)
            throws SemanticException {
        int count = (children == null) ? 0 : children.size();
        if (count != 2) {
            throw error("ERROR: LOGICAL node %s must have exactly 2 operands but has %d at path %s",
                    node.getType(), count, pathString(path));
        }

        for (int i = 0; i < children.size(); i++) {
            AstNode child = children.get(i);
            if (child.getType() == NodeType.LITERAL) {
                TokenType literalType = ((LiteralNode) child).getTokenType();
                if (literalType == TokenType.BOOLEAN) {
                    continue;
                }
            }
            if (child.getType() == NodeType.ATOM) {
                TokenType atomType = ((AtomNode) child).getTokenType();
                if (atomType == TokenType.BOOLEAN) {
                    continue;
                }
                
            }
            if (child.getType() == NodeType.FUNCCALL) {
                continue;
            }
            throw error("ERROR: LOGICAL node %s got unexpected operands at path %s",
                        node.getType(), pathString(path));
        }
    }

    private void checkComparison(AstNode node, List<AstNode> children, Deque<Integer> path)
            throws SemanticException {
        int count = (children == null) ? 0 : children.size();
        if (count != 2) {
            throw error("ERROR: COMPARISON node %s must have exactly 2 operands but has %d at path %s",
                    node.getType(), count, pathString(path));
        }

        AstNode left = children.get(0);
        AstNode right = children.get(1);

        // If nodes expose a semantic type, require equality.
        if (left instanceof Typed lt && right instanceof Typed rt) {
            Object ltType = lt.getDataType();
            Object rtType = rt.getDataType();
            if (!Objects.equals(ltType, rtType)) {
                throw error("ERROR: %s cannot be compared with %s (node %s) at path %s",
                        String.valueOf(ltType), String.valueOf(rtType), node.getType(), pathString(path));
            }
        }
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
}
