package ast;

import java.util.List;

public class ClassDeclaration extends Node {
    private String name;
    private List<VariableDeclaration> attributes;
    private List<MethodDeclaration> methods;

    public ClassDeclaration(String name, List<VariableDeclaration> attributes, List<MethodDeclaration> methods) {
        this.name = name;
        this.attributes = attributes;
        this.methods = methods;
    }

    // Getters, toString(), etc.
}
