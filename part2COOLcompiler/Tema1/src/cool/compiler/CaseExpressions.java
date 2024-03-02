package cool.compiler;

import cool.parser.CoolParser;
import org.antlr.v4.runtime.Token;

public class CaseExpressions extends Expression{
    private Expression expression;
    private Token id;
    private Token type;

    public Expression getExpression() {
        return expression;
    }

    public Token getId() {
        return id;
    }

    public Token getType() {
        return type;
    }

    public CaseExpressions(CoolParser.Case_exprContext context, Token token, Token id, Expression expression, Token type){
        super(context, token);
        this.id = id;
        this.expression = expression;
        this.type = type;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
