lexer grammar KageLexer;

// Keywords
VAL             : 'val' ;
PRINT           : 'print' ;

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

// Identifiers
Identifier      : [_]*[a-z][A-Za-z0-9_]* ;

// Separator
LPAREN          : '(' ;
RPAREN          : ')' ;

// Operators
PLUS            : '+' ;
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

