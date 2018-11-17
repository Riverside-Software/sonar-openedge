/* Name resolves to customer.name.
 * It appears that a weakly scoped *named* buffer will not
 * have its scope automagically raised for a field name
 * reference. Without the <<for first customer>>, this
 * snippet fails to compile.
 */
for first customer: end.
def buffer bcust for customer.
for last bcust: end.
display name.
