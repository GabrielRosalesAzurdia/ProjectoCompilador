package ast;

import java.util.List;

public class SwitchStatement extends Node {
    public Expression expression; // Expresión a evaluar
    public List<CaseStatement> cases; // Lista de casos

    public SwitchStatement(Expression expression, List<CaseStatement> cases) {
        this.expression = expression;
        this.cases = cases;
    }

    @Override
    public String toString() {
        return "SwitchStatement{" +
                "expression=" + expression +
                ", cases=" + cases +
                '}';
    }
}

class CaseStatement extends Node {
    public Expression caseValue; // Valor del caso
    public BlockStatement body; // Cuerpo del caso

    public CaseStatement(Expression caseValue, BlockStatement body) {
        this.caseValue = caseValue;
        this.body = body;
    }

    @Override
    public String toString() {
        return "CaseStatement{" +
                "caseValue=" + caseValue +
                ", body=" + body +
                '}';
    }
}