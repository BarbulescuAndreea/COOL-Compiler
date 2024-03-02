package cool.structures.scope;

import cool.compiler.Class;
import cool.structures.Symbol;
import cool.structures.Scope;
import java.util.LinkedHashMap;

public class LetSymbolScope extends Symbol implements Scope {
    public Scope scope;
    public LinkedHashMap<String, Symbol> symbols = new LinkedHashMap<>();

    public LetSymbolScope(String name, Scope scope) {
        super(name);
        this.scope = scope;
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
    public boolean add(Symbol symbol) {
        if (symbols.containsKey(symbol.getName())) {
            return false;
        } else {
            symbols.put(symbol.getName(), symbol);
            return true;
        }
    }

    @Override
    public Symbol lookup(String identifier) {
        Symbol foundSymbol = symbols.get(identifier);
        if (foundSymbol != null) {
            return foundSymbol;
        }

        return (scope != null) ? scope.lookup(identifier) : null;
    }
}
