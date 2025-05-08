package symboltable;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private Map<String, Symbol> symbols;
    private SymbolTable parent;

    public SymbolTable(SymbolTable parent) {
        this.symbols = new HashMap<>();
        this.parent = parent;
    }

    public SymbolTable getParent() {
        return parent;
    }

    // Inserta un símbolo en el ámbito actual
    public boolean insert(String name, Symbol symbol) {
        if (symbols.containsKey(name)) {
            return false; // Ya existe en el ámbito actual
        }
        symbols.put(name, symbol);
        return true;
    }

    // Busca en todos los ámbitos (actual y padres)
    public Symbol lookup(String name) {
        Symbol symbol = symbols.get(name);
        if (symbol != null) {
            return symbol;
        }
        if (parent != null) {
            return parent.lookup(name);
        }
        return null;
    }

    // Busca solo en el ámbito actual (para detectar redefiniciones)
    public boolean containsInCurrentScope(String name) {
        return symbols.containsKey(name);
    }

    // Para depuración
    public void printTable(String indent) {
        for (Map.Entry<String, Symbol> entry : symbols.entrySet()) {
            System.out.println(indent + entry.getKey() + " -> " + entry.getValue());
        }
        if (parent != null) {
            System.out.println(indent + "↑ Parent Scope:");
            parent.printTable(indent + "  ");
        }
    }

    public Map<String, Symbol> getAllSymbols() {
        Map<String, Symbol> allSymbols = new HashMap<>();
        if (parent != null) {
            allSymbols.putAll(parent.getAllSymbols());
        }
        allSymbols.putAll(symbols);
        return allSymbols;
    }

}
