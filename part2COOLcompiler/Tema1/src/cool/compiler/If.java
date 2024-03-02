package cool.compiler;

import cool.parser.CoolParser;
import org.antlr.v4.runtime.Token;

public class If extends Expression{
    private Expression then;
    private Expression elseCase;
    private Expression cond;
    private Token toDisplay;

    public Token getToDisplay() {
        return toDisplay;
    }

    public void setToDisplay(Token toDisplay) {
        this.toDisplay = toDisplay;
    }

    public Expression getThen() {
        return then;
    }

    public Expression getElseCase() {
        return elseCase;
    }

    public Expression getCond() {
        return cond;
    }

    public If(CoolParser.IfContext context, Token token, Expression cond, Expression then, Expression elseCase, Token toDisplay){
        super(context, token);
        this.cond = cond;
        this.then = then;
        this.elseCase = elseCase;
        this.toDisplay = toDisplay;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
