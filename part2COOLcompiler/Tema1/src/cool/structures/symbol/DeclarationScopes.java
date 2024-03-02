package cool.structures.symbol;
import cool.structures.Symbol;
import cool.structures.scope.ClassSymbolScope;
import cool.structures.scope.MethodSymbolScope;
import cool.structures.symbol.Declarations;

import java.util.ArrayList;
import java.util.List;

public class DeclarationScopes {
    public static ClassSymbolScope objectScope = new ClassSymbolScope("Object", null);
    public static ClassSymbolScope intClassScope = new ClassSymbolScope("Int", objectScope);
    public static ClassSymbolScope boolClassScope = new ClassSymbolScope("Bool", objectScope);
    public static ClassSymbolScope ioClassScope = new ClassSymbolScope("IO", objectScope);
    public static ClassSymbolScope stringClassScope = new ClassSymbolScope("String", objectScope);
    public static ClassSymbolScope selfTypeClassScope = new ClassSymbolScope("SELF_TYPE", objectScope);
    public static ArrayList<Symbol> forbiddenInherit = new ArrayList<>();

    // Object methods
    public static MethodSymbolScope abortMethod = new MethodSymbolScope("abort", objectScope, Declarations.objectType);
    public static MethodSymbolScope typeNameMethod = new MethodSymbolScope("type_name", objectScope, Declarations.objectType);
    public static MethodSymbolScope copyMethod = new MethodSymbolScope("copy", objectScope, Declarations.objectType);

    // IO symbols
    public static IdSymbol stringVar = new IdSymbol("x", "String");
    public static IdSymbol intVar = new IdSymbol("x", "Int");

    // IO methods
    public static MethodSymbolScope outStringMethod = new MethodSymbolScope("out_string", ioClassScope, Declarations.selfType);
    public static MethodSymbolScope outIntMethod = new MethodSymbolScope("out_int", ioClassScope, Declarations.selfType);
    public static MethodSymbolScope inStringMethod = new MethodSymbolScope("in_string", ioClassScope, Declarations.stringType);
    public static MethodSymbolScope inIntMethod = new MethodSymbolScope("in_int", ioClassScope, Declarations.intType);

    // String symbols
    public static IdSymbol s = new IdSymbol("s", Declarations.stringType);
    public static IdSymbol i = new IdSymbol("i", Declarations.intType);
    public static IdSymbol l = new IdSymbol("l", Declarations.intType);

    // String methods
    public static MethodSymbolScope concatMethod = new MethodSymbolScope("concat", stringClassScope, Declarations.stringType);
    public static MethodSymbolScope lengthMethod = new MethodSymbolScope("length", stringClassScope, Declarations.intType);
    public static MethodSymbolScope substrMethod = new MethodSymbolScope("substr", stringClassScope, Declarations.stringType);
}
