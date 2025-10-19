grammar Expression;

// --- PARSER RULES ---
stylesheet: statement* EOF;

statement: selector OPEN_BRACE property* CLOSE_BRACE;

selector: LOWER_IDENT
        | ID_IDENT
        | CLASS_IDENT;

property: LOWER_IDENT COLON value SEMICOLON;

value: PIXELSIZE
     | PERCENTAGE
     | SCALAR
     | COLOR
     | LOWER_IDENT
     | ID_IDENT
     | CLASS_IDENT;

// --- LEXER RULES ---
IF: 'if';
ELSE: 'else';
BOX_BRACKET_OPEN: '[';
BOX_BRACKET_CLOSE: ']';

TRUE: 'TRUE';
FALSE: 'FALSE';
PIXELSIZE: [0-9]+ 'px';
PERCENTAGE: [0-9]+ '%';
SCALAR: [0-9]+;

COLOR: '#' [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f];

ID_IDENT: '#' [a-z0-9\-]+;
CLASS_IDENT: '.' [a-z0-9\-]+;
LOWER_IDENT: [a-z] [a-z0-9\-]*;
CAPITAL_IDENT: [A-Z] [A-Za-z0-9_]*;

WS: [ \t\r\n]+ -> skip;

OPEN_BRACE: '{';
CLOSE_BRACE: '}';
SEMICOLON: ';';
COLON: ':';
PLUS: '+';
MIN: '-';
MUL: '*';
ASSIGNMENT_OPERATOR: ':=';
