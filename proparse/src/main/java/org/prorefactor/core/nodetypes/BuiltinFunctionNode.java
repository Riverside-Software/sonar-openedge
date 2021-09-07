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

import java.util.List;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.ProToken;

import eu.rssw.pct.elements.DataType;

public class BuiltinFunctionNode extends JPNode implements IExpression {

  public BuiltinFunctionNode(ProToken t, JPNode parent, int num, boolean hasChildren) {
    super(t, parent, num, hasChildren);
  }

  @Override
  public boolean isExpression() {
    return true;
  }

  public DataType getAddIntervalDataType() {
    List<JPNode> nodes = getFirstChild().queryExpressions();
    if (nodes.size() != 3) {
      return DataType.DATE;
    }
    return ((IExpression) nodes.get(0)).getDataType();
  }

  public DataType getIfDataType() {
    List<JPNode> nodes = getFirstChild().queryExpressions();
    if (nodes.size() != 3) {
      return DataType.NOT_COMPUTED;
    }
    return ((IExpression) nodes.get(1)).getDataType();
  }

  public DataType getCastDataType() {
    JPNode exprNode = getFirstChild().findDirectChild(ABLNodeType.TYPE_NAME);
    if (exprNode == null)
      return new DataType("Progress.Lang.Object");
    else
      return new DataType(((TypeNameNode) exprNode).getQualName());
  }

  public DataType getMinMaxDataType() {
    List<JPNode> nodes = getFirstChild().queryExpressions();
    boolean hasDecimal = false;
    for (JPNode node : nodes) {
      if( ((IExpression) node).getDataType() == DataType.DECIMAL) 
        hasDecimal = true;
    }
    return hasDecimal ? DataType.DECIMAL : DataType.INTEGER;
  }

  @Override
  public DataType getDataType() {
    switch (getFirstChild().getNodeType()) {
      case CAST:
        return getCastDataType();
      case GETCLASS:
        return new DataType("Progress.Lang.Class");
      case IF:
        return getIfDataType();
      case BOX:
        return new DataType("System.Object"); // Use second parameter if available
      case ADDINTERVAL:
        return getAddIntervalDataType();
      case ISODATE:
        return DataType.DATE;
      case MAXIMUM:
      case MINIMUM:
        return getMinMaxDataType();
      case FRAMECOL:
      case FRAMEROW:
        return DataType.DECIMAL;
      case ABSOLUTE:
      case ASC:
      case BUFFERGROUPID:
      case BUFFERTENANTID:
      case COUNTOF:
      case CURRENTRESULTROW:
      case CURRENTVALUE:
      case DAY:
      case DBTASKID:
      case DYNAMICCURRENTVALUE:
      case EXTENT:
      case FRAMEDOWN:
      case FRAMELINE:
      case GETBITS: // FIXME
      case GETBYTE:
      case GETBYTEORDER:
      case GETEFFECTIVETENANTID:
      case GETLONG:
      case GETSHORT:
      case GETUNSIGNEDSHORT:
      case INDEX:
      case INTEGER:
      case KEYCODE:
      case LENGTH:
      case LINECOUNTER:
      case LOOKUP:
      case MONTH:
      case MTIME:
      case NEXTVALUE:
      case NUMENTRIES:
      case NUMRESULTS:
      case PAGENUMBER:
      case PAGESIZE:
      case RANDOM:
      case RGBVALUE:
      case RECORDLENGTH:
      case RINDEX:
      case ROUND:
      case ROWSTATE:
      case TENANTID:
      case TENANTNAMETOID:
      case TIMEZONE:
      case USERID:
      case WEEKDAY:
      case YEAR:
        return DataType.INTEGER;
      case ETIME:
      case GETINT64:
      case GETPOINTERVALUE:
      case GETSIZE:
      case GETUNSIGNEDLONG:
      case INT64:
      case INTERVAL:
      case SEEK:
        return DataType.INT64;
      case ALIAS:
      case BUFFERGROUPNAME:
      case BUFFERTENANTNAME:
      case CAPS:
      case CHR:
      case CODEPAGECONVERT:
      case DBCODEPAGE:
      case DBCOLLATION:
      case DBPARAM:
      case DBTYPE:
      case DBVERSION:
      case ENCODE:
      case ENTRY:
      case FILL:
      case GETCODEPAGE:
      case GETCODEPAGES:
      case GETCOLLATIONS:
      case GETEFFECTIVETENANTNAME:
      case GETSTRING:
      case GUID:
      case HEXENCODE:
      case KBLABEL:
      case KEYFUNCTION:
      case KEYLABEL:
      case KEYWORD:
      case KEYWORDALL:
      case LC:
      case LDBNAME:
      case LEFTTRIM:
      case LIBRARY:
      case LISTEVENTS:
      case LISTQUERYATTRS:
      case LISTSETATTRS:
      case LISTWIDGETS:
      case MEMBER:
      case NORMALIZE:
      case OSGETENV:
      case PDBNAME:
      case PROGRAMNAME:
      case PROVERSION:
      case QUOTER:
      case REPLACE:
      case RIGHTTRIM:
      case SDBNAME:
      case SEARCH:
      case SSLSERVERNAME:
      case STRING:
      case SUBSTITUTE:
      case SUBSTRING:
      case TENANTNAME:
      case TRIM:
      case USER:
        return DataType.CHARACTER;
      case AMBIGUOUS:
      case AUDITENABLED:
      case AVAILABLE:
      case CANDO:
      case CANFIND:
      case CANQUERY:
      case CANSET:
      case COMPARE:
      case COMPARES:
      case CONNECTED:
      case CURRENTCHANGED:
      case DATASOURCEMODIFIED:
      case ERROR:
      case FIRST:
      case FIRSTOF:
      case ISCODEPAGEFIXED:
      case ISCOLUMNCODEPAGE:
      case ISDBMULTITENANT:
      case ISLEADBYTE:
      case LAST:
      case LASTOF:
      case LOCKED:
      case LOGICAL:
      case NEW:
      case QUERYOFFEND:
      case REJECTED:
      case SETDBCLIENT:
      case SETEFFECTIVETENANT:
      case SETUSERID:
      case TYPEOF:
      case VALIDEVENT:
      case VALIDHANDLE:
      case VALIDOBJECT:
        return DataType.LOGICAL;
      case BASE64ENCODE:
        return DataType.LONGCHAR;
      case BASE64DECODE:
      case DECRYPT:
      case ENCRYPT:
      case GETBYTES:
        return DataType.MEMPTR;
      case GENERATEPBEKEY:
      case HEXDECODE:
      case MD5DIGEST:
      case MESSAGEDIGEST:
      case RAW:
      case SHA1DIGEST:
        return DataType.RAW;
      case DECIMAL:
      case EXP:
      case GETDOUBLE:
      case GETFLOAT:
      case LOG:
      case SQRT:
      case TRUNCATE:
        return DataType.DECIMAL;
      case DATE:
        return DataType.DATE;
      case DATETIME:
        return DataType.DATETIME;
      case DATETIMETZ:
        return DataType.DATETIME_TZ;
      case GETDBCLIENT:
      case HANDLE:
      case WIDGETHANDLE:
        return DataType.HANDLE;
      case LOADPICTURE:
        return DataType.COMPONENT_HANDLE;
      case RECID:
        return DataType.RECID;
      case ROWID:
      case TOROWID:
        return DataType.ROWID;
      case DYNAMICCAST:
      case DYNAMICPROPERTY:
      case DYNAMICFUNCTION:
      case DYNAMICINVOKE:
      case UNBOX:
        return DataType.RUNTYPE;
      default:
        return DataType.NOT_COMPUTED;
    }
  }

  /**
   * @return True if function call changes the state of the AVM
   */
  public boolean hasSideEffect() {
    switch (getFirstChild().getNodeType()) {
      case ETIME:
      case NEXTVALUE:
      case SETDBCLIENT:
      case SETEFFECTIVETENANT:
      case SETUSERID:
      case DYNAMICCAST:
      case DYNAMICPROPERTY:
      case DYNAMICFUNCTION:
      case DYNAMICINVOKE:
      case NEW:
      case RETRY:
      case SUPER:
        return true;
      default:
        return false;
    }
  }
}
