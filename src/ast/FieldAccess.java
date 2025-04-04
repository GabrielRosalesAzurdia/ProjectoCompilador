package ast;

public class FieldAccess extends Expression {
    public final Expression object;
    public final String field;

    public FieldAccess(Expression object, String field) {
        this.object = object;
        this.field = field;
    }
}