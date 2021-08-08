/********************************************************************************
 * Copyright (c) 2015-2021 Riverside Software
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU Lesser General Public License v3.0
 * which is available at https://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-3.0
 ********************************************************************************/
package org.prorefactor.core.nodetypes;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.ProToken;

import eu.rssw.pct.elements.DataType;

public class SystemHandleNode extends JPNode implements IExpression {

  public SystemHandleNode(ProToken t, JPNode parent, int num, boolean hasChildren) {
    super(t, parent, num, hasChildren);
  }

  @Override
  public boolean isExpression() {
    return true;
  }

  @Override
  public DataType getDataType() {
    return DataType.HANDLE;
  }

  public DataType getAttributeDataType(String id) {
    ABLNodeType type = getFirstChild().getNodeType();
    if (type == ABLNodeType.ACTIVEFORM)
        return getActiveFormAttributeDataType(id);
    return ExpressionNode.getStandardAttributeDataType(id);
  }

  public DataType getMethodDataType(String id) {
    switch (getFirstChild().getNodeType()) {
      case ACTIVEFORM:
      case CLIPBOARD:
      case CODEBASELOCATOR:
      case COMSELF:
      case FILEINFORMATION:
      case FOCUS:
      case LASTEVENT:
      case MOUSE:
      case RCODEINFORMATION:
      case SELF:
      case SUPER: // TODO
      case TEXTCURSOR: // TODO Verify
      case THISOBJECT: // TODO
        // No methods
        return DataType.UNKNOWN;
      case ACTIVEWINDOW:
      case CURRENTWINDOW:
      case DEFAULTWINDOW:
        return getWindowMethodDataType(id);
      case AUDITCONTROL:
        return getAuditControlMethodDataType(id);
      case AUDITPOLICY:
        return getAuditPolicyMethodDataType(id);
      case COLORTABLE:
        return getColorTableMethodDataType(id);
      case COMPILER:
        return getCompilerMethodDataType(id);
      case DEBUGGER:
        return getDebuggerMethodDataType(id);
      case ERRORSTATUS:
        return getErrorStatusMethodDataType(id);
      case FONTTABLE:
        return getFontTableMethodDataType(id);
      case LOGMANAGER:
        return getLogManagerDataType(id);
      case PROFILER:
        return getProfilerMethodDataType(id);
      case SECURITYPOLICY:
        return getSecurityPolicyMethodDataType(id);
      case SESSION:
        return getSessionMethodDataType(id);
      case SOURCEPROCEDURE:
      case TARGETPROCEDURE:
      case THISPROCEDURE:
        return getProcedureMethodDataType(id);
      case WEBCONTEXT:
        return getWebContextMethodDataType(id);
      default:
        return DataType.UNKNOWN;
    }
  }

  private DataType getActiveFormAttributeDataType(String id) {
    switch(id) {
      case "NEXTFORM":
      case "PREVFORM":
        return new DataType("Progress.Windows.IForm");
      case "PROWINHANDLE":
        return DataType.HANDLE;
        default:
          return DataType.UNKNOWN;
    }
  }

  private DataType getAuditControlMethodDataType(String id) {
    switch (id) {
      case "BEGIN-EVENT-GROUP":
      case "LOG-AUDIT-EVENT" :
      case "SET-APPL-CONTEXT":
        return DataType.CHARACTER;
      case "CLEAR-APPL-CONTEXT":  
      case "END-EVENT-GROUP":
        return DataType.LOGICAL;
        default:
          return DataType.UNKNOWN;
    }
  }

  private DataType getAuditPolicyMethodDataType(String id) {
    switch (id) {
      case "ENCRYPT-AUDIT-MAC-KEY":
        return DataType.CHARACTER;
      case "REFRESH-AUDIT-POLICY":
        return DataType.LOGICAL;
      default:
        return DataType.UNKNOWN;
    }
  }

  private DataType getColorTableMethodDataType(String id) {
    switch (id) {
      case "GET-BLUE-VALUE":
      case "GET-GREEN-VALUE":
      case "GET-RED-VALUE":
      case "GET-RGB-VALUE":
        return DataType.INTEGER;
      case "GET-DYNAMIC":
      case "SET-BLUE-VALUE":
      case "SET-DYNAMIC":
      case "SET-GREEN-VALUE":
      case "SET-RED-VALUE":
      case "SET-RGB-VALUE":
        return DataType.LOGICAL;
      default:
        return DataType.UNKNOWN;
    }
  }

  private DataType getCompilerMethodDataType(String id) {
    switch (id) {
      case "GET-COLUMN":
      case "GET-ERROR-COLUMN":
      case "GET-ERROR-ROW":
      case "GET-FILE-OFFSET":
      case "GET-MESSAGE":
      case "GET-MESSAGE-TYPE":
      case "GET-NUMBER":
      case "GET-ROW":
        return DataType.INTEGER;
      case "GET-FILE-NAME":
        return DataType.CHARACTER;
      default:
        return DataType.UNKNOWN;
    }
  }

  private DataType getDebuggerMethodDataType(String id) {
    switch (id) {
      case "CANCEL-BREAK":
      case "CLEAR":
      case "DEBUG":
      case "SET-BREAK":
        return DataType.LOGICAL;
      case "DISPLAY-MESSAGE":
        return DataType.INTEGER;
      default:
        return DataType.UNKNOWN;
    }
  }

  private DataType getErrorStatusMethodDataType(String id) {
    switch (id) {
      case "GET-MESSAGE":
        return DataType.CHARACTER;
      case "GET-NUMBER":
        return DataType.INTEGER;
      default:
        return DataType.UNKNOWN;
    }
  }

  private DataType getFontTableMethodDataType(String id) {
    switch (id) {
      case "GET-TEXT-HEIGHT-CHARS":
      case "GET-TEXT-WIDTH-CHARS":
        return DataType.DECIMAL;
      case "GET-TEXT-HEIGHT-PIXELS":
      case "GET-TEXT-WIDTH-PIXELS":
        return DataType.INTEGER;
      default:
        return DataType.UNKNOWN;
    }
  }

  private DataType getLogManagerDataType(String id) {
    switch (id) {
      case "CLEAR-LOG":
      case "CLOSE-LOG":
      case "WRITE-MESSAGE":
        return DataType.LOGICAL;
      default:
        return DataType.UNKNOWN;
    }
  }

  private DataType getProcedureMethodDataType(String id) {
    switch (id) {
      case "ADD-SUPER-PROCEDURE":
      case "REMOVE-SUPER-PROCEDURE":
      case "SET-CALLBACK-PROCEDURE":
        return DataType.LOGICAL;
      case "GET-SIGNATURE":
        return DataType.CHARACTER;
      default:
        return DataType.UNKNOWN;
    }
  }
  
  private DataType getProfilerMethodDataType(String id) {
    switch (id) {
      case "USER-DATA":
      case "WRITE-DATA":
        return DataType.LOGICAL;
      default:
        return DataType.UNKNOWN;
    }
  }

  private DataType getSecurityPolicyMethodDataType(String id) {
    switch (id) {
      case "GET-CLIENT":
        return DataType.HANDLE;
      case "LOAD-DOMAINS":
      case "LOCK-REGISTRATION":
      case "REGISTER-DOMAIN":
      case "SET-CLIENT":
        return DataType.LOGICAL;
      default:
        return DataType.UNKNOWN;
    }
  }

  private DataType getSessionMethodDataType(String id) {
    switch (id) {
      case "GET-PRINTERS":
      case "GET-WAIT-STATE":
        return DataType.CHARACTER;
      case "ADD-SUPER-PROCEDURE":
      case "EXPORT":
      case "REMOVE-SUPER-PROCEDURE":
      case "SET-NUMERIC-FORMAT":
      case "SET-WAIT-STATE":
        return DataType.LOGICAL;
      default:
        return DataType.UNKNOWN;
    }
  }

  private DataType getWebContextMethodDataType(String id) {
    switch (id) {
      case "GET-BINARY-DATA":
        return DataType.MEMPTR;
      case "GET-CGI-LIST":
      case "GET-CGI-VALUE":
      case "GET-CONFIG-VALUE":
      case "URL-DECODE":
      case "URL-ENCODE":
        return DataType.CHARACTER;
      case "GET-CGI-LONG-VALUE":
        return DataType.LONGCHAR;
      case "INCREMENT-EXCLUSIVE-ID":
        return DataType.INTEGER;
      default:
        return DataType.UNKNOWN;
    }
  }

  private DataType getWindowMethodDataType(String id) {
    switch (id) {
      case "END-FILE-DROP":
      case "LOAD-ICON":
      case "LOAD-MOUSE-POINTER":
      case "LOAD-SMALL-ICON":
      case "MOVE-TO-BOTTOM":
      case "MOVE-TO-TOP":
        return DataType.LOGICAL;
      case "GET-DROPPED-FILE":
        return DataType.CHARACTER;
      case "GET-SELECTED-WIDGET":
        return DataType.HANDLE;
      default:
        return DataType.UNKNOWN;
    }
  }

}
