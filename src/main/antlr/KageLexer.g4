lexer grammar KageLexer;

// Whitespace
NEWLINE         : '\r\n' | '\r' | '\n' ;
WS              : [\t ]+ -> skip ;

// Keywords
VAR             : 'var' ;   // Replace var with val, encouraging immutability
VAL             : 'val' ;   // I may come back, remove val and (the eventual `fn` and just have haskell-style decls)
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
LPAREN          : '(' ;
RPAREN          : ')' ;
BANG            : '!' ;
PIPE            : '|' ;
PIPES           : '||' ;
AMP             : '&' ;
AMPS            : '&&' ;

// Identifiers
ID              : [_]*[a-z][A-Za-z0-9_]* ;
