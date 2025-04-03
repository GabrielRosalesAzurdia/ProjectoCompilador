package ast;

import java.util.List;

public class ClassDeclaration extends Node {
    public String name;
    public List<MethodDeclaration> methods; // Lista de métodos

    public ClassDeclaration(String name, List<MethodDeclaration> methods) {
        this.name = name;
        this.methods = methods;
    }

    @Override
    public String toString() {
        return "ClassDeclaration{" +
                "name='" + name + '\'' +
                ", methods=" + methods +
                '}' + '\n';
    }
}