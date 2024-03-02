package cool.compiler;

import cool.parser.CoolParser;
import org.antlr.v4.runtime.Token;

import java.util.List;

public class Block extends Expression{
    public List<Expression> expression;

    public List<Expression> getExpression() {
        return expression;
    }

    public Block(CoolParser.BlockContext context, Token token, List<Expression> expressionsList){
        super(context, token);
        this.expression = expressionsList;
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
