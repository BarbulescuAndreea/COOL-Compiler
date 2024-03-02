package cool.compiler;

import cool.parser.CoolParser;
import org.antlr.v4.runtime.Token;

public class Assignment extends Expression{
    private Token id;
    private Expression expression;
    private Token from;

    public Token getId() {
        return id;
    }
    public Token getFrom() {
        return from;
    }

    public Expression getExpression() {
        return expression;
    }

    Assignment(CoolParser.AssignmentContext context, Token token, Expression expression, Token id){
        super(context, token);
        this.id = id;
        this.expression = expression;
        this.from = context.expr().start;
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
