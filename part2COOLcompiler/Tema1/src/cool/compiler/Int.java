package cool.compiler;

import cool.parser.CoolParser;
import org.antlr.v4.runtime.Token;

public class Int extends Expression {
    private Integer value;

    public Integer getValue() {
        return value;
    }

    Int(CoolParser.IntegerContext context, Token token) {
        super(context, token);
        value = Integer.parseInt(token.getText());
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
