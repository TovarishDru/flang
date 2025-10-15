(setq x 5)
x


(setq  y '(plus -1.35 2.15))
(eval y)

(func  square (n) (times n n))
(square 5)

(prog (a b) (setq a 5) (setq b 6) (plus a b))

(cond (less 3 5) 1 0)

(setq x 0)
(while (less x 5) (setq x (plus x 1)))
x

(prog (n) (setq n 3) (return (times n n)) (plus n 10))

(cons 1 (cons 2 (cons 3 null)))

(head '(a b c))

(and (less 1 2) (greater 5 3))

(prog (x)
  (setq x 0)
  (while (less x 10)
    (cond (equal x 5) (break))
    (setq x (plus x 1)))
  x)

(setq x 10)
   (func test (x) (plus x 5))
   (test 3)
   x
