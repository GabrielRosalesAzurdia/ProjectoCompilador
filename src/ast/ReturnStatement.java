package ast;

public class ReturnStatement extends Node {
    public Expression value; // Valor que se retorna

    public ReturnStatement(Expression value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "ReturnStatement{" +
                "value=" + value +
                '}';
    }
}