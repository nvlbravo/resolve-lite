grammar Resolve;

moduleDecl
    :   (precisModuleDecl
    |   precisExtModuleDecl
    |   conceptExtModuleDecl
    |   conceptModuleDecl
    |   conceptImplModuleDecl
    |   conceptExtImplModuleDecl
    |   facilityModuleDecl
    |   shortFacilityModuleDecl) EOF
    ;

precisModuleDecl
    :   'Precis' name=ID ';'
        (usesList)?
        precisBlock
        'end' closename=ID ';' EOF
    ;

precisExtModuleDecl
    :   'Extension' name=ID 'for' precis=ID
        ('with' precisExt=ID)? ';'
        (usesList)?
        precisBlock
        'end' closename=ID ';'
    ;

conceptModuleDecl
    :   'Concept' name=ID specModuleParameterList? ';'
        (usesList)?
        (requiresClause)?
        conceptBlock
        'end' closename=ID ';'
    ;

conceptExtModuleDecl
    :   'Enhancement' name=ID specModuleParameterList?
        'for' concept=ID ';'
        (usesList)?
        conceptBlock
        'end' closename=ID ';'
    ;

conceptImplModuleDecl
    :   'Realization' name=ID implModuleParameterList?
        'for' concept=ID ';'
        (usesList)?
        implBlock
        'end' closename=ID ';'
    ;

conceptExtImplModuleDecl
    :   'Realization' name=ID implModuleParameterList?
        'for' extension=ID 'of' concept=ID ';'
        (usesList)?
        implBlock
        'end' closename=ID ';'
    ;

facilityModuleDecl
    :   'Facility' name=ID ';'
         (usesList)?
         (requiresClause)?
         facilityBlock
        'end' closename=ID ';'
    ;

shortFacilityModuleDecl
    :   facilityDecl
    ;

// uses, imports

usesList
    :   'uses' (usesSpecs|'(' usesSpecs ')') ';'
    ;

usesSpecs
    :   moduleIdentifierSpec (',' moduleIdentifierSpec)*
    ;

moduleIdentifierSpec : id=ID fromClause? aliasClause? ;
fromClause : 'from' moduleLibraryIdentifier ;
aliasClause : 'as' ID ;
moduleLibraryIdentifier : ID ('.' ID)* ;

// module blocks & items

precisBlock
    :   ( mathStandardDefnDecl
        | mathCategoricalDefnDecl
        | mathInductiveDefnDecl
        | mathTheoremDecl
        | mathClssftnAssertionDecl
        )*
    ;

conceptBlock
    :   ( mathStandardDefnDecl
        | typeModelDecl
        | operationDecl
        | constraintsClause
        )*
    ;

implBlock
    :   ( operationProcedureDecl
        | procedureDecl
        | typeRepresentationDecl
        | facilityDecl
        )*
    ;

facilityBlock
    :   ( facilityDecl
        | operationProcedureDecl
        | typeRepresentationDecl
        | mathStandardDefnDecl
        )*
    ;

// type refs & decls

type
    :   (qualifier=ID '::')? name=ID            #namedType
    |    'Record' (recordVarDeclGroup)* 'end'   #recordType
    ;

typeModelDecl
    :   'Type' 'family' name=ID 'is' 'modeled' 'by' mathClssftnExp ';'?
        'exemplar' exemplar=ID ';'?
        (constraintsClause)?
        (initializationClause)?
    ;

typeRepresentationDecl
    :   'Type' name=ID '=' type ';'?
        (conventionsClause)?
        (correspondenceClause)?
        (typeImplInit)?
    ;

// type initialization rules

specModuleInit
    :   'Facility_Init' (requiresClause)? (ensuresClause)?
    ;

typeImplInit
    :   'initialization'
        (varDeclGroup)* (stmt)*
        'end' ';'
    ;

// parameter and parameter-list related rules

specModuleParameterList
    :   '(' specModuleParameterDecl (';' specModuleParameterDecl)* ')'
    ;

implModuleParameterList
    :   '(' implModuleParameterDecl (';' implModuleParameterDecl)* ')'
    ;

operationParameterList
    :   '(' (parameterDeclGroup (';' parameterDeclGroup)*)?  ')'
    ;

specModuleParameterDecl
    :   parameterDeclGroup
    |   mathStandardDefnDecl
    |   genericTypeParameterDecl
    ;

implModuleParameterDecl
    :   parameterDeclGroup
   // |   operationDecl
    ;

parameterDeclGroup
    :   parameterMode ID (',' ID)* ':' type
    ;

genericTypeParameterDecl
    :   'type' name=ID
    ;

parameterMode
    :   ( 'alters'
        | 'updates'
        | 'clears'
        | 'restores'
        | 'preserves'
        | 'replaces'
        | 'evaluates'
        | ID
        )
    ;

// prog variable decls

recordVarDeclGroup
    :   ID (',' ID)* ':' type ';'?
    ;

varDeclGroup
    :   'Var' ID (',' ID)* ':' type ';'
    ;

// facility decls

facilityDecl
    :   'Facility' name=ID 'is' spec=ID (specArgs=moduleArgumentList)? specFrom=fromClause?
        (externally='externally')? 'realized' 'by' impl=ID
        (implArgs=moduleArgumentList)? implFrom=fromClause?
        (extensionPairing)* ';'?
    ;

extensionPairing
    :   'extended' 'by' spec=ID (specArgs=moduleArgumentList)? specFrom=fromClause?
        (externally='externally')? 'implemented' 'by' impl=ID
        (implArgs=moduleArgumentList)? implFrom=fromClause?
    ;

moduleArgumentList
    :   '(' progExp (',' progExp)* ')'
    ;

// operations & procedures

operationDecl
    :   ('Infix'|'Postfix')? 'Operation' name=ID alt=progSymbolName? operationParameterList (':' type)? ';'
        (requiresClause)? (ensuresClause)?
    ;

operationProcedureDecl
    :   'Operation' name=ID operationParameterList (':' type)? ';'
        (requiresClause)?
        (ensuresClause)?
        (recursive='Recursive')? 'Procedure'
        (noticeClause)*
        (varDeclGroup)*
        (stmt)*
        'end' closename=ID ';'
    ;

procedureDecl
    :   (recursive='Recursive')? 'Procedure' name=ID operationParameterList
        (':' type)? ';'
        (noticeClause)*
        (varDeclGroup)*
        (stmt)*
        'end' closename=ID ';'
    ;

// statements

stmt
    :   assignStmt
    |   swapStmt
    |   callStmt
    |   whileStmt
    |   ifStmt
    ;

assignStmt : left=progExp ':=' right=progExp ';' ;
swapStmt : left=progExp ':=:' right=progExp ';' ;
callStmt : progParamExp ';' ;

whileStmt
    :   'While' progExp
        changingClause? maintainingClause decreasingClause
        'do' stmt* 'end' ';'
    ;

ifStmt : 'If' progExp 'then' stmt* elseStmt? 'end' ';' ;
elseStmt : 'else' stmt* ;

// program expressions

progExp
    :   progPrimary                                     #progPrimaryExp
    |   '(' progExp ')'                                 #progNestedExp
    |   lhs=progExp '.' rhs=progExp                     #progSelectorExp
    |   progExp name=progSymbolExp progExp              #progInfixExp
//    |   progExp name=progSymbolName                     #progPostfixExp
    ;

progPrimary
    :   progLiteralExp
    |   progParamExp
    |   progSymbolExp
    ;

progParamExp
    :   progSymbolExp '(' (progExp (',' progExp)*)? ')'
    ;

progSymbolExp
    :   (qualifier=ID '::')? name=progSymbolName
    ;

progSymbolName
    :   (SYM | ID | '=')
    ;

progLiteralExp
    :   BOOL                #progBooleanLiteralExp
    |   INT                 #progIntegerLiteralExp
    |   CHAR                #progCharacterLiteralExp
    |   STRING              #progStringLiteralExp
    ;

// mathFor constructs

mathTheoremDecl
    :   ('Corollary'|'Theorem') name=ID ':' mathAssertionExp ';'
    ;

//had better be an assertion involving the use of ':' or
mathClssftnAssertionDecl
    :   'Classification' 'Corollary' ':' mathAssertionExp ';'
    ;

mathDefnSig
    :   mathPrefixDefnSigs
    |   mathInfixDefnSig
    |   mathOutfixDefnSig
    |   mathMixfixDefnSig
    ;

mathPrefixDefnSig
    :   mathSymbolName (',' mathSymbolName)* ('('
        mathVarDeclGroup (',' mathVarDeclGroup)* ')')? ':' mathClssftnExp
    ;

mathPrefixDefnSigs
    :   mathPrefixDefnSig (',' mathPrefixDefnSig)*
    ;

mathInfixDefnSig
    :   '(' mathVarDecl ')' name=mathSymbolName
        '(' mathVarDecl ')' ':' mathClssftnExp
    ;

mathOutfixDefnSig
    :   leftSym=mathBracketOp '(' mathVarDecl ')'
        rightSym=mathBracketOp ':' mathClssftnExp
    ;

mathMixfixDefnSig
    :   '(' mathVarDecl ')' lop=mathBracketOp '(' mathVarDecl ')'
        rop=mathBracketOp ':' mathClssftnExp
    ;

mathSymbolName:   (ID | MATH_UNICODE_SYM | SYM | INT | BOOL | '=') ;
mathBracketOp:  ('|'|'∥'|'⟨'|'⟩'|'⌈'|'⌉'|'⎝'|'⎠'|'['|']') ;

mathCategoricalDefnDecl
    :   'Categorical' 'Definition' 'for' mathPrefixDefnSigs
        'is' mathAssertionExp ';'
    ;

mathStandardDefnDecl
    :   (chainable='Chainable')? ('Implicit')? 'Definition' mathDefnSig
        ('is' body=mathAssertionExp)? ';'
    ;

mathInductiveDefnDecl
    :   'Inductive' 'Definition' mathDefnSig 'is'
        '(i.)' mathAssertionExp ';'
        '(ii.)' mathAssertionExp ';'
    ;

mathVarDeclGroup
    :   mathSymbolName (',' mathSymbolName)* ':' mathClssftnExp
    ;

mathVarDecl
    :   mathSymbolName ':' mathClssftnExp
    ;

// mathematical clauses

initializationClause : 'initialization' (ensuresClause);
requiresClause : 'requires' mathAssertionExp (entailsClause)? ';';
ensuresClause : 'ensures' mathAssertionExp ';';
constraintsClause : ('constraints') mathAssertionExp ';';
conventionsClause : 'conventions' mathAssertionExp (entailsClause)? ';';
correspondenceClause : 'correspondence' mathAssertionExp ';';
changingClause : 'changing' mathExp (',' mathExp)* ';' ;
maintainingClause : 'maintaining' mathAssertionExp ';' ;
decreasingClause : 'decreasing' mathExp ';' ;
entailsClause : 'which_entails' mathExp ;
noticeClause : 'Notice' mathExp ';' ;

// mathematical expressions

mathClssftnExp
    :   mathExp
    ;

mathAssertionExp
    :   mathExp
    |   mathQuantifiedExp
    ;

mathQuantifiedExp
    :   q=(FORALL|EXISTS) mathVarDeclGroup ',' mathAssertionExp
    ;
/*
mathExp
    :   lhs=mathExp op='.' rhs=mathExp                                  #mathSelectorExp
    |   name=mathExp lop='(' mathExp (',' mathExp)* rop=')'             #mathPrefixAppExp
    |   mathExp mathBracketOp mathExp (',' mathExp)* mathBracketOp      #mathNonStdAppExp
//  |   <assoc=right> lhs=mathExp op='->' rhs=mathExp                   #mathBuiltinInfixAppExp
    |   mathExp op=':' mathExp                                          #mathClssftnAssertionExp
    |   lhs=mathExp mathSymbolExp rhs=mathExp                           #mathInfixAppExp
//  |   mathExp op=('and'|'∧'|'or'|'∨') mathExp                         #mathBuiltinInfixAppExp
    |   '(' mathAssertionExp ')'                                        #mathNestedExp
    |   mathPrimeExp                                                    #mathPrimaryExp
    ;
*/
mathExp
    :   lhs=mathExp op='.' rhs=mathExp                                  #mathSelectorExp
    |   name=mathExp lop='(' mathExp (',' mathExp)* rop=')'             #mathPrefixAppExp
    |   mathExp mathBracketOp mathExp (',' mathExp)* mathBracketOp      #mathNonStdAppExp
    |   mathExp op=':' mathExp                                          #mathClssftnAssertionExp
    |   lhs=mathExp mathSymbolExp rhs=mathExp                           #mathInfixAppExp
    |   '(' mathAssertionExp ')'                                        #mathNestedExp
    |   mathPrimeExp                                                    #mathPrimaryExp
    ;

mathPrimeExp
    :   mathCartProdExp
    |   mathSymbolExp
    |   mathSetRestrictionExp
    |   mathOutfixAppExp
    |   mathSetExp
    |   mathLambdaExp
    |   mathAlternativeExp
    ;

mathCartProdExp
    :   'Cart_Prod' (mathVarDeclGroup ';')+ 'end'
    ;

mathSymbolExp
    :   incoming='#'? (qualifier=ID '::')? name=mathSymbolName
    ;

mathOutfixAppExp
    :   lop=mathBracketOp mathExp rop=mathBracketOp
    ;

mathSetRestrictionExp
    :   '{' mathVarDecl '|' mathAssertionExp '}'
    ;

mathSetExp
    :    '{' (mathExp (',' mathExp)*)? '}'
    ;

mathLambdaExp
    :   ('lambda'|'λ') mathVarDecl ',' mathExp
    ;

mathAlternativeExp
    :   '{{' (mathAlternativeItemExp)+ '}}'
    ;

mathAlternativeItemExp
    :   result=mathExp ('if' condition=mathExp ';' | 'otherwise' ';')
    ;

FORALL : ('Forall'|'∀');
EXISTS : ('Exists'|'∃');
BOOL : ('true'|'false');

LINE_COMMENT : '//' .*? ('\n'|EOF)	-> channel(HIDDEN) ;
COMMENT      : '/*' .*? '*/'    	-> channel(HIDDEN) ;

ID                  : [a-zA-Z_] [a-zA-Z0-9_]* ;
INT                 : [0-9]+ ;

//TODO: removed '|' (10/28/2016)
SYM                 : ('!'|'*'|'+'|'-'|'/'|'='|'~'|'<'|'>')+ ;

MATH_UNICODE_SYM
    :   [\u2100-\u214F]
    |   [\u2200-\u22FF]
    |   [\u27C0-\u27EF]
    |   [\u27F0-\u27FF]
    |   [\u2A00-\u2AFF]
    |   [\u2300-\u23BF]
    |   [\u0370-\u03FF] //greek letters
    ;

CHAR: '\'' . '\'' ;
RAW_STRING : '\'' (ESC | ~["\\])* '\'' ;
STRING : '"' (ESC | ~["\\])* '"' ;
fragment ESC : '\\' ["\bfnrt] ;
WS : [ \t\n\r]+ -> channel(HIDDEN) ;