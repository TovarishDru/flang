(setq x 0)
(while (less x 5) 
    (setq x (plus x 1))
)
x

(setq y 0)
(while (less y 10)
    (cond (equal y 7) (break))
    (setq y (plus y 1))
)
y

(setq arr '())
(while (less x 10)
    (setq arr (cons x arr))
    (setq x (plus x 1))
)
arr