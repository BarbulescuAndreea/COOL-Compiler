parser grammar CoolParser;

options {
    tokenVocab = CoolLexer;
}

@header{
    package cool.parser;
}

program
    :  (clazzes+=class SEMICOLON)+
    |   EOF
    ;

class : CLASS id=TYPE (INHERITS inheritedType=TYPE)? LBRACE (features+=feature SEMICOLON)* RBRACE;

feature : id=ID LPAREN (formalArgs+=formal (COMMA formalArgs+=formal)* )? RPAREN COLON type=TYPE LBRACE body=expr RBRACE        #funcDef
        | id=ID COLON type=TYPE (ASSIGN initValue=expr)?                                                                        #attributeDef
        ;

formal : id=ID COLON type=TYPE ;

expr : dispacher=expr (ATSIGN subclass=TYPE)? DOT func=ID LPAREN (actualParams+=expr (COMMA actualParams+=expr)*)? RPAREN                    #dispatch
     | func=ID LPAREN (actualParams+=expr (COMMA actualParams+=expr)*)? RPAREN                                                             #implicitDispatch
     | IF cond=expr THEN then=expr ELSE else=expr FI                                                                                        #if
     | WHILE cond=expr LOOP body=expr POOL                                                                                                  #while
     | LBRACE (expressions+=expr SEMICOLON)+ RBRACE                                                                                         #block
     | CASE cond=expr OF (ids+=ID COLON types+=TYPE CASEITEM bodies+=expr SEMICOLON)+ ESAC                                                  #case
     | LET ids+=ID COLON types+=TYPE (ASSIGN initValues+=expr)? (COMMA ids+=ID COLON types+=TYPE (ASSIGN initValues+=expr)?)* IN body=expr  #let
     | NEW type=TYPE                                                                                                                        #new
     | ISVOID expression=expr                                                                                                               #isvoid
     | TILDE expression=expr                                                                                                                #tilde
     | LPAREN expression=expr RPAREN                                                                                                        #paren
     | left=expr operation=(MULT | DIVIDE) right=expr                                                                                       #multDivide
     | left=expr operation=(PLUS | MINUS) right=expr                                                                                        #plusMinus
     | left=expr relation=(LOWER | LOWE_AND_EQUAL | EQUAL) right=expr                                                                       #relational
     | NOT expression=expr                                                                                                                  #not
     | left=ID ASSIGN right=expr                                                                                                            #assign
     | boolValue=(TRUE | FALSE)                                                                                                             #bool
     | id=ID                                                                                                                                #id
     | integer=INT                                                                                                                          #int
     | string=STRING                                                                                                                        #string
     ;