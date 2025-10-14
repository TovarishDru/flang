package models.symbol_table;

import models.nodes.*;

import java.util.Map;
import java.util.HashMap;

public class SymbolTable {
    private SymbolTable parent;
    Map<String, AstNode> symbols;

    public SymbolTable(SymbolTable parent) {
		this.parent = parent;
		this.symbols = new HashMap<>();
	}

    public void define(String name, AstNode value) {
		symbols.put(name, value);
	}

	public AstNode find(String name) {
		if (symbols.containsKey(name)) {
			return symbols.get(name);
		} else if (parent != null) {
			return parent.find(name);
		} else {
			return null;
		}
	}

	public boolean defined(String name) {
		return find(name) != null;
	}
}