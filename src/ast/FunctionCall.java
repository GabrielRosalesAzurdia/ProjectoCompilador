package ast;

import java.util.List;

public class FunctionCall extends Expression {
    private final String name;
    private final List<Expression> arguments;

    public FunctionCall(String name, List<Expression> arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    public String getName() {
        return name;
    }

    public List<Expression> getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        return "FunctionCall(" + name + ", args=" + arguments + ")";
    }
}
