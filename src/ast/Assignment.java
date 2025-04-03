package ast;

public class Assignment extends Expression {
    public String identifier;
    public Node value;

    public Assignment(String identifier, Node value) {
        this.identifier = identifier;
        this.value = value;
    }

    @Override
    public String toString() {
        return "Assignment{" +
                "identifier='" + identifier + '\'' +
                ", value=" + value +
                '}' + '\n';
    }
}
