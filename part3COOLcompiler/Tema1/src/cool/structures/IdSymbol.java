package cool.structures;

public class IdSymbol extends Symbol {
    protected ClassSymbol type;

    private String typeName;

    public IdSymbol(String name, String typeName) {
        super(name);
        this.typeName = typeName;
    }

    public Symbol getSymbol() {
        return type;
    }

    public void setSymbol(ClassSymbol symbol) {
        this.type = symbol;
    }

    public ClassSymbol getType() {
        return type;
    }

    public void setType(ClassSymbol type) {
        this.type = type;
    }

    public String getTypeName() {
        return typeName;
    }
}
