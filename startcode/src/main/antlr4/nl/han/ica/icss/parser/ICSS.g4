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
BOOL: (TRUE | FALSE);
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
color: COLOR;
pixelSize: PIXELSIZE;
percentage: PERCENTAGE;
bool: (TRUE | FALSE);
variableReference: CAPITAL_IDENT;
propertyName: LOWER_IDENT;
tagSelector: LOWER_IDENT;
idSelector: ID_IDENT;
classSelector: CLASS_IDENT;
value: (color | pixelSize | percentage | variableReference);


stylesheet: (variableAssignment | stylerule)* EOF;

stylerule: (tagSelector | idSelector | classSelector) OPEN_BRACE ( variableAssignment | declaration | ifClause)+ CLOSE_BRACE;

variableAssignment: variableReference ASSIGNMENT_OPERATOR (color | pixelSize | bool | percentage | expression) SEMICOLON;

declaration: propertyName COLON (value | expression) SEMICOLON;

expression
 : expression (MUL) expression #multiplyOperation
 | expression (PLUS|MIN) expression #addOrSubtractOperation
 | SCALAR #scalar
 | value #expressionValue;

ifClause: IF BOX_BRACKET_OPEN (variableReference | bool ) BOX_BRACKET_CLOSE OPEN_BRACE ( variableAssignment | declaration | ifClause)+ CLOSE_BRACE elseClause?;

elseClause: ELSE OPEN_BRACE ( variableAssignment | declaration | ifClause)+ CLOSE_BRACE;
