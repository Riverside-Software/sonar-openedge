
CLASS newsyntax.102b.CustomException INHERITS Progress.Lang.AppError:

	CONSTRUCTOR PUBLIC CustomException (mymsg AS CHARACTER, mynum AS INTEGER):
		AddMessage(mymsg, mynum).
	END CONSTRUCTOR.

END CLASS.
