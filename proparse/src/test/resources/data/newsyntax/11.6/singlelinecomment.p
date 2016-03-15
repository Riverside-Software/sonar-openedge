/**********************************************************************
 * Copyright (C) 2006-2014 by Consultingwerk Ltd. ("CW") -            *
 * www.consultingwerk.de and other contributors as listed             *
 * below.  All Rights Reserved.                                       *
 *                                                                    *
 *  Software is distributed on an "AS IS", WITHOUT WARRANTY OF ANY    *
 *   KIND, either express or implied.                                 *
 *                                                                    *
 *  Contributors:                                                     *
 *                                                                    *
 **********************************************************************/

USING Progress.Lang.* FROM PROPATH . 

DEFINE VARIABLE oClass AS Progress.Lang.Class // inline comment
NO-UNDO .   

DEFINE VARIABLE cClass AS CHARACTER NO-UNDO.

/* ***************************  Main Block  *************************** */

message 43 //single line comment
	view-as // new comment line
	alert-box // last line
	.
	
return.
