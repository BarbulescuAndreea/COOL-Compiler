//parser grammar CoolParser;
//
//options {
//    tokenVocab = CoolLexer;
//}
//
//@header{
//    package cool.parser;
//}
//
//program : (class SEMI)+ EOF;
//
//class : CLASS identificator = TYPE (INHERITS inherit = TYPE)? LEFTBRACE feature* RIGHTBRACE;
//// method : sum(a1: int, a2: int): rettype(int) { body }; ? - may not have arguments
//// val: int <- ? ;
//feature : identificator = ID LEFTPAREN argumentsList? RIGHTPAREN COLON type LEFTBRACE inside = expr RIGHTBRACE SEMI  # method
//        | identificator = ID COLON type (DARROW expr)? SEMI                                                          # attribute
//        ;
//
//argumentsList : (arguments += formal (COMMA arguments += formal)*); // list of the arguments
//
//formal : identificator = ID COLON type;
//
//local : identificator = ID COLON type (DARROW expr)?;
//
//type : TYPE;
//
//multdivv : (MULTIPLY | DIVIDE);
//
//plusmin : (PLUS | MINUS);
//
//localList : local (COMMA local)*; // list of variable declarations
//// case expr of
////      x:int => block/expr; (exprCase)
//blockCases : CASE cond = expr OF (case = expr SEMI)+ ESAC;
//// dispatch - dog@animal.latra(tare)
//// implicitdispatch self.latra(tare)
//expr : expr (AROND type)? POINT id = ID LEFTPAREN (expr (COMMA expr)*)? RIGHTPAREN  # dispatch
//     | identificator = ID LEFTPAREN (expr (COMMA expr)*)? RIGHTPAREN                # dispatchImpl
//     | LEFTBRACE (expr SEMI)+ RIGHTBRACE                     # block
//     | IF cond = expr THEN then = expr ELSE else = expr FI   # if
//     | WHILE cond = expr LOOP inside = expr POOL             # while
//     | LET localList? IN body = expr                         # let
//     | identificator = ID COLON type ASSIGNING expr          # exprCase
//     | blockCases                                            # blockCase
//     | NEW type                                              # new
//     | ISVOID val = expr                                     # isvoid
//     | expr multdivv expr                                    # multdiv
//     | expr plusmin expr                                     # plusminus //same priority - left to right
//     | TILDE val = expr                                      # tilde
//     | expr LESSTHAN expr                                    # lessTh
//     | expr LESSOREQ expr                                    # lessOrEq
//     | expr EQUAL expr                                       # equal
//     | NOT val = expr                                        # not
//     | identificator = ID DARROW val = expr                  # assignment
//     | LEFTPAREN val = expr RIGHTPAREN                       # paren
//     | ID                                                    # id
//     | INT                                                   # integer
//     | STRING                                                # string
//     | bool = (TRUE | FALSE)                                 # truefalse
//     ;
//parser grammar CoolParser;
//
//options {
//    tokenVocab = CoolLexer;
//}
//
//@header{
//    package cool.parser;
//}
//
//program : (class SEMI)+ EOF;
//
//class : CLASS id = (ID|TYPE) (INHERITS type = (TYPE | ID))? LBRACE feature* RBRACE;
//
//feature : id = ID LPAREN (args+=formal (COMMA args+=formal)*)? RPAREN COLON type = (TYPE | ID) LBRACE body = expr RBRACE SEMI  # method
//        | id = ID COLON type = (TYPE | ID) (DARROW expr)? SEMI                                                          # attribute
//        ;
//
//formal : ID COLON type = (TYPE | ID);
//
//local : ID COLON type = (TYPE | ID) (DARROW expr)?;
//
//expr : expr (AT type = (TYPE | ID))? DOT id = ID LPAREN (expr (COMMA expr)*)? RPAREN                # dispatch
//     | ID LPAREN (expr (COMMA expr)*)? RPAREN                # implicit_dispatch
//     | IF cond = expr THEN then = expr ELSE else = expr FI   # if
//     | WHILE cond = expr LOOP body = expr POOL               # while
//     | LBRACE (expr SEMI)+ RBRACE                            # block
//     | LET local (COMMA local)*? IN body = expr              # let
//     | CASE cond = expr OF (case = expr SEMI)+ ESAC          # case_block
//     | ID COLON type = (ID | TYPE) RE expr                   # case_expr
//     | NEW type = (ID | TYPE)                                # new
//     | ISVOID expr                                           # isvoid
//     | leftExp = expr op = (MULT | DIV) rightExp = expr      # multdiv
//     | leftExp = expr op = (MINUS | PLUS) rightExp = expr    # plusminus
//     | TILDE expr                                            # negation
//     | leftExp = expr LT rightExp = expr                     # less_than
//     | leftExp = expr LE rightExp = expr                     # less_than_or_equal
//     | leftExp = expr EQUAL rightExp = expr                  # equal
//     | NOT expr                                              # not
//     | ID DARROW expr                                        # assignment
//     | LPAREN expr RPAREN                                    # parentheses
//     | ID                                                    # identifier
//     | INT                                                   # integer
//     | STRING                                                # string
//     | TRUE                                                  # true
//     | FALSE                                                 # false
//     ;

parser grammar CoolParser;

options {
    tokenVocab = CoolLexer;
}

@header{
    package cool.parser;
}

program : (class SEMI)+ EOF ;

class : CLASS type = TYPE (INHERITS inherit = TYPE)? LBRACE (feature SEMI)* RBRACE ;

formal : id = ID COLON type = TYPE;

feature
    :
        id = ID LPAREN (formalParams+=formal (COMMA formalParams+=formal)*)? RPAREN COLON type = TYPE LBRACE methodBody=expr RBRACE     #method
        | id = ID COLON type = TYPE (ASSIGN expr)?                                                                                      #attribute
    ;

local : ID COLON type = (TYPE | ID) (ASSIGN expr)?;

expr
    :   expr (AT type = (TYPE | ID))? DOT id = ID LPAREN (expr (COMMA expr)*)? RPAREN                # dispatch
        | ID LPAREN (expr (COMMA expr)*)? RPAREN                # implicit_dispatch
        | IF cond = expr THEN then = expr ELSE else = expr FI   # if
        | WHILE cond = expr LOOP body = expr POOL               # while
        | LBRACE (expr SEMI)+ RBRACE                            # block
        | LET local (COMMA local)*? IN body = expr              # let
        | CASE cond = expr OF (case = expr SEMI)+ ESAC          # case_block
        | ID COLON type = (ID | TYPE) RESULTS_CASE expr                   # case_expr
        | NEW type = (ID | TYPE)                                # new
        | ISVOID expr                                           # isvoid
        | TILDA expr                                            # negation
        | leftExp = expr op = (MULTIPLY | DIVIDE) rightExp = expr      # multdiv
        | leftExp = expr op = (MINUS | PLUS) rightExp = expr    # plusminus
        | leftExp = expr LT rightExp = expr                     # less_than
        | leftExp = expr LE rightExp = expr                     # less_than_or_equal
        | leftExp = expr EQ rightExp = expr                  # equal
        | NOT expr                                              # not
        | ID ASSIGN expr                                        # assignment
        | LPAREN expr RPAREN                                    # parentheses
        | ID                                                    # identifier
        | INT                                                   # integer
        | STRING                                                # string
        | TRUE                                                  # true
        | FALSE                                                 # false
    ;