package cool.compiler;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public abstract class Expression extends ASTNode {
    Expression(ParserRuleContext context, Token token) {
        super(token, context);
    }
}
