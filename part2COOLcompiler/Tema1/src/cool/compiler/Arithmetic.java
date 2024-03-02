package cool.compiler;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public class Arithmetic extends Expression {
    private Expression rightEx;
    private Expression leftEx;
    private Token operation;

    public Expression getRightEx() {
        return rightEx;
    }

    public Expression getLeftEx() {
        return leftEx;
    }

    public Token getOperation() {
        return operation;
    }

    Arithmetic(ParserRuleContext context, Token token, Expression leftEx, Expression rightEx, Token operation) {
        super(context, token);
        this.operation = operation;
        this.leftEx = leftEx;
        this.rightEx = rightEx;
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
