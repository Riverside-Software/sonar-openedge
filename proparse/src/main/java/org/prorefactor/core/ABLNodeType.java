/********************************************************************************
 * Copyright (c) 2015-2022 Riverside Software
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
package org.prorefactor.core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;

import org.antlr.v4.runtime.Token;
import org.prorefactor.proparse.antlr4.Proparse;

import com.google.common.base.Strings;

import eu.rssw.pct.elements.DataType;

public enum ABLNodeType {
  // Placeholders and unknown tokens
  EMPTY_NODE(-1000, NodeTypesOption.PLACEHOLDER),
  INVALID_NODE(Token.INVALID_TYPE, NodeTypesOption.PLACEHOLDER),
  EOF_ANTLR4(Token.EOF, NodeTypesOption.PLACEHOLDER),

  // Preprocessor directives
  AMPANALYZERESUME(Proparse.AMPANALYZERESUME, NodeTypesOption.PREPROCESSOR),
  AMPANALYZESUSPEND(Proparse.AMPANALYZESUSPEND, NodeTypesOption.PREPROCESSOR),
  AMPELSE(Proparse.AMPELSE, NodeTypesOption.PREPROCESSOR),
  AMPELSEIF(Proparse.AMPELSEIF, NodeTypesOption.PREPROCESSOR),
  AMPENDIF(Proparse.AMPENDIF, NodeTypesOption.PREPROCESSOR),
  AMPGLOBALDEFINE(Proparse.AMPGLOBALDEFINE, NodeTypesOption.PREPROCESSOR),
  AMPIF(Proparse.AMPIF, NodeTypesOption.PREPROCESSOR),
  AMPMESSAGE(Proparse.AMPMESSAGE, NodeTypesOption.PREPROCESSOR),
  AMPSCOPEDDEFINE(Proparse.AMPSCOPEDDEFINE, NodeTypesOption.PREPROCESSOR),
  AMPTHEN(Proparse.AMPTHEN, NodeTypesOption.PREPROCESSOR),
  AMPUNDEFINE(Proparse.AMPUNDEFINE, NodeTypesOption.PREPROCESSOR),
  PROPARSEDIRECTIVE(Proparse.PROPARSEDIRECTIVE, NodeTypesOption.PREPROCESSOR),
  INCLUDEDIRECTIVE(Proparse.INCLUDEDIRECTIVE, NodeTypesOption.PLACEHOLDER),
  PREPROEXPR_TRUE(Proparse.PREPROEXPRTRUE, NodeTypesOption.PREPROCESSOR),
  PREPROEXPR_FALSE(Proparse.PREPROEXPRFALSE, NodeTypesOption.PREPROCESSOR),
  // Next elements are not used anymore
  // INCLUDEREFARG(Proparse.INCLUDEREFARG),
  // GLOBAL_DEFINE(Proparse.GLOBALDEFINE, NodeTypesOption.PREPROCESSOR),
  // SCOPEDDEFINE(Proparse.SCOPEDDEFINE, NodeTypesOption.PREPROCESSOR),
  // PREPROCESSDIRECTIVE(Proparse.PREPROCESSDIRECTIVE, NodeTypesOption.PREPROCESSOR),
  // PREPROCESSELSE(Proparse.PREPROCESSELSE, NodeTypesOption.PREPROCESSOR),
  // PREPROCESSELSEIF(Proparse.PREPROCESSELSEIF, NodeTypesOption.PREPROCESSOR),
  // PREPROCESSENDIF(Proparse.PREPROCESSENDIF, NodeTypesOption.PREPROCESSOR),
  // PREPROCESSIF(Proparse.PREPROCESSIF, NodeTypesOption.PREPROCESSOR),
  // PREPROCESSJMESSAGE(Proparse.PREPROCESSJMESSAGE, NodeTypesOption.PREPROCESSOR),
  // PREPROCESSMESSAGE(Proparse.PREPROCESSMESSAGE, NodeTypesOption.PREPROCESSOR),
  // PREPROCESSUNDEFINE(Proparse.PREPROCESSUNDEFINE, NodeTypesOption.PREPROCESSOR),

  // Symbols
  BACKSLASH(Proparse.BACKSLASH, "\\", NodeTypesOption.SYMBOL),
  BACKTICK(Proparse.BACKTICK, "`", NodeTypesOption.SYMBOL),
  CARET(Proparse.CARET, "^", NodeTypesOption.SYMBOL),
  COMMA(Proparse.COMMA, ",", NodeTypesOption.SYMBOL),
  DIVIDE(Proparse.DIVIDE, "/", NodeTypesOption.SYMBOL),
  DOUBLECOLON(Proparse.DOUBLECOLON, "::", NodeTypesOption.SYMBOL),
  DOUBLEQUOTE(Proparse.DOUBLEQUOTE, "\"", NodeTypesOption.SYMBOL),
  EQUAL(Proparse.EQUAL, "=", NodeTypesOption.SYMBOL),
  EXCLAMATION(Proparse.EXCLAMATION, "!", NodeTypesOption.SYMBOL),
  GTOREQUAL(Proparse.GTOREQUAL, ">=", NodeTypesOption.SYMBOL),
  GTORLT(Proparse.GTORLT, "<>", NodeTypesOption.SYMBOL),
  LEFTANGLE(Proparse.LEFTANGLE, "<", NodeTypesOption.SYMBOL),
  LEFTBRACE(Proparse.LEFTBRACE, "[", NodeTypesOption.SYMBOL),
  LEFTCURLY(Proparse.LEFTCURLY, "{", NodeTypesOption.SYMBOL),
  LEFTPAREN(Proparse.LEFTPAREN, "(", NodeTypesOption.SYMBOL),
  LEXAT(Proparse.LEXAT, "@", NodeTypesOption.SYMBOL),
  LEXCOLON(Proparse.LEXCOLON, ":", NodeTypesOption.SYMBOL),
  LTOREQUAL(Proparse.LTOREQUAL, "<=", NodeTypesOption.SYMBOL),
  MINUS(Proparse.MINUS, "-", NodeTypesOption.SYMBOL),
  MULTIPLY(Proparse.MULTIPLY, "*", NodeTypesOption.SYMBOL),
  NAMEDOT(Proparse.NAMEDOT, ".", NodeTypesOption.SYMBOL),
  OBJCOLON(Proparse.OBJCOLON, ":", NodeTypesOption.SYMBOL),
  PERIOD(Proparse.PERIOD, ".", NodeTypesOption.SYMBOL),
  PERIODSTART(Proparse.PERIODSTART, ".", NodeTypesOption.SYMBOL),
  PIPE(Proparse.PIPE, "|", NodeTypesOption.SYMBOL),
  PLUS(Proparse.PLUS, "+", NodeTypesOption.SYMBOL),
  RIGHTANGLE(Proparse.RIGHTANGLE, ">", NodeTypesOption.SYMBOL),
  RIGHTBRACE(Proparse.RIGHTBRACE, "]", NodeTypesOption.SYMBOL),
  RIGHTCURLY(Proparse.RIGHTCURLY, "}", NodeTypesOption.SYMBOL),
  RIGHTPAREN(Proparse.RIGHTPAREN, ")", NodeTypesOption.SYMBOL),
  SEMI(Proparse.SEMI, ";", NodeTypesOption.SYMBOL),
  SINGLEQUOTE(Proparse.SINGLEQUOTE, "'", NodeTypesOption.SYMBOL),
  SLASH(Proparse.SLASH, "/", NodeTypesOption.SYMBOL),
  STAR(Proparse.STAR, "*", NodeTypesOption.SYMBOL),
  TILDE(Proparse.TILDE, "~", NodeTypesOption.SYMBOL),
  UNARY_MINUS(Proparse.UNARY_MINUS, "-", NodeTypesOption.SYMBOL),
  UNARY_PLUS(Proparse.UNARY_PLUS, "+", NodeTypesOption.SYMBOL),
  UNKNOWNVALUE(Proparse.UNKNOWNVALUE, "?", NodeTypesOption.SYMBOL),
  PLUSEQUAL(Proparse.PLUSEQUAL, "+=", NodeTypesOption.SYMBOL),
  MINUSEQUAL(Proparse.MINUSEQUAL, "-=", NodeTypesOption.SYMBOL),
  STAREQUAL(Proparse.STAREQUAL, "*=", NodeTypesOption.SYMBOL),
  SLASHEQUAL(Proparse.SLASHEQUAL, "/=", NodeTypesOption.SYMBOL),
  ELVIS(Proparse.ELVIS, "?:", NodeTypesOption.SYMBOL),

  // Lexer (and later) elements
  ANNOTATION(Proparse.ANNOTATION),
  COMMENT(Proparse.COMMENT, NodeTypesOption.NONPRINTABLE),
  DOT_COMMENT(Proparse.DOT_COMMENT),
  FILENAME(Proparse.FILENAME),
  ID(Proparse.ID),
  LEXDATE(Proparse.LEXDATE, NodeTypesOption.NONPRINTABLE),
  NUMBER(Proparse.NUMBER, NodeTypesOption.SYMBOL),
  QSTRING(Proparse.QSTRING),
  UNQUOTEDSTRING(Proparse.UNQUOTEDSTRING),
  WS(Proparse.WS),
  // Next elements are not used anymore
  // COMMENTEND(Proparse.COMMENTEND, NodeTypesOption.NONPRINTABLE),
  // COMMENTSTART(Proparse.COMMENTSTART, NodeTypesOption.NONPRINTABLE),
  // CURLYAMP(Proparse.CURLYAMP),
  // CURLYNUMBER(Proparse.CURLYNUMBER),
  // CURLYSTAR(Proparse.CURLYSTAR),
  // DEFINETEXT(Proparse.DEFINETEXT),
  // DIGITS(Proparse.DIGITS),
  // DIGITSTART(Proparse.DIGITSTART),
  // DQSTRING(Proparse.DQSTRING),
  // ESCAPED_QUOTE(Proparse.ESCAPED_QUOTE),
  // FREECHAR(Proparse.FREECHAR),
  // ID_THREE(Proparse.ID_THREE),
  // ID_TWO(Proparse.ID_TWO),
  // IFCOND(Proparse.IFCOND),
  // IMPOSSIBLE_TOKEN(Proparse.IMPOSSIBLE_TOKEN),
  // IUNKNOWN(Proparse.IUNKNOWN, "iunknown", NodeTypesOption.KEYWORD),
  // LEXOTHER(Proparse.LEXOTHER, NodeTypesOption.NONPRINTABLE),
  // NEWLINE(Proparse.NEWLINE, NodeTypesOption.NONPRINTABLE),
  // PLUSMINUSSTART(Proparse.PLUSMINUSSTART),
  // SQSTRING(Proparse.SQSTRING),

  // Parser Structure Elements
  AGGREGATE_EXPRESSION(Proparse.Aggregate_expression, NodeTypesOption.STRUCTURE),
  AGGREGATE_PHRASE(Proparse.Aggregate_phrase, NodeTypesOption.STRUCTURE),
  ARRAY_REFERENCE(Proparse.Array_ref, NodeTypesOption.STRUCTURE),
  ASSIGN_DYNAMIC_NEW(Proparse.Assign_dynamic_new, NodeTypesOption.STRUCTURE),
  ASSIGN_FROM_BUFFER(Proparse.Assign_from_buffer, NodeTypesOption.STRUCTURE),
  ATTRIBUTE_REF(Proparse.Attribute_ref, NodeTypesOption.STRUCTURE),
  AUTOMATION_OBJECT(Proparse.Automationobject, NodeTypesOption.STRUCTURE),
  BLOCK_ITERATOR(Proparse.Block_iterator, NodeTypesOption.STRUCTURE),
  BLOCK_LABEL(Proparse.Block_label, NodeTypesOption.STRUCTURE),
  BUILTIN_FUNCTION(Proparse.Built_in_func, NodeTypesOption.STRUCTURE),
  CODE_BLOCK(Proparse.Code_block, NodeTypesOption.STRUCTURE),
  CONSTANT_REF(Proparse.Constant_ref, NodeTypesOption.STRUCTURE),
  EDITING_PHRASE(Proparse.Editing_phrase, NodeTypesOption.STRUCTURE),
  ENTERED_FUNC(Proparse.Entered_func, NodeTypesOption.STRUCTURE),
  EVENT_LIST(Proparse.Event_list, NodeTypesOption.STRUCTURE),
  EXPR_STATEMENT(Proparse.Expr_statement, NodeTypesOption.STRUCTURE),
  FIELD_REF(Proparse.Field_ref, NodeTypesOption.STRUCTURE),
  FORM_ITEM(Proparse.Form_item, NodeTypesOption.STRUCTURE),
  FORMAT_PHRASE(Proparse.Format_phrase, NodeTypesOption.STRUCTURE),
  IN_UI_REF(Proparse.In_UI_ref, NodeTypesOption.STRUCTURE),
  LOCAL_METHOD_REF(Proparse.Local_method_ref, NodeTypesOption.STRUCTURE),
  LEFT_PART(Proparse.Left_Part, NodeTypesOption.STRUCTURE),
  METHOD_REF(Proparse.Method_ref, NodeTypesOption.STRUCTURE),
  METHOD_PARAM_LIST(Proparse.Method_param_list, NodeTypesOption.STRUCTURE),
  NAMED_MEMBER(Proparse.Named_member, NodeTypesOption.STRUCTURE),
  NAMED_MEMBER_ARRAY(Proparse.Named_member_array, NodeTypesOption.STRUCTURE),
  NEW_TYPE_REF(Proparse.New_Type_expr, NodeTypesOption.STRUCTURE),
  NOT_CASESENS(Proparse.Not_casesens, NodeTypesOption.STRUCTURE),
  PARAMETER_ITEM(Proparse.Parameter, NodeTypesOption.STRUCTURE),
  PARAMETER_LIST(Proparse.Parameter_list, NodeTypesOption.STRUCTURE),
  PAREN_EXPR(Proparse.Paren_expr, NodeTypesOption.STRUCTURE),
  PROGRAM_ROOT(Proparse.Program_root, NodeTypesOption.STRUCTURE),
  PROGRAM_TAIL(Proparse.Program_tail, NodeTypesOption.STRUCTURE),
  PROPERTY_GETTER(Proparse.Property_getter, NodeTypesOption.STRUCTURE),
  PROPERTY_SETTER(Proparse.Property_setter, NodeTypesOption.STRUCTURE),
  RECORD_NAME(Proparse.Record_name, NodeTypesOption.STRUCTURE),
  RECORD_SEARCH(Proparse.Record_search, NodeTypesOption.STRUCTURE),
  SYSTEM_HANDLE_REF(Proparse.System_handle, NodeTypesOption.STRUCTURE),
  TYPE_NAME(Proparse.Type_name, NodeTypesOption.STRUCTURE),
  TYPELESS_TOKEN(Proparse.Typeless_token, NodeTypesOption.STRUCTURE),
  USER_FUNC(Proparse.User_func, NodeTypesOption.STRUCTURE),
  WIDGET_REF(Proparse.Widget_ref, NodeTypesOption.STRUCTURE),
  WITH_COLUMNS(Proparse.With_columns, NodeTypesOption.STRUCTURE),
  WITH_DOWN(Proparse.With_down, NodeTypesOption.STRUCTURE),

  // Hidden keywords
  AACBIT(Proparse.AACBIT, "_cbit", NodeTypesOption.KEYWORD),
  AACONTROL(Proparse.AACONTROL, "_control", NodeTypesOption.KEYWORD),
  AALIST(Proparse.AALIST, "_list", NodeTypesOption.KEYWORD),
  AAMEMORY(Proparse.AAMEMORY, "_memory", NodeTypesOption.KEYWORD),
  AAMSG(Proparse.AAMSG, "_msg", NodeTypesOption.KEYWORD),
  AAPCONTROL(Proparse.AAPCONTROL, "_pcontrol", NodeTypesOption.KEYWORD),
  AASERIAL(Proparse.AASERIAL, "_serial-num", 7, NodeTypesOption.KEYWORD),
  AATRACE(Proparse.AATRACE, "_trace", NodeTypesOption.KEYWORD),

  // Special keywords, usually with alternate syntax, weird abbreviation form
  // This list also includes a few unusual or almost unknown keywords
  // In short, anything which doesn't comply with -zgenkwlist
  UPPER(-1001, NodeTypesOption.PLACEHOLDER),
  COL(-1002, NodeTypesOption.PLACEHOLDER),
  COMPONENTHANDLE(-1003, NodeTypesOption.PLACEHOLDER),
  GATEWAYS(-1004, NodeTypesOption.PLACEHOLDER),
  EDGE(-1005, NodeTypesOption.PLACEHOLDER),
  EXCLUSIVE(-1006, NodeTypesOption.PLACEHOLDER),
  INIT(-1007, NodeTypesOption.PLACEHOLDER),
  LOWER(-1008, NodeTypesOption.PLACEHOLDER),
  MAX(-1009, NodeTypesOption.PLACEHOLDER),
  MOD(-1010, NodeTypesOption.PLACEHOLDER),
  NOATTR(-1011, NodeTypesOption.PLACEHOLDER),
  ROWHEIGHT(-1012, NodeTypesOption.PLACEHOLDER),
  TERM(-1013, NodeTypesOption.PLACEHOLDER),
  THRU(-1014, NodeTypesOption.PLACEHOLDER),
  TRANS(-1015, NodeTypesOption.PLACEHOLDER),
  WORKFILE(-1016, NodeTypesOption.PLACEHOLDER),

  ANALYZE(Proparse.ANALYZE, "analyze", 6, NodeTypesOption.KEYWORD),
  ASC(Proparse.ASC, "asc", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ASCENDING(Proparse.ASCENDING, "ascending", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  AUTOENDKEY(Proparse.AUTOENDKEY, "auto-end-key", "auto-endkey", NodeTypesOption.KEYWORD),
  BIGINT(Proparse.BIGINT, "bigint", NodeTypesOption.KEYWORD),
  BUTTON(Proparse.BUTTON, "button", NodeTypesOption.KEYWORD),
  BUTTONS(Proparse.BUTTONS, "buttons", NodeTypesOption.KEYWORD),
  CAPS(Proparse.CAPS, "caps", "upper", NodeTypesOption.KEYWORD),
  COLUMN(Proparse.COLUMN, "column", 3, NodeTypesOption.KEYWORD),
  COLUMNS(Proparse.COLUMNS, "columns", NodeTypesOption.KEYWORD),
  COMHANDLE(Proparse.COMHANDLE, "com-handle", "component-handle", NodeTypesOption.KEYWORD),
  COMPARE(Proparse.COMPARE, "compare", NodeTypesOption.KEYWORD),
  COMPARES(Proparse.COMPARES, "compares", NodeTypesOption.KEYWORD),
  CURRENCY(Proparse.CURRENCY, "currency", NodeTypesOption.KEYWORD),
  DATASERVERS(Proparse.DATASERVERS, "dataservers", 11, "gateways", 7, NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  DBIMS(Proparse.DBIMS, "dbims", NodeTypesOption.KEYWORD),
  EDGECHARS(Proparse.EDGECHARS, "edge-chars", 4, NodeTypesOption.KEYWORD),
  EDGEPIXELS(Proparse.EDGEPIXELS, "edge-pixels", 6, NodeTypesOption.KEYWORD),
  ERRORCODE(Proparse.ERRORCODE, "error-code", NodeTypesOption.KEYWORD),
  ENDKEY(Proparse.ENDKEY, "end-key", "endkey", NodeTypesOption.KEYWORD),
  ENABLEDFIELDS(Proparse.ENABLEDFIELDS, "enabled-fields", NodeTypesOption.KEYWORD),
  EXCLUSIVELOCK(Proparse.EXCLUSIVELOCK, "exclusive-lock", 9, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FIELD(Proparse.FIELD, "field", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FIELDS(Proparse.FIELDS, "fields", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FILE(Proparse.FILE, "file", 4, "file-name", "filename", NodeTypesOption.KEYWORD),
  FIXCHAR(Proparse.FIXCHAR, "fixchar", NodeTypesOption.KEYWORD),
  FONTBASEDLAYOUT(Proparse.FONTBASEDLAYOUT, "font-based-layout", NodeTypesOption.KEYWORD),
  GETBYTE(Proparse.GETBYTE, "get-byte", "getbyte", NodeTypesOption.KEYWORD),
  GETCODEPAGE(Proparse.GETCODEPAGE, "get-codepage", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  GETCODEPAGES(Proparse.GETCODEPAGES, "get-codepages", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  GETLICENSE(Proparse.GETLICENSE, "get-license", NodeTypesOption.KEYWORD),
  INITIAL(Proparse.INITIAL, "initial", 4, NodeTypesOption.KEYWORD),
  IUNKNOWN(Proparse.IUNKNOWN, "iunknown", NodeTypesOption.KEYWORD),
  KEYCODE(Proparse.KEYCODE, "key-code", "keycode", NodeTypesOption.KEYWORD),
  KEYFUNCTION(Proparse.KEYFUNCTION, "key-function", 8, "keyfunction", 7, NodeTypesOption.KEYWORD),
  KEYLABEL(Proparse.KEYLABEL, "key-label", "keylabel", NodeTypesOption.KEYWORD),
  LASTKEY(Proparse.LASTKEY, "last-key", "lastkey", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LC(Proparse.LC, "lc", "lower", NodeTypesOption.KEYWORD),
  MAXIMUM(Proparse.MAXIMUM, "max", "maximum", NodeTypesOption.KEYWORD),
  MENUBAR(Proparse.MENUBAR, "menu-bar", "menubar", NodeTypesOption.KEYWORD),
  MODULO(Proparse.MODULO, "modulo", 3, NodeTypesOption.KEYWORD),
  NOATTRLIST(Proparse.NOATTRLIST, "no-attr-list", 9, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOATTRSPACE(Proparse.NOATTRSPACE, "no-attr-space", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NORMAL(Proparse.NORMAL, "normal", NodeTypesOption.KEYWORD),
  ONLY(Proparse.ONLY, "only", NodeTypesOption.KEYWORD),
  ORDER(Proparse.ORDER, "order", NodeTypesOption.KEYWORD),
  PINNABLE(Proparse.PINNABLE, "pinnable", NodeTypesOption.KEYWORD),
  PROCTEXT(Proparse.PROCTEXT, "proc-text", NodeTypesOption.KEYWORD),
  PROCTEXTBUFFER(Proparse.PROCTEXTBUFFER, "proc-text-buffer", NodeTypesOption.KEYWORD),
  PUTBYTE(Proparse.PUTBYTE, "put-byte", "putbyte", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  REPOSITIONMODE(Proparse.REPOSITIONMODE, "reposition-mode", NodeTypesOption.KEYWORD),
  ROWHEIGHTCHARS(Proparse.ROWHEIGHTCHARS, "row-height", 10, "row-height-chars", 12, NodeTypesOption.KEYWORD),
  SAVECACHE(Proparse.SAVECACHE, "savecache", NodeTypesOption.KEYWORD),
  SENDSQLSTATEMENT(Proparse.SENDSQLSTATEMENT, "send-sql-statement", 8, NodeTypesOption.KEYWORD),
  SETCURRENTVALUE(Proparse.SETCURRENTVALUE, "set-current-value", NodeTypesOption.KEYWORD),
  STATUSBAR(Proparse.STATUSBAR, "status-bar", NodeTypesOption.KEYWORD),
  TERMINAL(Proparse.TERMINAL, "term", "terminal", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  THROUGH(Proparse.THROUGH, "through", "thru", NodeTypesOption.KEYWORD),
  TIMESTAMP(Proparse.TIMESTAMP, "timestamp", NodeTypesOption.KEYWORD),
  TOOLBAR(Proparse.TOOLBAR, "tool-bar", NodeTypesOption.KEYWORD),
  TRANSACTION(Proparse.TRANSACTION, "trans", 5, "transaction", 8, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  UNSIGNEDINTEGER(Proparse.UNSIGNEDINTEGER, "unsigned-integer", NodeTypesOption.KEYWORD),
  VAR(Proparse.VAR, "var", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  USERID(Proparse.USERID, "userid", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  USERID2(Proparse.USERID2, "user-id", NodeTypesOption.KEYWORD),
  WORKTABLE(Proparse.WORKTABLE, "work-table", 8, "workfile", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),

  // A
  ABORT(Proparse.ABORT, "abort", NodeTypesOption.KEYWORD),
  ABSOLUTE(Proparse.ABSOLUTE, "absolute", 3, NodeTypesOption.KEYWORD),
  ABSTRACT(Proparse.ABSTRACT, "abstract", NodeTypesOption.KEYWORD),
  ACCELERATOR(Proparse.ACCELERATOR, "accelerator", NodeTypesOption.KEYWORD),
  ACCEPTCHANGES(Proparse.ACCEPTCHANGES, "accept-changes", NodeTypesOption.KEYWORD),
  ACCEPTROWCHANGES(Proparse.ACCEPTROWCHANGES, "accept-row-changes", NodeTypesOption.KEYWORD),
  ACCUMULATE(Proparse.ACCUMULATE, "accumulate", 5, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ACROSS(Proparse.ACROSS, "across", NodeTypesOption.KEYWORD),
  ACTIVE(Proparse.ACTIVE, "active", NodeTypesOption.KEYWORD),
  ACTIVEFORM(Proparse.ACTIVEFORM, "active-form", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ACTIVEWINDOW(Proparse.ACTIVEWINDOW, "active-window", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ACTOR(Proparse.ACTOR, "actor", NodeTypesOption.KEYWORD),
  ADD(Proparse.ADD, "add", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ADDBUFFER(Proparse.ADDBUFFER, "add-buffer", NodeTypesOption.KEYWORD),
  ADDCALCCOLUMN(Proparse.ADDCALCCOLUMN, "add-calc-column", 12, NodeTypesOption.KEYWORD),
  ADDCOLUMNSFROM(Proparse.ADDCOLUMNSFROM, "add-columns-from", NodeTypesOption.KEYWORD),
  ADDEVENTSPROCEDURE(Proparse.ADDEVENTSPROCEDURE, "add-events-procedure", 15, NodeTypesOption.KEYWORD),
  ADDFIELDSFROM(Proparse.ADDFIELDSFROM, "add-fields-from", NodeTypesOption.KEYWORD),
  ADDFIRST(Proparse.ADDFIRST, "add-first", NodeTypesOption.KEYWORD),
  ADDHEADERENTRY(Proparse.ADDHEADERENTRY, "add-header-entry", NodeTypesOption.KEYWORD),
  ADDINDEXFIELD(Proparse.ADDINDEXFIELD, "add-index-field", NodeTypesOption.KEYWORD),
  ADDINTERVAL(Proparse.ADDINTERVAL, "add-interval", NodeTypesOption.KEYWORD),
  ADDLAST(Proparse.ADDLAST, "add-last", NodeTypesOption.KEYWORD),
  ADDLIKECOLUMN(Proparse.ADDLIKECOLUMN, "add-like-column", 12, NodeTypesOption.KEYWORD),
  ADDLIKEFIELD(Proparse.ADDLIKEFIELD, "add-like-field", NodeTypesOption.KEYWORD),
  ADDLIKEINDEX(Proparse.ADDLIKEINDEX, "add-like-index", NodeTypesOption.KEYWORD),
  ADDNEWFIELD(Proparse.ADDNEWFIELD, "add-new-field", NodeTypesOption.KEYWORD),
  ADDNEWINDEX(Proparse.ADDNEWINDEX, "add-new-index", NodeTypesOption.KEYWORD),
  ADDPARENTIDRELATION(Proparse.ADDPARENTIDRELATION, "add-parent-id-relation", NodeTypesOption.KEYWORD),
  ADDRELATION(Proparse.ADDRELATION, "add-relation", 7, NodeTypesOption.KEYWORD),
  ADDSCHEMALOCATION(Proparse.ADDSCHEMALOCATION, "add-schema-location", NodeTypesOption.KEYWORD),
  ADDSOURCEBUFFER(Proparse.ADDSOURCEBUFFER, "add-source-buffer", NodeTypesOption.KEYWORD),
  ADDSUPERPROCEDURE(Proparse.ADDSUPERPROCEDURE, "add-super-procedure", 14, NodeTypesOption.KEYWORD),
  ADMDATA(Proparse.ADMDATA, "adm-data", NodeTypesOption.KEYWORD),
  ADVISE(Proparse.ADVISE, "advise", NodeTypesOption.KEYWORD),
  AFTERBUFFER(Proparse.AFTERBUFFER, "after-buffer", NodeTypesOption.KEYWORD),
  AFTERROWID(Proparse.AFTERROWID, "after-rowid", NodeTypesOption.KEYWORD),
  AFTERTABLE(Proparse.AFTERTABLE, "after-table", NodeTypesOption.KEYWORD),
  AGGREGATE(Proparse.AGGREGATE, "aggregate", NodeTypesOption.KEYWORD),
  ALERTBOX(Proparse.ALERTBOX, "alert-box", NodeTypesOption.KEYWORD),
  ALIAS(Proparse.ALIAS, "alias", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ALL(Proparse.ALL, "all", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ALLOWCOLUMNSEARCHING(Proparse.ALLOWCOLUMNSEARCHING, "allow-column-searching", NodeTypesOption.KEYWORD),
  ALLOWPREVDESERIALIZATION(Proparse.ALLOWPREVDESERIALIZATION, "allow-prev-deserialization", NodeTypesOption.KEYWORD),
  ALLOWREPLICATION(Proparse.ALLOWREPLICATION, "allow-replication", NodeTypesOption.KEYWORD),
  ALTER(Proparse.ALTER, "alter", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ALTERNATEKEY(Proparse.ALTERNATEKEY, "alternate-key", NodeTypesOption.KEYWORD),
  ALWAYSONTOP(Proparse.ALWAYSONTOP, "always-on-top", NodeTypesOption.KEYWORD),
  AMBIGUOUS(Proparse.AMBIGUOUS, "ambiguous", 5, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  AND(Proparse.AND, "and", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ANSIONLY(Proparse.ANSIONLY, "ansi-only", NodeTypesOption.KEYWORD),
  ANY(Proparse.ANY, "any", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ANYKEY(Proparse.ANYKEY, "any-key", NodeTypesOption.KEYWORD),
  ANYPRINTABLE(Proparse.ANYPRINTABLE, "any-printable", NodeTypesOption.KEYWORD),
  ANYWHERE(Proparse.ANYWHERE, "anywhere", NodeTypesOption.KEYWORD),
  APPEND(Proparse.APPEND, "append", NodeTypesOption.KEYWORD),
  APPENDCHILD(Proparse.APPENDCHILD, "append-child", NodeTypesOption.KEYWORD),
  APPENDLINE(Proparse.APPENDLINE, "append-line", NodeTypesOption.KEYWORD),
  APPLALERTBOXES(Proparse.APPLALERTBOXES, "appl-alert-boxes", 10, NodeTypesOption.KEYWORD),
  APPLCONTEXTID(Proparse.APPLCONTEXTID, "appl-context-id", NodeTypesOption.KEYWORD),
  APPLICATION(Proparse.APPLICATION, "application", NodeTypesOption.KEYWORD),
  APPLY(Proparse.APPLY, "apply", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  APPLYCALLBACK(Proparse.APPLYCALLBACK, "apply-callback", NodeTypesOption.KEYWORD),
  APPSERVERINFO(Proparse.APPSERVERINFO, "appserver-info", NodeTypesOption.KEYWORD),
  APPSERVERPASSWORD(Proparse.APPSERVERPASSWORD, "appserver-password", NodeTypesOption.KEYWORD),
  APPSERVERUSERID(Proparse.APPSERVERUSERID, "appserver-userid", NodeTypesOption.KEYWORD),
  ARRAYMESSAGE(Proparse.ARRAYMESSAGE, "array-message", 7, NodeTypesOption.KEYWORD),
  AS(Proparse.AS, "as", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ASCURSOR(Proparse.ASCURSOR, "as-cursor", NodeTypesOption.KEYWORD),
  ASKOVERWRITE(Proparse.ASKOVERWRITE, "ask-overwrite", NodeTypesOption.KEYWORD),
  ASSEMBLY(Proparse.ASSEMBLY, "assembly", NodeTypesOption.KEYWORD),
  ASSIGN(Proparse.ASSIGN, "assign", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ASYNCHRONOUS(Proparse.ASYNCHRONOUS, "asynchronous", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ASYNCREQUESTCOUNT(Proparse.ASYNCREQUESTCOUNT, "async-request-count", NodeTypesOption.KEYWORD),
  ASYNCREQUESTHANDLE(Proparse.ASYNCREQUESTHANDLE, "async-request-handle", NodeTypesOption.KEYWORD),
  AT(Proparse.AT, "at", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ATTACH(Proparse.ATTACH, "attach", NodeTypesOption.KEYWORD),
  ATTACHDATASOURCE(Proparse.ATTACHDATASOURCE, "attach-data-source", NodeTypesOption.KEYWORD),
  ATTACHEDPAIRLIST(Proparse.ATTACHEDPAIRLIST, "attached-pairlist", NodeTypesOption.KEYWORD),
  ATTACHMENT(Proparse.ATTACHMENT, "attachment", NodeTypesOption.KEYWORD),
  ATTRIBUTENAMES(Proparse.ATTRIBUTENAMES, "attribute-names", NodeTypesOption.KEYWORD),
  ATTRIBUTETYPE(Proparse.ATTRIBUTETYPE, "attribute-type", NodeTypesOption.KEYWORD),
  ATTRSPACE(Proparse.ATTRSPACE, "attr-space", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  AUDITCONTROL(Proparse.AUDITCONTROL, "audit-control", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  AUDITENABLED(Proparse.AUDITENABLED, "audit-enabled", NodeTypesOption.KEYWORD),
  AUDITEVENTCONTEXT(Proparse.AUDITEVENTCONTEXT, "audit-event-context", NodeTypesOption.KEYWORD),
  AUDITPOLICY(Proparse.AUDITPOLICY, "audit-policy", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  AUTHENTICATIONFAILED(Proparse.AUTHENTICATIONFAILED, "authentication-failed", NodeTypesOption.KEYWORD),
  AUTHORIZATION(Proparse.AUTHORIZATION, "authorization", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  AUTOCOMPLETION(Proparse.AUTOCOMPLETION, "auto-completion", 9, NodeTypesOption.KEYWORD),
  AUTODELETE(Proparse.AUTODELETE, "auto-delete", NodeTypesOption.KEYWORD),
  AUTODELETEXML(Proparse.AUTODELETEXML, "auto-delete-xml", NodeTypesOption.KEYWORD),
  AUTOGO(Proparse.AUTOGO, "auto-go", NodeTypesOption.KEYWORD),
  AUTOINDENT(Proparse.AUTOINDENT, "auto-indent", 8, NodeTypesOption.KEYWORD),
  AUTOMATIC(Proparse.AUTOMATIC, "automatic", NodeTypesOption.KEYWORD),
  AUTORESIZE(Proparse.AUTORESIZE, "auto-resize", NodeTypesOption.KEYWORD),
  AUTORETURN(Proparse.AUTORETURN, "auto-return", 8, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  AUTOSYNCHRONIZE(Proparse.AUTOSYNCHRONIZE, "auto-synchronize", NodeTypesOption.KEYWORD),
  AUTOVALIDATE(Proparse.AUTOVALIDATE, "auto-validate", 8, NodeTypesOption.KEYWORD),
  AUTOZAP(Proparse.AUTOZAP, "auto-zap", 6, NodeTypesOption.KEYWORD),
  AVAILABLE(Proparse.AVAILABLE, "available", 5, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  AVAILABLEFORMATS(Proparse.AVAILABLEFORMATS, "available-formats", NodeTypesOption.KEYWORD),
  AVERAGE(Proparse.AVERAGE, "average", 3, NodeTypesOption.KEYWORD),
  AVG(Proparse.AVG, "avg", NodeTypesOption.KEYWORD),

  // B
  BACKGROUND(Proparse.BACKGROUND, "background", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BACKSPACE(Proparse.BACKSPACE, "backspace", NodeTypesOption.KEYWORD),
  BACKTAB(Proparse.BACKTAB, "back-tab", NodeTypesOption.KEYWORD),
  BACKWARDS(Proparse.BACKWARDS, "backwards", 8, NodeTypesOption.KEYWORD),
  BASE64(Proparse.BASE64, "base64", NodeTypesOption.KEYWORD),
  BASE64DECODE(Proparse.BASE64DECODE, "base64-decode", NodeTypesOption.KEYWORD),
  BASE64ENCODE(Proparse.BASE64ENCODE, "base64-encode", NodeTypesOption.KEYWORD),
  BASEADE(Proparse.BASEADE, "base-ade", NodeTypesOption.KEYWORD),
  BASEKEY(Proparse.BASEKEY, "base-key", NodeTypesOption.KEYWORD),
  BASICLOGGING(Proparse.BASICLOGGING, "basic-logging", NodeTypesOption.KEYWORD),
  BATCH(Proparse.BATCH, "batch", NodeTypesOption.KEYWORD),
  BATCHMODE(Proparse.BATCHMODE, "batch-mode", NodeTypesOption.KEYWORD),
  BATCHSIZE(Proparse.BATCHSIZE, "batch-size", NodeTypesOption.KEYWORD),
  BEFOREBUFFER(Proparse.BEFOREBUFFER, "before-buffer", NodeTypesOption.KEYWORD),
  BEFOREHIDE(Proparse.BEFOREHIDE, "before-hide", 8, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BEFOREROWID(Proparse.BEFOREROWID, "before-rowid", NodeTypesOption.KEYWORD),
  BEFORETABLE(Proparse.BEFORETABLE, "before-table", NodeTypesOption.KEYWORD),
  BEGINEVENTGROUP(Proparse.BEGINEVENTGROUP, "begin-event-group", NodeTypesOption.KEYWORD),
  BEGINS(Proparse.BEGINS, "begins", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BELL(Proparse.BELL, "bell", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BETWEEN(Proparse.BETWEEN, "between", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BGCOLOR(Proparse.BGCOLOR, "bgcolor", 3, NodeTypesOption.KEYWORD),
  BIGENDIAN(Proparse.BIGENDIAN, "big-endian", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BINARY(Proparse.BINARY, "binary", NodeTypesOption.KEYWORD),
  BIND(Proparse.BIND, "bind", NodeTypesOption.KEYWORD),
  BINDWHERE(Proparse.BINDWHERE, "bind-where", NodeTypesOption.KEYWORD),
  BLANK(Proparse.BLANK, "blank", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BLOB(Proparse.BLOB, "blob", NodeTypesOption.KEYWORD),
  BLOCK(Proparse.BLOCK, "block", NodeTypesOption.KEYWORD),
  BLOCKITERATIONDISPLAY(Proparse.BLOCKITERATIONDISPLAY, "block-iteration-display", NodeTypesOption.KEYWORD),
  BLOCKLEVEL(Proparse.BLOCKLEVEL, "block-level", 9, NodeTypesOption.KEYWORD),
  BORDERBOTTOM(Proparse.BORDERBOTTOM, "border-bottom", 8, NodeTypesOption.KEYWORD),
  BORDERBOTTOMCHARS(Proparse.BORDERBOTTOMCHARS, "border-bottom-chars", 15, NodeTypesOption.KEYWORD),
  BORDERBOTTOMPIXELS(Proparse.BORDERBOTTOMPIXELS, "border-bottom-pixels", 15, NodeTypesOption.KEYWORD),
  BORDERLEFT(Proparse.BORDERLEFT, "border-left", 8, NodeTypesOption.KEYWORD),
  BORDERLEFTCHARS(Proparse.BORDERLEFTCHARS, "border-left-chars", 13, NodeTypesOption.KEYWORD),
  BORDERLEFTPIXELS(Proparse.BORDERLEFTPIXELS, "border-left-pixels", 13, NodeTypesOption.KEYWORD),
  BORDERRIGHT(Proparse.BORDERRIGHT, "border-right", 8, NodeTypesOption.KEYWORD),
  BORDERRIGHTCHARS(Proparse.BORDERRIGHTCHARS, "border-right-chars", 14, NodeTypesOption.KEYWORD),
  BORDERRIGHTPIXELS(Proparse.BORDERRIGHTPIXELS, "border-right-pixels", 14, NodeTypesOption.KEYWORD),
  BORDERTOP(Proparse.BORDERTOP, "border-top", 8, NodeTypesOption.KEYWORD),
  BORDERTOPCHARS(Proparse.BORDERTOPCHARS, "border-top-chars", 12, NodeTypesOption.KEYWORD),
  BORDERTOPPIXELS(Proparse.BORDERTOPPIXELS, "border-top-pixels", 12, NodeTypesOption.KEYWORD),
  BOTH(Proparse.BOTH, "both", NodeTypesOption.KEYWORD),
  BOTTOM(Proparse.BOTTOM, "bottom", NodeTypesOption.KEYWORD),
  BOTTOMCOLUMN(Proparse.BOTTOMCOLUMN, "bottom-column", NodeTypesOption.KEYWORD),
  BOX(Proparse.BOX, "box", NodeTypesOption.KEYWORD),
  BOXSELECTABLE(Proparse.BOXSELECTABLE, "box-selectable", 10, NodeTypesOption.KEYWORD),
  BREAK(Proparse.BREAK, "break", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BREAKLINE(Proparse.BREAKLINE, "break-line", NodeTypesOption.KEYWORD),
  BROWSE(Proparse.BROWSE, "browse", NodeTypesOption.KEYWORD),
  BROWSECOLUMNDATATYPES(Proparse.BROWSECOLUMNDATATYPES, "browse-column-data-types", NodeTypesOption.KEYWORD),
  BROWSECOLUMNFORMATS(Proparse.BROWSECOLUMNFORMATS, "browse-column-formats", NodeTypesOption.KEYWORD),
  BROWSECOLUMNLABELS(Proparse.BROWSECOLUMNLABELS, "browse-column-labels", NodeTypesOption.KEYWORD),
  BROWSEHEADER(Proparse.BROWSEHEADER, "browse-header", NodeTypesOption.KEYWORD),
  BTOS(Proparse.BTOS, "btos", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BUFFER(Proparse.BUFFER, "buffer", NodeTypesOption.KEYWORD),
  BUFFERCHARS(Proparse.BUFFERCHARS, "buffer-chars", NodeTypesOption.KEYWORD),
  BUFFERCOMPARE(Proparse.BUFFERCOMPARE, "buffer-compare", 11, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BUFFERCOPY(Proparse.BUFFERCOPY, "buffer-copy", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BUFFERCREATE(Proparse.BUFFERCREATE, "buffer-create", NodeTypesOption.KEYWORD),
  BUFFERDELETE(Proparse.BUFFERDELETE, "buffer-delete", NodeTypesOption.KEYWORD),
  BUFFERFIELD(Proparse.BUFFERFIELD, "buffer-field", NodeTypesOption.KEYWORD),
  BUFFERGROUPID(Proparse.BUFFERGROUPID, "buffer-group-id", NodeTypesOption.KEYWORD),
  BUFFERGROUPNAME(Proparse.BUFFERGROUPNAME, "buffer-group-name", NodeTypesOption.KEYWORD),
  BUFFERHANDLE(Proparse.BUFFERHANDLE, "buffer-handle", NodeTypesOption.KEYWORD),
  BUFFERLINES(Proparse.BUFFERLINES, "buffer-lines", NodeTypesOption.KEYWORD),
  BUFFERNAME(Proparse.BUFFERNAME, "buffer-name", 8, NodeTypesOption.KEYWORD),
  BUFFERPARTITIONID(Proparse.BUFFERPARTITIONID, "buffer-partition-id", NodeTypesOption.KEYWORD),
  BUFFERRELEASE(Proparse.BUFFERRELEASE, "buffer-release", 13, NodeTypesOption.KEYWORD),
  BUFFERTENANTID(Proparse.BUFFERTENANTID, "buffer-tenant-id", NodeTypesOption.KEYWORD),
  BUFFERTENANTNAME(Proparse.BUFFERTENANTNAME, "buffer-tenant-name", NodeTypesOption.KEYWORD),
  BUFFERVALIDATE(Proparse.BUFFERVALIDATE, "buffer-validate", NodeTypesOption.KEYWORD),
  BUFFERVALUE(Proparse.BUFFERVALUE, "buffer-value", NodeTypesOption.KEYWORD),
  BY(Proparse.BY, "by", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BYPOINTER(Proparse.BYPOINTER, "by-pointer", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BYREFERENCE(Proparse.BYREFERENCE, "by-reference", NodeTypesOption.KEYWORD),
  BYTE(Proparse.BYTE, "byte", NodeTypesOption.KEYWORD),
  BYTESREAD(Proparse.BYTESREAD, "bytes-read", NodeTypesOption.KEYWORD),
  BYTESWRITTEN(Proparse.BYTESWRITTEN, "bytes-written", NodeTypesOption.KEYWORD),
  BYVALUE(Proparse.BYVALUE, "by-value", NodeTypesOption.KEYWORD),
  BYVARIANTPOINTER(Proparse.BYVARIANTPOINTER, "by-variant-pointer", 16, NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),

  // C
  CACHE(Proparse.CACHE, "cache", NodeTypesOption.KEYWORD),
  CACHESIZE(Proparse.CACHESIZE, "cache-size", NodeTypesOption.KEYWORD),
  CALL(Proparse.CALL, "call", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CALLNAME(Proparse.CALLNAME, "call-name", NodeTypesOption.KEYWORD),
  CALLTYPE(Proparse.CALLTYPE, "call-type", NodeTypesOption.KEYWORD),
  CANCELBREAK(Proparse.CANCELBREAK, "cancel-break", NodeTypesOption.KEYWORD),
  CANCELBUTTON(Proparse.CANCELBUTTON, "cancel-button", NodeTypesOption.KEYWORD),
  CANCELLED(Proparse.CANCELLED, "cancelled", NodeTypesOption.KEYWORD),
  CANCELPICK(Proparse.CANCELPICK, "cancel-pick", NodeTypesOption.KEYWORD),
  CANCELREQUESTS(Proparse.CANCELREQUESTS, "cancel-requests", NodeTypesOption.KEYWORD),
  CANCELREQUESTSAFTER(Proparse.CANCELREQUESTSAFTER, "cancel-requests-after", NodeTypesOption.KEYWORD),
  CANCREATE(Proparse.CANCREATE, "can-create", 8, NodeTypesOption.KEYWORD),
  CANDELETE(Proparse.CANDELETE, "can-delete", 8, NodeTypesOption.KEYWORD),
  CANDO(Proparse.CANDO, "can-do", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CANDODOMAINSUPPORT(Proparse.CANDODOMAINSUPPORT, "can-do-domain-support", NodeTypesOption.KEYWORD),
  CANFIND(Proparse.CANFIND, "can-find", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CANQUERY(Proparse.CANQUERY, "can-query", NodeTypesOption.KEYWORD),
  CANREAD(Proparse.CANREAD, "can-read", NodeTypesOption.KEYWORD),
  CANSET(Proparse.CANSET, "can-set", NodeTypesOption.KEYWORD),
  CANWRITE(Proparse.CANWRITE, "can-write", 8, NodeTypesOption.KEYWORD),
  CAREFULPAINT(Proparse.CAREFULPAINT, "careful-paint", NodeTypesOption.KEYWORD),
  CASE(Proparse.CASE, "case", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CASESENSITIVE(Proparse.CASESENSITIVE, "case-sensitive", 8, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CAST(Proparse.CAST, "cast", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CATCH(Proparse.CATCH, "catch", NodeTypesOption.KEYWORD),
  CDECL(Proparse.CDECL, "cdecl", NodeTypesOption.KEYWORD),
  CENTERED(Proparse.CENTERED, "centered", 6, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CHAINED(Proparse.CHAINED, "chained", NodeTypesOption.KEYWORD),
  CHARACTER(Proparse.CHARACTER, "character", 4, NodeTypesOption.KEYWORD),
  CHARACTERLENGTH(Proparse.CHARACTERLENGTH, "character_length", NodeTypesOption.KEYWORD),
  CHARSET(Proparse.CHARSET, "charset", NodeTypesOption.KEYWORD),
  CHECK(Proparse.CHECK, "check", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CHECKED(Proparse.CHECKED, "checked", NodeTypesOption.KEYWORD),
  CHECKMEMSTOMP(Proparse.CHECKMEMSTOMP, "check-mem-stomp", NodeTypesOption.KEYWORD),
  CHILDBUFFER(Proparse.CHILDBUFFER, "child-buffer", NodeTypesOption.KEYWORD),
  CHILDNUM(Proparse.CHILDNUM, "child-num", NodeTypesOption.KEYWORD),
  CHOICES(Proparse.CHOICES, "choices", NodeTypesOption.KEYWORD),
  CHOOSE(Proparse.CHOOSE, "choose", NodeTypesOption.KEYWORD),
  CHR(Proparse.CHR, "chr", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CLASS(Proparse.CLASS, "class", NodeTypesOption.KEYWORD),
  CLASSTYPE(Proparse.CLASSTYPE, "class-type", NodeTypesOption.KEYWORD),
  CLEAR(Proparse.CLEAR, "clear", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CLEARAPPLCONTEXT(Proparse.CLEARAPPLCONTEXT, "clear-appl-context", NodeTypesOption.KEYWORD),
  CLEARLOG(Proparse.CLEARLOG, "clear-log", NodeTypesOption.KEYWORD),
  CLEARSELECTION(Proparse.CLEARSELECTION, "clear-selection", 12, NodeTypesOption.KEYWORD),
  CLEARSORTARROWS(Proparse.CLEARSORTARROWS, "clear-sort-arrows", 16, NodeTypesOption.KEYWORD),
  CLIENTCONNECTIONID(Proparse.CLIENTCONNECTIONID, "client-connection-id", NodeTypesOption.KEYWORD),
  CLIENTPRINCIPAL(Proparse.CLIENTPRINCIPAL, "client-principal", NodeTypesOption.KEYWORD),
  CLIENTTTY(Proparse.CLIENTTTY, "client-tty", NodeTypesOption.KEYWORD),
  CLIENTTYPE(Proparse.CLIENTTYPE, "client-type", NodeTypesOption.KEYWORD),
  CLIENTWORKSTATION(Proparse.CLIENTWORKSTATION, "client-workstation", NodeTypesOption.KEYWORD),
  CLIPBOARD(Proparse.CLIPBOARD, "clipboard", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CLOB(Proparse.CLOB, "clob", NodeTypesOption.KEYWORD),
  CLONENODE(Proparse.CLONENODE, "clone-node", NodeTypesOption.KEYWORD),
  CLOSE(Proparse.CLOSE, "close", NodeTypesOption.KEYWORD),
  CLOSELOG(Proparse.CLOSELOG, "close-log", NodeTypesOption.KEYWORD),
  CODE(Proparse.CODE, "code", NodeTypesOption.KEYWORD),
  CODEBASELOCATOR(Proparse.CODEBASELOCATOR, "codebase-locator", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CODEPAGE(Proparse.CODEPAGE, "codepage", NodeTypesOption.KEYWORD),
  CODEPAGECONVERT(Proparse.CODEPAGECONVERT, "codepage-convert", NodeTypesOption.KEYWORD),
  COLLATE(Proparse.COLLATE, "collate", NodeTypesOption.KEYWORD),
  COLOF(Proparse.COLOF, "col-of", NodeTypesOption.KEYWORD),
  COLON(Proparse.COLON, "colon", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  COLONALIGNED(Proparse.COLONALIGNED, "colon-aligned", 11, NodeTypesOption.KEYWORD),
  COLOR(Proparse.COLOR, "color", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  COLORTABLE(Proparse.COLORTABLE, "color-table", NodeTypesOption.KEYWORD),
  COLUMNBGCOLOR(Proparse.COLUMNBGCOLOR, "column-bgcolor", 10, NodeTypesOption.KEYWORD),
  COLUMNCODEPAGE(Proparse.COLUMNCODEPAGE, "column-codepage", NodeTypesOption.KEYWORD),
  COLUMNDCOLOR(Proparse.COLUMNDCOLOR, "column-dcolor", NodeTypesOption.KEYWORD),
  COLUMNFGCOLOR(Proparse.COLUMNFGCOLOR, "column-fgcolor", 10, NodeTypesOption.KEYWORD),
  COLUMNFONT(Proparse.COLUMNFONT, "column-font", NodeTypesOption.KEYWORD),
  COLUMNLABEL(Proparse.COLUMNLABEL, "column-label", 10, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  COLUMNLABELBGCOLOR(Proparse.COLUMNLABELBGCOLOR, "column-label-bgcolor", 16, NodeTypesOption.KEYWORD),
  COLUMNLABELDCOLOR(Proparse.COLUMNLABELDCOLOR, "column-label-dcolor", NodeTypesOption.KEYWORD),
  COLUMNLABELFGCOLOR(Proparse.COLUMNLABELFGCOLOR, "column-label-fgcolor", 16, NodeTypesOption.KEYWORD),
  COLUMNLABELFONT(Proparse.COLUMNLABELFONT, "column-label-font", NodeTypesOption.KEYWORD),
  COLUMNLABELHEIGHTCHARS(Proparse.COLUMNLABELHEIGHTCHARS, "column-label-height-chars", 21, NodeTypesOption.KEYWORD),
  COLUMNLABELHEIGHTPIXELS(Proparse.COLUMNLABELHEIGHTPIXELS, "column-label-height-pixels", 21, NodeTypesOption.KEYWORD),
  COLUMNMOVABLE(Proparse.COLUMNMOVABLE, "column-movable", NodeTypesOption.KEYWORD),
  COLUMNOF(Proparse.COLUMNOF, "column-of", NodeTypesOption.KEYWORD),
  COLUMNPFCOLOR(Proparse.COLUMNPFCOLOR, "column-pfcolor", 10, NodeTypesOption.KEYWORD),
  COLUMNREADONLY(Proparse.COLUMNREADONLY, "column-read-only", NodeTypesOption.KEYWORD),
  COLUMNRESIZABLE(Proparse.COLUMNRESIZABLE, "column-resizable", NodeTypesOption.KEYWORD),
  COLUMNSCROLLING(Proparse.COLUMNSCROLLING, "column-scrolling", 9, NodeTypesOption.KEYWORD),
  COMBOBOX(Proparse.COMBOBOX, "combo-box", NodeTypesOption.KEYWORD),
  COMMAND(Proparse.COMMAND, "command", NodeTypesOption.KEYWORD),
  COMPILE(Proparse.COMPILE, "compile", NodeTypesOption.KEYWORD),
  COMPILER(Proparse.COMPILER, "compiler", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  COMPLETE(Proparse.COMPLETE, "complete", NodeTypesOption.KEYWORD),
  COMPONENTSELF(Proparse.COMPONENTSELF, "component-self", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  COMSELF(Proparse.COMSELF, "com-self", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CONFIGNAME(Proparse.CONFIGNAME, "config-name", NodeTypesOption.KEYWORD),
  CONNECT(Proparse.CONNECT, "connect", NodeTypesOption.KEYWORD),
  CONNECTED(Proparse.CONNECTED, "connected", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CONSTRAINED(Proparse.CONSTRAINED, "constrained", NodeTypesOption.KEYWORD),
  CONSTRUCTOR(Proparse.CONSTRUCTOR, "constructor", NodeTypesOption.KEYWORD),
  CONTAINEREVENT(Proparse.CONTAINEREVENT, "container-event", NodeTypesOption.KEYWORD),
  CONTAINS(Proparse.CONTAINS, "contains", NodeTypesOption.KEYWORD),
  CONTENTS(Proparse.CONTENTS, "contents", NodeTypesOption.KEYWORD),
  CONTEXT(Proparse.CONTEXT, "context", NodeTypesOption.KEYWORD),
  CONTEXTHELP(Proparse.CONTEXTHELP, "context-help", NodeTypesOption.KEYWORD),
  CONTEXTHELPFILE(Proparse.CONTEXTHELPFILE, "context-help-file", NodeTypesOption.KEYWORD),
  CONTEXTHELPID(Proparse.CONTEXTHELPID, "context-help-id", NodeTypesOption.KEYWORD),
  CONTEXTPOPUP(Proparse.CONTEXTPOPUP, "context-popup", 11, NodeTypesOption.KEYWORD),
  CONTROL(Proparse.CONTROL, "control", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CONTROLBOX(Proparse.CONTROLBOX, "control-box", NodeTypesOption.KEYWORD),
  CONTROLCONTAINER(Proparse.CONTROLCONTAINER, "control-container", 12, NodeTypesOption.KEYWORD),
  CONTROLFRAME(Proparse.CONTROLFRAME, "control-frame", 12, NodeTypesOption.KEYWORD),
  CONVERT(Proparse.CONVERT, "convert", NodeTypesOption.KEYWORD),
  CONVERT3DCOLORS(Proparse.CONVERT3DCOLORS, "convert-3d-colors", 10, NodeTypesOption.KEYWORD),
  CONVERTTOOFFSET(Proparse.CONVERTTOOFFSET, "convert-to-offset", 15, NodeTypesOption.KEYWORD),
  COPY(Proparse.COPY, "copy", NodeTypesOption.KEYWORD),
  COPYDATASET(Proparse.COPYDATASET, "copy-dataset", NodeTypesOption.KEYWORD),
  COPYLOB(Proparse.COPYLOB, "copy-lob", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  COPYSAXATTRIBUTES(Proparse.COPYSAXATTRIBUTES, "copy-sax-attributes", NodeTypesOption.KEYWORD),
  COPYTEMPTABLE(Proparse.COPYTEMPTABLE, "copy-temp-table", NodeTypesOption.KEYWORD),
  COUNT(Proparse.COUNT, "count", NodeTypesOption.KEYWORD),
  COUNTOF(Proparse.COUNTOF, "count-of", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  COVERAGE(Proparse.COVERAGE, "coverage", NodeTypesOption.KEYWORD),
  CPCASE(Proparse.CPCASE, "cpcase", NodeTypesOption.KEYWORD),
  CPCOLL(Proparse.CPCOLL, "cpcoll", NodeTypesOption.KEYWORD),
  CPINTERNAL(Proparse.CPINTERNAL, "cpinternal", 5, NodeTypesOption.KEYWORD),
  CPLOG(Proparse.CPLOG, "cplog", NodeTypesOption.KEYWORD),
  CPPRINT(Proparse.CPPRINT, "cpprint", NodeTypesOption.KEYWORD),
  CPRCODEIN(Proparse.CPRCODEIN, "cprcodein", NodeTypesOption.KEYWORD),
  CPRCODEOUT(Proparse.CPRCODEOUT, "cprcodeout", NodeTypesOption.KEYWORD),
  CPSTREAM(Proparse.CPSTREAM, "cpstream", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CPTERM(Proparse.CPTERM, "cpterm", NodeTypesOption.KEYWORD),
  CRCVALUE(Proparse.CRCVALUE, "crc-value", 7, NodeTypesOption.KEYWORD),
  CREATE(Proparse.CREATE, "create", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CREATELIKE(Proparse.CREATELIKE, "create-like", NodeTypesOption.KEYWORD),
  CREATELIKESEQUENTIAL(Proparse.CREATELIKESEQUENTIAL, "create-like-sequential", NodeTypesOption.KEYWORD),
  CREATENODE(Proparse.CREATENODE, "create-node", NodeTypesOption.KEYWORD),
  CREATENODENAMESPACE(Proparse.CREATENODENAMESPACE, "create-node-namespace", NodeTypesOption.KEYWORD),
  CREATEONADD(Proparse.CREATEONADD, "create-on-add", NodeTypesOption.KEYWORD),
  CREATERESULTLISTENTRY(Proparse.CREATERESULTLISTENTRY, "create-result-list-entry", NodeTypesOption.KEYWORD),
  CREATETESTFILE(Proparse.CREATETESTFILE, "create-test-file", NodeTypesOption.KEYWORD),
  CTOS(Proparse.CTOS, "ctos", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CURRENT(Proparse.CURRENT, "current", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CURRENTCHANGED(Proparse.CURRENTCHANGED, "current-changed", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CURRENTCOLUMN(Proparse.CURRENTCOLUMN, "current-column", NodeTypesOption.KEYWORD),
  CURRENTDATE(Proparse.CURRENTDATE, "current_date", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CURRENTENVIRONMENT(Proparse.CURRENTENVIRONMENT, "current-environment", 11, NodeTypesOption.KEYWORD),
  CURRENTITERATION(Proparse.CURRENTITERATION, "current-iteration", NodeTypesOption.KEYWORD),
  CURRENTLANGUAGE(Proparse.CURRENTLANGUAGE, "current-language", 12, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CURRENTQUERY(Proparse.CURRENTQUERY, "current-query", NodeTypesOption.KEYWORD),
  CURRENTREQUESTINFO(Proparse.CURRENTREQUESTINFO, "current-request-info", NodeTypesOption.KEYWORD),
  CURRENTRESPONSEINFO(Proparse.CURRENTRESPONSEINFO, "current-response-info", NodeTypesOption.KEYWORD),
  CURRENTRESULTROW(Proparse.CURRENTRESULTROW, "current-result-row", NodeTypesOption.KEYWORD),
  CURRENTROWMODIFIED(Proparse.CURRENTROWMODIFIED, "current-row-modified", NodeTypesOption.KEYWORD),
  CURRENTVALUE(Proparse.CURRENTVALUE, "current-value", NodeTypesOption.KEYWORD),
  CURRENTWINDOW(Proparse.CURRENTWINDOW, "current-window", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CURSOR(Proparse.CURSOR, "cursor", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CURSORCHAR(Proparse.CURSORCHAR, "cursor-char", NodeTypesOption.KEYWORD),
  CURSORDOWN(Proparse.CURSORDOWN, "cursor-down", NodeTypesOption.KEYWORD),
  CURSORLEFT(Proparse.CURSORLEFT, "cursor-left", NodeTypesOption.KEYWORD),
  CURSORLINE(Proparse.CURSORLINE, "cursor-line", NodeTypesOption.KEYWORD),
  CURSOROFFSET(Proparse.CURSOROFFSET, "cursor-offset", NodeTypesOption.KEYWORD),
  CURSORRIGHT(Proparse.CURSORRIGHT, "cursor-right", NodeTypesOption.KEYWORD),
  CURSORUP(Proparse.CURSORUP, "cursor-up", NodeTypesOption.KEYWORD),
  CUT(Proparse.CUT, "cut", NodeTypesOption.KEYWORD),

  // D
  DATABASE(Proparse.DATABASE, "database", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DATABIND(Proparse.DATABIND, "data-bind", 6, NodeTypesOption.KEYWORD),
  DATAENTRYRETURN(Proparse.DATAENTRYRETURN, "data-entry-return", 14, NodeTypesOption.KEYWORD),
  DATAREFRESHLINE(Proparse.DATAREFRESHLINE, "data-refresh-line", NodeTypesOption.KEYWORD),
  DATAREFRESHPAGE(Proparse.DATAREFRESHPAGE, "data-refresh-page", NodeTypesOption.KEYWORD),
  DATARELATION(Proparse.DATARELATION, "data-relation", 8, NodeTypesOption.KEYWORD),
  DATASET(Proparse.DATASET, "dataset", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DATASETHANDLE(Proparse.DATASETHANDLE, "dataset-handle", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DATASOURCE(Proparse.DATASOURCE, "data-source", NodeTypesOption.KEYWORD),
  DATASOURCECOMPLETEMAP(Proparse.DATASOURCECOMPLETEMAP, "data-source-complete-map", NodeTypesOption.KEYWORD),
  DATASOURCEMODIFIED(Proparse.DATASOURCEMODIFIED, "data-source-modified", NodeTypesOption.KEYWORD),
  DATASOURCEROWID(Proparse.DATASOURCEROWID, "data-source-rowid", NodeTypesOption.KEYWORD),
  DATATYPE(Proparse.DATATYPE, "data-type", 6, NodeTypesOption.KEYWORD),
  DATE(Proparse.DATE, "date", NodeTypesOption.KEYWORD),
  DATEFORMAT(Proparse.DATEFORMAT, "date-format", 6, NodeTypesOption.KEYWORD),
  DATETIME(Proparse.DATETIME, "datetime", NodeTypesOption.KEYWORD),
  DATETIMETZ(Proparse.DATETIMETZ, "datetime-tz", NodeTypesOption.KEYWORD),
  DAY(Proparse.DAY, "day", NodeTypesOption.KEYWORD),
  DBCODEPAGE(Proparse.DBCODEPAGE, "dbcodepage", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DBCOLLATION(Proparse.DBCOLLATION, "dbcollation", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DBCONTEXT(Proparse.DBCONTEXT, "db-context", NodeTypesOption.KEYWORD),
  DBLIST(Proparse.DBLIST, "db-list", NodeTypesOption.KEYWORD),
  DBNAME(Proparse.DBNAME, "dbname", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DBPARAM(Proparse.DBPARAM, "dbparam", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DBREFERENCES(Proparse.DBREFERENCES, "db-references", NodeTypesOption.KEYWORD),
  DBREMOTEHOST(Proparse.DBREMOTEHOST, "db-remote-host", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DBRESTRICTIONS(Proparse.DBRESTRICTIONS, "dbrestrictions", 6, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DBTASKID(Proparse.DBTASKID, "dbtaskid", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DBTYPE(Proparse.DBTYPE, "dbtype", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DBVERSION(Proparse.DBVERSION, "dbversion", 6, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DCOLOR(Proparse.DCOLOR, "dcolor", NodeTypesOption.KEYWORD),
  DDE(Proparse.DDE, "dde", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DDEERROR(Proparse.DDEERROR, "dde-error", NodeTypesOption.KEYWORD),
  DDEID(Proparse.DDEID, "dde-id", 5, NodeTypesOption.KEYWORD),
  DDEITEM(Proparse.DDEITEM, "dde-item", NodeTypesOption.KEYWORD),
  DDENAME(Proparse.DDENAME, "dde-name", NodeTypesOption.KEYWORD),
  DDENOTIFY(Proparse.DDENOTIFY, "dde-notify", NodeTypesOption.KEYWORD),
  DDETOPIC(Proparse.DDETOPIC, "dde-topic", NodeTypesOption.KEYWORD),
  DEBLANK(Proparse.DEBLANK, "deblank", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DEBUG(Proparse.DEBUG, "debug", 4, NodeTypesOption.KEYWORD),
  DEBUGALERT(Proparse.DEBUGALERT, "debug-alert", NodeTypesOption.KEYWORD),
  DEBUGGER(Proparse.DEBUGGER, "debugger", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DEBUGLIST(Proparse.DEBUGLIST, "debug-list", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DEBUGSETTENANT(Proparse.DEBUGSETTENANT, "debug-set-tenant", NodeTypesOption.KEYWORD),
  DECIMAL(Proparse.DECIMAL, "decimal", 3, NodeTypesOption.KEYWORD),
  DECIMALS(Proparse.DECIMALS, "decimals", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DECLARE(Proparse.DECLARE, "declare", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DECLARENAMESPACE(Proparse.DECLARENAMESPACE, "declare-namespace", NodeTypesOption.KEYWORD),
  DECRYPT(Proparse.DECRYPT, "decrypt", NodeTypesOption.KEYWORD),
  DEFAULT(Proparse.DEFAULT, "default", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DEFAULTACTION(Proparse.DEFAULTACTION, "default-action", NodeTypesOption.KEYWORD),
  DEFAULTBUFFERHANDLE(Proparse.DEFAULTBUFFERHANDLE, "default-buffer-handle", NodeTypesOption.KEYWORD),
  DEFAULTBUTTON(Proparse.DEFAULTBUTTON, "default-button", 11, NodeTypesOption.KEYWORD),
  DEFAULTCOMMIT(Proparse.DEFAULTCOMMIT, "default-commit", NodeTypesOption.KEYWORD),
  DEFAULTEXTENSION(Proparse.DEFAULTEXTENSION, "default-extension", 10, NodeTypesOption.KEYWORD),
  DEFAULTNOXLATE(Proparse.DEFAULTNOXLATE, "default-noxlate", 12, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DEFAULTPOPUP(Proparse.DEFAULTPOPUP, "default-pop-up", NodeTypesOption.KEYWORD),
  DEFAULTSTRING(Proparse.DEFAULTSTRING, "default-string", NodeTypesOption.KEYWORD),
  DEFAULTVALUE(Proparse.DEFAULTVALUE, "default-value", NodeTypesOption.KEYWORD),
  DEFAULTWINDOW(Proparse.DEFAULTWINDOW, "default-window", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DEFERLOBFETCH(Proparse.DEFERLOBFETCH, "defer-lob-fetch", NodeTypesOption.KEYWORD),
  DEFINE(Proparse.DEFINE, "define", 3, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DEFINED(Proparse.DEFINED, "defined", NodeTypesOption.KEYWORD),
  DEFINEUSEREVENTMANAGER(Proparse.DEFINEUSEREVENTMANAGER, "define-user-event-manager", NodeTypesOption.KEYWORD),
  DEL(Proparse.DEL, "del", NodeTypesOption.KEYWORD),
  DELEGATE(Proparse.DELEGATE, "delegate", NodeTypesOption.KEYWORD),
  DELETE(Proparse.DELETE, "delete", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DELETECHAR(Proparse.DELETECHAR, "delete-char", NodeTypesOption.KEYWORD),
  DELETECHARACTER(Proparse.DELETECHARACTER, "delete-character", NodeTypesOption.KEYWORD),
  DELETECOLUMN(Proparse.DELETECOLUMN, "delete-column", NodeTypesOption.KEYWORD),
  DELETECURRENTROW(Proparse.DELETECURRENTROW, "delete-current-row", NodeTypesOption.KEYWORD),
  DELETEENDLINE(Proparse.DELETEENDLINE, "delete-end-line", NodeTypesOption.KEYWORD),
  DELETEFIELD(Proparse.DELETEFIELD, "delete-field", NodeTypesOption.KEYWORD),
  DELETEHEADERENTRY(Proparse.DELETEHEADERENTRY, "delete-header-entry", NodeTypesOption.KEYWORD),
  DELETELINE(Proparse.DELETELINE, "delete-line", NodeTypesOption.KEYWORD),
  DELETENODE(Proparse.DELETENODE, "delete-node", NodeTypesOption.KEYWORD),
  DELETERESULTLISTENTRY(Proparse.DELETERESULTLISTENTRY, "delete-result-list-entry", NodeTypesOption.KEYWORD),
  DELETESELECTEDROW(Proparse.DELETESELECTEDROW, "delete-selected-row", NodeTypesOption.KEYWORD),
  DELETESELECTEDROWS(Proparse.DELETESELECTEDROWS, "delete-selected-rows", NodeTypesOption.KEYWORD),
  DELETEWORD(Proparse.DELETEWORD, "delete-word", NodeTypesOption.KEYWORD),
  DELIMITER(Proparse.DELIMITER, "delimiter", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DESCENDING(Proparse.DESCENDING, "descending", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DESCRIPTION(Proparse.DESCRIPTION, "description", 8, NodeTypesOption.KEYWORD),
  DESELECT(Proparse.DESELECT, "deselect", NodeTypesOption.KEYWORD),
  DESELECTEXTEND(Proparse.DESELECTEXTEND, "deselect-extend", NodeTypesOption.KEYWORD),
  DESELECTFOCUSEDROW(Proparse.DESELECTFOCUSEDROW, "deselect-focused-row", NodeTypesOption.KEYWORD),
  DESELECTION(Proparse.DESELECTION, "deselection", NodeTypesOption.KEYWORD),
  DESELECTIONEXTEND(Proparse.DESELECTIONEXTEND, "deselection-extend", NodeTypesOption.KEYWORD),
  DESELECTROWS(Proparse.DESELECTROWS, "deselect-rows", NodeTypesOption.KEYWORD),
  DESELECTSELECTEDROW(Proparse.DESELECTSELECTEDROW, "deselect-selected-row", NodeTypesOption.KEYWORD),
  DESTRUCTOR(Proparse.DESTRUCTOR, "destructor", NodeTypesOption.KEYWORD),
  DETACH(Proparse.DETACH, "detach", NodeTypesOption.KEYWORD),
  DETACHDATASOURCE(Proparse.DETACHDATASOURCE, "detach-data-source", NodeTypesOption.KEYWORD),
  DIALOGBOX(Proparse.DIALOGBOX, "dialog-box", NodeTypesOption.KEYWORD),
  DIALOGHELP(Proparse.DIALOGHELP, "dialog-help", NodeTypesOption.KEYWORD),
  DICTIONARY(Proparse.DICTIONARY, "dictionary", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DIR(Proparse.DIR, "dir", NodeTypesOption.KEYWORD),
  DIRECTORY(Proparse.DIRECTORY, "directory", NodeTypesOption.KEYWORD),
  DISABLE(Proparse.DISABLE, "disable", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DISABLEAUTOZAP(Proparse.DISABLEAUTOZAP, "disable-auto-zap", NodeTypesOption.KEYWORD),
  DISABLECONNECTIONS(Proparse.DISABLECONNECTIONS, "disable-connections", NodeTypesOption.KEYWORD),
  DISABLED(Proparse.DISABLED, "disabled", NodeTypesOption.KEYWORD),
  DISABLEDUMPTRIGGERS(Proparse.DISABLEDUMPTRIGGERS, "disable-dump-triggers", NodeTypesOption.KEYWORD),
  DISABLELOADTRIGGERS(Proparse.DISABLELOADTRIGGERS, "disable-load-triggers", NodeTypesOption.KEYWORD),
  DISCONNECT(Proparse.DISCONNECT, "disconnect", 6, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DISMISSMENU(Proparse.DISMISSMENU, "dismiss-menu", NodeTypesOption.KEYWORD),
  DISPLAY(Proparse.DISPLAY, "display", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DISPLAYMESSAGE(Proparse.DISPLAYMESSAGE, "display-message", NodeTypesOption.KEYWORD),
  DISPLAYTIMEZONE(Proparse.DISPLAYTIMEZONE, "display-timezone", NodeTypesOption.KEYWORD),
  DISPLAYTYPE(Proparse.DISPLAYTYPE, "display-type", 9, NodeTypesOption.KEYWORD),
  DISTINCT(Proparse.DISTINCT, "distinct", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DLLCALLTYPE(Proparse.DLLCALLTYPE, "dll-call-type", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DO(Proparse.DO, "do", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DOMAINDESCRIPTION(Proparse.DOMAINDESCRIPTION, "domain-description", NodeTypesOption.KEYWORD),
  DOMAINNAME(Proparse.DOMAINNAME, "domain-name", NodeTypesOption.KEYWORD),
  DOMAINTYPE(Proparse.DOMAINTYPE, "domain-type", NodeTypesOption.KEYWORD),
  DOS(Proparse.DOS, "dos", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DOSEND(Proparse.DOSEND, "dos-end", NodeTypesOption.KEYWORD),
  DOTNETCLRLOADED(Proparse.DOTNETCLRLOADED, "dotnet-clr-loaded", NodeTypesOption.KEYWORD),
  DOUBLE(Proparse.DOUBLE, "double", NodeTypesOption.KEYWORD),
  DOWN(Proparse.DOWN, "down", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DRAGENABLED(Proparse.DRAGENABLED, "drag-enabled", NodeTypesOption.KEYWORD),
  DROP(Proparse.DROP, "drop", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DROPDOWN(Proparse.DROPDOWN, "drop-down", NodeTypesOption.KEYWORD),
  DROPDOWNLIST(Proparse.DROPDOWNLIST, "drop-down-list", NodeTypesOption.KEYWORD),
  DROPFILENOTIFY(Proparse.DROPFILENOTIFY, "drop-file-notify", NodeTypesOption.KEYWORD),
  DROPTARGET(Proparse.DROPTARGET, "drop-target", NodeTypesOption.KEYWORD),
  DSLOGMANAGER(Proparse.DSLOGMANAGER, "dslog-manager", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DUMP(Proparse.DUMP, "dump", NodeTypesOption.KEYWORD),
  DUMPLOGGINGNOW(Proparse.DUMPLOGGINGNOW, "dump-logging-now", NodeTypesOption.KEYWORD),
  DYNAMIC(Proparse.DYNAMIC, "dynamic", NodeTypesOption.KEYWORD),
  DYNAMICCAST(Proparse.DYNAMICCAST, "dynamic-cast", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DYNAMICCURRENTVALUE(Proparse.DYNAMICCURRENTVALUE, "dynamic-current-value", NodeTypesOption.KEYWORD),
  DYNAMICENUM(Proparse.DYNAMICENUM, "dynamic-enum", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DYNAMICFUNCTION(Proparse.DYNAMICFUNCTION, "dynamic-function", 12, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DYNAMICINVOKE(Proparse.DYNAMICINVOKE, "dynamic-invoke", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DYNAMICNEW(Proparse.DYNAMICNEW, "dynamic-new", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DYNAMICNEXTVALUE(Proparse.DYNAMICNEXTVALUE, "dynamic-next-value", NodeTypesOption.KEYWORD),
  DYNAMICPROPERTY(Proparse.DYNAMICPROPERTY, "dynamic-property", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),

  // E
  EACH(Proparse.EACH, "each", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ECHO(Proparse.ECHO, "echo", NodeTypesOption.KEYWORD),
  EDITCANPASTE(Proparse.EDITCANPASTE, "edit-can-paste", NodeTypesOption.KEYWORD),
  EDITCANUNDO(Proparse.EDITCANUNDO, "edit-can-undo", NodeTypesOption.KEYWORD),
  EDITCLEAR(Proparse.EDITCLEAR, "edit-clear", NodeTypesOption.KEYWORD),
  EDITCOPY(Proparse.EDITCOPY, "edit-copy", NodeTypesOption.KEYWORD),
  EDITCUT(Proparse.EDITCUT, "edit-cut", NodeTypesOption.KEYWORD),
  EDITING(Proparse.EDITING, "editing", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  EDITOR(Proparse.EDITOR, "editor", NodeTypesOption.KEYWORD),
  EDITORBACKTAB(Proparse.EDITORBACKTAB, "editor-backtab", NodeTypesOption.KEYWORD),
  EDITORTAB(Proparse.EDITORTAB, "editor-tab", NodeTypesOption.KEYWORD),
  EDITPASTE(Proparse.EDITPASTE, "edit-paste", NodeTypesOption.KEYWORD),
  EDITUNDO(Proparse.EDITUNDO, "edit-undo", NodeTypesOption.KEYWORD),
  ELSE(Proparse.ELSE, "else", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  EMPTY(Proparse.EMPTY, "empty", NodeTypesOption.KEYWORD),
  EMPTYDATASET(Proparse.EMPTYDATASET, "empty-dataset", NodeTypesOption.KEYWORD),
  EMPTYSELECTION(Proparse.EMPTYSELECTION, "empty-selection", NodeTypesOption.KEYWORD),
  EMPTYTEMPTABLE(Proparse.EMPTYTEMPTABLE, "empty-temp-table", NodeTypesOption.KEYWORD),
  ENABLE(Proparse.ENABLE, "enable", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ENABLECONNECTIONS(Proparse.ENABLECONNECTIONS, "enable-connections", NodeTypesOption.KEYWORD),
  ENABLED(Proparse.ENABLED, "enabled", NodeTypesOption.KEYWORD),
  ENCODE(Proparse.ENCODE, "encode", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ENCODEDOMAINACCESSCODE(Proparse.ENCODEDOMAINACCESSCODE, "encode-domain-access-code", NodeTypesOption.KEYWORD),
  ENCODING(Proparse.ENCODING, "encoding", NodeTypesOption.KEYWORD),
  ENCRYPT(Proparse.ENCRYPT, "encrypt", NodeTypesOption.KEYWORD),
  ENCRYPTAUDITMACKEY(Proparse.ENCRYPTAUDITMACKEY, "encrypt-audit-mac-key", NodeTypesOption.KEYWORD),
  ENCRYPTIONSALT(Proparse.ENCRYPTIONSALT, "encryption-salt", NodeTypesOption.KEYWORD),
  END(Proparse.END, "end", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ENDBOXSELECTION(Proparse.ENDBOXSELECTION, "end-box-selection", NodeTypesOption.KEYWORD),
  ENDDOCUMENT(Proparse.ENDDOCUMENT, "end-document", NodeTypesOption.KEYWORD),
  ENDELEMENT(Proparse.ENDELEMENT, "end-element", NodeTypesOption.KEYWORD),
  ENDERROR(Proparse.ENDERROR, "end-error", NodeTypesOption.KEYWORD),
  ENDEVENTGROUP(Proparse.ENDEVENTGROUP, "end-event-group", NodeTypesOption.KEYWORD),
  ENDFILEDROP(Proparse.ENDFILEDROP, "end-file-drop", NodeTypesOption.KEYWORD),
  ENDMOVE(Proparse.ENDMOVE, "end-move", NodeTypesOption.KEYWORD),
  ENDRESIZE(Proparse.ENDRESIZE, "end-resize", NodeTypesOption.KEYWORD),
  ENDROWRESIZE(Proparse.ENDROWRESIZE, "end-row-resize", NodeTypesOption.KEYWORD),
  ENDSEARCH(Proparse.ENDSEARCH, "end-search", NodeTypesOption.KEYWORD),
  ENDUSERPROMPT(Proparse.ENDUSERPROMPT, "end-user-prompt", NodeTypesOption.KEYWORD),
  ENTERED(Proparse.ENTERED, "entered", NodeTypesOption.KEYWORD),
  ENTERMENUBAR(Proparse.ENTERMENUBAR, "enter-menubar", NodeTypesOption.KEYWORD),
  ENTITYEXPANSIONLIMIT(Proparse.ENTITYEXPANSIONLIMIT, "entity-expansion-limit", NodeTypesOption.KEYWORD),
  ENTRY(Proparse.ENTRY, "entry", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ENTRYTYPESLIST(Proparse.ENTRYTYPESLIST, "entry-types-list", NodeTypesOption.KEYWORD),
  ENUM(Proparse.ENUM, "enum", NodeTypesOption.KEYWORD),
  EQ(Proparse.EQ, "eq", NodeTypesOption.KEYWORD),
  ERROR(Proparse.ERROR, "error", NodeTypesOption.KEYWORD),
  ERRORCOLUMN(Proparse.ERRORCOLUMN, "error-column", 9, NodeTypesOption.KEYWORD),
  ERROROBJECT(Proparse.ERROROBJECT, "error-object", NodeTypesOption.KEYWORD),
  ERROROBJECTDETAIL(Proparse.ERROROBJECTDETAIL, "error-object-detail", NodeTypesOption.KEYWORD),
  ERRORROW(Proparse.ERRORROW, "error-row", NodeTypesOption.KEYWORD),
  ERRORSTACKTRACE(Proparse.ERRORSTACKTRACE, "error-stack-trace", NodeTypesOption.KEYWORD),
  ERRORSTATUS(Proparse.ERRORSTATUS, "error-status", 10, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ERRORSTRING(Proparse.ERRORSTRING, "error-string", NodeTypesOption.KEYWORD),
  ESCAPE(Proparse.ESCAPE, "escape", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ETIME(Proparse.ETIME, "etime", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  EVENT(Proparse.EVENT, "event", NodeTypesOption.KEYWORD),
  EVENTGROUPID(Proparse.EVENTGROUPID, "event-group-id", NodeTypesOption.KEYWORD),
  EVENTHANDLER(Proparse.EVENTHANDLER, "event-handler", NodeTypesOption.KEYWORD),
  EVENTHANDLERCONTEXT(Proparse.EVENTHANDLERCONTEXT, "event-handler-context", NodeTypesOption.KEYWORD),
  EVENTHANDLEROBJECT(Proparse.EVENTHANDLEROBJECT, "event-handler-object", NodeTypesOption.KEYWORD),
  EVENTPROCEDURE(Proparse.EVENTPROCEDURE, "event-procedure", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  EVENTPROCEDURECONTEXT(Proparse.EVENTPROCEDURECONTEXT, "event-procedure-context", NodeTypesOption.KEYWORD),
  EVENTS(Proparse.EVENTS, "events", NodeTypesOption.KEYWORD),
  EVENTTYPE(Proparse.EVENTTYPE, "event-type", 7, NodeTypesOption.KEYWORD),
  EXCEPT(Proparse.EXCEPT, "except", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  EXCLUSIVEID(Proparse.EXCLUSIVEID, "exclusive-id", NodeTypesOption.KEYWORD),
  EXCLUSIVEWEBUSER(Proparse.EXCLUSIVEWEBUSER, "exclusive-web-user", 13, NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  EXECUTE(Proparse.EXECUTE, "execute", NodeTypesOption.KEYWORD),
  EXECUTIONLOG(Proparse.EXECUTIONLOG, "execution-log", NodeTypesOption.KEYWORD),
  EXISTS(Proparse.EXISTS, "exists", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  EXIT(Proparse.EXIT, "exit", NodeTypesOption.KEYWORD),
  EXITCODE(Proparse.EXITCODE, "exit-code", NodeTypesOption.KEYWORD),
  EXP(Proparse.EXP, "exp", NodeTypesOption.KEYWORD),
  EXPAND(Proparse.EXPAND, "expand", NodeTypesOption.KEYWORD),
  EXPANDABLE(Proparse.EXPANDABLE, "expandable", NodeTypesOption.KEYWORD),
  EXPIRE(Proparse.EXPIRE, "expire", NodeTypesOption.KEYWORD),
  EXPLICIT(Proparse.EXPLICIT, "explicit", NodeTypesOption.KEYWORD),
  EXPORT(Proparse.EXPORT, "export", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  EXPORTPRINCIPAL(Proparse.EXPORTPRINCIPAL, "export-principal", NodeTypesOption.KEYWORD),
  EXTENDED(Proparse.EXTENDED, "extended", NodeTypesOption.KEYWORD),
  EXTENT(Proparse.EXTENT, "extent", NodeTypesOption.KEYWORD),
  EXTERNAL(Proparse.EXTERNAL, "external", NodeTypesOption.KEYWORD),
  EXTRACT(Proparse.EXTRACT, "extract", NodeTypesOption.KEYWORD),

  // F
  FALSE(Proparse.FALSE, "false", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FALSELEAKS(Proparse.FALSELEAKS, "false-leaks", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FETCH(Proparse.FETCH, "fetch", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FETCHSELECTEDROW(Proparse.FETCHSELECTEDROW, "fetch-selected-row", NodeTypesOption.KEYWORD),
  FGCOLOR(Proparse.FGCOLOR, "fgcolor", 3, NodeTypesOption.KEYWORD),
  FILEACCESSDATE(Proparse.FILEACCESSDATE, "file-access-date", 13, NodeTypesOption.KEYWORD),
  FILEACCESSTIME(Proparse.FILEACCESSTIME, "file-access-time", 13, NodeTypesOption.KEYWORD),
  FILECREATEDATE(Proparse.FILECREATEDATE, "file-create-date", 13, NodeTypesOption.KEYWORD),
  FILECREATETIME(Proparse.FILECREATETIME, "file-create-time", 13, NodeTypesOption.KEYWORD),
  FILEINFORMATION(Proparse.FILEINFORMATION, "file-information", 9, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FILEMODDATE(Proparse.FILEMODDATE, "file-mod-date", 10, NodeTypesOption.KEYWORD),
  FILEMODTIME(Proparse.FILEMODTIME, "file-mod-time", 10, NodeTypesOption.KEYWORD),
  FILEOFFSET(Proparse.FILEOFFSET, "file-offset", 8, NodeTypesOption.KEYWORD),
  FILESIZE(Proparse.FILESIZE, "file-size", NodeTypesOption.KEYWORD),
  FILETYPE(Proparse.FILETYPE, "file-type", NodeTypesOption.KEYWORD),
  FILL(Proparse.FILL, "fill", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FILLED(Proparse.FILLED, "filled", NodeTypesOption.KEYWORD),
  FILLIN(Proparse.FILLIN, "fill-in", NodeTypesOption.KEYWORD),
  FILLMODE(Proparse.FILLMODE, "fill-mode", NodeTypesOption.KEYWORD),
  FILLWHERESTRING(Proparse.FILLWHERESTRING, "fill-where-string", NodeTypesOption.KEYWORD),
  FILTERS(Proparse.FILTERS, "filters", NodeTypesOption.KEYWORD),
  FINAL(Proparse.FINAL, "final", NodeTypesOption.KEYWORD),
  FINALLY(Proparse.FINALLY, "finally", NodeTypesOption.KEYWORD),
  FIND(Proparse.FIND, "find", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FINDBYROWID(Proparse.FINDBYROWID, "find-by-rowid", NodeTypesOption.KEYWORD),
  FINDCASESENSITIVE(Proparse.FINDCASESENSITIVE, "find-case-sensitive", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  FINDCURRENT(Proparse.FINDCURRENT, "find-current", NodeTypesOption.KEYWORD),
  FINDER(Proparse.FINDER, "finder", NodeTypesOption.KEYWORD),
  FINDFIRST(Proparse.FINDFIRST, "find-first", NodeTypesOption.KEYWORD),
  FINDGLOBAL(Proparse.FINDGLOBAL, "find-global", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FINDLAST(Proparse.FINDLAST, "find-last", NodeTypesOption.KEYWORD),
  FINDNEXT(Proparse.FINDNEXT, "find-next", NodeTypesOption.KEYWORD),
  FINDNEXTOCCURRENCE(Proparse.FINDNEXTOCCURRENCE, "find-next-occurrence", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  FINDPREVIOUS(Proparse.FINDPREVIOUS, "find-previous", NodeTypesOption.KEYWORD),
  FINDPREVOCCURRENCE(Proparse.FINDPREVOCCURRENCE, "find-prev-occurrence", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  FINDSELECT(Proparse.FINDSELECT, "find-select", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FINDUNIQUE(Proparse.FINDUNIQUE, "find-unique", NodeTypesOption.KEYWORD),
  FINDWRAPAROUND(Proparse.FINDWRAPAROUND, "find-wrap-around", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FIREHOSECURSOR(Proparse.FIREHOSECURSOR, "firehose-cursor", NodeTypesOption.KEYWORD),
  FIRST(Proparse.FIRST, "first", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FIRSTASYNCREQUEST(Proparse.FIRSTASYNCREQUEST, "first-async-request", 11, NodeTypesOption.KEYWORD),
  FIRSTBUFFER(Proparse.FIRSTBUFFER, "first-buffer", NodeTypesOption.KEYWORD),
  FIRSTCHILD(Proparse.FIRSTCHILD, "first-child", NodeTypesOption.KEYWORD),
  FIRSTCOLUMN(Proparse.FIRSTCOLUMN, "first-column", NodeTypesOption.KEYWORD),
  FIRSTDATASET(Proparse.FIRSTDATASET, "first-dataset", NodeTypesOption.KEYWORD),
  FIRSTDATASOURCE(Proparse.FIRSTDATASOURCE, "first-data-source", NodeTypesOption.KEYWORD),
  FIRSTFORM(Proparse.FIRSTFORM, "first-form", NodeTypesOption.KEYWORD),
  FIRSTOBJECT(Proparse.FIRSTOBJECT, "first-object", NodeTypesOption.KEYWORD),
  FIRSTOF(Proparse.FIRSTOF, "first-of", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FIRSTPROCEDURE(Proparse.FIRSTPROCEDURE, "first-procedure", 10, NodeTypesOption.KEYWORD),
  FIRSTQUERY(Proparse.FIRSTQUERY, "first-query", NodeTypesOption.KEYWORD),
  FIRSTSERVER(Proparse.FIRSTSERVER, "first-server", 10, NodeTypesOption.KEYWORD),
  FIRSTSERVERSOCKET(Proparse.FIRSTSERVERSOCKET, "first-server-socket", NodeTypesOption.KEYWORD),
  FIRSTSOCKET(Proparse.FIRSTSOCKET, "first-socket", NodeTypesOption.KEYWORD),
  FIRSTTABITEM(Proparse.FIRSTTABITEM, "first-tab-item", 11, NodeTypesOption.KEYWORD),
  FITLASTCOLUMN(Proparse.FITLASTCOLUMN, "fit-last-column", NodeTypesOption.KEYWORD),
  FIXCODEPAGE(Proparse.FIXCODEPAGE, "fix-codepage", NodeTypesOption.KEYWORD),
  FIXEDONLY(Proparse.FIXEDONLY, "fixed-only", NodeTypesOption.KEYWORD),
  FLAGS(Proparse.FLAGS, "flags", NodeTypesOption.KEYWORD),
  FLATBUTTON(Proparse.FLATBUTTON, "flat-button", NodeTypesOption.KEYWORD),
  FLOAT(Proparse.FLOAT, "float", NodeTypesOption.KEYWORD),
  FOCUS(Proparse.FOCUS, "focus", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FOCUSEDROW(Proparse.FOCUSEDROW, "focused-row", NodeTypesOption.KEYWORD),
  FOCUSEDROWSELECTED(Proparse.FOCUSEDROWSELECTED, "focused-row-selected", NodeTypesOption.KEYWORD),
  FOCUSIN(Proparse.FOCUSIN, "focus-in", NodeTypesOption.KEYWORD),
  FONT(Proparse.FONT, "font", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FONTTABLE(Proparse.FONTTABLE, "font-table", NodeTypesOption.KEYWORD),
  FOR(Proparse.FOR, "for", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FORCEFILE(Proparse.FORCEFILE, "force-file", NodeTypesOption.KEYWORD),
  FOREGROUND(Proparse.FOREGROUND, "foreground", 4, NodeTypesOption.KEYWORD),
  FOREIGNKEYHIDDEN(Proparse.FOREIGNKEYHIDDEN, "foreign-key-hidden", NodeTypesOption.KEYWORD),
  FORMAT(Proparse.FORMAT, "format", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FORMATTED(Proparse.FORMATTED, "formatted", 8, NodeTypesOption.KEYWORD),
  FORMINPUT(Proparse.FORMINPUT, "form-input", NodeTypesOption.KEYWORD),
  FORMLONGINPUT(Proparse.FORMLONGINPUT, "form-long-input", NodeTypesOption.KEYWORD),
  FORWARDONLY(Proparse.FORWARDONLY, "forward-only", NodeTypesOption.KEYWORD),
  FORWARDS(Proparse.FORWARDS, "forwards", 7, NodeTypesOption.KEYWORD),
  FRAGMENT(Proparse.FRAGMENT, "fragment", 7, NodeTypesOption.KEYWORD),
  FRAME(Proparse.FRAME, "frame", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FRAMECOL(Proparse.FRAMECOL, "frame-col", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FRAMEDB(Proparse.FRAMEDB, "frame-db", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FRAMEDOWN(Proparse.FRAMEDOWN, "frame-down", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FRAMEFIELD(Proparse.FRAMEFIELD, "frame-field", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FRAMEFILE(Proparse.FRAMEFILE, "frame-file", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FRAMEINDEX(Proparse.FRAMEINDEX, "frame-index", 10, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FRAMELINE(Proparse.FRAMELINE, "frame-line", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FRAMENAME(Proparse.FRAMENAME, "frame-name", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FRAMEROW(Proparse.FRAMEROW, "frame-row", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FRAMESPACING(Proparse.FRAMESPACING, "frame-spacing", 9, NodeTypesOption.KEYWORD),
  FRAMEVALUE(Proparse.FRAMEVALUE, "frame-value", 9, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FRAMEX(Proparse.FRAMEX, "frame-x", NodeTypesOption.KEYWORD),
  FRAMEY(Proparse.FRAMEY, "frame-y", NodeTypesOption.KEYWORD),
  FREQUENCY(Proparse.FREQUENCY, "frequency", NodeTypesOption.KEYWORD),
  FROM(Proparse.FROM, "from", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FROMCHARS(Proparse.FROMCHARS, "from-chars", 6, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FROMCURRENT(Proparse.FROMCURRENT, "from-current", 8, NodeTypesOption.KEYWORD),
  FROMNOREORDER(Proparse.FROMNOREORDER, "fromnoreorder", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FROMPIXELS(Proparse.FROMPIXELS, "from-pixels", 6, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FULLHEIGHT(Proparse.FULLHEIGHT, "full-height", NodeTypesOption.KEYWORD),
  FULLHEIGHTCHARS(Proparse.FULLHEIGHTCHARS, "full-height-chars", 13, NodeTypesOption.KEYWORD),
  FULLHEIGHTPIXELS(Proparse.FULLHEIGHTPIXELS, "full-height-pixels", 13, NodeTypesOption.KEYWORD),
  FULLPATHNAME(Proparse.FULLPATHNAME, "full-pathname", 10, NodeTypesOption.KEYWORD),
  FULLWIDTHCHARS(Proparse.FULLWIDTHCHARS, "full-width-chars", 10, NodeTypesOption.KEYWORD),
  FULLWIDTHPIXELS(Proparse.FULLWIDTHPIXELS, "full-width-pixels", 12, NodeTypesOption.KEYWORD),
  FUNCTION(Proparse.FUNCTION, "function", NodeTypesOption.KEYWORD),
  FUNCTIONCALLTYPE(Proparse.FUNCTIONCALLTYPE, "function-call-type", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),

  // G
  GE(Proparse.GE, "ge", NodeTypesOption.KEYWORD),
  GENERATEMD5(Proparse.GENERATEMD5, "generate-md5", NodeTypesOption.KEYWORD),
  GENERATEPBEKEY(Proparse.GENERATEPBEKEY, "generate-pbe-key", NodeTypesOption.KEYWORD),
  GENERATEPBESALT(Proparse.GENERATEPBESALT, "generate-pbe-salt", NodeTypesOption.KEYWORD),
  GENERATERANDOMKEY(Proparse.GENERATERANDOMKEY, "generate-random-key", NodeTypesOption.KEYWORD),
  GENERATEUUID(Proparse.GENERATEUUID, "generate-uuid", NodeTypesOption.KEYWORD),
  GET(Proparse.GET, "get", NodeTypesOption.KEYWORD),
  GETATTRCALLTYPE(Proparse.GETATTRCALLTYPE, "get-attr-call-type", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  GETATTRIBUTE(Proparse.GETATTRIBUTE, "get-attribute", NodeTypesOption.KEYWORD),
  GETATTRIBUTENODE(Proparse.GETATTRIBUTENODE, "get-attribute-node", NodeTypesOption.KEYWORD),
  GETBINARYDATA(Proparse.GETBINARYDATA, "get-binary-data", NodeTypesOption.KEYWORD),
  GETBITS(Proparse.GETBITS, "get-bits", NodeTypesOption.KEYWORD),
  GETBLUEVALUE(Proparse.GETBLUEVALUE, "get-blue-value", 8, NodeTypesOption.KEYWORD),
  GETBROWSECOLUMN(Proparse.GETBROWSECOLUMN, "get-browse-column", 14, NodeTypesOption.KEYWORD),
  GETBUFFERHANDLE(Proparse.GETBUFFERHANDLE, "get-buffer-handle", NodeTypesOption.KEYWORD),
  GETBYTEORDER(Proparse.GETBYTEORDER, "get-byte-order", NodeTypesOption.KEYWORD),
  GETBYTES(Proparse.GETBYTES, "get-bytes", NodeTypesOption.KEYWORD),
  GETBYTESAVAILABLE(Proparse.GETBYTESAVAILABLE, "get-bytes-available", NodeTypesOption.KEYWORD),
  GETCALLBACKPROCCONTEXT(Proparse.GETCALLBACKPROCCONTEXT, "get-callback-proc-context", NodeTypesOption.KEYWORD),
  GETCALLBACKPROCNAME(Proparse.GETCALLBACKPROCNAME, "get-callback-proc-name", NodeTypesOption.KEYWORD),
  GETCGILIST(Proparse.GETCGILIST, "get-cgi-list", NodeTypesOption.KEYWORD),
  GETCGILONGVALUE(Proparse.GETCGILONGVALUE, "get-cgi-long-value", NodeTypesOption.KEYWORD),
  GETCGIVALUE(Proparse.GETCGIVALUE, "get-cgi-value", NodeTypesOption.KEYWORD),
  GETCHANGES(Proparse.GETCHANGES, "get-changes", NodeTypesOption.KEYWORD),
  GETCHILD(Proparse.GETCHILD, "get-child", NodeTypesOption.KEYWORD),
  GETCHILDRELATION(Proparse.GETCHILDRELATION, "get-child-relation", 13, NodeTypesOption.KEYWORD),
  GETCLASS(Proparse.GETCLASS, "get-class", NodeTypesOption.KEYWORD),
  GETCLIENT(Proparse.GETCLIENT, "get-client", NodeTypesOption.KEYWORD),
  GETCOLLATIONS(Proparse.GETCOLLATIONS, "get-collations", 8, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  GETCOLUMN(Proparse.GETCOLUMN, "get-column", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  GETCONFIGVALUE(Proparse.GETCONFIGVALUE, "get-config-value", NodeTypesOption.KEYWORD),
  GETCURRENT(Proparse.GETCURRENT, "get-current", 8, NodeTypesOption.KEYWORD),
  GETDATASETBUFFER(Proparse.GETDATASETBUFFER, "get-dataset-buffer", NodeTypesOption.KEYWORD),
  GETDBCLIENT(Proparse.GETDBCLIENT, "get-db-client", NodeTypesOption.KEYWORD),
  GETDIR(Proparse.GETDIR, "get-dir", NodeTypesOption.KEYWORD),
  GETDOCUMENTELEMENT(Proparse.GETDOCUMENTELEMENT, "get-document-element", NodeTypesOption.KEYWORD),
  GETDOUBLE(Proparse.GETDOUBLE, "get-double", NodeTypesOption.KEYWORD),
  GETDROPPEDFILE(Proparse.GETDROPPEDFILE, "get-dropped-file", NodeTypesOption.KEYWORD),
  GETDYNAMIC(Proparse.GETDYNAMIC, "get-dynamic", NodeTypesOption.KEYWORD),
  GETEFFECTIVETENANTID(Proparse.GETEFFECTIVETENANTID, "get-effective-tenant-id", NodeTypesOption.KEYWORD),
  GETEFFECTIVETENANTNAME(Proparse.GETEFFECTIVETENANTNAME, "get-effective-tenant-name", NodeTypesOption.KEYWORD),
  GETERRORCOLUMN(Proparse.GETERRORCOLUMN, "get-error-column", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  GETERRORROW(Proparse.GETERRORROW, "get-error-row", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  GETFILE(Proparse.GETFILE, "get-file", NodeTypesOption.KEYWORD),
  GETFILENAME(Proparse.GETFILENAME, "get-file-name", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  GETFILEOFFSET(Proparse.GETFILEOFFSET, "get-file-offset", 14, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  GETFIRST(Proparse.GETFIRST, "get-first", 8, NodeTypesOption.KEYWORD),
  GETFLOAT(Proparse.GETFLOAT, "get-float", NodeTypesOption.KEYWORD),
  GETGREENVALUE(Proparse.GETGREENVALUE, "get-green-value", 9, NodeTypesOption.KEYWORD),
  GETHEADERENTRY(Proparse.GETHEADERENTRY, "get-header-entry", 15, NodeTypesOption.KEYWORD),
  GETINDEXBYNAMESPACENAME(Proparse.GETINDEXBYNAMESPACENAME, "get-index-by-namespace-name", NodeTypesOption.KEYWORD),
  GETINDEXBYQNAME(Proparse.GETINDEXBYQNAME, "get-index-by-qname", NodeTypesOption.KEYWORD),
  GETINT64(Proparse.GETINT64, "get-int64", NodeTypesOption.KEYWORD),
  GETITERATION(Proparse.GETITERATION, "get-iteration", NodeTypesOption.KEYWORD),
  GETKEYVALUE(Proparse.GETKEYVALUE, "get-key-value", 11, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  GETLAST(Proparse.GETLAST, "get-last", NodeTypesOption.KEYWORD),
  GETLOCALNAMEBYINDEX(Proparse.GETLOCALNAMEBYINDEX, "get-localname-by-index", NodeTypesOption.KEYWORD),
  GETLONG(Proparse.GETLONG, "get-long", NodeTypesOption.KEYWORD),
  GETMESSAGE(Proparse.GETMESSAGE, "get-message", NodeTypesOption.KEYWORD),
  GETMESSAGETYPE(Proparse.GETMESSAGETYPE, "get-message-type", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  GETNEXT(Proparse.GETNEXT, "get-next", NodeTypesOption.KEYWORD),
  GETNODE(Proparse.GETNODE, "get-node", NodeTypesOption.KEYWORD),
  GETNUMBER(Proparse.GETNUMBER, "get-number", NodeTypesOption.KEYWORD),
  GETPARENT(Proparse.GETPARENT, "get-parent", NodeTypesOption.KEYWORD),
  GETPOINTERVALUE(Proparse.GETPOINTERVALUE, "get-pointer-value", NodeTypesOption.KEYWORD),
  GETPREV(Proparse.GETPREV, "get-prev", NodeTypesOption.KEYWORD),
  GETPRINTERS(Proparse.GETPRINTERS, "get-printers", NodeTypesOption.KEYWORD),
  GETPROPERTY(Proparse.GETPROPERTY, "get-property", NodeTypesOption.KEYWORD),
  GETQNAMEBYINDEX(Proparse.GETQNAMEBYINDEX, "get-qname-by-index", NodeTypesOption.KEYWORD),
  GETREDVALUE(Proparse.GETREDVALUE, "get-red-value", 7, NodeTypesOption.KEYWORD),
  GETRELATION(Proparse.GETRELATION, "get-relation", 7, NodeTypesOption.KEYWORD),
  GETREPOSITIONEDROW(Proparse.GETREPOSITIONEDROW, "get-repositioned-row", NodeTypesOption.KEYWORD),
  GETRGBVALUE(Proparse.GETRGBVALUE, "get-rgb-value", 7, NodeTypesOption.KEYWORD),
  GETROW(Proparse.GETROW, "get-row", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  GETSAFEUSER(Proparse.GETSAFEUSER, "get-safe-user", NodeTypesOption.KEYWORD),
  GETSELECTEDWIDGET(Proparse.GETSELECTEDWIDGET, "get-selected-widget", 12, NodeTypesOption.KEYWORD),
  GETSERIALIZED(Proparse.GETSERIALIZED, "get-serialized", NodeTypesOption.KEYWORD),
  GETSHORT(Proparse.GETSHORT, "get-short", NodeTypesOption.KEYWORD),
  GETSIGNATURE(Proparse.GETSIGNATURE, "get-signature", NodeTypesOption.KEYWORD),
  GETSIZE(Proparse.GETSIZE, "get-size", NodeTypesOption.KEYWORD),
  GETSOCKETOPTION(Proparse.GETSOCKETOPTION, "get-socket-option", NodeTypesOption.KEYWORD),
  GETSOURCEBUFFER(Proparse.GETSOURCEBUFFER, "get-source-buffer", NodeTypesOption.KEYWORD),
  GETSTRING(Proparse.GETSTRING, "get-string", NodeTypesOption.KEYWORD),
  GETTABITEM(Proparse.GETTABITEM, "get-tab-item", NodeTypesOption.KEYWORD),
  GETTEXTHEIGHT(Proparse.GETTEXTHEIGHT, "get-text-height", NodeTypesOption.KEYWORD),
  GETTEXTHEIGHTCHARS(Proparse.GETTEXTHEIGHTCHARS, "get-text-height-chars", 17, NodeTypesOption.KEYWORD),
  GETTEXTHEIGHTPIXELS(Proparse.GETTEXTHEIGHTPIXELS, "get-text-height-pixels", 17, NodeTypesOption.KEYWORD),
  GETTEXTWIDTH(Proparse.GETTEXTWIDTH, "get-text-width", NodeTypesOption.KEYWORD),
  GETTEXTWIDTHCHARS(Proparse.GETTEXTWIDTHCHARS, "get-text-width-chars", 16, NodeTypesOption.KEYWORD),
  GETTEXTWIDTHPIXELS(Proparse.GETTEXTWIDTHPIXELS, "get-text-width-pixels", 16, NodeTypesOption.KEYWORD),
  GETTOPBUFFER(Proparse.GETTOPBUFFER, "get-top-buffer", NodeTypesOption.KEYWORD),
  GETTYPEBYINDEX(Proparse.GETTYPEBYINDEX, "get-type-by-index", NodeTypesOption.KEYWORD),
  GETTYPEBYNAMESPACENAME(Proparse.GETTYPEBYNAMESPACENAME, "get-type-by-namespace-name", NodeTypesOption.KEYWORD),
  GETTYPEBYQNAME(Proparse.GETTYPEBYQNAME, "get-type-by-qname", NodeTypesOption.KEYWORD),
  GETUNSIGNEDLONG(Proparse.GETUNSIGNEDLONG, "get-unsigned-long", NodeTypesOption.KEYWORD),
  GETUNSIGNEDSHORT(Proparse.GETUNSIGNEDSHORT, "get-unsigned-short", NodeTypesOption.KEYWORD),
  GETURIBYINDEX(Proparse.GETURIBYINDEX, "get-uri-by-index", NodeTypesOption.KEYWORD),
  GETVALUEBYINDEX(Proparse.GETVALUEBYINDEX, "get-value-by-index", NodeTypesOption.KEYWORD),
  GETVALUEBYNAMESPACENAME(Proparse.GETVALUEBYNAMESPACENAME, "get-value-by-namespace-name", NodeTypesOption.KEYWORD),
  GETVALUEBYQNAME(Proparse.GETVALUEBYQNAME, "get-value-by-qname", NodeTypesOption.KEYWORD),
  GETWAITSTATE(Proparse.GETWAITSTATE, "get-wait-state", 8, NodeTypesOption.KEYWORD),
  GLOBAL(Proparse.GLOBAL, "global", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  GO(Proparse.GO, "go", NodeTypesOption.KEYWORD),
  GOON(Proparse.GOON, "go-on", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  GOPENDING(Proparse.GOPENDING, "go-pending", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  GOTO(Proparse.GOTO, "goto", NodeTypesOption.KEYWORD),
  GRANT(Proparse.GRANT, "grant", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  GRANTARCHIVE(Proparse.GRANTARCHIVE, "grant-archive", NodeTypesOption.KEYWORD),
  GRAPHICEDGE(Proparse.GRAPHICEDGE, "graphic-edge", 9, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  GRAYED(Proparse.GRAYED, "grayed", NodeTypesOption.KEYWORD),
  GRIDFACTORHORIZONTAL(Proparse.GRIDFACTORHORIZONTAL, "grid-factor-horizontal", 13, NodeTypesOption.KEYWORD),
  GRIDFACTORVERTICAL(Proparse.GRIDFACTORVERTICAL, "grid-factor-vertical", 13, NodeTypesOption.KEYWORD),
  GRIDSET(Proparse.GRIDSET, "grid-set", NodeTypesOption.KEYWORD),
  GRIDSNAP(Proparse.GRIDSNAP, "grid-snap", NodeTypesOption.KEYWORD),
  GRIDUNITHEIGHT(Proparse.GRIDUNITHEIGHT, "grid-unit-height", NodeTypesOption.KEYWORD),
  GRIDUNITHEIGHTCHARS(Proparse.GRIDUNITHEIGHTCHARS, "grid-unit-height-chars", 18, NodeTypesOption.KEYWORD),
  GRIDUNITHEIGHTPIXELS(Proparse.GRIDUNITHEIGHTPIXELS, "grid-unit-height-pixels", 18, NodeTypesOption.KEYWORD),
  GRIDUNITWIDTH(Proparse.GRIDUNITWIDTH, "grid-unit-width", NodeTypesOption.KEYWORD),
  GRIDUNITWIDTHCHARS(Proparse.GRIDUNITWIDTHCHARS, "grid-unit-width-chars", 17, NodeTypesOption.KEYWORD),
  GRIDUNITWIDTHPIXELS(Proparse.GRIDUNITWIDTHPIXELS, "grid-unit-width-pixels", 17, NodeTypesOption.KEYWORD),
  GRIDVISIBLE(Proparse.GRIDVISIBLE, "grid-visible", NodeTypesOption.KEYWORD),
  GROUP(Proparse.GROUP, "group", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  GROUPBOX(Proparse.GROUPBOX, "group-box", NodeTypesOption.KEYWORD),
  GTHAN(Proparse.GTHAN, "gt", NodeTypesOption.KEYWORD),
  GUID(Proparse.GUID, "guid", NodeTypesOption.KEYWORD),

  // H
  HANDLE(Proparse.HANDLE, "handle", NodeTypesOption.KEYWORD),
  HANDLER(Proparse.HANDLER, "handler", NodeTypesOption.KEYWORD),
  HASLOBS(Proparse.HASLOBS, "has-lobs", NodeTypesOption.KEYWORD),
  HASRECORDS(Proparse.HASRECORDS, "has-records", NodeTypesOption.KEYWORD),
  HAVING(Proparse.HAVING, "having", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  HEADER(Proparse.HEADER, "header", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  HEIGHT(Proparse.HEIGHT, "height", NodeTypesOption.KEYWORD),
  HEIGHTCHARS(Proparse.HEIGHTCHARS, "height-chars", 8, NodeTypesOption.KEYWORD),
  HEIGHTPIXELS(Proparse.HEIGHTPIXELS, "height-pixels", 8, NodeTypesOption.KEYWORD),
  HELP(Proparse.HELP, "help", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  HELPCONTEXT(Proparse.HELPCONTEXT, "help-context", 8, NodeTypesOption.KEYWORD),
  HELPFILENAME(Proparse.HELPFILENAME, "helpfile-name", 10, NodeTypesOption.KEYWORD),
  HELPTOPIC(Proparse.HELPTOPIC, "help-topic", NodeTypesOption.KEYWORD),
  HEXDECODE(Proparse.HEXDECODE, "hex-decode", NodeTypesOption.KEYWORD),
  HEXENCODE(Proparse.HEXENCODE, "hex-encode", NodeTypesOption.KEYWORD),
  HIDDEN(Proparse.HIDDEN, "hidden", NodeTypesOption.KEYWORD),
  HIDE(Proparse.HIDE, "hide", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  HINT(Proparse.HINT, "hint", NodeTypesOption.KEYWORD),
  HOME(Proparse.HOME, "home", NodeTypesOption.KEYWORD),
  HORIZEND(Proparse.HORIZEND, "horiz-end", NodeTypesOption.KEYWORD),
  HORIZHOME(Proparse.HORIZHOME, "horiz-home", NodeTypesOption.KEYWORD),
  HORIZONTAL(Proparse.HORIZONTAL, "horizontal", 4, NodeTypesOption.KEYWORD),
  HORIZSCROLLDRAG(Proparse.HORIZSCROLLDRAG, "horiz-scroll-drag", NodeTypesOption.KEYWORD),
  HOSTBYTEORDER(Proparse.HOSTBYTEORDER, "host-byte-order", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  HTMLCHARSET(Proparse.HTMLCHARSET, "html-charset", NodeTypesOption.KEYWORD),
  HTMLENDOFLINE(Proparse.HTMLENDOFLINE, "html-end-of-line", NodeTypesOption.KEYWORD),
  HTMLENDOFPAGE(Proparse.HTMLENDOFPAGE, "html-end-of-page", NodeTypesOption.KEYWORD),
  HTMLFRAMEBEGIN(Proparse.HTMLFRAMEBEGIN, "html-frame-begin", NodeTypesOption.KEYWORD),
  HTMLFRAMEEND(Proparse.HTMLFRAMEEND, "html-frame-end", NodeTypesOption.KEYWORD),
  HTMLHEADERBEGIN(Proparse.HTMLHEADERBEGIN, "html-header-begin", NodeTypesOption.KEYWORD),
  HTMLHEADEREND(Proparse.HTMLHEADEREND, "html-header-end", NodeTypesOption.KEYWORD),
  HTMLTITLEBEGIN(Proparse.HTMLTITLEBEGIN, "html-title-begin", NodeTypesOption.KEYWORD),
  HTMLTITLEEND(Proparse.HTMLTITLEEND, "html-title-end", NodeTypesOption.KEYWORD),
  HWND(Proparse.HWND, "hwnd", NodeTypesOption.KEYWORD),

  // I
  ICFPARAMETER(Proparse.ICFPARAMETER, "icfparameter", 8, NodeTypesOption.KEYWORD),
  ICON(Proparse.ICON, "icon", NodeTypesOption.KEYWORD),
  IF(Proparse.IF, "if", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  IGNORECURRENTMODIFIED(Proparse.IGNORECURRENTMODIFIED, "ignore-current-modified", 18, NodeTypesOption.KEYWORD),
  IMAGE(Proparse.IMAGE, "image", NodeTypesOption.KEYWORD),
  IMAGEDOWN(Proparse.IMAGEDOWN, "image-down", NodeTypesOption.KEYWORD),
  IMAGEINSENSITIVE(Proparse.IMAGEINSENSITIVE, "image-insensitive", NodeTypesOption.KEYWORD),
  IMAGESIZE(Proparse.IMAGESIZE, "image-size", NodeTypesOption.KEYWORD),
  IMAGESIZECHARS(Proparse.IMAGESIZECHARS, "image-size-chars", 12, NodeTypesOption.KEYWORD),
  IMAGESIZEPIXELS(Proparse.IMAGESIZEPIXELS, "image-size-pixels", 12, NodeTypesOption.KEYWORD),
  IMAGEUP(Proparse.IMAGEUP, "image-up", NodeTypesOption.KEYWORD),
  IMMEDIATEDISPLAY(Proparse.IMMEDIATEDISPLAY, "immediate-display", NodeTypesOption.KEYWORD),
  IMPLEMENTS(Proparse.IMPLEMENTS, "implements", NodeTypesOption.KEYWORD),
  IMPORT(Proparse.IMPORT, "import", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  IMPORTNODE(Proparse.IMPORTNODE, "import-node", NodeTypesOption.KEYWORD),
  IMPORTPRINCIPAL(Proparse.IMPORTPRINCIPAL, "import-principal", NodeTypesOption.KEYWORD),
  IN(Proparse.IN, "in", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  INCREMENTEXCLUSIVEID(Proparse.INCREMENTEXCLUSIVEID, "increment-exclusive-id", NodeTypesOption.KEYWORD),
  INDEX(Proparse.INDEX, "index", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  INDEXEDREPOSITION(Proparse.INDEXEDREPOSITION, "indexed-reposition", NodeTypesOption.KEYWORD),
  INDEXHINT(Proparse.INDEXHINT, "index-hint", NodeTypesOption.KEYWORD),
  INDEXINFORMATION(Proparse.INDEXINFORMATION, "index-information", 10, NodeTypesOption.KEYWORD),
  INDICATOR(Proparse.INDICATOR, "indicator", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  INFORMATION(Proparse.INFORMATION, "information", 4, NodeTypesOption.KEYWORD),
  INHANDLE(Proparse.INHANDLE, "in-handle", NodeTypesOption.KEYWORD),
  INHERITBGCOLOR(Proparse.INHERITBGCOLOR, "inherit-bgcolor", 11, NodeTypesOption.KEYWORD),
  INHERITCOLORMODE(Proparse.INHERITCOLORMODE, "inherit-color-mode", NodeTypesOption.KEYWORD),
  INHERITFGCOLOR(Proparse.INHERITFGCOLOR, "inherit-fgcolor", 11, NodeTypesOption.KEYWORD),
  INHERITS(Proparse.INHERITS, "inherits", NodeTypesOption.KEYWORD),
  INITIALDIR(Proparse.INITIALDIR, "initial-dir", NodeTypesOption.KEYWORD),
  INITIALFILTER(Proparse.INITIALFILTER, "initial-filter", NodeTypesOption.KEYWORD),
  INITIALIZE(Proparse.INITIALIZE, "initialize", NodeTypesOption.KEYWORD),
  INITIALIZEDOCUMENTTYPE(Proparse.INITIALIZEDOCUMENTTYPE, "initialize-document-type", NodeTypesOption.KEYWORD),
  INITIATE(Proparse.INITIATE, "initiate", NodeTypesOption.KEYWORD),
  INNER(Proparse.INNER, "inner", NodeTypesOption.KEYWORD),
  INNERCHARS(Proparse.INNERCHARS, "inner-chars", NodeTypesOption.KEYWORD),
  INNERLINES(Proparse.INNERLINES, "inner-lines", NodeTypesOption.KEYWORD),
  INPUT(Proparse.INPUT, "input", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  INPUTOUTPUT(Proparse.INPUTOUTPUT, "input-output", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  INPUTVALUE(Proparse.INPUTVALUE, "input-value", NodeTypesOption.KEYWORD),
  INSERT(Proparse.INSERT, "insert", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  INSERTATTRIBUTE(Proparse.INSERTATTRIBUTE, "insert-attribute", NodeTypesOption.KEYWORD),
  INSERTBACKTAB(Proparse.INSERTBACKTAB, "insert-backtab", 8, NodeTypesOption.KEYWORD),
  INSERTBEFORE(Proparse.INSERTBEFORE, "insert-before", NodeTypesOption.KEYWORD),
  INSERTCOLUMN(Proparse.INSERTCOLUMN, "insert-column", NodeTypesOption.KEYWORD),
  INSERTFIELD(Proparse.INSERTFIELD, "insert-field", NodeTypesOption.KEYWORD),
  INSERTFIELDDATA(Proparse.INSERTFIELDDATA, "insert-field-data", NodeTypesOption.KEYWORD),
  INSERTFIELDLABEL(Proparse.INSERTFIELDLABEL, "insert-field-label", NodeTypesOption.KEYWORD),
  INSERTFILE(Proparse.INSERTFILE, "insert-file", NodeTypesOption.KEYWORD),
  INSERTMODE(Proparse.INSERTMODE, "insert-mode", NodeTypesOption.KEYWORD),
  INSERTROW(Proparse.INSERTROW, "insert-row", NodeTypesOption.KEYWORD),
  INSERTSTRING(Proparse.INSERTSTRING, "insert-string", NodeTypesOption.KEYWORD),
  INSERTTAB(Proparse.INSERTTAB, "insert-tab", 8, NodeTypesOption.KEYWORD),
  INSTANTIATINGPROCEDURE(Proparse.INSTANTIATINGPROCEDURE, "instantiating-procedure", NodeTypesOption.KEYWORD),
  INT64(Proparse.INT64, "int64", NodeTypesOption.KEYWORD),
  INTEGER(Proparse.INTEGER, "integer", 3, NodeTypesOption.KEYWORD),
  INTERFACE(Proparse.INTERFACE, "interface", NodeTypesOption.KEYWORD),
  INTERNALENTRIES(Proparse.INTERNALENTRIES, "internal-entries", NodeTypesOption.KEYWORD),
  INTERVAL(Proparse.INTERVAL, "interval", NodeTypesOption.KEYWORD),
  INTO(Proparse.INTO, "into", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  INVOKE(Proparse.INVOKE, "invoke", NodeTypesOption.KEYWORD),
  IS(Proparse.IS, "is", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ISATTRSPACE(Proparse.ISATTRSPACE, "is-attr-space", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ISCLASS(Proparse.ISCLASS, "is-class", 7, NodeTypesOption.KEYWORD),
  ISCODEPAGEFIXED(Proparse.ISCODEPAGEFIXED, "is-codepage-fixed", NodeTypesOption.KEYWORD),
  ISCOLUMNCODEPAGE(Proparse.ISCOLUMNCODEPAGE, "is-column-codepage", NodeTypesOption.KEYWORD),
  ISDBMULTITENANT(Proparse.ISDBMULTITENANT, "is-db-multi-tenant", NodeTypesOption.KEYWORD),
  ISJSON(Proparse.ISJSON, "is-json", NodeTypesOption.KEYWORD),
  ISLEADBYTE(Proparse.ISLEADBYTE, "is-lead-byte", NodeTypesOption.KEYWORD),
  ISMULTITENANT(Proparse.ISMULTITENANT, "is-multi-tenant", NodeTypesOption.KEYWORD),
  ISODATE(Proparse.ISODATE, "iso-date", NodeTypesOption.KEYWORD),
  ISOPEN(Proparse.ISOPEN, "is-open", NodeTypesOption.KEYWORD),
  ISPARAMETERSET(Proparse.ISPARAMETERSET, "is-parameter-set", NodeTypesOption.KEYWORD),
  ISPARTITIONED(Proparse.ISPARTITIONED, "is-partitioned", 13, NodeTypesOption.KEYWORD),
  ISROWSELECTED(Proparse.ISROWSELECTED, "is-row-selected", NodeTypesOption.KEYWORD),
  ISSELECTED(Proparse.ISSELECTED, "is-selected", NodeTypesOption.KEYWORD),
  ISXML(Proparse.ISXML, "is-xml", NodeTypesOption.KEYWORD),
  ITEM(Proparse.ITEM, "item", NodeTypesOption.KEYWORD),
  ITEMSPERROW(Proparse.ITEMSPERROW, "items-per-row", NodeTypesOption.KEYWORD),
  ITERATIONCHANGED(Proparse.ITERATIONCHANGED, "iteration-changed", NodeTypesOption.KEYWORD),

  // J
  JOIN(Proparse.JOIN, "join", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  JOINBYSQLDB(Proparse.JOINBYSQLDB, "join-by-sqldb", NodeTypesOption.KEYWORD),
  JOINONSELECT(Proparse.JOINONSELECT, "join-on-select", NodeTypesOption.KEYWORD),

  // K
  KBLABEL(Proparse.KBLABEL, "kblabel", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  KEEPCONNECTIONOPEN(Proparse.KEEPCONNECTIONOPEN, "keep-connection-open", NodeTypesOption.KEYWORD),
  KEEPFRAMEZORDER(Proparse.KEEPFRAMEZORDER, "keep-frame-z-order", 12, NodeTypesOption.KEYWORD),
  KEEPMESSAGES(Proparse.KEEPMESSAGES, "keep-messages", NodeTypesOption.KEYWORD),
  KEEPSECURITYCACHE(Proparse.KEEPSECURITYCACHE, "keep-security-cache", NodeTypesOption.KEYWORD),
  KEEPTABORDER(Proparse.KEEPTABORDER, "keep-tab-order", NodeTypesOption.KEYWORD),
  KEY(Proparse.KEY, "key", NodeTypesOption.KEYWORD),
  KEYCACHEJOIN(Proparse.KEYCACHEJOIN, "keycache-join", NodeTypesOption.KEYWORD),
  KEYS(Proparse.KEYS, "keys", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  KEYWORD(Proparse.KEYWORD, "keyword", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  KEYWORDALL(Proparse.KEYWORDALL, "keyword-all", NodeTypesOption.KEYWORD),

  // L
  LABEL(Proparse.LABEL, "label", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LABELBGCOLOR(Proparse.LABELBGCOLOR, "label-bgcolor", 9, NodeTypesOption.KEYWORD),
  LABELDCOLOR(Proparse.LABELDCOLOR, "label-dcolor", 8, NodeTypesOption.KEYWORD),
  LABELFGCOLOR(Proparse.LABELFGCOLOR, "label-fgcolor", 9, NodeTypesOption.KEYWORD),
  LABELFONT(Proparse.LABELFONT, "label-font", NodeTypesOption.KEYWORD),
  LABELPFCOLOR(Proparse.LABELPFCOLOR, "label-pfcolor", 9, NodeTypesOption.KEYWORD),
  LABELS(Proparse.LABELS, "labels", NodeTypesOption.KEYWORD),
  LABELSHAVECOLONS(Proparse.LABELSHAVECOLONS, "labels-have-colons", NodeTypesOption.KEYWORD),
  LANDSCAPE(Proparse.LANDSCAPE, "landscape", NodeTypesOption.KEYWORD),
  LANGUAGES(Proparse.LANGUAGES, "languages", 8, NodeTypesOption.KEYWORD),
  LARGE(Proparse.LARGE, "large", NodeTypesOption.KEYWORD),
  LARGETOSMALL(Proparse.LARGETOSMALL, "large-to-small", NodeTypesOption.KEYWORD),
  LAST(Proparse.LAST, "last", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LASTASYNCREQUEST(Proparse.LASTASYNCREQUEST, "last-async-request", 10, NodeTypesOption.KEYWORD),
  LASTBATCH(Proparse.LASTBATCH, "last-batch", NodeTypesOption.KEYWORD),
  LASTCHILD(Proparse.LASTCHILD, "last-child", NodeTypesOption.KEYWORD),
  LASTEVENT(Proparse.LASTEVENT, "last-event", 9, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LASTFORM(Proparse.LASTFORM, "last-form", NodeTypesOption.KEYWORD),
  LASTOBJECT(Proparse.LASTOBJECT, "last-object", NodeTypesOption.KEYWORD),
  LASTOF(Proparse.LASTOF, "last-of", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LASTPROCEDURE(Proparse.LASTPROCEDURE, "last-procedure", 10, NodeTypesOption.KEYWORD),
  LASTSERVER(Proparse.LASTSERVER, "last-server", 9, NodeTypesOption.KEYWORD),
  LASTSERVERSOCKET(Proparse.LASTSERVERSOCKET, "last-server-socket", NodeTypesOption.KEYWORD),
  LASTSOCKET(Proparse.LASTSOCKET, "last-socket", NodeTypesOption.KEYWORD),
  LASTTABITEM(Proparse.LASTTABITEM, "last-tab-item", 10, NodeTypesOption.KEYWORD),
  LDBNAME(Proparse.LDBNAME, "ldbname", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LE(Proparse.LE, "le", NodeTypesOption.KEYWORD),
  LEADING(Proparse.LEADING, "leading", NodeTypesOption.KEYWORD),
  LEAKDETECTION(Proparse.LEAKDETECTION, "leak-detection", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LEAVE(Proparse.LEAVE, "leave", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LEFT(Proparse.LEFT, "left", NodeTypesOption.KEYWORD),
  LEFTALIGNED(Proparse.LEFTALIGNED, "left-aligned", 10, NodeTypesOption.KEYWORD),
  LEFTEND(Proparse.LEFTEND, "left-end", NodeTypesOption.KEYWORD),
  LEFTTRIM(Proparse.LEFTTRIM, "left-trim", NodeTypesOption.KEYWORD),
  LENGTH(Proparse.LENGTH, "length", NodeTypesOption.KEYWORD),
  LIBRARY(Proparse.LIBRARY, "library", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LIBRARYCALLINGCONVENTION(Proparse.LIBRARYCALLINGCONVENTION, "library-calling-convention", NodeTypesOption.KEYWORD),
  LIKE(Proparse.LIKE, "like", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LIKESEQUENTIAL(Proparse.LIKESEQUENTIAL, "like-sequential", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LINE(Proparse.LINE, "line", NodeTypesOption.KEYWORD),
  LINECOUNTER(Proparse.LINECOUNTER, "line-counter", 10, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LINEDOWN(Proparse.LINEDOWN, "line-down", NodeTypesOption.KEYWORD),
  LINELEFT(Proparse.LINELEFT, "line-left", NodeTypesOption.KEYWORD),
  LINERIGHT(Proparse.LINERIGHT, "line-right", NodeTypesOption.KEYWORD),
  LINEUP(Proparse.LINEUP, "line-up", NodeTypesOption.KEYWORD),
  LISTEVENTS(Proparse.LISTEVENTS, "list-events", NodeTypesOption.KEYWORD),
  LISTING(Proparse.LISTING, "listing", 5, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LISTINGS(Proparse.LISTINGS, "listings", NodeTypesOption.KEYWORD),
  LISTITEMPAIRS(Proparse.LISTITEMPAIRS, "list-item-pairs", NodeTypesOption.KEYWORD),
  LISTITEMS(Proparse.LISTITEMS, "list-items", NodeTypesOption.KEYWORD),
  LISTPROPERTYNAMES(Proparse.LISTPROPERTYNAMES, "list-property-names", NodeTypesOption.KEYWORD),
  LISTQUERYATTRS(Proparse.LISTQUERYATTRS, "list-query-attrs", NodeTypesOption.KEYWORD),
  LISTSETATTRS(Proparse.LISTSETATTRS, "list-set-attrs", NodeTypesOption.KEYWORD),
  LISTWIDGETS(Proparse.LISTWIDGETS, "list-widgets", NodeTypesOption.KEYWORD),
  LITERALQUESTION(Proparse.LITERALQUESTION, "literal-question", NodeTypesOption.KEYWORD),
  LITTLEENDIAN(Proparse.LITTLEENDIAN, "little-endian", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LOAD(Proparse.LOAD, "load", NodeTypesOption.KEYWORD),
  LOADDOMAINS(Proparse.LOADDOMAINS, "load-domains", NodeTypesOption.KEYWORD),
  LOADFROM(Proparse.LOADFROM, "load-from", NodeTypesOption.KEYWORD),
  LOADICON(Proparse.LOADICON, "load-icon", NodeTypesOption.KEYWORD),
  LOADIMAGE(Proparse.LOADIMAGE, "load-image", NodeTypesOption.KEYWORD),
  LOADIMAGEDOWN(Proparse.LOADIMAGEDOWN, "load-image-down", NodeTypesOption.KEYWORD),
  LOADIMAGEINSENSITIVE(Proparse.LOADIMAGEINSENSITIVE, "load-image-insensitive", NodeTypesOption.KEYWORD),
  LOADIMAGEUP(Proparse.LOADIMAGEUP, "load-image-up", NodeTypesOption.KEYWORD),
  LOADMOUSEPOINTER(Proparse.LOADMOUSEPOINTER, "load-mouse-pointer", 12, NodeTypesOption.KEYWORD),
  LOADPICTURE(Proparse.LOADPICTURE, "load-picture", NodeTypesOption.KEYWORD),
  LOADRESULTINTO(Proparse.LOADRESULTINTO, "load-result-into", NodeTypesOption.KEYWORD),
  LOADSMALLICON(Proparse.LOADSMALLICON, "load-small-icon", NodeTypesOption.KEYWORD),
  LOBDIR(Proparse.LOBDIR, "lob-dir", NodeTypesOption.KEYWORD),
  LOCALHOST(Proparse.LOCALHOST, "local-host", NodeTypesOption.KEYWORD),
  LOCALNAME(Proparse.LOCALNAME, "local-name", NodeTypesOption.KEYWORD),
  LOCALPORT(Proparse.LOCALPORT, "local-port", NodeTypesOption.KEYWORD),
  LOCALVERSIONINFO(Proparse.LOCALVERSIONINFO, "local-version-info", NodeTypesOption.KEYWORD),
  LOCATORCOLUMNNUMBER(Proparse.LOCATORCOLUMNNUMBER, "locator-column-number", NodeTypesOption.KEYWORD),
  LOCATORLINENUMBER(Proparse.LOCATORLINENUMBER, "locator-line-number", NodeTypesOption.KEYWORD),
  LOCATORPUBLICID(Proparse.LOCATORPUBLICID, "locator-public-id", NodeTypesOption.KEYWORD),
  LOCATORSYSTEMID(Proparse.LOCATORSYSTEMID, "locator-system-id", NodeTypesOption.KEYWORD),
  LOCATORTYPE(Proparse.LOCATORTYPE, "locator-type", NodeTypesOption.KEYWORD),
  LOCKED(Proparse.LOCKED, "locked", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LOCKREGISTRATION(Proparse.LOCKREGISTRATION, "lock-registration", NodeTypesOption.KEYWORD),
  LOG(Proparse.LOG, "log", NodeTypesOption.KEYWORD),
  LOGAUDITEVENT(Proparse.LOGAUDITEVENT, "log-audit-event", NodeTypesOption.KEYWORD),
  LOGENTRYTYPES(Proparse.LOGENTRYTYPES, "log-entry-types", NodeTypesOption.KEYWORD),
  LOGFILENAME(Proparse.LOGFILENAME, "logfile-name", NodeTypesOption.KEYWORD),
  LOGGINGLEVEL(Proparse.LOGGINGLEVEL, "logging-level", NodeTypesOption.KEYWORD),
  LOGICAL(Proparse.LOGICAL, "logical", NodeTypesOption.KEYWORD),
  LOGID(Proparse.LOGID, "log-id", NodeTypesOption.KEYWORD),
  LOGINEXPIRATIONTIMESTAMP(Proparse.LOGINEXPIRATIONTIMESTAMP, "login-expiration-timestamp", NodeTypesOption.KEYWORD),
  LOGINHOST(Proparse.LOGINHOST, "login-host", NodeTypesOption.KEYWORD),
  LOGINSTATE(Proparse.LOGINSTATE, "login-state", NodeTypesOption.KEYWORD),
  LOGMANAGER(Proparse.LOGMANAGER, "log-manager", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LOGOUT(Proparse.LOGOUT, "logout", NodeTypesOption.KEYWORD),
  LOGTHRESHOLD(Proparse.LOGTHRESHOLD, "log-threshold", NodeTypesOption.KEYWORD),
  LONG(Proparse.LONG, "long", NodeTypesOption.KEYWORD),
  LONGCHAR(Proparse.LONGCHAR, "longchar", 6, NodeTypesOption.KEYWORD),
  LONGCHARTONODEVALUE(Proparse.LONGCHARTONODEVALUE, "longchar-to-node-value", NodeTypesOption.KEYWORD),
  LOOKAHEAD(Proparse.LOOKAHEAD, "lookahead", NodeTypesOption.KEYWORD),
  LOOKUP(Proparse.LOOKUP, "lookup", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LTHAN(Proparse.LTHAN, "lt", NodeTypesOption.KEYWORD),

  // M
  MACHINECLASS(Proparse.MACHINECLASS, "machine-class", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  MAINMENU(Proparse.MAINMENU, "main-menu", NodeTypesOption.KEYWORD),
  MANDATORY(Proparse.MANDATORY, "mandatory", NodeTypesOption.KEYWORD),
  MANUALHIGHLIGHT(Proparse.MANUALHIGHLIGHT, "manual-highlight", NodeTypesOption.KEYWORD),
  MAP(Proparse.MAP, "map", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  MARGINEXTRA(Proparse.MARGINEXTRA, "margin-extra", NodeTypesOption.KEYWORD),
  MARGINHEIGHT(Proparse.MARGINHEIGHT, "margin-height", NodeTypesOption.KEYWORD),
  MARGINHEIGHTCHARS(Proparse.MARGINHEIGHTCHARS, "margin-height-chars", 15, NodeTypesOption.KEYWORD),
  MARGINHEIGHTPIXELS(Proparse.MARGINHEIGHTPIXELS, "margin-height-pixels", 15, NodeTypesOption.KEYWORD),
  MARGINWIDTH(Proparse.MARGINWIDTH, "margin-width", NodeTypesOption.KEYWORD),
  MARGINWIDTHCHARS(Proparse.MARGINWIDTHCHARS, "margin-width-chars", 14, NodeTypesOption.KEYWORD),
  MARGINWIDTHPIXELS(Proparse.MARGINWIDTHPIXELS, "margin-width-pixels", 14, NodeTypesOption.KEYWORD),
  MARKNEW(Proparse.MARKNEW, "mark-new", NodeTypesOption.KEYWORD),
  MARKROWSTATE(Proparse.MARKROWSTATE, "mark-row-state", NodeTypesOption.KEYWORD),
  MATCHES(Proparse.MATCHES, "matches", NodeTypesOption.KEYWORD),
  MAXBUTTON(Proparse.MAXBUTTON, "max-button", NodeTypesOption.KEYWORD),
  MAXCHARS(Proparse.MAXCHARS, "max-chars", NodeTypesOption.KEYWORD),
  MAXDATAGUESS(Proparse.MAXDATAGUESS, "max-data-guess", NodeTypesOption.KEYWORD),
  MAXHEIGHT(Proparse.MAXHEIGHT, "max-height", NodeTypesOption.KEYWORD),
  MAXHEIGHTCHARS(Proparse.MAXHEIGHTCHARS, "max-height-chars", 12, NodeTypesOption.KEYWORD),
  MAXHEIGHTPIXELS(Proparse.MAXHEIGHTPIXELS, "max-height-pixels", 12, NodeTypesOption.KEYWORD),
  MAXIMIZE(Proparse.MAXIMIZE, "maximize", NodeTypesOption.KEYWORD),
  MAXIMUMLEVEL(Proparse.MAXIMUMLEVEL, "maximum-level", NodeTypesOption.KEYWORD),
  MAXROWS(Proparse.MAXROWS, "max-rows", NodeTypesOption.KEYWORD),
  MAXSIZE(Proparse.MAXSIZE, "max-size", NodeTypesOption.KEYWORD),
  MAXVALUE(Proparse.MAXVALUE, "max-value", 7, NodeTypesOption.KEYWORD),
  MAXWIDTH(Proparse.MAXWIDTH, "max-width", NodeTypesOption.KEYWORD),
  MAXWIDTHCHARS(Proparse.MAXWIDTHCHARS, "max-width-chars", 11, NodeTypesOption.KEYWORD),
  MAXWIDTHPIXELS(Proparse.MAXWIDTHPIXELS, "max-width-pixels", 11, NodeTypesOption.KEYWORD),
  MD5DIGEST(Proparse.MD5DIGEST, "md5-digest", NodeTypesOption.KEYWORD),
  MD5VALUE(Proparse.MD5VALUE, "md5-value", NodeTypesOption.KEYWORD),
  MEMBER(Proparse.MEMBER, "member", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  MEMPTR(Proparse.MEMPTR, "memptr", NodeTypesOption.KEYWORD),
  MEMPTRTONODEVALUE(Proparse.MEMPTRTONODEVALUE, "memptr-to-node-value", NodeTypesOption.KEYWORD),
  MENU(Proparse.MENU, "menu", NodeTypesOption.KEYWORD),
  MENUDROP(Proparse.MENUDROP, "menu-drop", NodeTypesOption.KEYWORD),
  MENUITEM(Proparse.MENUITEM, "menu-item", NodeTypesOption.KEYWORD),
  MENUKEY(Proparse.MENUKEY, "menu-key", 6, NodeTypesOption.KEYWORD),
  MENUMOUSE(Proparse.MENUMOUSE, "menu-mouse", 6, NodeTypesOption.KEYWORD),
  MERGEBYFIELD(Proparse.MERGEBYFIELD, "merge-by-field", NodeTypesOption.KEYWORD),
  MERGECHANGES(Proparse.MERGECHANGES, "merge-changes", NodeTypesOption.KEYWORD),
  MERGEROWCHANGES(Proparse.MERGEROWCHANGES, "merge-row-changes", NodeTypesOption.KEYWORD),
  MESSAGE(Proparse.MESSAGE, "message", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  MESSAGEAREA(Proparse.MESSAGEAREA, "message-area", NodeTypesOption.KEYWORD),
  MESSAGEAREAFONT(Proparse.MESSAGEAREAFONT, "message-area-font", NodeTypesOption.KEYWORD),
  MESSAGEAREAMSG(Proparse.MESSAGEAREAMSG, "message-area-msg", NodeTypesOption.KEYWORD),
  MESSAGEDIGEST(Proparse.MESSAGEDIGEST, "message-digest", NodeTypesOption.KEYWORD),
  MESSAGELINE(Proparse.MESSAGELINE, "message-line", NodeTypesOption.KEYWORD),
  MESSAGELINES(Proparse.MESSAGELINES, "message-lines", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  METHOD(Proparse.METHOD, "method", NodeTypesOption.KEYWORD),
  MINBUTTON(Proparse.MINBUTTON, "min-button", NodeTypesOption.KEYWORD),
  MINCOLUMNWIDTHCHARS(Proparse.MINCOLUMNWIDTHCHARS, "min-column-width-chars", 18, NodeTypesOption.KEYWORD),
  MINCOLUMNWIDTHPIXELS(Proparse.MINCOLUMNWIDTHPIXELS, "min-column-width-pixels", 18, NodeTypesOption.KEYWORD),
  MINHEIGHT(Proparse.MINHEIGHT, "min-height", NodeTypesOption.KEYWORD),
  MINHEIGHTCHARS(Proparse.MINHEIGHTCHARS, "min-height-chars", 12, NodeTypesOption.KEYWORD),
  MINHEIGHTPIXELS(Proparse.MINHEIGHTPIXELS, "min-height-pixels", 12, NodeTypesOption.KEYWORD),
  MINIMUM(Proparse.MINIMUM, "minimum", 3, NodeTypesOption.KEYWORD),
  MINSCHEMAMARSHALL(Proparse.MINSCHEMAMARSHALL, "min-schema-marshall", 18, NodeTypesOption.KEYWORD),
  MINSIZE(Proparse.MINSIZE, "min-size", NodeTypesOption.KEYWORD),
  MINVALUE(Proparse.MINVALUE, "min-value", 7, NodeTypesOption.KEYWORD),
  MINWIDTH(Proparse.MINWIDTH, "min-width", NodeTypesOption.KEYWORD),
  MINWIDTHCHARS(Proparse.MINWIDTHCHARS, "min-width-chars", 11, NodeTypesOption.KEYWORD),
  MINWIDTHPIXELS(Proparse.MINWIDTHPIXELS, "min-width-pixels", 11, NodeTypesOption.KEYWORD),
  MODIFIED(Proparse.MODIFIED, "modified", NodeTypesOption.KEYWORD),
  MONTH(Proparse.MONTH, "month", NodeTypesOption.KEYWORD),
  MOUSE(Proparse.MOUSE, "mouse", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  MOUSEPOINTER(Proparse.MOUSEPOINTER, "mouse-pointer", 7, NodeTypesOption.KEYWORD),
  MOVABLE(Proparse.MOVABLE, "movable", NodeTypesOption.KEYWORD),
  MOVE(Proparse.MOVE, "move", NodeTypesOption.KEYWORD),
  MOVEAFTERTABITEM(Proparse.MOVEAFTERTABITEM, "move-after-tab-item", 10, NodeTypesOption.KEYWORD),
  MOVEBEFORETABITEM(Proparse.MOVEBEFORETABITEM, "move-before-tab-item", 10, NodeTypesOption.KEYWORD),
  MOVECOLUMN(Proparse.MOVECOLUMN, "move-column", 8, NodeTypesOption.KEYWORD),
  MOVETOBOTTOM(Proparse.MOVETOBOTTOM, "move-to-bottom", 9, NodeTypesOption.KEYWORD),
  MOVETOEOF(Proparse.MOVETOEOF, "move-to-eof", NodeTypesOption.KEYWORD),
  MOVETOTOP(Proparse.MOVETOTOP, "move-to-top", 9, NodeTypesOption.KEYWORD),
  MPE(Proparse.MPE, "mpe", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  MTIME(Proparse.MTIME, "mtime", NodeTypesOption.KEYWORD),
  MULTICOMPILE(Proparse.MULTICOMPILE, "multi-compile", NodeTypesOption.KEYWORD),
  MULTIPLE(Proparse.MULTIPLE, "multiple", NodeTypesOption.KEYWORD),
  MULTIPLEKEY(Proparse.MULTIPLEKEY, "multiple-key", NodeTypesOption.KEYWORD),
  MULTITASKINGINTERVAL(Proparse.MULTITASKINGINTERVAL, "multitasking-interval", NodeTypesOption.KEYWORD),
  MUSTEXIST(Proparse.MUSTEXIST, "must-exist", NodeTypesOption.KEYWORD),
  MUSTUNDERSTAND(Proparse.MUSTUNDERSTAND, "must-understand", NodeTypesOption.KEYWORD),

  // N
  NAME(Proparse.NAME, "name", NodeTypesOption.KEYWORD),
  NAMESPACEPREFIX(Proparse.NAMESPACEPREFIX, "namespace-prefix", NodeTypesOption.KEYWORD),
  NAMESPACEURI(Proparse.NAMESPACEURI, "namespace-uri", NodeTypesOption.KEYWORD),
  NATIVE(Proparse.NATIVE, "native", NodeTypesOption.KEYWORD),
  NE(Proparse.NE, "ne", NodeTypesOption.KEYWORD),
  NEEDSAPPSERVERPROMPT(Proparse.NEEDSAPPSERVERPROMPT, "needs-appserver-prompt", NodeTypesOption.KEYWORD),
  NEEDSPROMPT(Proparse.NEEDSPROMPT, "needs-prompt", NodeTypesOption.KEYWORD),
  NESTED(Proparse.NESTED, "nested", NodeTypesOption.KEYWORD),
  NEW(Proparse.NEW, "new", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NEWINSTANCE(Proparse.NEWINSTANCE, "new-instance", NodeTypesOption.KEYWORD),
  NEWLINE(Proparse.NEWLINE, "new-line", NodeTypesOption.KEYWORD),
  NEWROW(Proparse.NEWROW, "new-row", NodeTypesOption.KEYWORD),
  NEXT(Proparse.NEXT, "next", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NEXTCOLUMN(Proparse.NEXTCOLUMN, "next-column", 8, NodeTypesOption.KEYWORD),
  NEXTERROR(Proparse.NEXTERROR, "next-error", NodeTypesOption.KEYWORD),
  NEXTFRAME(Proparse.NEXTFRAME, "next-frame", NodeTypesOption.KEYWORD),
  NEXTPROMPT(Proparse.NEXTPROMPT, "next-prompt", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NEXTROWID(Proparse.NEXTROWID, "next-rowid", NodeTypesOption.KEYWORD),
  NEXTSIBLING(Proparse.NEXTSIBLING, "next-sibling", NodeTypesOption.KEYWORD),
  NEXTTABITEM(Proparse.NEXTTABITEM, "next-tab-item", 12, NodeTypesOption.KEYWORD),
  NEXTVALUE(Proparse.NEXTVALUE, "next-value", NodeTypesOption.KEYWORD),
  NEXTWORD(Proparse.NEXTWORD, "next-word", NodeTypesOption.KEYWORD),
  NO(Proparse.NO, "no", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOAPPLY(Proparse.NOAPPLY, "no-apply", NodeTypesOption.KEYWORD),
  NOARRAYMESSAGE(Proparse.NOARRAYMESSAGE, "no-array-message", 10, NodeTypesOption.KEYWORD),
  NOASSIGN(Proparse.NOASSIGN, "no-assign", NodeTypesOption.KEYWORD),
  NOAUTOTRIM(Proparse.NOAUTOTRIM, "no-auto-trim", 11, NodeTypesOption.KEYWORD),
  NOAUTOVALIDATE(Proparse.NOAUTOVALIDATE, "no-auto-validate", NodeTypesOption.KEYWORD),
  NOBINDWHERE(Proparse.NOBINDWHERE, "no-bind-where", NodeTypesOption.KEYWORD),
  NOBOX(Proparse.NOBOX, "no-box", NodeTypesOption.KEYWORD),
  NOCOLUMNSCROLLING(Proparse.NOCOLUMNSCROLLING, "no-column-scrolling", 12, NodeTypesOption.KEYWORD),
  NOCONSOLE(Proparse.NOCONSOLE, "no-console", NodeTypesOption.KEYWORD),
  NOCONVERT(Proparse.NOCONVERT, "no-convert", NodeTypesOption.KEYWORD),
  NOCONVERT3DCOLORS(Proparse.NOCONVERT3DCOLORS, "no-convert-3d-colors", 13, NodeTypesOption.KEYWORD),
  NOCURRENTVALUE(Proparse.NOCURRENTVALUE, "no-current-value", NodeTypesOption.KEYWORD),
  NODEBUG(Proparse.NODEBUG, "no-debug", NodeTypesOption.KEYWORD),
  NODETYPE(Proparse.NODETYPE, "node-type", NodeTypesOption.KEYWORD),
  NODEVALUE(Proparse.NODEVALUE, "node-value", NodeTypesOption.KEYWORD),
  NODEVALUETOLONGCHAR(Proparse.NODEVALUETOLONGCHAR, "node-value-to-longchar", NodeTypesOption.KEYWORD),
  NODEVALUETOMEMPTR(Proparse.NODEVALUETOMEMPTR, "node-value-to-memptr", NodeTypesOption.KEYWORD),
  NODRAG(Proparse.NODRAG, "no-drag", NodeTypesOption.KEYWORD),
  NOECHO(Proparse.NOECHO, "no-echo", NodeTypesOption.KEYWORD),
  NOEMPTYSPACE(Proparse.NOEMPTYSPACE, "no-empty-space", NodeTypesOption.KEYWORD),
  NOERROR(Proparse.NOERROR, "no-error", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOFILL(Proparse.NOFILL, "no-fill", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOFIREHOSECURSOR(Proparse.NOFIREHOSECURSOR, "no-firehose-cursor", NodeTypesOption.KEYWORD),
  NOFOCUS(Proparse.NOFOCUS, "no-focus", NodeTypesOption.KEYWORD),
  NOHELP(Proparse.NOHELP, "no-help", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOHIDE(Proparse.NOHIDE, "no-hide", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOINDEXHINT(Proparse.NOINDEXHINT, "no-index-hint", NodeTypesOption.KEYWORD),
  NOINHERITBGCOLOR(Proparse.NOINHERITBGCOLOR, "no-inherit-bgcolor", 14, NodeTypesOption.KEYWORD),
  NOINHERITFGCOLOR(Proparse.NOINHERITFGCOLOR, "no-inherit-fgcolor", 14, NodeTypesOption.KEYWORD),
  NOJOINBYSQLDB(Proparse.NOJOINBYSQLDB, "no-join-by-sqldb", NodeTypesOption.KEYWORD),
  NOKEYCACHEJOIN(Proparse.NOKEYCACHEJOIN, "no-keycache-join", NodeTypesOption.KEYWORD),
  NOLABELS(Proparse.NOLABELS, "no-labels", 8, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOLOBS(Proparse.NOLOBS, "no-lobs", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOLOCK(Proparse.NOLOCK, "no-lock", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOLOOKAHEAD(Proparse.NOLOOKAHEAD, "no-lookahead", NodeTypesOption.KEYWORD),
  NOMAP(Proparse.NOMAP, "no-map", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOMESSAGE(Proparse.NOMESSAGE, "no-message", 6, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NONAMESPACESCHEMALOCATION(Proparse.NONAMESPACESCHEMALOCATION, "nonamespace-schema-location", NodeTypesOption.KEYWORD),
  NONE(Proparse.NONE, "none", NodeTypesOption.KEYWORD),
  NONSERIALIZABLE(Proparse.NONSERIALIZABLE, "non-serializable", NodeTypesOption.KEYWORD),
  NOPAUSE(Proparse.NOPAUSE, "no-pause", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOPREFETCH(Proparse.NOPREFETCH, "no-prefetch", 8, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOQUERYORDERADDED(Proparse.NOQUERYORDERADDED, "no-query-order-added", 10, NodeTypesOption.KEYWORD),
  NOQUERYUNIQUEADDED(Proparse.NOQUERYUNIQUEADDED, "no-query-unique-added", 10, NodeTypesOption.KEYWORD),
  NORETURNVALUE(Proparse.NORETURNVALUE, "no-return-value", 13, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NORMALIZE(Proparse.NORMALIZE, "normalize", NodeTypesOption.KEYWORD),
  NOROWMARKERS(Proparse.NOROWMARKERS, "no-row-markers", NodeTypesOption.KEYWORD),
  NOSCHEMAMARSHALL(Proparse.NOSCHEMAMARSHALL, "no-schema-marshall", 17, NodeTypesOption.KEYWORD),
  NOSCROLLBARVERTICAL(Proparse.NOSCROLLBARVERTICAL, "no-scrollbar-vertical", 14, NodeTypesOption.KEYWORD),
  NOSCROLLING(Proparse.NOSCROLLING, "no-scrolling", NodeTypesOption.KEYWORD),
  NOSEPARATECONNECTION(Proparse.NOSEPARATECONNECTION, "no-separate-connection", NodeTypesOption.KEYWORD),
  NOSEPARATORS(Proparse.NOSEPARATORS, "no-separators", NodeTypesOption.KEYWORD),
  NOT(Proparse.NOT, "not", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOTABSTOP(Proparse.NOTABSTOP, "no-tab-stop", 6, NodeTypesOption.KEYWORD),
  NOTACTIVE(Proparse.NOTACTIVE, "not-active", NodeTypesOption.KEYWORD),
  NOUNDERLINE(Proparse.NOUNDERLINE, "no-underline", 6, NodeTypesOption.KEYWORD),
  NOUNDO(Proparse.NOUNDO, "no-undo", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOVALIDATE(Proparse.NOVALIDATE, "no-validate", 6, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOW(Proparse.NOW, "now", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOWAIT(Proparse.NOWAIT, "no-wait", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOWORDWRAP(Proparse.NOWORDWRAP, "no-word-wrap", NodeTypesOption.KEYWORD),
  NULL(Proparse.NULL, "null", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NUMALIASES(Proparse.NUMALIASES, "num-aliases", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NUMBUFFERS(Proparse.NUMBUFFERS, "num-buffers", NodeTypesOption.KEYWORD),
  NUMBUTTONS(Proparse.NUMBUTTONS, "num-buttons", 7, NodeTypesOption.KEYWORD),
  NUMCHILDRELATIONS(Proparse.NUMCHILDRELATIONS, "num-child-relations", NodeTypesOption.KEYWORD),
  NUMCHILDREN(Proparse.NUMCHILDREN, "num-children", NodeTypesOption.KEYWORD),
  NUMCOLUMNS(Proparse.NUMCOLUMNS, "num-columns", 7, NodeTypesOption.KEYWORD),
  NUMCOPIES(Proparse.NUMCOPIES, "num-copies", NodeTypesOption.KEYWORD),
  NUMDBS(Proparse.NUMDBS, "num-dbs", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NUMDROPPEDFILES(Proparse.NUMDROPPEDFILES, "num-dropped-files", NodeTypesOption.KEYWORD),
  NUMENTRIES(Proparse.NUMENTRIES, "num-entries", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NUMERIC(Proparse.NUMERIC, "numeric", NodeTypesOption.KEYWORD),
  NUMERICDECIMALPOINT(Proparse.NUMERICDECIMALPOINT, "numeric-decimal-point", 11, NodeTypesOption.KEYWORD),
  NUMERICFORMAT(Proparse.NUMERICFORMAT, "numeric-format", 9, NodeTypesOption.KEYWORD),
  NUMERICSEPARATOR(Proparse.NUMERICSEPARATOR, "numeric-separator", 11, NodeTypesOption.KEYWORD),
  NUMFIELDS(Proparse.NUMFIELDS, "num-fields", NodeTypesOption.KEYWORD),
  NUMFORMATS(Proparse.NUMFORMATS, "num-formats", NodeTypesOption.KEYWORD),
  NUMHEADERENTRIES(Proparse.NUMHEADERENTRIES, "num-header-entries", NodeTypesOption.KEYWORD),
  NUMITEMS(Proparse.NUMITEMS, "num-items", NodeTypesOption.KEYWORD),
  NUMITERATIONS(Proparse.NUMITERATIONS, "num-iterations", NodeTypesOption.KEYWORD),
  NUMLINES(Proparse.NUMLINES, "num-lines", NodeTypesOption.KEYWORD),
  NUMLOCKEDCOLUMNS(Proparse.NUMLOCKEDCOLUMNS, "num-locked-columns", 14, NodeTypesOption.KEYWORD),
  NUMLOGFILES(Proparse.NUMLOGFILES, "num-log-files", NodeTypesOption.KEYWORD),
  NUMMESSAGES(Proparse.NUMMESSAGES, "num-messages", NodeTypesOption.KEYWORD),
  NUMPARAMETERS(Proparse.NUMPARAMETERS, "num-parameters", NodeTypesOption.KEYWORD),
  NUMREFERENCES(Proparse.NUMREFERENCES, "num-references", NodeTypesOption.KEYWORD),
  NUMRELATIONS(Proparse.NUMRELATIONS, "num-relations", NodeTypesOption.KEYWORD),
  NUMREPLACED(Proparse.NUMREPLACED, "num-replaced", 8, NodeTypesOption.KEYWORD),
  NUMRESULTS(Proparse.NUMRESULTS, "num-results", NodeTypesOption.KEYWORD),
  NUMSELECTED(Proparse.NUMSELECTED, "num-selected", NodeTypesOption.KEYWORD),
  NUMSELECTEDROWS(Proparse.NUMSELECTEDROWS, "num-selected-rows", NodeTypesOption.KEYWORD),
  NUMSELECTEDWIDGETS(Proparse.NUMSELECTEDWIDGETS, "num-selected-widgets", NodeTypesOption.KEYWORD),
  NUMSOURCEBUFFERS(Proparse.NUMSOURCEBUFFERS, "num-source-buffers", NodeTypesOption.KEYWORD),
  NUMTABS(Proparse.NUMTABS, "num-tabs", NodeTypesOption.KEYWORD),
  NUMTOPBUFFERS(Proparse.NUMTOPBUFFERS, "num-top-buffers", NodeTypesOption.KEYWORD),
  NUMTORETAIN(Proparse.NUMTORETAIN, "num-to-retain", NodeTypesOption.KEYWORD),
  NUMVISIBLECOLUMNS(Proparse.NUMVISIBLECOLUMNS, "num-visible-columns", 15, NodeTypesOption.KEYWORD),

  // O
  OBJECT(Proparse.OBJECT, "object", NodeTypesOption.KEYWORD),
  OCTETLENGTH(Proparse.OCTETLENGTH, "octet_length", NodeTypesOption.KEYWORD),
  OF(Proparse.OF, "of", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OFF(Proparse.OFF, "off", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OFFEND(Proparse.OFFEND, "off-end", NodeTypesOption.KEYWORD),
  OFFHOME(Proparse.OFFHOME, "off-home", NodeTypesOption.KEYWORD),
  OK(Proparse.OK, "ok", NodeTypesOption.KEYWORD),
  OKCANCEL(Proparse.OKCANCEL, "ok-cancel", NodeTypesOption.KEYWORD),
  OLD(Proparse.OLD, "old", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OLEINVOKELOCALE(Proparse.OLEINVOKELOCALE, "ole-invoke-locale", 15, NodeTypesOption.KEYWORD),
  OLENAMESLOCALE(Proparse.OLENAMESLOCALE, "ole-names-locale", 14, NodeTypesOption.KEYWORD),
  ON(Proparse.ON, "on", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ONFRAMEBORDER(Proparse.ONFRAMEBORDER, "on-frame-border", 8, NodeTypesOption.KEYWORD),
  OPEN(Proparse.OPEN, "open", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OPENLINEABOVE(Proparse.OPENLINEABOVE, "open-line-above", NodeTypesOption.KEYWORD),
  OPSYS(Proparse.OPSYS, "opsys", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OPTION(Proparse.OPTION, "option", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OPTIONS(Proparse.OPTIONS, "options", NodeTypesOption.KEYWORD),
  OPTIONSFILE(Proparse.OPTIONSFILE, "options-file", NodeTypesOption.KEYWORD),
  OR(Proparse.OR, "or", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ORDEREDJOIN(Proparse.ORDEREDJOIN, "ordered-join", NodeTypesOption.KEYWORD),
  ORDINAL(Proparse.ORDINAL, "ordinal", NodeTypesOption.KEYWORD),
  ORIENTATION(Proparse.ORIENTATION, "orientation", NodeTypesOption.KEYWORD),
  ORIGINHANDLE(Proparse.ORIGINHANDLE, "origin-handle", NodeTypesOption.KEYWORD),
  ORIGINROWID(Proparse.ORIGINROWID, "origin-rowid", NodeTypesOption.KEYWORD),
  OS2(Proparse.OS2, "os2", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OS400(Proparse.OS400, "os400", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OSAPPEND(Proparse.OSAPPEND, "os-append", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OSCOMMAND(Proparse.OSCOMMAND, "os-command", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OSCOPY(Proparse.OSCOPY, "os-copy", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OSCREATEDIR(Proparse.OSCREATEDIR, "os-create-dir", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OSDELETE(Proparse.OSDELETE, "os-delete", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OSDIR(Proparse.OSDIR, "os-dir", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OSDRIVES(Proparse.OSDRIVES, "os-drives", 8, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OSERROR(Proparse.OSERROR, "os-error", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OSGETENV(Proparse.OSGETENV, "os-getenv", NodeTypesOption.KEYWORD),
  OSRENAME(Proparse.OSRENAME, "os-rename", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OTHERWISE(Proparse.OTHERWISE, "otherwise", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OUTER(Proparse.OUTER, "outer", NodeTypesOption.KEYWORD),
  OUTERJOIN(Proparse.OUTERJOIN, "outer-join", NodeTypesOption.KEYWORD),
  OUTOFDATA(Proparse.OUTOFDATA, "out-of-data", NodeTypesOption.KEYWORD),
  OUTPUT(Proparse.OUTPUT, "output", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OVERLAY(Proparse.OVERLAY, "overlay", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OVERRIDE(Proparse.OVERRIDE, "override", NodeTypesOption.KEYWORD),
  OWNER(Proparse.OWNER, "owner", NodeTypesOption.KEYWORD),
  OWNERDOCUMENT(Proparse.OWNERDOCUMENT, "owner-document", NodeTypesOption.KEYWORD),

  // P
  PACKAGEPRIVATE(Proparse.PACKAGEPRIVATE, "package-private", NodeTypesOption.KEYWORD),
  PACKAGEPROTECTED(Proparse.PACKAGEPROTECTED, "package-protected", NodeTypesOption.KEYWORD),
  PAGE(Proparse.PAGE, "page", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PAGEBOTTOM(Proparse.PAGEBOTTOM, "page-bottom", 8, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PAGED(Proparse.PAGED, "paged", NodeTypesOption.KEYWORD),
  PAGEDOWN(Proparse.PAGEDOWN, "page-down", NodeTypesOption.KEYWORD),
  PAGELEFT(Proparse.PAGELEFT, "page-left", NodeTypesOption.KEYWORD),
  PAGENUMBER(Proparse.PAGENUMBER, "page-number", 8, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PAGERIGHT(Proparse.PAGERIGHT, "page-right", NodeTypesOption.KEYWORD),
  PAGERIGHTTEXT(Proparse.PAGERIGHTTEXT, "page-right-text", NodeTypesOption.KEYWORD),
  PAGESIZE(Proparse.PAGESIZE, "page-size", NodeTypesOption.KEYWORD),
  PAGETOP(Proparse.PAGETOP, "page-top", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PAGEUP(Proparse.PAGEUP, "page-up", NodeTypesOption.KEYWORD),
  PAGEWIDTH(Proparse.PAGEWIDTH, "page-width", 8, NodeTypesOption.KEYWORD),
  PARAMETER(Proparse.PARAMETER, "parameter", 5, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PARENT(Proparse.PARENT, "parent", NodeTypesOption.KEYWORD),
  PARENTBUFFER(Proparse.PARENTBUFFER, "parent-buffer", NodeTypesOption.KEYWORD),
  PARENTFIELDSAFTER(Proparse.PARENTFIELDSAFTER, "parent-fields-after", NodeTypesOption.KEYWORD),
  PARENTFIELDSBEFORE(Proparse.PARENTFIELDSBEFORE, "parent-fields-before", NodeTypesOption.KEYWORD),
  PARENTIDFIELD(Proparse.PARENTIDFIELD, "parent-id-field", NodeTypesOption.KEYWORD),
  PARENTIDRELATION(Proparse.PARENTIDRELATION, "parent-id-relation", NodeTypesOption.KEYWORD),
  PARENTRELATION(Proparse.PARENTRELATION, "parent-relation", 10, NodeTypesOption.KEYWORD),
  PARENTWINDOWCLOSE(Proparse.PARENTWINDOWCLOSE, "parent-window-close", NodeTypesOption.KEYWORD),
  PARSESTATUS(Proparse.PARSESTATUS, "parse-status", NodeTypesOption.KEYWORD),
  PARTIALKEY(Proparse.PARTIALKEY, "partial-key", NodeTypesOption.KEYWORD),
  PASCAL(Proparse.PASCAL, "pascal", NodeTypesOption.KEYWORD),
  PASSWORDFIELD(Proparse.PASSWORDFIELD, "password-field", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PASTE(Proparse.PASTE, "paste", NodeTypesOption.KEYWORD),
  PATHNAME(Proparse.PATHNAME, "pathname", NodeTypesOption.KEYWORD),
  PAUSE(Proparse.PAUSE, "pause", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PBEHASHALGORITHM(Proparse.PBEHASHALGORITHM, "pbe-hash-algorithm", 12, NodeTypesOption.KEYWORD),
  PBEKEYROUNDS(Proparse.PBEKEYROUNDS, "pbe-key-rounds", NodeTypesOption.KEYWORD),
  PDBNAME(Proparse.PDBNAME, "pdbname", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PERFORMANCE(Proparse.PERFORMANCE, "performance", 4, NodeTypesOption.KEYWORD),
  PERSISTENT(Proparse.PERSISTENT, "persistent", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PERSISTENTCACHEDISABLED(Proparse.PERSISTENTCACHEDISABLED, "persistent-cache-disabled", NodeTypesOption.KEYWORD),
  PERSISTENTPROCEDURE(Proparse.PERSISTENTPROCEDURE, "persistent-procedure", NodeTypesOption.KEYWORD),
  PFCOLOR(Proparse.PFCOLOR, "pfcolor", 3, NodeTypesOption.KEYWORD),
  PICK(Proparse.PICK, "pick", NodeTypesOption.KEYWORD),
  PICKAREA(Proparse.PICKAREA, "pick-area", NodeTypesOption.KEYWORD),
  PICKBOTH(Proparse.PICKBOTH, "pick-both", NodeTypesOption.KEYWORD),
  PIXELS(Proparse.PIXELS, "pixels", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PIXELSPERCOLUMN(Proparse.PIXELSPERCOLUMN, "pixels-per-column", 14, NodeTypesOption.KEYWORD),
  PIXELSPERROW(Proparse.PIXELSPERROW, "pixels-per-row", NodeTypesOption.KEYWORD),
  POPUPMENU(Proparse.POPUPMENU, "popup-menu", 7, NodeTypesOption.KEYWORD),
  POPUPONLY(Proparse.POPUPONLY, "popup-only", 7, NodeTypesOption.KEYWORD),
  PORTRAIT(Proparse.PORTRAIT, "portrait", NodeTypesOption.KEYWORD),
  POSITION(Proparse.POSITION, "position", NodeTypesOption.KEYWORD),
  PRECISION(Proparse.PRECISION, "precision", NodeTypesOption.KEYWORD),
  PREFERDATASET(Proparse.PREFERDATASET, "prefer-dataset", NodeTypesOption.KEYWORD),
  PREPARED(Proparse.PREPARED, "prepared", NodeTypesOption.KEYWORD),
  PREPARESTRING(Proparse.PREPARESTRING, "prepare-string", NodeTypesOption.KEYWORD),
  PREPROCESS(Proparse.PREPROCESS, "preprocess", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PRESELECT(Proparse.PRESELECT, "preselect", 6, NodeTypesOption.KEYWORD),
  PREV(Proparse.PREV, "prev", NodeTypesOption.KEYWORD),
  PREVCOLUMN(Proparse.PREVCOLUMN, "prev-column", 8, NodeTypesOption.KEYWORD),
  PREVFRAME(Proparse.PREVFRAME, "prev-frame", NodeTypesOption.KEYWORD),
  PREVSIBLING(Proparse.PREVSIBLING, "prev-sibling", NodeTypesOption.KEYWORD),
  PREVTABITEM(Proparse.PREVTABITEM, "prev-tab-item", 10, NodeTypesOption.KEYWORD),
  PREVWORD(Proparse.PREVWORD, "prev-word", NodeTypesOption.KEYWORD),
  PRIMARY(Proparse.PRIMARY, "primary", NodeTypesOption.KEYWORD),
  PRIMARYPASSPHRASE(Proparse.PRIMARYPASSPHRASE, "primary-passphrase", NodeTypesOption.KEYWORD),
  PRINTER(Proparse.PRINTER, "printer", NodeTypesOption.KEYWORD),
  PRINTERCONTROLHANDLE(Proparse.PRINTERCONTROLHANDLE, "printer-control-handle", NodeTypesOption.KEYWORD),
  PRINTERHDC(Proparse.PRINTERHDC, "printer-hdc", NodeTypesOption.KEYWORD),
  PRINTERNAME(Proparse.PRINTERNAME, "printer-name", NodeTypesOption.KEYWORD),
  PRINTERPORT(Proparse.PRINTERPORT, "printer-port", NodeTypesOption.KEYWORD),
  PRINTERSETUP(Proparse.PRINTERSETUP, "printer-setup", NodeTypesOption.KEYWORD),
  PRIVATE(Proparse.PRIVATE, "private", NodeTypesOption.KEYWORD),
  PRIVATEDATA(Proparse.PRIVATEDATA, "private-data", 9, NodeTypesOption.KEYWORD),
  PRIVILEGES(Proparse.PRIVILEGES, "privileges", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PROCEDURE(Proparse.PROCEDURE, "procedure", 5, NodeTypesOption.KEYWORD),
  PROCEDURECALLTYPE(Proparse.PROCEDURECALLTYPE, "procedure-call-type", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  PROCEDURECOMPLETE(Proparse.PROCEDURECOMPLETE, "procedure-complete", NodeTypesOption.KEYWORD),
  PROCEDURENAME(Proparse.PROCEDURENAME, "procedure-name", NodeTypesOption.KEYWORD),
  PROCEDURETYPE(Proparse.PROCEDURETYPE, "procedure-type", NodeTypesOption.KEYWORD),
  PROCESS(Proparse.PROCESS, "process", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PROCESSARCHITECTURE(Proparse.PROCESSARCHITECTURE, "process-architecture", NodeTypesOption.KEYWORD),
  PROCHANDLE(Proparse.PROCHANDLE, "proc-handle", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PROCSTATUS(Proparse.PROCSTATUS, "proc-status", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PROFILEFILE(Proparse.PROFILEFILE, "profile-file", NodeTypesOption.KEYWORD),
  PROFILER(Proparse.PROFILER, "profiler", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PROFILING(Proparse.PROFILING, "profiling", NodeTypesOption.KEYWORD),
  PROGRAMNAME(Proparse.PROGRAMNAME, "program-name", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PROGRESS(Proparse.PROGRESS, "progress", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PROGRESSSOURCE(Proparse.PROGRESSSOURCE, "progress-source", 10, NodeTypesOption.KEYWORD),
  PROMPT(Proparse.PROMPT, "prompt", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PROMPTFOR(Proparse.PROMPTFOR, "prompt-for", 8, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PROMSGS(Proparse.PROMSGS, "promsgs", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PROPATH(Proparse.PROPATH, "propath", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PROPERTY(Proparse.PROPERTY, "property", NodeTypesOption.KEYWORD),
  PROTECTED(Proparse.PROTECTED, "protected", NodeTypesOption.KEYWORD),
  PROVERSION(Proparse.PROVERSION, "proversion", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PROXY(Proparse.PROXY, "proxy", NodeTypesOption.KEYWORD),
  PROXYPASSWORD(Proparse.PROXYPASSWORD, "proxy-password", NodeTypesOption.KEYWORD),
  PROXYUSERID(Proparse.PROXYUSERID, "proxy-userid", NodeTypesOption.KEYWORD),
  PUBLIC(Proparse.PUBLIC, "public", NodeTypesOption.KEYWORD),
  PUBLICID(Proparse.PUBLICID, "public-id", NodeTypesOption.KEYWORD),
  PUBLISH(Proparse.PUBLISH, "publish", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PUBLISHEDEVENTS(Proparse.PUBLISHEDEVENTS, "published-events", NodeTypesOption.KEYWORD),
  PUT(Proparse.PUT, "put", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PUTBITS(Proparse.PUTBITS, "put-bits", NodeTypesOption.KEYWORD),
  PUTBYTES(Proparse.PUTBYTES, "put-bytes", NodeTypesOption.KEYWORD),
  PUTDOUBLE(Proparse.PUTDOUBLE, "put-double", NodeTypesOption.KEYWORD),
  PUTFLOAT(Proparse.PUTFLOAT, "put-float", NodeTypesOption.KEYWORD),
  PUTINT64(Proparse.PUTINT64, "put-int64", NodeTypesOption.KEYWORD),
  PUTKEYVALUE(Proparse.PUTKEYVALUE, "put-key-value", 11, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PUTLONG(Proparse.PUTLONG, "put-long", NodeTypesOption.KEYWORD),
  PUTSHORT(Proparse.PUTSHORT, "put-short", NodeTypesOption.KEYWORD),
  PUTSTRING(Proparse.PUTSTRING, "put-string", NodeTypesOption.KEYWORD),
  PUTUNSIGNEDLONG(Proparse.PUTUNSIGNEDLONG, "put-unsigned-long", NodeTypesOption.KEYWORD),
  PUTUNSIGNEDSHORT(Proparse.PUTUNSIGNEDSHORT, "put-unsigned-short", NodeTypesOption.KEYWORD),

  // Q
  QUALIFIEDUSERID(Proparse.QUALIFIEDUSERID, "qualified-user-id", NodeTypesOption.KEYWORD),
  QUERY(Proparse.QUERY, "query", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  QUERYCLOSE(Proparse.QUERYCLOSE, "query-close", NodeTypesOption.KEYWORD),
  QUERYOFFEND(Proparse.QUERYOFFEND, "query-off-end", NodeTypesOption.KEYWORD),
  QUERYOPEN(Proparse.QUERYOPEN, "query-open", NodeTypesOption.KEYWORD),
  QUERYPREPARE(Proparse.QUERYPREPARE, "query-prepare", NodeTypesOption.KEYWORD),
  QUERYTUNING(Proparse.QUERYTUNING, "query-tuning", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  QUESTION(Proparse.QUESTION, "question", NodeTypesOption.KEYWORD),
  QUIT(Proparse.QUIT, "quit", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  QUOTER(Proparse.QUOTER, "quoter", NodeTypesOption.KEYWORD),

  // R
  RADIOBUTTONS(Proparse.RADIOBUTTONS, "radio-buttons", NodeTypesOption.KEYWORD),
  RADIOSET(Proparse.RADIOSET, "radio-set", NodeTypesOption.KEYWORD),
  RANDOM(Proparse.RANDOM, "random", NodeTypesOption.KEYWORD),
  RAW(Proparse.RAW, "raw", NodeTypesOption.KEYWORD),
  RAWTRANSFER(Proparse.RAWTRANSFER, "raw-transfer", NodeTypesOption.KEYWORD),
  RCODEINFORMATION(Proparse.RCODEINFORMATION, "rcode-information", 10, NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  READ(Proparse.READ, "read", NodeTypesOption.KEYWORD),
  READAVAILABLE(Proparse.READAVAILABLE, "read-available", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  READEXACTNUM(Proparse.READEXACTNUM, "read-exact-num", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  READFILE(Proparse.READFILE, "read-file", NodeTypesOption.KEYWORD),
  READJSON(Proparse.READJSON, "read-json", NodeTypesOption.KEYWORD),
  READKEY(Proparse.READKEY, "readkey", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  READONLY(Proparse.READONLY, "read-only", NodeTypesOption.KEYWORD),
  READRESPONSE(Proparse.READRESPONSE, "read-response", NodeTypesOption.KEYWORD),
  READXML(Proparse.READXML, "read-xml", NodeTypesOption.KEYWORD),
  READXMLSCHEMA(Proparse.READXMLSCHEMA, "read-xmlschema", NodeTypesOption.KEYWORD),
  REAL(Proparse.REAL, "real", NodeTypesOption.KEYWORD),
  RECALL(Proparse.RECALL, "recall", NodeTypesOption.KEYWORD),
  RECID(Proparse.RECID, "recid", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  RECORDLENGTH(Proparse.RECORDLENGTH, "record-length", 10, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  RECTANGLE(Proparse.RECTANGLE, "rectangle", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  RECURSIVE(Proparse.RECURSIVE, "recursive", NodeTypesOption.KEYWORD),
  REFERENCEONLY(Proparse.REFERENCEONLY, "reference-only", NodeTypesOption.KEYWORD),
  REFRESH(Proparse.REFRESH, "refresh", NodeTypesOption.KEYWORD),
  REFRESHABLE(Proparse.REFRESHABLE, "refreshable", NodeTypesOption.KEYWORD),
  REFRESHAUDITPOLICY(Proparse.REFRESHAUDITPOLICY, "refresh-audit-policy", NodeTypesOption.KEYWORD),
  REGISTERDOMAIN(Proparse.REGISTERDOMAIN, "register-domain", NodeTypesOption.KEYWORD),
  REINSTATE(Proparse.REINSTATE, "reinstate", NodeTypesOption.KEYWORD),
  REJECTCHANGES(Proparse.REJECTCHANGES, "reject-changes", NodeTypesOption.KEYWORD),
  REJECTED(Proparse.REJECTED, "rejected", NodeTypesOption.KEYWORD),
  REJECTROWCHANGES(Proparse.REJECTROWCHANGES, "reject-row-changes", NodeTypesOption.KEYWORD),
  RELATIONFIELDS(Proparse.RELATIONFIELDS, "relation-fields", 11, NodeTypesOption.KEYWORD),
  RELATIONSACTIVE(Proparse.RELATIONSACTIVE, "relations-active", NodeTypesOption.KEYWORD),
  RELEASE(Proparse.RELEASE, "release", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  REMOTE(Proparse.REMOTE, "remote", NodeTypesOption.KEYWORD),
  REMOTEHOST(Proparse.REMOTEHOST, "remote-host", NodeTypesOption.KEYWORD),
  REMOTEPORT(Proparse.REMOTEPORT, "remote-port", NodeTypesOption.KEYWORD),
  REMOVEATTRIBUTE(Proparse.REMOVEATTRIBUTE, "remove-attribute", NodeTypesOption.KEYWORD),
  REMOVECHILD(Proparse.REMOVECHILD, "remove-child", NodeTypesOption.KEYWORD),
  REMOVEEVENTSPROCEDURE(Proparse.REMOVEEVENTSPROCEDURE, "remove-events-procedure", 18, NodeTypesOption.KEYWORD),
  REMOVESUPERPROCEDURE(Proparse.REMOVESUPERPROCEDURE, "remove-super-procedure", 17, NodeTypesOption.KEYWORD),
  REPEAT(Proparse.REPEAT, "repeat", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  REPLACE(Proparse.REPLACE, "replace", NodeTypesOption.KEYWORD),
  REPLACECHILD(Proparse.REPLACECHILD, "replace-child", NodeTypesOption.KEYWORD),
  REPLACESELECTIONTEXT(Proparse.REPLACESELECTIONTEXT, "replace-selection-text", NodeTypesOption.KEYWORD),
  REPLICATIONCREATE(Proparse.REPLICATIONCREATE, "replication-create", NodeTypesOption.KEYWORD),
  REPLICATIONDELETE(Proparse.REPLICATIONDELETE, "replication-delete", NodeTypesOption.KEYWORD),
  REPLICATIONWRITE(Proparse.REPLICATIONWRITE, "replication-write", NodeTypesOption.KEYWORD),
  REPORTS(Proparse.REPORTS, "reports", NodeTypesOption.KEYWORD),
  REPOSITION(Proparse.REPOSITION, "reposition", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  REPOSITIONBACKWARDS(Proparse.REPOSITIONBACKWARDS, "reposition-backwards", 15, NodeTypesOption.KEYWORD),
  REPOSITIONFORWARDS(Proparse.REPOSITIONFORWARDS, "reposition-forwards", 15, NodeTypesOption.KEYWORD),
  REPOSITIONPARENTRELATION(Proparse.REPOSITIONPARENTRELATION, "reposition-parent-relation", 21,
      NodeTypesOption.KEYWORD),
  REPOSITIONTOROW(Proparse.REPOSITIONTOROW, "reposition-to-row", NodeTypesOption.KEYWORD),
  REPOSITIONTOROWID(Proparse.REPOSITIONTOROWID, "reposition-to-rowid", NodeTypesOption.KEYWORD),
  REQUEST(Proparse.REQUEST, "request", NodeTypesOption.KEYWORD),
  REQUESTINFO(Proparse.REQUESTINFO, "request-info", NodeTypesOption.KEYWORD),
  RESET(Proparse.RESET, "reset", NodeTypesOption.KEYWORD),
  RESIZABLE(Proparse.RESIZABLE, "resizable", 6, NodeTypesOption.KEYWORD),
  RESIZE(Proparse.RESIZE, "resize", NodeTypesOption.KEYWORD),
  RESPONSEINFO(Proparse.RESPONSEINFO, "response-info", NodeTypesOption.KEYWORD),
  RESTARTROW(Proparse.RESTARTROW, "restart-row", NodeTypesOption.KEYWORD),
  RESTARTROWID(Proparse.RESTARTROWID, "restart-rowid", NodeTypesOption.KEYWORD),
  RESULT(Proparse.RESULT, "result", NodeTypesOption.KEYWORD),
  RESUMEDISPLAY(Proparse.RESUMEDISPLAY, "resume-display", NodeTypesOption.KEYWORD),
  RETAIN(Proparse.RETAIN, "retain", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  RETAINSHAPE(Proparse.RETAINSHAPE, "retain-shape", 8, NodeTypesOption.KEYWORD),
  RETRY(Proparse.RETRY, "retry", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  RETRYCANCEL(Proparse.RETRYCANCEL, "retry-cancel", NodeTypesOption.KEYWORD),
  RETURN(Proparse.RETURN, "return", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  RETURNINSERTED(Proparse.RETURNINSERTED, "return-inserted", 10, NodeTypesOption.KEYWORD),
  RETURNS(Proparse.RETURNS, "returns", NodeTypesOption.KEYWORD),
  RETURNTOSTARTDIR(Proparse.RETURNTOSTARTDIR, "return-to-start-dir", 18, NodeTypesOption.KEYWORD),
  RETURNVALUE(Proparse.RETURNVALUE, "return-value", 10, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  RETURNVALUEDATATYPE(Proparse.RETURNVALUEDATATYPE, "return-value-data-type", NodeTypesOption.KEYWORD),
  RETURNVALUEDLLTYPE(Proparse.RETURNVALUEDLLTYPE, "return-value-dll-type", NodeTypesOption.KEYWORD),
  REVERSEFROM(Proparse.REVERSEFROM, "reverse-from", NodeTypesOption.KEYWORD),
  REVERT(Proparse.REVERT, "revert", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  REVOKE(Proparse.REVOKE, "revoke", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  RGBVALUE(Proparse.RGBVALUE, "rgb-value", 5, NodeTypesOption.KEYWORD),
  RIGHT(Proparse.RIGHT, "right", NodeTypesOption.KEYWORD),
  RIGHTALIGNED(Proparse.RIGHTALIGNED, "right-aligned", 11, NodeTypesOption.KEYWORD),
  RIGHTEND(Proparse.RIGHTEND, "right-end", NodeTypesOption.KEYWORD),
  RIGHTTRIM(Proparse.RIGHTTRIM, "right-trim", NodeTypesOption.KEYWORD),
  RINDEX(Proparse.RINDEX, "r-index", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ROLE(Proparse.ROLE, "role", NodeTypesOption.KEYWORD),
  ROLES(Proparse.ROLES, "roles", NodeTypesOption.KEYWORD),
  ROUND(Proparse.ROUND, "round", NodeTypesOption.KEYWORD),
  ROUNDED(Proparse.ROUNDED, "rounded", NodeTypesOption.KEYWORD),
  ROUTINELEVEL(Proparse.ROUTINELEVEL, "routine-level", NodeTypesOption.KEYWORD),
  ROW(Proparse.ROW, "row", NodeTypesOption.KEYWORD),
  ROWCREATED(Proparse.ROWCREATED, "row-created", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ROWDELETED(Proparse.ROWDELETED, "row-deleted", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ROWDISPLAY(Proparse.ROWDISPLAY, "row-display", NodeTypesOption.KEYWORD),
  ROWENTRY(Proparse.ROWENTRY, "row-entry", NodeTypesOption.KEYWORD),
  ROWHEIGHTPIXELS(Proparse.ROWHEIGHTPIXELS, "row-height-pixels", 12, NodeTypesOption.KEYWORD),
  ROWID(Proparse.ROWID, "rowid", NodeTypesOption.KEYWORD),
  ROWLEAVE(Proparse.ROWLEAVE, "row-leave", NodeTypesOption.KEYWORD),
  ROWMARKERS(Proparse.ROWMARKERS, "row-markers", 6, NodeTypesOption.KEYWORD),
  ROWMODIFIED(Proparse.ROWMODIFIED, "row-modified", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ROWOF(Proparse.ROWOF, "row-of", NodeTypesOption.KEYWORD),
  ROWRESIZABLE(Proparse.ROWRESIZABLE, "row-resizable", NodeTypesOption.KEYWORD),
  ROWSTATE(Proparse.ROWSTATE, "row-state", NodeTypesOption.KEYWORD),
  ROWUNMODIFIED(Proparse.ROWUNMODIFIED, "row-unmodified", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  RULE(Proparse.RULE, "rule", NodeTypesOption.KEYWORD),
  RULEROW(Proparse.RULEROW, "rule-row", NodeTypesOption.KEYWORD),
  RULEY(Proparse.RULEY, "rule-y", NodeTypesOption.KEYWORD),
  RUN(Proparse.RUN, "run", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  RUNPROCEDURE(Proparse.RUNPROCEDURE, "run-procedure", 8, NodeTypesOption.KEYWORD),

  // S
  SAVE(Proparse.SAVE, "save", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SAVEAS(Proparse.SAVEAS, "save-as", NodeTypesOption.KEYWORD),
  SAVEFILE(Proparse.SAVEFILE, "save-file", NodeTypesOption.KEYWORD),
  SAVEROWCHANGES(Proparse.SAVEROWCHANGES, "save-row-changes", NodeTypesOption.KEYWORD),
  SAVEWHERESTRING(Proparse.SAVEWHERESTRING, "save-where-string", NodeTypesOption.KEYWORD),
  SAXATTRIBUTES(Proparse.SAXATTRIBUTES, "sax-attributes", NodeTypesOption.KEYWORD),
  SAXCOMPLETE(Proparse.SAXCOMPLETE, "sax-complete", 10, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SAXPARSE(Proparse.SAXPARSE, "sax-parse", NodeTypesOption.KEYWORD),
  SAXPARSEFIRST(Proparse.SAXPARSEFIRST, "sax-parse-first", NodeTypesOption.KEYWORD),
  SAXPARSENEXT(Proparse.SAXPARSENEXT, "sax-parse-next", NodeTypesOption.KEYWORD),
  SAXPARSERERROR(Proparse.SAXPARSERERROR, "sax-parser-error", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SAXREADER(Proparse.SAXREADER, "sax-reader", NodeTypesOption.KEYWORD),
  SAXRUNNING(Proparse.SAXRUNNING, "sax-running", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SAXUNINITIALIZED(Proparse.SAXUNINITIALIZED, "sax-uninitialized", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SAXWRITEBEGIN(Proparse.SAXWRITEBEGIN, "sax-write-begin", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SAXWRITECOMPLETE(Proparse.SAXWRITECOMPLETE, "sax-write-complete", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SAXWRITECONTENT(Proparse.SAXWRITECONTENT, "sax-write-content", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SAXWRITEELEMENT(Proparse.SAXWRITEELEMENT, "sax-write-element", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SAXWRITEERROR(Proparse.SAXWRITEERROR, "sax-write-error", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SAXWRITEIDLE(Proparse.SAXWRITEIDLE, "sax-write-idle", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SAXWRITER(Proparse.SAXWRITER, "sax-writer", NodeTypesOption.KEYWORD),
  SAXWRITETAG(Proparse.SAXWRITETAG, "sax-write-tag", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SAXXML(Proparse.SAXXML, "sax-xml", NodeTypesOption.KEYWORD),
  SCHEMA(Proparse.SCHEMA, "schema", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SCHEMACHANGE(Proparse.SCHEMACHANGE, "schema-change", NodeTypesOption.KEYWORD),
  SCHEMALOCATION(Proparse.SCHEMALOCATION, "schema-location", NodeTypesOption.KEYWORD),
  SCHEMAMARSHAL(Proparse.SCHEMAMARSHAL, "schema-marshal", NodeTypesOption.KEYWORD),
  SCHEMAPATH(Proparse.SCHEMAPATH, "schema-path", NodeTypesOption.KEYWORD),
  SCREEN(Proparse.SCREEN, "screen", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SCREENIO(Proparse.SCREENIO, "screen-io", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SCREENLINES(Proparse.SCREENLINES, "screen-lines", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SCREENVALUE(Proparse.SCREENVALUE, "screen-value", 10, NodeTypesOption.KEYWORD),
  SCROLL(Proparse.SCROLL, "scroll", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SCROLLABLE(Proparse.SCROLLABLE, "scrollable", NodeTypesOption.KEYWORD),
  SCROLLBARDRAG(Proparse.SCROLLBARDRAG, "scrollbar-drag", NodeTypesOption.KEYWORD),
  SCROLLBARHORIZONTAL(Proparse.SCROLLBARHORIZONTAL, "scrollbar-horizontal", 11, NodeTypesOption.KEYWORD),
  SCROLLBARS(Proparse.SCROLLBARS, "scroll-bars", NodeTypesOption.KEYWORD),
  SCROLLBARVERTICAL(Proparse.SCROLLBARVERTICAL, "scrollbar-vertical", 11, NodeTypesOption.KEYWORD),
  SCROLLEDROWPOSITION(Proparse.SCROLLEDROWPOSITION, "scrolled-row-position", 16, NodeTypesOption.KEYWORD),
  SCROLLHORIZONTAL(Proparse.SCROLLHORIZONTAL, "scroll-horizontal", NodeTypesOption.KEYWORD),
  SCROLLING(Proparse.SCROLLING, "scrolling", NodeTypesOption.KEYWORD),
  SCROLLLEFT(Proparse.SCROLLLEFT, "scroll-left", NodeTypesOption.KEYWORD),
  SCROLLMODE(Proparse.SCROLLMODE, "scroll-mode", NodeTypesOption.KEYWORD),
  SCROLLNOTIFY(Proparse.SCROLLNOTIFY, "scroll-notify", NodeTypesOption.KEYWORD),
  SCROLLRIGHT(Proparse.SCROLLRIGHT, "scroll-right", NodeTypesOption.KEYWORD),
  SCROLLTOCURRENTROW(Proparse.SCROLLTOCURRENTROW, "scroll-to-current-row", NodeTypesOption.KEYWORD),
  SCROLLTOITEM(Proparse.SCROLLTOITEM, "scroll-to-item", 11, NodeTypesOption.KEYWORD),
  SCROLLTOSELECTEDROW(Proparse.SCROLLTOSELECTEDROW, "scroll-to-selected-row", NodeTypesOption.KEYWORD),
  SCROLLVERTICAL(Proparse.SCROLLVERTICAL, "scroll-vertical", NodeTypesOption.KEYWORD),
  SDBNAME(Proparse.SDBNAME, "sdbname", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SEAL(Proparse.SEAL, "seal", NodeTypesOption.KEYWORD),
  SEALTIMESTAMP(Proparse.SEALTIMESTAMP, "seal-timestamp", NodeTypesOption.KEYWORD),
  SEARCH(Proparse.SEARCH, "search", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SEARCHSELF(Proparse.SEARCHSELF, "search-self", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SEARCHTARGET(Proparse.SEARCHTARGET, "search-target", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SECTION(Proparse.SECTION, "section", NodeTypesOption.KEYWORD),
  SECURITYPOLICY(Proparse.SECURITYPOLICY, "security-policy", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SEEK(Proparse.SEEK, "seek", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SELECT(Proparse.SELECT, "select", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SELECTABLE(Proparse.SELECTABLE, "selectable", NodeTypesOption.KEYWORD),
  SELECTALL(Proparse.SELECTALL, "select-all", NodeTypesOption.KEYWORD),
  SELECTED(Proparse.SELECTED, "selected", NodeTypesOption.KEYWORD),
  SELECTEDITEMS(Proparse.SELECTEDITEMS, "selected-items", NodeTypesOption.KEYWORD),
  SELECTEXTEND(Proparse.SELECTEXTEND, "select-extend", NodeTypesOption.KEYWORD),
  SELECTFOCUSEDROW(Proparse.SELECTFOCUSEDROW, "select-focused-row", NodeTypesOption.KEYWORD),
  SELECTION(Proparse.SELECTION, "selection", NodeTypesOption.KEYWORD),
  SELECTIONEND(Proparse.SELECTIONEND, "selection-end", NodeTypesOption.KEYWORD),
  SELECTIONEXTEND(Proparse.SELECTIONEXTEND, "selection-extend", NodeTypesOption.KEYWORD),
  SELECTIONLIST(Proparse.SELECTIONLIST, "selection-list", NodeTypesOption.KEYWORD),
  SELECTIONSTART(Proparse.SELECTIONSTART, "selection-start", NodeTypesOption.KEYWORD),
  SELECTIONTEXT(Proparse.SELECTIONTEXT, "selection-text", NodeTypesOption.KEYWORD),
  SELECTNEXTROW(Proparse.SELECTNEXTROW, "select-next-row", NodeTypesOption.KEYWORD),
  SELECTONJOIN(Proparse.SELECTONJOIN, "select-on-join", NodeTypesOption.KEYWORD),
  SELECTPREVROW(Proparse.SELECTPREVROW, "select-prev-row", NodeTypesOption.KEYWORD),
  SELECTREPOSITIONEDROW(Proparse.SELECTREPOSITIONEDROW, "select-repositioned-row", NodeTypesOption.KEYWORD),
  SELECTROW(Proparse.SELECTROW, "select-row", NodeTypesOption.KEYWORD),
  SELF(Proparse.SELF, "self", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SEND(Proparse.SEND, "send", NodeTypesOption.KEYWORD),
  SENSITIVE(Proparse.SENSITIVE, "sensitive", NodeTypesOption.KEYWORD),
  SEPARATECONNECTION(Proparse.SEPARATECONNECTION, "separate-connection", NodeTypesOption.KEYWORD),
  SEPARATORFGCOLOR(Proparse.SEPARATORFGCOLOR, "separator-fgcolor", 13, NodeTypesOption.KEYWORD),
  SEPARATORS(Proparse.SEPARATORS, "separators", NodeTypesOption.KEYWORD),
  SERIALIZABLE(Proparse.SERIALIZABLE, "serializable", NodeTypesOption.KEYWORD),
  SERIALIZEHIDDEN(Proparse.SERIALIZEHIDDEN, "serialize-hidden", NodeTypesOption.KEYWORD),
  SERIALIZENAME(Proparse.SERIALIZENAME, "serialize-name", NodeTypesOption.KEYWORD),
  SERIALIZEROW(Proparse.SERIALIZEROW, "serialize-row", NodeTypesOption.KEYWORD),
  SERVER(Proparse.SERVER, "server", NodeTypesOption.KEYWORD),
  SERVERCONNECTIONBOUND(Proparse.SERVERCONNECTIONBOUND, "server-connection-bound", 20, NodeTypesOption.KEYWORD),
  SERVERCONNECTIONBOUNDREQUEST(Proparse.SERVERCONNECTIONBOUNDREQUEST, "server-connection-bound-request", 26,
      NodeTypesOption.KEYWORD),
  SERVERCONNECTIONCONTEXT(Proparse.SERVERCONNECTIONCONTEXT, "server-connection-context", 20, NodeTypesOption.KEYWORD),
  SERVERCONNECTIONID(Proparse.SERVERCONNECTIONID, "server-connection-id", NodeTypesOption.KEYWORD),
  SERVEROPERATINGMODE(Proparse.SERVEROPERATINGMODE, "server-operating-mode", NodeTypesOption.KEYWORD),
  SERVERSOCKET(Proparse.SERVERSOCKET, "server-socket", NodeTypesOption.KEYWORD),
  SESSION(Proparse.SESSION, "session", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SESSIONEND(Proparse.SESSIONEND, "session-end", NodeTypesOption.KEYWORD),
  SESSIONID(Proparse.SESSIONID, "session-id", NodeTypesOption.KEYWORD),
  SET(Proparse.SET, "set", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SETACTOR(Proparse.SETACTOR, "set-actor", NodeTypesOption.KEYWORD),
  SETAPPLCONTEXT(Proparse.SETAPPLCONTEXT, "set-appl-context", NodeTypesOption.KEYWORD),
  SETATTRCALLTYPE(Proparse.SETATTRCALLTYPE, "set-attr-call-type", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SETATTRIBUTE(Proparse.SETATTRIBUTE, "set-attribute", NodeTypesOption.KEYWORD),
  SETATTRIBUTENODE(Proparse.SETATTRIBUTENODE, "set-attribute-node", NodeTypesOption.KEYWORD),
  SETBLUEVALUE(Proparse.SETBLUEVALUE, "set-blue-value", 8, NodeTypesOption.KEYWORD),
  SETBREAK(Proparse.SETBREAK, "set-break", NodeTypesOption.KEYWORD),
  SETBUFFERS(Proparse.SETBUFFERS, "set-buffers", NodeTypesOption.KEYWORD),
  SETBYTEORDER(Proparse.SETBYTEORDER, "set-byte-order", NodeTypesOption.KEYWORD),
  SETCALLBACK(Proparse.SETCALLBACK, "set-callback", NodeTypesOption.KEYWORD),
  SETCALLBACKPROCEDURE(Proparse.SETCALLBACKPROCEDURE, "set-callback-procedure", NodeTypesOption.KEYWORD),
  SETCELLFOCUS(Proparse.SETCELLFOCUS, "set-cell-focus", NodeTypesOption.KEYWORD),
  SETCLIENT(Proparse.SETCLIENT, "set-client", NodeTypesOption.KEYWORD),
  SETCOMMIT(Proparse.SETCOMMIT, "set-commit", NodeTypesOption.KEYWORD),
  SETCONNECTPROCEDURE(Proparse.SETCONNECTPROCEDURE, "set-connect-procedure", NodeTypesOption.KEYWORD),
  SETCONTENTS(Proparse.SETCONTENTS, "set-contents", NodeTypesOption.KEYWORD),
  SETDBCLIENT(Proparse.SETDBCLIENT, "set-db-client", NodeTypesOption.KEYWORD),
  SETDBLOGGING(Proparse.SETDBLOGGING, "set-db-logging", NodeTypesOption.KEYWORD),
  SETDYNAMIC(Proparse.SETDYNAMIC, "set-dynamic", NodeTypesOption.KEYWORD),
  SETEFFECTIVETENANT(Proparse.SETEFFECTIVETENANT, "set-effective-tenant", NodeTypesOption.KEYWORD),
  SETEVENTMANAGEROPTION(Proparse.SETEVENTMANAGEROPTION, "set-event-manager-option", NodeTypesOption.KEYWORD),
  SETGREENVALUE(Proparse.SETGREENVALUE, "set-green-value", 9, NodeTypesOption.KEYWORD),
  SETINPUTSOURCE(Proparse.SETINPUTSOURCE, "set-input-source", NodeTypesOption.KEYWORD),
  SETMUSTUNDERSTAND(Proparse.SETMUSTUNDERSTAND, "set-must-understand", NodeTypesOption.KEYWORD),
  SETNODE(Proparse.SETNODE, "set-node", NodeTypesOption.KEYWORD),
  SETNUMERICFORMAT(Proparse.SETNUMERICFORMAT, "set-numeric-format", 16, NodeTypesOption.KEYWORD),
  SETOPTION(Proparse.SETOPTION, "set-option", NodeTypesOption.KEYWORD),
  SETOUTPUTDESTINATION(Proparse.SETOUTPUTDESTINATION, "set-output-destination", NodeTypesOption.KEYWORD),
  SETPARAMETER(Proparse.SETPARAMETER, "set-parameter", NodeTypesOption.KEYWORD),
  SETPOINTERVALUE(Proparse.SETPOINTERVALUE, "set-pointer-value", 15, NodeTypesOption.KEYWORD),
  SETPROPERTY(Proparse.SETPROPERTY, "set-property", NodeTypesOption.KEYWORD),
  SETREADRESPONSEPROCEDURE(Proparse.SETREADRESPONSEPROCEDURE, "set-read-response-procedure", NodeTypesOption.KEYWORD),
  SETREDVALUE(Proparse.SETREDVALUE, "set-red-value", 7, NodeTypesOption.KEYWORD),
  SETREPOSITIONEDROW(Proparse.SETREPOSITIONEDROW, "set-repositioned-row", NodeTypesOption.KEYWORD),
  SETRGBVALUE(Proparse.SETRGBVALUE, "set-rgb-value", 7, NodeTypesOption.KEYWORD),
  SETROLE(Proparse.SETROLE, "set-role", NodeTypesOption.KEYWORD),
  SETROLLBACK(Proparse.SETROLLBACK, "set-rollback", NodeTypesOption.KEYWORD),
  SETSAFEUSER(Proparse.SETSAFEUSER, "set-safe-user", NodeTypesOption.KEYWORD),
  SETSELECTION(Proparse.SETSELECTION, "set-selection", NodeTypesOption.KEYWORD),
  SETSERIALIZED(Proparse.SETSERIALIZED, "set-serialized", NodeTypesOption.KEYWORD),
  SETSIZE(Proparse.SETSIZE, "set-size", NodeTypesOption.KEYWORD),
  SETSOCKETOPTION(Proparse.SETSOCKETOPTION, "set-socket-option", NodeTypesOption.KEYWORD),
  SETSORTARROW(Proparse.SETSORTARROW, "set-sort-arrow", NodeTypesOption.KEYWORD),
  SETSTATE(Proparse.SETSTATE, "set-state", NodeTypesOption.KEYWORD),
  SETTINGS(Proparse.SETTINGS, "settings", NodeTypesOption.KEYWORD),
  SETUSERID(Proparse.SETUSERID, "setuserid", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SETWAITSTATE(Proparse.SETWAITSTATE, "set-wait-state", 8, NodeTypesOption.KEYWORD),
  SHA1DIGEST(Proparse.SHA1DIGEST, "sha1-digest", NodeTypesOption.KEYWORD),
  SHARED(Proparse.SHARED, "shared", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SHARELOCK(Proparse.SHARELOCK, "share-lock", 5, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SHORT(Proparse.SHORT, "short", NodeTypesOption.KEYWORD),
  SHOWINTASKBAR(Proparse.SHOWINTASKBAR, "show-in-taskbar", 12, NodeTypesOption.KEYWORD),
  SHOWSTATS(Proparse.SHOWSTATS, "show-stats", 9, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SIDELABELS(Proparse.SIDELABELS, "side-labels", 8, NodeTypesOption.KEYWORD),
  SIDELABELHANDLE(Proparse.SIDELABELHANDLE, "side-label-handle", 12, NodeTypesOption.KEYWORD),
  SIGNATURE(Proparse.SIGNATURE, "signature", NodeTypesOption.KEYWORD),
  SIGNATUREVALUE(Proparse.SIGNATUREVALUE, "signature-value", NodeTypesOption.KEYWORD),
  SILENT(Proparse.SILENT, "silent", NodeTypesOption.KEYWORD),
  SIMPLE(Proparse.SIMPLE, "simple", NodeTypesOption.KEYWORD),
  SINGLE(Proparse.SINGLE, "single", NodeTypesOption.KEYWORD),
  SINGLECHARACTER(Proparse.SINGLECHARACTER, "single-character", NodeTypesOption.KEYWORD),
  SINGLERUN(Proparse.SINGLERUN, "single-run", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SINGLETON(Proparse.SINGLETON, "singleton", NodeTypesOption.KEYWORD),
  SIZE(Proparse.SIZE, "size", NodeTypesOption.KEYWORD),
  SIZECHARS(Proparse.SIZECHARS, "size-chars", 6, NodeTypesOption.KEYWORD),
  SIZEPIXELS(Proparse.SIZEPIXELS, "size-pixels", 6, NodeTypesOption.KEYWORD),
  SKIP(Proparse.SKIP, "skip", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SKIPDELETEDRECORD(Proparse.SKIPDELETEDRECORD, "skip-deleted-record", 16, NodeTypesOption.KEYWORD),
  SKIPGROUPDUPLICATES(Proparse.SKIPGROUPDUPLICATES, "skip-group-duplicates", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  SKIPSCHEMACHECK(Proparse.SKIPSCHEMACHECK, "skip-schema-check", NodeTypesOption.KEYWORD),
  SLIDER(Proparse.SLIDER, "slider", NodeTypesOption.KEYWORD),
  SMALLICON(Proparse.SMALLICON, "small-icon", NodeTypesOption.KEYWORD),
  SMALLINT(Proparse.SMALLINT, "smallint", NodeTypesOption.KEYWORD),
  SMALLTITLE(Proparse.SMALLTITLE, "small-title", NodeTypesOption.KEYWORD),
  SOAPFAULT(Proparse.SOAPFAULT, "soap-fault", NodeTypesOption.KEYWORD),
  SOAPFAULTACTOR(Proparse.SOAPFAULTACTOR, "soap-fault-actor", NodeTypesOption.KEYWORD),
  SOAPFAULTCODE(Proparse.SOAPFAULTCODE, "soap-fault-code", NodeTypesOption.KEYWORD),
  SOAPFAULTDETAIL(Proparse.SOAPFAULTDETAIL, "soap-fault-detail", NodeTypesOption.KEYWORD),
  SOAPFAULTMISUNDERSTOODHEADER(Proparse.SOAPFAULTMISUNDERSTOODHEADER, "soap-fault-misunderstood-header",
      NodeTypesOption.KEYWORD),
  SOAPFAULTNODE(Proparse.SOAPFAULTNODE, "soap-fault-node", NodeTypesOption.KEYWORD),
  SOAPFAULTROLE(Proparse.SOAPFAULTROLE, "soap-fault-role", NodeTypesOption.KEYWORD),
  SOAPFAULTSTRING(Proparse.SOAPFAULTSTRING, "soap-fault-string", NodeTypesOption.KEYWORD),
  SOAPFAULTSUBCODE(Proparse.SOAPFAULTSUBCODE, "soap-fault-subcode", NodeTypesOption.KEYWORD),
  SOAPHEADER(Proparse.SOAPHEADER, "soap-header", NodeTypesOption.KEYWORD),
  SOAPHEADERENTRYREF(Proparse.SOAPHEADERENTRYREF, "soap-header-entryref", NodeTypesOption.KEYWORD),
  SOAPVERSION(Proparse.SOAPVERSION, "soap-version", NodeTypesOption.KEYWORD),
  SOCKET(Proparse.SOCKET, "socket", NodeTypesOption.KEYWORD),
  SOME(Proparse.SOME, "some", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SORT(Proparse.SORT, "sort", NodeTypesOption.KEYWORD),
  SORTASCENDING(Proparse.SORTASCENDING, "sort-ascending", NodeTypesOption.KEYWORD),
  SORTNUMBER(Proparse.SORTNUMBER, "sort-number", NodeTypesOption.KEYWORD),
  SOURCE(Proparse.SOURCE, "source", NodeTypesOption.KEYWORD),
  SOURCEPROCEDURE(Proparse.SOURCEPROCEDURE, "source-procedure", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SPACE(Proparse.SPACE, "space", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SQL(Proparse.SQL, "sql", NodeTypesOption.KEYWORD),
  SQRT(Proparse.SQRT, "sqrt", NodeTypesOption.KEYWORD),
  SSLSERVERNAME(Proparse.SSLSERVERNAME, "ssl-server-name", NodeTypesOption.KEYWORD),
  STANDALONE(Proparse.STANDALONE, "standalone", NodeTypesOption.KEYWORD),
  START(Proparse.START, "start", NodeTypesOption.KEYWORD),
  STARTBOXSELECTION(Proparse.STARTBOXSELECTION, "start-box-selection", NodeTypesOption.KEYWORD),
  STARTDOCUMENT(Proparse.STARTDOCUMENT, "start-document", NodeTypesOption.KEYWORD),
  STARTELEMENT(Proparse.STARTELEMENT, "start-element", NodeTypesOption.KEYWORD),
  STARTEXTENDBOXSELECTION(Proparse.STARTEXTENDBOXSELECTION, "start-extend-box-selection", NodeTypesOption.KEYWORD),
  STARTING(Proparse.STARTING, "starting", NodeTypesOption.KEYWORD),
  STARTMEMCHECK(Proparse.STARTMEMCHECK, "start-mem-check", NodeTypesOption.KEYWORD),
  STARTMOVE(Proparse.STARTMOVE, "start-move", NodeTypesOption.KEYWORD),
  STARTRESIZE(Proparse.STARTRESIZE, "start-resize", NodeTypesOption.KEYWORD),
  STARTROWRESIZE(Proparse.STARTROWRESIZE, "start-row-resize", NodeTypesOption.KEYWORD),
  STARTSEARCH(Proparse.STARTSEARCH, "start-search", NodeTypesOption.KEYWORD),
  STARTUPPARAMETERS(Proparse.STARTUPPARAMETERS, "startup-parameters", NodeTypesOption.KEYWORD),
  STATEDETAIL(Proparse.STATEDETAIL, "state-detail", NodeTypesOption.KEYWORD),
  STATIC(Proparse.STATIC, "static", NodeTypesOption.KEYWORD),
  STATISTICS(Proparse.STATISTICS, "statistics", NodeTypesOption.KEYWORD),
  STATUS(Proparse.STATUS, "status", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  STATUSAREA(Proparse.STATUSAREA, "status-area", NodeTypesOption.KEYWORD),
  STATUSAREAFONT(Proparse.STATUSAREAFONT, "status-area-font", NodeTypesOption.KEYWORD),
  STATUSAREAMSG(Proparse.STATUSAREAMSG, "status-area-msg", NodeTypesOption.KEYWORD),
  STDCALL(Proparse.STDCALL, "stdcall", NodeTypesOption.KEYWORD),
  STOMPDETECTION(Proparse.STOMPDETECTION, "stomp-detection", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  STOMPFREQUENCY(Proparse.STOMPFREQUENCY, "stomp-frequency", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  STOP(Proparse.STOP, "stop", NodeTypesOption.KEYWORD),
  STOPAFTER(Proparse.STOPAFTER, "stop-after", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  STOPDISPLAY(Proparse.STOPDISPLAY, "stop-display", NodeTypesOption.KEYWORD),
  STOPMEMCHECK(Proparse.STOPMEMCHECK, "stop-mem-check", NodeTypesOption.KEYWORD),
  STOPOBJECT(Proparse.STOPOBJECT, "stop-object", NodeTypesOption.KEYWORD),
  STOPPARSING(Proparse.STOPPARSING, "stop-parsing", NodeTypesOption.KEYWORD),
  STOPPED(Proparse.STOPPED, "stopped", 6, NodeTypesOption.KEYWORD),
  STOREDPROCEDURE(Proparse.STOREDPROCEDURE, "stored-procedure", 11, NodeTypesOption.KEYWORD),
  STREAM(Proparse.STREAM, "stream", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  STREAMHANDLE(Proparse.STREAMHANDLE, "stream-handle", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  STREAMIO(Proparse.STREAMIO, "stream-io", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  STRETCHTOFIT(Proparse.STRETCHTOFIT, "stretch-to-fit", NodeTypesOption.KEYWORD),
  STRICT(Proparse.STRICT, "strict", NodeTypesOption.KEYWORD),
  STRICTENTITYRESOLUTION(Proparse.STRICTENTITYRESOLUTION, "strict-entity-resolution", NodeTypesOption.KEYWORD),
  STRING(Proparse.STRING, "string", NodeTypesOption.KEYWORD),
  STRINGVALUE(Proparse.STRINGVALUE, "string-value", NodeTypesOption.KEYWORD),
  STRINGXREF(Proparse.STRINGXREF, "string-xref", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SUBAVERAGE(Proparse.SUBAVERAGE, "sub-average", 7, NodeTypesOption.KEYWORD),
  SUBCOUNT(Proparse.SUBCOUNT, "sub-count", NodeTypesOption.KEYWORD),
  SUBMAXIMUM(Proparse.SUBMAXIMUM, "sub-maximum", 7, NodeTypesOption.KEYWORD),
  SUBMENU(Proparse.SUBMENU, "sub-menu", NodeTypesOption.KEYWORD),
  SUBMENUHELP(Proparse.SUBMENUHELP, "sub-menu-help", NodeTypesOption.KEYWORD),
  SUBMINIMUM(Proparse.SUBMINIMUM, "sub-minimum", 7, NodeTypesOption.KEYWORD),
  SUBSCRIBE(Proparse.SUBSCRIBE, "subscribe", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SUBSTITUTE(Proparse.SUBSTITUTE, "substitute", 5, NodeTypesOption.KEYWORD),
  SUBSTRING(Proparse.SUBSTRING, "substring", 6, NodeTypesOption.KEYWORD),
  SUBTOTAL(Proparse.SUBTOTAL, "sub-total", NodeTypesOption.KEYWORD),
  SUBTYPE(Proparse.SUBTYPE, "subtype", NodeTypesOption.KEYWORD),
  SUM(Proparse.SUM, "sum", NodeTypesOption.KEYWORD),
  SUMMARY(Proparse.SUMMARY, "summary", NodeTypesOption.KEYWORD),
  SUPER(Proparse.SUPER, "super", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SUPERPROCEDURES(Proparse.SUPERPROCEDURES, "super-procedures", 10, NodeTypesOption.KEYWORD),
  SUPPRESSNAMESPACEPROCESSING(Proparse.SUPPRESSNAMESPACEPROCESSING, "suppress-namespace-processing",
      NodeTypesOption.KEYWORD),
  SUPPRESSWARNINGS(Proparse.SUPPRESSWARNINGS, "suppress-warnings", 10, NodeTypesOption.KEYWORD),
  SUPPRESSWARNINGSLIST(Proparse.SUPPRESSWARNINGSLIST, "suppress-warnings-list", NodeTypesOption.KEYWORD),
  SUSPEND(Proparse.SUSPEND, "suspend", NodeTypesOption.KEYWORD),
  SYMMETRICENCRYPTIONAAD(Proparse.SYMMETRICENCRYPTIONAAD, "symmetric-encryption-aad", NodeTypesOption.KEYWORD),
  SYMMETRICENCRYPTIONALGORITHM(Proparse.SYMMETRICENCRYPTIONALGORITHM, "symmetric-encryption-algorithm",
      NodeTypesOption.KEYWORD),
  SYMMETRICENCRYPTIONIV(Proparse.SYMMETRICENCRYPTIONIV, "symmetric-encryption-iv", NodeTypesOption.KEYWORD),
  SYMMETRICENCRYPTIONKEY(Proparse.SYMMETRICENCRYPTIONKEY, "symmetric-encryption-key", NodeTypesOption.KEYWORD),
  SYMMETRICSUPPORT(Proparse.SYMMETRICSUPPORT, "symmetric-support", NodeTypesOption.KEYWORD),
  SYNCHRONIZE(Proparse.SYNCHRONIZE, "synchronize", NodeTypesOption.KEYWORD),
  SYSTEMALERTBOXES(Proparse.SYSTEMALERTBOXES, "system-alert-boxes", 12, NodeTypesOption.KEYWORD),
  SYSTEMDIALOG(Proparse.SYSTEMDIALOG, "system-dialog", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SYSTEMHELP(Proparse.SYSTEMHELP, "system-help", NodeTypesOption.KEYWORD),
  SYSTEMID(Proparse.SYSTEMID, "system-id", NodeTypesOption.KEYWORD),

  // T
  TAB(Proparse.TAB, "tab", NodeTypesOption.KEYWORD),
  TABLE(Proparse.TABLE, "table", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TABLECRCLIST(Proparse.TABLECRCLIST, "table-crc-list", NodeTypesOption.KEYWORD),
  TABLEHANDLE(Proparse.TABLEHANDLE, "table-handle", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TABLELIST(Proparse.TABLELIST, "table-list", NodeTypesOption.KEYWORD),
  TABLENUMBER(Proparse.TABLENUMBER, "table-number", 9, NodeTypesOption.KEYWORD),
  TABLESCAN(Proparse.TABLESCAN, "table-scan", NodeTypesOption.KEYWORD),
  TABPOSITION(Proparse.TABPOSITION, "tab-position", NodeTypesOption.KEYWORD),
  TABSTOP(Proparse.TABSTOP, "tab-stop", NodeTypesOption.KEYWORD),
  TARGET(Proparse.TARGET, "target", NodeTypesOption.KEYWORD),
  TARGETPROCEDURE(Proparse.TARGETPROCEDURE, "target-procedure", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TEMPDIRECTORY(Proparse.TEMPDIRECTORY, "temp-directory", 8, NodeTypesOption.KEYWORD),
  TEMPTABLE(Proparse.TEMPTABLE, "temp-table", NodeTypesOption.KEYWORD),
  TEMPTABLEPREPARE(Proparse.TEMPTABLEPREPARE, "temp-table-prepare", 17, NodeTypesOption.KEYWORD),
  TENANT(Proparse.TENANT, "tenant", NodeTypesOption.KEYWORD),
  TENANTID(Proparse.TENANTID, "tenant-id", NodeTypesOption.KEYWORD),
  TENANTNAME(Proparse.TENANTNAME, "tenant-name", NodeTypesOption.KEYWORD),
  TENANTNAMETOID(Proparse.TENANTNAMETOID, "tenant-name-to-id", NodeTypesOption.KEYWORD),
  TENANTWHERE(Proparse.TENANTWHERE, "tenant-where", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TERMINATE(Proparse.TERMINATE, "terminate", NodeTypesOption.KEYWORD),
  TEXT(Proparse.TEXT, "text", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TEXTCURSOR(Proparse.TEXTCURSOR, "text-cursor", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TEXTSEGGROWTH(Proparse.TEXTSEGGROWTH, "text-seg-growth", 8, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TEXTSELECTED(Proparse.TEXTSELECTED, "text-selected", NodeTypesOption.KEYWORD),
  THEN(Proparse.THEN, "then", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  THISOBJECT(Proparse.THISOBJECT, "this-object", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  THISPROCEDURE(Proparse.THISPROCEDURE, "this-procedure", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  THREADSAFE(Proparse.THREADSAFE, "thread-safe", NodeTypesOption.KEYWORD),
  THREED(Proparse.THREED, "three-d", NodeTypesOption.KEYWORD),
  THROW(Proparse.THROW, "throw", NodeTypesOption.KEYWORD),
  TICMARKS(Proparse.TICMARKS, "tic-marks", NodeTypesOption.KEYWORD),
  TIME(Proparse.TIME, "time", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TIMESOURCE(Proparse.TIMESOURCE, "time-source", NodeTypesOption.KEYWORD),
  TIMEZONE(Proparse.TIMEZONE, "timezone", NodeTypesOption.KEYWORD),
  TITLE(Proparse.TITLE, "title", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TITLEBGCOLOR(Proparse.TITLEBGCOLOR, "title-bgcolor", 9, NodeTypesOption.KEYWORD),
  TITLEDCOLOR(Proparse.TITLEDCOLOR, "title-dcolor", 8, NodeTypesOption.KEYWORD),
  TITLEFGCOLOR(Proparse.TITLEFGCOLOR, "title-fgcolor", 9, NodeTypesOption.KEYWORD),
  TITLEFONT(Proparse.TITLEFONT, "title-font", 8, NodeTypesOption.KEYWORD),
  TO(Proparse.TO, "to", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TODAY(Proparse.TODAY, "today", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TOGGLEBOX(Proparse.TOGGLEBOX, "toggle-box", NodeTypesOption.KEYWORD),
  TOOLTIP(Proparse.TOOLTIP, "tooltip", NodeTypesOption.KEYWORD),
  TOOLTIPS(Proparse.TOOLTIPS, "tooltips", NodeTypesOption.KEYWORD),
  TOP(Proparse.TOP, "top", NodeTypesOption.KEYWORD),
  TOPCOLUMN(Proparse.TOPCOLUMN, "top-column", NodeTypesOption.KEYWORD),
  TOPIC(Proparse.TOPIC, "topic", NodeTypesOption.KEYWORD),
  TOPNAVQUERY(Proparse.TOPNAVQUERY, "top-nav-query", NodeTypesOption.KEYWORD),
  TOPONLY(Proparse.TOPONLY, "top-only", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TOROWID(Proparse.TOROWID, "to-rowid", NodeTypesOption.KEYWORD),
  TOTAL(Proparse.TOTAL, "total", NodeTypesOption.KEYWORD),
  TRACEFILTER(Proparse.TRACEFILTER, "trace-filter", NodeTypesOption.KEYWORD),
  TRACING(Proparse.TRACING, "tracing", NodeTypesOption.KEYWORD),
  TRACKINGCHANGES(Proparse.TRACKINGCHANGES, "tracking-changes", NodeTypesOption.KEYWORD),
  TRAILING(Proparse.TRAILING, "trailing", NodeTypesOption.KEYWORD),
  TRANSACTIONMODE(Proparse.TRANSACTIONMODE, "transaction-mode", NodeTypesOption.KEYWORD),
  TRANSINITPROCEDURE(Proparse.TRANSINITPROCEDURE, "trans-init-procedure", 15, NodeTypesOption.KEYWORD),
  TRANSPARENT(Proparse.TRANSPARENT, "transparent", 8, NodeTypesOption.KEYWORD),
  TRIGGER(Proparse.TRIGGER, "trigger", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TRIGGERS(Proparse.TRIGGERS, "triggers", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TRIM(Proparse.TRIM, "trim", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TRUE(Proparse.TRUE, "true", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TRUNCATE(Proparse.TRUNCATE, "truncate", 5, NodeTypesOption.KEYWORD),
  TTCODEPAGE(Proparse.TTCODEPAGE, "ttcodepage", NodeTypesOption.KEYWORD),
  TYPE(Proparse.TYPE, "type", NodeTypesOption.KEYWORD),
  TYPEOF(Proparse.TYPEOF, "type-of", NodeTypesOption.KEYWORD),

  // U
  UNBOX(Proparse.UNBOX, "unbox", NodeTypesOption.KEYWORD),
  UNBUFFERED(Proparse.UNBUFFERED, "unbuffered", 6, NodeTypesOption.KEYWORD),
  UNDERLINE(Proparse.UNDERLINE, "underline", 6, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  UNDO(Proparse.UNDO, "undo", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  UNDOTHROWSCOPE(Proparse.UNDOTHROWSCOPE, "undo-throw-scope", NodeTypesOption.KEYWORD),
  UNFORMATTED(Proparse.UNFORMATTED, "unformatted", 6, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  UNION(Proparse.UNION, "union", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  UNIQUE(Proparse.UNIQUE, "unique", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  UNIQUEID(Proparse.UNIQUEID, "unique-id", NodeTypesOption.KEYWORD),
  UNIQUEMATCH(Proparse.UNIQUEMATCH, "unique-match", NodeTypesOption.KEYWORD),
  UNIX(Proparse.UNIX, "unix", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  UNIXEND(Proparse.UNIXEND, "unix-end", NodeTypesOption.KEYWORD),
  UNLESSHIDDEN(Proparse.UNLESSHIDDEN, "unless-hidden", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  UNLOAD(Proparse.UNLOAD, "unload", NodeTypesOption.KEYWORD),
  UNSIGNEDBYTE(Proparse.UNSIGNEDBYTE, "unsigned-byte", NodeTypesOption.KEYWORD),
  UNSIGNEDINT64(Proparse.UNSIGNEDINT64, "unsigned-int64", NodeTypesOption.KEYWORD),
  UNSIGNEDLONG(Proparse.UNSIGNEDLONG, "unsigned-long", NodeTypesOption.KEYWORD),
  UNSIGNEDSHORT(Proparse.UNSIGNEDSHORT, "unsigned-short", NodeTypesOption.KEYWORD),
  UNSUBSCRIBE(Proparse.UNSUBSCRIBE, "unsubscribe", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  UP(Proparse.UP, "up", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  UPDATE(Proparse.UPDATE, "update", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  UPDATEATTRIBUTE(Proparse.UPDATEATTRIBUTE, "update-attribute", NodeTypesOption.KEYWORD),
  URL(Proparse.URL, "url", NodeTypesOption.KEYWORD),
  URLDECODE(Proparse.URLDECODE, "url-decode", NodeTypesOption.KEYWORD),
  URLENCODE(Proparse.URLENCODE, "url-encode", NodeTypesOption.KEYWORD),
  URLPASSWORD(Proparse.URLPASSWORD, "url-password", NodeTypesOption.KEYWORD),
  URLUSERID(Proparse.URLUSERID, "url-userid", NodeTypesOption.KEYWORD),
  USE(Proparse.USE, "use", NodeTypesOption.KEYWORD),
  USEDICTEXPS(Proparse.USEDICTEXPS, "use-dict-exps", 7, NodeTypesOption.KEYWORD),
  USEFILENAME(Proparse.USEFILENAME, "use-filename", NodeTypesOption.KEYWORD),
  USEINDEX(Proparse.USEINDEX, "use-index", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  USER(Proparse.USER, "user", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  USERDATA(Proparse.USERDATA, "user-data", NodeTypesOption.KEYWORD),
  USEREVVIDEO(Proparse.USEREVVIDEO, "use-revvideo", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  USETEXT(Proparse.USETEXT, "use-text", NodeTypesOption.KEYWORD),
  USEUNDERLINE(Proparse.USEUNDERLINE, "use-underline", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  USEWIDGETPOOL(Proparse.USEWIDGETPOOL, "use-widget-pool", NodeTypesOption.KEYWORD),
  USING(Proparse.USING, "using", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  UTCOFFSET(Proparse.UTCOFFSET, "utc-offset", NodeTypesOption.KEYWORD),

  // V
  V6DISPLAY(Proparse.V6DISPLAY, "v6display", NodeTypesOption.KEYWORD),
  V6FRAME(Proparse.V6FRAME, "v6frame", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  VALIDATE(Proparse.VALIDATE, "validate", NodeTypesOption.KEYWORD),
  VALIDATEDOMAINACCESSCODE(Proparse.VALIDATEDOMAINACCESSCODE, "validate-domain-access-code", NodeTypesOption.KEYWORD),
  VALIDATEEXPRESSION(Proparse.VALIDATEEXPRESSION, "validate-expression", 18, NodeTypesOption.KEYWORD),
  VALIDATEMESSAGE(Proparse.VALIDATEMESSAGE, "validate-message", NodeTypesOption.KEYWORD),
  VALIDATESEAL(Proparse.VALIDATESEAL, "validate-seal", NodeTypesOption.KEYWORD),
  VALIDATEXML(Proparse.VALIDATEXML, "validate-xml", NodeTypesOption.KEYWORD),
  VALIDATIONENABLED(Proparse.VALIDATIONENABLED, "validation-enabled", NodeTypesOption.KEYWORD),
  VALIDEVENT(Proparse.VALIDEVENT, "valid-event", NodeTypesOption.KEYWORD),
  VALIDHANDLE(Proparse.VALIDHANDLE, "valid-handle", NodeTypesOption.KEYWORD),
  VALIDOBJECT(Proparse.VALIDOBJECT, "valid-object", NodeTypesOption.KEYWORD),
  VALUE(Proparse.VALUE, "value", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  VALUECHANGED(Proparse.VALUECHANGED, "value-changed", NodeTypesOption.KEYWORD),
  VALUES(Proparse.VALUES, "values", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  VARIABLE(Proparse.VARIABLE, "variable", 4, NodeTypesOption.KEYWORD),
  VERBOSE(Proparse.VERBOSE, "verbose", 4, NodeTypesOption.KEYWORD),
  VERSION(Proparse.VERSION, "version", NodeTypesOption.KEYWORD),
  VERTICAL(Proparse.VERTICAL, "vertical", 4, NodeTypesOption.KEYWORD),
  VIEW(Proparse.VIEW, "view", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  VIEWAS(Proparse.VIEWAS, "view-as", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  VIEWFIRSTCOLUMNONREOPEN(Proparse.VIEWFIRSTCOLUMNONREOPEN, "view-first-column-on-reopen", NodeTypesOption.KEYWORD),
  VIRTUALHEIGHT(Proparse.VIRTUALHEIGHT, "virtual-height", NodeTypesOption.KEYWORD),
  VIRTUALHEIGHTCHARS(Proparse.VIRTUALHEIGHTCHARS, "virtual-height-chars", 16, NodeTypesOption.KEYWORD),
  VIRTUALHEIGHTPIXELS(Proparse.VIRTUALHEIGHTPIXELS, "virtual-height-pixels", 16, NodeTypesOption.KEYWORD),
  VIRTUALWIDTH(Proparse.VIRTUALWIDTH, "virtual-width", NodeTypesOption.KEYWORD),
  VIRTUALWIDTHCHARS(Proparse.VIRTUALWIDTHCHARS, "virtual-width-chars", 15, NodeTypesOption.KEYWORD),
  VIRTUALWIDTHPIXELS(Proparse.VIRTUALWIDTHPIXELS, "virtual-width-pixels", 15, NodeTypesOption.KEYWORD),
  VISIBLE(Proparse.VISIBLE, "visible", NodeTypesOption.KEYWORD),
  VMS(Proparse.VMS, "vms", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  VOID(Proparse.VOID, "void", NodeTypesOption.KEYWORD),

  // W
  WAIT(Proparse.WAIT, "wait", NodeTypesOption.KEYWORD),
  WAITFOR(Proparse.WAITFOR, "wait-for", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WARNING(Proparse.WARNING, "warning", NodeTypesOption.KEYWORD),
  WCADMINAPP(Proparse.WCADMINAPP, "wc-admin-app", NodeTypesOption.KEYWORD),
  WEBCONTEXT(Proparse.WEBCONTEXT, "web-context", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WEBNOTIFY(Proparse.WEBNOTIFY, "web-notify", NodeTypesOption.KEYWORD),
  WEEKDAY(Proparse.WEEKDAY, "weekday", NodeTypesOption.KEYWORD),
  WHEN(Proparse.WHEN, "when", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WHERE(Proparse.WHERE, "where", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WHERESTRING(Proparse.WHERESTRING, "where-string", NodeTypesOption.KEYWORD),
  WHILE(Proparse.WHILE, "while", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WIDGET(Proparse.WIDGET, "widget", NodeTypesOption.KEYWORD),
  WIDGETENTER(Proparse.WIDGETENTER, "widget-enter", 8, NodeTypesOption.KEYWORD),
  WIDGETHANDLE(Proparse.WIDGETHANDLE, "widget-handle", 8, NodeTypesOption.KEYWORD),
  WIDGETID(Proparse.WIDGETID, "widget-id", NodeTypesOption.KEYWORD),
  WIDGETLEAVE(Proparse.WIDGETLEAVE, "widget-leave", 8, NodeTypesOption.KEYWORD),
  WIDGETPOOL(Proparse.WIDGETPOOL, "widget-pool", NodeTypesOption.KEYWORD),
  WIDTH(Proparse.WIDTH, "width", NodeTypesOption.KEYWORD),
  WIDTHCHARS(Proparse.WIDTHCHARS, "width-chars", 7, NodeTypesOption.KEYWORD),
  WIDTHPIXELS(Proparse.WIDTHPIXELS, "width-pixels", 7, NodeTypesOption.KEYWORD),
  WINDOW(Proparse.WINDOW, "window", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WINDOWCLOSE(Proparse.WINDOWCLOSE, "window-close", NodeTypesOption.KEYWORD),
  WINDOWDELAYEDMINIMIZE(Proparse.WINDOWDELAYEDMINIMIZE, "window-delayed-minimize", 18, NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  WINDOWMAXIMIZED(Proparse.WINDOWMAXIMIZED, "window-maximized", 12, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WINDOWMINIMIZED(Proparse.WINDOWMINIMIZED, "window-minimized", 12, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WINDOWNAME(Proparse.WINDOWNAME, "window-name", NodeTypesOption.KEYWORD),
  WINDOWNORMAL(Proparse.WINDOWNORMAL, "window-normal", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WINDOWRESIZED(Proparse.WINDOWRESIZED, "window-resized", NodeTypesOption.KEYWORD),
  WINDOWRESTORED(Proparse.WINDOWRESTORED, "window-restored", NodeTypesOption.KEYWORD),
  WINDOWSTATE(Proparse.WINDOWSTATE, "window-state", 10, NodeTypesOption.KEYWORD),
  WINDOWSYSTEM(Proparse.WINDOWSYSTEM, "window-system", 10, NodeTypesOption.KEYWORD),
  WITH(Proparse.WITH, "with", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WORDINDEX(Proparse.WORDINDEX, "word-index", NodeTypesOption.KEYWORD),
  WORDWRAP(Proparse.WORDWRAP, "word-wrap", NodeTypesOption.KEYWORD),
  WORKAREAHEIGHTPIXELS(Proparse.WORKAREAHEIGHTPIXELS, "work-area-height-pixels", 18, NodeTypesOption.KEYWORD),
  WORKAREAWIDTHPIXELS(Proparse.WORKAREAWIDTHPIXELS, "work-area-width-pixels", 17, NodeTypesOption.KEYWORD),
  WORKAREAX(Proparse.WORKAREAX, "work-area-x", NodeTypesOption.KEYWORD),
  WORKAREAY(Proparse.WORKAREAY, "work-area-y", NodeTypesOption.KEYWORD),
  WRITE(Proparse.WRITE, "write", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WRITECDATA(Proparse.WRITECDATA, "write-cdata", NodeTypesOption.KEYWORD),
  WRITECHARACTERS(Proparse.WRITECHARACTERS, "write-characters", NodeTypesOption.KEYWORD),
  WRITECOMMENT(Proparse.WRITECOMMENT, "write-comment", NodeTypesOption.KEYWORD),
  WRITEDATA(Proparse.WRITEDATA, "write-data", NodeTypesOption.KEYWORD),
  WRITEDATAELEMENT(Proparse.WRITEDATAELEMENT, "write-data-element", NodeTypesOption.KEYWORD),
  WRITEEMPTYELEMENT(Proparse.WRITEEMPTYELEMENT, "write-empty-element", NodeTypesOption.KEYWORD),
  WRITEENTITYREF(Proparse.WRITEENTITYREF, "write-entity-ref", NodeTypesOption.KEYWORD),
  WRITEEXTERNALDTD(Proparse.WRITEEXTERNALDTD, "write-external-dtd", NodeTypesOption.KEYWORD),
  WRITEFRAGMENT(Proparse.WRITEFRAGMENT, "write-fragment", NodeTypesOption.KEYWORD),
  WRITEJSON(Proparse.WRITEJSON, "write-json", NodeTypesOption.KEYWORD),
  WRITEMESSAGE(Proparse.WRITEMESSAGE, "write-message", NodeTypesOption.KEYWORD),
  WRITEPROCESSINGINSTRUCTION(Proparse.WRITEPROCESSINGINSTRUCTION, "write-processing-instruction",
      NodeTypesOption.KEYWORD),
  WRITESTATUS(Proparse.WRITESTATUS, "write-status", NodeTypesOption.KEYWORD),
  WRITEXML(Proparse.WRITEXML, "write-xml", NodeTypesOption.KEYWORD),
  WRITEXMLSCHEMA(Proparse.WRITEXMLSCHEMA, "write-xmlschema", NodeTypesOption.KEYWORD),

  // X
  X(Proparse.X, "x", NodeTypesOption.KEYWORD),
  XCODE(Proparse.XCODE, "xcode", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  XCODESESSIONKEY(Proparse.XCODESESSIONKEY, "xcode-session-key", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  XDOCUMENT(Proparse.XDOCUMENT, "x-document", NodeTypesOption.KEYWORD),
  XMLDATATYPE(Proparse.XMLDATATYPE, "xml-data-type", NodeTypesOption.KEYWORD),
  XMLENTITYEXPANSIONLIMIT(Proparse.XMLENTITYEXPANSIONLIMIT, "xml-entity-expansion-limit", NodeTypesOption.KEYWORD),
  XMLNODENAME(Proparse.XMLNODENAME, "xml-node-name", NodeTypesOption.KEYWORD),
  XMLNODETYPE(Proparse.XMLNODETYPE, "xml-node-type", NodeTypesOption.KEYWORD),
  XMLSCHEMAPATH(Proparse.XMLSCHEMAPATH, "xml-schema-path", 14, NodeTypesOption.KEYWORD),
  XMLSTRICTENTITYRESOLUTION(Proparse.XMLSTRICTENTITYRESOLUTION, "xml-strict-entity-resolution",
      NodeTypesOption.KEYWORD),
  XMLSUPPRESSNAMESPACEPROCESSING(Proparse.XMLSUPPRESSNAMESPACEPROCESSING, "xml-suppress-namespace-processing",
      NodeTypesOption.KEYWORD),
  XNODEREF(Proparse.XNODEREF, "x-noderef", NodeTypesOption.KEYWORD),
  XOF(Proparse.XOF, "x-of", NodeTypesOption.KEYWORD),
  XOR(Proparse.XOR, "xor", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  XREF(Proparse.XREF, "xref", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  XREFXML(Proparse.XREFXML, "xref-xml", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),

  // Y
  Y(Proparse.Y, "y", NodeTypesOption.KEYWORD),
  YEAR(Proparse.YEAR, "year", NodeTypesOption.KEYWORD),
  YEAROFFSET(Proparse.YEAROFFSET, "year-offset", NodeTypesOption.KEYWORD),
  YES(Proparse.YES, "yes", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  YESNO(Proparse.YESNO, "yes-no", NodeTypesOption.KEYWORD),
  YESNOCANCEL(Proparse.YESNOCANCEL, "yes-no-cancel", NodeTypesOption.KEYWORD),
  YOF(Proparse.YOF, "y-of", NodeTypesOption.KEYWORD);

  private static final EnumSet<ABLNodeType> NO_ARGUMENT_FUNCTIONS = EnumSet.of( //
      AACONTROL, //
      AAPCONTROL, //
      AASERIAL, //
      CURRENTLANGUAGE, //
      CURSOR, //
      DATASERVERS, //
      DBNAME, //
      ETIME, //
      FRAMECOL, //
      FRAMEDB, //
      FRAMEDOWN, //
      FRAMEFIELD, //
      FRAMEFILE, //
      FRAMEINDEX, //
      FRAMELINE, //
      FRAMENAME, //
      FRAMEROW, //
      FRAMEVALUE, //
      GATEWAYS, //
      GENERATEPBESALT, //
      GENERATERANDOMKEY, //
      GENERATEUUID, //
      GETCODEPAGES, //
      GETDBCLIENT, //
      GOPENDING, //
      GUID, //
      ISATTRSPACE, //
      LASTKEY, //
      LINECOUNTER, //
      MACHINECLASS, //
      MESSAGELINES, //
      MTIME, //
      // NOT ENTERED ???
      NOW, //
      NUMALIASES, //
      NUMDBS, //
      OPSYS, //
      OSDRIVES, //
      OSERROR, //
      PAGENUMBER, //
      PAGESIZE, //
      PROCESSARCHITECTURE, //
      PROCHANDLE, //
      PROCSTATUS, //
      PROGRESS, //
      PROMSGS, //
      PROPATH, //
      PROVERSION, //
      RETRY, //
      RETURNVALUE, //
      SCREENLINES, //
      SUPER, //
      TERMINAL, //
      TIME, //
      TIMEZONE, //
      TODAY, //
      TRANSACTION, //
      USER, //
      USERID //
  );

  private static final EnumSet<ABLNodeType> REGULAR_FUNCTIONS = EnumSet.of( //
      AACBIT, //
      AAMSG, //
      ABSOLUTE, //
      ADDINTERVAL, //
      ALIAS, //
      AMBIGUOUS, //
      ASC, //
      ASCENDING, //
      AVAILABLE, //
      AVG, //
      BASE64DECODE, //
      BASE64ENCODE, //
      BOX, //
      BUFFERGROUPID, //
      BUFFERGROUPNAME, //
      BUFFERPARTITIONID, //
      BUFFERTENANTID, //
      BUFFERTENANTNAME, //
      CANDO, //
      CANQUERY, //
      CANSET, //
      CAPS, //
      CHR, //
      CODEPAGECONVERT, //
      COLLATE, //
      COMPARE, //
      COMPARES, //
      CONNECTED, //
      COUNT, //
      COUNTOF, //
      CURRENTCHANGED, //
      CURRENTRESULTROW, //
      DATASOURCEMODIFIED, //
      DATE, //
      DATETIME, //
      DATETIMETZ, //
      DAY, //
      DBCODEPAGE, //
      DBCOLLATION, //
      DBPARAM, //
      DBREMOTEHOST, //
      DBRESTRICTIONS, //
      DBTASKID, //
      DBTYPE, //
      DBVERSION, //
      DECIMAL, //
      DECRYPT, //
      DYNAMICCAST, //
      DYNAMICENUM, //
      DYNAMICNEXTVALUE, //
      DYNAMICPROPERTY, //
      ENCODE, //
      ENCRYPT, //
      ENTRY, //
      ERROR, //
      ETIME, //
      EXP, //
      EXTENT, //
      FILL, //
      FIRST, //
      FIRSTOF, //
      GENERATEPBEKEY, //
      GETBITS, //
      GETBYTE, //
      GETBYTEORDER, //
      GETBYTES, //
      GETCODEPAGE, //
      GETCODEPAGES, //
      GETCOLLATIONS, //
      GETDBCLIENT, //
      GETDOUBLE, //
      GETEFFECTIVETENANTID, //
      GETEFFECTIVETENANTNAME, //
      GETFLOAT, //
      GETINT64, //
      GETLICENSE, //
      GETLONG, //
      GETPOINTERVALUE, //
      GETSHORT, //
      GETSIZE, //
      GETSTRING, //
      GETUNSIGNEDLONG, //
      GETUNSIGNEDSHORT, //
      HANDLE, //
      HEXDECODE, //
      HEXENCODE, //
      INDEX, //
      INT64, //
      INTEGER, //
      INTERVAL, //
      ISCODEPAGEFIXED, //
      ISCOLUMNCODEPAGE, //
      ISDBMULTITENANT, //
      ISLEADBYTE, //
      ISODATE, //
      KBLABEL, //
      KEYCODE, //
      KEYFUNCTION, //
      KEYLABEL, //
      KEYWORD, //
      KEYWORDALL, //
      LAST, //
      LASTOF, //
      LC, //
      LDBNAME, //
      LEFTTRIM, //
      LENGTH, //
      LIBRARY, //
      LISTEVENTS, //
      LISTQUERYATTRS, //
      LISTSETATTRS, //
      LISTWIDGETS, //
      LOADPICTURE, //
      LOCKED, //
      LOG, //
      LOGICAL, //
      LOOKUP, //
      MAXIMUM, //
      MD5DIGEST, //
      MEMBER, //
      MESSAGEDIGEST, //
      MINIMUM, //
      MONTH, //
      MTIME, //
      NEW, //
      NORMALIZE, //
      NUMENTRIES, //
      NUMRESULTS, //
      OSGETENV, //
      PDBNAME, //
      PROGRAMNAME, //
      PROVERSION, //
      QUERYOFFEND, //
      QUOTER, //
      RANDOM, //
      RAW, //
      RECID, //
      RECORDLENGTH, //
      REJECTED, //
      REPLACE, //
      RGBVALUE, //
      RIGHTTRIM, //
      RINDEX, //
      ROUND, //
      ROWID, //
      ROWSTATE, //
      SDBNAME, //
      SEARCH, //
      SETDBCLIENT, //
      SETEFFECTIVETENANT, //
      SETUSERID, //
      SHA1DIGEST, //
      SQRT, //
      SSLSERVERNAME, //
      STRING, //
      SUBSTITUTE, //
      SUBSTRING, //
      SUM, //
      TENANTID, //
      TENANTNAME, //
      TENANTNAMETOID, //
      TIMEZONE, //
      TOROWID, //
      TRIM, //
      TRUNCATE, //
      UNBOX, //
      USER, //
      USERID, //
      VALIDEVENT, //
      VALIDHANDLE, //
      VALIDOBJECT, //
      WEEKDAY, //
      WIDGETHANDLE, //
      YEAR //
  );

  private static final EnumSet<ABLNodeType> RECORD_FUNCTIONS = EnumSet.of( //
      AMBIGUOUS, //
      AVAILABLE, //
      CURRENTCHANGED, //
      DATASOURCEMODIFIED, //
      ERROR, //
      LOCKED, //
      NEW, //
      RECID, //
      RECORDLENGTH, //
      REJECTED, //
      ROWID, //
      ROWSTATE //
  );

  private static final EnumSet<ABLNodeType> OPTIONAL_ARG_FUNCTIONS = EnumSet.of( //
      AUDITENABLED, //
      GETDBCLIENT, //
      GETEFFECTIVETENANTID, //
      GETEFFECTIVETENANTNAME, //
      GUID, //
      TENANTID, //
      TENANTNAME //
  );

  private static final EnumSet<ABLNodeType> SYSTEM_HANDLES = EnumSet.of( //
      AAMEMORY, //
      ACTIVEFORM, //
      ACTIVEWINDOW, //
      AUDITCONTROL, //
      AUDITPOLICY, //
      CLIPBOARD, //
      CODEBASELOCATOR, //
      COLORTABLE, //
      COMPILER, //
      COMSELF, //
      CURRENTWINDOW, //
      DSLOGMANAGER, //
      DEBUGGER, //
      DEFAULTWINDOW, //
      ERRORSTATUS, //
      FILEINFORMATION, //
      FOCUS, //
      FONTTABLE, //
      LASTEVENT, //
      LOGMANAGER, //
      MOUSE, //
      PROFILER, //
      RCODEINFORMATION, //
      SECURITYPOLICY, //
      SELF, //
      SESSION, //
      SOURCEPROCEDURE, //
      SUPER, //
      TARGETPROCEDURE, //
      TEXTCURSOR, //
      THISOBJECT, //
      THISPROCEDURE, //
      WEBCONTEXT //
  );

  private static final EnumSet<ABLNodeType> DATATYPE_IN_VARIABLE = EnumSet.of( //
      CHARACTER, //
      COMHANDLE, //
      DATE, //
      DATETIME, //
      DATETIMETZ, //
      DECIMAL, //
      HANDLE, //
      INTEGER, //
      INT64, //
      LOGICAL, //
      LONGCHAR, //
      MEMPTR, //
      RAW, //
      RECID, //
      ROWID, //
      WIDGETHANDLE, //
      IN, // Works for INTEGER
      LOG, // Works for LOGICAL
      ROW, // Works for ROWID
      WIDGET, // Works for WIDGETHANDLE
      BLOB, //
      CLOB, //
      BYTE, //
      DOUBLE, //
      FLOAT, //
      LONG, //
      SHORT, //
      UNSIGNEDBYTE, //
      UNSIGNEDSHORT, //
      UNSIGNEDINTEGER, //
      VOID //
  );

  private static final String ERR_INIT = "Error while initializing typeMap - Duplicate key ";
  private static Map<String, ABLNodeType> literalsMap = new HashMap<>();
  private static Map<Integer, ABLNodeType> typeMap = new HashMap<>();

  // Private attributes
  private int typeNum;
  private String text;
  private EnumSet<NodeTypesOption> options;

  // Keywords can have up to two alternate syntax
  private String alt1;
  private String alt2;

  // And can be abbreviated too
  private int abbrMain;
  private int abbrAlt1;
  private int abbrAlt2;

  private ABLNodeType(int type) {
    this(type, "");
  }

  private ABLNodeType(int type, String text) {
    this(type, text, text.length());
  }

  private ABLNodeType(int type, String text, int minAbbrev) {
    this.typeNum = type;
    this.text = text;
    this.abbrMain = minAbbrev;
    this.options = EnumSet.noneOf(NodeTypesOption.class);
  }

  private ABLNodeType(int type, NodeTypesOption opt, NodeTypesOption... options) {
    this(type, "");
    this.options = EnumSet.of(opt, options);
  }

  private ABLNodeType(int type, String text, NodeTypesOption opt, NodeTypesOption... options) {
    this.typeNum = type;
    this.text = text;
    this.abbrMain = text.length();
    this.options = EnumSet.of(opt, options);
  }

  private ABLNodeType(int type, String text, int minabbr, NodeTypesOption opt, NodeTypesOption... options) {
    this.typeNum = type;
    this.text = text;
    this.abbrMain = minabbr;
    this.options = EnumSet.of(opt, options);
  }

  private ABLNodeType(int type, String text, String extraLiteral, NodeTypesOption opt, NodeTypesOption... options) {
    this(type, text, text.length(), extraLiteral, opt, options);
  }

  private ABLNodeType(int type, String text, int minabbr, String alt1, NodeTypesOption opt,
      NodeTypesOption... options) {
    this(type, text, minabbr, opt, options);
    this.alt1 = alt1;
    this.abbrAlt1 = alt1.length();
  }

  private ABLNodeType(int type, String text, int minAbbr, String alt1, int minAbbr1, NodeTypesOption opt,
      NodeTypesOption... options) {
    this(type, text, minAbbr, opt, options);
    this.alt1 = alt1;
    this.abbrAlt1 = minAbbr1;
  }

  private ABLNodeType(int type, String fullText, int minAbbr, String alt1, String alt2, NodeTypesOption opt,
      NodeTypesOption... options) {
    this(type, fullText, minAbbr, alt1, opt, options);
    this.alt2 = alt2;
    this.abbrAlt2 = alt2.length();
  }

  public int getType() {
    return typeNum;
  }

  /**
   * @return Associated text. Can be null if not a keyword
   */
  public String getText() {
    return text;
  }

  /**
   * @return First alternate keyword
   */
  public String getAlternate() {
    return alt1;
  }

  /**
   * @return Second alternate keyword
   */
  public String getAlternate2() {
    return alt2;
  }

  /**
   * @return True if node type is a keyword
   */
  public boolean isKeyword() {
    return options.contains(NodeTypesOption.KEYWORD);
  }

  public boolean isPreprocessor() {
    return options.contains(NodeTypesOption.PREPROCESSOR);
  }

  public boolean isSymbol() {
    return options.contains(NodeTypesOption.SYMBOL);
  }

  /**
   * @return True if node type is a keyword but can't be used as a variable name or field name among other things
   */
  public boolean isReservedKeyword() {
    return options.contains(NodeTypesOption.KEYWORD) && options.contains(NodeTypesOption.RESERVED);
  }

  /**
   * @return True if node type is a keyword and can be used as a variable name or field name among other things
   */
  public boolean isUnreservedKeywordType() {
    return options.contains(NodeTypesOption.KEYWORD) && !options.contains(NodeTypesOption.RESERVED);
  }

  public boolean isSystemHandle() {
    return SYSTEM_HANDLES.contains(this);
  }

  public boolean isValidDatatype() {
    return DATATYPE_IN_VARIABLE.contains(this);
  }

  public boolean isNoArgFunc() {
    return NO_ARGUMENT_FUNCTIONS.contains(this);
  }

  public boolean isOptionalArgFunction() {
    return OPTIONAL_ARG_FUNCTIONS.contains(this);
  }

  public boolean isRegularFunc() {
    return REGULAR_FUNCTIONS.contains(this);
  }

  public boolean isRecordFunc() {
    return RECORD_FUNCTIONS.contains(this);
  }

  public boolean isAbbreviated(String txt) {
    if (Strings.isNullOrEmpty(txt) || !isKeyword())
      return false;
    String lowText = txt.toLowerCase();
    if (text.startsWith(lowText)) {
      return text.length() > lowText.length();
    } else if ((alt1 != null) && alt1.startsWith(lowText)) {
      return alt1.length() > lowText.length();
    } else if ((alt2 != null) && alt2.startsWith(lowText)) {
      return alt2.length() > lowText.length();
    }
    return false;
  }

  static {
    for (ABLNodeType e : ABLNodeType.values()) {
      // No duplicates allowed in definition
      if (typeMap.put(e.typeNum, e) != null)
        throw new IllegalStateException(ERR_INIT + e.typeNum);

      if (e.options.contains(NodeTypesOption.KEYWORD)) {
        // Full-text map is only filled with keywords
        for (int zz = e.abbrMain; zz <= e.text.length(); zz++) {
          if (literalsMap.put(e.text.substring(0, zz).toLowerCase(), e) != null)
            throw new IllegalStateException(ERR_INIT + e.text.substring(0, zz));
        }
        if (e.alt1 != null) {
          for (int zz = e.abbrAlt1; zz <= e.alt1.length(); zz++) {
            if (literalsMap.put(e.alt1.substring(0, zz), e) != null)
              throw new IllegalStateException(ERR_INIT + e.alt1.substring(0, zz));
          }
        }
        if (e.alt2 != null) {
          for (int zz = e.abbrAlt2; zz <= e.alt2.length(); zz++) {
            if (literalsMap.put(e.alt2.substring(0, zz), e) != null)
              throw new IllegalStateException(ERR_INIT + e.alt2.substring(0, zz));
          }
        }
      }
    }
  }

  public static ABLNodeType getNodeType(int type) {
    ABLNodeType nodeType = typeMap.get(type);
    return nodeType == null ? INVALID_NODE : nodeType;
  }

  static boolean isValidType(int type) {
    return typeMap.keySet().contains(type);
  }

  /**
   * Returns uppercase of the type info record's full text. Returns null if there's no type info for the type number.
   * Returns empty string if there's no text for the type.
   */
  public static String getFullText(int type) {
    ABLNodeType e = typeMap.get(type);
    if (e == null)
      return null;
    if (e.options.contains(NodeTypesOption.PLACEHOLDER))
      return null;
    if (!e.options.contains(NodeTypesOption.KEYWORD))
      return "";
    return Strings.nullToEmpty(e.text).toUpperCase();
  }

  public static ABLNodeType getLiteral(String text) {
    return getLiteral(text, null);
  }

  public static ABLNodeType getLiteral(String text, ABLNodeType defaultType) {
    if (text == null)
      return defaultType;
    ABLNodeType type = literalsMap.get(text.toLowerCase());
    if (type == null)
      return defaultType;
    return type;
  }

  public static String getFullText(String text) {
    if (text == null)
      return "";
    ABLNodeType type = literalsMap.get(text.toLowerCase());
    if (type == null)
      return "";
    return type.text.toUpperCase();
  }

  /**
   * Get the type number for a type name. For those type names that have it, the "_KW" suffix is optional.
   * 
   * @param s type name
   * @return -1 if invalid type name is entered.
   */
  public static int getTypeNum(String s) {
    if (s == null)
      return -1;
    if (s.startsWith("_"))
      return -1;
    ABLNodeType ret = literalsMap.get(s.toLowerCase());
    if (ret == null) {
      // It's possible that we've been passed a token type name which needs
      // to have the _KW suffix added to it.
      ret = literalsMap.get(s.toLowerCase() + "_KW");
    }
    if (ret == null)
      return -1;
    return ret.getType();
  }

  public static boolean isKeywordType(int nodeType) {
    ABLNodeType type = typeMap.get(nodeType);
    if (type == null)
      return false;
    return type.isKeyword();
  }

  /**
   * @return True if node type can't be used as a variable name or field name among other things
   */
  public static boolean isReserved(int nodeType) {
    ABLNodeType type = typeMap.get(nodeType);
    if (type == null)
      return false;
    return type.isReservedKeyword();
  }

  static boolean isUnreservedKeywordType(int nodeType) {
    ABLNodeType type = typeMap.get(nodeType);
    if (type == null)
      return false;
    return type.isUnreservedKeywordType();
  }

  public static boolean isSystemHandleName(int nodeType) {
    ABLNodeType type = typeMap.get(nodeType);
    if (type == null)
      return false;
    return type.isSystemHandle();
  }

  public static boolean isValidDatatype(int nodeType) {
    ABLNodeType type = typeMap.get(nodeType);
    if (type == null)
      return false;
    return type.isValidDatatype();
  }

  static boolean mayBeNoArgFunc(int nodeType) {
    ABLNodeType type = typeMap.get(nodeType);
    if (type == null)
      return false;
    return type.isNoArgFunc();
  }

  static boolean isRegularFunc(int nodeType) {
    ABLNodeType type = typeMap.get(nodeType);
    if (type == null)
      return false;
    return type.isRegularFunc();
  }

  public static DataType getDataType(int nodeType) {
    switch (nodeType) {
      case Proparse.VOID:
        return DataType.VOID;
      case Proparse.CHARACTER:
        return DataType.CHARACTER;
      case Proparse.DATE:
        return DataType.DATE;
      case Proparse.LOGICAL:
        return DataType.LOGICAL;
      case Proparse.INTEGER:
        return DataType.INTEGER;
      case Proparse.DECIMAL:
        return DataType.DECIMAL;
      case Proparse.RECID:
        return DataType.RECID;
      case Proparse.RAW:
        return DataType.RAW;
      case Proparse.HANDLE:
      case Proparse.WIDGETHANDLE:
        return DataType.HANDLE;
      case Proparse.MEMPTR:
        return DataType.MEMPTR;
      case Proparse.ROWID:
        return DataType.ROWID;
      case Proparse.COMHANDLE:
        return DataType.COMPONENT_HANDLE;
      case Proparse.TABLE:
        return DataType.TABLE;
      case Proparse.TABLEHANDLE:
        return DataType.TABLE_HANDLE;
      case Proparse.BLOB:
        return DataType.BLOB;
      case Proparse.CLOB:
        return DataType.CLOB;
      case Proparse.BYTE:
        return DataType.BYTE;
      case Proparse.SHORT:
        return DataType.SHORT;
      case Proparse.LONG:
        return DataType.LONG;
      case Proparse.FLOAT:
        return DataType.FLOAT;
      case Proparse.DOUBLE:
        return DataType.DOUBLE;
      case Proparse.UNSIGNEDSHORT:
        return DataType.UNSIGNED_SHORT;
      case Proparse.UNSIGNEDBYTE:
        return DataType.UNSIGNED_BYTE;
      case Proparse.CURRENCY:
        return DataType.CURRENCY;
      case Proparse.ERRORCODE:
        return DataType.ERROR_CODE;
      case Proparse.FIXCHAR:
        return DataType.FIXCHAR;
      case Proparse.BIGINT:
        return DataType.BIGINT;
      case Proparse.TIME:
        return DataType.TIME;
      case Proparse.DATETIME:
        return DataType.DATETIME;
      case Proparse.DATASET:
        return DataType.DATASET;
      case Proparse.DATASETHANDLE:
        return DataType.DATASET_HANDLE;
      case Proparse.LONGCHAR:
        return DataType.LONGCHAR;
      case Proparse.DATETIMETZ:
        return DataType.DATETIME_TZ;
      case Proparse.INT64:
        return DataType.INT64;
      case Proparse.UNSIGNEDINTEGER:
        return DataType.UNSIGNED_INTEGER;
      default:
        return DataType.UNKNOWN;
    }
  }

  /**
   * An AS phrase allows further abbreviations on the datatype names. Input a token's text, this returns 0 if it is not
   * a datatype abbreviation, otherwise returns the integer token type for the abbreviation. Here's the normal keyword
   * abbreviation, with what AS phrase allows:
   * <ul>
   * <li>char: c
   * <li>date: da
   * <li>dec: de
   * <li>int: i
   * <li>logical: l
   * <li>recid: rec
   * <li>rowid: rowi
   * <li>widget-h: widg
   * </ul>
   */
  public static ABLNodeType abbrevDatatype(String text) {
    String s = text.toLowerCase();
    if ("cha".startsWith(s))
      return ABLNodeType.CHARACTER;
    if ("da".equals(s) || "dat".equals(s))
      return ABLNodeType.DATE;
    if ("de".equals(s))
      return ABLNodeType.DECIMAL;
    if ("i".equals(s) || "in".equals(s))
      return ABLNodeType.INTEGER;
    if ("logical".startsWith(s))
      return ABLNodeType.LOGICAL;
    if ("rec".equals(s) || "reci".equals(s))
      return ABLNodeType.RECID;
    if ("rowi".equals(s))
      return ABLNodeType.ROWID;
    if ("widget-h".startsWith(s) && s.length() >= 4)
      return ABLNodeType.WIDGETHANDLE;

    return ABLNodeType.INVALID_NODE;
  }

  private static void generateBaseTokenTypes(BufferedReader reader, PrintStream out, PrintStream out2)
      throws IOException {
    SortedSet<String> keywords = new TreeSet<>();
    String line = reader.readLine();
    while ((line != null) && !line.trim().isEmpty()) {
      line = line.substring(9);
      for (String str : line.split(" ")) {
        str = str.replace("-", "").replace("_", "").replace("(", "");
        keywords.add(str);
      }
      line = reader.readLine();
    }
    // Collections.sort(keywords);
    int index = 301;
    for (String kw : keywords) {
      out.println(kw + "=" + (index++));
      out2.println(kw);
    }
  }

  private static void generateVSCodeKeywords(final PrintStream out) {
    boolean first = true;
    out.println("export const keywords = [");
    for (ABLNodeType type : ABLNodeType.values()) {
      if (type.isKeyword()) {
        out.print((first ? "" : ",") + " ");
        out.println("\"" + type.getText().toLowerCase() + "\"");
        first = false;
        if (type.getText().length() != type.abbrMain) {
          out.println(", \"" + type.getText().toLowerCase().substring(0, type.abbrMain) + "\"");
        }
      }
    }
    out.println("];");
  }

  private static void generateKeywordsG4(final PrintStream out) {
    out.println("// Generated file - Do not manually edit");
    out.println();
    out.println("parser grammar keywords;");
    out.println();
    out.println("options {");
    out.println("  tokenVocab=BaseTokenTypes;");
    out.println("}");
    out.println();

    final Comparator<ABLNodeType> naturalOrder = (ABLNodeType t1,
        ABLNodeType t2) -> t1.toString().compareTo(t2.toString());
    final Predicate<ABLNodeType> p1 = type -> !RECORD_FUNCTIONS.contains(type)
        && !OPTIONAL_ARG_FUNCTIONS.contains(type);
    out.println("argFunction:");
    out.println("  (");
    REGULAR_FUNCTIONS.stream().filter(p1).sorted(naturalOrder).findFirst().ifPresent(
        type -> out.println("    " + type));
    REGULAR_FUNCTIONS.stream().filter(p1).sorted(naturalOrder).skip(1).forEach(type -> out.println("  | " + type));
    out.println("  )");
    out.println("  functionArgs");
    out.println(";");
    out.println();

    out.println("recordFunction:");
    out.println("  (");
    RECORD_FUNCTIONS.stream().sorted(naturalOrder).findFirst().ifPresent(type -> out.println("    " + type));
    RECORD_FUNCTIONS.stream().sorted(naturalOrder).skip(1).forEach(type -> out.println("  | " + type));
    out.println("  )");
    out.println("  ( LEFTPAREN record RIGHTPAREN | record )");
    out.println(";");
    out.println();

    out.println("optionalArgFunction:");
    out.println("  (");
    OPTIONAL_ARG_FUNCTIONS.stream().sorted(naturalOrder).findFirst().ifPresent(type -> out.println("    " + type));
    OPTIONAL_ARG_FUNCTIONS.stream().sorted(naturalOrder).skip(1).forEach(type -> out.println("  | " + type));
    out.println("  )");
    out.println("  optionalFunctionArgs");
    out.println(";");
    out.println();

    out.println("noArgFunction:");
    NO_ARGUMENT_FUNCTIONS.stream().sorted(naturalOrder).findFirst().ifPresent(type -> out.println("  " + type));
    NO_ARGUMENT_FUNCTIONS.stream().sorted(naturalOrder).skip(1).forEach(type -> out.println("| " + type));
    out.println(";");
    out.println();

    out.println("systemHandleName:");
    SYSTEM_HANDLES.stream().sorted(naturalOrder).findFirst().ifPresent(type -> out.println("  " + type));
    SYSTEM_HANDLES.stream().sorted(naturalOrder).skip(1).forEach(type -> out.println("| " + type));
    out.println(";");
    out.println();

    out.println("unreservedkeyword:");
    Arrays.stream(ABLNodeType.values()).filter(ABLNodeType::isUnreservedKeywordType).sorted(
        naturalOrder).findFirst().ifPresent(type -> out.println("  " + type));
    Arrays.stream(ABLNodeType.values()).filter(ABLNodeType::isUnreservedKeywordType).sorted(naturalOrder).skip(
        1).forEach(type -> out.println("| " + type));
    out.println(";");
  }

  private static void extractBaseTokenTypes(BufferedReader reader, PrintStream out) throws IOException {
    String line = reader.readLine();
    while (line != null) {
      if (!line.trim().isEmpty() && (line.indexOf('=') >= 0)
          && (Integer.parseInt(line.substring(line.indexOf('=') + 1)) >= 301)) {
        out.println(line.substring(0, line.indexOf('=')));
      }
      line = reader.readLine();
    }
  }

  public static void main(String[] args) throws IOException {
    try (PrintStream output = new PrintStream("src/main/antlr4/imports/keywords.g4")) {
      generateKeywordsG4(output);
    }
    generateVSCodeKeywords(System.out);
    try (FileReader reader = new FileReader("kwlist.txt");
        BufferedReader reader2 = new BufferedReader(reader);
        PrintStream output = new PrintStream("out.txt");
        PrintStream output2 = new PrintStream("out2.txt");) {
      generateBaseTokenTypes(reader2, output, output2);
    }
    try (FileReader reader = new FileReader("src/main/antlr4/imports/BaseTokenTypes.tokens");
        BufferedReader reader2 = new BufferedReader(reader);
        PrintStream output = new PrintStream("out3.txt")) {
      extractBaseTokenTypes(reader2, output);
    }
  }
}
