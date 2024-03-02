package cool.compiler;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public class LeftOPRight extends Expression {
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

    LeftOPRight(ParserRuleContext context, Token token, Expression leftEx, Token operation, Expression rightEx) {
        super(context, token);
        this.operation = operation;
        this.rightEx = rightEx;
        this.leftEx = leftEx;
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
