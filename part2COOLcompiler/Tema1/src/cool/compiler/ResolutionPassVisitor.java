package cool.compiler;
import java.util.*;
import java.util.stream.Collectors;
import cool.structures.Scope;
import cool.structures.Symbol;
import cool.structures.SymbolTable;
import cool.structures.scope.*;
import cool.structures.symbol.*;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.ParserRuleContext;

public class ResolutionPassVisitor implements ASTVisitor<Symbol>{
    Scope currentScope = null;

    private boolean areOperandsNull(Symbol leftOperand, Symbol rightOperand) {
        if (leftOperand == null || rightOperand == null) {
            handleError("One of the operands is null.");
            return true;
        }
        return false;
    }
    private void handleError(String errorMessage) {
        System.err.println(errorMessage);
    }
    @Override
    public Symbol visit(Arithmetic arithmetic) {
        Symbol rightOp = arithmetic.getRightEx().accept(this);

        Symbol leftOp = arithmetic.getLeftEx().accept(this);
        if (areOperandsNull(leftOp, rightOp)) {
            return null;
        }

        if (!leftOp.equals(DeclarationScopes.intClassScope)) {
            handleOperandTypeError(arithmetic, leftOp, DeclarationScopes.intClassScope, arithmetic.getContext().start);
        }

        if (!rightOp.equals(DeclarationScopes.intClassScope)) {
            handleOperandTypeError(arithmetic, rightOp, DeclarationScopes.intClassScope, arithmetic.getContext().stop);
        }
        return DeclarationScopes.intClassScope;
    }
    // afiseaza eroarea daca tipul de operand nu e ok (ambii operanzi de tipul int)
    private void handleOperandTypeError(Arithmetic arithmetic, Symbol operandSymbol, Symbol expectedType, Token errorToken) {
        String errorMessage = String.format("Operand of %s has type %s instead of %s",
                arithmetic.getOperation().getText(),
                operandSymbol.getName(),
                expectedType.getName());
        SymbolTable.error(arithmetic.getContext(), errorToken, errorMessage);
    }


    @Override
    public Symbol visit(Assignment assign) {
        if (Objects.equals(Declarations.self, assign.getId().getText())) {
            return DeclarationScopes.boolClassScope;
        }
        Symbol attrib = assign.getExpression().accept(this);
        if(attrib == null){
            return null;
        }
        ClassSymbolScope typeass = ((IdSymbol) currentScope.lookup(assign.getId().getText())).getScope();
        String parent = getInheritedChildName(attrib);
        // ex: A inherits B, ob de tip B = new A, dar nu pot sa asignez A = new B
        while (parent != null && !parent.equals(typeass.getName())) {
            Symbol inherited = Declarations.globals.lookup(parent);

            if (inherited instanceof ClassSymbolScope) {
                parent = ((ClassSymbolScope) inherited).getChildName();
            } else {
                break;
            }
        }

        if (parent != null) {
            return attrib;
        }
        // daca tipul expresiei de assign e diferit de un tip parinte
        if (!attrib.equals(typeass)) {
            reportTypeError(assign.getContext(), assign.getFrom(), attrib, typeass, assign.getId().getText());
        }
        return attrib;
    }
    private String getInheritedChildName(Symbol symbol) {
        if (symbol instanceof ClassSymbolScope) {
            return ((ClassSymbolScope) symbol).getChildName();
        }
        return null;
    }
    private void reportTypeError(ParserRuleContext context, Token location, Symbol assignedType, Symbol declaredType, String identifierName) {
        String errno = String.format("Type %s of assigned expression is incompatible with declared type %s of identifier %s",
                assignedType.getName(), declaredType.getName(), identifierName);

        SymbolTable.error(context, location, errno);
    }

    @Override
    public Symbol visit(Block block) {
        List<Symbol> symbolList = block.getExpression().stream()
                .map(expressionNode -> expressionNode.accept(this))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return symbolList.isEmpty() ? null : symbolList.get(0);
    }

    @Override
    public Symbol visit(CaseLines caseLines) {
        // luat toate case-urile din block ul de case => accept
        // return commontype dintre expresiile de case - ex: case { OBJECT x .. , Int y, String f = > return Object
        List<Symbol> caseSymbolList = caseLines.getCases().stream()
                .map(expr -> expr.accept(this))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (caseSymbolList.isEmpty()) {
            return null;
        }

        return (caseSymbolList.size() == 1)
                ? caseSymbolList.get(0)
                : findCommonInherit(caseSymbolList);
    }

    private ClassSymbolScope findCommonInherit(List<Symbol> caseSymbolList) {
        Optional<ClassSymbolScope> commonInherit = caseSymbolList.stream()
                .map(symbol -> (ClassSymbolScope) symbol)
                .reduce(this::findCommonInherit);

        return commonInherit.orElse(DeclarationScopes.objectScope);
    }

    private ClassSymbolScope findCommonInherit(ClassSymbolScope first, ClassSymbolScope second) {
        // ia 2 case-branch uri(2 expr de case) si verifica care e tipul comun
        // daca nu se gas tip comun pana la object, return object
        while (first != null) {
            ClassSymbolScope temp = second;
            while (temp != null) {
                if (first.equals(temp)) {
                    return first;
                }
                temp = (ClassSymbolScope) temp.getParent();
            }
            first = (ClassSymbolScope) first.getParent();
        }
        return DeclarationScopes.objectScope;
    }

    @Override
    public Symbol visit(CaseExpressions exprCasesParam) {
        Symbol caseSymbolType = Declarations.globals.lookup(exprCasesParam.getType().getText());

        if (Declarations.globals.lookup(exprCasesParam.getType().getText()) == null) {
            String errorMessage = String.format("Case variable %s has undefined type %s",
                    exprCasesParam.getId().getText(), exprCasesParam.getType().getText());

            SymbolTable.error(exprCasesParam.getContext(), exprCasesParam.getType(), errorMessage);
            return null;
        }
        // verificare daca tipul caseexpressionului exista in tabela globala de simboluri, setare id si scope
        IdSymbol caseBranchIdSymbol = createCaseBranchIdSymbol(exprCasesParam.getId().getText(), exprCasesParam.getType().getText(), caseSymbolType);
        CaseSymbolScope caseScope = createCaseSymbolScope(caseBranchIdSymbol);
        // accept expresia din case expression
        return processCaseExpression(exprCasesParam.getExpression(), caseScope);
    }

    private IdSymbol createCaseBranchIdSymbol(String id, String type, Symbol symbolCase) {
        return new IdSymbolBuilder()
                .setId(id)
                .setType(type)
                .setScope((ClassSymbolScope) symbolCase)
                .build();
    }

    class IdSymbolBuilder {
        private String id;
        private String type;
        private ClassSymbolScope scope;

        public IdSymbolBuilder setId(String id) {
            this.id = id;
            return this;
        }

        public IdSymbolBuilder setType(String type) {
            this.type = type;
            return this;
        }

        public IdSymbolBuilder setScope(ClassSymbolScope scope) {
            this.scope = scope;
            return this;
        }

        public IdSymbol build() {
            IdSymbol idSymbol = new IdSymbol(id, type);
            idSymbol.setScope(scope);
            return idSymbol;
        }
    }

    private CaseSymbolScope createCaseSymbolScope(IdSymbol branchForCase) {
        return new CaseSymbolScope(branchForCase, currentScope);
    }

    private Symbol processCaseExpression(Expression expression, CaseSymbolScope caseScope) {
        try {
            enterScope(caseScope);
            return expression.accept(this);
        } finally {
            exitScope();
        }
    }

    private void enterScope(Scope newScope) {
        currentScope = newScope;
    }

    private void exitScope() {
        currentScope = currentScope.getParent();
    }

    @Override
    public Symbol visit(Class classCPL) {
        ClassSymbolScope classScope = classCPL.getClassSymbolScope();

        if (classScope == null) {
            return null;
        }

        if (!validateClassInheritance(classCPL)) {
            return null;
        }
        currentScope = classScope;
        classCPL.getFeatures().forEach(featureNode -> featureNode.accept(this));
        return null;
    }

    private boolean validateClassInheritance(Class classNode) {
        Class curr = classNode;
        ClassSymbolScope currentClassScope = curr.getClassSymbolScope();
        Symbol classParent;

        while (currentClassScope != DeclarationScopes.objectScope) {
            if (curr.getMyClassInherit() != null) {
                classParent = Declarations.globals.lookup(curr.getMyClassInherit().getText());
            }else{
                break;
            }

            if (classParent == null) {
                String errorMessage = String.format("Class %s has undefined parent %s",
                        curr.getMyClassName().getText(), curr.getMyClassInherit().getText());

                SymbolTable.error(curr.getContext(), curr.getMyClassInherit(), errorMessage);
                return false;
            }
            curr.getClassSymbolScope().scope = ((ClassSymbolScope) classParent);
            curr = Declarations.globals.getClassByName((classParent).getName());
            if (curr == null) {
                return false;
            }
            if (classNode.getClassSymbolScope() == curr.getClassSymbolScope()) {
                String errorMessage = String.format("Inheritance cycle for class %s",
                        classNode.getMyClassName().getText());

                SymbolTable.error(classNode.getContext(), classNode.getMyClassName(), errorMessage);
                return false;
            }
        }

        return true;
    }

    @Override
    public Symbol visit(Dispatch dispatch) {
        return null;
    }

    @Override
    public Symbol visit(Expression expression) {
        return expression.accept(this);
    }

    @Override
    public Symbol visit(False falseCPL) {
        return DeclarationScopes.boolClassScope;
    }

    //
    @Override
    public Symbol visit(Feature feature) {
        // atribut / method
        // 1. atribut
        if(feature.getUpdatedAttributeContext() == 1){
            if(feature.getIdSymbol() == null){
                return null;
            }
            Token field = feature.getUpdatedFeatureId(), attributeType = feature.getUpdatedFeatureType(); // attribute

            Scope parent = currentScope.getParent();
            Symbol parentSys = parent.lookup(field.getText()), type = Declarations.globals.lookup(attributeType.getText());

            // verifica daca tipul atributului exista in scopul global(parent mare)
            // daca nu exista deloc, e undefined type
            if (type == null) {
                String errorMessage = String.format("Class %s has attribute %s with undefined type %s",
                        ((ClassSymbolScope) currentScope).getName(), field.getText(), attributeType.getText());
                // afisare erori in out
                SymbolTable.error(feature.getContext(), attributeType, errorMessage);
                return null;
            }
            // class A cu atrib x, B inherits A => nu mai poate contine atrib x iar
            if(parentSys != null) {
                String errorMessage = String.format("Class %s redefines inherited attribute %s",
                        ((ClassSymbolScope) currentScope).getName(), field.getText());

                SymbolTable.error(feature.getContext(), field, errorMessage);
                return null;
            }

            Symbol typesymb = Declarations.globals.lookup(attributeType.getText());
            // accepta expresia
            feature.setIdSymbolGeneral(typesymb);
            if(!feature.getUpdatedExpressions().isEmpty()){
                feature.getUpdatedExpressions().stream()
                        .findFirst()
                        .ifPresent(expression -> expression.accept(this));

            }
        }
        // 2. method
        if(feature.getUpdatedMethodContext() == 1){
                    // token - salvare linie si col in care apare eroare
                    Token funcName = feature.getUpdatedFeatureId();
                    Token funcType = feature.getUpdatedFeatureType();
                    MethodSymbolScope methodScope = feature.getMethodScope();
                    // scope ul pentru metodele din clasa
                    if (methodScope == null) {
                        return null;
                    }
                    Symbol returnType = Declarations.globals.lookup(funcType.getText());
                    methodScope.setReturnType(Declarations.globals.lookup(funcType.getText()));

                    Scope initialScope = currentScope;
                    currentScope = methodScope;
                    List<Formal> formalList = feature.getUpdatedFormalList();
                    for (Formal formalNode : formalList) {
                        formalNode.accept(this);
                    }
                    MethodSymbolScope overrideMethod = (MethodSymbolScope) ((ClassSymbolScope) (initialScope.getParent())).lookupMethod(methodScope.getName());
                    // verificare daca metoda la care se face override are acelasi numar de param ca cea din clasa parinte
                    if(overrideMethod != null){
                        if((methodScope.getParams()).size() != (overrideMethod.getParams()).size()){
                            reportDifferentNumberOfParametersError(feature, (Symbol) initialScope, funcName);
                            return null;
                        }
                        // verificare ca metoda la care se face override sa intoarca acelasi tip ca in clasa parinte
                        // si toti parametrii sa fie de acelasi tip
                        for(int i = 0; i < (overrideMethod.getParams()).size(); i++){
                            var param = (new LinkedList<>((methodScope.getParams()).entrySet())).get(i);
                            var wrap = (new LinkedList<>((overrideMethod.getParams()).entrySet())).get(i);

                            ClassSymbolScope parameterScope = ((IdSymbol) param.getValue()).getScope();
                            ClassSymbolScope overriddenParameterScope = ((IdSymbol) wrap.getValue()).getScope();
                            if(!parameterScope.getName().equals(overriddenParameterScope.getName())){
                                String errorMessage = String.format("Class %s overrides method %s but changes type of formal parameter %s from %s to %s",
                                        ((ClassSymbolScope) initialScope).getName(),
                                        feature.getUpdatedFeatureId().getText(),
                                        param.getKey(),
                                        overriddenParameterScope.getName(),
                                        parameterScope.getName());
                                SymbolTable.error(feature.getContext(), feature.getUpdatedFormalList().get(i).getType(), errorMessage);
                                return null;
                            }
                        }
                        if(!(methodScope.getReturnMethod()).getName().equals((overrideMethod.getReturnMethod()).getName())){
                            SymbolTable.error(feature.getContext(), funcType, "Class " + ((ClassSymbolScope) initialScope).getName() + " overrides method " + funcName.getText() + " but changes return type from " + (overrideMethod.getReturnMethod()).getName() + " to " + ( methodScope.getReturnMethod()).getName());
                            return null;
                        }
                    }
                    if(Objects.nonNull(feature.getUpdatedExpressions().get(0))){
                        Symbol insideSys = null;
                        // simbolul returnat de expresie - corpul metodei
                        if (feature.getUpdatedExpressions().get(0) != null) {
                            insideSys = feature.getUpdatedExpressions().get(0).accept(this);
                        }
                        // daca am return un tip si eu dau return la altcv
                        if(insideSys == null){
                            currentScope = initialScope;
                            return null;
                        }

                        if (returnType == insideSys) {
                            currentScope = initialScope;
                            return null;
                        }

                        String inherit = ((ClassSymbolScope)insideSys).getChildName();
                        // verific daca tipul expresiei e acelasi cu tipul declarat in feature
                        while (inherit != null) {
                            if (inherit.equals(returnType.getName())) {
                                resetAndReturnToInitialScope(initialScope);
                                return null;
                            }
                            Symbol inherited = Declarations.globals.lookup(inherit);
                            if (!isInheritedClassSymbolScope(inherited)) {
                                reportIncompatibleReturnType(feature, insideSys, funcName.getText(), returnType);
                                resetAndReturnToInitialScope(initialScope);
                                return null;
                            }
                            inherit = ((ClassSymbolScope) inherited).getChildName();
                        }
                        currentScope = initialScope;
                        return null;
                    }
               }



        return null;
    }

    private boolean isInheritedClassSymbolScope(Symbol inherited) {
        return inherited instanceof ClassSymbolScope;
    }

        private void reportIncompatibleReturnType(Feature feature, Symbol bodySymbol, String methodName, Symbol returnType) {
            String errorMessage = String.format("Type %s of the body of method %s is incompatible with declared return type %s",
                    bodySymbol.getName(), methodName, returnType);

            SymbolTable.error(feature.getContext(), feature.getContext().start, errorMessage);
        }

        private void resetAndReturnToInitialScope(Scope initialScope) {
            currentScope = initialScope;
    }
    private void reportDifferentNumberOfParametersError(Feature feature, Symbol initialScope, Token methodName) {
        String errorMessage = String.format("Class %s overrides method %s with different number of formal parameters",
                (initialScope).getName(),
                methodName.getText());

        SymbolTable.error(feature.getContext(), methodName, errorMessage);
    }

    @Override
    public Symbol visit(Formal formal) {
        if (formal.getIdSymbol() == null) {
            return null;
        }
        Token typee = formal.getType(), formalName = formal.getId();
        Symbol sysTypee = (typee != null) ? Declarations.globals.lookup(typee.getText()) : null;

        if (sysTypee == null) {
            handleUndefinedType(formal, formalName.getText(), typee.getText());
            return null;
        }
        formal.setIdSymbol2(sysTypee);
        return null;
    }

    private void handleUndefinedType(Formal formal, String formalName, String formalType) {
        Scope parent = currentScope.getParent();
        String errorMessage = String.format("Method %s of class %s has formal parameter %s with undefined type %s",
                ((MethodSymbolScope) currentScope).getName(),
                ((ClassSymbolScope) parent).getName(),
                formalName,
                formalType);

        SymbolTable.error(formal.getContext(), formal.getType(), errorMessage);
    }


    @Override
    public Symbol visit(Id id) {
        String txtID = id.getToken().getText();

        if (Declarations.self.equals(txtID)) {
            return null;
        }

        Symbol sysId = currentScope.lookup(txtID);

        return (sysId instanceof IdSymbol) ? ((IdSymbol) sysId).getScope() : null;
    }


    @Override
    public Symbol visit(If ifToken) {
        Symbol ifCond = ifToken.getCond().accept(this);
        Symbol ifThen = ifToken.getThen().accept(this);
        Symbol ifElse = ifToken.getElseCase().accept(this);

        if (!isConditionBoolean(ifCond)) {
            handleIfConditionError(ifToken, ifCond);
        }

        ClassSymbolScope commonAncestor = findCommonAncestor(ifThen, ifElse);

        return commonAncestor != null ? commonAncestor : DeclarationScopes.objectScope;
    }

    private void handleIfConditionError(If ifToken, Symbol conditionSymbol) {
        SymbolTable.error(ifToken.getContext(), ifToken.getToDisplay(),
                String.format("If condition has type %s instead of Bool", conditionSymbol.getName()));
    }

    private ClassSymbolScope findCommonAncestor(Symbol branch1, Symbol branch2) {
        Set<ClassSymbolScope> ancestorSet = new HashSet<>();

        while (branch1 != null) {
            ancestorSet.add((ClassSymbolScope) branch1);
            branch1 = (ClassSymbolScope) ((ClassSymbolScope) branch1).getParent();
        }

        while (branch2 != null) {
            if (ancestorSet.contains(branch2)) {
                return (ClassSymbolScope) branch2;
            }
            branch2 = (ClassSymbolScope) ((ClassSymbolScope) branch2).getParent();
        }

        return null;
    }


    @Override
    public Symbol visit(ImplicitDispatch implicitDispatch) {
        return null;
    }

    @Override
    public Symbol visit(Int intToken) {
        return DeclarationScopes.intClassScope;
    }

    @Override
    public Symbol visit(Let let) {
        try (ScopeSwitcher ignored = new ScopeSwitcher(let.getScope())) {
            let.getLets().forEach(localNode -> localNode.accept(this));
            return let.getExpression().accept(this);
        }
    }

    class ScopeSwitcher implements AutoCloseable {
        private final Scope originalScope;

        public ScopeSwitcher(Scope newScope) {
            this.originalScope = currentScope;
            currentScope = newScope;
        }

        @Override
        public void close() {
            currentScope = originalScope;
        }
    }


    @Override
    public Symbol visit(Locals locals) {
        IdSymbol idLocal = locals.getIdSymbol();
        Token localToken = locals.getId(), localNodeType = locals.getTypeOrId();

        if (idLocal == null) {
            return null;
        }
        Symbol localNodeTypeSymbol = Declarations.globals.lookup(localNodeType.getText());
        idLocal.setScope((ClassSymbolScope) localNodeTypeSymbol);
        if (localNodeTypeSymbol == null) {
            handleUndefinedType(locals, localToken.getText(), localNodeType.getText());
            return null;
        }
        if (locals.getExpression() != null) {
            Symbol expressionSymbol = locals.getExpression().accept(this);

            if (expressionSymbol != null && validateExpressionType(idLocal, localNodeTypeSymbol, expressionSymbol, locals.getContext())) {
                currentScope.add(idLocal);
            }

            return null;
        }
        return null;
    }

    private void handleUndefinedType(Locals locals, String nodeName, String nodeType) {
        String errorMessage = String.format("Let variable %s has undefined type %s", nodeName, nodeType);
        SymbolTable.error(locals.getContext(), locals.getTypeOrId(), errorMessage);
    }


    private boolean validateExpressionType(IdSymbol localIdSymbol, Symbol localNodeTypeSymbol, Symbol expressionSymbol, ParserRuleContext context) {
        if(localNodeTypeSymbol == expressionSymbol) {
            return true;
        }
        String parent = ((ClassSymbolScope) expressionSymbol).getChildName();
        while (parent != null) {
            boolean isEqual = parent.equals(localNodeTypeSymbol.getName());
            if(isEqual){
                return true;
            }
            Symbol inherited = (Declarations.globals.lookup(parent) != null) ? Declarations.globals.lookup(parent) : null;
            if (!(inherited instanceof ClassSymbolScope)) {
                String errorMessage = String.format("Type %s of initialization expression of identifier %s is incompatible with declared type %s",
                        localNodeTypeSymbol.getName(), localIdSymbol.getName(), localIdSymbol.getScope().getName());

                SymbolTable.error(context, context.start, errorMessage);
                return false;
            }
            parent = ((ClassSymbolScope) inherited).getChildName();
        }

        String errorMessage = String.format("Type %s of initialization expression of identifier %s is incompatible with declared type %s",
                localNodeTypeSymbol.getName(), localIdSymbol.getName(), localIdSymbol.getScope().getName());

        SymbolTable.error(context, context.start, errorMessage);
        return false;
    }


    @Override
    public Symbol visit(New newToken) {
        String type_new = newToken.getId().getText();
        Symbol sys_new = Declarations.globals.lookup(type_new);
        if (sys_new == null) {
            handleUndefinedTypeError(newToken, type_new);
            return null;
        }

        return sys_new;
    }

    private void handleUndefinedTypeError(New newToken, String newTypeId) {
        String errorMessage = String.format("new is used with undefined type %s", newTypeId);
        SymbolTable.error(newToken.getContext(), newToken.getId(), errorMessage);
    }

    @Override
    public Symbol visit(Program program) {
        for (Class classs : program.getClasses()) {
            visitClass(classs);
        }
        return null;
    }

    private void visitClass(Class classs) {
        classs.accept(this);
    }

    @Override
    public Symbol visit(LeftOPRight leftOPRight) {
        Symbol rightOp = leftOPRight.getRightEx().accept(this);
        Symbol leftOp = leftOPRight.getLeftEx().accept(this);

        if (rightOp == null) {
            return null;
        }
        if (leftOp == null) {
            return null;
        }
        if ("=".equals((leftOPRight.getOperation()).getText())) {
            checkAndReportComparisonError(leftOPRight.getContext(), leftOPRight.getToken(), leftOp, rightOp);
        }
        else {
            if (leftOp != DeclarationScopes.intClassScope) {
                checkAndReportTypeMismatchError(leftOPRight, rightOp, DeclarationScopes.intClassScope);
            }

            if (rightOp != DeclarationScopes.intClassScope) {
                checkAndReportTypeMismatchError(leftOPRight, leftOp, DeclarationScopes.intClassScope, rightOp);
            }
        }
        return DeclarationScopes.boolClassScope;
    }
    private void checkAndReportTypeMismatchError(LeftOPRight leftOPRight, Symbol actualType, Symbol expectedType) {
        if (actualType != expectedType) {
            String errorMessage = String.format("Operand of %s has type %s instead of %s",
                    leftOPRight.getOperation().getText(), actualType.getName(), expectedType.getName());

            SymbolTable.error(leftOPRight.getContext(), leftOPRight.getContext().start, errorMessage);
        }
    }
    private void checkAndReportTypeMismatchError(LeftOPRight leftOPRight, Symbol actualType, Symbol expectedType, Symbol otherType) {
        if (actualType != expectedType) {
            String errorMessage = String.format("Operand of %s has type %s instead of %s",
                    leftOPRight.getOperation().getText(), actualType.getName(), expectedType.getName());

            SymbolTable.error(leftOPRight.getContext(), leftOPRight.getContext().stop, errorMessage);
        }

        if (otherType != DeclarationScopes.intClassScope) {
            String errorMessage = String.format("Operand of %s has type %s instead of Int",
                    leftOPRight.getOperation().getText(), otherType.getName());

            SymbolTable.error(leftOPRight.getContext(), leftOPRight.getContext().stop, errorMessage);
        }
    }

    private void checkAndReportComparisonError(ParserRuleContext context, Token token, Symbol leftOperand, Symbol rightOperand) {
        boolean leftOperandType = isCompatibleType(leftOperand);
        boolean rightOperandType = isCompatibleType(rightOperand);

        if ((leftOperandType || rightOperandType) && !leftOperand.equals(rightOperand)) {
            String errorMessage = String.format("Cannot compare %s with %s", leftOperand.getName(), rightOperand.getName());
            SymbolTable.error(context, token, errorMessage);
        }
    }
    private boolean isCompatibleType(Symbol operand) {
        return operand == DeclarationScopes.intClassScope ||
                operand == DeclarationScopes.boolClassScope ||
                operand == DeclarationScopes.stringClassScope;
    }

    @Override
    public Symbol visit(VarString varString) {
        return DeclarationScopes.stringClassScope;
    }

    @Override
    public Symbol visit(True trueToken) {
        return DeclarationScopes.boolClassScope;
    }

    @Override
    public Symbol visit(Unary unary) {
        Symbol symbol = unary.getExpression().accept(this);
        String operationText = (unary.getOperation()).getText();

        if ("~".equals(operationText)) {
            return handleNegationOperation(unary, symbol);
        } else if ("not".equals(operationText)) {
            return handleNotOperation(unary, symbol);
        } else if ("isvoid".equals(operationText)) {
            return handleIsVoidOperation(unary, symbol);
        } else {
            return symbol;
        }
    }

    private Symbol handleNegationOperation(Unary unary, Symbol symbol) {
        if (symbol != DeclarationScopes.boolClassScope) {
            handleUnaryError(unary, symbol, DeclarationScopes.intClassScope, "Int");
            return DeclarationScopes.intClassScope;
        }
        return symbol;
    }

    private Symbol handleNotOperation(Unary unary, Symbol symbol) {
        if (symbol != DeclarationScopes.boolClassScope) {
            handleUnaryError(unary, symbol, DeclarationScopes.boolClassScope, "Bool");
            return DeclarationScopes.boolClassScope;
        }
        return symbol;
    }

    private Symbol handleIsVoidOperation(Unary unary, Symbol symbol) {
        return DeclarationScopes.boolClassScope;
    }

    private void handleUnaryError(Unary unary, Symbol symbol, Symbol errorType, String typeName) {
        SymbolTable.error(unary.getContext(), unary.getContext().stop,
                String.format("Operand of %s has type %s instead of %s", unary.getOperation().getText(), symbol.getName(), typeName));
    }


    @Override
    public Symbol visit(While whileToken) {
        Symbol whileSys = whileToken.getCond().accept(this);

        if (!isConditionBoolean(whileSys)) {
            handleWhileConditionError(whileToken, whileSys);
            return DeclarationScopes.objectScope;
        }
        return whileSys;
    }

    private boolean isConditionBoolean(Symbol conditionSymbol) {
        return conditionSymbol == DeclarationScopes.boolClassScope;
    }

    private void handleWhileConditionError(While whileToken, Symbol conditionSymbol) {
        SymbolTable.error(whileToken.getContext(), whileToken.getToDisplay(),
                String.format("While condition has type %s instead of Bool", conditionSymbol.getName()));
    }

}
