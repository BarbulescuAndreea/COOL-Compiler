package cool.compiler;
import org.antlr.v4.runtime.Token;
import java.util.List;

public abstract class ASTNode {
    public <T> T accept(ASTVisitor<T> visitor) {
        return null;
    }
}

abstract class Class extends ASTNode {
    private List<Feature> features;
    private String myClass;
    private String parentClass;
    private String childClass;

    public Class(String parentClass, String childClass, String myClass, List<Feature> features) {
        this.features = features;
        this.parentClass = parentClass;
        this.childClass = childClass;
        this.myClass = myClass;
    }

    public String getMyClass() {
        return myClass;
    }

    public String getChildClass() {
        return childClass;
    }

    public List<Feature> getFeatures() {
        return features;
    }
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

abstract class Program extends ASTNode {
    private final List<Class> classes;
    public Program(List<Class> classes) {
        this.classes = classes;
    }
    public List<Class> getClasses() {
        return classes;
    }
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

abstract class Expression extends ASTNode {
    public Token token;

    public Token getToken() {
        return token;
    }

    Expression(Token token) {
        this.token = token;
    }
}

abstract class Feature extends ASTNode {
    private String updatedFeatureId;
    private String updatedFeatureType;
    private List<Formal> updatedFormalList;
    private List<Expression> updatedExpressions;
    private int updatedAttributeContext;
    private int updatedMethodContext;

    public int getUpdatedAttributeContext() {
        return updatedAttributeContext;
    }

    public int getUpdatedMethodContext() {
        return updatedMethodContext;
    }

    // Constructor for updated method feature
    public Feature(String id, String type, List<Expression> expressions, int attributeContext, int methodContext, List<Formal> formalList) {
        this.updatedFeatureId = id;
        this.updatedFeatureType = type;
        this.updatedFormalList = formalList;
        this.updatedExpressions = expressions;
        this.updatedAttributeContext = attributeContext;
        this.updatedMethodContext = methodContext;
    }

    public String getUpdatedFeatureId() {
        return updatedFeatureId;
    }
    public String getUpdatedFeatureType() {
        return updatedFeatureType;
    }
    public List<Expression> getUpdatedExpressions() {
        return updatedExpressions;
    }
    public List<Formal> getUpdatedFormalList() {
        return updatedFormalList;
    }
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
class While extends Expression{
    private Expression cond;
    private Expression inside;

    public Expression getCond() {
        return cond;
    }

    public Expression getInside() {
        return inside;
    }

    public While(Token token, Expression cond, Expression inside){
        super(token);
        this.cond = cond;
        this.inside = inside;
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class If extends Expression{
    private Expression then;
    private Expression elseCase;
    private Expression cond;

    public Expression getThen() {
        return then;
    }

    public Expression getElseCase() {
        return elseCase;
    }

    public Expression getCond() {
        return cond;
    }

    public If(Token token, Expression cond, Expression then, Expression elseCase){
        super(token);
        this.cond = cond;
        this.then = then;
        this.elseCase = elseCase;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Block extends Expression{
    public List<Expression> expression;

    public List<Expression> getExpression() {
        return expression;
    }

    public Block(Token token, List<Expression> expressionsList){
        super(token);
        this.expression = expressionsList;
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Let extends Expression {
    private Expression expression;
    private List<Locals> lets;

    public Expression getExpression() {
        return expression;
    }

    public List<Locals> getLets() {
        return lets;
    }

    public Let(Token token, Expression expression, List<Locals> lets){
        super(token);
        this.expression = expression;
        this.lets = lets;
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Arithmetic extends Expression {
    private Expression rightEx;
    private Expression leftEx;
    private String operation;

    public Expression getRightEx() {
        return rightEx;
    }

    public Expression getLeftEx() {
        return leftEx;
    }

    public String getOperation() {
        return operation;
    }

    Arithmetic(Token token, Expression leftEx, Expression rightEx, String operation) {
        super(token);
        this.operation = operation;
        this.leftEx = leftEx;
        this.rightEx = rightEx;
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Locals extends ASTNode {
    private String id;
    private String typeOrId;
    private Expression expression;

    public String getId() {
        return id;
    }

    public String getTypeOrId() {
        return typeOrId;
    }

    public Expression getExpression() {
        return expression;
    }

    public Locals(Expression expression, String id, String typeOrId) {
        this.expression = expression;
        this.id = id;
        this.typeOrId = typeOrId;
    }
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

abstract class Formal extends ASTNode {
    private String id;
    private String type;

    public Formal(String id, String type) {
        this.id = id;
        this.type = type;
    }
    public String getId() {
        return id;
    }
    public String getType() {
        return type;
    }
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}


// paranteze, not, tilde, isvoid, assign
class Unary extends Expression {
    private String operation;
    private Expression expression;

    public String getOperation() {
        return operation;
    }

    public Expression getExpression() {
        return expression;
    }

    Unary(Token token, Expression expression, String operation) {
        super(token);
        this.expression = expression;
        this.operation = operation;
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Dispatch extends Expression{
    private String id;
    private String type;
    private Expression expression;
    private List<Expression> expressionsList;

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Expression getExpression() {
        return expression;
    }

    public List<Expression> getExpressionsList() {
        return expressionsList;
    }

    public Dispatch(Token token, String type, String id, List<Expression> expressionsList, Expression expression){
        super(token);

        this.expression = expression;
        this.expressionsList = expressionsList;
        this.id = id;
        this.type = type;
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
class ImplicitDispatch extends Expression{
    private String id;
    private List<Expression> expressions;

    public String getId() {
        return id;
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    public ImplicitDispatch(Token token, List<Expression> expressions, String id){
        super(token);
        this.id = id;
        this.expressions = expressions;
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Assignment extends Expression{
    private String id;
    private Expression expression;

    public String getId() {
        return id;
    }

    public Expression getExpression() {
        return expression;
    }

    Assignment(Token token, Expression expression, String id){
        super(token);
        this.id = id;
        this.expression = expression;
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}


class CaseLines extends Expression{
    public Expression cond;
    public List<Expression> cases;

    public Expression getCond() {
        return cond;
    }

    public List<Expression> getCases() {
        return cases;
    }

    public CaseLines(Token token, Expression cond, List<Expression> cases){
        super(token);
        this.cond = cond;
        this.cases = cases;
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class CaseExpressions extends Expression{
    private Expression expression;
    private String id;
    private String type;

    public Expression getExpression() {
        return expression;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public CaseExpressions(Token token, String id, Expression expression, String type){
        super(token);
        this.id = id;
        this.expression = expression;
        this.type = type;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class New extends Expression {
    private String id;

    public String getId() {
        return id;
    }

    public New(Token token, String id) {
        super(token);
        this.id = id;
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Self extends Expression {
    public Self(Token token) {
        super(token);
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class LeftOPRight extends Expression {
    private Expression rightEx;
    private Expression leftEx;
    private String operation;

    public Expression getRightEx() {
        return rightEx;
    }

    public Expression getLeftEx() {
        return leftEx;
    }

    public String getOperation() {
        return operation;
    }

    LeftOPRight(Token token, Expression leftEx, String operation, Expression rightEx) {
        super(token);
        this.operation = operation;
        this.rightEx = rightEx;
        this.leftEx = leftEx;
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Id extends Expression {
    Id(Token token) {
        super(token);
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Int extends Expression {
    private Integer value;

    public Integer getValue() {
        return value;
    }

    Int(Token token) {
        super(token);
        value = Integer.parseInt(token.getText());
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class VarString extends Expression {
    String value;
    VarString(Token token) {
        super(token);
        value = token.getText();
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class False extends Expression {
    public Boolean value = false;
    public False(Token token) {
        super(token);
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class True extends Expression {
    public Boolean value = true;

    public True(Token token) {
        super(token);
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
