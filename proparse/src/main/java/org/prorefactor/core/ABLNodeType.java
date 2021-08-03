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
package org.prorefactor.core;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.antlr.v4.runtime.Token;
import org.prorefactor.proparse.antlr4.Proparse;

import com.google.common.base.Strings;

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
  LTOREQUAL(Proparse.LTOREQUAL, ">=", NodeTypesOption.SYMBOL),
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
  AGGREGATE_PHRASE(Proparse.Aggregate_phrase, NodeTypesOption.STRUCTURE),
  ARRAY_SUBSCRIPT(Proparse.Array_subscript, NodeTypesOption.STRUCTURE),
  ASSIGN_DYNAMIC_NEW(Proparse.Assign_dynamic_new, NodeTypesOption.STRUCTURE),
  ASSIGN_FROM_BUFFER(Proparse.Assign_from_buffer, NodeTypesOption.STRUCTURE),
  AUTOMATION_OBJECT(Proparse.Automationobject, NodeTypesOption.STRUCTURE),
  BLOCK_ITERATOR(Proparse.Block_iterator, NodeTypesOption.STRUCTURE),
  BLOCK_LABEL(Proparse.Block_label, NodeTypesOption.STRUCTURE),
  CODE_BLOCK(Proparse.Code_block, NodeTypesOption.STRUCTURE),
  EDITING_PHRASE(Proparse.Editing_phrase, NodeTypesOption.STRUCTURE),
  ENTERED_FUNC(Proparse.Entered_func, NodeTypesOption.STRUCTURE),
  EVENT_LIST(Proparse.Event_list, NodeTypesOption.STRUCTURE),
  EXPR_STATEMENT(Proparse.Expr_statement, NodeTypesOption.STRUCTURE),
  FIELD_REF(Proparse.Field_ref, NodeTypesOption.STRUCTURE),
  FORM_ITEM(Proparse.Form_item, NodeTypesOption.STRUCTURE),
  FORMAT_PHRASE(Proparse.Format_phrase, NodeTypesOption.STRUCTURE),
  LOCAL_METHOD_REF(Proparse.Local_method_ref, NodeTypesOption.STRUCTURE),
  ATTRIBUTE_REF(Proparse.Attribute_ref, NodeTypesOption.STRUCTURE),
  LEFT_PART(Proparse.Left_Part, NodeTypesOption.STRUCTURE),
  METHOD_PARAM_LIST(Proparse.Method_param_list, NodeTypesOption.STRUCTURE),
  NOT_CASESENS(Proparse.Not_casesens, NodeTypesOption.STRUCTURE),
  PARAMETER_LIST(Proparse.Parameter_list, NodeTypesOption.STRUCTURE),
  PROGRAM_ROOT(Proparse.Program_root, NodeTypesOption.STRUCTURE),
  PROGRAM_TAIL(Proparse.Program_tail, NodeTypesOption.STRUCTURE),
  PROPERTY_GETTER(Proparse.Property_getter, NodeTypesOption.STRUCTURE),
  PROPERTY_SETTER(Proparse.Property_setter, NodeTypesOption.STRUCTURE),
  RECORD_NAME(Proparse.Record_name, NodeTypesOption.STRUCTURE),
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
  KEYCODE(Proparse.KEYCODE, "key-code", "keycode", NodeTypesOption.KEYWORD),
  KEYFUNCTION(Proparse.KEYFUNCTION, "key-function", 8, "keyfunction", 7, NodeTypesOption.KEYWORD),
  KEYLABEL(Proparse.KEYLABEL, "key-label", "keylabel", NodeTypesOption.KEYWORD),
  LASTKEY(Proparse.LASTKEY, "last-key", "lastkey", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LC(Proparse.LC, "lc", "lower", NodeTypesOption.KEYWORD),
  MAXIMUM(Proparse.MAXIMUM, "max", "maximum", NodeTypesOption.KEYWORD),
  MENUBAR(Proparse.MENUBAR, "menu-bar", "menubar", NodeTypesOption.KEYWORD),
  MODULO(Proparse.MODULO, "modulo", 3, NodeTypesOption.KEYWORD),
  MPE(Proparse.MPE, "mpe", NodeTypesOption.KEYWORD),
  NOATTRLIST(Proparse.NOATTRLIST, "no-attr-list", 9, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOATTRSPACE(Proparse.NOATTRSPACE, "no-attr-space", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NORMAL(Proparse.NORMAL, "normal", NodeTypesOption.KEYWORD),
  NOTACTIVE(Proparse.NOTACTIVE, "not-active", NodeTypesOption.KEYWORD),
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
  SIDELABELS(Proparse.SIDELABELS, "side-labels", 8, NodeTypesOption.KEYWORD),
  STATUSBAR(Proparse.STATUSBAR, "status-bar", NodeTypesOption.KEYWORD),
  TERMINAL(Proparse.TERMINAL, "term", "terminal", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  THROUGH(Proparse.THROUGH, "through", "thru", NodeTypesOption.KEYWORD),
  TIMESTAMP(Proparse.TIMESTAMP, "timestamp", NodeTypesOption.KEYWORD),
  TOOLBAR(Proparse.TOOLBAR, "tool-bar", NodeTypesOption.KEYWORD),
  TRANSACTION(Proparse.TRANSACTION, "trans", 5, "transaction", 8, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  UNSIGNEDINTEGER(Proparse.UNSIGNEDINTEGER, "unsigned-integer", NodeTypesOption.KEYWORD),
  VAR(Proparse.VAR, "var", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WORKTABLE(Proparse.WORKTABLE, "work-table", 8, "workfile", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),

  // A
  ABSOLUTE(Proparse.ABSOLUTE, "absolute", 3, NodeTypesOption.KEYWORD),
  ABSTRACT(Proparse.ABSTRACT, "abstract", NodeTypesOption.KEYWORD),
  ACCELERATOR(Proparse.ACCELERATOR, "accelerator", NodeTypesOption.KEYWORD),
  ACCUMULATE(Proparse.ACCUMULATE, "accumulate", 5, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ACTIVEFORM(Proparse.ACTIVEFORM, "active-form", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ACTIVEWINDOW(Proparse.ACTIVEWINDOW, "active-window", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ADD(Proparse.ADD, "add", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ADDINTERVAL(Proparse.ADDINTERVAL, "add-interval", NodeTypesOption.KEYWORD),
  ADVISE(Proparse.ADVISE, "advise", NodeTypesOption.KEYWORD),
  ALERTBOX(Proparse.ALERTBOX, "alert-box", NodeTypesOption.KEYWORD),
  ALIAS(Proparse.ALIAS, "alias", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ALL(Proparse.ALL, "all", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ALLOWREPLICATION(Proparse.ALLOWREPLICATION, "allow-replication", NodeTypesOption.KEYWORD),
  ALTER(Proparse.ALTER, "alter", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ALTERNATEKEY(Proparse.ALTERNATEKEY, "alternate-key", NodeTypesOption.KEYWORD),
  AMBIGUOUS(Proparse.AMBIGUOUS, "ambiguous", 5, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  AND(Proparse.AND, "and", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ANSIONLY(Proparse.ANSIONLY, "ansi-only", NodeTypesOption.KEYWORD),
  ANY(Proparse.ANY, "any", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ANYWHERE(Proparse.ANYWHERE, "anywhere", NodeTypesOption.KEYWORD),
  APPEND(Proparse.APPEND, "append", NodeTypesOption.KEYWORD),
  APPLICATION(Proparse.APPLICATION, "application", NodeTypesOption.KEYWORD),
  APPLY(Proparse.APPLY, "apply", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ARRAYMESSAGE(Proparse.ARRAYMESSAGE, "array-message", 7, NodeTypesOption.KEYWORD),
  AS(Proparse.AS, "as", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ASKOVERWRITE(Proparse.ASKOVERWRITE, "ask-overwrite", NodeTypesOption.KEYWORD),
  ASSEMBLY(Proparse.ASSEMBLY, "assembly", NodeTypesOption.KEYWORD),
  ASSIGN(Proparse.ASSIGN, "assign", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ASYNCHRONOUS(Proparse.ASYNCHRONOUS, "asynchronous", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  AT(Proparse.AT, "at", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ATTACHMENT(Proparse.ATTACHMENT, "attachment", NodeTypesOption.KEYWORD),
  ATTRSPACE(Proparse.ATTRSPACE, "attr-space", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  AUDITCONTROL(Proparse.AUDITCONTROL, "audit-control", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  AUDITENABLED(Proparse.AUDITENABLED, "audit-enabled", NodeTypesOption.KEYWORD),
  AUDITPOLICY(Proparse.AUDITPOLICY, "audit-policy", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  AUTHORIZATION(Proparse.AUTHORIZATION, "authorization", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  AUTOCOMPLETION(Proparse.AUTOCOMPLETION, "auto-completion", 9, NodeTypesOption.KEYWORD),
  AUTOGO(Proparse.AUTOGO, "auto-go", NodeTypesOption.KEYWORD),
  AUTOMATIC(Proparse.AUTOMATIC, "automatic", NodeTypesOption.KEYWORD),
  AUTORETURN(Proparse.AUTORETURN, "auto-return", 8, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  AVAILABLE(Proparse.AVAILABLE, "available", 5, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  AVERAGE(Proparse.AVERAGE, "average", 3, NodeTypesOption.KEYWORD),
  AVG(Proparse.AVG, "avg", NodeTypesOption.KEYWORD),

  // B
  BACKGROUND(Proparse.BACKGROUND, "background", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BACKWARDS(Proparse.BACKWARDS, "backwards", 8, NodeTypesOption.KEYWORD),
  BASE64(Proparse.BASE64, "base64", NodeTypesOption.KEYWORD),
  BASE64DECODE(Proparse.BASE64DECODE, "base64-decode", NodeTypesOption.KEYWORD),
  BASE64ENCODE(Proparse.BASE64ENCODE, "base64-encode", NodeTypesOption.KEYWORD),
  BASEKEY(Proparse.BASEKEY, "base-key", NodeTypesOption.KEYWORD),
  BATCHSIZE(Proparse.BATCHSIZE, "batch-size", NodeTypesOption.KEYWORD),
  BEFOREHIDE(Proparse.BEFOREHIDE, "before-hide", 8, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BEFORETABLE(Proparse.BEFORETABLE, "before-table", NodeTypesOption.KEYWORD),
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
  BLOCKLEVEL(Proparse.BLOCKLEVEL, "block-level", 9, NodeTypesOption.KEYWORD),
  BOTH(Proparse.BOTH, "both", NodeTypesOption.KEYWORD),
  BOTTOM(Proparse.BOTTOM, "bottom", NodeTypesOption.KEYWORD),
  BOX(Proparse.BOX, "box", NodeTypesOption.KEYWORD),
  BREAK(Proparse.BREAK, "break", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BROWSE(Proparse.BROWSE, "browse", NodeTypesOption.KEYWORD),
  BTOS(Proparse.BTOS, "btos", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BUFFER(Proparse.BUFFER, "buffer", NodeTypesOption.KEYWORD),
  BUFFERCHARS(Proparse.BUFFERCHARS, "buffer-chars", NodeTypesOption.KEYWORD),
  BUFFERCOMPARE(Proparse.BUFFERCOMPARE, "buffer-compare", 11, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BUFFERCOPY(Proparse.BUFFERCOPY, "buffer-copy", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BUFFERGROUPID(Proparse.BUFFERGROUPID, "buffer-group-id", NodeTypesOption.KEYWORD),
  BUFFERGROUPNAME(Proparse.BUFFERGROUPNAME, "buffer-group-name", NodeTypesOption.KEYWORD),
  BUFFERLINES(Proparse.BUFFERLINES, "buffer-lines", NodeTypesOption.KEYWORD),
  BUFFERNAME(Proparse.BUFFERNAME, "buffer-name", 8, NodeTypesOption.KEYWORD),
  BUFFERTENANTID(Proparse.BUFFERTENANTID, "buffer-tenant-id", NodeTypesOption.KEYWORD),
  BUFFERTENANTNAME(Proparse.BUFFERTENANTNAME, "buffer-tenant-name", NodeTypesOption.KEYWORD),
  BY(Proparse.BY, "by", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BYPOINTER(Proparse.BYPOINTER, "by-pointer", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BYREFERENCE(Proparse.BYREFERENCE, "by-reference", NodeTypesOption.KEYWORD),
  BYTE(Proparse.BYTE, "byte", NodeTypesOption.KEYWORD),
  BYVALUE(Proparse.BYVALUE, "by-value", NodeTypesOption.KEYWORD),
  BYVARIANTPOINTER(Proparse.BYVARIANTPOINTER, "by-variant-pointer", 16, NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),

  // C
  CACHE(Proparse.CACHE, "cache", NodeTypesOption.KEYWORD),
  CACHESIZE(Proparse.CACHESIZE, "cache-size", NodeTypesOption.KEYWORD),
  CALL(Proparse.CALL, "call", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CANCELBUTTON(Proparse.CANCELBUTTON, "cancel-button", NodeTypesOption.KEYWORD),
  CANDO(Proparse.CANDO, "can-do", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CANFIND(Proparse.CANFIND, "can-find", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CANQUERY(Proparse.CANQUERY, "can-query", NodeTypesOption.KEYWORD),
  CANSET(Proparse.CANSET, "can-set", NodeTypesOption.KEYWORD),
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
  CHOOSE(Proparse.CHOOSE, "choose", NodeTypesOption.KEYWORD),
  CHR(Proparse.CHR, "chr", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CLASS(Proparse.CLASS, "class", NodeTypesOption.KEYWORD),
  CLEAR(Proparse.CLEAR, "clear", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CLIENTPRINCIPAL(Proparse.CLIENTPRINCIPAL, "client-principal", NodeTypesOption.KEYWORD),
  CLIPBOARD(Proparse.CLIPBOARD, "clipboard", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CLOB(Proparse.CLOB, "clob", NodeTypesOption.KEYWORD),
  CLOSE(Proparse.CLOSE, "close", NodeTypesOption.KEYWORD),
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
  COLUMNOF(Proparse.COLUMNOF, "column-of", NodeTypesOption.KEYWORD),
  COLUMNPFCOLOR(Proparse.COLUMNPFCOLOR, "column-pfcolor", 10, NodeTypesOption.KEYWORD),
  COMBOBOX(Proparse.COMBOBOX, "combo-box", NodeTypesOption.KEYWORD),
  COMMAND(Proparse.COMMAND, "command", NodeTypesOption.KEYWORD),
  COMPILE(Proparse.COMPILE, "compile", NodeTypesOption.KEYWORD),
  COMPILER(Proparse.COMPILER, "compiler", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  COMPLETE(Proparse.COMPLETE, "complete", NodeTypesOption.KEYWORD),
  COMSELF(Proparse.COMSELF, "com-self", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CONFIGNAME(Proparse.CONFIGNAME, "config-name", NodeTypesOption.KEYWORD),
  CONNECT(Proparse.CONNECT, "connect", NodeTypesOption.KEYWORD),
  CONNECTED(Proparse.CONNECTED, "connected", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CONSTRUCTOR(Proparse.CONSTRUCTOR, "constructor", NodeTypesOption.KEYWORD),
  CONTAINS(Proparse.CONTAINS, "contains", NodeTypesOption.KEYWORD),
  CONTENTS(Proparse.CONTENTS, "contents", NodeTypesOption.KEYWORD),
  CONTEXT(Proparse.CONTEXT, "context", NodeTypesOption.KEYWORD),
  CONTEXTHELP(Proparse.CONTEXTHELP, "context-help", NodeTypesOption.KEYWORD),
  CONTEXTHELPFILE(Proparse.CONTEXTHELPFILE, "context-help-file", NodeTypesOption.KEYWORD),
  CONTEXTHELPID(Proparse.CONTEXTHELPID, "context-help-id", NodeTypesOption.KEYWORD),
  CONTEXTPOPUP(Proparse.CONTEXTPOPUP, "context-popup", 11, NodeTypesOption.KEYWORD),
  CONTROL(Proparse.CONTROL, "control", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CONTROLFRAME(Proparse.CONTROLFRAME, "control-frame", 12, NodeTypesOption.KEYWORD),
  CONVERT(Proparse.CONVERT, "convert", NodeTypesOption.KEYWORD),
  CONVERT3DCOLORS(Proparse.CONVERT3DCOLORS, "convert-3d-colors", 10, NodeTypesOption.KEYWORD),
  COPYDATASET(Proparse.COPYDATASET, "copy-dataset", NodeTypesOption.KEYWORD),
  COPYLOB(Proparse.COPYLOB, "copy-lob", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  COPYTEMPTABLE(Proparse.COPYTEMPTABLE, "copy-temp-table", NodeTypesOption.KEYWORD),
  COUNT(Proparse.COUNT, "count", NodeTypesOption.KEYWORD),
  COUNTOF(Proparse.COUNTOF, "count-of", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CREATE(Proparse.CREATE, "create", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CREATELIKESEQUENTIAL(Proparse.CREATELIKESEQUENTIAL, "create-like-sequential", NodeTypesOption.KEYWORD),
  CREATETESTFILE(Proparse.CREATETESTFILE, "create-test-file", NodeTypesOption.KEYWORD),
  CURRENT(Proparse.CURRENT, "current", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CURRENTCHANGED(Proparse.CURRENTCHANGED, "current-changed", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CURRENTENVIRONMENT(Proparse.CURRENTENVIRONMENT, "current-environment", 11, NodeTypesOption.KEYWORD),
  CURRENTLANGUAGE(Proparse.CURRENTLANGUAGE, "current-language", 12, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CURRENTQUERY(Proparse.CURRENTQUERY, "current-query", NodeTypesOption.KEYWORD),
  CURRENTRESULTROW(Proparse.CURRENTRESULTROW, "current-result-row", NodeTypesOption.KEYWORD),
  CURRENTVALUE(Proparse.CURRENTVALUE, "current-value", NodeTypesOption.KEYWORD),
  CURRENTWINDOW(Proparse.CURRENTWINDOW, "current-window", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CURSOR(Proparse.CURSOR, "cursor", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),

  // D
  DATABASE(Proparse.DATABASE, "database", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DATABIND(Proparse.DATABIND, "data-bind", 6, NodeTypesOption.KEYWORD),
  DATARELATION(Proparse.DATARELATION, "data-relation", 8, NodeTypesOption.KEYWORD),
  DATASET(Proparse.DATASET, "dataset", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DATASETHANDLE(Proparse.DATASETHANDLE, "dataset-handle", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DATASOURCE(Proparse.DATASOURCE, "data-source", NodeTypesOption.KEYWORD),
  DATASOURCEMODIFIED(Proparse.DATASOURCEMODIFIED, "data-source-modified", NodeTypesOption.KEYWORD),
  DATASOURCEROWID(Proparse.DATASOURCEROWID, "data-source-rowid", NodeTypesOption.KEYWORD),
  DATE(Proparse.DATE, "date", NodeTypesOption.KEYWORD),
  DATETIME(Proparse.DATETIME, "datetime", NodeTypesOption.KEYWORD),
  DATETIMETZ(Proparse.DATETIMETZ, "datetime-tz", NodeTypesOption.KEYWORD),
  DAY(Proparse.DAY, "day", NodeTypesOption.KEYWORD),
  DBCODEPAGE(Proparse.DBCODEPAGE, "dbcodepage", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DBCOLLATION(Proparse.DBCOLLATION, "dbcollation", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DBNAME(Proparse.DBNAME, "dbname", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DBPARAM(Proparse.DBPARAM, "dbparam", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DBREMOTEHOST(Proparse.DBREMOTEHOST, "db-remote-host", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DBRESTRICTIONS(Proparse.DBRESTRICTIONS, "dbrestrictions", 6, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DBTASKID(Proparse.DBTASKID, "dbtaskid", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DBTYPE(Proparse.DBTYPE, "dbtype", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DBVERSION(Proparse.DBVERSION, "dbversion", 6, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DCOLOR(Proparse.DCOLOR, "dcolor", NodeTypesOption.KEYWORD),
  DDE(Proparse.DDE, "dde", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DEBLANK(Proparse.DEBLANK, "deblank", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DEBUG(Proparse.DEBUG, "debug", 4, NodeTypesOption.KEYWORD),
  DEBUGGER(Proparse.DEBUGGER, "debugger", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DEBUGLIST(Proparse.DEBUGLIST, "debug-list", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DECIMAL(Proparse.DECIMAL, "decimal", 3, NodeTypesOption.KEYWORD),
  DECIMALS(Proparse.DECIMALS, "decimals", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DECLARE(Proparse.DECLARE, "declare", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DECRYPT(Proparse.DECRYPT, "decrypt", NodeTypesOption.KEYWORD),
  DEFAULT(Proparse.DEFAULT, "default", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DEFAULTBUTTON(Proparse.DEFAULTBUTTON, "default-button", 11, NodeTypesOption.KEYWORD),
  DEFAULTEXTENSION(Proparse.DEFAULTEXTENSION, "default-extension", 10, NodeTypesOption.KEYWORD),
  DEFAULTNOXLATE(Proparse.DEFAULTNOXLATE, "default-noxlate", 12, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DEFAULTVALUE(Proparse.DEFAULTVALUE, "default-value", NodeTypesOption.KEYWORD),
  DEFAULTWINDOW(Proparse.DEFAULTWINDOW, "default-window", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DEFERLOBFETCH(Proparse.DEFERLOBFETCH, "defer-lob-fetch", NodeTypesOption.KEYWORD),
  DEFINE(Proparse.DEFINE, "define", 3, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DEFINED(Proparse.DEFINED, "defined", NodeTypesOption.KEYWORD),
  DELEGATE(Proparse.DELEGATE, "delegate", NodeTypesOption.KEYWORD),
  DELETE(Proparse.DELETE, "delete", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DELETECHARACTER(Proparse.DELETECHARACTER, "delete-character", NodeTypesOption.KEYWORD),
  DELETERESULTLISTENTRY(Proparse.DELETERESULTLISTENTRY, "delete-result-list-entry", NodeTypesOption.KEYWORD),
  DELIMITER(Proparse.DELIMITER, "delimiter", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DESCENDING(Proparse.DESCENDING, "descending", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DESELECTION(Proparse.DESELECTION, "deselection", NodeTypesOption.KEYWORD),
  DESTRUCTOR(Proparse.DESTRUCTOR, "destructor", NodeTypesOption.KEYWORD),
  DIALOGBOX(Proparse.DIALOGBOX, "dialog-box", NodeTypesOption.KEYWORD),
  DIALOGHELP(Proparse.DIALOGHELP, "dialog-help", NodeTypesOption.KEYWORD),
  DICTIONARY(Proparse.DICTIONARY, "dictionary", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DIR(Proparse.DIR, "dir", NodeTypesOption.KEYWORD),
  DISABLE(Proparse.DISABLE, "disable", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DISABLEAUTOZAP(Proparse.DISABLEAUTOZAP, "disable-auto-zap", NodeTypesOption.KEYWORD),
  DISABLED(Proparse.DISABLED, "disabled", NodeTypesOption.KEYWORD),
  DISCONNECT(Proparse.DISCONNECT, "disconnect", 6, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DISPLAY(Proparse.DISPLAY, "display", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DISTINCT(Proparse.DISTINCT, "distinct", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DO(Proparse.DO, "do", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DOS(Proparse.DOS, "dos", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DOUBLE(Proparse.DOUBLE, "double", NodeTypesOption.KEYWORD),
  DOWN(Proparse.DOWN, "down", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DROP(Proparse.DROP, "drop", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DROPDOWN(Proparse.DROPDOWN, "drop-down", NodeTypesOption.KEYWORD),
  DROPDOWNLIST(Proparse.DROPDOWNLIST, "drop-down-list", NodeTypesOption.KEYWORD),
  DROPFILENOTIFY(Proparse.DROPFILENOTIFY, "drop-file-notify", NodeTypesOption.KEYWORD),
  DROPTARGET(Proparse.DROPTARGET, "drop-target", NodeTypesOption.KEYWORD),
  DUMP(Proparse.DUMP, "dump", NodeTypesOption.KEYWORD),
  DYNAMIC(Proparse.DYNAMIC, "dynamic", NodeTypesOption.KEYWORD),
  DYNAMICCAST(Proparse.DYNAMICCAST, "dynamic-cast", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DYNAMICCURRENTVALUE(Proparse.DYNAMICCURRENTVALUE, "dynamic-current-value", NodeTypesOption.KEYWORD),
  DYNAMICFUNCTION(Proparse.DYNAMICFUNCTION, "dynamic-function", 12, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DYNAMICINVOKE(Proparse.DYNAMICINVOKE, "dynamic-invoke", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DYNAMICNEW(Proparse.DYNAMICNEW, "dynamic-new", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DYNAMICNEXTVALUE(Proparse.DYNAMICNEXTVALUE, "dynamic-next-value", NodeTypesOption.KEYWORD),
  DYNAMICPROPERTY(Proparse.DYNAMICPROPERTY, "dynamic-property", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),

  // E
  EACH(Proparse.EACH, "each", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ECHO(Proparse.ECHO, "echo", NodeTypesOption.KEYWORD),
  EDITING(Proparse.EDITING, "editing", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  EDITOR(Proparse.EDITOR, "editor", NodeTypesOption.KEYWORD),
  EDITUNDO(Proparse.EDITUNDO, "edit-undo", NodeTypesOption.KEYWORD),
  ELSE(Proparse.ELSE, "else", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  EMPTY(Proparse.EMPTY, "empty", NodeTypesOption.KEYWORD),
  ENABLE(Proparse.ENABLE, "enable", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ENCODE(Proparse.ENCODE, "encode", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ENCRYPT(Proparse.ENCRYPT, "encrypt", NodeTypesOption.KEYWORD),
  ENCRYPTIONSALT(Proparse.ENCRYPTIONSALT, "encryption-salt", NodeTypesOption.KEYWORD),
  END(Proparse.END, "end", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ENDMOVE(Proparse.ENDMOVE, "end-move", NodeTypesOption.KEYWORD),
  ENDRESIZE(Proparse.ENDRESIZE, "end-resize", NodeTypesOption.KEYWORD),
  ENDROWRESIZE(Proparse.ENDROWRESIZE, "end-row-resize", NodeTypesOption.KEYWORD),
  ENTERED(Proparse.ENTERED, "entered", NodeTypesOption.KEYWORD),
  ENTRY(Proparse.ENTRY, "entry", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ENUM(Proparse.ENUM, "enum", NodeTypesOption.KEYWORD),
  EQ(Proparse.EQ, "eq", NodeTypesOption.KEYWORD),
  ERROR(Proparse.ERROR, "error", NodeTypesOption.KEYWORD),
  ERRORSTACKTRACE(Proparse.ERRORSTACKTRACE, "error-stack-trace", NodeTypesOption.KEYWORD),
  ERRORSTATUS(Proparse.ERRORSTATUS, "error-status", 10, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ESCAPE(Proparse.ESCAPE, "escape", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ETIME(Proparse.ETIME, "etime", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  EVENT(Proparse.EVENT, "event", NodeTypesOption.KEYWORD),
  EVENTPROCEDURE(Proparse.EVENTPROCEDURE, "event-procedure", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  EVENTS(Proparse.EVENTS, "events", NodeTypesOption.KEYWORD),
  EXCEPT(Proparse.EXCEPT, "except", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  EXCLUSIVEID(Proparse.EXCLUSIVEID, "exclusive-id", NodeTypesOption.KEYWORD),
  EXCLUSIVEWEBUSER(Proparse.EXCLUSIVEWEBUSER, "exclusive-web-user", 13, NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  EXECUTE(Proparse.EXECUTE, "execute", NodeTypesOption.KEYWORD),
  EXISTS(Proparse.EXISTS, "exists", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  EXP(Proparse.EXP, "exp", NodeTypesOption.KEYWORD),
  EXPAND(Proparse.EXPAND, "expand", NodeTypesOption.KEYWORD),
  EXPANDABLE(Proparse.EXPANDABLE, "expandable", NodeTypesOption.KEYWORD),
  EXPLICIT(Proparse.EXPLICIT, "explicit", NodeTypesOption.KEYWORD),
  EXPORT(Proparse.EXPORT, "export", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  EXTENDED(Proparse.EXTENDED, "extended", NodeTypesOption.KEYWORD),
  EXTENT(Proparse.EXTENT, "extent", NodeTypesOption.KEYWORD),
  EXTERNAL(Proparse.EXTERNAL, "external", NodeTypesOption.KEYWORD),

  // F
  FALSE(Proparse.FALSE, "false", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FALSELEAKS(Proparse.FALSELEAKS, "false-leaks", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FETCH(Proparse.FETCH, "fetch", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FGCOLOR(Proparse.FGCOLOR, "fgcolor", 3, NodeTypesOption.KEYWORD),
  FILEINFORMATION(Proparse.FILEINFORMATION, "file-information", 9, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FILL(Proparse.FILL, "fill", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FILLIN(Proparse.FILLIN, "fill-in", NodeTypesOption.KEYWORD),
  FILLWHERESTRING(Proparse.FILLWHERESTRING, "fill-where-string", NodeTypesOption.KEYWORD),
  FILTERS(Proparse.FILTERS, "filters", NodeTypesOption.KEYWORD),
  FINAL(Proparse.FINAL, "final", NodeTypesOption.KEYWORD),
  FINALLY(Proparse.FINALLY, "finally", NodeTypesOption.KEYWORD),
  FIND(Proparse.FIND, "find", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FINDCASESENSITIVE(Proparse.FINDCASESENSITIVE, "find-case-sensitive", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  FINDER(Proparse.FINDER, "finder", NodeTypesOption.KEYWORD),
  FINDGLOBAL(Proparse.FINDGLOBAL, "find-global", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FINDNEXTOCCURRENCE(Proparse.FINDNEXTOCCURRENCE, "find-next-occurrence", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  FINDPREVOCCURRENCE(Proparse.FINDPREVOCCURRENCE, "find-prev-occurrence", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  FINDSELECT(Proparse.FINDSELECT, "find-select", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FINDWRAPAROUND(Proparse.FINDWRAPAROUND, "find-wrap-around", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FIRST(Proparse.FIRST, "first", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FIRSTFORM(Proparse.FIRSTFORM, "first-form", NodeTypesOption.KEYWORD),
  FIRSTOF(Proparse.FIRSTOF, "first-of", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FITLASTCOLUMN(Proparse.FITLASTCOLUMN, "fit-last-column", NodeTypesOption.KEYWORD),
  FIXCODEPAGE(Proparse.FIXCODEPAGE, "fix-codepage", NodeTypesOption.KEYWORD),
  FIXEDONLY(Proparse.FIXEDONLY, "fixed-only", NodeTypesOption.KEYWORD),
  FLAGS(Proparse.FLAGS, "flags", NodeTypesOption.KEYWORD),
  FLATBUTTON(Proparse.FLATBUTTON, "flat-button", NodeTypesOption.KEYWORD),
  FLOAT(Proparse.FLOAT, "float", NodeTypesOption.KEYWORD),
  FOCUS(Proparse.FOCUS, "focus", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FONT(Proparse.FONT, "font", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FONTTABLE(Proparse.FONTTABLE, "font-table", NodeTypesOption.KEYWORD),
  FOR(Proparse.FOR, "for", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FORCEFILE(Proparse.FORCEFILE, "force-file", NodeTypesOption.KEYWORD),
  FOREIGNKEYHIDDEN(Proparse.FOREIGNKEYHIDDEN, "foreign-key-hidden", NodeTypesOption.KEYWORD),
  FORMAT(Proparse.FORMAT, "format", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FORMINPUT(Proparse.FORMINPUT, "form-input", NodeTypesOption.KEYWORD),
  FORMLONGINPUT(Proparse.FORMLONGINPUT, "form-long-input", NodeTypesOption.KEYWORD),
  FORWARDS(Proparse.FORWARDS, "forwards", 7, NodeTypesOption.KEYWORD),
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
  FRAMEVALUE(Proparse.FRAMEVALUE, "frame-value", 9, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FREQUENCY(Proparse.FREQUENCY, "frequency", NodeTypesOption.KEYWORD),
  FROM(Proparse.FROM, "from", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FROMCURRENT(Proparse.FROMCURRENT, "from-current", 8, NodeTypesOption.KEYWORD),
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
  GETBITS(Proparse.GETBITS, "get-bits", NodeTypesOption.KEYWORD),
  GETBUFFERHANDLE(Proparse.GETBUFFERHANDLE, "get-buffer-handle", NodeTypesOption.KEYWORD),
  GETBYTEORDER(Proparse.GETBYTEORDER, "get-byte-order", NodeTypesOption.KEYWORD),
  GETBYTES(Proparse.GETBYTES, "get-bytes", NodeTypesOption.KEYWORD),
  GETCGILIST(Proparse.GETCGILIST, "get-cgi-list", NodeTypesOption.KEYWORD),
  GETCGILONGVALUE(Proparse.GETCGILONGVALUE, "get-cgi-long-value", NodeTypesOption.KEYWORD),
  GETCGIVALUE(Proparse.GETCGIVALUE, "get-cgi-value", NodeTypesOption.KEYWORD),
  GETCLASS(Proparse.GETCLASS, "get-class", NodeTypesOption.KEYWORD),
  GETCOLLATIONS(Proparse.GETCOLLATIONS, "get-collations", 8, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  GETCONFIGVALUE(Proparse.GETCONFIGVALUE, "get-config-value", NodeTypesOption.KEYWORD),
  GETDBCLIENT(Proparse.GETDBCLIENT, "get-db-client", NodeTypesOption.KEYWORD),
  GETDIR(Proparse.GETDIR, "get-dir", NodeTypesOption.KEYWORD),
  GETDOUBLE(Proparse.GETDOUBLE, "get-double", NodeTypesOption.KEYWORD),
  GETEFFECTIVETENANTID(Proparse.GETEFFECTIVETENANTID, "get-effective-tenant-id", NodeTypesOption.KEYWORD),
  GETEFFECTIVETENANTNAME(Proparse.GETEFFECTIVETENANTNAME, "get-effective-tenant-name", NodeTypesOption.KEYWORD),
  GETFILE(Proparse.GETFILE, "get-file", NodeTypesOption.KEYWORD),
  GETFLOAT(Proparse.GETFLOAT, "get-float", NodeTypesOption.KEYWORD),
  GETINT64(Proparse.GETINT64, "get-int64", NodeTypesOption.KEYWORD),
  GETKEYVALUE(Proparse.GETKEYVALUE, "get-key-value", 11, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  GETLONG(Proparse.GETLONG, "get-long", NodeTypesOption.KEYWORD),
  GETPOINTERVALUE(Proparse.GETPOINTERVALUE, "get-pointer-value", NodeTypesOption.KEYWORD),
  GETSHORT(Proparse.GETSHORT, "get-short", NodeTypesOption.KEYWORD),
  GETSIZE(Proparse.GETSIZE, "get-size", NodeTypesOption.KEYWORD),
  GETSTRING(Proparse.GETSTRING, "get-string", NodeTypesOption.KEYWORD),
  GETUNSIGNEDLONG(Proparse.GETUNSIGNEDLONG, "get-unsigned-long", NodeTypesOption.KEYWORD),
  GETUNSIGNEDSHORT(Proparse.GETUNSIGNEDSHORT, "get-unsigned-short", NodeTypesOption.KEYWORD),
  GLOBAL(Proparse.GLOBAL, "global", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  GOON(Proparse.GOON, "go-on", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  GOPENDING(Proparse.GOPENDING, "go-pending", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  GRANT(Proparse.GRANT, "grant", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  GRAPHICEDGE(Proparse.GRAPHICEDGE, "graphic-edge", 9, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  GROUP(Proparse.GROUP, "group", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  GROUPBOX(Proparse.GROUPBOX, "group-box", NodeTypesOption.KEYWORD),
  GTHAN(Proparse.GTHAN, "gt", NodeTypesOption.KEYWORD),
  GUID(Proparse.GUID, "guid", NodeTypesOption.KEYWORD),

  // H
  HANDLE(Proparse.HANDLE, "handle", NodeTypesOption.KEYWORD),
  HAVING(Proparse.HAVING, "having", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  HEADER(Proparse.HEADER, "header", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  HEIGHT(Proparse.HEIGHT, "height", NodeTypesOption.KEYWORD),
  HEIGHTCHARS(Proparse.HEIGHTCHARS, "height-chars", 8, NodeTypesOption.KEYWORD),
  HEIGHTPIXELS(Proparse.HEIGHTPIXELS, "height-pixels", 8, NodeTypesOption.KEYWORD),
  HELP(Proparse.HELP, "help", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  HELPTOPIC(Proparse.HELPTOPIC, "help-topic", NodeTypesOption.KEYWORD),
  HEXDECODE(Proparse.HEXDECODE, "hex-decode", NodeTypesOption.KEYWORD),
  HEXENCODE(Proparse.HEXENCODE, "hex-encode", NodeTypesOption.KEYWORD),
  HIDDEN(Proparse.HIDDEN, "hidden", NodeTypesOption.KEYWORD),
  HIDE(Proparse.HIDE, "hide", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  HINT(Proparse.HINT, "hint", NodeTypesOption.KEYWORD),
  HORIZONTAL(Proparse.HORIZONTAL, "horizontal", 4, NodeTypesOption.KEYWORD),
  HOSTBYTEORDER(Proparse.HOSTBYTEORDER, "host-byte-order", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  HTMLENDOFLINE(Proparse.HTMLENDOFLINE, "html-end-of-line", NodeTypesOption.KEYWORD),
  HTMLFRAMEBEGIN(Proparse.HTMLFRAMEBEGIN, "html-frame-begin", NodeTypesOption.KEYWORD),
  HTMLFRAMEEND(Proparse.HTMLFRAMEEND, "html-frame-end", NodeTypesOption.KEYWORD),
  HTMLHEADERBEGIN(Proparse.HTMLHEADERBEGIN, "html-header-begin", NodeTypesOption.KEYWORD),
  HTMLHEADEREND(Proparse.HTMLHEADEREND, "html-header-end", NodeTypesOption.KEYWORD),
  HTMLTITLEBEGIN(Proparse.HTMLTITLEBEGIN, "html-title-begin", NodeTypesOption.KEYWORD),
  HTMLTITLEEND(Proparse.HTMLTITLEEND, "html-title-end", NodeTypesOption.KEYWORD),

  // I
  IF(Proparse.IF, "if", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  IMAGE(Proparse.IMAGE, "image", NodeTypesOption.KEYWORD),
  IMAGEDOWN(Proparse.IMAGEDOWN, "image-down", NodeTypesOption.KEYWORD),
  IMAGEINSENSITIVE(Proparse.IMAGEINSENSITIVE, "image-insensitive", NodeTypesOption.KEYWORD),
  IMAGESIZE(Proparse.IMAGESIZE, "image-size", NodeTypesOption.KEYWORD),
  IMAGESIZECHARS(Proparse.IMAGESIZECHARS, "image-size-chars", 12, NodeTypesOption.KEYWORD),
  IMAGESIZEPIXELS(Proparse.IMAGESIZEPIXELS, "image-size-pixels", 12, NodeTypesOption.KEYWORD),
  IMAGEUP(Proparse.IMAGEUP, "image-up", NodeTypesOption.KEYWORD),
  IMPLEMENTS(Proparse.IMPLEMENTS, "implements", NodeTypesOption.KEYWORD),
  IMPORT(Proparse.IMPORT, "import", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  IN(Proparse.IN, "in", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  INCREMENTEXCLUSIVEID(Proparse.INCREMENTEXCLUSIVEID, "increment-exclusive-id", NodeTypesOption.KEYWORD),
  INDEX(Proparse.INDEX, "index", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  INDEXEDREPOSITION(Proparse.INDEXEDREPOSITION, "indexed-reposition", NodeTypesOption.KEYWORD),
  INDEXHINT(Proparse.INDEXHINT, "index-hint", NodeTypesOption.KEYWORD),
  INDICATOR(Proparse.INDICATOR, "indicator", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  INFORMATION(Proparse.INFORMATION, "information", 4, NodeTypesOption.KEYWORD),
  INHERITBGCOLOR(Proparse.INHERITBGCOLOR, "inherit-bgcolor", 11, NodeTypesOption.KEYWORD),
  INHERITFGCOLOR(Proparse.INHERITFGCOLOR, "inherit-fgcolor", 11, NodeTypesOption.KEYWORD),
  INHERITS(Proparse.INHERITS, "inherits", NodeTypesOption.KEYWORD),
  INITIALDIR(Proparse.INITIALDIR, "initial-dir", NodeTypesOption.KEYWORD),
  INITIALFILTER(Proparse.INITIALFILTER, "initial-filter", NodeTypesOption.KEYWORD),
  INITIATE(Proparse.INITIATE, "initiate", NodeTypesOption.KEYWORD),
  INNER(Proparse.INNER, "inner", NodeTypesOption.KEYWORD),
  INNERCHARS(Proparse.INNERCHARS, "inner-chars", NodeTypesOption.KEYWORD),
  INNERLINES(Proparse.INNERLINES, "inner-lines", NodeTypesOption.KEYWORD),
  INPUT(Proparse.INPUT, "input", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  INPUTOUTPUT(Proparse.INPUTOUTPUT, "input-output", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  INSERT(Proparse.INSERT, "insert", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  INT64(Proparse.INT64, "int64", NodeTypesOption.KEYWORD),
  INTEGER(Proparse.INTEGER, "integer", 3, NodeTypesOption.KEYWORD),
  INTERFACE(Proparse.INTERFACE, "interface", NodeTypesOption.KEYWORD),
  INTERVAL(Proparse.INTERVAL, "interval", NodeTypesOption.KEYWORD),
  INTO(Proparse.INTO, "into", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  IS(Proparse.IS, "is", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ISATTRSPACE(Proparse.ISATTRSPACE, "is-attr-space", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ISCODEPAGEFIXED(Proparse.ISCODEPAGEFIXED, "is-codepage-fixed", NodeTypesOption.KEYWORD),
  ISCOLUMNCODEPAGE(Proparse.ISCOLUMNCODEPAGE, "is-column-codepage", NodeTypesOption.KEYWORD),
  ISDBMULTITENANT(Proparse.ISDBMULTITENANT, "is-db-multi-tenant", NodeTypesOption.KEYWORD),
  ISLEADBYTE(Proparse.ISLEADBYTE, "is-lead-byte", NodeTypesOption.KEYWORD),
  ISMULTITENANT(Proparse.ISMULTITENANT, "is-multi-tenant", NodeTypesOption.KEYWORD),
  ISODATE(Proparse.ISODATE, "iso-date", NodeTypesOption.KEYWORD),
  ITEM(Proparse.ITEM, "item", NodeTypesOption.KEYWORD),

  // J
  JOIN(Proparse.JOIN, "join", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  JOINBYSQLDB(Proparse.JOINBYSQLDB, "join-by-sqldb", NodeTypesOption.KEYWORD),

  // K
  KBLABEL(Proparse.KBLABEL, "kblabel", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  KEEPMESSAGES(Proparse.KEEPMESSAGES, "keep-messages", NodeTypesOption.KEYWORD),
  KEEPTABORDER(Proparse.KEEPTABORDER, "keep-tab-order", NodeTypesOption.KEYWORD),
  KEY(Proparse.KEY, "key", NodeTypesOption.KEYWORD),
  KEYS(Proparse.KEYS, "keys", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  KEYWORD(Proparse.KEYWORD, "keyword", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  KEYWORDALL(Proparse.KEYWORDALL, "keyword-all", NodeTypesOption.KEYWORD),

  // L
  LABEL(Proparse.LABEL, "label", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LABELBGCOLOR(Proparse.LABELBGCOLOR, "label-bgcolor", 9, NodeTypesOption.KEYWORD),
  LABELDCOLOR(Proparse.LABELDCOLOR, "label-dcolor", 8, NodeTypesOption.KEYWORD),
  LABELFGCOLOR(Proparse.LABELFGCOLOR, "label-fgcolor", 9, NodeTypesOption.KEYWORD),
  LABELFONT(Proparse.LABELFONT, "label-font", NodeTypesOption.KEYWORD),
  LANDSCAPE(Proparse.LANDSCAPE, "landscape", NodeTypesOption.KEYWORD),
  LANGUAGES(Proparse.LANGUAGES, "languages", 8, NodeTypesOption.KEYWORD),
  LARGE(Proparse.LARGE, "large", NodeTypesOption.KEYWORD),
  LARGETOSMALL(Proparse.LARGETOSMALL, "large-to-small", NodeTypesOption.KEYWORD),
  LAST(Proparse.LAST, "last", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LASTBATCH(Proparse.LASTBATCH, "last-batch", NodeTypesOption.KEYWORD),
  LASTEVENT(Proparse.LASTEVENT, "last-event", 9, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LASTFORM(Proparse.LASTFORM, "last-form", NodeTypesOption.KEYWORD),
  LASTOF(Proparse.LASTOF, "last-of", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LDBNAME(Proparse.LDBNAME, "ldbname", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LE(Proparse.LE, "le", NodeTypesOption.KEYWORD),
  LEAKDETECTION(Proparse.LEAKDETECTION, "leak-detection", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LEAVE(Proparse.LEAVE, "leave", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LEFT(Proparse.LEFT, "left", NodeTypesOption.KEYWORD),
  LEFTALIGNED(Proparse.LEFTALIGNED, "left-aligned", 10, NodeTypesOption.KEYWORD),
  LEFTTRIM(Proparse.LEFTTRIM, "left-trim", NodeTypesOption.KEYWORD),
  LENGTH(Proparse.LENGTH, "length", NodeTypesOption.KEYWORD),
  LIBRARY(Proparse.LIBRARY, "library", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LIKE(Proparse.LIKE, "like", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LIKESEQUENTIAL(Proparse.LIKESEQUENTIAL, "like-sequential", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LINECOUNTER(Proparse.LINECOUNTER, "line-counter", 10, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LISTEVENTS(Proparse.LISTEVENTS, "list-events", NodeTypesOption.KEYWORD),
  LISTING(Proparse.LISTING, "listing", 5, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LISTITEMPAIRS(Proparse.LISTITEMPAIRS, "list-item-pairs", NodeTypesOption.KEYWORD),
  LISTITEMS(Proparse.LISTITEMS, "list-items", NodeTypesOption.KEYWORD),
  LISTQUERYATTRS(Proparse.LISTQUERYATTRS, "list-query-attrs", NodeTypesOption.KEYWORD),
  LISTSETATTRS(Proparse.LISTSETATTRS, "list-set-attrs", NodeTypesOption.KEYWORD),
  LISTWIDGETS(Proparse.LISTWIDGETS, "list-widgets", NodeTypesOption.KEYWORD),
  LITTLEENDIAN(Proparse.LITTLEENDIAN, "little-endian", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LOAD(Proparse.LOAD, "load", NodeTypesOption.KEYWORD),
  LOADPICTURE(Proparse.LOADPICTURE, "load-picture", NodeTypesOption.KEYWORD),
  LOBDIR(Proparse.LOBDIR, "lob-dir", NodeTypesOption.KEYWORD),
  LOCKED(Proparse.LOCKED, "locked", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LOG(Proparse.LOG, "log", NodeTypesOption.KEYWORD),
  LOGICAL(Proparse.LOGICAL, "logical", NodeTypesOption.KEYWORD),
  LOGMANAGER(Proparse.LOGMANAGER, "log-manager", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LONG(Proparse.LONG, "long", NodeTypesOption.KEYWORD),
  LONGCHAR(Proparse.LONGCHAR, "longchar", 6, NodeTypesOption.KEYWORD),
  LOOKAHEAD(Proparse.LOOKAHEAD, "lookahead", NodeTypesOption.KEYWORD),
  LOOKUP(Proparse.LOOKUP, "lookup", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LTHAN(Proparse.LTHAN, "lt", NodeTypesOption.KEYWORD),

  // M
  MACHINECLASS(Proparse.MACHINECLASS, "machine-class", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  MAP(Proparse.MAP, "map", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  MARGINEXTRA(Proparse.MARGINEXTRA, "margin-extra", NodeTypesOption.KEYWORD),
  MARKNEW(Proparse.MARKNEW, "mark-new", NodeTypesOption.KEYWORD),
  MARKROWSTATE(Proparse.MARKROWSTATE, "mark-row-state", NodeTypesOption.KEYWORD),
  MATCHES(Proparse.MATCHES, "matches", NodeTypesOption.KEYWORD),
  MAXCHARS(Proparse.MAXCHARS, "max-chars", NodeTypesOption.KEYWORD),
  MAXIMIZE(Proparse.MAXIMIZE, "maximize", NodeTypesOption.KEYWORD),
  MAXIMUMLEVEL(Proparse.MAXIMUMLEVEL, "maximum-level", NodeTypesOption.KEYWORD),
  MAXROWS(Proparse.MAXROWS, "max-rows", NodeTypesOption.KEYWORD),
  MAXSIZE(Proparse.MAXSIZE, "max-size", NodeTypesOption.KEYWORD),
  MAXVALUE(Proparse.MAXVALUE, "max-value", 7, NodeTypesOption.KEYWORD),
  MD5DIGEST(Proparse.MD5DIGEST, "md5-digest", NodeTypesOption.KEYWORD),
  MEMBER(Proparse.MEMBER, "member", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  MEMPTR(Proparse.MEMPTR, "memptr", NodeTypesOption.KEYWORD),
  MENU(Proparse.MENU, "menu", NodeTypesOption.KEYWORD),
  MENUITEM(Proparse.MENUITEM, "menu-item", NodeTypesOption.KEYWORD),
  MERGEBYFIELD(Proparse.MERGEBYFIELD, "merge-by-field", NodeTypesOption.KEYWORD),
  MESSAGE(Proparse.MESSAGE, "message", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  MESSAGEDIGEST(Proparse.MESSAGEDIGEST, "message-digest", NodeTypesOption.KEYWORD),
  MESSAGELINE(Proparse.MESSAGELINE, "message-line", NodeTypesOption.KEYWORD),
  MESSAGELINES(Proparse.MESSAGELINES, "message-lines", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  METHOD(Proparse.METHOD, "method", NodeTypesOption.KEYWORD),
  MINIMUM(Proparse.MINIMUM, "minimum", 3, NodeTypesOption.KEYWORD),
  MINSIZE(Proparse.MINSIZE, "min-size", NodeTypesOption.KEYWORD),
  MINVALUE(Proparse.MINVALUE, "min-value", 7, NodeTypesOption.KEYWORD),
  MONTH(Proparse.MONTH, "month", NodeTypesOption.KEYWORD),
  MOUSE(Proparse.MOUSE, "mouse", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  MOUSEPOINTER(Proparse.MOUSEPOINTER, "mouse-pointer", 7, NodeTypesOption.KEYWORD),
  MTIME(Proparse.MTIME, "mtime", NodeTypesOption.KEYWORD),
  MULTIPLE(Proparse.MULTIPLE, "multiple", NodeTypesOption.KEYWORD),
  MULTIPLEKEY(Proparse.MULTIPLEKEY, "multiple-key", NodeTypesOption.KEYWORD),
  MUSTEXIST(Proparse.MUSTEXIST, "must-exist", NodeTypesOption.KEYWORD),

  // N
  NAMESPACEPREFIX(Proparse.NAMESPACEPREFIX, "namespace-prefix", NodeTypesOption.KEYWORD),
  NAMESPACEURI(Proparse.NAMESPACEURI, "namespace-uri", NodeTypesOption.KEYWORD),
  NATIVE(Proparse.NATIVE, "native", NodeTypesOption.KEYWORD),
  NE(Proparse.NE, "ne", NodeTypesOption.KEYWORD),
  NESTED(Proparse.NESTED, "nested", NodeTypesOption.KEYWORD),
  NEW(Proparse.NEW, "new", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NEWINSTANCE(Proparse.NEWINSTANCE, "new-instance", NodeTypesOption.KEYWORD),
  NEXT(Proparse.NEXT, "next", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NEXTPROMPT(Proparse.NEXTPROMPT, "next-prompt", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NEXTVALUE(Proparse.NEXTVALUE, "next-value", NodeTypesOption.KEYWORD),
  NO(Proparse.NO, "no", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOAPPLY(Proparse.NOAPPLY, "no-apply", NodeTypesOption.KEYWORD),
  NOARRAYMESSAGE(Proparse.NOARRAYMESSAGE, "no-array-message", 10, NodeTypesOption.KEYWORD),
  NOASSIGN(Proparse.NOASSIGN, "no-assign", NodeTypesOption.KEYWORD),
  NOAUTOVALIDATE(Proparse.NOAUTOVALIDATE, "no-auto-validate", NodeTypesOption.KEYWORD),
  NOBINDWHERE(Proparse.NOBINDWHERE, "no-bind-where", NodeTypesOption.KEYWORD),
  NOBOX(Proparse.NOBOX, "no-box", NodeTypesOption.KEYWORD),
  NOCOLUMNSCROLLING(Proparse.NOCOLUMNSCROLLING, "no-column-scrolling", 12, NodeTypesOption.KEYWORD),
  NOCONSOLE(Proparse.NOCONSOLE, "no-console", NodeTypesOption.KEYWORD),
  NOCONVERT(Proparse.NOCONVERT, "no-convert", NodeTypesOption.KEYWORD),
  NOCONVERT3DCOLORS(Proparse.NOCONVERT3DCOLORS, "no-convert-3d-colors", 13, NodeTypesOption.KEYWORD),
  NOCURRENTVALUE(Proparse.NOCURRENTVALUE, "no-current-value", NodeTypesOption.KEYWORD),
  NODEBUG(Proparse.NODEBUG, "no-debug", NodeTypesOption.KEYWORD),
  NODRAG(Proparse.NODRAG, "no-drag", NodeTypesOption.KEYWORD),
  NOECHO(Proparse.NOECHO, "no-echo", NodeTypesOption.KEYWORD),
  NOEMPTYSPACE(Proparse.NOEMPTYSPACE, "no-empty-space", NodeTypesOption.KEYWORD),
  NOERROR(Proparse.NOERROR, "no-error", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOFILL(Proparse.NOFILL, "no-fill", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOFOCUS(Proparse.NOFOCUS, "no-focus", NodeTypesOption.KEYWORD),
  NOHELP(Proparse.NOHELP, "no-help", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOHIDE(Proparse.NOHIDE, "no-hide", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOINDEXHINT(Proparse.NOINDEXHINT, "no-index-hint", NodeTypesOption.KEYWORD),
  NOINHERITBGCOLOR(Proparse.NOINHERITBGCOLOR, "no-inherit-bgcolor", 14, NodeTypesOption.KEYWORD),
  NOINHERITFGCOLOR(Proparse.NOINHERITFGCOLOR, "no-inherit-fgcolor", 14, NodeTypesOption.KEYWORD),
  NOJOINBYSQLDB(Proparse.NOJOINBYSQLDB, "no-join-by-sqldb", NodeTypesOption.KEYWORD),
  NOLABELS(Proparse.NOLABELS, "no-labels", 8, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOLOBS(Proparse.NOLOBS, "no-lobs", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOLOCK(Proparse.NOLOCK, "no-lock", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOLOOKAHEAD(Proparse.NOLOOKAHEAD, "no-lookahead", NodeTypesOption.KEYWORD),
  NOMAP(Proparse.NOMAP, "no-map", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOMESSAGE(Proparse.NOMESSAGE, "no-message", 6, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NONE(Proparse.NONE, "none", NodeTypesOption.KEYWORD),
  NONSERIALIZABLE(Proparse.NONSERIALIZABLE, "non-serializable", NodeTypesOption.KEYWORD),
  NOPAUSE(Proparse.NOPAUSE, "no-pause", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOPREFETCH(Proparse.NOPREFETCH, "no-prefetch", 8, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NORETURNVALUE(Proparse.NORETURNVALUE, "no-return-value", 13, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NORMALIZE(Proparse.NORMALIZE, "normalize", NodeTypesOption.KEYWORD),
  NOROWMARKERS(Proparse.NOROWMARKERS, "no-row-markers", NodeTypesOption.KEYWORD),
  NOSCROLLBARVERTICAL(Proparse.NOSCROLLBARVERTICAL, "no-scrollbar-vertical", 14, NodeTypesOption.KEYWORD),
  NOSEPARATECONNECTION(Proparse.NOSEPARATECONNECTION, "no-separate-connection", NodeTypesOption.KEYWORD),
  NOSEPARATORS(Proparse.NOSEPARATORS, "no-separators", NodeTypesOption.KEYWORD),
  NOT(Proparse.NOT, "not", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOTABSTOP(Proparse.NOTABSTOP, "no-tab-stop", 6, NodeTypesOption.KEYWORD),
  NOUNDERLINE(Proparse.NOUNDERLINE, "no-underline", 6, NodeTypesOption.KEYWORD),
  NOUNDO(Proparse.NOUNDO, "no-undo", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOVALIDATE(Proparse.NOVALIDATE, "no-validate", 6, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOW(Proparse.NOW, "now", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOWAIT(Proparse.NOWAIT, "no-wait", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOWORDWRAP(Proparse.NOWORDWRAP, "no-word-wrap", NodeTypesOption.KEYWORD),
  NULL(Proparse.NULL, "null", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NUMALIASES(Proparse.NUMALIASES, "num-aliases", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NUMCOPIES(Proparse.NUMCOPIES, "num-copies", NodeTypesOption.KEYWORD),
  NUMDBS(Proparse.NUMDBS, "num-dbs", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NUMENTRIES(Proparse.NUMENTRIES, "num-entries", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NUMERIC(Proparse.NUMERIC, "numeric", NodeTypesOption.KEYWORD),
  NUMRESULTS(Proparse.NUMRESULTS, "num-results", NodeTypesOption.KEYWORD),

  // O
  OBJECT(Proparse.OBJECT, "object", NodeTypesOption.KEYWORD),
  OCTETLENGTH(Proparse.OCTETLENGTH, "octet_length", NodeTypesOption.KEYWORD),
  OF(Proparse.OF, "of", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OFF(Proparse.OFF, "off", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OK(Proparse.OK, "ok", NodeTypesOption.KEYWORD),
  OKCANCEL(Proparse.OKCANCEL, "ok-cancel", NodeTypesOption.KEYWORD),
  OLD(Proparse.OLD, "old", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ON(Proparse.ON, "on", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OPEN(Proparse.OPEN, "open", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OPSYS(Proparse.OPSYS, "opsys", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OPTION(Proparse.OPTION, "option", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OPTIONS(Proparse.OPTIONS, "options", NodeTypesOption.KEYWORD),
  OPTIONSFILE(Proparse.OPTIONSFILE, "options-file", NodeTypesOption.KEYWORD),
  OR(Proparse.OR, "or", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ORDEREDJOIN(Proparse.ORDEREDJOIN, "ordered-join", NodeTypesOption.KEYWORD),
  ORDINAL(Proparse.ORDINAL, "ordinal", NodeTypesOption.KEYWORD),
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
  OUTPUT(Proparse.OUTPUT, "output", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OVERLAY(Proparse.OVERLAY, "overlay", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OVERRIDE(Proparse.OVERRIDE, "override", NodeTypesOption.KEYWORD),

  // P
  PACKAGEPRIVATE(Proparse.PACKAGEPRIVATE, "package-private", NodeTypesOption.KEYWORD),
  PACKAGEPROTECTED(Proparse.PACKAGEPROTECTED, "package-protected", NodeTypesOption.KEYWORD),
  PAGE(Proparse.PAGE, "page", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PAGEBOTTOM(Proparse.PAGEBOTTOM, "page-bottom", 8, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PAGED(Proparse.PAGED, "paged", NodeTypesOption.KEYWORD),
  PAGENUMBER(Proparse.PAGENUMBER, "page-number", 8, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PAGESIZE(Proparse.PAGESIZE, "page-size", NodeTypesOption.KEYWORD),
  PAGETOP(Proparse.PAGETOP, "page-top", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PAGEWIDTH(Proparse.PAGEWIDTH, "page-width", 8, NodeTypesOption.KEYWORD),
  PARAMETER(Proparse.PARAMETER, "parameter", 5, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PARENT(Proparse.PARENT, "parent", NodeTypesOption.KEYWORD),
  PARENTFIELDSAFTER(Proparse.PARENTFIELDSAFTER, "parent-fields-after", NodeTypesOption.KEYWORD),
  PARENTFIELDSBEFORE(Proparse.PARENTFIELDSBEFORE, "parent-fields-before", NodeTypesOption.KEYWORD),
  PARENTIDFIELD(Proparse.PARENTIDFIELD, "parent-id-field", NodeTypesOption.KEYWORD),
  PARENTIDRELATION(Proparse.PARENTIDRELATION, "parent-id-relation", NodeTypesOption.KEYWORD),
  PARTIALKEY(Proparse.PARTIALKEY, "partial-key", NodeTypesOption.KEYWORD),
  PASCAL(Proparse.PASCAL, "pascal", NodeTypesOption.KEYWORD),
  PASSWORDFIELD(Proparse.PASSWORDFIELD, "password-field", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PAUSE(Proparse.PAUSE, "pause", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PBEHASHALGORITHM(Proparse.PBEHASHALGORITHM, "pbe-hash-algorithm", 12, NodeTypesOption.KEYWORD),
  PBEKEYROUNDS(Proparse.PBEKEYROUNDS, "pbe-key-rounds", NodeTypesOption.KEYWORD),
  PDBNAME(Proparse.PDBNAME, "pdbname", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PERFORMANCE(Proparse.PERFORMANCE, "performance", 4, NodeTypesOption.KEYWORD),
  PERSISTENT(Proparse.PERSISTENT, "persistent", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PFCOLOR(Proparse.PFCOLOR, "pfcolor", 3, NodeTypesOption.KEYWORD),
  PORTRAIT(Proparse.PORTRAIT, "portrait", NodeTypesOption.KEYWORD),
  POSITION(Proparse.POSITION, "position", NodeTypesOption.KEYWORD),
  PRECISION(Proparse.PRECISION, "precision", NodeTypesOption.KEYWORD),
  PREFERDATASET(Proparse.PREFERDATASET, "prefer-dataset", NodeTypesOption.KEYWORD),
  PREPROCESS(Proparse.PREPROCESS, "preprocess", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PRESELECT(Proparse.PRESELECT, "preselect", 6, NodeTypesOption.KEYWORD),
  PREV(Proparse.PREV, "prev", NodeTypesOption.KEYWORD),
  PRIMARY(Proparse.PRIMARY, "primary", NodeTypesOption.KEYWORD),
  PRINTER(Proparse.PRINTER, "printer", NodeTypesOption.KEYWORD),
  PRINTERSETUP(Proparse.PRINTERSETUP, "printer-setup", NodeTypesOption.KEYWORD),
  PRIVATE(Proparse.PRIVATE, "private", NodeTypesOption.KEYWORD),
  PRIVILEGES(Proparse.PRIVILEGES, "privileges", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PROCEDURE(Proparse.PROCEDURE, "procedure", 5, NodeTypesOption.KEYWORD),
  PROCEDURECALLTYPE(Proparse.PROCEDURECALLTYPE, "procedure-call-type", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  PROCESS(Proparse.PROCESS, "process", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PROCESSARCHITECTURE(Proparse.PROCESSARCHITECTURE, "process-architecture", NodeTypesOption.KEYWORD),
  PROCHANDLE(Proparse.PROCHANDLE, "proc-handle", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PROCSTATUS(Proparse.PROCSTATUS, "proc-status", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PROFILER(Proparse.PROFILER, "profiler", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PROGRAMNAME(Proparse.PROGRAMNAME, "program-name", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PROGRESS(Proparse.PROGRESS, "progress", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PROMPT(Proparse.PROMPT, "prompt", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PROMPTFOR(Proparse.PROMPTFOR, "prompt-for", 8, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PROMSGS(Proparse.PROMSGS, "promsgs", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PROPATH(Proparse.PROPATH, "propath", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PROPERTY(Proparse.PROPERTY, "property", NodeTypesOption.KEYWORD),
  PROTECTED(Proparse.PROTECTED, "protected", NodeTypesOption.KEYWORD),
  PROVERSION(Proparse.PROVERSION, "proversion", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PUBLIC(Proparse.PUBLIC, "public", NodeTypesOption.KEYWORD),
  PUBLISH(Proparse.PUBLISH, "publish", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
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
  QUERY(Proparse.QUERY, "query", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  QUERYCLOSE(Proparse.QUERYCLOSE, "query-close", NodeTypesOption.KEYWORD),
  QUERYOFFEND(Proparse.QUERYOFFEND, "query-off-end", NodeTypesOption.KEYWORD),
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
  READKEY(Proparse.READKEY, "readkey", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  READONLY(Proparse.READONLY, "read-only", NodeTypesOption.KEYWORD),
  REAL(Proparse.REAL, "real", NodeTypesOption.KEYWORD),
  RECID(Proparse.RECID, "recid", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  RECORDLENGTH(Proparse.RECORDLENGTH, "record-length", 10, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  RECTANGLE(Proparse.RECTANGLE, "rectangle", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  RECURSIVE(Proparse.RECURSIVE, "recursive", NodeTypesOption.KEYWORD),
  REFERENCEONLY(Proparse.REFERENCEONLY, "reference-only", NodeTypesOption.KEYWORD),
  REJECTED(Proparse.REJECTED, "rejected", NodeTypesOption.KEYWORD),
  RELATIONFIELDS(Proparse.RELATIONFIELDS, "relation-fields", 11, NodeTypesOption.KEYWORD),
  RELEASE(Proparse.RELEASE, "release", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  REPEAT(Proparse.REPEAT, "repeat", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  REPLACE(Proparse.REPLACE, "replace", NodeTypesOption.KEYWORD),
  REPLICATIONCREATE(Proparse.REPLICATIONCREATE, "replication-create", NodeTypesOption.KEYWORD),
  REPLICATIONDELETE(Proparse.REPLICATIONDELETE, "replication-delete", NodeTypesOption.KEYWORD),
  REPLICATIONWRITE(Proparse.REPLICATIONWRITE, "replication-write", NodeTypesOption.KEYWORD),
  REPOSITION(Proparse.REPOSITION, "reposition", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  REPOSITIONBACKWARDS(Proparse.REPOSITIONBACKWARDS, "reposition-backwards", 15, NodeTypesOption.KEYWORD),
  REPOSITIONFORWARDS(Proparse.REPOSITIONFORWARDS, "reposition-forwards", 15, NodeTypesOption.KEYWORD),
  REPOSITIONTOROW(Proparse.REPOSITIONTOROW, "reposition-to-row", NodeTypesOption.KEYWORD),
  REPOSITIONTOROWID(Proparse.REPOSITIONTOROWID, "reposition-to-rowid", NodeTypesOption.KEYWORD),
  REQUEST(Proparse.REQUEST, "request", NodeTypesOption.KEYWORD),
  RESTARTROW(Proparse.RESTARTROW, "restart-row", NodeTypesOption.KEYWORD),
  RESULT(Proparse.RESULT, "result", NodeTypesOption.KEYWORD),
  RETAIN(Proparse.RETAIN, "retain", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  RETAINSHAPE(Proparse.RETAINSHAPE, "retain-shape", 8, NodeTypesOption.KEYWORD),
  RETRY(Proparse.RETRY, "retry", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  RETRYCANCEL(Proparse.RETRYCANCEL, "retry-cancel", NodeTypesOption.KEYWORD),
  RETURN(Proparse.RETURN, "return", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  RETURNS(Proparse.RETURNS, "returns", NodeTypesOption.KEYWORD),
  RETURNTOSTARTDIR(Proparse.RETURNTOSTARTDIR, "return-to-start-dir", 18, NodeTypesOption.KEYWORD),
  RETURNVALUE(Proparse.RETURNVALUE, "return-value", 10, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  REVERSEFROM(Proparse.REVERSEFROM, "reverse-from", NodeTypesOption.KEYWORD),
  REVERT(Proparse.REVERT, "revert", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  REVOKE(Proparse.REVOKE, "revoke", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  RGBVALUE(Proparse.RGBVALUE, "rgb-value", 5, NodeTypesOption.KEYWORD),
  RIGHT(Proparse.RIGHT, "right", NodeTypesOption.KEYWORD),
  RIGHTALIGNED(Proparse.RIGHTALIGNED, "right-aligned", 11, NodeTypesOption.KEYWORD),
  RIGHTTRIM(Proparse.RIGHTTRIM, "right-trim", NodeTypesOption.KEYWORD),
  RINDEX(Proparse.RINDEX, "r-index", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ROUND(Proparse.ROUND, "round", NodeTypesOption.KEYWORD),
  ROUNDED(Proparse.ROUNDED, "rounded", NodeTypesOption.KEYWORD),
  ROUTINELEVEL(Proparse.ROUTINELEVEL, "routine-level", NodeTypesOption.KEYWORD),
  ROW(Proparse.ROW, "row", NodeTypesOption.KEYWORD),
  ROWCREATED(Proparse.ROWCREATED, "row-created", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ROWDELETED(Proparse.ROWDELETED, "row-deleted", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ROWHEIGHTPIXELS(Proparse.ROWHEIGHTPIXELS, "row-height-pixels", 12, NodeTypesOption.KEYWORD),
  ROWID(Proparse.ROWID, "rowid", NodeTypesOption.KEYWORD),
  ROWMODIFIED(Proparse.ROWMODIFIED, "row-modified", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ROWOF(Proparse.ROWOF, "row-of", NodeTypesOption.KEYWORD),
  ROWSTATE(Proparse.ROWSTATE, "row-state", NodeTypesOption.KEYWORD),
  ROWUNMODIFIED(Proparse.ROWUNMODIFIED, "row-unmodified", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  RULE(Proparse.RULE, "rule", NodeTypesOption.KEYWORD),
  RUN(Proparse.RUN, "run", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  RUNPROCEDURE(Proparse.RUNPROCEDURE, "run-procedure", 8, NodeTypesOption.KEYWORD),

  // S
  SAVE(Proparse.SAVE, "save", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SAVEAS(Proparse.SAVEAS, "save-as", NodeTypesOption.KEYWORD),
  SAVEWHERESTRING(Proparse.SAVEWHERESTRING, "save-where-string", NodeTypesOption.KEYWORD),
  SAXATTRIBUTES(Proparse.SAXATTRIBUTES, "sax-attributes", NodeTypesOption.KEYWORD),
  SAXCOMPLETE(Proparse.SAXCOMPLETE, "sax-complete", 10, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
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
  SCHEMA(Proparse.SCHEMA, "schema", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SCREEN(Proparse.SCREEN, "screen", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SCREENIO(Proparse.SCREENIO, "screen-io", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SCREENLINES(Proparse.SCREENLINES, "screen-lines", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SCREENVALUE(Proparse.SCREENVALUE, "screen-value", 10, NodeTypesOption.KEYWORD),
  SCROLL(Proparse.SCROLL, "scroll", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SCROLLABLE(Proparse.SCROLLABLE, "scrollable", NodeTypesOption.KEYWORD),
  SCROLLBARHORIZONTAL(Proparse.SCROLLBARHORIZONTAL, "scrollbar-horizontal", 11, NodeTypesOption.KEYWORD),
  SCROLLBARVERTICAL(Proparse.SCROLLBARVERTICAL, "scrollbar-vertical", 11, NodeTypesOption.KEYWORD),
  SCROLLING(Proparse.SCROLLING, "scrolling", NodeTypesOption.KEYWORD),
  SDBNAME(Proparse.SDBNAME, "sdbname", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SEARCH(Proparse.SEARCH, "search", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SEARCHSELF(Proparse.SEARCHSELF, "search-self", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SEARCHTARGET(Proparse.SEARCHTARGET, "search-target", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SECTION(Proparse.SECTION, "section", NodeTypesOption.KEYWORD),
  SECURITYPOLICY(Proparse.SECURITYPOLICY, "security-policy", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SEEK(Proparse.SEEK, "seek", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SELECT(Proparse.SELECT, "select", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SELECTION(Proparse.SELECTION, "selection", NodeTypesOption.KEYWORD),
  SELECTIONLIST(Proparse.SELECTIONLIST, "selection-list", NodeTypesOption.KEYWORD),
  SELF(Proparse.SELF, "self", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SEND(Proparse.SEND, "send", NodeTypesOption.KEYWORD),
  SENSITIVE(Proparse.SENSITIVE, "sensitive", NodeTypesOption.KEYWORD),
  SEPARATECONNECTION(Proparse.SEPARATECONNECTION, "separate-connection", NodeTypesOption.KEYWORD),
  SEPARATORS(Proparse.SEPARATORS, "separators", NodeTypesOption.KEYWORD),
  SERIALIZABLE(Proparse.SERIALIZABLE, "serializable", NodeTypesOption.KEYWORD),
  SERIALIZEHIDDEN(Proparse.SERIALIZEHIDDEN, "serialize-hidden", NodeTypesOption.KEYWORD),
  SERIALIZENAME(Proparse.SERIALIZENAME, "serialize-name", NodeTypesOption.KEYWORD),
  SERVER(Proparse.SERVER, "server", NodeTypesOption.KEYWORD),
  SERVERSOCKET(Proparse.SERVERSOCKET, "server-socket", NodeTypesOption.KEYWORD),
  SESSION(Proparse.SESSION, "session", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SET(Proparse.SET, "set", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SETATTRCALLTYPE(Proparse.SETATTRCALLTYPE, "set-attr-call-type", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SETBYTEORDER(Proparse.SETBYTEORDER, "set-byte-order", NodeTypesOption.KEYWORD),
  SETCONTENTS(Proparse.SETCONTENTS, "set-contents", NodeTypesOption.KEYWORD),
  SETDBCLIENT(Proparse.SETDBCLIENT, "set-db-client", NodeTypesOption.KEYWORD),
  SETEFFECTIVETENANT(Proparse.SETEFFECTIVETENANT, "set-effective-tenant", NodeTypesOption.KEYWORD),
  SETPOINTERVALUE(Proparse.SETPOINTERVALUE, "set-pointer-value", 15, NodeTypesOption.KEYWORD),
  SETSIZE(Proparse.SETSIZE, "set-size", NodeTypesOption.KEYWORD),
  SETUSERID(Proparse.SETUSERID, "setuserid", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SHA1DIGEST(Proparse.SHA1DIGEST, "sha1-digest", NodeTypesOption.KEYWORD),
  SHARED(Proparse.SHARED, "shared", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SHARELOCK(Proparse.SHARELOCK, "share-lock", 5, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SHORT(Proparse.SHORT, "short", NodeTypesOption.KEYWORD),
  SHOWSTATS(Proparse.SHOWSTATS, "show-stats", 9, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SIGNATURE(Proparse.SIGNATURE, "signature", NodeTypesOption.KEYWORD),
  SILENT(Proparse.SILENT, "silent", NodeTypesOption.KEYWORD),
  SIMPLE(Proparse.SIMPLE, "simple", NodeTypesOption.KEYWORD),
  SINGLE(Proparse.SINGLE, "single", NodeTypesOption.KEYWORD),
  SINGLERUN(Proparse.SINGLERUN, "single-run", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SINGLETON(Proparse.SINGLETON, "singleton", NodeTypesOption.KEYWORD),
  SIZE(Proparse.SIZE, "size", NodeTypesOption.KEYWORD),
  SIZECHARS(Proparse.SIZECHARS, "size-chars", 6, NodeTypesOption.KEYWORD),
  SIZEPIXELS(Proparse.SIZEPIXELS, "size-pixels", 6, NodeTypesOption.KEYWORD),
  SKIP(Proparse.SKIP, "skip", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SKIPDELETEDRECORD(Proparse.SKIPDELETEDRECORD, "skip-deleted-record", 16, NodeTypesOption.KEYWORD),
  SKIPGROUPDUPLICATES(Proparse.SKIPGROUPDUPLICATES, "skip-group-duplicates", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  SLIDER(Proparse.SLIDER, "slider", NodeTypesOption.KEYWORD),
  SMALLINT(Proparse.SMALLINT, "smallint", NodeTypesOption.KEYWORD),
  SOAPHEADER(Proparse.SOAPHEADER, "soap-header", NodeTypesOption.KEYWORD),
  SOAPHEADERENTRYREF(Proparse.SOAPHEADERENTRYREF, "soap-header-entryref", NodeTypesOption.KEYWORD),
  SOCKET(Proparse.SOCKET, "socket", NodeTypesOption.KEYWORD),
  SOME(Proparse.SOME, "some", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SORT(Proparse.SORT, "sort", NodeTypesOption.KEYWORD),
  SOURCE(Proparse.SOURCE, "source", NodeTypesOption.KEYWORD),
  SOURCEPROCEDURE(Proparse.SOURCEPROCEDURE, "source-procedure", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SPACE(Proparse.SPACE, "space", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SQL(Proparse.SQL, "sql", NodeTypesOption.KEYWORD),
  SQRT(Proparse.SQRT, "sqrt", NodeTypesOption.KEYWORD),
  SSLSERVERNAME(Proparse.SSLSERVERNAME, "ssl-server-name", NodeTypesOption.KEYWORD),
  START(Proparse.START, "start", NodeTypesOption.KEYWORD),
  STARTING(Proparse.STARTING, "starting", NodeTypesOption.KEYWORD),
  STARTMOVE(Proparse.STARTMOVE, "start-move", NodeTypesOption.KEYWORD),
  STARTRESIZE(Proparse.STARTRESIZE, "start-resize", NodeTypesOption.KEYWORD),
  STARTROWRESIZE(Proparse.STARTROWRESIZE, "start-row-resize", NodeTypesOption.KEYWORD),
  STATIC(Proparse.STATIC, "static", NodeTypesOption.KEYWORD),
  STATUS(Proparse.STATUS, "status", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  STDCALL(Proparse.STDCALL, "stdcall", NodeTypesOption.KEYWORD),
  STOMPDETECTION(Proparse.STOMPDETECTION, "stomp-detection", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  STOMPFREQUENCY(Proparse.STOMPFREQUENCY, "stomp-frequency", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  STOP(Proparse.STOP, "stop", NodeTypesOption.KEYWORD),
  STOPAFTER(Proparse.STOPAFTER, "stop-after", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  STOREDPROCEDURE(Proparse.STOREDPROCEDURE, "stored-procedure", 11, NodeTypesOption.KEYWORD),
  STREAM(Proparse.STREAM, "stream", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  STREAMHANDLE(Proparse.STREAMHANDLE, "stream-handle", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  STREAMIO(Proparse.STREAMIO, "stream-io", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  STRETCHTOFIT(Proparse.STRETCHTOFIT, "stretch-to-fit", NodeTypesOption.KEYWORD),
  STRING(Proparse.STRING, "string", NodeTypesOption.KEYWORD),
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
  SUM(Proparse.SUM, "sum", NodeTypesOption.KEYWORD),
  SUMMARY(Proparse.SUMMARY, "summary", NodeTypesOption.KEYWORD),
  SUPER(Proparse.SUPER, "super", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SYMMETRICENCRYPTIONALGORITHM(Proparse.SYMMETRICENCRYPTIONALGORITHM, "symmetric-encryption-algorithm",
      NodeTypesOption.KEYWORD),
  SYMMETRICENCRYPTIONIV(Proparse.SYMMETRICENCRYPTIONIV, "symmetric-encryption-iv", NodeTypesOption.KEYWORD),
  SYMMETRICENCRYPTIONKEY(Proparse.SYMMETRICENCRYPTIONKEY, "symmetric-encryption-key", NodeTypesOption.KEYWORD),
  SYMMETRICSUPPORT(Proparse.SYMMETRICSUPPORT, "symmetric-support", NodeTypesOption.KEYWORD),
  SYSTEMDIALOG(Proparse.SYSTEMDIALOG, "system-dialog", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SYSTEMHELP(Proparse.SYSTEMHELP, "system-help", NodeTypesOption.KEYWORD),

  // T
  TABLE(Proparse.TABLE, "table", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TABLEHANDLE(Proparse.TABLEHANDLE, "table-handle", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TABLENUMBER(Proparse.TABLENUMBER, "table-number", 9, NodeTypesOption.KEYWORD),
  TABLESCAN(Proparse.TABLESCAN, "table-scan", NodeTypesOption.KEYWORD),
  TARGET(Proparse.TARGET, "target", NodeTypesOption.KEYWORD),
  TARGETPROCEDURE(Proparse.TARGETPROCEDURE, "target-procedure", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TEMPTABLE(Proparse.TEMPTABLE, "temp-table", NodeTypesOption.KEYWORD),
  TENANT(Proparse.TENANT, "tenant", NodeTypesOption.KEYWORD),
  TENANTID(Proparse.TENANTID, "tenant-id", NodeTypesOption.KEYWORD),
  TENANTNAME(Proparse.TENANTNAME, "tenant-name", NodeTypesOption.KEYWORD),
  TENANTNAMETOID(Proparse.TENANTNAMETOID, "tenant-name-to-id", NodeTypesOption.KEYWORD),
  TENANTWHERE(Proparse.TENANTWHERE, "tenant-where", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TERMINATE(Proparse.TERMINATE, "terminate", NodeTypesOption.KEYWORD),
  TEXT(Proparse.TEXT, "text", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TEXTCURSOR(Proparse.TEXTCURSOR, "text-cursor", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TEXTSEGGROWTH(Proparse.TEXTSEGGROWTH, "text-seg-growth", 8, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  THEN(Proparse.THEN, "then", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  THISOBJECT(Proparse.THISOBJECT, "this-object", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  THISPROCEDURE(Proparse.THISPROCEDURE, "this-procedure", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  THREED(Proparse.THREED, "three-d", NodeTypesOption.KEYWORD),
  THROW(Proparse.THROW, "throw", NodeTypesOption.KEYWORD),
  TICMARKS(Proparse.TICMARKS, "tic-marks", NodeTypesOption.KEYWORD),
  TIME(Proparse.TIME, "time", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TIMEZONE(Proparse.TIMEZONE, "timezone", NodeTypesOption.KEYWORD),
  TITLE(Proparse.TITLE, "title", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TO(Proparse.TO, "to", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TODAY(Proparse.TODAY, "today", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TOGGLEBOX(Proparse.TOGGLEBOX, "toggle-box", NodeTypesOption.KEYWORD),
  TOOLTIP(Proparse.TOOLTIP, "tooltip", NodeTypesOption.KEYWORD),
  TOP(Proparse.TOP, "top", NodeTypesOption.KEYWORD),
  TOPIC(Proparse.TOPIC, "topic", NodeTypesOption.KEYWORD),
  TOPNAVQUERY(Proparse.TOPNAVQUERY, "top-nav-query", NodeTypesOption.KEYWORD),
  TOPONLY(Proparse.TOPONLY, "top-only", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TOROWID(Proparse.TOROWID, "to-rowid", NodeTypesOption.KEYWORD),
  TOTAL(Proparse.TOTAL, "total", NodeTypesOption.KEYWORD),
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
  TYPEOF(Proparse.TYPEOF, "type-of", NodeTypesOption.KEYWORD),

  // U
  UNBOX(Proparse.UNBOX, "unbox", NodeTypesOption.KEYWORD),
  UNBUFFERED(Proparse.UNBUFFERED, "unbuffered", 6, NodeTypesOption.KEYWORD),
  UNDERLINE(Proparse.UNDERLINE, "underline", 6, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  UNDO(Proparse.UNDO, "undo", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  UNFORMATTED(Proparse.UNFORMATTED, "unformatted", 6, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  UNION(Proparse.UNION, "union", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  UNIQUE(Proparse.UNIQUE, "unique", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  UNIQUEMATCH(Proparse.UNIQUEMATCH, "unique-match", NodeTypesOption.KEYWORD),
  UNIX(Proparse.UNIX, "unix", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  UNLESSHIDDEN(Proparse.UNLESSHIDDEN, "unless-hidden", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  UNLOAD(Proparse.UNLOAD, "unload", NodeTypesOption.KEYWORD),
  UNSIGNEDBYTE(Proparse.UNSIGNEDBYTE, "unsigned-byte", NodeTypesOption.KEYWORD),
  UNSIGNEDSHORT(Proparse.UNSIGNEDSHORT, "unsigned-short", NodeTypesOption.KEYWORD),
  UNSUBSCRIBE(Proparse.UNSUBSCRIBE, "unsubscribe", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  UP(Proparse.UP, "up", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  UPDATE(Proparse.UPDATE, "update", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  URLDECODE(Proparse.URLDECODE, "url-decode", NodeTypesOption.KEYWORD),
  URLENCODE(Proparse.URLENCODE, "url-encode", NodeTypesOption.KEYWORD),
  USE(Proparse.USE, "use", NodeTypesOption.KEYWORD),
  USEDICTEXPS(Proparse.USEDICTEXPS, "use-dict-exps", 7, NodeTypesOption.KEYWORD),
  USEFILENAME(Proparse.USEFILENAME, "use-filename", NodeTypesOption.KEYWORD),
  USEINDEX(Proparse.USEINDEX, "use-index", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  USER(Proparse.USER, "user", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  USEREVVIDEO(Proparse.USEREVVIDEO, "use-revvideo", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  USERID(Proparse.USERID, "userid", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  USETEXT(Proparse.USETEXT, "use-text", NodeTypesOption.KEYWORD),
  USEUNDERLINE(Proparse.USEUNDERLINE, "use-underline", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  USEWIDGETPOOL(Proparse.USEWIDGETPOOL, "use-widget-pool", NodeTypesOption.KEYWORD),
  USING(Proparse.USING, "using", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),

  // V
  V6FRAME(Proparse.V6FRAME, "v6frame", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  VALIDATE(Proparse.VALIDATE, "validate", NodeTypesOption.KEYWORD),
  VALIDEVENT(Proparse.VALIDEVENT, "valid-event", NodeTypesOption.KEYWORD),
  VALIDHANDLE(Proparse.VALIDHANDLE, "valid-handle", NodeTypesOption.KEYWORD),
  VALIDOBJECT(Proparse.VALIDOBJECT, "valid-object", NodeTypesOption.KEYWORD),
  VALUE(Proparse.VALUE, "value", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  VALUECHANGED(Proparse.VALUECHANGED, "value-changed", NodeTypesOption.KEYWORD),
  VALUES(Proparse.VALUES, "values", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  VARIABLE(Proparse.VARIABLE, "variable", 4, NodeTypesOption.KEYWORD),
  VERBOSE(Proparse.VERBOSE, "verbose", 4, NodeTypesOption.KEYWORD),
  VERTICAL(Proparse.VERTICAL, "vertical", 4, NodeTypesOption.KEYWORD),
  VIEW(Proparse.VIEW, "view", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  VIEWAS(Proparse.VIEWAS, "view-as", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  VISIBLE(Proparse.VISIBLE, "visible", NodeTypesOption.KEYWORD),
  VMS(Proparse.VMS, "vms", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  VOID(Proparse.VOID, "void", NodeTypesOption.KEYWORD),

  // W
  WAIT(Proparse.WAIT, "wait", NodeTypesOption.KEYWORD),
  WAITFOR(Proparse.WAITFOR, "wait-for", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WARNING(Proparse.WARNING, "warning", NodeTypesOption.KEYWORD),
  WEBCONTEXT(Proparse.WEBCONTEXT, "web-context", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WEEKDAY(Proparse.WEEKDAY, "weekday", NodeTypesOption.KEYWORD),
  WHEN(Proparse.WHEN, "when", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WHERE(Proparse.WHERE, "where", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WHILE(Proparse.WHILE, "while", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WIDGET(Proparse.WIDGET, "widget", NodeTypesOption.KEYWORD),
  WIDGETHANDLE(Proparse.WIDGETHANDLE, "widget-handle", 8, NodeTypesOption.KEYWORD),
  WIDGETID(Proparse.WIDGETID, "widget-id", NodeTypesOption.KEYWORD),
  WIDGETPOOL(Proparse.WIDGETPOOL, "widget-pool", NodeTypesOption.KEYWORD),
  WIDTH(Proparse.WIDTH, "width", NodeTypesOption.KEYWORD),
  WIDTHCHARS(Proparse.WIDTHCHARS, "width-chars", 7, NodeTypesOption.KEYWORD),
  WIDTHPIXELS(Proparse.WIDTHPIXELS, "width-pixels", 7, NodeTypesOption.KEYWORD),
  WINDOW(Proparse.WINDOW, "window", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WINDOWDELAYEDMINIMIZE(Proparse.WINDOWDELAYEDMINIMIZE, "window-delayed-minimize", 18, NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  WINDOWMAXIMIZED(Proparse.WINDOWMAXIMIZED, "window-maximized", 12, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WINDOWMINIMIZED(Proparse.WINDOWMINIMIZED, "window-minimized", 12, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WINDOWNAME(Proparse.WINDOWNAME, "window-name", NodeTypesOption.KEYWORD),
  WINDOWNORMAL(Proparse.WINDOWNORMAL, "window-normal", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WITH(Proparse.WITH, "with", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WORDINDEX(Proparse.WORDINDEX, "word-index", NodeTypesOption.KEYWORD),
  WRITE(Proparse.WRITE, "write", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),

  // X
  X(Proparse.X, "x", NodeTypesOption.KEYWORD),
  XCODE(Proparse.XCODE, "xcode", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  XDOCUMENT(Proparse.XDOCUMENT, "x-document", NodeTypesOption.KEYWORD),
  XMLDATATYPE(Proparse.XMLDATATYPE, "xml-data-type", NodeTypesOption.KEYWORD),
  XMLNODENAME(Proparse.XMLNODENAME, "xml-node-name", NodeTypesOption.KEYWORD),
  XMLNODETYPE(Proparse.XMLNODETYPE, "xml-node-type", NodeTypesOption.KEYWORD),
  XNODEREF(Proparse.XNODEREF, "x-noderef", NodeTypesOption.KEYWORD),
  XOF(Proparse.XOF, "x-of", NodeTypesOption.KEYWORD),
  XOR(Proparse.XOR, "xor", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  XREF(Proparse.XREF, "xref", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  XREFXML(Proparse.XREFXML, "xref-xml", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),

  // Y
  Y(Proparse.Y, "y", NodeTypesOption.KEYWORD),
  YEAR(Proparse.YEAR, "year", NodeTypesOption.KEYWORD),
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
      BUFFERTENANTNAME, //
      BUFFERTENANTID, //
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
      GETEFFECTIVETENANTNAME,//
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
      VOID
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

  public static void main(String[] args) throws IOException {
    try (PrintStream output = new PrintStream("src/main/antlr4/imports/keywords.g4")) {
      generateKeywordsG4(output);
    }
  }
}
