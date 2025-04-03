package ast;

public class WhileStatement extends Node {
    public Expression condition; // Condición del bucle
    public BlockStatement body; // Cuerpo del bucle

    public WhileStatement(Expression condition, BlockStatement body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public String toString() {
        return "WhileStatement{" +
                "condition=" + condition +
                ", body=" + body +
                '}'+'\n';
    }
}