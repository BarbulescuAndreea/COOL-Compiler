package cool.structures;

import java.util.*;

public class ClassSymbol extends Symbol implements Scope {
    private Map<String, Symbol> methods = new LinkedHashMap<>();
    private Map<String, Symbol> attributes = new LinkedHashMap<>();

    public List<String> getMethodsNames() {
        return methodsNames;
    }

    public List<String> getAttributesNames() {
        return attributesNames;
    }

    private List<String> attributesNames = new ArrayList<>();
    private List<String> methodsNames = new ArrayList<>();

    public Map<String, Symbol> getMethods() {
        return methods;
    }

    public Map<String, Symbol> getAttributes() {
        return attributes;
    }

    private Scope parent;
    private ClassSymbol inheritedClassSymbol;

    private String inheritedClassName;

    private boolean isSelfType = false;

    public ClassSymbol(String name, Scope globalScope,
                       ClassSymbol inheritedClassSymbol) {
        super(name);
        this.parent = globalScope;
        this.inheritedClassSymbol = inheritedClassSymbol;

        if (inheritedClassSymbol != null)
            this.inheritedClassName = inheritedClassSymbol.getName();
    }

    public ClassSymbol(String name, Scope globalScope,
                       String inheritedClassName) {
        super(name);
        this.parent = globalScope;

        if(inheritedClassName == null)
            this.inheritedClassName = Constants.Object;
        else
            this.inheritedClassName = inheritedClassName;
    }

    @Override
    public boolean add(Symbol sym) {
//        // Reject duplicates in the same scope.
        if (attributes.containsKey(sym.getName()))
            return false;
//
        attributes.put(sym.getName(), sym);

        return true;
//        return false;
    }

    @Override
    public Symbol lookup(String name) {
        var sym = attributes.get(name);

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
        var sym = attributes.get(name);

        if (sym != null)
            return sym;

        if (inheritedClassName != null)
            inheritedClassSymbol = (ClassSymbol) parent.lookup(inheritedClassName);

        if (inheritedClassSymbol != null)
            return inheritedClassSymbol.lookupForVariable(name);

        return null;
    }

    @Override
    public Symbol lookupForType(String name, Scope globalScope, String enclosingClassName) {
        if (Constants.SELF_TYPE.equals(name))
            return new SelfTypeSymbol(globalScope, this);

        if (globalScope != null)
            return globalScope.lookup(name);

        return null;
    }

    @Override
    public Symbol lookupForMethod(String name, String clazz, Scope globalScope) {
        ClassSymbol clazzSymbol = (ClassSymbol) globalScope.lookupForType(clazz, globalScope, null);
        FunctionSymbol functionSymbol = clazzSymbol.getLocalMethod(name);

        return functionSymbol;
    }

    /*
        Attributes
     */
    public boolean addAttribute(Symbol sym) {
        // Reject duplicates in the same scope.
        if (attributes.containsKey(sym.getName()))
            return false;

        attributes.put(sym.getName(), sym);

        return true;
    }

    public Symbol lookupForAttribute(String name) {
        var sym = attributes.get(name);

        if (sym != null)
            return sym;

        if (inheritedClassName != null)
            inheritedClassSymbol = (ClassSymbol) parent.lookup(inheritedClassName);

        if (inheritedClassSymbol != null)
            return inheritedClassSymbol.lookupForAttribute(name);

        return null;
    }

    public Symbol lookupForInheritedAttribute(String name) {
        if(inheritedClassName == null)
            return null;

        if(inheritedClassSymbol == null)
            inheritedClassSymbol = (ClassSymbol) parent.lookup(inheritedClassName);

        if (inheritedClassSymbol != null)
            return inheritedClassSymbol.lookupForAttribute(name);

        return null;
    }

    public Symbol lookupForInheritedMethod(String name) {
        if(inheritedClassName == null)
            return null;

        if(inheritedClassSymbol == null)
            inheritedClassSymbol = (ClassSymbol) parent.lookup(inheritedClassName);

        if (inheritedClassSymbol != null)
            return inheritedClassSymbol.lookupForMethod(name);

        return null;
    }

    public boolean checkForParent(ClassSymbol parentToFind) {
        if(parentToFind == null)
            return false;

        if(this.getName().equals(parentToFind.getName()))
            return true;

        if(inheritedClassName == null)
            return false;

        if(inheritedClassSymbol == null)
            inheritedClassSymbol = (ClassSymbol) parent.lookup(inheritedClassName);

        if (inheritedClassSymbol != null)
            return inheritedClassSymbol.checkForParent(parentToFind);

        return false;
    }


    /*
        Methods
     */
    public boolean addMethod(Symbol sym) {
        // Reject duplicates in the same scope.
        if (methods.containsKey(sym.getName()))
            return false;

        methods.put(sym.getName(), sym);

        return true;
    }

    public Symbol lookupForMethod(String name) {
        var sym = methods.get(name);

        if (sym != null)
            return sym;

        if (inheritedClassSymbol != null)
            return inheritedClassSymbol.lookupForMethod(name);

        return null;
    }

    public Response checkInheritence(String startChild, List<String> ancestors) {
        if(inheritedClassName == null)
            return new Response(StatusCodes.OK, new ArrayList<>());

        if(inheritedClassSymbol == null)
            inheritedClassSymbol = (ClassSymbol) parent.lookup(inheritedClassName);

        if(inheritedClassSymbol == null)
            return new Response(StatusCodes.CLASS_WITH_UNDEFINED_PARENT,
                    new ArrayList<>(Arrays.asList(startChild, inheritedClassName)));

        if(ancestors.contains(this.name))
            return new Response(StatusCodes.CLASS_CYCLIC_INHERITANCE,
                    new ArrayList<>(Arrays.asList(startChild)));

        ancestors.add(this.name);

        return inheritedClassSymbol.checkInheritence(startChild, ancestors);
    }

    public String getInheritedClassName() {
        return inheritedClassName;
    }

    public void setInheritedClassName(String inheritedClassName) {
        this.inheritedClassName = inheritedClassName;
    }

    public ClassSymbol getInheritedClassSymbol() {
        if(inheritedClassName != null && inheritedClassSymbol == null)
            inheritedClassSymbol = (ClassSymbol) parent.lookup(inheritedClassName);

        return inheritedClassSymbol;
    }

    public FunctionSymbol getLocalMethod(String name) {
        FunctionSymbol functionSymbol = (FunctionSymbol) methods.get(name);

        if(functionSymbol == null && inheritedClassName != null) {
            return getInheritedClassSymbol().getLocalMethod(name);
        }

        return functionSymbol;
    }

    public void setInheritedClassSymbol(ClassSymbol inheritedClassSymbol) {
        this.inheritedClassSymbol = inheritedClassSymbol;
    }

    public boolean isSelfType() {
        return false;
    }
}


