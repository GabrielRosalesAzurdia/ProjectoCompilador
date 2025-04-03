package ast;

public class UnaryExpression extends Expression {
    public String operator; // Operador unario
    public Expression operand; // Operando

    public UnaryExpression(String operator, Expression operand) {
        this.operator = operator;
        this.operand = operand;
    }

    @Override
    public String toString() {
        return "UnaryExpression{" +
                "operator='" + operator + '\'' +
                ", operand=" + operand +
                '}';
    }
}