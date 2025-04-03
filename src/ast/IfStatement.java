package ast;

public class IfStatement extends Node {
    public Node condition; // La condición del if
    public Node thenBranch; // El bloque de código que se ejecuta si la condición es verdadera
    public Node elseBranch; // El bloque de código que se ejecuta si la condición es falsa (opcional)

    public IfStatement(Node condition, Node thenBranch, Node elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    @Override
    public String toString() {
        return "IfStatement{" +
                "condition=" + condition +
                ", thenBranch=" + thenBranch +
                (elseBranch != null ? ", elseBranch=" + elseBranch : "") +
                '}';
    }
}