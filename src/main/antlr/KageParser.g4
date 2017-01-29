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

typeProp
    : prop=Identifier typeAnnotation=TypeAnnotation
    ;

typeProps
    : typeProp (',' NEWLINE* typeProp)*
    ;

typePropsBlock
    : '{' NEWLINE* props=typeProps NEWLINE* '}'
    ;

statement
    : valDeclaration                                                                    #valDeclarationStatement
    | fnDeclaration                                                                     #fnDeclarationStatement
    | 'type' name=Identifier props=typePropsBlock?                                      #typeDeclarationStatement
    | print                                                                             #printStatement
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

expressions
    : expression (',' expression)*
    ;

fnDeclaration
    : 'fn' fnName=Identifier '(' params=fnParams? ')' typeAnnotation=TypeAnnotation? '=' NEWLINE* body=statementOrExpression
    ;

print
    : 'print' '(' expression ')'
    ;


// Expressions

expression
    : '(' NEWLINE* expression NEWLINE* ')'                                              #parenExpression

    | target=expression '.' prop=Identifier                                             #dotExpression

    | invokee=expression '(' params=expressions? ')'                                    #invocation

    | 'let' NEWLINE+ statements NEWLINE+ 'in' NEWLINE+ statementOrExpression            #letInExpression

    | operator=('-'|'!') expression                                                     #unaryOperation

    | left=expression operator=('/'|'*') right=expression                               #binaryOperation
    | left=expression operator=('+'|'-') right=expression                               #binaryOperation
    | left=expression operator='++' right=expression                                    #binaryOperation
    | left=expression operator=('<'|'<='|'>'|'>=') right=expression                     #binaryOperation
    | left=expression operator=('=='|'!=') right=expression                             #binaryOperation
    | left=expression operator=('||'|'&&') right=expression                             #binaryOperation

    | 'if' cond=expression NEWLINE* 'then' NEWLINE* thenBody=statementOrExpression
        (NEWLINE* 'else' NEWLINE* elseBody=statementOrExpression)?                      #ifThenElseExpression

    | Identifier                                                                        #bindingReference
    | StringLiteral                                                                     #stringLiteral
    | IntLiteral                                                                        #intLiteral
    | DecimalLiteral                                                                    #decLiteral
    | BooleanLiteral                                                                    #boolLiteral

    | '(' items=expressions ')'                                                         #tupleLiteral
    ;

