package cool.compiler;

import cool.parser.CoolParser;
import cool.structures.scope.ClassSymbolScope;
import org.antlr.v4.runtime.Token;

import java.util.List;

public abstract class Class extends ASTNode {
    private List<Feature> features;
    private Token myClassName;
    private String parentClass;
    private Token myClassInherit;
    private ClassSymbolScope classSymbolScope;

    public Token getMyClassName() {
        return myClassName;
    }

    public Token getMyClassInherit() {
        return myClassInherit;
    }

    public Class(CoolParser.ClassContext context, Token myClassName, Token myClassInherit, String parentClass, List<Feature> features, Token token) {
        super(token, context);
        this.features = features;
        this.parentClass = parentClass;
        this.myClassName = myClassName;
        this.myClassInherit = myClassInherit;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public ClassSymbolScope getClassSymbolScope() {
        return classSymbolScope;
    }

    public void setClassSymbolScope(ClassSymbolScope classSymbolScope) {
        this.classSymbolScope = classSymbolScope;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
