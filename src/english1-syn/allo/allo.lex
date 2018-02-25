[sur: ".", cat: (v' decl), allo: A_null]

!template[ allo: A_learn
           flx:  C_learn
           syn:  T_1
]                
!+[sur: learn, core: learn, ontology: (Food)]
!+[sur: walk,  core: walk]
!+[sur: talk,  core: talk]

!template[ allo: A_give
           flx:  C_give
           syn:  T_1
]
!+[sur: give, core: give]

!template[ allo: A_man
           syn:  T_2
]
!+[sur: man, core: man]
!+[sur: woman, core: woman]

!template[ allo: A_book
           flx:  C_book
           syn:  T_2
]
![sur, core]
book,  book
dog,   dog
cat,   cat
boy,   boy
girl,  girl

# ALTERNATIVE:
# !template[ allo: A_book
#            flx:  C_book
#            syn:  T_2
# ]
# !+[sur: book, core: book]
# !+[sur: dog,  core: dog]
# !+[sur: cat,  core: cat]

!template[ allo: A_wolf
           flx:  C_wolf
           syn:  T_2
]
!+[sur: wolf, core: wolf]

!template[ allo: A_math
           syn:  T_2
]
![sur, core]
math,  math
money, money

!template[ allo: A_quick
           flx:  C_quick
           syn:  T_3
]
!+[sur: quick, core: quick]

!template[ allo: A_null
           syn:  T_2
]
!+[sur: the, core: @1, cat: (sn' snp), sem: (def)]
!+[sur: the, core: @1, cat: (pn' pnp), sem: (def)]


!template[ allo: A_null
           syn:  T_2
]
!+[sur: a, core: @1, cat: (sn' snp), sem: (indef)]


!template[ allo:     A_null
           cat:      (flx)
         ]
!+sur: s ing ed es ly er est 


