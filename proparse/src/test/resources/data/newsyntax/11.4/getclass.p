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
/*------------------------------------------------------------------------
    File        : get-class.p
    Purpose     : 

    Syntax      :

    Description : 

    Author(s)   : 
    Created     : Sun Jan 25 18:38:25 CET 2015
    Notes       :
  ----------------------------------------------------------------------*/

/* ***************************  Definitions  ************************** */

USING Progress.Lang.* FROM PROPATH . 

DEFINE VARIABLE oClass AS Progress.Lang.Class NO-UNDO .   
DEFINE VARIABLE cClass AS CHARACTER NO-UNDO.

/* ***************************  Main Block  *************************** */

oClass = GET-CLASS (AppError) .

oClass = GET-CLASS (Progress.Lang.Error) .

ASSIGN cClass = GET-CLASS (AppError):TypeName .

