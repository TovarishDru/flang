(func name () 
    (
        (setq x 50)
    )
)
x

(prog (x)
    (setq x 500)
)
x

(setq y 1)
(while (less y 5)
    (setq y (plus 1 y))
    (setq x 12)
)
x

(lambda () (setq x 500))
x