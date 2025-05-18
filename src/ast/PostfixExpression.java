package ast;

public class PostfixExpression extends Expression {
    private final Expression target;
    private final String operator;

    public PostfixExpression(Expression target, String operator) {
        this.target = target;
        this.operator = operator;
    }

    public Expression getTarget() {
        return target;
    }

    public String getOperator() {
        return operator;
    }

    @Override
    public String toString() {
        return target + operator;
    }
}
