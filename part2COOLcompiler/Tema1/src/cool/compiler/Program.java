package cool.compiler;

import cool.parser.CoolParser;
import org.antlr.v4.runtime.Token;

import java.util.List;

public abstract class Program extends ASTNode {
    private final List<Class> classes;
    public Program(CoolParser.ProgramContext context, Token token, List<Class> classes) {
        super(token, context);
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
