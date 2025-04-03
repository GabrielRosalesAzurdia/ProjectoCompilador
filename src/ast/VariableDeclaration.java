package ast;

public class VariableDeclaration extends Node {
    public String type;
    public String identifier;
    public Node initializer; // Puede ser una expresión

    public VariableDeclaration(String type, String identifier, Node initializer) {
        this.type = type;
        this.identifier = identifier;
        this.initializer = initializer;
    }

    @Override
    public String toString() {
        return "VariableDeclaration{" +
                "type='" + type + '\'' +
                ", identifier='" + identifier + '\'' +
                ", initializer=" + initializer +
                '}';
    }
}
