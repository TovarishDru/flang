(func apply (num operation)
    (operation num)
)

(apply 5 (lambda (a) (plus a 20)))

(func sub_five (num)
    (minus num 5)
)
(apply 5 sub_five)



(func transform_num_for_limit (num operation limiter)
    (
        (while (limiter num)
            (setq num (operation num))
        )
    )
)

(func square (num) (times num num))
(transform_num_for_limit 5 square (lambda (x) (less x 500)))



((lambda (oper num) (oper num)) sub_five 25)

((lambda (oper num) (oper num)) (lambda (x) (plus x 5)) 5)