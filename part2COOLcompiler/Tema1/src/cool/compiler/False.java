package cool.compiler;

import cool.parser.CoolParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public class False extends Expression {
    public Boolean value = false;
    public False(ParserRuleContext context, Token token) {
        super(context, token);
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
