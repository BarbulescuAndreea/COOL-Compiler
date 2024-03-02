package cool.compiler;

import cool.parser.CoolParser;
import org.antlr.v4.runtime.Token;

public class Id extends Expression {
    Id(CoolParser.IdentifierContext context, Token token) {
        super(context, token);
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
