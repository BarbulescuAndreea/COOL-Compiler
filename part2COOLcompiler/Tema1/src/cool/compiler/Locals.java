package cool.compiler;

import cool.parser.CoolParser;
import cool.structures.symbol.IdSymbol;
import org.antlr.v4.runtime.Token;

public class Locals extends ASTNode {
    private Token id;
    private Token typeOrId;
    private Expression expression;

    public IdSymbol getIdSymbol() {
        return idSymbol;
    }

    public void setIdSymbol(IdSymbol idSymbol) {
        this.idSymbol = idSymbol;
    }

    public IdSymbol idSymbol;

    public Token getId() {
        return id;
    }

    public Token getTypeOrId() {
        return typeOrId;
    }

    public Expression getExpression() {
        return expression;
    }

    public Locals(CoolParser.LocalContext context, Expression expression, Token id, Token typeOrId, Token token) {
        super(token, context);
        this.expression = expression;
        this.id = id;
        this.typeOrId = typeOrId;
    }
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
