package models.nodes;

import java.util.ArrayList;

public class PredicateNode extends AstNode {
	private final String predicate; // "isint"|"isreal"|"isbool"|"isnull"|"isatom"|"islist"
	private final AstNode argument;

	public PredicateNode(String predicate, AstNode argument) {
		super(NodeType.PREDICATE, null, new ArrayList<>());
		this.predicate = predicate;
		this.argument = argument;
		addChild(argument);
	}

	public String getPredicate() { return predicate; }
	public AstNode getArgument() { return argument; }

	@Override
    public String toString() {
        return "PredicateNode(" + predicate.toString() + ":" + argument.toString() + ")";
    }

}
