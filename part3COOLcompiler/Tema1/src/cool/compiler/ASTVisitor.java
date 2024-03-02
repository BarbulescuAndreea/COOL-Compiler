package cool.compiler;

public interface ASTVisitor<T> {
    T visit(Program program);

    /*
        Class related
     */
    T visit(Clazz clazz);

    T visit(AttributeDefinition attributeDefinition);

    T visit(FunctionDefinition functionDefinition);

    T visit(FormalDefinition formal);

    T visit(IsVoid isVoid);

    T visit(NewExpression newExpression);


    /*
        Variables Related
     */
    T visit(Id id);

    T visit(Assign assign);

    T visit(Dispatch dispatch);


    /*
        Int, Bool, String
     */
    T visit(Int intt);

    T visit(Bool bool);

    T visit(Stringg stringg);


    /*
        Arithmetic Operations
     */
    T visit(ArithmeticOperation operation);

    T visit(TildeOperation tildeOperation);

    T visit(ParenthesisOperation operation);


    /*
        Relational Operations
     */
    T visit(RelationalOperation relationalOperation);

    T visit(Not not);


    /*
        Block Operation
     */
    T visit(Iff iff);

    T visit(While whilee);

    T visit(Let let);

    T visit(LetItem letItem);

    T visit(Case casee);

    T visit(CaseItem caseItem);

    T visit(Block block);
}
