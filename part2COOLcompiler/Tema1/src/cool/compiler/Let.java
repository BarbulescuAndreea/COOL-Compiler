package cool.compiler;

import cool.parser.CoolParser;
import cool.structures.scope.LetSymbolScope;
import org.antlr.v4.runtime.Token;

import java.util.List;

public class Let extends Expression {
    private Expression expression;
    private List<Locals> lets;
    LetSymbolScope scope;

    public LetSymbolScope getScope() {
        return scope;
    }

    public void setScope(LetSymbolScope scope) {
        this.scope = scope;
    }

    public Expression getExpression() {
        return expression;
    }

    public List<Locals> getLets() {
        return lets;
    }

    public Let(CoolParser.LetContext context, Token token, Expression expression, List<Locals> lets){
        super(context, token);
        this.expression = expression;
        this.lets = lets;
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
