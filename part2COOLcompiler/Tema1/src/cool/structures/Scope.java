package cool.structures;

import cool.compiler.Class;

public interface Scope {
    public boolean add(Symbol sym);
    
    public Symbol lookup(String str);
    
    public Scope getParent();

    public boolean addClass(Class classNode);

    public Class getClassByName(String name);
}
