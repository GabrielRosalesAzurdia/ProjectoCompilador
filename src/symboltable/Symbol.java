package symboltable;

public class Symbol {
    private String name;
    private String type;
    private int scope;

    public Symbol(String name, String type, int scope) {
        this.name = name;
        this.type = type;
        this.scope = scope;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getScope() {
        return scope;
    }

    @Override
    public String toString() {
        return "Symbol{name='" + name + "', type='" + type + "', scope=" + scope + "}";
    }
}