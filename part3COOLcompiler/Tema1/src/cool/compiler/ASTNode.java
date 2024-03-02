package cool.compiler;

import cool.structures.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.util.LinkedList;

public class ASTNode {
    Token token;

    ParserRuleContext ctx;

    ASTNode(Token token, ParserRuleContext ctx) { this.token = token; this.ctx = ctx; }

    public <T> T accept(ASTVisitor<T> visitor) { return null; };
}

abstract class Expression extends ASTNode {
    Expression(Token token, ParserRuleContext ctx) { super(token, ctx);}
}

class Program extends Expression {
    LinkedList<Expression> classes;

    Program(Token token,
            LinkedList<Expression> classes,
            ParserRuleContext ctx) {
        super(token, ctx);
        this.classes = classes;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}


// Class Related
class Clazz extends Expression {
    Token id;
    Token inheritedType;
    LinkedList<Expression> features;

    Id classId;

    Symbol symbol;

    Scope globalScope;

    Clazz(Token start,
          Token id,
          Token inheritedType,
          LinkedList<Expression> features,
          Id classId,
          ParserRuleContext ctx) {
        super(start, ctx);
        this.id = id;
        this.inheritedType = inheritedType;
        this.features = features;
        this.classId = classId;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class AttributeDefinition extends Expression {
    private Symbol idSymbol;
    private ClassSymbol typeSymbol;

    private Scope scope;
    Token type;
    Token id;
    Expression initValue;
    public AttributeDefinition(Token start,
                               Token type,
                               Token id,
                               Expression initValue,
                               ParserRuleContext ctx) {
        super(start, ctx);
        this.type = type;
        this.id = id;
        this.initValue = initValue;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public Symbol getIdSymbol() {
        return idSymbol;
    }

    public void setIdSymbol(Symbol idSymbol) {
        this.idSymbol = idSymbol;
    }

    public ClassSymbol getTypeSymbol() {
        return typeSymbol;
    }

    public void setTypeSymbol(ClassSymbol typeSymbol) {
        this.typeSymbol = typeSymbol;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }
}

class FormalDefinition extends Expression {
    private IdSymbol symbol;
    private FunctionSymbol scope;
    Token type;
    Token id;
    public FormalDefinition(Token start,
                            Token type,
                            Token id,
                            ParserRuleContext ctx) {
        super(start, ctx);
        this.type = type;
        this.id = id;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public IdSymbol getSymbol() {
        return symbol;
    }

    public void setSymbol(IdSymbol symbol) {
        this.symbol = symbol;
    }

    public FunctionSymbol getScope() {
        return scope;
    }

    public void setScope(FunctionSymbol scope) {
        this.scope = scope;
    }
}

class FunctionDefinition extends Expression {

    FunctionSymbol functionSymbol;

    Scope scope;

    Token type;
    Token id;
    LinkedList<FormalDefinition> formalArguments;
    Expression body;
    public FunctionDefinition(Token start,
                              Token type,
                              Token id,
                              LinkedList<FormalDefinition> formalArguments,
                              Expression body,
                              ParserRuleContext ctx) {
        super(start, ctx);
        this.type = type;
        this.id = id;
        this.formalArguments = formalArguments;
        this.body = body;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public FunctionSymbol getFunctionSymbol() {
        return functionSymbol;
    }

    public void setFunctionSymbol(FunctionSymbol functionSymbol) {
        this.functionSymbol = functionSymbol;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }
}


// Variables related
class Id extends Expression {
    private Symbol symbol;

    private Scope scope;
    public Id(Token token, ParserRuleContext ctx) {
        super(token, ctx);
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public void setSymbol(Symbol IdSymbol) {
        this.symbol = IdSymbol;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }
}

class Assign extends Expression {
    Token left;
    Expression right;
    public Assign(Token start,
                  Token left,
                  Expression right,
                  ParserRuleContext ctx) {
        super(start, ctx);
        this.left = left;
        this.right = right;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}


class IsVoid extends Expression {
    Expression expression;
    public IsVoid(Token start,
                  Expression expression,
                  ParserRuleContext ctx) {
        super(start, ctx);
        this.expression = expression;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class NewExpression extends Expression {
    Token type;
    public NewExpression(Token start,
                         Token type,
                         ParserRuleContext ctx) {
        super(start, ctx);
        this.type = type;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}



// Ints, Bools and Strings
class Int extends Expression {

    public Int(Token start, ParserRuleContext ctx) {
        super(start, ctx);
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Bool extends Expression {
    public Bool(Token token, ParserRuleContext ctx) {
        super(token, ctx);
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Stringg extends Expression {
    public Stringg(Token token, ParserRuleContext ctx) {
        super(token, ctx);
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}


// Arithmectic Operations
class ArithmeticOperation extends Expression {
    Expression left;
    Expression right;
    Token operation;
    public ArithmeticOperation(Token start,
                Expression left,
                Expression right,
                Token operation,
                ParserRuleContext ctx) {
        super(start, ctx);
        this.left = left;
        this.right = right;
        this.operation = operation;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class TildeOperation extends Expression {
    Expression expression;
    public TildeOperation(Token start,
                          Expression expression,
                          ParserRuleContext ctx) {
        super(start, ctx);
        this.expression = expression;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class ParenthesisOperation extends Expression {
    Expression expression;
    public ParenthesisOperation(Token start,
                                Expression expression,
                                ParserRuleContext ctx) {
        super(start, ctx);
        this.expression = expression;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}


// Relational Operations
class RelationalOperation extends Expression {
    Expression left;
    Expression right;
    Token relation;
    public RelationalOperation(Token start,
                               Expression left,
                               Expression right,
                               Token relation,
                               ParserRuleContext ctx) {
        super(start, ctx);
        this.left = left;
        this.right = right;
        this.relation = relation;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Not extends Expression {
    Expression expression;
    public Not(Token token,
               Expression expression,
               ParserRuleContext ctx) {
        super(token, ctx);
        this.expression = expression;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

// Dispatch
class Dispatch extends Expression {
    Expression dispatcher;
    Token subclass;
    Token function;
    LinkedList<Expression> actualParams;

    boolean isImplicit;

    public Dispatch(Token token, Expression dispatcher, Token subclass,
                    Token function, LinkedList<Expression> actualParams,
                    ParserRuleContext ctx) {
        super(token, ctx);
        this.dispatcher = dispatcher;
        this.subclass = subclass;
        this.function = function;
        this.actualParams = actualParams;
        this.isImplicit = false;
    }

    public Dispatch(Token token, Expression dispatcher, Token subclass,
                    Token function, LinkedList<Expression> actualParams,
                    boolean isImplicit, ParserRuleContext ctx) {
        super(token, ctx);
        this.dispatcher = dispatcher;
        this.subclass = subclass;
        this.function = function;
        this.actualParams = actualParams;
        this.isImplicit = isImplicit;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}


class Iff extends Expression {
    Expression cond;
    Expression then;
    Expression elsee;

    public Iff(Token start, Expression cond, Expression then,
               Expression elsee, ParserRuleContext ctx) {
        super(start, ctx);
        this.cond = cond;
        this.then = then;
        this.elsee = elsee;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}


class While extends Expression {
    Expression cond;
    Expression body;

    public While(Token start,
                 Expression cond,
                 Expression body,
                 ParserRuleContext ctx) {
        super(start, ctx);
        this.cond = cond;
        this.body = body;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

// Let
class LetItem extends Expression {
    private IdSymbol symbol;
    private Scope scope;
    Token id;
    Token type;
    Expression initValue;

    LetItem(Token start,
            Token id,
            Token type,
            Expression initValue,
            ParserRuleContext ctx) {
        super(start, ctx);
        this.id = id;
        this.type = type;
        this.initValue = initValue;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public IdSymbol getSymbol() {
        return symbol;
    }

    public void setSymbol(IdSymbol symbol) {
        this.symbol = symbol;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }
}


class Let extends Expression {
    Scope scope;
    LinkedList<LetItem> items;
    LinkedList<Token> ids;
    LinkedList<Token> types;
    LinkedList<Expression> initValues;

    Expression body;

    //TODO: a more correct way to associate initValues, this is not 100% correct
    // if variables are all on the same line
    public void associateLetItems() {
        items = new LinkedList<>();
        int i = 0, j = 0;
        for (Token id: ids) {
            Expression body = null;

            if (j < initValues.size()) {
                if ( i == ids.size() - 1) {
                    body = initValues.get(j);
                    j++;
                } else {
                    if (initValues.get(j).token.getLine() < ids.get(i + 1).getLine()) {
                        body = initValues.get(j);
                        j++;
                    }
                }
            }

            items.add(new LetItem(id, id, types.get(i), body, this.ctx));
            i++;
        }
    }

    public Let(Token start,
               LinkedList<Token> ids,
               LinkedList<Token> types,
               LinkedList<Expression> initValues,
               Expression body,
               ParserRuleContext ctx) {
        super(start, ctx);
        this.ids = ids;
        this.types = types;
        this.initValues = initValues;
        this.body = body;
        associateLetItems();
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }
}


// Case
class CaseItem extends Expression {
    Scope scope;
    IdSymbol symbol;
    Token id;
    Token type;
    Expression body;

    CaseItem(Token start,
            Token id,
            Token type,
            Expression body,
             ParserRuleContext ctx) {
        super(start, ctx);
        this.id = id;
        this.type = type;
        this.body = body;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public IdSymbol getSymbol() {
        return symbol;
    }

    public void setSymbol(IdSymbol symbol) {
        this.symbol = symbol;
    }
}


class Case extends Expression {
    Expression cond;
    LinkedList<CaseItem> items;
    LinkedList<Token> ids;
    LinkedList<Token> types;
    LinkedList<Expression> bodies;

    public void associateCaseItems() {
        items = new LinkedList<>();
        int i = 0;
        for (Token id: ids) {
            items.add(new CaseItem(id, id, types.get(i), bodies.get(i), this.ctx));
            i++;
        }
    }

    public Case(Token start,
               Expression cond,
               LinkedList<Token> ids,
               LinkedList<Token> types,
               LinkedList<Expression> bodies,
                ParserRuleContext ctx) {
        super(start, ctx);
        this.cond = cond;
        this.ids = ids;
        this.types = types;
        this.bodies = bodies;
        associateCaseItems();
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Block extends Expression {
    LinkedList<Expression> expressions;


    public Block(Token token, LinkedList<Expression> expressions,
                 ParserRuleContext ctx) {
        super(token, ctx);
        this.expressions = expressions;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}





