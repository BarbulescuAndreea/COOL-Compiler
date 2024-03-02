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

WS
    :  [ \n\f\r\t]+ -> skip
    ;

STRING
    : '"' (ESC_SEQ | '\\"' | .)*?
    ('"' {
            String text = getText();
            //Delete quotes
            text = text.substring(1, text.length() - 1);

            StringBuilder processedText = new StringBuilder();
            int val = 0;
            boolean escapeMode = false;
            char[] charArray = text.toCharArray();
           while (val < charArray.length) {
               char c = charArray[val];
               if (escapeMode) {
                   if(c == 'n'){
                       processedText.append('\n');
                   }else if (c == 't'){
                       processedText.append('\t');
                   }else if (c == 'b'){
                       processedText.append('\b');
                   }else if (c == 'f'){
                       processedText.append('\f');
                   }else{
                       processedText.append(c);
                   }
                   escapeMode = false;
               }else if (c == '\\'){
                   escapeMode = true;
               }else{
                   processedText.append(c);
               }
               val++;
           }
            int textLength = text.length();
            if (textLength > 1024) {
                raiseError("String constant too long");
                return;
            }
            if (text.contains("\u0000")) {
                raiseError("String contains null character");
                return;
            }
            setText(processedText.toString()); // Set the modified text as the token text
        }
        | EOF {raiseError("EOF in string constant");}
        )
    ;

IF : 'if';
THEN : 'then';
ELSE : 'else';
FI : 'fi';
LET : 'let';
LOOP : 'loop';
POOL : 'pool';
WHILE : 'while';
CASE : 'case';
ESAC : 'esac';
CLASS : 'class';

// boolean true & false
FALSE : 'false';
TRUE : 'true';

IN : 'in';
OF : 'of';
NEW: 'new';
NOT: 'not';
AROND : '@';
POINT : '.';
COLON : ':';
TILDE : '~';
INHERITS : 'inherits';
ISVOID : 'isvoid';
SELF : 'self';

// define type
TYPE : INTTYPE | STRINGTYPE | BOOLTYPE | OBJTYPE | IOTYPE | 'SELF_TYPE';
INTTYPE : 'Int';
STRINGTYPE : 'String';
BOOLTYPE: 'Bool';
OBJTYPE : 'Object';
IOTYPE : 'IO';

SEMI : ';';
COMMA : ',';

// arithmetics
MINUS : '-';
PLUS : '+';
MULTIPLY : '*';
DIVIDE : '/';
EQUAL : '=';
LESSTHAN : '<';
LESSOREQ : '<=';
ASSIGNING : '=>';
DARROW : '<-';

LEFTPAREN : '(';
RIGHTPAREN : ')';
LEFTBRACE : '{';
RIGHTBRACE : '}';

fragment ESC_SEQ
    : '\\' ('n' | 't' | 'b' | 'f')
    ;

ID : ([a-zA-Z] | '_')([a-zA-Z] | '_' | [0-9])*;

INT : DIGITS;

fragment DIGITS : [0-9]+;
//exponent e^.. : 'e' ('-' | '+')? DIGITS;
FLOAT
    : (DIGITS ('.' DIGITS?)? ('e' ('-' | '+')? DIGITS)?)
    | ('.' DIGITS ('e' ('-' | '+')? DIGITS)?)
    ;

LINE_COMMENT
    :   '--' ~[\r\n]* -> skip
    ;

BLOCK_COMMENT
    : '(*' ( ~[*] ~[)] | BLOCK_COMMENT )*? '*)' { skip(); }
    | '(*' .*? '*)' { raiseError("Unterminated comment"); skip(); }
    ;

INVALID_CHAR : . {
    raiseError("Invalid character: " + getText());
};

//EOF_IN_COMMENT : '(*' ~('\r' | '\n')* EOF {
//    raiseError("ERROR: EOF in comment");
//};
//
//UNTERMINATED_STRING : '"' ~('\r' | '\n')* '\n' {
//    raiseError("Unterminated string constant");
//};
//
//EOF_IN_STRING : '"' ~('\r' | '\n')* EOF '"'{
//    raiseError("EOF in string constant");
//};
//
//UNMATCHED_CLOSE_COMMENT : '*)' {
//    raiseError("Unmatched *)");
//};
//
