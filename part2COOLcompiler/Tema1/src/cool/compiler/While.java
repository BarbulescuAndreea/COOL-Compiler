package cool.compiler;

import cool.parser.CoolParser;
import org.antlr.v4.runtime.Token;

public class While extends Expression{
    private Expression cond;
    private Expression inside;

    Token toDisplay;

    public Token getToDisplay() {
        return toDisplay;
    }

    public Expression getCond() {
        return cond;
    }

    public Expression getInside() {
        return inside;
    }

    public While(CoolParser.WhileContext context, Token toDisplay, Token token, Expression cond, Expression inside){
        super(context, token);
        this.cond = cond;
        this.inside = inside;
        this.toDisplay = toDisplay;
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
