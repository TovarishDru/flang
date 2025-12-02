(func sub_five (num)
    (minus num 5)
)

((lambda (oper num) (oper num)) sub_five 25)

((lambda (oper num) (oper num)) (lambda (x) (plus x 5)) 5)