grammar ICSS;

// --- LEXER ---

IF: 'if';
ELSE: 'else';
BOX_BRACKET_OPEN: '[';
BOX_BRACKET_CLOSE: ']';

// Literals
TRUE: 'TRUE';
FALSE: 'FALSE';
PIXELSIZE: [0-9]+ 'px';
PERCENTAGE: [0-9]+ '%';
SCALAR: [0-9]+;

// Colors
COLOR: '#' [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f];

// Identifiers
ID_IDENT: '#' [a-z0-9\-]+;
CLASS_IDENT: '.' [a-z0-9\-]+;
LOWER_IDENT: [a-z] [a-z0-9\-]*;
CAPITAL_IDENT: [A-Z] [A-Za-z0-9_]*;

// Whitespace
WS: [ \t\r\n]+ -> skip;

// Symbols
OPEN_BRACE: '{';
CLOSE_BRACE: '}';
SEMICOLON: ';';
COLON: ':';
PLUS: '+';
MIN: '-';
MUL: '*';
ASSIGNMENT_OPERATOR: ':=';

// --- PARSER ---

stylesheet
    : statement* EOF
    ;

block
    : (declaration | ifStatement)*
    ;

statement
    : variableAssignment
    | styleRule
    | declaration
    | ifStatement
    ;

variableAssignment
    : CAPITAL_IDENT ASSIGNMENT_OPERATOR value SEMICOLON
    ;

styleRule
    : selector OPEN_BRACE block CLOSE_BRACE
    ;

selector
    : ID_IDENT
    | CLASS_IDENT
    | LOWER_IDENT
    ;

declaration
    : LOWER_IDENT COLON value SEMICOLON
    ;

value
    : expression
    ;

expression
    : expression (PLUS | MIN) term
    | term
    ;

term
    : term MUL primaryValue
    | primaryValue
    ;

primaryValue
    : COLOR
    | PIXELSIZE
    | PERCENTAGE
    | SCALAR
    | LOWER_IDENT
    | CAPITAL_IDENT
    | TRUE
    | FALSE
    | ID_IDENT
    | CLASS_IDENT
    | BOX_BRACKET_OPEN expression BOX_BRACKET_CLOSE
    ;

ifStatement
    : IF BOX_BRACKET_OPEN expression BOX_BRACKET_CLOSE
      OPEN_BRACE block CLOSE_BRACE
      (ELSE elseBlock)?
    ;

elseBlock
    : OPEN_BRACE block CLOSE_BRACE
    ;
