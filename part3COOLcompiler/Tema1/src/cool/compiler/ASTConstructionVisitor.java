package cool.compiler;

import cool.parser.CoolParser;
import cool.parser.CoolParserBaseVisitor;
import org.antlr.v4.runtime.Token;

import java.util.LinkedList;

public class ASTConstructionVisitor extends CoolParserBaseVisitor<ASTNode> {

    @Override
    public ASTNode visitProgram(CoolParser.ProgramContext ctx) {
        LinkedList<Expression> classes = new LinkedList<>();
        ctx.clazzes.forEach(c -> classes.add((Expression)visit(c)));

        return new Program(ctx.start, classes, ctx);
    }

    @Override
    public ASTNode visitClass(CoolParser.ClassContext ctx) {
        LinkedList<Expression> features = new LinkedList<>();
        ctx.features.forEach(f -> features.add((Expression)visit(f)));

        return new Clazz(ctx.start, ctx.id, ctx.inheritedType, features, new Id(ctx.id, ctx), ctx);
    }

    @Override
    public ASTNode visitFuncDef(CoolParser.FuncDefContext ctx) {
        LinkedList<FormalDefinition> formalArguments = new LinkedList<>();
        ctx.formalArgs.forEach(f -> formalArguments.add((FormalDefinition)visit(f)));

        Expression body = (Expression)visit(ctx.body);

        return new FunctionDefinition(ctx.start, ctx.type, ctx.id, formalArguments, body, ctx);
    }

    @Override
    public ASTNode visitAttributeDef(CoolParser.AttributeDefContext ctx) {
        Expression initValue = null;
        if (ctx.initValue != null)
            initValue = (Expression) visit(ctx.initValue);

        return new AttributeDefinition(ctx.start, ctx.type, ctx.id, initValue, ctx);
    }

    @Override
    public ASTNode visitFormal(CoolParser.FormalContext ctx) {
        return new FormalDefinition(ctx.start, ctx.type, ctx.id, ctx);
    }

    @Override
    public ASTNode visitNew(CoolParser.NewContext ctx) {
        return new NewExpression(ctx.start, ctx.type, ctx);
    }

    @Override
    public ASTNode visitPlusMinus(CoolParser.PlusMinusContext ctx) {
        Expression left = (Expression) visit(ctx.left);
        Expression right = (Expression) visit(ctx.right);

        return new ArithmeticOperation(ctx.start, left, right, ctx.operation, ctx);
    }

    @Override
    public ASTNode visitDispatch(CoolParser.DispatchContext ctx) {
        Expression dispatcher = (Expression) visit(ctx.dispacher);
        LinkedList<Expression> actualParams = new LinkedList<>();
        ctx.actualParams.forEach(a -> actualParams.add((Expression)visit(a)));

        return new Dispatch(ctx.start, dispatcher, ctx.subclass, ctx.func, actualParams, false, ctx);
    }

    @Override
    public ASTNode visitString(CoolParser.StringContext ctx) {
        return new Stringg(ctx.STRING().getSymbol(), ctx);
    }

    @Override
    public ASTNode visitBool(CoolParser.BoolContext ctx) {

        return new Bool(ctx.boolValue, ctx);
    }

    @Override
    public ASTNode visitIsvoid(CoolParser.IsvoidContext ctx) {
        Expression expression = (Expression) visit(ctx.expression);

        return new IsVoid(ctx.start, expression, ctx);
    }

    @Override
    public ASTNode visitImplicitDispatch(CoolParser.ImplicitDispatchContext ctx) {
        LinkedList<Expression> actualParams = new LinkedList<>();
        ctx.actualParams.forEach(a -> actualParams.add((Expression)visit(a)));

        return new Dispatch(ctx.start, null, null, ctx.func, actualParams, true, ctx);
    }

    @Override
    public ASTNode visitWhile(CoolParser.WhileContext ctx) {
        Expression cond = (Expression) visit(ctx.cond);
        Expression body = (Expression) visit(ctx.body);

        return new While(ctx.start, cond, body, ctx);
    }

    @Override
    public ASTNode visitTilde(CoolParser.TildeContext ctx) {
        Expression expression = (Expression) visit(ctx.expression);

        return new TildeOperation(ctx.start, expression, ctx);
    }

    @Override
    public ASTNode visitInt(CoolParser.IntContext ctx) {
        return new Int(ctx.INT().getSymbol(), ctx);
    }

    @Override
    public ASTNode visitMultDivide(CoolParser.MultDivideContext ctx) {
        Expression left = (Expression) visit(ctx.left);
        Expression right = (Expression) visit(ctx.right);

        return new ArithmeticOperation(ctx.start, left, right, ctx.operation, ctx);
    }

    @Override
    public ASTNode visitNot(CoolParser.NotContext ctx) {
        Expression expression = (Expression) visit(ctx.expression);

        return new Not(ctx.start, expression, ctx);
    }

    @Override
    public ASTNode visitParen(CoolParser.ParenContext ctx) {
        Expression body = (Expression) visit(ctx.expression);

        return new ParenthesisOperation(ctx.start, body, ctx);
    }

    @Override
    public ASTNode visitBlock(CoolParser.BlockContext ctx) {
        LinkedList<Expression> expressions = new LinkedList<>();
        ctx.expressions.forEach(e -> expressions.add((Expression) visit(e)));

        return new Block(ctx.start, expressions, ctx);
    }

    @Override
    public ASTNode visitLet(CoolParser.LetContext ctx) {
        LinkedList<Token> ids = new LinkedList<>();
        LinkedList<Token> types = new LinkedList<>();
        LinkedList<Expression> initValues = new LinkedList<>();
        Expression body = (Expression) visit(ctx.body) ;

        ids.addAll(ctx.ids);
        types.addAll(ctx.types);
        ctx.initValues.forEach(v -> initValues.add((Expression) visit(v)));

        return new Let(ctx.start, ids, types, initValues, body, ctx);
    }

    @Override
    public ASTNode visitRelational(CoolParser.RelationalContext ctx) {
        Expression left = (Expression) visit(ctx.left);
        Expression right = (Expression) visit(ctx.right);

        return new RelationalOperation(ctx.start, left, right, ctx.relation, ctx);
    }

    @Override
    public ASTNode visitId(CoolParser.IdContext ctx) {
        return new Id(ctx.ID().getSymbol(), ctx);
    }

    @Override
    public ASTNode visitIf(CoolParser.IfContext ctx) {
        Expression cond = (Expression) visit(ctx.cond);
        Expression then = (Expression) visit(ctx.then);
        Expression elsee = (Expression) visit(ctx.else_);

        return new Iff(ctx.start, cond, then, elsee, ctx);
    }

    @Override
    public ASTNode visitCase(CoolParser.CaseContext ctx) {
        LinkedList<Token> ids = new LinkedList<>();
        LinkedList<Token> types = new LinkedList<>();
        LinkedList<Expression> bodies = new LinkedList<>();
        Expression cond = (Expression) visit(ctx.cond);

        ids.addAll(ctx.ids);
        types.addAll(ctx.types);
        ctx.bodies.forEach(v -> bodies.add((Expression) visit(v)));

        return new Case(ctx.start, cond, ids, types, bodies, ctx);
    }

    @Override
    public ASTNode visitAssign(CoolParser.AssignContext ctx) {
        Expression right = (Expression) visit(ctx.right);

        return new Assign(ctx.start, ctx.left, right, ctx);
    }
}
