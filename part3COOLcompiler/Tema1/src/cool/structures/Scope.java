package cool.structures;

public interface Scope {
    public boolean add(Symbol sym);
    
    public Symbol lookup(String str);
    
    public Scope getParent();

    public Symbol lookupForVariable(String name);

    public Symbol lookupForType(String name, Scope globalScope, String enclosingClass);
    public Symbol lookupForMethod(String name, String clazz, Scope globalScope);



}
