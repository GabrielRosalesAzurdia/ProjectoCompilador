package ast;

public class AssignmentExpression extends Expression {
    public final Expression target;
    public final Expression value;

    public AssignmentExpression(Expression target, Expression value) {
        this.target = target;
        this.value = value;
    }
}
