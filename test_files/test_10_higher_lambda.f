(func sub_five (num)
    (minus num 5)
)
(apply 5 sub_five)

((lambda (oper num) (oper num)) sub_five 25)

((lambda (oper num) (oper num)) (lambda (x) (plus x 5)) 5)