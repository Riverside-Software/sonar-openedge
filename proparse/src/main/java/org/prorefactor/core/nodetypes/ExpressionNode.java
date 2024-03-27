/********************************************************************************
 * Copyright (c) 2015-2024 Riverside Software
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

import java.util.List;
import java.util.function.Function;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.ProToken;
import org.prorefactor.proparse.support.IProparseEnvironment;

import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.IMethodElement;
import eu.rssw.pct.elements.IPropertyElement;
import eu.rssw.pct.elements.ITypeInfo;
import eu.rssw.pct.elements.IVariableElement;

public abstract class ExpressionNode extends JPNode implements IExpression {

  ExpressionNode(ProToken t, JPNode parent, int num, boolean hasChildren) {
    super(t, parent, num, hasChildren);
  }

  @Override
  public boolean isIExpression() {
    return true;
  }

  @Override
  public IExpression asIExpression() {
    return this;
  }

  @Override
  public JPNode asJPNode() {
    return this;
  }

  static DataType getStandardAttributeDataType(String id) {
    switch (id) {
      case "ACCELERATOR":
      case "ACTOR":
      case "ADM-DATA":
      case "APPL-CONTEXT-ID":
      case "APPSERVER-INFO":
      case "APPSERVER-PASSWORD":
      case "APPSERVER-USERID":
      case "ATTACHED-PAIRLIST":
      case "ATTRIBUTE-NAMES":
      case "AUDIT-EVENT-CONTEXT":
      case "AVAILABLE-FORMATS":
      case "BASE-ADE":
      case "BUFFER-GROUP-NAME":
      case "BUFFER-NAME":
      case "BUFFER-TENANT-NAME":
      case "CALL-NAME":
      case "CHARSET":
      case "CLASS-TYPE":
      case "CLIENT-CONNECTION-ID":
      case "CLIENT-TTY":
      case "CLIENT-TYPE":
      case "CLIENT-WORKSTATION":
      case "CODEPAGE":
      case "COLUMN-LABEL":
      case "CONFIG-NAME":
      case "CONTEXT-HELP-FILE":
      case "CPCASE":
      case "CPCOLL":
      case "CPINTERNAL":
      case "CPLOG":
      case "CPPRINT":
      case "CPRCODEIN":
      case "CPRCODEOUT":
      case "CPSTREAM":
      case "CPTERM":
      case "CURRENT-ENVIRONMENT":
      case "DATA-SOURCE-COMPLETE-MAP":
      case "DATA-TYPE":
      case "DATE-FORMAT":
      case "DB-CONTEXT":
      case "DB-LIST":
      case "DB-REFERENCES":
      case "DBNAME":
      case "DDE-ITEM":
      case "DDE-NAME":
      case "DDE-TOPIC":
      case "DEFAULT-STRING":
      case "DELIMITER":
      case "DISPLAY-TYPE":
      case "DOMAIN-DESCRIPTION":
      case "DOMAIN-NAME":
      case "DOMAIN-TYPE":
      case "ENCODING":
      case "END-USER-PROMPT":
      case "ENTRY-TYPES-LIST":
      case "ERROR-STRING":
      case "EVENT-GROUP-ID":
      case "EVENT-PROCEDURE":
      case "EVENT-TYPE":
      case "EXCLUSIVE-ID":
      case "FILE-NAME":
      case "FILE-TYPE":
      case "FILL-MODE":
      case "FILL-WHERE-STRING":
      case "FORM-INPUT":
      case "FORMAT":
      case "FRAME-NAME":
      case "FULL-PATHNAME":
      case "FUNCTION":
      case "HELP":
      case "HTML-CHARSET":
      case "HTML-END-OF-LINE":
      case "HTML-END-OF-PAGE":
      case "HTML-FRAME-BEGIN":
      case "HTML-FRAME-END":
      case "HTML-HEADER-BEGIN":
      case "HTML-HEADER-END":
      case "HTML-TITLE-BEGIN":
      case "HTML-TITLE-END":
      case "ICFPARAMETER":
      case "ICON":
      case "IMAGE":
      case "IMAGE-DOWN":
      case "IMAGE-INSENSITIVE":
      case "IMAGE-UP":
      case "INDEX-INFORMATION":
      case "INITIAL":
      case "INTERNAL-ENTRIES":
      case "KEYS":
      case "LABEL":
      case "LANGUAGES":
      case "LIBRARY":
      case "LIBRARY-CALLING-CONVENTION":
      case "LIST-ITEM-PAIRS":
      case "LIST-ITEMS":
      case "LOCAL-HOST":
      case "LOCAL-NAME":
      case "LOCATOR-PUBLIC-ID":
      case "LOCATOR-SYSTEM-ID":
      case "LOCATOR-TYPE":
      case "LOG-ENTRY-TYPES":
      case "LOGFILE-NAME":
      case "LOGIN-HOST":
      case "LOGIN-STATE":
      case "MD5-VALUE":
      case "MENU-KEY":
      case "MOUSE-POINTER":
      case "NAME":
      case "NAMESPACE-PREFIX":
      case "NAMESPACE-URI":
      case "NODE-VALUE":
      case "NONAMESPACE-SCHEMA-LOCATION":
      case "NUMERIC-DECIMAL-POINT":
      case "NUMERIC-FORMAT":
      case "NUMERIC-SEPARATOR":
      case "OPTIONS":
      case "PARAMETER":
      case "PARENT-FIELDS-AFTER":
      case "PARENT-FIELDS-BEFORE":
      case "PATHNAME":
      case "PBE-HASH-ALGORITHM":
      case "PREPARE-STRING":
      case "PRIMARY":
      case "PRIMARY-PASSPHRASE":
      case "PRINTER-NAME":
      case "PRINTER-PORT":
      case "PRIVATE-DATA":
      case "PROCEDURE-NAME":
      case "PROCEDURE-TYPE":
      case "PROXY-PASSWORD":
      case "PROXY-USERID":
      case "PUBLIC-ID":
      case "PUBLISHED-EVENTS":
      case "QUALIFIED-USER-ID":
      case "RADIO-BUTTONS":
      case "RELATION-FIELDS":
      case "REMOTE-HOST":
      case "RETURN-VALUE-DATA-TYPE":
      case "RETURN-VALUE-DLL-TYPE":
      case "ROLE":
      case "ROLES":
      case "SAVE-WHERE-STRING":
      case "SCHEMA-CHANGE":
      case "SCHEMA-LOCATION":
      case "SCHEMA-MARSHAL":
      case "SCHEMA-PATH":
      case "SCREEN-VALUE":
      case "SELECTION-TEXT":
      case "SERIALIZE-NAME":
      case "SERVER-CONNECTION-CONTEXT":
      case "SERVER-CONNECTION-ID":
      case "SERVER-OPERATING-MODE":
      case "SESSION-ID":
      case "SMALL-ICON":
      case "SOAP-FAULT-ACTOR":
      case "SOAP-FAULT-CODE":
      case "SOAP-FAULT-MISUNDERSTOOD-HEADER":
      case "SOAP-FAULT-NODE":
      case "SOAP-FAULT-ROLE":
      case "SOAP-FAULT-STRING":
      case "SOAP-FAULT-SUBCODE":
      case "SOAP-VERSION":
      case "SSL-SERVER-NAME":
      case "STARTUP-PARAMETERS":
      case "STATE-DETAIL":
      case "STREAM":
      case "SUBTYPE":
      case "SUPER-PROCEDURES":
      case "SUPPRESS-WARNINGS-LIST":
      case "SYMMETRIC-ENCRYPTION-ALGORITHM":
      case "SYMMETRIC-SUPPORT":
      case "SYSTEM-ID":
      case "TABLE":
      case "TABLE-CRC-LIST":
      case "TABLE-LIST":
      case "TEMP-DIRECTORY":
      case "TIC-MARKS":
      case "TIME-SOURCE":
      case "TITLE":
      case "TOOLTIP":
      case "TYPE":
      case "UNDO-THROW-SCOPE":
      case "URL":
      case "URL-PASSWORD":
      case "URL-USERID":
      case "USER-ID":
      case "VALIDATE-EXPRESSION":
      case "VALIDATE-MESSAGE":
      case "VALUE":
      case "VERSION":
      case "VIEW-AS":
      case "WHERE-STRING":
      case "WINDOW-SYSTEM":
      case "XCODE-SESSION-KEY":
      case "XML-DATA-TYPE":
      case "XML-NODE-NAME":
      case "XML-NODE-TYPE":
      case "XML-SCHEMA-PATH":
        return DataType.CHARACTER;
      case "FILE-CREATE-DATE":
      case "FILE-MOD-DATE":
        return DataType.DATE;
      case "LOGIN-EXPIRATION-TIMESTAMP":
      case "SEAL-TIMESTAMP":
        return DataType.DATETIME_TZ;
      case "AFTER-BUFFER":
      case "AFTER-TABLE":
      case "ASYNC-REQUEST-HANDLE":
      case "BACKGROUND":
      case "BEFORE-BUFFER":
      case "BEFORE-TABLE":
      case "BUFFER-FIELD":
      case "BUFFER-HANDLE":
      case "CANCEL-BUTTON":
      case "CHILD-BUFFER":
      case "CURRENT-COLUMN":
      case "CURRENT-WINDOW":
      case "DATA-SOURCE":
      case "DATASET":
      case "DEFAULT-BUFFER-HANDLE":
      case "DEFAULT-BUTTON":
      case "ERROR-OBJECT-DETAIL":
      case "EVENT-PROCEDURE-CONTEXT":
      case "FIRST-ASYNC-REQUEST":
      case "FIRST-BUFFER":
      case "FIRST-CHILD":
      case "FIRST-COLUMN":
      case "FIRST-DATA-SOURCE":
      case "FIRST-DATASET":
      case "FIRST-PROCEDURE":
      case "FIRST-QUERY":
      case "FIRST-SERVER":
      case "FIRST-SERVER-SOCKET":
      case "FIRST-SOCKET":
      case "FIRST-TAB-ITEM":
      case "FRAME":
      case "HANDLE":
      case "HANDLER":
      case "IN-HANDLE":
      case "INSTANTIATING-PROCEDURE":
      case "LAST-ASYNC-REQUEST":
      case "LAST-CHILD":
      case "LAST-PROCEDURE":
      case "LAST-SERVER":
      case "LAST-SERVER-SOCKET":
      case "LAST-SOCKET":
      case "LAST-TAB-ITEM":
      case "MENU-BAR":
      case "NEXT-COLUMN":
      case "NEXT-SIBLING":
      case "NEXT-TAB-ITEM":
      case "ORIGIN-HANDLE":
      case "OWNER":
      case "OWNER-DOCUMENT":
      case "PARENT":
      case "PARENT-BUFFER":
      case "PARENT-RELATION":
      case "PERSISTENT-PROCEDURE":
      case "POPUP-MENU":
      case "PREV-COLUMN":
      case "PREV-SIBLING":
      case "PREV-TAB-ITEM":
      case "QUERY":
      case "SERVER":
      case "SIDE-LABEL-HANDLE":
      case "SOAP-FAULT-DETAIL":
      case "TABLE-HANDLE":
      case "TOP-NAV-QUERY":
      case "TRANS-INIT-PROCEDURE":
      case "TRANSACTION":
      case "WIDGET-ENTER":
      case "WIDGET-LEAVE":
      case "WINDOW":
      case "X-DOCUMENT":
        return DataType.HANDLE;
      case "ASYNC-REQUEST-COUNT":
      case "BATCH-SIZE":
      case "BGCOLOR":
      case "BORDER-BOTTOM-PIXELS":
      case "BORDER-LEFT-PIXELS":
      case "BORDER-RIGHT-PIXELS":
      case "BORDER-TOP-PIXELS":
      case "BUFFER-CHARS":
      case "BUFFER-GROUP-ID":
      case "BUFFER-LINES":
      case "BUFFER-PARTITION-ID":
      case "BUFFER-TENANT-ID":
      case "BYTES-READ":
      case "BYTES-WRITTEN":
      case "CACHE":
      case "CALL-TYPE":
      case "CHILD-NUM":
      case "CODE":
      case "COLUMN-BGCOLOR":
      case "COLUMN-DCOLOR":
      case "COLUMN-FGCOLOR":
      case "COLUMN-FONT":
      case "COLUMN-PFCOLOR":
      case "CONTEXT-HELP-ID":
      case "CRC-VALUE":
      case "CURRENT-RESULT-ROW":
      case "CURSOR-CHAR":
      case "CURSOR-LINE":
      case "CURSOR-OFFSET":
      case "DCOLOR":
      case "DDE-ERROR":
      case "DDE-ID":
      case "DECIMALS":
      case "DISPLAY-TIMEZONE":
      case "DOWN":
      case "EDGE-PIXELS":
      case "ENTITY-EXPANSION-LIMIT":
      case "ERROR-COLUMN":
      case "ERROR-ROW":
      case "EXTENT":
      case "FGCOLOR":
      case "FILE-CREATE-TIME":
      case "FILE-MOD-TIME":
      case "FILE-OFFSET":
      case "FILE-SIZE":
      case "FOCUSED-ROW":
      case "FONT":
      case "FRAME-COL":
      case "FRAME-ROW":
      case "FRAME-SPACING":
      case "FRAME-X":
      case "FRAME-Y":
      case "FREQUENCY":
      case "FULL-HEIGHT-PIXELS":
      case "FULL-WIDTH-PIXELS":
      case "GRID-FACTOR-HORIZONTAL":
      case "GRID-FACTOR-VERTICAL":
      case "GRID-UNIT-HEIGHT-PIXELS":
      case "GRID-UNIT-WIDTH-PIXELS":
      case "HEIGHT-PIXELS":
      case "HWND":
      case "INDEX":
      case "INNER-CHARS":
      case "INNER-LINES":
      case "ITEMS-PER-ROW":
      case "LABEL-BGCOLOR":
      case "LABEL-DCOLOR":
      case "LABEL-FGCOLOR":
      case "LABEL-FONT":
      case "LENGTH":
      case "LINE":
      case "LOCAL-PORT":
      case "LOCATOR-COLUMN-NUMBER":
      case "LOCATOR-LINE-NUMBER":
      case "LOG-THRESHOLD":
      case "LOGGING-LEVEL":
      case "MAX-CHARS":
      case "MAX-DATA-GUESS":
      case "MAX-HEIGHT-PIXELS":
      case "MAX-VALUE":
      case "MAX-WIDTH-PIXELS":
      case "MAXIMUM-LEVEL":
      case "MENU-MOUSE":
      case "MESSAGE-AREA-FONT":
      case "MIN-COLUMN-WIDTH-PIXELS":
      case "MIN-HEIGHT-PIXELS":
      case "MIN-VALUE":
      case "MIN-WIDTH-PIXELS":
      case "MULTITASKING-INTERVAL":
      case "NUM-BUFFERS":
      case "NUM-BUTTONS":
      case "NUM-CHILD-RELATIONS":
      case "NUM-CHILDREN":
      case "NUM-COLUMNS":
      case "NUM-DROPPED-FILES":
      case "NUM-ENTRIES":
      case "NUM-FIELDS":
      case "NUM-FORMATS":
      case "NUM-HEADER-ENTRIES":
      case "NUM-ITEMS":
      case "NUM-LINES":
      case "NUM-LOCKED-COLUMNS":
      case "NUM-LOG-FILES":
      case "NUM-MESSAGES":
      case "NUM-PARAMETERS":
      case "NUM-REFERENCES":
      case "NUM-RELATIONS":
      case "NUM-REPLACED":
      case "NUM-RESULTS":
      case "NUM-SELECTED-ROWS":
      case "NUM-SELECTED-WIDGETS":
      case "NUM-SOURCE-BUFFERS":
      case "NUM-TABS":
      case "NUM-TO-RETAIN":
      case "NUM-TOP-BUFFERS":
      case "NUM-VISIBLE-COLUMNS":
      case "ORDINAL":
      case "PARSE-STATUS":
      case "PBE-KEY-ROUNDS":
      case "PFCOLOR":
      case "PIXELS-PER-COLUMN":
      case "PIXELS-PER-ROW":
      case "POSITION":
      case "PRINTER-CONTROL-HANDLE":
      case "PRINTER-HDC":
      case "RECID":
      case "RECORD-LENGTH":
      case "REMOTE-PORT":
      case "RESTART-ROW":
      case "ROW-HEIGHT-PIXELS":
      case "ROW-STATE":
      case "SCREEN-LINES":
      case "SELECTION-END":
      case "SELECTION-START":
      case "SEPARATOR-FGCOLOR":
      case "SORT-NUMBER":
      case "STATUS-AREA-FONT":
      case "TAB-POSITION":
      case "TABLE-NUMBER":
      case "TIMEZONE":
      case "TITLE-BGCOLOR":
      case "TITLE-DCOLOR":
      case "TITLE-FGCOLOR":
      case "TITLE-FONT":
      case "UNIQUE-ID":
      case "VIRTUAL-HEIGHT-PIXELS":
      case "VIRTUAL-WIDTH-PIXELS":
      case "WIDGET-ID":
      case "WIDTH-PIXELS":
      case "WINDOW-STATE":
      case "WORK-AREA-HEIGHT-PIXELS":
      case "WORK-AREA-WIDTH-PIXELS":
      case "WORK-AREA-X":
      case "WORK-AREA-Y":
      case "WRITE-STATUS":
      case "X":
      case "XML-ENTITY-EXPANSION-LIMIT":
      case "Y":
      case "YEAR-OFFSET":
        return DataType.INTEGER;
      case "BORDER-BOTTOM-CHARS":
      case "BORDER-LEFT-CHARS":
      case "BORDER-RIGHT-CHARS":
      case "BORDER-TOP-CHARS":
      case "COLUMN":
      case "EDGE-CHARS":
      case "FULL-HEIGHT-CHARS":
      case "FULL-WIDTH-CHARS":
      case "GRID-UNIT-HEIGHT-CHARS":
      case "GRID-UNIT-WIDTH-CHARS":
      case "HEIGHT-CHARS":
      case "MAX-HEIGHT-CHARS":
      case "MAX-WIDTH-CHARS":
      case "MIN-COLUMN-WIDTH-CHARS":
      case "MIN-HEIGHT-CHARS":
      case "MIN-WIDTH-CHARS":
      case "ROW":
      case "ROW-HEIGHT-CHARS":
      case "VIRTUAL-HEIGHT-CHARS":
      case "VIRTUAL-WIDTH-CHARS":
      case "WIDTH-CHARS":
        return DataType.DECIMAL;
      case "ACTIVE":
      case "ALLOW-COLUMN-SEARCHING":
      case "ALWAYS-ON-TOP":
      case "AMBIGUOUS":
      case "APPL-ALERT-BOXES":
      case "ASYNCHRONOUS":
      case "ATTR-SPACE":
      case "AUTO-COMPLETION":
      case "AUTO-DELETE":
      case "AUTO-DELETE-XML":
      case "AUTO-END-KEY":
      case "AUTO-GO":
      case "AUTO-INDENT":
      case "AUTO-RESIZE":
      case "AUTO-RETURN":
      case "AUTO-SYNCHRONIZE":
      case "AUTO-VALIDATE":
      case "AUTO-ZAP":
      case "AVAILABLE":
      case "BASIC-LOGGING":
      case "BATCH-MODE":
      case "BLANK":
      case "BLOCK-ITERATION-DISPLAY":
      case "BOX":
      case "BOX-SELECTABLE":
      case "CAN-CREATE":
      case "CAN-DELETE":
      case "CAN-DO-DOMAIN-SUPPORT":
      case "CAN-READ":
      case "CAN-WRITE":
      case "CANCELLED":
      case "CAREFUL-PAINT":
      case "CASE-SENSITIVE":
      case "CENTERED":
      case "CHECKED":
      case "COLUMN-MOVABLE":
      case "COLUMN-READ-ONLY":
      case "COLUMN-RESIZABLE":
      case "COLUMN-SCROLLING":
      case "COMPLETE":
      case "CONTEXT-HELP":
      case "CONTROL-BOX":
      case "CONVERT-3D-COLORS":
      case "CURRENT-CHANGED":
      case "CURRENT-ROW-MODIFIED":
      case "DATA-ENTRY-RETURN":
      case "DATA-SOURCE-MODIFIED":
      case "DEBLANK":
      case "DEBUG-ALERT":
      case "DEFAULT":
      case "DEFAULT-COMMIT":
      case "DISABLE-AUTO-ZAP":
      case "DRAG-ENABLED":
      case "DROP-TARGET":
      case "DYNAMIC":
      case "EDIT-CAN-PASTE":
      case "EDIT-CAN-UNDO":
      case "EMPTY":
      case "ERROR":
      case "ERROR-STACK-TRACE":
      case "EXECUTION-LOG":
      case "EXPAND":
      case "EXPANDABLE":
      case "FILLED":
      case "FIT-LAST-COLUMN":
      case "FLAT-BUTTON":
      case "FOCUSED-ROW-SELECTED":
      case "FOREGROUND":
      case "FOREIGN-KEY-HIDDEN":
      case "FORMATTED":
      case "FORWARD-ONLY":
      case "FRAGMENT":
      case "GRAPHIC-EDGE":
      case "GRID-SNAP":
      case "GRID-VISIBLE":
      case "GROUP-BOX":
      case "HAS-LOBS":
      case "HAS-RECORDS":
      case "HIDDEN":
      case "HORIZONTAL":
      case "IGNORE-CURRENT-MODIFIED":
      case "IMMEDIATE-DISPLAY":
      case "INHERIT-BGCOLOR":
      case "INHERIT-FGCOLOR":
      case "IS-CLASS":
      case "IS-JSON":
      case "IS-MULTI-TENANT":
      case "IS-OPEN":
      case "IS-PARTITIONED":
      case "IS-XML":
      case "KEEP-CONNECTION-OPEN":
      case "KEEP-FRAME-Z-ORDER":
      case "KEEP-SECURITY-CACHE":
      case "KEY":
      case "LABELS":
      case "LABELS-HAVE-COLONS":
      case "LARGE":
      case "LARGE-TO-SMALL":
      case "LAST-BATCH":
      case "LITERAL-QUESTION":
      case "LOCKED":
      case "MANDATORY":
      case "MANUAL-HIGHLIGHT":
      case "MAX-BUTTON":
      case "MERGE-BY-FIELD":
      case "MESSAGE-AREA":
      case "MIN-BUTTON":
      case "MIN-SCHEMA-MARSHAL":
      case "MODIFIED":
      case "MOVABLE":
      case "MULTI-COMPILE":
      case "MULTIPLE":
      case "MUST-UNDERSTAND":
      case "NEEDS-APPSERVER-PROMPT":
      case "NEEDS-PROMPT":
      case "NESTED":
      case "NEW":
      case "NEW-ROW":
      case "NO-CURRENT-VALUE":
      case "NO-EMPTY-SPACE":
      case "NO-FOCUS":
      case "NO-SCHEMA-MARSHAL":
      case "NO-VALIDATE":
      case "ON-FRAME-BORDER":
      case "OVERLAY":
      case "PAGE-BOTTOM":
      case "PAGE-TOP":
      case "PARENT-ID-RELATION":
      case "PASSWORD-FIELD":
      case "PERSISTENT":
      case "PERSISTENT-CACHE-DISABLED":
      case "POPUP-ONLY":
      case "PREFER-DATASET":
      case "PREPARED":
      case "PROGRESS-SOURCE":
      case "PROXY":
      case "QUERY-OFF-END":
      case "QUIT":
      case "READ-ONLY":
      case "RECURSIVE":
      case "REFRESHABLE":
      case "REJECTED":
      case "RELATIONS-ACTIVE":
      case "REMOTE":
      case "REPOSITION":
      case "RESIZABLE":
      case "RESIZE":
      case "RETAIN-SHAPE":
      case "RETURN-INSERTED":
      case "ROUNDED":
      case "ROW-MARKERS":
      case "ROW-RESIZABLE":
      case "SCROLL-BARS":
      case "SCROLLABLE":
      case "SCROLLBAR-HORIZONTAL":
      case "SCROLLBAR-VERTICAL":
      case "SELECTABLE":
      case "SELECTED":
      case "SENSITIVE":
      case "SEPARATORS":
      case "SERIALIZE-HIDDEN":
      case "SERVER-CONNECTION-BOUND":
      case "SERVER-CONNECTION-BOUND-REQUEST":
      case "SESSION-END":
      case "SHOW-IN-TASKBAR":
      case "SIDE-LABELS":
      case "SINGLE-RUN":
      case "SINGLETON":
      case "SKIP-DELETED-RECORD":
      case "SMALL-TITLE":
      case "SORT":
      case "SORT-ASCENDING":
      case "STANDALONE":
      case "STATUS-AREA":
      case "STOP":
      case "STOPPED":
      case "STRETCH-TO-FIT":
      case "STRICT":
      case "STRICT-ENTITY-RESOLUTION":
      case "SUPPRESS-NAMESPACE-PROCESSING":
      case "SUPPRESS-WARNINGS":
      case "SYSTEM-ALERT-BOXES":
      case "TAB-STOP":
      case "TEXT-SELECTED":
      case "THREAD-SAFE":
      case "THREE-D":
      case "TOGGLE-BOX":
      case "TOOLTIPS":
      case "TOP-ONLY":
      case "TRACKING-CHANGES":
      case "TRANSPARENT":
      case "UNDO":
      case "UNIQUE-MATCH":
      case "V6DISPLAY":
      case "VALIDATE-XML":
      case "VALIDATION-ENABLED":
      case "VIEW-FIRST-COLUMN-ON-REOPEN":
      case "VISIBLE":
      case "WARNING":
      case "WC-ADMIN-APP":
      case "WORD-WRAP":
      case "XML-STRICT-ENTITY-RESOLUTION":
      case "XML-SUPPRESS-NAMESPACE-PROCESSING":
        return DataType.LOGICAL;
      case "ENCRYPTION-SALT":
      case "FORM-LONG-INPUT":
      case "SYMMETRIC-ENCRYPTION-IV":
      case "SYMMETRIC-ENCRYPTION-KEY":
        return DataType.RAW;
      case "AFTER-ROWID":
      case "BEFORE-ROWID":
      case "DATA-SOURCE-ROWID":
      case "NEXT-ROWID":
      case "ORIGIN-ROWID":
      case "RESTART-ROWID":
      case "ROWID":
        return DataType.ROWID;
      default:
        return DataType.NOT_COMPUTED;
    }
  }

  static DataType getStandardMethodDataType(String id) {
    switch (id) {
      case "ADD-NEW-FIELD":
        return DataType.LOGICAL;
      // TODO Full list
      default:
        return DataType.NOT_COMPUTED;
    }
  }

  static DataType getObjectMethodDataType(Function<String, ITypeInfo> provider, JPNode node, ITypeInfo info, String methodName) {
    // Create array of dataTypes
    List<JPNode> paramItems = node.getDirectChildren(ABLNodeType.PARAMETER_ITEM);
    DataType[] params = new DataType[paramItems.size()];
    int zz = 0;
    for (JPNode ch : paramItems) {
      DataType dt = DataType.UNKNOWN;
      for (JPNode ch2 : ch.getDirectChildren()) {
        if ((dt == DataType.UNKNOWN) && ch2.isIExpression()) {
          dt = ch2.asIExpression().getDataType();
        }
      }
      params[zz++] = dt;
    }

    if (info != null) {
      IMethodElement methd = info.getMethod(provider, methodName, params);
      return methd == null ? DataType.NOT_COMPUTED : methd.getReturnType();
    } else {
      return DataType.NOT_COMPUTED;
    }
  }

  static DataType getObjectAttributeDataType(IProparseEnvironment session, ITypeInfo info, String propName,
      boolean firstLevel) {
    while (info != null) {
      for (IPropertyElement prop : info.getProperties()) {
        if (prop.getName().equalsIgnoreCase(propName))
          return prop.getVariable().getDataType();
      }
      if (firstLevel) {
        for (IVariableElement v : info.getVariables()) {
          if (v.getName().equalsIgnoreCase(propName))
            return v.getDataType();
        }
        firstLevel = false;
      }
      info = session.getTypeInfo(info.getParentTypeName());
    }
    return DataType.NOT_COMPUTED;
  }

}
