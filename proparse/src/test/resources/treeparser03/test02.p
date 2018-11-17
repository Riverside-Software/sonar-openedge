/* Check that an INPUT field can be referenced immediately after it's
 * declared, for example, in a VALIDATE phrase.
 */


display
   "Hello" @ customer.name
     validate(input customer.name = "world!", "Must be world!")
   .
