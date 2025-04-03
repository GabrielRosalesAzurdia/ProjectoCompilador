package ast;

import java.util.List;

public class BlockStatement extends Node {
    public List<Node> statements; // Lista de declaraciones dentro del bloque

    public BlockStatement(List<Node> statements) {
        this.statements = statements;
    }

    @Override
    public String toString() {
        return "BlockStatement{" +
                "statements=" + statements +
                '}';
    }
}