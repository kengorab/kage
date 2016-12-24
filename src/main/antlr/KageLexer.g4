lexer grammar KageLexer;

// Whitespace
NEWLINE         : '\r\n' | '\r' | '\n' ;
WS              : [\t ]+ -> skip ;

// Keywords
VAL             : 'val' ;
PRINT           : 'print' ;

// Literals
INTLIT          : '0'|[1-9][0-9]* ;
DECLIT          : '0'|[1-9][0-9]* '.' [0-9]+ ;
BOOLLIT         : 'true'|'false' ;

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

// Tokens
LPAREN          : '(' ;
RPAREN          : ')' ;

// Identifiers
ID              : [_]*[a-z][A-Za-z0-9_]* ;
