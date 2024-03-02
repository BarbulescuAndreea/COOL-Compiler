package cool.structures;
import java.util.*;
import cool.compiler.Class;

public class DefaultScope implements Scope {
    private Map<String, Symbol> symbols = new LinkedHashMap<>();
    private Scope parentScope;
    private Map<String, Class> globalScope = new LinkedHashMap<>();
    
    public DefaultScope(Scope parent) {
        this.parentScope = parent;
    }

    @Override
    public Scope getParent() {
        return parentScope;
    }

    @Override
    public String toString() {
        return symbols.values().toString();
    }

    @Override
    public Symbol lookup(String identifier) {
        Symbol foundSymbol = findSymbolLocally(identifier);

        if (foundSymbol != null) {
            return foundSymbol;
        }

        return (parentScope != null) ? parentScope.lookup(identifier) : null;
    }

    private Symbol findSymbolLocally(String identifier) {
        return symbols.get(identifier);
    }

    @Override
    public boolean add(Symbol symbol) {
        boolean isAdded = !symbols.containsKey(symbol.getName());
        if (isAdded) {
            symbols.put(symbol.getName(), symbol);
        }

        return isAdded;
    }

    public boolean addClass(Class classNode) {
        String className = classNode.getMyClassName().getText();

        if (!globalScope.containsKey(className)) {
            globalScope.put(className, classNode);
            return true;
        }

        return false;
    }

    public Class getClassByName(String name){
        return globalScope.get(name);
    }

}
