grammar Expression;
loop: FORKEYWORD OPENPAR assignment condition increment CLOSEPAR
      OPENBRACE body CLOSEBRACE;

assignment: IDENTIFIER ASSIGNOP INTEGER SEMI;
condition: IDENTIFIER comparator INTEGER SEMI;
comparator: LT | GT;
increment: IDENTIFIER INCOP;
body: statement*;
statement: call | assignment;
call: IDENTIFIER OPENPAR args CLOSEPAR SEMI;
args: INTEGER (COMMA INTEGER)*;

LT: '<';
GT: '>';
ASSIGNOP: '=';
INCOP: '++';
OPENPAR: '(';
CLOSEPAR: ')';
OPENBRACE: '{';
CLOSEBRACE: '}';
COMMA: ',';
SEMI: ';';

FORKEYWORD: 'for';
IDENTIFIER: [a-z]*;
INTEGER: [0-9]*;
WS: [ \t\n]+ -> skip;