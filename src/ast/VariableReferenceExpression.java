package ast;

// Ejemplo de una referencia a una variable en una expresión
public class VariableReferenceExpression extends Expression {
    public String name;

    public VariableReferenceExpression(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "VariableReferenceExpression{" +
                "name='" + name + '\'' +
                '}';
    }
}
