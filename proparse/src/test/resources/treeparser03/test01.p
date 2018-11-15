
/* Check that the order of creation of symbols is OK.
 * At one point, the new symbol was being added before the end
 * of the statement, and the LIKE was referring to itself, so
 * the datatype was not getting set, and an assertion was thrown.
 */
def var redundant as integer.
procedure redundantProcedure:
  def var redundant like redundant.
end.
