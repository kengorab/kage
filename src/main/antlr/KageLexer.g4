lexer grammar KageLexer;

// Keywords
VAL             : 'val' ;
PRINT           : 'print' ;
FN              : 'fn' ;
LET             : 'let' ;
IN              : 'in' ;

// Integer literals
IntLiteral
    : '0'|[1-9][0-9]*
    ;

// Decimal literals
DecimalLiteral
    : '0'|[1-9][0-9]* '.' [0-9]+
    ;

// Boolean literals
BooleanLiteral
    : 'true'
    | 'false'
    ;

// String literals
StringLiteral
    : '"' StringCharacters? '"'
    ;

fragment
StringCharacters
    : StringCharacter+
    ;

fragment
StringCharacter
    : ~["\\]
    | EscapeSequence
    ;

fragment
EscapeSequence
    : '\\' [btnr"\\]
    ;

// Identifiers
Identifier
    :   JavaLetter JavaLetterOrDigit*
    ;

fragment
JavaLetter
    :   [a-zA-Z$_] // these are the "java letters" below 0x7F
    |   // covers all characters above 0x7F which are not a surrogate
        ~[\u0000-\u007F\uD800-\uDBFF]
    |   // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
        [\uD800-\uDBFF] [\uDC00-\uDFFF]
    ;

fragment
JavaLetterOrDigit
    :   [a-zA-Z0-9$_] // these are the "java letters or digits" below 0x7F
    |   // covers all characters above 0x7F which are not a surrogate
        ~[\u0000-\u007F\uD800-\uDBFF]
    |   // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
        [\uD800-\uDBFF] [\uDC00-\uDFFF]
    ;

TypeAnnotation
    : ':' WS* Identifier
    ;

// Separator
LPAREN          : '(' ;
RPAREN          : ')' ;
LBRACE          : '{' ;
RBRACE          : '}' ;
COLON           : ':' ;
COMMA           : ',' ;

// Operators
PLUS            : '+' ;
CONCAT          : '++' ;
MINUS           : '-' ;
ASTERISK        : '*' ;
DIVISION        : '/' ;
ASSIGN          : '=' ;
BANG            : '!' ;
PIPE            : '|' ;
PIPES           : '||' ;
AMP             : '&' ;
AMPS            : '&&' ;

// Whitespace
NEWLINE         : '\r\n' | '\r' | '\n' ;
WS              : [\t ]+ -> skip ;

