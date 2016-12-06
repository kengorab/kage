parser grammar KageParser;

options { tokenVocab=KageLexer; }

kageFile : lines=line+ ;

line : statement (NEWLINE+ | EOF) ;

statement : varDeclaration  #varDeclarationStatement
          | assignment      #assignmentStatement
          | print           #printStatement ;

print : PRINT LPAREN expression RPAREN ;

varDeclaration : VAR assignment ;

assignment : ID ASSIGN expression ;

expression : left=expression operator=(DIVISION|ASTERISK) right=expression  #binaryOperation
           | left=expression operator=(PLUS|MINUS) right=expression         #binaryOperation
           | left=expression operator=(PIPES|AMPS) right=expression         #binaryOperation
           | LPAREN expression RPAREN                                       #parenExpression
           | ID                                                             #varReference
           | MINUS expression                                               #minusExpression
           | INTLIT                                                         #intLiteral
           | DECLIT                                                         #decLiteral
           | BOOLLIT                                                        #boolLiteral ;
