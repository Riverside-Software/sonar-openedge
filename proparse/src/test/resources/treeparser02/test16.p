/* displays the first customer.name. If the display
 * comes before the procedure definition, then the compile fails,
 * "no for, find, or create...".
 */
run getit.
procedure getit:
  find first customer.
end.
display customer.name.
