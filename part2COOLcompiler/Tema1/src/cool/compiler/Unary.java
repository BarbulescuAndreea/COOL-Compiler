package cool.compiler;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public class Unary extends Expression {
    private Token operation;
    private Expression expression;

    public Token getOperation() {
        return operation;
    }

    public Expression getExpression() {
        return expression;
    }

    Unary(ParserRuleContext context, Token token, Expression expression, Token operation) {
        super(context, token);
        this.expression = expression;
        this.operation = operation;
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
