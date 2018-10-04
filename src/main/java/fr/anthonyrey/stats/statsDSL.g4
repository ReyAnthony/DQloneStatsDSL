grammar statsDSL;

/* Parser Rules */
program
    : classDef+ EOF ;
classDef
    : ID LCURLY innerClassDef RCURLY;
innerClassDef
    : initStat initLevel*;
initStat
    : INIT LCURLY statDef+ RCURLY;
initLevel
    : NUMBER xpDef LCURLY statDef+ RCURLY;
xpDef
    : LPARENS NUMBER RPARENS;
statDef
    : stat NUMBER;
stat
    : AGI|ATK|DEF|MAG|HP ;

/* Lexer Rules */
LCURLY:        '{';
RCURLY:        '}';
LPARENS:        '(';
RPARENS:        ')';
INIT:          'init';
AGI:            'AGI';
ATK:            'ATK';
DEF:            'DEF';
MAG:            'MAG';
HP:             'HP';
ID:             ([a-zA-Z_])+;
NUMBER:         [0-9]+;
MULTILINE_COMMENT
    : '!--' .*? '--!' -> skip ;
LINE_COMMENT
    : '--' ~[\r\n]* -> skip ;
WHITESPACE:     (' '|'\t')+ -> skip ;
NEWLINE:        ('\r'? '\n' | '\r')+ -> skip ;
