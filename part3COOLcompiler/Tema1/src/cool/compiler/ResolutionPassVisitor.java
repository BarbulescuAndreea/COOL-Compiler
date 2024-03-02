package cool.compiler;

import cool.structures.*;

import java.lang.reflect.Type;
import java.util.*;

public class ResolutionPassVisitor implements ASTVisitor<ClassSymbol> {
    Scope currentScope = SymbolTable.globals;
    Scope globalScope = SymbolTable.globals;

    boolean checkTypeCompatibility(ClassSymbol left, ClassSymbol right) {
        if(left == null || right == null)
            return false;

        if(left.isSelfType() && !right.isSelfType())
            return false;

        if(right.isSelfType()) {
            right = ((SelfTypeSymbol) right).getActualClass();
        }

        if(left.isSelfType()) {
            left = ((SelfTypeSymbol) left).getActualClass();
        }

        // No SELF_TYPE here
        return right.checkForParent(left);
    }

    ClassSymbol findCommonAncestor(ClassSymbol left, ClassSymbol right) {

        // No SELF_TYPE here
        if(left == null || right == null)
            return null;

        if(left instanceof SelfTypeSymbol && !(right instanceof SelfTypeSymbol)) {
            left = ((SelfTypeSymbol)left).getActualClass();
        }

        if(right instanceof SelfTypeSymbol && !(left instanceof SelfTypeSymbol)) {
            right = ((SelfTypeSymbol)right).getActualClass();
        }

        if(left instanceof SelfTypeSymbol && right instanceof SelfTypeSymbol) {
            return new SelfTypeSymbol(globalScope, findCommonAncestor(((SelfTypeSymbol)left).getActualClass(),
                                                                    ((SelfTypeSymbol)right).getActualClass()));
        }

        ClassSymbol leftParent = left;
        while (leftParent != null) {
            if(right.checkForParent(leftParent)) {
                return leftParent;
            }

            leftParent = leftParent.getInheritedClassSymbol();
        }

        return (ClassSymbol) currentScope.lookupForType(Constants.Object, globalScope, findEnclosingClass().getName());
    }

    ClassSymbol findEnclosingClass() {
        Scope crt = currentScope;

        while (!(crt instanceof ClassSymbol)) {
            crt = crt.getParent();
        }
        return (ClassSymbol) crt;
    }

    ClassSymbol checkArrayTypeCompatibility(List<ClassSymbol> types) {
        types.stream().filter(Objects::nonNull);
        if(types.isEmpty())
            return null;

        ClassSymbol res = types.get(0);
        for(ClassSymbol crt: types) {
            res = findCommonAncestor(res, crt);
        }

        return res;
    }


    @Override
    public ClassSymbol visit(Program program) {
        for (var clazz: program.classes) {
            clazz.accept(this);
        }

        return null;
    }

    @Override
    public ClassSymbol visit(Clazz clazz) {
        var classId = clazz.classId;
        ClassSymbol symbol = (ClassSymbol) classId.getSymbol();

        if(symbol == null)
            return null;

        if ( clazz.inheritedType != null) {
            Response res = symbol.checkInheritence(classId.token.getText(), new ArrayList<>());
            if(res.getCode() != StatusCodes.OK){
                if(res.getCode() == StatusCodes.CLASS_CYCLIC_INHERITANCE) {
                    SymbolTable.error(clazz.ctx, clazz.id, res.getMessage());
                } else {
                    SymbolTable.error(clazz.ctx, clazz.inheritedType, res.getMessage());
                }
            }
        }

        currentScope = symbol;
        clazz.features.forEach(f -> f.accept(this));
        currentScope = symbol.getParent();

        return symbol;
    }

    @Override
    public ClassSymbol visit(AttributeDefinition attributeDefinition) {
        var idSymbol = attributeDefinition.getIdSymbol();
        ClassSymbol enclosingClass = (ClassSymbol) currentScope;

        if(idSymbol == null)
            return null;

        Symbol attrDuplicate = enclosingClass.lookupForInheritedAttribute(attributeDefinition.id.getText());
        if(attrDuplicate != null) {
            Response res = new Response(StatusCodes.ATTRIBUTE_REDEFINES_INHERITED,
                                new ArrayList<>(Arrays.asList( enclosingClass.getName(),
                                            attributeDefinition.id.getText())));
            SymbolTable.error(attributeDefinition.ctx, attributeDefinition.id, res.getMessage());

            return null;
        }

        ClassSymbol type = (ClassSymbol) globalScope.lookupForType(attributeDefinition.type.getText(),
                                            globalScope, findEnclosingClass().getName());
        if(type == null) {
            Response res = new Response(StatusCodes.ATTRIBUTE_WITH_UNDEFINED_TYPE,
                    new ArrayList<>(Arrays.asList(enclosingClass.getName(),
                            attributeDefinition.id.getText(), attributeDefinition.type.getText())));
            SymbolTable.error(attributeDefinition.ctx, attributeDefinition.type, res.getMessage());

            return null;
        }

        if(attributeDefinition.initValue != null) {
            ClassSymbol initialType = attributeDefinition.initValue.accept(this);
            if(initialType != null && !checkTypeCompatibility(type, initialType)) {
                Response res = new Response(StatusCodes.ATTRIBUTE_AND_INITIALIZATION_DONT_MATCH,
                                                new ArrayList<>(Arrays.asList(initialType.getName(),
                                                        attributeDefinition.id.getText(), type.getName())));
                SymbolTable.error(attributeDefinition.ctx, attributeDefinition.initValue.token, res.getMessage());

                return null;
            }
        }

        attributeDefinition.setTypeSymbol(type);

        return type;
    }

    @Override
    public ClassSymbol visit(FunctionDefinition functionDefinition) {
        var funcSymbol = functionDefinition.getFunctionSymbol();
        ClassSymbol enclosingClass = (ClassSymbol) currentScope;

        if(funcSymbol == null)
            return null;

        ClassSymbol type = (ClassSymbol)globalScope.lookupForType(functionDefinition.type.getText(),
                                globalScope,
                                findEnclosingClass().getName());
        if(type == null) {
            Response res = new Response(StatusCodes.FUNCTION_WITH_UNDEFINED_RETURN_TYPE,
                    new ArrayList<>(Arrays.asList(enclosingClass.getName(),
                            enclosingClass.getName(), functionDefinition.id.getText(),
                            functionDefinition.type.getText())));
            SymbolTable.error(functionDefinition.ctx, functionDefinition.type, res.getMessage());

            return null;
        }

        FunctionSymbol overrideFunc = (FunctionSymbol) enclosingClass.lookupForInheritedMethod(functionDefinition.id.getText());
        if(overrideFunc != null) {
            if(overrideFunc.getArgumentsSize() != funcSymbol.getArgumentsSize()) {
                Response res = new Response(StatusCodes.FUNCTION_OVERRIDE_DIFFERENT_NR_OF_PARAMS,
                                    new ArrayList<>(Arrays.asList(enclosingClass.getName(),
                                                                    functionDefinition.id.getText())));
                SymbolTable.error(functionDefinition.ctx, functionDefinition.id, res.getMessage());

                return null;
            }

            Set<Map.Entry<String, String>> overrideArgs = overrideFunc.getArguments().entrySet();
            Iterator<Map.Entry<String, String>> overrideArgsIterator = overrideArgs.iterator();

            Set<Map.Entry<String, String>> crtArgs = funcSymbol.getArguments().entrySet();

            int idx = 0;
            for(var arg: crtArgs) {
                Map.Entry<String, String> overrideArg = overrideArgsIterator.next();
                if(!arg.getValue().equals(overrideArg.getValue())) {
                    Response res = new Response(StatusCodes.FUNCTION_OVERRIDE_CHANGE_FORMAL_TYPE,
                                        new ArrayList<>(Arrays.asList(enclosingClass.getName(),
                                                            functionDefinition.id.getText(),
                                                            arg.getKey(), overrideArg.getValue(), arg.getValue())));
                    SymbolTable.error(functionDefinition.ctx,
                                        functionDefinition.formalArguments.get(idx).type,
                                        res.getMessage());

                    return null;
                }
                idx++;
            }

            if(!type.getName().equals(overrideFunc.getReturnType())) {
                Response res = new Response(StatusCodes.FUNCTION_OVERRIDE_CHANGE_RETURN_TYPE,
                                            new ArrayList<>(Arrays.asList(enclosingClass.getName(),
                                                    functionDefinition.id.getText(),
                                                    overrideFunc.getReturnType(),
                                                    type.getName())));
                SymbolTable.error(functionDefinition.ctx, functionDefinition.type, res.getMessage());

                return null;
            }
        }

        currentScope = funcSymbol;

        for(var formal: functionDefinition.formalArguments) {
            formal.accept(this);
        }
        ClassSymbol bodyType = functionDefinition.body.accept(this);

        currentScope = currentScope.getParent();
        if(bodyType != null && !checkTypeCompatibility(type, bodyType)) {
            Response res = new Response(StatusCodes.FUNCTION_RETURN_AND_BODY_DONT_MATCH,
                                new ArrayList<>(Arrays.asList(bodyType.getName(),
                                                                functionDefinition.id.getText(),
                                                                type.getName())));
            SymbolTable.error(functionDefinition.ctx, functionDefinition.body.token, res.getMessage());

            return null;
        }

        return type;
    }

    @Override
    public ClassSymbol visit(FormalDefinition formal) {
        IdSymbol symbol = formal.getSymbol();
        FunctionSymbol enclosingFunction = (FunctionSymbol)currentScope;

        if(symbol == null)
            return null;

        ClassSymbol type = (ClassSymbol) enclosingFunction.lookupForType(formal.type.getText(), globalScope, findEnclosingClass().getName());
        if(type == null) {
            Response res = new Response(StatusCodes.FORMAL_PARAM_UNDEFINED_TYPE,
                                new ArrayList<>(Arrays.asList(enclosingFunction.getName(),
                                                                ((ClassSymbol)enclosingFunction.getParent()).getName(),
                                                                formal.id.getText(),
                                                                formal.type.getText())));
            SymbolTable.error(formal.ctx, formal.type, res.getMessage());

            return null;
        }
        symbol.setType(type);

        return type;
    }

    @Override
    public ClassSymbol visit(IsVoid isVoid) {
        isVoid.expression.accept(this);
        return (ClassSymbol) currentScope.lookupForType(Constants.BOOL, globalScope,
                        findEnclosingClass().getName());
    }

    @Override
    public ClassSymbol visit(NewExpression newExpression) {
        ClassSymbol type = (ClassSymbol) currentScope.lookupForType(newExpression.type.getText(), globalScope,
                    findEnclosingClass().getName());

        if(type == null) {
            Response res = new Response(StatusCodes.NEW_UNDEFINED_TYPE,
                                        new ArrayList<>(Arrays.asList(newExpression.type.getText())));
            SymbolTable.error(newExpression.ctx, newExpression.type, res.getMessage());

            return null;
        }

        return type;
    }

    @Override
    public ClassSymbol visit(Id id) {
        IdSymbol symbol = (IdSymbol) currentScope.lookupForVariable(id.token.getText());

        if(symbol == null) {
            Response res = new Response(StatusCodes.UNDEFINED_IDENTIFIER,
                    new ArrayList<>(Arrays.asList(id.token.getText())));
            SymbolTable.error(id.ctx, id.token, res.getMessage());
        }

        id.setSymbol(symbol);
        if(symbol == null)
            return null;

        ClassSymbol typeSymbol = (ClassSymbol) currentScope.lookupForType(symbol.getTypeName(), globalScope,
                findEnclosingClass().getName());

        return typeSymbol;
    }

    @Override
    public ClassSymbol visit(Assign assign) {
        IdSymbol symbol = (IdSymbol) currentScope.lookupForVariable(assign.token.getText());

        if(symbol == null)
            return null;

        ClassSymbol type = (ClassSymbol) currentScope.lookupForType(symbol.getTypeName(), globalScope,
                findEnclosingClass().getName());
        ClassSymbol rightType = assign.right.accept(this);

        if(rightType != null && !checkTypeCompatibility(type, rightType)) {
            Response res = new Response(StatusCodes.ASSIGN_ASSIGNED_AND_DECLARED_TYPES_DONT_MATCH,
                                        new ArrayList<>(Arrays.asList(rightType.getName(),
                                                type.getName(), assign.left.getText())));
            SymbolTable.error(assign.ctx, assign.right.token, res.getMessage());

            return null;
        }

        return null;
    }

    @Override
    public ClassSymbol visit(Dispatch dispatch) {

        ClassSymbol clazzDispacher = null;

        if(dispatch.dispatcher == null || Constants.SELF.equals(dispatch.dispatcher.token.getText())) {
            clazzDispacher = findEnclosingClass();
        } else {
            if (dispatch.dispatcher instanceof Dispatch || (dispatch.dispatcher instanceof NewExpression))
                clazzDispacher = dispatch.dispatcher.accept(this);
            else{
                IdSymbol variable = (IdSymbol) currentScope.lookupForVariable(dispatch.dispatcher.token.getText());
                if (variable == null)
                    return null;

                clazzDispacher = (ClassSymbol) currentScope.lookupForType(variable.getTypeName(), globalScope,
                    findEnclosingClass().getName());

            }

        }

        if (clazzDispacher == null)
            return null;

        ClassSymbol subclass = null;
        if(dispatch.subclass != null) {
            if(Constants.SELF_TYPE.equals(dispatch.subclass.getText())) {
                Response res = new Response(StatusCodes.DISPATCH_ON_SELF_TYPE,
                        new ArrayList<>());
                SymbolTable.error(dispatch.ctx, dispatch.subclass, res.getMessage());

                return null;
            }

            subclass = (ClassSymbol) currentScope.lookupForType(dispatch.subclass.getText(), globalScope,
                                                findEnclosingClass().getName());
            if(subclass == null) {
                Response res = new Response(StatusCodes.DISPATCH_ON_UNDEFINED_TYPE,
                        new ArrayList<>(Arrays.asList(dispatch.subclass.getText())));
                SymbolTable.error(dispatch.ctx, dispatch.subclass, res.getMessage());

                return null;
            }
        } else {
            subclass = clazzDispacher;
        }

        if (!clazzDispacher.checkForParent(subclass)) {
            Response res = new Response(StatusCodes.DISPATCH_ON_UNRELATED_CLASS,
                    new ArrayList<>(Arrays.asList(dispatch.subclass.getText(),
                            clazzDispacher.getName())));
            SymbolTable.error(dispatch.ctx, dispatch.subclass, res.getMessage());

            return null;
        }

        if (subclass instanceof SelfTypeSymbol) {
            subclass = ((SelfTypeSymbol)subclass).getActualClass();
        }

        FunctionSymbol functionSymbol = (FunctionSymbol) currentScope.lookupForMethod(dispatch.function.getText(), subclass.getName(), globalScope);
        if(functionSymbol == null) {
            Response res = new Response(StatusCodes.DISPATCH_METHOD_UNDEFINED,
                    new ArrayList<>(Arrays.asList(dispatch.function.getText(),
                                subclass.getName())));
            SymbolTable.error(dispatch.ctx, dispatch.function, res.getMessage());

            return null;
        }

        if(functionSymbol.getArgumentsSize() != dispatch.actualParams.size()) {
            Response res = new Response(StatusCodes.DISPATCH_WRONG_NUMBER_OF_ARGUMENTS,
                    new ArrayList<>(Arrays.asList(dispatch.function.getText(),
                            subclass.getName())));
            SymbolTable.error(dispatch.ctx, dispatch.function, res.getMessage());

            return null;
        }

        int idx = 0;
        Set<Map.Entry<String, String>> formalParams = functionSymbol.getArguments().entrySet();
        for (Map.Entry<String, String> formalArg: formalParams) {
            ClassSymbol formalArgClass = (ClassSymbol) currentScope.lookupForType(formalArg.getValue(),
                                            currentScope, findEnclosingClass().getName());
            ClassSymbol actualArgClass = dispatch.actualParams.get(idx).accept(this);

            if(!checkTypeCompatibility(formalArgClass, actualArgClass)) {
                Response res = new Response(StatusCodes.DISPATCH_ACTUAL_PARAM_WRONG_TYPE,
                        new ArrayList<>(Arrays.asList(dispatch.function.getText(),
                                subclass.getName(),
                                actualArgClass.getName(),
                                formalArg.getKey(), formalArg.getValue())));
                SymbolTable.error(dispatch.ctx, dispatch.actualParams.get(idx).token, res.getMessage());
            }

            idx++;
        }

        ClassSymbol returnType = null ;
        if (Constants.SELF_TYPE.equals(functionSymbol.getReturnType())) {
            returnType = new SelfTypeSymbol(globalScope, clazzDispacher);
        } else {
            returnType = (ClassSymbol) currentScope.lookupForType(functionSymbol.getReturnType(), globalScope,
                                                    findEnclosingClass().getName());
        }

        if (returnType instanceof SelfTypeSymbol && dispatch.dispatcher != null
                        && !Constants.SELF.equals(dispatch.dispatcher.token.getText()))
            return ((SelfTypeSymbol)returnType).getActualClass();
        return returnType;
    }

    @Override
    public ClassSymbol visit(Int intt) {
        return (ClassSymbol) currentScope.lookupForType(Constants.INT, globalScope, findEnclosingClass().getName());
    }

    @Override
    public ClassSymbol visit(Bool bool) {
        return (ClassSymbol) currentScope.lookupForType(Constants.BOOL, globalScope, findEnclosingClass().getName());
    }

    @Override
    public ClassSymbol visit(Stringg stringg) {
        return (ClassSymbol) currentScope.lookupForType(Constants.STRING, globalScope, findEnclosingClass().getName());
    }

    @Override
    public ClassSymbol visit(ArithmeticOperation operation) {

        ClassSymbol leftType = operation.left.accept(this);
        ClassSymbol rightType = operation.right.accept(this);

        if(leftType != null && !Constants.INT.equals(leftType.getName())) {
            Response res = new Response(StatusCodes.OPERATOR_OPERANDS_ARENT_INT,
                    new ArrayList<>(Arrays.asList(operation.operation.getText(), leftType.getName())));
            SymbolTable.error(operation.ctx, operation.left.token, res.getMessage());

            return null;
        }

        if(rightType != null && !Constants.INT.equals(rightType.getName())) {
            Response res = new Response(StatusCodes.OPERATOR_OPERANDS_ARENT_INT,
                    new ArrayList<>(Arrays.asList(operation.operation.getText(), rightType.getName())));
            SymbolTable.error(operation.ctx, operation.right.token, res.getMessage());

            return null;
        }

        if(leftType == null || rightType == null)
            return null;

        return (ClassSymbol) currentScope.lookupForType(Constants.INT, globalScope, findEnclosingClass().getName());
    }

    @Override
    public ClassSymbol visit(TildeOperation tildeOperation) {
        ClassSymbol type = tildeOperation.expression.accept(this);

        if(type == null)
            return null;

        if(!Constants.INT.equals(type.getName())) {
            Response res = new Response(StatusCodes.OPERATOR_OPERANDS_ARENT_INT,
                                        new ArrayList<>(Arrays.asList(Constants.TILDE,
                                                type.getName())));
            SymbolTable.error(tildeOperation.ctx, tildeOperation.expression.token, res.getMessage());

            return null;
        }

        return (ClassSymbol) currentScope.lookupForType(Constants.BOOL, globalScope, findEnclosingClass().getName());
    }

    @Override
    public ClassSymbol visit(ParenthesisOperation operation) {
        return operation.expression.accept(this);
    }

    @Override
    public ClassSymbol visit(RelationalOperation relationalOperation) {

        ClassSymbol leftType = relationalOperation.left.accept(this);
        ClassSymbol rightType = relationalOperation.right.accept(this);

        if (!relationalOperation.relation.getText().equals("=")) {    // < <=
            if(!Constants.INT.equals(leftType.getName())) {
                Response res = new Response(StatusCodes.OPERATOR_OPERANDS_ARENT_INT,
                        new ArrayList<>(Arrays.asList(relationalOperation.relation.getText(), leftType.getName())));
                SymbolTable.error(relationalOperation.ctx, relationalOperation.left.token, res.getMessage());

                return null;
            }

            if(!Constants.INT.equals(rightType.getName())) {
                Response res = new Response(StatusCodes.OPERATOR_OPERANDS_ARENT_INT,
                        new ArrayList<>(Arrays.asList(relationalOperation.relation.getText(), rightType.getName())));
                SymbolTable.error(relationalOperation.ctx, relationalOperation.right.token, res.getMessage());

                return null;
            }
        } else {        // =
            if(leftType != null || rightType != null) {
                if((Constants.COMPARABLE.contains(leftType.getName())
                        || Constants.COMPARABLE.contains(rightType.getName())) &&
                            !leftType.getName().equals(rightType.getName())) {
                    Response res = new Response(StatusCodes.OPERATOR_CANT_COMPARE,
                            new ArrayList<>(Arrays.asList(leftType.getName(), rightType.getName())));
                    SymbolTable.error(relationalOperation.ctx, relationalOperation.relation, res.getMessage());

                    return null;
                }
            } else {
                return null;
            }
        }

        return (ClassSymbol) currentScope.lookupForType(Constants.BOOL, globalScope, findEnclosingClass().getName());
    }

    @Override
    public ClassSymbol visit(Not not) {
        ClassSymbol type = not.expression.accept(this);

        if(type == null)
            return null;

        if(!Constants.BOOL.equals(type.getName())) {
            Response res = new Response(StatusCodes.OPERATOR_OPERAND_ISNT_BOOL,
                    new ArrayList<>(Arrays.asList(type.getName())));
            SymbolTable.error(not.ctx, not.expression.token, res.getMessage());
        }

        return null;
    }

    @Override
    public ClassSymbol visit(Iff iff) {

        ClassSymbol type = iff.cond.accept(this);

        if(type == null)
            return null;

        if(!Constants.BOOL.equals(type.getName())) {
            Response res = new Response(StatusCodes.IF_COND_NOT_BOOL,
                    new ArrayList<>(Arrays.asList(type.getName())));
            SymbolTable.error(iff.ctx, iff.cond.token, res.getMessage());
        }

        ClassSymbol thenType = iff.then.accept(this);
        ClassSymbol elseeType = iff.elsee.accept(this);

        return findCommonAncestor(thenType, elseeType);
    }

    @Override
    public ClassSymbol visit(While whilee) {

        ClassSymbol type = whilee.cond.accept(this);

        if(type == null)
            return null;

        if(!Constants.BOOL.equals(type.getName())) {
            Response res = new Response(StatusCodes.WHILE_COND_NOT_BOOL,
                    new ArrayList<>(Arrays.asList(type.getName())));
            SymbolTable.error(whilee.ctx, whilee.cond.token, res.getMessage());
        }

        whilee.body.accept(this);

        return (ClassSymbol) currentScope.lookupForType(Constants.Object, globalScope, findEnclosingClass().getName());
    }

    @Override
    public ClassSymbol visit(Let let) {
        Scope startLet = currentScope;

        let.items.forEach(i -> i.accept(this));
        ClassSymbol res = let.body.accept(this);

        currentScope = startLet;
        return res;
    }

    @Override
    public ClassSymbol visit(LetItem letItem) {
        if(letItem.getScope() == null)
            return null;

        ClassSymbol type = (ClassSymbol) currentScope.lookupForType(letItem.type.getText(), globalScope, findEnclosingClass().getName());

        if(type == null) {
            Response res = new Response(StatusCodes.LET_VARIABLES_UNDEFINED_TYPE,
                                        new ArrayList<>(Arrays.asList(letItem.id.getText(),
                                                           letItem.type.getText())));
            SymbolTable.error(letItem.ctx, letItem.type, res.getMessage());

            return null;
        }

        if(letItem.initValue != null) {
            ClassSymbol initialType = letItem.initValue.accept(this);

            if(initialType != null && !checkTypeCompatibility(type, initialType)) {
                Response res = new Response(StatusCodes.LET_INITIALIZATION_AND_TYPE_DONT_MATCH,
                                            new ArrayList<>(Arrays.asList(initialType.getName(),
                                                            letItem.id.getText(),
                                                            type.getName())));
                SymbolTable.error(letItem.ctx, letItem.initValue.token, res.getMessage());

                return null;
            }
        }

        currentScope = letItem.getScope();
        return null;
    }
    @Override
    public ClassSymbol visit(Case casee) {
        casee.cond.accept(this);

        List<ClassSymbol> branchTypes = new ArrayList<>();
        casee.items.forEach(i -> branchTypes.add(i.accept(this)));

        return checkArrayTypeCompatibility(branchTypes);
    }

    @Override
    public ClassSymbol visit(CaseItem caseItem) {
        ClassSymbol type = (ClassSymbol) currentScope.lookupForType(caseItem.type.getText(), globalScope, findEnclosingClass().getName());

        Scope scope = caseItem.getScope();
        Symbol symbol = caseItem.getSymbol();

        if(symbol == null)
            return null;

        if(type == null && !Constants.SELF_TYPE.equals(caseItem.type.getText())) {
            Response res = new Response(StatusCodes.CASE_VARIABLE_UNDEFINED_TYPE,
                    new ArrayList<>(Arrays.asList(caseItem.id.getText(), caseItem.type.getText())));
            SymbolTable.error(caseItem.ctx, caseItem.type, res.getMessage());

            return null;
        }

        currentScope = scope;
        ClassSymbol bodyType = caseItem.body.accept(this);
        currentScope = currentScope.getParent();

        return bodyType;
    }

    @Override
    public ClassSymbol visit(Block block) {
        ClassSymbol blockType = null;

        for(Expression e: block.expressions)
            blockType = e.accept(this);

        return blockType;
    }
}
