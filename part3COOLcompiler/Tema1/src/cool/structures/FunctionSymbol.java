package cool.structures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class FunctionSymbol extends Symbol implements Scope {
    private Map<String, Symbol> symbols = new LinkedHashMap<>();
    private ClassSymbol parent;

    private String returnType;

    private Map<String, String> arguments = new LinkedHashMap<>();

    public FunctionSymbol(String name, ClassSymbol parent,
                          String returnType, Map<String, String> arguments) {
        super(name);
        this.parent = parent;
        this.returnType = returnType;
        this.arguments = arguments;
    }

    public FunctionSymbol(String name, ClassSymbol parent) {
        super(name);
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

    @Override
    public Symbol lookup(String name) {
//        if (name != null && !name.isEmpty()
//                && Character.isUpperCase(name.charAt(0)))
//            return lookupForType(name, getParent().getParent());

        return getParent().lookup(name);
    }

    public Response addArgument(String name, String type) {
        if (arguments.containsKey(name))
            return new Response(StatusCodes.FORMAL_PARAM_REDEFINED,
                    new ArrayList<>(Arrays.asList(this.name, getEnclosingClassName(), name)));

        // find arg type too
//        Symbol argSymbol = lookupForType(type);
//        if(argSymbol == null)
//            return new Response(StatusCodes.FUNCTION_WITH_FORMAL_PARAM_OF_UNDEFINED_TYPE,
//                    new ArrayList<>(Arrays.asList(this.name, getEnclosingClassName(), name, type)));

        arguments.put(name, type);
        return new Response(StatusCodes.OK, new ArrayList<>());
    }

    public String getEnclosingClassName() {
        if (parent == null)
            return "";

        return parent.getName();
    }

    public Symbol lookupForType(String name, Scope globalScope, String enclosingClassName) {
        if(Constants.SELF_TYPE.equals(name))
            return new SelfTypeSymbol(globalScope, (ClassSymbol) globalScope.lookupForType(enclosingClassName, globalScope , enclosingClassName));

        if (parent != null)
            return globalScope.lookup(name);

        return null;
    }

    public Symbol lookupForVariable(String name) {
        var sym = symbols.get(name);

        if (sym != null)
            return sym;

        if (parent != null)
            return parent.lookupForVariable(name);

        return null;
    }

    @Override
    public Symbol lookupForMethod(String name, String clazz, Scope globalScope) {
        ClassSymbol clazzSymbol = (ClassSymbol) globalScope.lookupForType(clazz, globalScope, null);
        FunctionSymbol functionSymbol = clazzSymbol.getLocalMethod(name);

        return functionSymbol;
    }




    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    @Override
    public Scope getParent() {
        return parent;
    }

    public Map<String, String> getArguments() {
        return arguments;
    }

    public int getArgumentsSize() {
        return arguments.size();
    }
}
