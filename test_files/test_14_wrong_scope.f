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

(lambda () (setq x 500))
x