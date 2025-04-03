package ast;

// Ejemplo de un nodo para valores literales (números, cadenas)
public class LiteralExpression extends Expression {
    public Object value; // Puede ser un número, cadena, etc.

    public LiteralExpression(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "LiteralExpression{" +
                "value=" + value +
                '}';
    }
}
