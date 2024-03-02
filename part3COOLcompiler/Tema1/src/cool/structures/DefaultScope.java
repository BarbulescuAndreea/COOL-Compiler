package cool.structures;

import java.util.*;

public class DefaultScope implements Scope {
    
    private Map<String, Symbol> symbols = new LinkedHashMap<>();
    
    private Scope parent;
    
    public DefaultScope(Scope parent) {
        this.parent = parent;
    }

    @Override
    public boolean add(Symbol sym) {
        // Reject duplicates in the same scope.
        if (symbols.containsKey(sym.getName()))
            return false;
        
        symbols.put(sym.getName(), sym);
        
        return true;
    }

    public Map<String, Symbol> getSymbols() {
        return symbols;
    }

    @Override
    public Symbol lookup(String name) {
        var sym = symbols.get(name);
        
        if (sym != null)
            return sym;
        
        if (parent != null)
            return parent.lookup(name);
        
        return null;
    }

    @Override
    public Scope getParent() {
        return parent;
    }

    @Override
    public Symbol lookupForVariable(String name) {
        var sym = symbols.get(name);

        if (sym != null)
            return sym;

        if (parent != null)
            return parent.lookupForVariable(name);

        return null;
    }

    @Override
    public Symbol lookupForType(String name, Scope globalScope, String enclosingClass) {
        if (Constants.SELF_TYPE.equals(name))
            return new SelfTypeSymbol(globalScope,
                    (ClassSymbol) globalScope.lookup(enclosingClass));

        if (globalScope != null)
            return globalScope.lookup(name);

        return null;
    }

    @Override
    public Symbol lookupForMethod(String name, String clazz, Scope globalScope) {
        return getParent().lookupForMethod(name, clazz, globalScope);
    }

    @Override
    public String toString() {
        return symbols.values().toString();
    }


}
