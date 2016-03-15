 
 /*------------------------------------------------------------------------
    File        : StreamHandleClass
    Purpose     : 
    Syntax      : 
    Description : 
    Author(s)   : Mike
    Created     : Tue Dec 16 21:42:13 CET 2014
    Notes       : 
  ----------------------------------------------------------------------*/

USING Progress.Lang.*.
USING Consultingwerk.Web.* FROM PROPATH.

BLOCK-LEVEL ON ERROR UNDO, THROW.

CLASS Consultingwerk.ProparseTests.StreamHandle.StreamHandleClass: 

	/*------------------------------------------------------------------------------
	 Purpose:
	 Notes:
	------------------------------------------------------------------------------*/
		
	CONSTRUCTOR PUBLIC StreamHandleClass (  ):
		SUPER ().

		DEFINE VARIABLE pcData AS CHARACTER NO-UNDO.
		
        PUT STREAM-HANDLE WebContext:WebStream UNFORMATTED pcData . 
		
		
	END CONSTRUCTOR.


END CLASS.