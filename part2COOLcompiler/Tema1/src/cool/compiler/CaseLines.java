package cool.compiler;

import cool.parser.CoolParser;
import org.antlr.v4.runtime.Token;

import java.util.List;

public class CaseLines extends Expression{
    public Expression cond;
    public List<Expression> cases;

    public Expression getCond() {
        return cond;
    }

    public List<Expression> getCases() {
        return cases;
    }

    public CaseLines(CoolParser.Case_blockContext context, Token token, Expression cond, List<Expression> cases){
        super(context, token);
        this.cond = cond;
        this.cases = cases;
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
