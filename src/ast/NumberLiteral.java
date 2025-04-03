package ast;

public class NumberLiteral extends Expression {
    public int value;

    public NumberLiteral(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "NumberLiteral{" +
                "value=" + value +
                '}'+'\n';
    }
}
