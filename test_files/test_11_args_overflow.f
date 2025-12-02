(plus 1 2 5)

(minus 2 82 9 2)

(divide 8 28 28)

(times 1234 2345 123)

(or true false true)

(and true false true)

(xor false false false)

(not false true)

(func add_nums (num1 num2)
    (plus num1 num2)
)

(add_nums 1 2 5)

((lambda (a b) (plus a b)) 1 2 5)