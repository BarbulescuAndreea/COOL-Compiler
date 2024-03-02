package cool.compiler;

import cool.structures.*;
import org.w3c.dom.css.CSSUnknownRule;

import java.util.*;


public class DefinitionPassVisitor implements ASTVisitor<Void> {
    Scope currentScope = SymbolTable.globals;
    Scope globalScope = SymbolTable.globals;
    @Override
    public Void visit(Program program) {
        for (var clazz: program.classes) {
            clazz.accept(this);
        }

        return null;
    }

    @Override
    public Void visit(Clazz clazz) {
        var classId = clazz.classId;
        ClassSymbol symbol;

        if (clazz.inheritedType != null) {
            symbol = new ClassSymbol(classId.token.getText(), globalScope,
                    clazz.inheritedType.getText());
        } else {
            symbol = new ClassSymbol(classId.token.getText(), globalScope, (String)null);
        }


        if(clazz.id.getText().equals(Constants.SELF_TYPE)) {
            Response res = new Response(StatusCodes.CLASS_NAMED_SELF_TYPE,
                    new ArrayList<>(Collections.singletonList(clazz.id.getText())));
            SymbolTable.error(clazz.ctx, clazz.id, res.getMessage());

            return null;
        }

        if(clazz.inheritedType != null &&
                Constants.ILLEGAL_EXTENDS.contains(clazz.inheritedType.getText())) {
            Response res = new Response(StatusCodes.CLASS_WITH_ILLEGAL_PARENT,
                                new ArrayList<>(Arrays.asList(clazz.id.getText(),
                                                            clazz.inheritedType.getText())));
            SymbolTable.error(clazz.ctx, clazz.inheritedType, res.getMessage());

            symbol.setInheritedClassName(Constants.Object);
        }

        if(!globalScope.add(symbol)) {
            Response res = new Response(StatusCodes.CLASS_REDEFINED,
                                    new ArrayList<>(Collections.singletonList(clazz.id.getText())));
            SymbolTable.error(clazz.ctx, clazz.id, res.getMessage());

            return null;
        }

        classId.setSymbol(symbol);
        classId.setScope(globalScope);
        globalScope.add(symbol);

        currentScope = symbol;
        clazz.features.forEach(f -> f.accept(this));
        currentScope = symbol.getParent();

        return null;
    }

    @Override
    public Void visit(AttributeDefinition attributeDefinition) {
        IdSymbol symbol = new IdSymbol(attributeDefinition.id.getText(), attributeDefinition.type.getText());
        ClassSymbol enclosingClass = (ClassSymbol)currentScope;

        if(Constants.SELF.equals(attributeDefinition.id.getText())) {
            Response res = new Response(StatusCodes.ATTRIBUTE_CALLED_SELF,
                    new ArrayList<>(Collections.singletonList(enclosingClass.getName())));
            SymbolTable.error(attributeDefinition.ctx, attributeDefinition.id, res.getMessage());

            return null;
        }

        if(!((ClassSymbol)currentScope).addAttribute(symbol)) {
            Response res = new Response(StatusCodes.ATTRIBUTE_REDEFINED,
                    new ArrayList<>(Arrays.asList(enclosingClass.getName(), attributeDefinition.id.getText())));
            SymbolTable.error(attributeDefinition.ctx, attributeDefinition.id, res.getMessage());

            return null;
        }

        attributeDefinition.setIdSymbol(symbol);
        attributeDefinition.setScope(currentScope);

        if (attributeDefinition.initValue != null)
            attributeDefinition.initValue.accept(this);

        currentScope.add(symbol);

        return null;
    }

    @Override
    public Void visit(FunctionDefinition functionDefinition) {
        Map<String, String> arguments = new LinkedHashMap<>();
        functionDefinition.formalArguments.forEach(a -> arguments.put(a.id.getText(), a.type.getText()));
        FunctionSymbol symbol = new FunctionSymbol(functionDefinition.id.getText(),
                                    (ClassSymbol) currentScope, functionDefinition.type.getText(),
                                    arguments);
        ClassSymbol enclosingClass = (ClassSymbol)currentScope;

        if(!enclosingClass.addMethod(symbol)) {
            Response res = new Response(StatusCodes.FUNCTION_REDEFINED,
                                new ArrayList<>(Arrays.asList(enclosingClass.getName(),
                                                functionDefinition.id.getText())));
            SymbolTable.error(functionDefinition.ctx, functionDefinition.id, res.getMessage());

            return null;
        }

        functionDefinition.setFunctionSymbol(symbol);
        functionDefinition.setScope(currentScope);
        currentScope = symbol;

        for (var formal: functionDefinition.formalArguments) {

            if(Constants.SELF.equals(formal.id.getText())) {
                Response res = new Response(StatusCodes.FORMAL_PARAM_CALLED_SELF,
                                new ArrayList<>(Arrays.asList(functionDefinition.id.getText(),
                                        enclosingClass.getName())));
                SymbolTable.error(formal.ctx, formal.id, res.getMessage());

                continue;
            }

            if(Constants.SELF_TYPE.equals(formal.type.getText())) {
                Response res = new Response(StatusCodes.FORMAL_PARAM_TYPE_SELF_TYPE,
                        new ArrayList<>(Arrays.asList(functionDefinition.id.getText(),
                                enclosingClass.getName(), formal.id.getText())));
                SymbolTable.error(formal.ctx, formal.type, res.getMessage());

                continue;
            }

            formal.accept(this);
        }
        functionDefinition.body.accept(this);

        currentScope = currentScope.getParent();
        return null;
    }

    @Override
    public Void visit(FormalDefinition formal) {
        IdSymbol symbol = new IdSymbol(formal.id.getText(), formal.type.getText());
        FunctionSymbol enclosingFunction = (FunctionSymbol)currentScope;

        if(!enclosingFunction.add(symbol)) {
            Response res = new Response(StatusCodes.FORMAL_PARAM_REDEFINED,
                    new ArrayList<>(Arrays.asList(enclosingFunction.getName(),
                            ((ClassSymbol)enclosingFunction.getParent()).getName(), formal.id.getText())));
            SymbolTable.error(formal.ctx, formal.id, res.getMessage());

            return null;
        }


        formal.setSymbol(symbol);
        formal.setScope(enclosingFunction);

        return null;
    }

    @Override
    public Void visit(IsVoid isVoid) {

        isVoid.expression.accept(this);

        return null;
    }

    @Override
    public Void visit(NewExpression newExpression) {
        return null;
    }

    @Override
    public Void visit(Id id) {
        return null;
    }

    @Override
    public Void visit(Assign assign) {

        if(Constants.SELF.equals(assign.left.getText())) {
            Response res = new Response(StatusCodes.ASSIGN_TO_SELF,
                    new ArrayList<>(Arrays.asList()));
            SymbolTable.error(assign.ctx, assign.left, res.getMessage());

            return null;
        }

        assign.right.accept(this);

        return null;
    }

    @Override
    public Void visit(Dispatch dispatch) {

        dispatch.actualParams.forEach(a -> a.accept(this));

        return null;
    }

    @Override
    public Void visit(Int intt) {
        return null;
    }

    @Override
    public Void visit(Bool bool) {
        return null;
    }

    @Override
    public Void visit(Stringg stringg) {
        return null;
    }

    @Override
    public Void visit(ArithmeticOperation operation) {
        return null;
    }

    @Override
    public Void visit(TildeOperation tildeOperation) {
        return null;
    }

    @Override
    public Void visit(ParenthesisOperation operation) {
        return null;
    }

    @Override
    public Void visit(RelationalOperation relationalOperation) {
        return null;
    }

    @Override
    public Void visit(Not not) {
        return null;
    }

    @Override
    public Void visit(Iff iff) {
        iff.cond.accept(this);
        iff.then.accept(this);
        iff.elsee.accept(this);

        return null;
    }

    @Override
    public Void visit(While whilee) {
        whilee.cond.accept(this);
        whilee.body.accept(this);

        return null;
    }

    @Override
    public Void visit(Let let) {
        Scope startLet = currentScope;

        let.items.forEach(i -> i.accept(this));
        let.body.accept(this);

        currentScope = startLet;

        return null;
    }

    @Override
    public Void visit(LetItem letItem) {
        IdSymbol symbol = new IdSymbol(letItem.id.getText(), letItem.type.getText());
        Scope scope = new DefaultScope(currentScope);

        if(Constants.SELF.equals(letItem.id.getText())) {
            Response res = new Response(StatusCodes.LET_VARIABLE_NAMED_SELF,
                    new ArrayList<>(Arrays.asList()));
            SymbolTable.error(letItem.ctx, letItem.id, res.getMessage());

            return null;
        }

        if (letItem.initValue != null)
            letItem.initValue.accept(this);

        currentScope = scope;
        currentScope.add(symbol);

        letItem.setSymbol(symbol);
        letItem.setScope(scope);
        return null;
    }

    @Override
    public Void visit(Case casee) {
        casee.cond.accept(this);
        casee.items.forEach(i -> i.accept(this));

        return null;
    }

    @Override
    public Void visit(CaseItem caseItem) {
        IdSymbol symbol = new IdSymbol(caseItem.id.getText(), caseItem.type.getText());
        Scope scope = new DefaultScope(currentScope);

        if(Constants.SELF.equals(caseItem.id.getText())) {
            Response res = new Response(StatusCodes.CASE_VARIABLE_NAMED_SELF,
                    new ArrayList<>(Arrays.asList()));
            SymbolTable.error(caseItem.ctx, caseItem.id, res.getMessage());

            return null;
        }

        if(Constants.SELF_TYPE.equals(caseItem.type.getText())) {
            Response res = new Response(StatusCodes.CASE_VARIABLE_TYPE_SELF_TYPE,
                    new ArrayList<>(Arrays.asList(caseItem.id.getText())));
            SymbolTable.error(caseItem.ctx, caseItem.type, res.getMessage());

            return null;
        }

        caseItem.setScope(scope);
        caseItem.setSymbol(symbol);

        currentScope = scope;
        currentScope.add(symbol);
        caseItem.body.accept(this);
        currentScope = currentScope.getParent();

        return null;
    }

    @Override
    public Void visit(Block block) {
        block.expressions.forEach(e -> e.accept(this));

        return null;
    }
}
