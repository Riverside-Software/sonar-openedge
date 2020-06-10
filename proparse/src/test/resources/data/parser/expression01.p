def var x  as int.
def var x1 as int.
def var x2 as int.
def var x3 as int.

x1 + x2 + x3.
x1 + x2 * x3.
x1 = x2 = x3.
// Perfectly valid code... Should be reported by a rule
x1 + x2 = x3.
x1 < x2 or x3 > x2 or x1 + x2 * x3 = x3.
