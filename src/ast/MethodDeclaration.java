package ast;

import java.util.List;

public class MethodDeclaration extends Node {
    public String returnType;
    public String name;
    public List<VariableDeclaration> parameters; // Parámetros del método
    public BlockStatement body; // Cuerpo del método

    public MethodDeclaration(String returnType, String name, List<VariableDeclaration> parameters, BlockStatement body) {
        this.returnType = returnType;
        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }

    @Override
    public String toString() {
        return "MethodDeclaration{" +
                "returnType='" + returnType + '\'' +
                ", name='" + name + '\'' +
                ", parameters=" + parameters +
                ", body=" + body +
                '}'+'\n';
    }
}