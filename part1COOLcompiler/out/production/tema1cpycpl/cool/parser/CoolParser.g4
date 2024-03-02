parser grammar CoolParser;

options {
    tokenVocab = CoolLexer;
}

@header{
    package cool.parser;
}

program : (class SEMI)+ EOF;

class : CLASS identificator = ID (INHERITS type)? LEFTBRACE feature* RIGHTBRACE;

feature : identificator = ID LEFTPAREN argumentsList? RIGHTPAREN COLON type LEFTBRACE inside = expr RIGHTBRACE SEMI  # method
        | identificator = ID COLON type (DARROW expr)? SEMI                                                          # attribute
        ;

argumentsList : (arguments += formal (COMMA arguments += formal)*); // list of the arguments

formal : identificator = ID COLON type;

local : identificator = ID COLON type (DARROW expr)?;

type : (TYPE | ID);

multdivv : (MULTIPLY | DIVIDE);

plusmin : (PLUS | MINUS);

localList : local (COMMA local)*; // list of variable declarations

blockCases : CASE cond = expr OF (case = expr SEMI)+ ESAC;

expr : expr (AROND type)? POINT id = ID LEFTPAREN (expr (COMMA expr)*)? RIGHTPAREN  # dispatch
     | identificator = ID LEFTPAREN (expr (COMMA expr)*)? RIGHTPAREN                # dispatchImpl
     | LEFTBRACE (expr SEMI)+ RIGHTBRACE                     # block
     | IF cond = expr THEN then = expr ELSE else = expr FI   # if
     | WHILE cond = expr LOOP inside = expr POOL             # while
     | LET localList? IN body = expr                         # let
     | identificator = ID COLON type ASSIGNING expr          # exprCase
     | blockCases                                            # blockCase
     | NEW type                                              # new
     | ISVOID val = expr                                     # isvoid
     | expr multdivv expr                                    # multdiv
     | expr plusmin expr                                     # plusminus
     | TILDE val = expr                                      # tilde
     | expr LESSTHAN expr                                    # lessTh
     | expr LESSOREQ expr                                    # lessOrEq
     | expr EQUAL expr                                       # equal
     | NOT val = expr                                        # not
     | identificator = ID DARROW val = expr                  # assignment
     | LEFTPAREN val = expr RIGHTPAREN                       # paren
     | ID                                                    # id
     | SELF                                                  # self
     | INT                                                   # integer
     | STRING                                                # string
     | bool = (TRUE | FALSE)                                 # truefalse
     ;