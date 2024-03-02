package cool.compiler;
import cool.structures.SymbolTable;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import cool.lexer.*;
import cool.parser.CoolParser;
import cool.parser.CoolParserBaseVisitor;
import java.util.Optional;
public class Compiler {
    // Annotates class nodes with the names of files where they are defined.
    public static ParseTreeProperty<String> fileNames = new ParseTreeProperty<>();

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("No file(s) given");
            return;
        }
        
        CoolLexer lexer = null;
        CommonTokenStream tokenStream = null;
        CoolParser parser = null;
        ParserRuleContext globalTree = null;
        
        // True if any lexical or syntax errors occur.
        boolean lexicalSyntaxErrors;
        
        // Parse each input file and build one big parse tree out of
        // individual parse trees.
        for (var fileName : args) {
            var input = CharStreams.fromFileName(fileName);

            // Lexer
            if (lexer == null)
                lexer = new CoolLexer(input);
            else
                lexer.setInputStream(input);

            // Token stream
            if (tokenStream == null)
                tokenStream = new CommonTokenStream(lexer);
            else
                tokenStream.setTokenSource(lexer);


            // Test lexer only.
            tokenStream.fill();
//            List<Token> tokens = tokenStream.getTokens();
//            tokens.stream().forEach(token -> {
//                var text = token.getText();
//                var name = CoolLexer.VOCABULARY.getSymbolicName(token.getType());
//            });

            // Parser
            if (parser == null)
                parser = new CoolParser(tokenStream);
            else
                parser.setTokenStream(tokenStream);

            // Customized error listener, for including file names in error
            // messages.
            var errorListener = new BaseErrorListener() {
                public boolean errors = false;

                @Override
                public void syntaxError(Recognizer<?, ?> recognizer,
                                        Object offendingSymbol,
                                        int line, int charPositionInLine,
                                        String msg,
                                        RecognitionException e) {
                    String newMsg = "\"" + new File(fileName).getName() + "\", line " +
                            line + ":" + (charPositionInLine + 1) + ", ";

                    Token token = (Token) offendingSymbol;
                    if (token.getType() == CoolLexer.ERROR)
                        newMsg += "Lexical error: " + token.getText();
                    else
                        newMsg += "Syntax error: " + msg;

                    System.err.println(newMsg);
                    errors = true;
                }
            };

            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);

            // Actual parsing
            var tree = parser.program();
            if (globalTree == null)
                globalTree = tree;
            else
                // Add the current parse tree's children to the global tree.
                for (int i = 0; i < tree.getChildCount(); i++)
                    globalTree.addAnyChild(tree.getChild(i));

            // Annotate class nodes with file names, to be used later
            // in semantic error messages.
            for (int i = 0; i < tree.getChildCount(); i++) {
                var child = tree.getChild(i);
                // The only ParserRuleContext children of the program node
                // are class nodes.
                if (child instanceof ParserRuleContext)
                    fileNames.put(child, fileName);
            }

            // Record any lexical or syntax errors.
            lexicalSyntaxErrors = errorListener.errors;

            // Stop before semantic analysis phase, in case errors occurred.
            if (lexicalSyntaxErrors) {
                System.err.println("Compilation halted");
                return;
            }

            // TODO Print tree
            // create new simplified tree & independent of syntax
            // node for every program, class, attr
            var astConstructionVisitor = new CoolParserBaseVisitor<ASTNode>() {
                @Override
                public ASTNode visitProgram(CoolParser.ProgramContext ctx) {
                    List<Class> classes = new ArrayList<>();
                    for (CoolParser.ClassContext classContext : ctx.class_()) {
                        classes.add((Class) visit(classContext));
                    }
                    return new Program(ctx, ctx.start, classes) {};
                }

                @Override
                public ASTNode visitClass(CoolParser.ClassContext ctx) {
                    Token myClassInherit = ctx.inherit != null ? ctx.inherit : null;
                    List<Feature> features = new ArrayList<>();
                    for (CoolParser.FeatureContext featureContext : ctx.feature()) {
                        features.add((Feature) visit(featureContext));
                    }
                    return new Class(ctx, ctx.type, ctx.inherit, ctx.parent.toString(), features, ctx.type) {};
                }

            @Override
            public ASTNode visitMethod(CoolParser.MethodContext ctx) {
                List<Formal> listOfFormals = null;
                List<Expression> listOfExpr = new ArrayList<>();

                Expression expr = (Expression) visit(ctx.expr());
                listOfExpr.add(expr);

                if (ctx.formalParams != null) {
                    listOfFormals = new ArrayList<>();
                    for (CoolParser.FormalContext formalContext : ctx.formalParams) {
                        Formal formal = new Formal(ctx, formalContext.id, formalContext.type, ctx.start) {};
                        listOfFormals.add(formal);
                    }
                }

                Token returnType = ctx.type;

                return new Feature(ctx, ctx.id, returnType, listOfExpr, 0, 1, listOfFormals, ctx.ID().getSymbol()) {};
            }

                @Override
                public ASTNode visitCase_expr(CoolParser.Case_exprContext ctx) {
                    Token start = ctx.start;
                    Token identifier = ctx.ID(0).getSymbol();
                    Token type = ctx.type;

                    Expression expression = (Expression) visit(ctx.expr());

                    return new CaseExpressions(ctx, start, identifier, expression, type);
                }

                @Override
                public ASTNode visitCase_block(CoolParser.Case_blockContext ctx) {
                    Token start = ctx.start;

                    Expression condition = (Expression) visit(ctx.cond);

                    List<Expression> expressions = ctx.expr().stream()
                            .skip(1)
                            .map(obj -> (Expression) visit(obj))
                            .toList();

                    return new CaseLines(ctx, start, condition, expressions);
                }

                @Override
                public ASTNode visitAttribute(CoolParser.AttributeContext ctx) {
                    List<Expression> listOfExpr = new ArrayList<>();

                    Expression expression = (ctx.expr() != null) ? (Expression) visit(ctx.expr()) : null;
                    if(expression != null)
                        listOfExpr.add(expression);

                    Token id = ctx.id;
                    Token type = ctx.type;

                    return new Feature(ctx, id, type, listOfExpr, 1, 0, null, ctx.ID().getSymbol()) {};
                }

                @Override
                public ASTNode visitLocal(CoolParser.LocalContext ctx) {
                    Expression expr = null;
                    if (ctx.expr() != null) {
                        expr = (Expression) visit(ctx.expr());
                    }
                    Token identifier = ctx.ID(0).getSymbol();
                    Token type = ctx.type;

                    return new Locals(ctx, expr, identifier, type, ctx.start) {};
                }


                @Override
                public ASTNode visitFormal(CoolParser.FormalContext ctx) {
                    Token id = ctx.id;
                    Token type = ctx.type;

                    return new Formal(ctx, id, type, ctx.start) {};
                }


                @Override
                public ASTNode visitDispatch(CoolParser.DispatchContext ctx) {
                    Token start = ctx.start;
                    String type = ctx.type != null ? ctx.type.getText() : null;
                    String id = ctx.id.getText();

                    List<Expression> expressions = ctx.expr().stream().skip(1).map(obj -> (Expression) visit(obj)).toList();
                    Expression dispatchExpression = (Expression) visit(ctx.expr(0));

                    return new Dispatch(ctx, start, type, id, expressions, dispatchExpression);
                }

                @Override
                public ASTNode visitImplicit_dispatch(CoolParser.Implicit_dispatchContext ctx) {
                    Token start = ctx.start;

                    List<Expression> expressions = new ArrayList<>();
                    for (CoolParser.ExprContext exprContext : ctx.expr()) {
                        expressions.add((Expression) visit(exprContext));
                    }

                    String id = ctx.ID().getText();

                    return new ImplicitDispatch(ctx, start, expressions, id);
                }

                @Override
                public ASTNode visitWhile(CoolParser.WhileContext ctx) {
                    Token start = ctx.start;

                    List<Expression> expressions = ctx.expr().stream()
                            .map(obj -> (Expression) visit(obj))
                            .toList();

                    return new While(ctx, ctx.cond.start, ctx.WHILE().getSymbol(), expressions.get(0), expressions.get(1));
                }

                @Override
                public ASTNode visitIf(CoolParser.IfContext ctx) {
                    Token start = ctx.start;

                    List<Expression> expressions = ctx.expr().stream()
                            .map(obj -> (Expression) visit(obj))
                            .toList();

                    return new If(ctx, start, expressions.get(0), expressions.get(1), expressions.get(2), ctx.cond.start);
                }

                @Override
                public ASTNode visitBlock(CoolParser.BlockContext ctx) {
                    Token start = ctx.start;

                    List<Expression> expressions = ctx.expr().stream()
                            .map(obj -> (Expression) visit(obj))
                            .toList();

                    return new Block(ctx, start, expressions);
                }

                @Override
                public ASTNode visitLet(CoolParser.LetContext ctx) {
                    Token start = ctx.start;
                    Expression expression = (Expression) visit(ctx.expr());

                    List<Locals> localsList = ctx.local().stream()
                            .map(let -> (Locals) visit(let))
                            .toList();

                    return new Let(ctx, start, expression, localsList);
                }

                @Override
                public ASTNode visitNot(CoolParser.NotContext ctx) {
                    Token start = ctx.start;
                    Expression value = (Expression) visit(ctx.expr());
                    Token operation = ctx.NOT().getSymbol();

                    return new Unary(ctx, start, value, operation);
                }

                @Override
                public ASTNode visitNew(CoolParser.NewContext ctx) {
                    Token start = ctx.start;
                    Token typeName = ctx.type;

                    return new New(ctx, start, typeName);
                }

                @Override
                public ASTNode visitNegation(CoolParser.NegationContext ctx) {
                    Token start = ctx.start;
                    Expression value = (Expression) visit(ctx.expr());
                    Token operation = ctx.TILDA().getSymbol();

                    return new Unary(ctx, start, value, operation);
                }

                @Override
                public ASTNode visitIsvoid(CoolParser.IsvoidContext ctx) {
                    Token start = ctx.start;
                    Expression value = (Expression) visit(ctx.expr());
                    Token operation = ctx.ISVOID().getSymbol();

                    return new Unary(ctx, start, value, operation);
                }

                @Override
                public ASTNode visitAssignment(CoolParser.AssignmentContext ctx) {
                    Token start = ctx.start;
                    Expression expression = (Expression) visit(ctx.expr());
                    Token identifier = ctx.ID().getSymbol();

                    return new Assignment(ctx, start, expression, identifier);
                }

                @Override
                public ASTNode visitTrue(CoolParser.TrueContext ctx) {
                    return new True(ctx, ctx.TRUE().getSymbol());
                }

                @Override
                public ASTNode visitFalse(CoolParser.FalseContext ctx) {
                    return new False(ctx, ctx.FALSE().getSymbol());
                }

                @Override
                public ASTNode visitPlusminus(CoolParser.PlusminusContext ctx) {
                    Token start = ctx.start;
                    Expression left = (Expression) visit(ctx.expr(0));
                    Expression right = (Expression) visit(ctx.expr(1));
                    Token operation = ctx.op;

                    return new Arithmetic(ctx, start, left, right, operation);
                }

                @Override
                public ASTNode visitMultdiv(CoolParser.MultdivContext ctx) {
                    Token start = ctx.start;
                    Expression left = (Expression) visit(ctx.expr(0));
                    Expression right = (Expression) visit(ctx.expr(1));
                    Token operation = ctx.op;

                    return new Arithmetic(ctx, start, left, right, operation);
                }

                @Override
                public ASTNode visitEqual(CoolParser.EqualContext ctx) {
                    Token start = ctx.start;
                    Expression left = (Expression) visit(ctx.expr(0));
                    Expression right = (Expression) visit(ctx.expr(1));

                    return new LeftOPRight(ctx, ctx.EQ().getSymbol(), left, ctx.EQ().getSymbol(), right);
                }


                @Override
                public ASTNode visitLess_than(CoolParser.Less_thanContext ctx) {
                    Token start = ctx.start;
                    Expression left = (Expression) visit(ctx.expr(0));
                    Expression right = (Expression) visit(ctx.expr(1));

                    return new LeftOPRight(ctx, ctx.LT().getSymbol(), left, ctx.LT().getSymbol(), right);
                }

                @Override
                public ASTNode visitLess_than_or_equal(CoolParser.Less_than_or_equalContext ctx) {
                    Token start = ctx.start;
                    Expression left = (Expression) visit(ctx.expr(0));
                    Expression right = (Expression) visit(ctx.expr(1));

                    return new LeftOPRight(ctx, ctx.LE().getSymbol(), left, ctx.LE().getSymbol(), right);
                }

                @Override
                public ASTNode visitParentheses(CoolParser.ParenthesesContext ctx) {
                    Token start = ctx.start;
                    Expression value = (Expression) visit(ctx.expr());

                    return new Unary(ctx, start, value, ctx.start);
                }

                @Override
                public ASTNode visitIdentifier(CoolParser.IdentifierContext ctx) {
                    Token idToken = ctx.ID().getSymbol();
                    return new Id(ctx, idToken);
                }

                @Override
                public ASTNode visitInteger(CoolParser.IntegerContext ctx) {
                    Token intToken = ctx.INT().getSymbol();
                    return new Int(ctx, intToken);
                }

                @Override
                public ASTNode visitString(CoolParser.StringContext ctx) {
                    return new VarString(ctx, ctx.STRING().getSymbol());
                }
            };

            var astNode = astConstructionVisitor.visit(tree); // genereaza astnode cu noduri cu nume - creaza struct tree
            // parcurgere
//            var printTreeVisitorsEach = new ASTVisitor<Void>() {
//                int space = 0;
//                void addSpaces(String message) {
//                    StringBuilder spaces = new StringBuilder();
//                    spaces.append("  ".repeat(Math.max(0, space)));
//                    System.out.println(spaces + message);
//                }
//
//                int incrementIndentation(int space){
//                    space++;
//                    return space;
//                }
//
//                int decrementIndentation(int space){
//                    space--;
//                    return space;
//                }

                // visit Program
//                @Override
//                public Void visit(Program program) {
//                    addSpaces("program");
//                    List<Class> classes = program.getClasses();
//                    Iterator<Class> iterator = classes.iterator();
//                    while (iterator.hasNext()) {
//                        Class classToken = iterator.next();
//                        space = incrementIndentation(space);
//                        classToken.accept(this); // instance of
//                        space = decrementIndentation(space);
//                    }
//                    return null;
//                }
//
//                // visit Class
//                @Override
//                public Void visit(Class myClass) {
//                    addSpaces("class");
//                    int originalSpace = space;
//                    space = incrementIndentation(space);
//                    addSpaces(myClass.getMyClass());
//
//                    if(myClass.getChildClass() != null)
//                        addSpaces(myClass.getChildClass());
//                    myClass.getFeatures().forEach(feature -> feature.accept(this));
//
//                    space = originalSpace;
//                    return null;
//                }
//
//                // visit Expression
//                @Override
//                public Void visit(Expression expression) {
//                    String text = expression.getToken().getText();
//                    addSpaces(text);
//                    return null;
//                }
//
//                // visit Feature - method and attributes
//                @Override
//                public Void visit(Feature feature) {
//                    String featureType = (feature.getUpdatedAttributeContext() == 1) ? "attribute" : "method";
//                    addSpaces(featureType);
//                    space = incrementIndentation(space);
//
//                    addSpaces(feature.getUpdatedFeatureId());
//
//                    if ("method".equals(featureType) && feature.getUpdatedFormalList() != null) {
//                        feature.getUpdatedFormalList().forEach(formal -> formal.accept(this));
//                    }
//
//                    addSpaces(feature.getUpdatedFeatureType());
//
//                    if (!feature.getUpdatedExpressions().isEmpty()) {
//                        feature.getUpdatedExpressions().forEach(expr -> expr.accept(this));
//                    }
//
//                    space = decrementIndentation(space);
//                    return null;
//                }
//
//                // visit Block
//                @Override
//                public Void visit(Block block) {
//                    addSpaces("block");
//                    int originalSpace = space;
//                    space = incrementIndentation(space);
//
//                    block.getExpression().forEach(expr -> expr.accept(this));
//
//                    space = originalSpace;
//                    return null;
//                }
//
//                // visit While Token
//                @Override
//                public Void visit(While whileT) {
//                    addSpaces("while");
//                    space = incrementIndentation(space);
//
//                    whileT.getCond().accept(this);
//                    whileT.getInside().accept(this);
//
//                    space = decrementIndentation(space);
//                    return null;
//                }
//
//                // visit If token
//                @Override
//                public Void visit(If ifVar) {
//                    addSpaces("if");
//                    space = incrementIndentation(space);
//
//                    ifVar.getCond().accept(this);
//                    ifVar.getThen().accept(this);
//                    ifVar.getElseCase().accept(this);
//
//                    space = decrementIndentation(space);
//                    return null;
//                }
//
//                // visit Let
//                @Override
//                public Void visit(Let let) {
//                    addSpaces("let");
//                    space = incrementIndentation(space);
//
//                    let.getLets().forEach(local -> local.accept(this));
//                    let.getExpression().accept(this);
//
//                    space = decrementIndentation(space);
//                    return null;
//                }
//
//                // visit ArithmeticOperations
//                @Override
//                public Void visit(Arithmetic arithmeticOP) {
//                    addSpaces(arithmeticOP.getOperation());
//                    space = incrementIndentation(space);
//
//                    arithmeticOP.getLeftEx().accept(this);
//                    arithmeticOP.getRightEx().accept(this);
//
//                    space = decrementIndentation(space);
//
//                    return null;
//                }
//
//
//                // visit False Token
//                @Override
//                public Void visit(False fToken) {
//                    String text = fToken.getToken().getText();
//                    addSpaces(text);
//                    return null;
//                }
//
//                // visit True Token
//                @Override
//                public Void visit(True trueT) {
//                    String str = trueT.token.getText();
//                    addSpaces(str);
//                    return null;
//                }
//
//                // visit Cases - block and assignment
//                @Override
//                public Void visit(CaseLines caseLines) {
//                    addSpaces("case");
//                    int originalSpace = space;
//                    space = incrementIndentation(space);
//
//                    caseLines.getCond().accept(this);
//                    caseLines.getCases().forEach(expr -> expr.accept(this));
//
//                    space = originalSpace;
//
//                    return null;
//                }
//
//                @Override
//                public Void visit(CaseExpressions caseExpressions) {
//                    addSpaces("case branch");
//                    int originalSpace = space;
//                    space = incrementIndentation(space);
//
//                    addSpaces(caseExpressions.getId());
//                    addSpaces(caseExpressions.getType());
//                    caseExpressions.getExpression().accept(this);
//
//                    space = originalSpace;
//                    return null;
//                }
//
//                // visit Assignment Operations
//                @Override
//                public Void visit(Assignment assignmentOP) {
//                    addSpaces("<-"); // assignment
//                    space = incrementIndentation(space);
//
//                    addSpaces(assignmentOP.getId());
//                    assignmentOP.getExpression().accept(this);
//
//                    space = decrementIndentation(space);
//
//                    return null;
//                }
//
//                // visit Dispatch
//                @Override
//                public Void visit(Dispatch dispatch) {
//                    addSpaces(".");
//                    int originalSpace = space;
//                    space = incrementIndentation(space);
//                    // caine.latra - caine
//                    dispatch.getExpression().accept(this);
//
//                    Optional.ofNullable(dispatch.getType()).ifPresent(this::addSpaces);
//                    addSpaces(dispatch.getId());
//                    // accept lista de params
//                    dispatch.getExpressionsList().forEach(expr -> expr.accept(this));
//
//                    space = originalSpace;
//                    return null;
//                }
//
//                @Override
//                public Void visit(ImplicitDispatch dispatch) {
//                    addSpaces("implicit dispatch");
//                    space = incrementIndentation(space);
//
//                    addSpaces(dispatch.getId());
//
//                    dispatch.getExpressions().forEach(expr -> expr.accept(this));
//
//                    space = decrementIndentation(space);
//                    return null;
//                }
//
//                // visit Formal
//                @Override
//                public Void visit(Formal formal) {
//                    addSpaces("formal");
//                    space = incrementIndentation(space);
//
//                    addSpaces(formal.getId());
//                    addSpaces(formal.getType());
//
//                    space = decrementIndentation(space);
//                    return null;
//                }
//
//                // visit Local Token
//                @Override
//                public Void visit(Locals local) {
//                    addSpaces("local");
//                    int originalSpace = space;
//                    space = incrementIndentation(space);
//
//                    addSpaces(local.getId());
//                    addSpaces(local.getTypeOrId());
//
//                    if (local.getExpression() != null) {
//                        local.getExpression().accept(this);
//                    }
//
//                    space = originalSpace;
//                    return null;
//                }
//
//                // visit Id type
//                @Override
//                public Void visit(Id id) {
//                    String text = id.getToken().getText();
//                    addSpaces(text);
//                    return null;
//                }
//                // visit Int type
//                @Override
//                public Void visit(Int intType) {
//                    String intint = intType.token.getText();
//                    addSpaces(intint);
//                    return null;
//                }
//
//                // visit New
//                @Override
//                public Void visit(New news) {
//                    addSpaces("new");
//                    int originalSpace = space;
//                    space = incrementIndentation(space);
//
//                    addSpaces(news.getId());
//
//                    space = originalSpace;
//                    return null;
//                }
//
//                @Override
//                public Void visit(LeftOPRight leftOPRight) {
//                    addSpaces(leftOPRight.getOperation());
//                    space = incrementIndentation(space);
//                    // 5+3 - visit 5 + 3
//                    leftOPRight.getLeftEx().accept(this);
//                    leftOPRight.getRightEx().accept(this);
//
//                    space = decrementIndentation(space);
//                    return null;
//                }
//
//                // visit String Token
//                @Override
//                public Void visit(VarString varString) {
//                    String str = varString.token.getText();
//                    addSpaces(str);
//                    return null;
//                }
//
//                // visit Unary Operations - tilde
//                @Override
//                public Void visit(Unary unary) {
//                    String operation = unary.getOperation();
//
//                    if (operation != null) {
//                        addSpaces(operation);
//                        space = incrementIndentation(space);
//                    }
//                    // unary expr (ex: ~(5+3))
//                    unary.getExpression().accept(this);
//
//                    if (operation != null) {
//                        space = decrementIndentation(space);
//                    }
//
//                    return null;
//                }
//
//            };
//
//            astNode.accept(printTreeVisitorsEach);
            // Populate global scope.
            SymbolTable.defineBasicClasses();

            //ast.accept(printVisitor);
            astNode.accept(new DefinitionPassVisitor());
            astNode.accept(new ResolutionPassVisitor());

            if (SymbolTable.hasSemanticErrors()) {
                System.err.println("Compilation halted");
                return;
            }
        }

    }
}
