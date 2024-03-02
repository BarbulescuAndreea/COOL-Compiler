package cool.structures;

public class SelfTypeSymbol extends ClassSymbol{

    private ClassSymbol actualClass;

    public SelfTypeSymbol(Scope globalScope,  ClassSymbol dispatcherClass) {
        super(Constants.SELF_TYPE, globalScope, (ClassSymbol) null);
        this.actualClass = dispatcherClass;
    }


    public boolean isSelfType() {
        return true;
    }

    public ClassSymbol getActualClass() {
        return actualClass;
    }

    public void setActualClass(ClassSymbol actualClass) {
        this.actualClass = actualClass;
    }
}
