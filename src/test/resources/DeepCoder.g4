grammar DeepCoder;
stmtList : stmtList NEWLINE stmt
	| stmt
	;
NEWLINE : '\n'
	;
stmt : lowercaseASCII COLONEQUALS varDef
	;
funcArg : lowercaseASCII
	| lambda
	;
funcArgs : funcArg
	| funcArgs SPACE funcArg
	;
SPACE : ' '
	;
fUNCTION_NAME : Head
	| Last
	| Take
	| Drop
	| Access
	| Minimum
	| Maximum
	| Reverse
	| Sort
	| Sum
	| Map
	| Filter
	| Count
	| ZipWith
	| ScanL1
	;
varDef : fUNCTION_NAME SPACE funcArgs
	| intint
	;
lowercaseASCII : Q
	| W
	| E
	| R
	| T
	| Y
	| U
	| I
	| O
	| P
	| A
	| S
	| D
	| F
	| G
	| H
	| J
	| K
	| L
	| Z
	| X
	| C
	| V
	| B
	| N
	| M
	;
COLONEQUALS : ':='
	;
intint : LBintRB
	| Int
	;
lambda : LPPLUS1RP
	| LPDASH1RP
	| LPTIMES2RP
	| LPFSLASH2RP
	| LPTIMESLPDASH1RPRP
	| LPTIMESTIMES2RP
	| LPTIMES3RP
	| LPFSLASH3RP
	| LPTIMES4RP
	| LPFSLASH4RP
	| LPGT0RP
	| LPLT0RP
	| LPPERCENT2EQUALSEQUALS0RP
	| LPPERCENT2SPACEEQUALSEQUALSSPACE1RP
	| LPPLUSRP
	| LPDASHRP
	| LPTIMESRP
	| MIN
	| MAX
	;
Head : 'Head'
	;
Last : 'Last'
	;
Take : 'Take'
	;
Drop : 'Drop'
	;
Access : 'Access'
	;
Minimum : 'Minimum'
	;
Maximum : 'Maximum'
	;
Reverse : 'Reverse'
	;
Sort : 'Sort'
	;
Sum : 'Sum'
	;
Map : 'Map'
	;
Filter : 'Filter'
	;
Count : 'Count'
	;
ZipWith : 'ZipWith'
	;
ScanL1 : 'ScanL1'
	;
Q : 'q'
	;
W : 'w'
	;
E : 'e'
	;
R : 'r'
	;
T : 't'
	;
Y : 'y'
	;
U : 'u'
	;
I : 'i'
	;
O : 'o'
	;
P : 'p'
	;
A : 'a'
	;
S : 's'
	;
D : 'd'
	;
F : 'f'
	;
G : 'g'
	;
H : 'h'
	;
J : 'j'
	;
K : 'k'
	;
L : 'l'
	;
Z : 'z'
	;
X : 'x'
	;
C : 'c'
	;
V : 'v'
	;
B : 'b'
	;
N : 'n'
	;
M : 'm'
	;
LBintRB : '[int]'
	;
Int : 'int'
	;
LPPLUS1RP : '(+1)'
	;
LPDASH1RP : '(-1)'
	;
LPTIMES2RP : '(*2)'
	;
LPFSLASH2RP : '(/2)'
	;
LPTIMESLPDASH1RPRP : '(*(-1))'
	;
LPTIMESTIMES2RP : '(**2)'
	;
LPTIMES3RP : '(*3)'
	;
LPFSLASH3RP : '(/3)'
	;
LPTIMES4RP : '(*4)'
	;
LPFSLASH4RP : '(/4)'
	;
LPGT0RP : '(>0)'
	;
LPLT0RP : '(<0)'
	;
LPPERCENT2EQUALSEQUALS0RP : '(%2==0)'
	;
LPPERCENT2SPACEEQUALSEQUALSSPACE1RP : '(%2 == 1)'
	;
LPPLUSRP : '(+)'
	;
LPDASHRP : '(-)'
	;
LPTIMESRP : '(*)'
	;
MIN : 'MIN'
	;
MAX : 'MAX'
	;
