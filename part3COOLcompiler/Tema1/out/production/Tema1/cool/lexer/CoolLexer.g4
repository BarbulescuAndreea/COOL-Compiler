lexer grammar CoolLexer;

tokens { ERROR } 

@header{
    package cool.lexer;	
}

@members{    
    private void raiseError(String msg) {
        setText(msg);
        setType(ERROR);
    }
}


// Reserved and special characters/strings.commands

IF : 'if';
THEN : 'then' ;
ELSE : 'else' ;
FI : 'fi' ;

CLASS : 'class' ;
INHERITS : 'inherits' ;
NEW : 'new' ;
ATSIGN : '@' ;
ISVOID : 'isvoid';

LET : 'let' ;
IN : 'in';

LOOP : 'loop' ;
POOL : 'pool' ;
WHILE : 'while' ;

CASE : 'case' ;
OF : 'of' ;
ESAC : 'esac' ;
CASEITEM : '=>';

TRUE : 'true' ;
FALSE : 'false' ;

ASSIGN : '<-' ;

PLUS : '+';
MINUS : '-';
MULT : '*';
DIVIDE : '/';
TILDE : '~' ;


NOT : 'not' ;
EQUAL : '=';
LOWER : '<' ;
LOWE_AND_EQUAL : '<=' ;

SEMICOLON : ';' ;
COLON : ':' ;
DOT : '.' ;
COMMA : ',' ;


// Numbers
fragment DIGIT : [0-9];
INT : DIGIT+;


// Identificatori
fragment LETTER: [a-zA-Z];
fragment LOWER_CASE_LETTER : [a-z];
fragment UPPER_CASE_LETTER : [A-Z];
ID : LOWER_CASE_LETTER (LETTER | DIGIT | '_')*;
TYPE : UPPER_CASE_LETTER (LETTER | DIGIT | '_')*
     | 'self'
     | 'SELF_TYPE';


// Paranthesis
LPAREN : '(' ;
RPAREN : ')';
LBOX : '[' ;
RBOX : ']' ;
LBRACE : '{' ;
RBRACE : '}' ;

fragment ESCAPED_NEWLINE : '\\' NEW_LINE ;
// Strings
STRING : '"' ('\\"'  | ESCAPED_NEWLINE | (~('\u0000' | '\n')) ) *? '"'
    {
        String defaultText = getText();

        // check for unescaped new lines
        int enter = defaultText.indexOf('\n');
        while(enter != -1 && enter < defaultText.length()) {
            if (enter == 0 || defaultText.charAt(enter - 2) != '\\') {
                raiseError("Unterminated string constant");
            }
            enter = defaultText.indexOf('\n', enter + 1);
        }

        // delete "
        setText(defaultText.substring(1, defaultText.length() - 1)
                            .replace("\\t", "\t")
                            .replace("\\b", "\b")
                            .replace("\\f", "\f")
                            .replace("\\n", "\n")
                            .replaceAll("\\\\(?![\\\\])", "")
                );

        if (getText().length() > 1024) {
            raiseError("String constant too long");
        }
    };



STRING_CONTANS_NULL : '"' ('\\"' | ~'\n')*? '"'
    {
        raiseError("String contains null character");
    };
STRING_UNTERMINATED : '"' ('\\"' | . )*? ('"' | '\n')
    {
        raiseError("Unterminated string constant");
    };
STRING_CONTAINS_END_OF_FILE : '"' ('\\"' | ~'"' )*? EOF '"'?
    {
        raiseError("EOF in string constant");
    };


fragment NEW_LINE : '\r'? '\n';


// Comments
LINE_COMMENT
    : '--' .*? (NEW_LINE | EOF) -> skip
    ;

UNAMTECHED_BLOCK_COMMENT : '*)' { raiseError("Unmatched *)");} ;
BLOCK_COMMENT
    : '(*'
        (BLOCK_COMMENT
            | OPNENED_NOT_CLOSED {
                raiseError("EOF in comment");
            }
            | .
        )*? '*)'
    {
        if ( !getText().equals("EOF in comment"))
            skip();
    } ;
OPNENED_NOT_CLOSED : '(*' .*?
        { raiseError("EOF in comment"); };
INVALID_CHARACTER : '#'  {
        raiseError("Invalid character: #");
    };



WS
    :   [ \n\f\r\t]+ -> skip
    ; 