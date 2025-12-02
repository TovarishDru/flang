(func Cube (arg) (times (times arg arg) arg))

(Cube 4)

(func Trivial () 1)

(Trivial)

(setq x 10)
(func test_loc (x) (plus x 5))
(test_loc x)

(func test_glob () (plus x 5))
(test_glob)
x

(func change_glob () (setq x (plus x 5)))
(change_glob)
x

(func fac (num)
    (func fac_helper (num acc)
        (cond
            (equal num 0)
            acc
            (fac_helper (minus num 1) (times acc num))
        )
    )

    (fac_helper num 1)
)

(fac 5)

(func makeList (A) (cons A (cons A ())))
(makeList 1)