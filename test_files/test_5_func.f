(func Cube (arg) (times (times arg arg) arg))

(Cube 4)

(func Trivial () 1)

(Trivial)

(func makeList (A) '(A))

(makeList '1)

(setq x 10)
(func test (x) (plus x 5))
(test x)
x