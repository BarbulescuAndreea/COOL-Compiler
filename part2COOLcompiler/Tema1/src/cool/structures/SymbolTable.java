package cool.structures;

import java.io.File;
import org.antlr.v4.runtime.*;
import cool.compiler.Compiler;
import cool.parser.CoolParser;
import cool.structures.symbol.Declarations;
import cool.structures.symbol.DeclarationScopes;

public class SymbolTable {
    public static void defineBasicClasses() {
        Declarations.Err = false;

        Declarations.globals = new DefaultScope(null);
        Declarations.globals.add(DeclarationScopes.objectScope);
        Declarations.globals.add(DeclarationScopes.stringClassScope);
        Declarations.globals.add(DeclarationScopes.selfTypeClassScope);
        Declarations.globals.add(DeclarationScopes.intClassScope);
        Declarations.globals.add(DeclarationScopes.ioClassScope);
        Declarations.globals.add(DeclarationScopes.boolClassScope);
        DeclarationScopes.forbiddenInherit.add(DeclarationScopes.intClassScope);
        DeclarationScopes.forbiddenInherit.add(DeclarationScopes.stringClassScope);
        DeclarationScopes.forbiddenInherit.add(DeclarationScopes.selfTypeClassScope);
        DeclarationScopes.forbiddenInherit.add(DeclarationScopes.boolClassScope);
        DeclarationScopes.ioClassScope.add(DeclarationScopes.outStringMethod);
        DeclarationScopes.ioClassScope.add(DeclarationScopes.outIntMethod);
        DeclarationScopes.ioClassScope.add(DeclarationScopes.inStringMethod);
        DeclarationScopes.ioClassScope.add(DeclarationScopes.inIntMethod);
        DeclarationScopes.objectScope.add(DeclarationScopes.copyMethod);
        DeclarationScopes.objectScope.add(DeclarationScopes.abortMethod);
        DeclarationScopes.abortMethod.setReturnType(DeclarationScopes.objectScope);
        DeclarationScopes.abortMethod.setSolvedTrue(true);
        DeclarationScopes.objectScope.add(DeclarationScopes.typeNameMethod);
        DeclarationScopes.typeNameMethod.setReturnType(DeclarationScopes.objectScope);
        DeclarationScopes.typeNameMethod.setSolvedTrue(true);
        DeclarationScopes.stringClassScope.add(DeclarationScopes.substrMethod);
        DeclarationScopes.stringClassScope.add(DeclarationScopes.concatMethod);
        DeclarationScopes.stringClassScope.add(DeclarationScopes.lengthMethod);
        DeclarationScopes.inStringMethod.setReturnType(DeclarationScopes.stringClassScope);
        DeclarationScopes.inStringMethod.setSolvedTrue(true);
        DeclarationScopes.outStringMethod.setReturnType(DeclarationScopes.selfTypeClassScope);
        DeclarationScopes.outStringMethod.setSolvedTrue(true);
        DeclarationScopes.concatMethod.setReturnType(DeclarationScopes.stringClassScope);
        DeclarationScopes.concatMethod.setSolvedTrue(true);
        DeclarationScopes.inIntMethod.setReturnType(DeclarationScopes.intClassScope);
        DeclarationScopes.inIntMethod.setSolvedTrue(true);
        DeclarationScopes.outIntMethod.setReturnType(DeclarationScopes.selfTypeClassScope);
        DeclarationScopes.outIntMethod.setSolvedTrue(true);
        DeclarationScopes.copyMethod.setReturnType(DeclarationScopes.objectScope);
        DeclarationScopes.copyMethod.setSolvedTrue(true);
        DeclarationScopes.substrMethod.setReturnType(DeclarationScopes.stringClassScope);
        DeclarationScopes.substrMethod.setSolvedTrue(true);
        DeclarationScopes.lengthMethod.setReturnType(DeclarationScopes.intClassScope);
        DeclarationScopes.lengthMethod.setSolvedTrue(true);
        DeclarationScopes.intVar.setScope(DeclarationScopes.intClassScope);
        DeclarationScopes.intVar.setSolvedTrue(true);
        DeclarationScopes.stringVar.setScope(DeclarationScopes.stringClassScope);
        DeclarationScopes.stringVar.setSolvedTrue(true);
        DeclarationScopes.outStringMethod.add(DeclarationScopes.stringVar);
        DeclarationScopes.outIntMethod.add(DeclarationScopes.intVar);
        DeclarationScopes.lengthMethod.add(DeclarationScopes.i);
        DeclarationScopes.lengthMethod.add(DeclarationScopes.l);
        DeclarationScopes.s.setScope(DeclarationScopes.stringClassScope);
        DeclarationScopes.s.setSolvedTrue(true);
        DeclarationScopes.concatMethod.add(DeclarationScopes.s);
        DeclarationScopes.l.setScope(DeclarationScopes.intClassScope);
        DeclarationScopes.l.setSolvedTrue(true);
        DeclarationScopes.i.setScope(DeclarationScopes.intClassScope);
        DeclarationScopes.i.setSolvedTrue(true);
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
        
        Declarations.Err = true;
    }
    
    public static void error(String str) {
        String message = "Semantic error: " + str;
        
        System.err.println(message);
        
        Declarations.Err = true;
    }
    
    public static boolean hasSemanticErrors() {
        return Declarations.Err;
    }
}
