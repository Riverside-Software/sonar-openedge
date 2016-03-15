 
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

CLASS Consultingwerk.ProparseTests.StreamHandle.StreamHandleClass2: 

	/*------------------------------------------------------------------------------
	 Purpose:
	 Notes:
	------------------------------------------------------------------------------*/
		
	CONSTRUCTOR PUBLIC StreamHandleClass2 (  ):
		SUPER ().

		DEFINE VARIABLE pcData AS CHARACTER NO-UNDO.
		DEFINE VARIABLE h AS HANDLE NO-UNDO.
		
		/* resolving the static-refernece to WebContext:WebStream with 
		   a local variable h fixes the issue with STREAM-HANDLE */ 
		ASSIGN h = WebContext:WebStream .
		
        PUT STREAM-HANDLE h UNFORMATTED pcData . 
		
		
	END CONSTRUCTOR.


END CLASS.