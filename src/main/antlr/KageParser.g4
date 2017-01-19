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

statements
    : statement (NEWLINE+ statement)*
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

fnParam
    : Identifier TypeAnnotation
    ;

fnParams
    : fnParam (',' fnParam)*
    ;

fnDeclaration
    : 'fn' fnName=Identifier '(' params=fnParams? ')' typeAnnotation=TypeAnnotation? '=' NEWLINE* body=statementOrExpression
    ;

print
    : 'print' '(' expression ')'
    ;


// Expressions

expression
    : '(' expression ')'                                                        #parenExpression

    | invokee=expression '(' ')'                                                #invocation

    | 'let' NEWLINE+ statements NEWLINE+ 'in' NEWLINE+ statementOrExpression    #letInExpression

    | operator=('-'|'!') expression                                             #unaryOperation

    | left=expression operator=('/'|'*') right=expression                       #binaryOperation
    | left=expression operator=('+'|'-') right=expression                       #binaryOperation
    | left=expression operator=('<'|'<='|'>'|'>=') right=expression             #binaryOperation
    | left=expression operator=('=='|'!=') right=expression                     #binaryOperation
    | left=expression operator=('||'|'&&') right=expression                     #binaryOperation
    | left=expression operator='++' right=expression                            #binaryOperation

    | Identifier                                                                #bindingReference
    | StringLiteral                                                             #stringLiteral
    | IntLiteral                                                                #intLiteral
    | DecimalLiteral                                                            #decLiteral
    | BooleanLiteral                                                            #boolLiteral
    ;

