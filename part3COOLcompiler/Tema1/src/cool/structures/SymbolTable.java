package cool.structures;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import cool.parser.CoolParser;
import org.antlr.v4.runtime.*;

import cool.compiler.Compiler;

public class SymbolTable {
    public static Scope globals;
    
    private static boolean semanticErrors;

    private static ClassSymbol ObjectClass(Scope defaultScope) {
        ClassSymbol objectClass = new ClassSymbol(Constants.Object, defaultScope, (ClassSymbol) null);

        objectClass.addMethod(new FunctionSymbol("abort", objectClass,
                Constants.Object, new LinkedHashMap<>()));
        objectClass.addMethod(new FunctionSymbol("type_name", objectClass,
                Constants.STRING, new LinkedHashMap<>()));
        objectClass.addMethod(new FunctionSymbol("copy", objectClass,
                Constants.SELF_TYPE, new LinkedHashMap<>()));

        objectClass.addAttribute(new IdSymbol(Constants.SELF, Constants.SELF_TYPE));
        return objectClass;
    }

    private static ClassSymbol IOClass(Scope defaultScope, ClassSymbol objectClass) {
        ClassSymbol io = new ClassSymbol(Constants.IO, defaultScope, objectClass);

        Map<String, String> args = new LinkedHashMap<>();
        args.put("x", Constants.STRING);
        io.addMethod(new FunctionSymbol("out_string", io,Constants.SELF_TYPE, args));

        args = new LinkedHashMap<>();
        args.put("x", Constants.INT);
        io.addMethod(new FunctionSymbol("out_int", io, Constants.SELF_TYPE, args));

        io.addMethod(new FunctionSymbol("in_string", io, Constants.STRING, new LinkedHashMap<>()));
        io.addMethod(new FunctionSymbol("in_int", io, Constants.INT, new LinkedHashMap<>()));

        return io;
    }

    private static ClassSymbol IntClass(Scope defaultScope, ClassSymbol objectClass) {
        ClassSymbol intt = new ClassSymbol(Constants.INT, defaultScope, objectClass);

        return intt;
    }

    private static ClassSymbol StringClass(Scope defaultScope, ClassSymbol objectClass) {
        ClassSymbol stringg = new ClassSymbol(Constants.STRING, defaultScope, objectClass);

        stringg.addMethod(new FunctionSymbol("length", stringg, Constants.INT, new LinkedHashMap<>()));

        Map<String, String> args = new LinkedHashMap<>();
        args.put("s", Constants.STRING);
        stringg.addMethod(new FunctionSymbol("concat", stringg, Constants.STRING, args));

        args = new LinkedHashMap<>();
        args.put("i", Constants.INT);
        args.put("l", Constants.INT);
        stringg.addMethod(new FunctionSymbol("substr", stringg, Constants.STRING, args));

        return stringg;
    }

    private static ClassSymbol BoolClass(Scope defaultScope, ClassSymbol objectClass) {
        ClassSymbol bool = new ClassSymbol(Constants.BOOL, defaultScope, objectClass);

        return bool;
    }

    public static void defineBasicClasses() {
        globals = new DefaultScope(null);
        semanticErrors = false;
        
        // TODO Populate global scope.
        ClassSymbol objectClass = ObjectClass(globals);
        globals.add(objectClass);
        globals.add(IOClass(globals, objectClass));
        globals.add(IntClass(globals, objectClass));
        globals.add(StringClass(globals, objectClass));
        globals.add(BoolClass(globals, objectClass));
    }
    
    /**
     * Displays a semantic error message.
     * 
     * @param ctx Used to determine the enclosing class context of this error,
     *            which knows the file name in which the class was defined.
     * @param info Used for line and column information.
     * @param str The error message.
     */
    public static void error(ParserRuleContext ctx, Token info, String str) {
        while (! (ctx.getParent() instanceof CoolParser.ProgramContext))
            ctx = ctx.getParent();
        
        String message = "\"" + new File(Compiler.fileNames.get(ctx)).getName()
                + "\", line " + info.getLine()
                + ":" + (info.getCharPositionInLine() + 1)
                + ", Semantic error: " + str;
        
        System.err.println(message);
        
        semanticErrors = true;
    }
    
    public static void error(String str) {
        String message = "Semantic error: " + str;
        
        System.err.println(message);
        
        semanticErrors = true;
    }
    
    public static boolean hasSemanticErrors() {
        return semanticErrors;
    }
}
