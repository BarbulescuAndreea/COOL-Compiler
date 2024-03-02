//lexer grammar CoolLexer;
//
//tokens { ERROR }
//
//@header{
//    package cool.lexer;
//}
//
//@members{
//    private static final int MAX_STRING_LENGTH = 1024;
//    public static final String EOF_STRING_ERROR = "EOF in string constant";
//    public static final String STRING_CONSTANT_TO_LONG_ERROR = "String constant too long";
//    public static final String INVALID_CHARACTER_ERROR = "Invalid character: ";
//    public static final String EOF_COMMENT_ERROR = "EOF in comment";
//    public static final String UNMATCHED_COMMENT_ERROR = "Unmatched *)";
//    public static final String UNTERMINATED_STRING_CONSTANT_ERROR = "Unterminated string constant";
//    public static final String STRING_NULL_CHARACTER_ERROR = "String contains null character";
//    public static final String NULL_STRING = "\0";
//
//    private void raiseError(String msg) {
//        setText(msg);
//        setType(ERROR);
//    }
//}
//
//IF : 'if';
//THEN : 'then';
//ELSE : 'else';
//FI : 'fi';
//LET : 'let';
//LOOP : 'loop';
//POOL : 'pool';
//WHILE : 'while';
//CASE : 'case';
//ESAC : 'esac';
//CLASS : 'class';
//
//// boolean true & false
//FALSE : 'false';
//TRUE : 'true';
//
//IN : 'in';
//OF : 'of';
//NEW: 'new';
//NOT: 'not';
//AROND : '@';
//POINT : '.';
//COLON : ':';
//TILDE : '~';
//INHERITS : 'inherits';
//ISVOID : 'isvoid';
//SELF : 'self';
//
//SEMI : ';';
//COMMA : ',';
//
//// arithmetics
//MINUS : '-';
//PLUS : '+';
//MULTIPLY : '*';
//DIVIDE : '/';
//EQUAL : '=';
//LESSTHAN : '<';
//LESSOREQ : '<=';
//ASSIGNING : '=>';
//DARROW : '<-';
//
//LEFTPAREN : '(';
//RIGHTPAREN : ')';
//LEFTBRACE : '{';
//RIGHTBRACE : '}';
//QUOTE : '"';
//
//fragment UPPER_CASE_LETTER : [A-Z] ;
//fragment LOWER_CASE_LETTER : [a-z] ;
//
///* type identifier */
//TYPE : UPPER_CASE_LETTER+ (('_') | UPPER_CASE_LETTER | LOWER_CASE_LETTER | DIGIT)* ;
//
//
///* object identifier */
//ID : LOWER_CASE_LETTER+ (('_') | UPPER_CASE_LETTER | LOWER_CASE_LETTER | DIGIT)* ;
//
//fragment NEW_LINE : '\r'? '\n';
//fragment SINGLE_LINE_COMMENT_START : '--' ;
//fragment OPEN_PAREN_STAR : '(*' ;
//fragment CLOSE_PAREN_STAR : '*)' ;
//
//LINE_COMMENT
//    : SINGLE_LINE_COMMENT_START .*? (NEW_LINE | EOF) -> skip
//    ;
//
//BLOCK_COMMENT
//    : OPEN_PAREN_STAR
//      (BLOCK_COMMENT | .)*?
//      (CLOSE_PAREN_STAR { skip(); } | EOF { raiseError(EOF_COMMENT_ERROR); })
//    ;
//
//UNMATCHED_COMMENT : CLOSE_PAREN_STAR { raiseError(UNMATCHED_COMMENT_ERROR); } ;
//
///*
//    Integer
//*/
//fragment DIGIT : [0-9] ;
//INT : DIGIT+ ;
//
//STRING: QUOTE ('\\"' | '\\' NEW_LINE | .)*? (
//	QUOTE {
//		String str = getText().substring(1, getText().length() - 1).replace("\\\r\n", "\r\n").replace("\\\n", "\n").replace("\\n", "\n").replace("\\t", "\t").replace("\\f", "\f").replaceAll("\\\\(?!\\\\)", "");
//
//		if (str.length() > MAX_STRING_LENGTH) {
//			raiseError(STRING_CONSTANT_TO_LONG_ERROR);
//            return;
//        }
//
//		if (str.contains(NULL_STRING)) {
//			raiseError(STRING_NULL_CHARACTER_ERROR);
//		    return;
//		}
//
//		setText(str);
//	}
//	| EOF { raiseError(EOF_STRING_ERROR); }
//	| NEW_LINE { raiseError(UNTERMINATED_STRING_CONSTANT_ERROR); }
//);
//
//WS
//    :   [ \n\f\r\t]+ -> skip
//    ;
//
//INVALID_CHARACTER : . { raiseError(INVALID_CHARACTER_ERROR + getText()); } ;
//
////fragment ESC_SEQ
////    : '\\' ('n' | 't' | 'b' | 'f')
////    ;
////
////INT : DIGITS;
////
////LETTERS: [a-zA-Z];
////
////TYPE: [A-Z]+ ('_' | LETTERS | DIGIT)* ;
////ID: [a-z]+ ('_' | LETTERS | DIGIT)* ;
////fragment DIGITS : [0-9]+;
////fragment DIGIT : [0-9];
////
//////exponent e^.. : 'e' ('-' | '+')? DIGITS;
////// nu exista in Cool
//////FLOAT
//////    : (DIGITS ('.' DIGITS?)? ('e' ('-' | '+')? DIGITS)?) // ex: 1.23 e^10
//////    | ('.' DIGITS ('e' ('-' | '+')? DIGITS)?) // .123
//////    ;
////LINE_COMMENT
////    :   '--' ~[\r\n]* -> skip
////    ;
////
////BLOCK_COMMENT
////// orice caracter inafara de *)
////// (* *) *)
////    : '(*' ( ~[*] ~[)] | BLOCK_COMMENT )*? '*)' { skip(); }
////    | '(*' .*? '*)' { raiseError("Unterminated comment"); skip(); }
////    ;
////
////STRING
////    : '"' (ESC_SEQ | '\\"' | .)*?  // Un șir de caractere începe cu ghilimele duble
////    ('"' {
////            String text = getText();  // Obține textul din șir (inclusiv ghilimelele)
////            // Șterge ghilimelele de la început și sfârșit
////            text = text.substring(1, text.length() - 1);
////
////            StringBuilder processedText = new StringBuilder();
////            int val = 0;
////            boolean escapeMode = false;
////            char[] charArray = text.toCharArray(); // itereaza mai ușor prin caracterele unui șir
////            while (val < charArray.length) {
////                char c = charArray[val];
////                if (escapeMode) {
////                    // Tratează caracterele de escape (\n, \t, \b, \f)
////                    if (c == 'n') {
////                        processedText.append('\n');
////                    } else if (c == 't') {
////                        processedText.append('\t');
////                    } else if (c == 'b') {
////                        processedText.append('\b');
////                    } else if (c == 'f') {
////                        processedText.append('\f');
////                    } else {
////                        processedText.append(c);
////                    }
////                    escapeMode = false;
////                } else if (c == '\\') {
////                    escapeMode = true;  // Activează modul escape la întâlnirea caracterului '\'
////                } else {
////                    processedText.append(c);  // Adaugă caracterul la șirul procesat
////                }
////                val++;
////            }
////            int textLength = text.length();
////            if (textLength > 1024) {
////                raiseError("String constant too long");  // Ridică o eroare dacă șirul este prea lung
////                return;
////            }
////            if (text.contains("\u0000")) {
////                raiseError("String contains null character");  // Ridică o eroare dacă șirul conține caracter nul
////                return;
////            }
////            setText(processedText.toString()); // Setează textul modificat ca text al tokenului
////        }
////        | EOF {raiseError("EOF in string constant");}  // Ridică o eroare dacă întâlnim sfârșitul fișierului în șir
////    )
////    ;
////
////WS
////    :  [ \n\f\r\t]+ -> skip
////    ;
////
////INVALID_CHAR : . {
////    raiseError("Invalid character: " + getText());
////};
//
//
//
////EOF_IN_COMMENT : '(*' ~('\r' | '\n')* EOF {
////    raiseError("ERROR: EOF in comment");
////};
////
////UNTERMINATED_STRING : '"' ~('\r' | '\n')* '\n' {
////    raiseError("Unterminated string constant");
////};
////
////EOF_IN_STRING : '"' ~('\r' | '\n')* EOF '"'{
////    raiseError("EOF in string constant");
////};
////
////UNMATCHED_CLOSE_COMMENT : '*)' {
////    raiseError("Unmatched *)");
////};
////
//lexer grammar CoolLexer;
//
//tokens { ERROR }
//
//@header{
//    package cool.lexer;
//}
//
//@members{
//    private void raiseError(String msg) {
//        setText(msg);
//        setType(ERROR);
//    }
//}
//
//WS
//    :   [ \n\f\r\t]+ -> skip
//    ;
//
//IF : 'if';
//THEN : 'then';
//ELSE : 'else';
//FI : 'fi';
//CLASS : 'class';
//FALSE : 'false';
//TRUE : 'true';
//IN : 'in';
//INHERITS : 'inherits';
//ISVOID : 'isvoid';
//LET : 'let';
//LOOP : 'loop';
//POOL : 'pool';
//WHILE : 'while';
//CASE : 'case';
//ESAC : 'esac';
//NEW : 'new';
//OF : 'of';
//NOT : 'not';
//AT : '@';
//DOT : '.';
//COLON : ':';
//TILDE : '~';
//TYPE : 'Int' | 'Bool' | 'String' | 'Object' | 'IO' | 'SELF_TYPE';
//
//SEMI : ';';
//COMMA : ',';
//LPAREN : '(';
//RPAREN : ')';
//LBRACE : '{';
//RBRACE : '}';
//PLUS : '+';
//MINUS : '-';
//MULT : '*';
//DIV : '/';
//EQUAL : '=';
//LT : '<';
//LE : '<=';
//RE : '=>';
//DARROW : '<-';
//
//fragment ESC_SEQ
//    : '\\' ('n' | 't' | 'b' | 'f')
//    ;
//
//
//fragment LETTER : [a-zA-Z];
//ID : (LETTER | '')(LETTER | '' | DIGIT)*;
//
//fragment DIGIT : [0-9];
//INT : DIGIT+;
//
//fragment DIGITS : DIGIT+;
//fragment EXPONENT : 'e' ('+' | '-')? DIGITS;
//FLOAT : (DIGITS ('.' DIGITS?)? | '.' DIGITS) EXPONENT?;
//
//STRING
//    : '"' (ESC_SEQ | '\\"' | .)*?
//    ('"' {
//            String text = getText();
//            text = text.substring(1, text.length() - 1);
//
//            StringBuilder processedText = new StringBuilder();
//            boolean escapeMode = false;
//            for (char c : text.toCharArray()) {
//                if (escapeMode) {
//                    switch (c) {
//                        case 'n':
//                            processedText.append('\n');
//                            break;
//                        case 't':
//                            processedText.append('\t');
//                            break;
//                        case 'b':
//                            processedText.append('\b');
//                            break;
//                        case 'f':
//                            processedText.append('\f');
//                            break;
//                        default:
//                            processedText.append(c);
//                            break;
//                    }
//                    escapeMode = false;
//                } else if (c == '\\') {
//                    escapeMode = true;
//                } else {
//                    processedText.append(c);
//                }
//            }
//            setText(processedText.toString());
//        }
//        )
//    ;
//
//LINE_COMMENT
//    :   '--' ~[\r\n]* -> skip
//    ;
//
//BLOCK_COMMENT
//    : '(*'
//      (BLOCK_COMMENT | .)*?
//      ('*)' | EOF { raiseError("EOF in comment"); }) -> skip
//    ;
//
//INVALID_CHAR : . {
//    raiseError("Invalid character: " + getText());
//};
//

lexer grammar CoolLexer;

tokens { ERROR }

@header{
    package cool.lexer;
}

@members{
    private static final int MAX_STRING_LENGTH = 1024;
    public static final String EOF_STRING_ERROR = "EOF in string constant";
    public static final String STRING_CONSTANT_TO_LONG_ERROR = "String constant too long";
    public static final String INVALID_CHARACTER_ERROR = "Invalid character: ";
    public static final String EOF_COMMENT_ERROR = "EOF in comment";
    public static final String UNMATCHED_COMMENT_ERROR = "Unmatched *)";
    public static final String UNTERMINATED_STRING_CONSTANT_ERROR = "Unterminated string constant";
    public static final String STRING_NULL_CHARACTER_ERROR = "String contains null character";
    public static final String NULL_STRING = "\0";

    private void raiseError(String msg) {
        setText(msg);
        setType(ERROR);
    }
}

IF : 'if';
THEN : 'then';
ELSE : 'else';
FI : 'fi';
CLASS : 'class';
FALSE : 'false';
TRUE : 'true';
IN : 'in';
INHERITS : 'inherits';
ISVOID : 'isvoid';
LET : 'let';
LOOP : 'loop';
POOL : 'pool';
WHILE : 'while';
CASE : 'case';
ESAC : 'esac';
NEW : 'new';
OF : 'of';
NOT : 'not';

/* parentheses */
LPAREN : '(' ;
RPAREN : ')' ;

/* braces */
LBRACE : '{' ;
RBRACE : '}' ;

COMMA : ',' ;
COLON : ':' ;
SEMI : ';' ;

ASSIGN : '<-' ;
RESULTS_CASE : '=>' ;

PLUS : '+' ;
MINUS : '-' ;
MULTIPLY : '*' ;
DIVIDE : '/' ;
TILDA : '~' ;

LT : '<' ;
LE : '<=' ;
EQ : '=' ;
DOT : '.';
AT : '@';

QUOTE : '"';

fragment UPPER_CASE_LETTER : [A-Z] ;
fragment LOWER_CASE_LETTER : [a-z] ;

///Identifiers/

/* type identifier */
TYPE : UPPER_CASE_LETTER+ (('_') | UPPER_CASE_LETTER | LOWER_CASE_LETTER | DIGIT)* ;


/* object identifier */
ID : LOWER_CASE_LETTER+ (('_') | UPPER_CASE_LETTER | LOWER_CASE_LETTER | DIGIT)* ;

fragment NEW_LINE : '\r'? '\n';
fragment SINGLE_LINE_COMMENT_START : '--' ;
fragment OPEN_PAREN_STAR : '(*' ;
fragment CLOSE_PAREN_STAR : '*)' ;

LINE_COMMENT
    : SINGLE_LINE_COMMENT_START .*? (NEW_LINE | EOF) -> skip
    ;

BLOCK_COMMENT
    : OPEN_PAREN_STAR
      (BLOCK_COMMENT | .)*?
      (CLOSE_PAREN_STAR { skip(); } | EOF { raiseError(EOF_COMMENT_ERROR); })
    ;

UNMATCHED_COMMENT : CLOSE_PAREN_STAR { raiseError(UNMATCHED_COMMENT_ERROR); } ;

/*
    Integer
*/
fragment DIGIT : [0-9] ;
INT : DIGIT+ ;

STRING: QUOTE ('\\"' | '\\' NEW_LINE | .)*? (
	QUOTE {
		String str = getText().substring(1, getText().length() - 1).replace("\\\r\n", "\r\n").replace("\\\n", "\n").replace("\\n", "\n").replace("\\t", "\t").replace("\\f", "\f").replaceAll("\\\\(?!\\\\)", "");

		if (str.length() > MAX_STRING_LENGTH) {
			raiseError(STRING_CONSTANT_TO_LONG_ERROR);
            return;
        }

		if (str.contains(NULL_STRING)) {
			raiseError(STRING_NULL_CHARACTER_ERROR);
		    return;
		}

		setText(str);
	}
	| EOF { raiseError(EOF_STRING_ERROR); }
	| NEW_LINE { raiseError(UNTERMINATED_STRING_CONSTANT_ERROR); }
);

WS
    :   [ \n\f\r\t]+ -> skip
    ;

INVALID_CHARACTER : . { raiseError(INVALID_CHARACTER_ERROR + getText()); } ;