package ast;

public class StringLiteral extends Expression {
    public String value;

    public StringLiteral(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "StringLiteral{" +
                "value=\"" + value + "\"" +
                '}'+'\n';
    }
}
