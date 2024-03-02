package cool.compiler;

import cool.parser.CoolParser;
import org.antlr.v4.runtime.Token;

public class New extends Expression {
    private Token id;

    public Token getId() {
        return id;
    }

    public New(CoolParser.NewContext context, Token token, Token id) {
        super(context, token);
        this.id = id;
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
