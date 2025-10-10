grammar Expressions;

prog: expression EOF;

expression: INT;

INT: [0-9]+;
