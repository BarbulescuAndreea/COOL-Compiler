package cool.compiler;

public interface ASTVisitor<T> {
    T visit(Program programNode);
    T visit(Class classNode);
    T visit(Feature feature);
    T visit(Locals local);
    T visit(Formal formal);
    T visit(Expression expression);
    T visit(Block block);
    T visit(If ifToken);
    T visit(Let let);
    T visit(CaseLines caseBlock);
    T visit(CaseExpressions caseExpr);
    T visit(LeftOPRight relational);
    T visit(Arithmetic arithmetic);
    T visit(Assignment assignment);
    T visit(Dispatch dispatch);
    T visit(Unary unary);
    T visit(False falseToken);
    T visit(Id id);
    T visit(VarString stringToken);
    T visit(ImplicitDispatch implicitDispatch);
    T visit(Int intToken);
    T visit(New newToken);
    T visit(True trueToken);
    T visit(While whileToken);
}

