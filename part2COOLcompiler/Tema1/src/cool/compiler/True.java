package cool.compiler;

import cool.parser.CoolParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public class True extends Expression {
    public Boolean value = true;

    public True(ParserRuleContext context, Token token) {
        super(context, token);
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
