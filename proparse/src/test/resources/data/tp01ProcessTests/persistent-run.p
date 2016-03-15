/* persistent-run   - Run a program as a persistent procedure.          */

/* THIS IS TEST DATA FOR CODE ANALYSIS TESTS. */

define variable hTarget as handle no-undo.

run persistent-proc.p persistent set hTarget.

/* Call persistent-proc.test_01. */
run test_01 in hTarget.

/* Call internal procedure test_01. */
run test_01.

procedure test_01:
	/* Call persisten-proc.test_01. */
	run test_02 in hTarget.
end procedure.

