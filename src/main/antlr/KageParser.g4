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
    | fnDeclaration   #fnDeclarationStatement
    | print           #printStatement
    ;

valDeclaration
    : 'val' Identifier typeAnnotation=TypeAnnotation? '=' expression
    ;

fnDeclaration
    : 'fn' fnName=Identifier '(' ')' '=' body=expression
    ;

print
    : 'print' '(' expression ')'
    ;


// Expressions

expression
    : '(' expression ')'                                             #parenExpression

    | invokee=expression '(' ')'                                     #invocation

    | operator=('-'|'!') expression                                  #unaryOperation

    | left=expression operator=('/'|'*') right=expression            #binaryOperation
    | left=expression operator=('+'|'-') right=expression            #binaryOperation
    | left=expression operator=('||'|'&&') right=expression          #binaryOperation
    | left=expression operator='++' right=expression                 #binaryOperation

    | Identifier                                                     #bindingReference
    | StringLiteral                                                  #stringLiteral
    | IntLiteral                                                     #intLiteral
    | DecimalLiteral                                                 #decLiteral
    | BooleanLiteral                                                 #boolLiteral
    ;

