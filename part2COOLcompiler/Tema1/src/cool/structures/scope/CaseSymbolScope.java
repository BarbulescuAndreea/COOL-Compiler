package cool.structures.scope;
import cool.structures.Scope;
import cool.compiler.Class;
import cool.structures.Symbol;
import cool.structures.symbol.IdSymbol;

public class CaseSymbolScope implements Scope {
    public IdSymbol symbol;
    public Scope scope;
    public CaseSymbolScope(IdSymbol idSymbol, Scope scope){
        this.scope = scope;
        this.symbol = idSymbol;
    }
    @Override
    public Scope getParent() {
        return scope;
    }

    @Override
    public boolean addClass(Class classNode) {
        return false;
    }

    @Override
    public Class getClassByName(String name) {
        return null;
    }
    @Override
    public boolean add(Symbol sym) {
        return false;
    }
    @Override
    public Symbol lookup(String identifier) {
        if (identifier.equals(symbol.getName())) {
            return symbol;
        } else {
            return (scope != null) ? scope.lookup(identifier) : null;
        }
    }

}
