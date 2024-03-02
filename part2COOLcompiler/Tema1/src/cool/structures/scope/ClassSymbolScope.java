package cool.structures.scope;

import cool.compiler.Class;
import cool.structures.Symbol;
import cool.structures.symbol.IdSymbol;
import cool.structures.Scope;
import java.util.LinkedHashMap;

public class ClassSymbolScope extends Symbol implements Scope {
    public Scope scope;
    public LinkedHashMap<String, MethodSymbolScope> funcs;
    public LinkedHashMap<String, IdSymbol> attrs;

    public ClassSymbolScope(String name, Scope scope) {
        super(name);
        this.scope = scope;
        funcs = new LinkedHashMap<>();
        attrs = new LinkedHashMap<>();
    }
    @Override
    public Symbol lookup(String identifier) {
        Symbol foundSymbol = attrs.get(identifier);
        if (foundSymbol != null) {
            return foundSymbol;
        }

        return (scope != null) ? scope.lookup(identifier) : null;
    }

    public Symbol lookupMethod(String identifier) {
        Symbol foundMethodSymbol = funcs.get(identifier);
        if (foundMethodSymbol != null) {
            return foundMethodSymbol;
        }

        return (scope != null) ? scope.lookup(identifier) : null;
    }

    @Override
    public boolean add(Symbol symbol) {
        if (symbol instanceof IdSymbol) {
            IdSymbol idSymbol = (IdSymbol) symbol;
            return attrs.putIfAbsent(idSymbol.getName(), idSymbol) == null;
        } else if (symbol instanceof MethodSymbolScope) {
            MethodSymbolScope methodSymbol = (MethodSymbolScope) symbol;
            return funcs.putIfAbsent(methodSymbol.getName(), methodSymbol) == null;
        }
        return false;
    }

    @Override
    public Scope getParent() {
        return scope;
    }

    public String getChildName(){
        if(scope instanceof ClassSymbolScope){
            return ((ClassSymbolScope) scope).getName();
        }
        else{
            return null;
        }
    }

    @Override
    public boolean addClass(Class classNode) {
        return false;
    }

    @Override
    public Class getClassByName(String name) {
        return null;
    }
}
