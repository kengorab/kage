parser grammar KageParser;

options { tokenVocab=KageLexer; }

kageFile
    : lines=line+
    ;

line
    : statementOrExpression (NEWLINE+ | EOF)
    ;

statementOrExpression
    : statement
    | expression
    ;

// Statements

statement
    : valDeclaration  #valDeclarationStatement
    | print           #printStatement ;

valDeclaration
    : 'val' Identifier typeAnnotation=TypeAnnotation? '=' expression ;

print
    : 'print' '(' expression ')' ;


// Expressions

expression
    : '(' expression ')'                                             #parenExpression
    | operator=('-'|'!') expression                                  #unaryOperation
    | left=expression operator=('/'|'*') right=expression            #binaryOperation
    | left=expression operator=('+'|'-') right=expression            #binaryOperation
    | left=expression operator=('||'|'&&') right=expression          #binaryOperation
    | left=expression operator='++' right=expression                 #binaryOperation
    | Identifier                                                     #bindingReference
    | StringLiteral                                                  #stringLiteral
    | IntLiteral                                                     #intLiteral
    | DecimalLiteral                                                 #decLiteral
    | BooleanLiteral                                                 #boolLiteral ;
