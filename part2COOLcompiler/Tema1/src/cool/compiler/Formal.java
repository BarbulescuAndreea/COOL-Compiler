package cool.compiler;

import cool.parser.CoolParser;
import cool.structures.Symbol;
import cool.structures.scope.ClassSymbolScope;
import cool.structures.symbol.IdSymbol;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public abstract class Formal extends ASTNode {
    private Token id;
    private Token type;

    public IdSymbol getIdSymbol() {
        return idSymbol;
    }

    public void setIdSymbol(IdSymbol idSymbol) {
        this.idSymbol = idSymbol;
    }

    public void setIdSymbol2(Symbol symbolGeneral) {this.idSymbol.setScope((ClassSymbolScope) symbolGeneral);}

    private IdSymbol idSymbol;

    public Formal(ParserRuleContext context, Token id, Token type, Token token) {
        super(token, context);
        this.id = id;
        this.type = type;
    }
    public Token getId() {
        return id;
    }
    public Token getType() {
        return type;
    }
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
