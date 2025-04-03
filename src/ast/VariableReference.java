package ast;

public class VariableReference extends Expression {
    public String name;

    public VariableReference(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "VariableReference{" +
                "name='" + name + '\'' +
                '}';
    }
}