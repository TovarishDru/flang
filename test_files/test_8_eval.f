(eval 5)
(eval -1.35)
(eval true)
(eval null)


(setq x 10)
(eval x)

(eval 'x)

(eval '(plus 2 3))
(eval '(minus 10 3))
(eval '(times 2 (plus 3 4)))

(setq expr '(plus 2 3))
(eval expr)

(setq t '(2 3))
(setq expr (cons 'plus t))
(eval expr)
(setq expr (cons 'plus (tail '(1 2 3)



(eval '(plus 2 (times 3 4)))

(setq y (plus -1.35 2.15))
(eval y)
(setq expr '(plus -1.35 2.15))
(eval expr)

(eval '())
