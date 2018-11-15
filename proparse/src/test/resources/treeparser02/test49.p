/* Bug079
 * Tree parser was missing the EXCEPT phrase in DISPLAY
 * statements, causing the *occasional* evaluation to billto,
 * and always incorrectly flagging as unqualified.
 */
find first customer.
find first billto.
display customer except name address.
