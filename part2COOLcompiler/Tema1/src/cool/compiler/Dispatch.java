package cool.compiler;

import cool.parser.CoolParser;
import org.antlr.v4.runtime.Token;

import java.util.List;

public class Dispatch extends Expression{
    private String id;
    private String type;
    private Expression expression;
    private List<Expression> expressionsList;

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Expression getExpression() {
        return expression;
    }

    public List<Expression> getExpressionsList() {
        return expressionsList;
    }

    public Dispatch(CoolParser.DispatchContext context, Token token, String type, String id, List<Expression> expressionsList, Expression expression){
        super(context, token);
        this.expression = expression;
        this.expressionsList = expressionsList;
        this.id = id;
        this.type = type;
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
