((lambda (a b) (plus a b)) 55 -176)

(setq myFunc (lambda (p) (cond (less p 0) plus minus)))
((myFunc -1) 1 2)

(((lambda (p) (cond (less p 0) plus minus)) 1) 1 2)

(cond ((lambda (x) (less x 5)) 6) 1 0)