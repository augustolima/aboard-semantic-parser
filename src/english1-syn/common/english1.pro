# Project file
enc: UTF-8
att: english1.att
ops: english1.ops
tbl: ./filter.tbl
     ../allo/allo.tbl
     ../morph/combi.tbl

lamorph 
rul: ../morph/combi.rul
var: ../morph/english1.var
lex: ../allo/allo.all
sem: ../morph/sem.lex

lahear
rul: ../syn/syntax.rul
var: ../syn/syntax.var

lathink