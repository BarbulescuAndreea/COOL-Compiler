package cool.compiler;

import cool.parser.CoolParser;
import org.antlr.v4.runtime.Token;

public class VarString extends Expression {
    String value;
    VarString(CoolParser.StringContext context, Token token) {
        super(context, token);
        value = token.getText();
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
