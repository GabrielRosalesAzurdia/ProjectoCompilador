package ast;

public class ExpressionStatement extends Node {
    public Expression expression; // La expresión

    public ExpressionStatement(Expression expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return "ExpressionStatement{" +
                "expression=" + expression +
                '}';
    }
}