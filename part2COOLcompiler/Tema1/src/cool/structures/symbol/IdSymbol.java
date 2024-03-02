package cool.structures.symbol;
import cool.structures.Symbol;
import cool.structures.SymbolTable;
import cool.structures.scope.ClassSymbolScope;

public class IdSymbol extends Symbol {
    public ClassSymbolScope scope;
    public boolean solvedTrue;
    public String type_id;

    public IdSymbol(String name, String idType) {
        super(name);
        this.type_id = idType;
    }

    public ClassSymbolScope getScope() {
        return scope;
    }

    public void setScope(ClassSymbolScope scope) {
        this.scope = scope;
    }

    public boolean isSolvedTrue() {
        return solvedTrue;
    }

    public void setSolvedTrue(boolean solvedTrue) {
        this.solvedTrue = solvedTrue;
    }

    public String getType_id() {
        return type_id;
    }

    public void setType_id(String type_id) {
        this.type_id = type_id;
    }
    public void customResolve() {
        scope = (ClassSymbolScope) Declarations.globals.lookup(type_id);
        solvedTrue = true;
    }
}
