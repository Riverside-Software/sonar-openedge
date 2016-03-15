

/* Test FROM ASSEMBLY, .NET class references, .NET inner class references. */
USING System.Windows.Forms.* FROM ASSEMBLY.
DEFINE VARIABLE rControl AS CLASS Control.
DEFINE VARIABLE rCollection AS CLASS Control+ControlCollection.


/* Test an ambiguous reference. */
DEFINE VARIABLE cLabel AS CHARACTER NO-UNDO.
DEFINE FRAME a Sports2000.Customer.Name.
FIND FIRST Sports2000.Customer.
cLabel = Sports2000.Customer.Name:Label. /* Ambiguous reference */

display newsyntax.101b.Test1:prop1.


/* NOW can be an initial value for a date. Issue reported on oehive by Niek. */
define variable dtDatumVanaf as datetime no-undo initial now.

