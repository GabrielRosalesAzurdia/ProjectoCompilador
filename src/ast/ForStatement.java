package ast;

public class ForStatement extends Node {
    public VariableDeclaration initializer; // Inicialización de la variable
    public Expression condition; // Condición del bucle
    public Expression update; // Actualización de la variable
    public BlockStatement body; // Cuerpo del bucle

    public ForStatement(VariableDeclaration initializer, Expression condition, Expression update, BlockStatement body) {
        this.initializer = initializer;
        this.condition = condition;
        this.update = update;
        this.body = body;
    }

    @Override
    public String toString() {
        return "ForStatement{" +
                "initializer=" + initializer +
                ", condition=" + condition +
                ", update=" + update +
                ", body=" + body +
                '}' + '\n';
    }
}