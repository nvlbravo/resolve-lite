/*
 * [The "BSD license"]
 * Copyright (c) 2015 Clemson University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
grammar Resolve;

module
    :   precisModule
    |   conceptModule
    ;

// precis module

precisModule
    :   'Precis' name=Identifier ';'
        (importList)?
        (precisItems)?
        'end' closename=Identifier ';' EOF
    ;

precisItems
    :   (precisItem)+
    ;

precisItem
    :   mathDefinitionDecl
    ;

// concept module

conceptModule
    :   'Concept' name=Identifier (moduleParameterList)? ';'
        (importList)?
        (requiresClause)?
        (conceptItems)?
        'end' closename=Identifier ';' EOF
    ;

conceptItems
    :   (conceptItem)+
    ;

conceptItem
    :   operationDecl
    |   typeModelDecl
    |   mathDefinitionDecl
    ;

// uses, imports

importList
    :   'uses' Identifier (',' Identifier)* ';'
    ;

// parameter related rules

operationParameterList
    :   '(' (parameterDeclGroup (';' parameterDeclGroup)*)?  ')'
    ;

moduleParameterList
    :   '(' moduleParameterDecl (';' moduleParameterDecl)* ')'
    ;

moduleParameterDecl
    :   typeParameterDecl
    |   parameterDeclGroup
    ;

typeParameterDecl
    :   'type' name=Identifier
    ;

parameterDeclGroup
    :   Identifier (',' Identifier)* ':' type
    ;

parameterMode
    :   ( 'alters'
        | 'updates'
        | 'clears'
        | 'restores'
        | 'preserves'
        | 'replaces'
        | 'evaluates' )
    ;

// type and record related rules

type
    :   (qualifier=Identifier '::')? name=Identifier
    ;

typeModelDecl
    :   'Type' 'Family' name=Identifier 'is' 'modeled' 'by' mathTypeExp ';'
        'exemplar' exemplar=Identifier ';'
        (constraintClause)?
        (typeModelInit)?
        (typeModelFinal)?
    ;

// initialization, finalization rules

typeModelInit
    :   'initialization' (requiresClause)? (ensuresClause)?
    ;

typeModelFinal
    :   'finalization' (requiresClause)? (ensuresClause)?
    ;

// functions

operationDecl
    :   ('Operation'|'Oper') name=Identifier operationParameterList
        (':' type)? ';' (requiresClause)? (ensuresClause)?
    ;

// variable declarations

mathVariableDeclGroup
    :   Identifier (',' Identifier)* ':' mathTypeExp
    ;

mathVariableDecl
    :   Identifier ':' mathTypeExp
    ;

// mathematical theorems, corollaries, etc

mathTheoremDecl
    :   ('Theorem'|'Lemma'|'Corollary') name=Identifier
        ':' mathAssertionExp ';'
    ;

// mathematical definitions

mathDefinitionDecl
    :   mathStandardDefinitionDecl
    |   mathInductiveDefinitionDecl
    ;

mathInductiveDefinitionDecl
    :   'Inductive' 'Definition' inductiveDefinitionSignature
        'is' '(i.)' mathAssertionExp ';' '(ii.)' mathAssertionExp ';'
    ;

mathStandardDefinitionDecl
    :   'Definition' definitionSignature ('is' mathAssertionExp)? ';'
    ;

inductiveDefinitionSignature
    :   inductivePrefixSignature
    |   inductiveInfixSignature
    ;

inductivePrefixSignature
    :   'on' mathVariableDecl 'of' prefixOp
        '(' (inductiveParameterList ',')? Identifier ')' ':' mathTypeExp
    ;

inductiveInfixSignature
    :   'on' mathVariableDecl 'of' '(' mathVariableDecl ')' infixOp
        '(' Identifier ')' ':' mathTypeExp
    ;

inductiveParameterList
    :   mathVariableDeclGroup (',' mathVariableDeclGroup)*
    ;

definitionSignature
    :   standardInfixSignature
    |   standardOutfixSignature
    |   standardPrefixSignature
    ;

standardInfixSignature
    :   '(' mathVariableDecl ')'
        infixOp
        '(' mathVariableDecl ')' ':' mathTypeExp
    ;

standardOutfixSignature
    :   ( lOp='|'  '(' mathVariableDecl ')' rOp='|'
    |     lOp='||' '(' mathVariableDecl ')' rOp='||'
    |     lOp='<'  '(' mathVariableDecl ')' rOp='>') ':' mathTypeExp
    ;

standardPrefixSignature
    :   prefixOp (definitionParameterList)? ':' mathTypeExp
    ;

prefixOp
    :   infixOp
    |   IntegerLiteral
    ;

infixOp
    :   ('implies'|'+'|'o'|'-'|'/'|'*'|'..'|'and'|'or')
    |   ('union'|'intersect'|'is_in'|'is_not_in'|'>'|'<'|'>='|'<=')
    |   Identifier
    ;

definitionParameterList
    :   '(' mathVariableDeclGroup (',' mathVariableDeclGroup)* ')'
    ;

// mathematical clauses

affectsClause
    :   parameterMode Identifier (',' Identifier)*
    ;

requiresClause
    :   'requires' mathAssertionExp ';'
    ;

ensuresClause
    :   'ensures' mathAssertionExp ';'
    ;

constraintClause
    :   ('constraint'|'Constraint') mathAssertionExp ';'
    ;

changingClause
    :   'changing' progVariableExp (',' progVariableExp)*
    ;

maintainingClause
    :   'maintaining' mathAssertionExp ';'
    ;

decreasingClause
    :   'decreasing' mathAssertionExp ';'
    ;

whereClause
    :   'where' mathAssertionExp
    ;

correspondenceClause
    :   'correspondence' mathAssertionExp ';'
    ;

conventionClause
    :   'convention' mathAssertionExp ';'
    ;

// mathematical expressions

mathTypeExp
    :   mathExp
    ;

mathAssertionExp
    :   mathExp
    |   mathQuantifiedExp
    ;

mathQuantifiedExp
    :   'For' 'all' mathVariableDeclGroup (whereClause)? ','
         mathAssertionExp
    ;

mathExp
    :   mathPrimaryExp                                  #mathPrimeExp
    |   op=('+'|'-'|'~'|'not') mathExp                  #mathUnaryExp
    |   mathExp op=('*'|'/'|'~') mathExp                #mathInfixExp
    |   mathExp op=('+'|'-') mathExp                    #mathInfixExp
    |   mathExp op=('..'|'->') mathExp                  #mathInfixExp
    |   mathExp op=('o'|'union'|'intersect') mathExp    #mathInfixExp
    |   mathExp op=('is_in'|'is_not_in') mathExp        #mathInfixExp
    |   mathExp op=('<='|'>='|'>'|'<') mathExp          #mathInfixExp
    |   mathExp op=('='|'/=') mathExp                   #mathInfixExp
    |   mathExp op='implies' mathExp                    #mathInfixExp
    |   mathExp op=('and'|'or') mathExp                 #mathInfixExp
    |   mathExp (':') mathExp                           #mathTypeAssertExp
    |   '(' mathAssertionExp ')'                        #mathNestedExp
    ;

mathPrimaryExp
    :   mathLiteralExp
    |   mathDotExp
    |   mathFunctionApplicationExp
    |   mathOutfixExp
    |   mathSetExp
    |   mathTupleExp
    |   mathLambdaExp
    ;

mathLiteralExp
    :   BooleanLiteral      #mathBooleanExp
    |   IntegerLiteral      #mathIntegerExp
    ;

mathDotExp
    :   mathFunctionApplicationExp ('.' mathFunctionApplicationExp)+
    ;

mathFunctionApplicationExp
    :   '#' mathCleanFunctionExp
    |   mathCleanFunctionExp
    ;

mathCleanFunctionExp
    :   name=Identifier '(' mathExp (',' mathExp)* ')'  #mathFunctionExp
    |   (qualifier=Identifier '::')? name=Identifier    #mathVariableExp
    |   ('+'|'-'|'*'|'/')                               #mathOpExp
    ;

mathOutfixExp
    :   lop='<' mathExp rop='>'
    |   lop='|' mathExp rop='|'
    |   lop='||' mathExp rop='||'
    ;

mathSetExp
    :   '{' mathVariableDecl '|' mathAssertionExp '}'   #mathSetBuilderExp//Todo
    |   '{' (mathExp (',' mathExp)*)? '}'               #mathSetCollectionExp
    ;

mathTupleExp
    :   '(' mathExp (',' mathExp)+ ')'
    ;

//NOTE: Allows only very rudimentary lambda expressions.

mathLambdaExp
    :   'lambda' '(' mathVariableDeclGroup (',' mathVariableDeclGroup)* ')'
        '.' '(' mathAssertionExp ')'
    ;

// program expressions

progExp
    :   op=('not'|'-') progExp                  #progApplicationExp
    |   progExp op=('*'|'/') progExp            #progApplicationExp
    |   progExp op=('+'|'-') progExp            #progApplicationExp
    |   progExp op=('<='|'>='|'>'|'<') progExp  #progApplicationExp
    |   progExp op=('='|'/=') progExp           #progApplicationExp
    |   '(' progExp ')'                         #progNestedExp
    |   progPrimary                             #progPrimaryExp
    ;

progPrimary
    :   progLiteralExp
    |   progVariableExp
    |   progParamExp
    ;

//This intermediate rule is really only needed to help make
//the 'changingClause' rule a little more strict about what it accepts.
//A root VariableExp class is no longer reflected in the ast.
progVariableExp
    :   progDotExp
    |   progNamedExp
    ;

progDotExp
    :   progNamedExp ('.' progNamedExp)+
    ;

progParamExp
    :   (qualifier=Identifier '::')? name=Identifier
        '(' (progExp (',' progExp)*)? ')'
    ;

progNamedExp
    :   (qualifier=Identifier '::')? name=Identifier
    ;

progLiteralExp
    :   IntegerLiteral      #progIntegerExp
    |   CharacterLiteral    #progCharacterExp
    |   StringLiteral       #progStringExp
    ;

// literal rules and fragments

BooleanLiteral
    :   'true'
    |   'false'
    |   'B'
    ;

IntegerLiteral
    :   DecimalIntegerLiteral
    ;

CharacterLiteral
    :   '\'' SingleCharacter '\''
    ;

StringLiteral
    :   '\"' StringCharacters? '\"'
    ;

fragment
StringCharacters
    :   StringCharacter+
    ;

fragment
StringCharacter
    :   ~["\\]
    ;

fragment
DecimalIntegerLiteral
    :   '0'
    |   NonZeroDigit (Digits)?
    ;

fragment
Digits
    :   Digit (Digit)*
    ;

fragment
Digit
    :   '0'
    |   NonZeroDigit
    ;

fragment
NonZeroDigit
    :   [1-9]
    ;

fragment
SingleCharacter
    :   ~['\\]
    ;

// Some lexer tokens (allows for easy switch stmts)

Not      : 'not';
Or       : 'and';
And      : 'or';
NEquals   : '/=';
Equals   : '=';
GTEquals : '>=';
LTEquals : '<=';
GT       : '>';
LT       : '<';
Add      : '+';
Subtract : '-';
Multiply : '*';
Divide   : '/';

// whitespace, identifier rules, and comments

Identifier
    :   Letter LetterOrDigit*
    ;

Letter
    :   [a-zA-Z$_]
    ;

LetterOrDigit
    :   [a-zA-Z0-9$_]
    ;

SPACE
    :  [ \t\r\n\u000C]+ -> skip
    ;

COMMENT
    :   '(*' .*? '*)' -> skip
    ;

LINE_COMMENT
    :   '--' ~[\r\n]* -> skip
    ;