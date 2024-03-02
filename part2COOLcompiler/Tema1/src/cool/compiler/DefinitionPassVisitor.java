package cool.compiler;

import cool.structures.*;
import cool.structures.symbol.*;
import cool.structures.scope.*;
import org.antlr.v4.runtime.Token;

import java.util.*;

public class DefinitionPassVisitor implements ASTVisitor<Void>{
    Scope currentScope = null;
    @Override
    public Void visit(Arithmetic arithmetic) {
        return null;
    }

    @Override
    public Void visit(Assignment assignment) {
        if(Objects.equals(Declarations.self, assignment.getId().getText())) {
            SymbolTable.error(
                    assignment.getContext(),
                    assignment.getId(),
                    String.format("Cannot assign to %s", assignment.getId().getText())
            );
            return null;
        }
        Objects.requireNonNull(assignment.getExpression(), "Expression cannot be null").accept(this);
        return null;
    }

    @Override
    public Void visit(Block block) {
        Optional.ofNullable(block.getExpression())
                .ifPresent(expressions -> expressions.forEach(this::acceptExpression));
        return null;
    }
    private void acceptExpression(Expression expressionNode) {
        expressionNode.accept(this);
    }

    @Override
    public Void visit(CaseLines caseBlock) {
        caseBlock.getCond().accept(this);
        Iterator<Expression> iterator = caseBlock.getCases().iterator();
        iterator.forEachRemaining(this::acceptExpression);
        return null;
    }

    @Override
    public Void visit(CaseExpressions caseExpr) {
        if(Objects.equals(Declarations.self, caseExpr.getId().getText())){
            SymbolTable.error(
                    caseExpr.getContext(),
                    caseExpr.getId(),
                    "Case variable has illegal name %s".formatted(caseExpr.getId().getText())
            );
            return null;
        }
        if(Objects.equals(Declarations.selfType, caseExpr.getType().getText())){
            SymbolTable.error(
                    caseExpr.getContext(),
                    caseExpr.getType(),
                    "Case variable %s has illegal type %s".formatted(
                            caseExpr.getId().getText(),
                            Declarations.selfType
                    )
            );
        }
        Optional.ofNullable(caseExpr.getExpression()).ifPresent(expressionNode -> expressionNode.accept(this));
        return null;
    }

    @Override
    public Void visit(Class classNode) {
        Symbol parentSys;
        Token classId = classNode.getMyClassName(), inheritClassName = classNode.getMyClassInherit();
        if (Objects.equals(classId.getText(), Declarations.selfType)) {
            SymbolTable.error(
                    classNode.getContext(),
                    classNode.getToken(),
                    "Class has illegal name %s".formatted(Declarations.selfType)
            );
            return null;
        }
        // parentSys - vede clasa parinte - daca nu am o clasa parinte, intoarce object
        parentSys = (inheritClassName != null)
                ? Declarations.globals.lookup(inheritClassName.getText())
                : DeclarationScopes.objectScope;
        // instantiaza scope ul clasei
        ClassSymbolScope classScope = new ClassSymbolScope(
                classId.getText(),
                (inheritClassName != null) ? (Scope) Declarations.globals.lookup(inheritClassName.getText()) : DeclarationScopes.objectScope
        );
        if (!Objects.equals(Declarations.globals.add(classScope), Boolean.TRUE)) {
            SymbolTable.error(
                    classNode.getContext(),
                    classNode.getToken(),
                    "Class %s is redefined".formatted(classId.getText())
            );
            return null;
        }
        Declarations.globals.addClass(classNode);
        if (inheritClassName != null &&
                DeclarationScopes.forbiddenInherit.stream().anyMatch(symbol -> Objects.equals(symbol, parentSys))) {
            SymbolTable.error(
                    classNode.getContext(),
                    classNode.getMyClassInherit(),
                    "Class %s has illegal parent %s".formatted(classId.getText(), inheritClassName.getText())
            );
            return null;
        }
        currentScope = classScope;
        classNode.getFeatures().forEach(featureNode -> featureNode.accept(this));
        classNode.setClassSymbolScope(classScope);
        return null;
    }

    @Override
    public Void visit(Dispatch dispatch) {
        return null;
    }

    @Override
    public Void visit(Expression expression) {
        return null;
    }

    @Override
    public Void visit(False falseToken) {
        return null;
    }

    @Override
    public Void visit(Feature featureNode) {
        if(featureNode.getUpdatedAttributeContext() == 1){
            Token attributeName = featureNode.getUpdatedFeatureId(), attributeType = featureNode.getUpdatedFeatureType();
            if (Objects.equals(attributeName.getText(), Declarations.self)) {
                SymbolTable.error(
                        featureNode.getContext(),
                        attributeName,
                        "Class %s has attribute with illegal name %s".formatted(
                                ((ClassSymbolScope) currentScope).getName(),
                                Declarations.self
                        )
                );
                return null;
            }
            IdSymbol attributeSymbol = new IdSymbol(attributeName.getText(), attributeType.getText());
            if (!Objects.equals(currentScope.add(attributeSymbol), Boolean.TRUE)) {
                SymbolTable.error(
                        featureNode.getContext(),
                        attributeName,
                        "Class %s redefines attribute %s".formatted(
                                ((ClassSymbolScope) currentScope).getName(),
                                attributeName.getText()
                        )
                );
                return null;
            }
            featureNode.setIdSymbol(attributeSymbol);
        }
        Optional.ofNullable(featureNode.getUpdatedExpressions())
                .filter(expressions -> !expressions.isEmpty())
                .map(expressions -> expressions.get(0))
                .ifPresent(expressionNode -> expressionNode.accept(this));
        if(featureNode.getUpdatedMethodContext() == 1){
            MethodSymbolScope methodScope = new MethodSymbolScope((featureNode.getUpdatedFeatureId()).getText(), currentScope, (featureNode.getUpdatedFeatureType()).getText());
            if(!(currentScope.add(methodScope))) {
                SymbolTable.error(
                        featureNode.getContext(),
                        featureNode.getUpdatedFeatureId(),
                        "Class %s redefines method %s".formatted(
                                ((ClassSymbolScope) currentScope).getName(),
                                featureNode.getUpdatedFeatureId().getText()
                        )
                );
                return null;
            }
            featureNode.setMethodScope(methodScope);
            Scope initialScope = currentScope;
            currentScope = methodScope;
            processFormalListAndExpressions(featureNode);
            currentScope = initialScope;
        }
        return null;
    }
    private void processFormalListAndExpressions(Feature featureNode) {
        featureNode.getUpdatedFormalList().forEach(this::processFormalNode);
        featureNode.getUpdatedExpressions().stream().forEach(this::processExpression);
    }

    private void processFormalNode(Formal formalNode) {
        formalNode.accept(this);
    }

    private void processExpression(Expression expressionNode) {
        expressionNode.accept(this);
    }

    @Override
    public Void visit(Formal formal) {
        Token formalToken = formal.getId(), formalType = formal.getType();
        if (Objects.equals(Declarations.selfType, formalType.getText())) {
            handleIllegalTypeSelfError(formal, formalToken, formalType);
            return null;
        }
        if (Objects.equals(Declarations.self, formalToken.getText())) {
            handleIllegalNameError(formal, formalToken);
            return null;
        }
        IdSymbol formalSymbol = new IdSymbol(formalToken.getText(), formalType.getText());
        if (!currentScope.add(formalSymbol)) {
            handleFormalRedefinitionError(formal, formalToken);
            return null;
        }
        formal.setIdSymbol(formalSymbol);
        return null;
    }
    private void handleIllegalNameError(Formal formal, Token formalName) {
        Scope parent = currentScope.getParent();
        String errorMessage = String.format("Method %s of class %s has formal parameter with illegal name %s",
                ((MethodSymbolScope) currentScope).getName(),
                ((ClassSymbolScope) parent).getName(),
                formalName.getText());
        SymbolTable.error(formal.getContext(), formalName, errorMessage);
    }
    private void handleFormalRedefinitionError(Formal formal, Token formalName) {
        Scope parent = currentScope.getParent();

        String errorMessage = String.format("Method %s of class %s redefines formal parameter %s",
                ((MethodSymbolScope) currentScope).getName(),
                ((ClassSymbolScope) parent).getName(),
                formalName.getText());
        SymbolTable.error(formal.getContext(), formalName, errorMessage);
    }

    private void handleIllegalTypeSelfError(Formal formal, Token formalName, Token formalType) {
        Scope parent = currentScope.getParent();
        String errorMessage = String.format("Method %s of class %s has formal parameter %s with illegal type SELF_TYPE",
                ((MethodSymbolScope) currentScope).getName(),
                ((ClassSymbolScope) parent).getName(),
                formalName.getText());
        SymbolTable.error(formal.getContext(), formalType, errorMessage);
    }

    @Override
    public Void visit(Id id) {
        return null;
    }

    @Override
    public Void visit(If ifVar) {
        visitCondition(ifVar);
        visitThenBranch(ifVar);
        visitElseBranch(ifVar);
        return null;
    }

    private void visitCondition(If ifToken) {
        ifToken.getCond().accept(this);
    }

    private void visitThenBranch(If ifToken) {
        ifToken.getThen().accept(this);
    }

    private void visitElseBranch(If ifToken) {
        ifToken.getElseCase().accept(this);
    }


    @Override
    public Void visit(ImplicitDispatch implicitDispatch) {
        return null;
    }

    @Override
    public Void visit(Int intToken) {
        return null;
    }

    @Override
    public Void visit(Let let) {
        LetSymbolScope scopeforLet = new LetSymbolScope("scope", currentScope);
        let.setScope(scopeforLet);

        Scope initialScope = currentScope;
        currentScope = scopeforLet;

        try {
            let.getLets().forEach(localNode -> localNode.accept(this));
            let.getExpression().accept(this);
        } finally {
            currentScope = initialScope;
        }

        return null;
    }

    @Override
    public Void visit(Locals locals) {
        checkForIllegalName(locals);

        IdSymbol idLocal = createIdSymbol(locals);
        locals.setIdSymbol(idLocal);

        processExpression(locals);
        return null;
    }

    private void checkForIllegalName(Locals locals) {
        if (Declarations.self.equals(locals.getId().getText())) {
            SymbolTable.error(
                    locals.getContext(),
                    locals.getId(),
                    "Let variable has illegal name self"
            );
        }
    }

    private IdSymbol createIdSymbol(Locals locals) {
        String id = locals.getId().getText();
        String type = locals.getTypeOrId().getText();
        IdSymbol sys = createIdSymbol(id, type);
        return sys;
    }
    private IdSymbol createIdSymbol(String idText, String typeOrIdText) {
        return new IdSymbol(idText, typeOrIdText);
    }

    private void processExpression(Locals locals) {
        if (locals.getExpression() != null) {
            locals.getExpression().accept(this);
        }
    }

    @Override
    public Void visit(New newToken) {
        return null;
    }

    @Override
    public Void visit(Program program) {
        List<Class> classes = program.getClasses();
        int size = classes.size();
        for (int i = 0; i < size; i++) {
            classes.get(i).accept(this);
        }
        return null;
    }


    @Override
    public Void visit(LeftOPRight leftOPRight) {
        visitLeftOPRight(leftOPRight);
        return null;
    }

    private void visitLeftOPRight(LeftOPRight leftOPRight) {
        leftOPRight.getRightEx().accept(this);
        leftOPRight.getLeftEx().accept(this);
    }


    @Override
    public Void visit(VarString varString) {
        return null;
    }

    @Override
    public Void visit(True trueToken) {
        return null;
    }

    @Override
    public Void visit(Unary unary) {
        return null;
    }

    @Override
    public Void visit(While whileToken) {
        visitWhile(whileToken);
        return null;
    }

    private void visitWhile(While whileToken) {
        whileToken.getCond().accept(this);
        whileToken.getInside().accept(this);
    }
}
