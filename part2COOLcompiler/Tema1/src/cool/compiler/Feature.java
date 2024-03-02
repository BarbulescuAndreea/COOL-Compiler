package cool.compiler;

import cool.structures.scope.MethodSymbolScope;
import cool.structures.symbol.IdSymbol;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import cool.structures.*;
import cool.structures.scope.*;

import java.util.List;

public abstract class Feature extends ASTNode {
    private Token updatedFeatureId;
    private Token updatedFeatureType;
    private List<Formal> updatedFormalList;
    private List<Expression> updatedExpressions;
    private int updatedAttributeContext;
    private int updatedMethodContext;
    private IdSymbol idSymbol;
    private MethodSymbolScope methodScope;

    public IdSymbol getIdSymbol() {
        return idSymbol;
    }

    public void setIdSymbol(IdSymbol idSymbol) {
        this.idSymbol = idSymbol;
    }

    public MethodSymbolScope getMethodScope() {
        return methodScope;
    }

    public void setMethodScope(MethodSymbolScope methodScope) {
        this.methodScope = methodScope;
    }

    public int getUpdatedAttributeContext() {
        return updatedAttributeContext;
    }

    public int getUpdatedMethodContext() {
        return updatedMethodContext;
    }

    public void setIdSymbolGeneral(Symbol symbolGeneral) {this.idSymbol.setScope((ClassSymbolScope) symbolGeneral);}

    // Constructor for updated method feature
    public Feature(ParserRuleContext context, Token id, Token type, List<Expression> expressions, int attributeContext, int methodContext, List<Formal> formalList, Token token) {
        super(token, context);
        this.updatedFeatureId = id;
        this.updatedFeatureType = type;
        this.updatedFormalList = formalList;
        this.updatedExpressions = expressions;
        this.updatedAttributeContext = attributeContext;
        this.updatedMethodContext = methodContext;
    }

    public Token getUpdatedFeatureId() {
        return updatedFeatureId;
    }
    public Token getUpdatedFeatureType() {
        return updatedFeatureType;
    }
    public List<Expression> getUpdatedExpressions() {
        return updatedExpressions;
    }
    public List<Formal> getUpdatedFormalList() {
        return updatedFormalList;
    }
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
