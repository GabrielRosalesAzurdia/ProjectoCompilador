package ast;

// Ejemplo de una expresión aritmética
public class BinaryExpression extends Expression {
    public Expression left; // Operando izquierdo
    public String operator;  // Operador (por ejemplo, +, -, *, /)
    public Expression right; // Operando derecho

    public BinaryExpression(Expression left, String operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public String toString() {
        return "BinaryExpression{" +
                "left=" + left +
                ", operator='" + operator + '\'' +
                ", right=" + right +
                '}';
    }
}
