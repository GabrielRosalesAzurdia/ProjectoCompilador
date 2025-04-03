package ast;

import java.util.List;

public class TryCatchStatement extends Node {
    public BlockStatement tryBlock; // Bloque try
    public List<CatchClause> catchClauses; // Lista de cláusulas catch

    public TryCatchStatement(BlockStatement tryBlock, List<CatchClause> catchClauses) {
        this.tryBlock = tryBlock;
        this.catchClauses = catchClauses;
    }

    @Override
    public String toString() {
        return "TryCatchStatement{" +
                "tryBlock=" + tryBlock +
                ", catchClauses=" + catchClauses +
                '}';
    }
}

class CatchClause extends Node {
    public String exceptionVariable; // Variable de excepción
    public BlockStatement body; // Cuerpo del catch

    public CatchClause(String exceptionVariable, BlockStatement body) {
        this.exceptionVariable = exceptionVariable;
        this.body = body;
    }

    @Override
    public String toString() {
        return "CatchClause{" +
                "exceptionVariable='" + exceptionVariable + '\'' +
                ", body=" + body +
                '}';
    }
}