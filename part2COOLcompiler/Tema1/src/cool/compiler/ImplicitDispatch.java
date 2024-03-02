package cool.compiler;

import cool.parser.CoolParser;
import org.antlr.v4.runtime.Token;

import java.util.List;

public class ImplicitDispatch extends Expression{
    private String id;
    private List<Expression> expressions;

    public String getId() {
        return id;
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    public ImplicitDispatch(CoolParser.Implicit_dispatchContext context, Token token, List<Expression> expressions, String id){
        super(context, token);
        this.id = id;
        this.expressions = expressions;
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
