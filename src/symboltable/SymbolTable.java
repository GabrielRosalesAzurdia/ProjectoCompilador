package symboltable;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private Map<String, Symbol> symbols;

    public SymbolTable() {
        symbols = new HashMap<>();
    }

    // Método para agregar un símbolo
    public void addSymbol(String name, String type, int scope) {
        symbols.put(name, new Symbol(name, type, scope));
    }

    // Método para obtener un símbolo
    public Symbol getSymbol(String name) {
        return symbols.get(name);
    }

    // Método para verificar si un símbolo existe
    public boolean contains(String name) {
        return symbols.containsKey(name);
    }

    // Método para obtener todos los símbolos
    public Map<String, Symbol> getAllSymbols() {
        return symbols;
    }
}