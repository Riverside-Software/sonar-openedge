/* Test "Names" refactoring
*/

def temp-table tt1 field f1 as char.
find first tt1.
display tt1.f1.

FIND FIRST customer.
DISPLAY customer.balance.
DISPLAY customer.name.

def buffer bBill for billto.
find first bBill.
display bBill.billtoid.

