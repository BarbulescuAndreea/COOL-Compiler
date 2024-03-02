package cool.structures.scope;
import cool.structures.Scope;
import cool.compiler.Class;
import java.util.LinkedHashMap;
import cool.structures.Symbol;
import cool.structures.SymbolTable;
import cool.structures.symbol.Declarations;

public class MethodSymbolScope extends Symbol implements Scope {
    public Scope scope;
    public boolean solvedTrue;
    public Symbol returnMethod;
    public String method;
    public LinkedHashMap<String, Symbol> params;

    public MethodSymbolScope(String name, Scope scope, String type) {
        super(name);
        this.scope = scope;
        this.method = type;
        params = new LinkedHashMap<>();
    }
    @Override
    public boolean add(Symbol symbol) {
        if (!params.containsKey(symbol.getName())) {
            params.put(symbol.getName(), symbol);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Symbol lookup(String identifier) {
        if (params.containsKey(identifier)) {
            return params.get(identifier);
        } else if (scope != null) {
            return scope.lookup(identifier);
        } else {
            return null;
        }
    }


    @Override
    public boolean addClass(Class classNode) {
        return false;
    }

    @Override
    public Scope getParent() {
        return scope;
    }

    @Override
    public Class getClassByName(String name) {
        return null;
    }

    public boolean isSolvedTrue() {
        return solvedTrue;
    }


    public void setSolvedTrue(boolean solvedTrue) {
        this.solvedTrue = solvedTrue;
    }

    public LinkedHashMap<String, Symbol> getParams() {
        return params;
    }

    public void setParams(LinkedHashMap<String, Symbol> params) {
        this.params = params;
    }

    public Symbol getReturnMethod() {
        return returnMethod;
    }

    public void setReturnMethod(Symbol returnMethod) {
        this.returnMethod = returnMethod;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public void resolve() {
        returnMethod = Declarations.globals.lookup(method);
        solvedTrue = true;
    }

    public void setReturnType(Symbol returnType) {
        this.returnMethod = returnType;
    }
}
