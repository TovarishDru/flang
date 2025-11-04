package stages;

import models.nodes.AstNode;
import models.nodes.NodeType;

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

    private final Set<NodeType> arithmeticKinds;
    private final Set<NodeType> logicalKinds;
    private final Set<NodeType> comparisonKinds;


    public Semanter(Set<NodeType> arithmeticKinds,
                    Set<NodeType> logicalKinds,
                    Set<NodeType> comparisonKinds) {
        this.arithmeticKinds = Objects.requireNonNull(arithmeticKinds, "arithmeticKinds");
        this.logicalKinds = Objects.requireNonNull(logicalKinds, "logicalKinds");
        this.comparisonKinds = Objects.requireNonNull(comparisonKinds, "comparisonKinds");
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

        if (arithmeticKinds.contains(kind)) {
            checkArithmetic(node, children, path);
        } else if (logicalKinds.contains(kind)) {
            checkLogical(node, children, path);
        } else if (comparisonKinds.contains(kind)) {
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
        if (children == null || children.isEmpty()) {
            throw error("ERROR: ARITHMETIC node %s has no operands (children) at path %s",
                    node.getType(), pathString(path));
        }
    }

    private void checkLogical(AstNode node, List<AstNode> children, Deque<Integer> path)
            throws SemanticException {
        int count = (children == null) ? 0 : children.size();
        if (count != 2) {
            throw error("ERROR: LOGICAL node %s must have exactly 2 operands but has %d at path %s",
                    node.getType(), count, pathString(path));
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
