parser grammar KageParser;

options { tokenVocab=KageLexer; }

kageFile
    : lines=line+ ;

line
    : statement (NEWLINE+ | EOF) ;


// Statements

statement
    : valDeclaration  #valDeclarationStatement
    | print           #printStatement ;

valDeclaration
    : 'val' Identifier '=' expression ;

print
    : 'print' '(' expression ')' ;


// Expressions

expression
    : '(' expression ')'                                             #parenExpression
    | operator=('-'|'!') expression                                  #unaryOperation
    | left=expression operator=('/'|'*') right=expression            #binaryOperation
    | left=expression operator=('+'|'-') right=expression            #binaryOperation
    | left=expression operator=('||'|'&&') right=expression          #binaryOperation
    | Identifier                                                     #bindingReference
    | StringLiteral                                                  #stringLiteral
    | IntLiteral                                                     #intLiteral
    | DecimalLiteral                                                 #decLiteral
    | BooleanLiteral                                                 #boolLiteral ;
