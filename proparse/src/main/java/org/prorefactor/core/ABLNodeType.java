/********************************************************************************
 * Copyright (c) 2015-2018 Riverside Software
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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.antlr.v4.runtime.Token;

import com.google.common.base.Strings;
import org.prorefactor.proparse.antlr4.Proparse;

public enum ABLNodeType {
  // Placeholders and unknown tokens
  EMPTY_NODE(-1000, NodeTypesOption.PLACEHOLDER),
  INVALID_NODE(Token.INVALID_TYPE, NodeTypesOption.PLACEHOLDER),
  EOF_ANTLR4(Token.EOF, NodeTypesOption.PLACEHOLDER),
  INCLUDEDIRECTIVE(Proparse.INCLUDEDIRECTIVE, NodeTypesOption.PLACEHOLDER),

  // A
  AACBIT(Proparse.AACBIT, "_cbit", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  AACONTROL(Proparse.AACONTROL, "_control", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  AALIST(Proparse.AALIST, "_list", NodeTypesOption.KEYWORD),
  AAMEMORY(Proparse.AAMEMORY, "_memory", NodeTypesOption.KEYWORD, NodeTypesOption.SYSHDL),
  AAMSG(Proparse.AAMSG, "_msg", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  AAPCONTROL(Proparse.AAPCONTROL, "_pcontrol", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  AASERIAL(Proparse.AASERIAL, "_serial-num", 7, NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  AATRACE(Proparse.AATRACE, "_trace", NodeTypesOption.KEYWORD),
  ABSOLUTE(Proparse.ABSOLUTE, "absolute", 3, NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  ABSTRACT(Proparse.ABSTRACT, "abstract", NodeTypesOption.KEYWORD),
  ACCELERATOR(Proparse.ACCELERATOR, "accelerator", NodeTypesOption.KEYWORD),
  ACCUMULATE(Proparse.ACCUMULATE, "accumulate", 5, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ACTIVEFORM(Proparse.ACTIVEFORM, "active-form", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.SYSHDL),
  ACTIVEWINDOW(Proparse.ACTIVEWINDOW, "active-window", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.SYSHDL),
  ADD(Proparse.ADD, "add", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ADDINTERVAL(Proparse.ADDINTERVAL, "add-interval", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  ADVISE(Proparse.ADVISE, "advise", NodeTypesOption.KEYWORD),
  ALERTBOX(Proparse.ALERTBOX, "alert-box", NodeTypesOption.KEYWORD),
  ALIAS(Proparse.ALIAS, "alias", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  ALL(Proparse.ALL, "all", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ALLOWREPLICATION(Proparse.ALLOWREPLICATION, "allow-replication", NodeTypesOption.KEYWORD),
  ALTER(Proparse.ALTER, "alter", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ALTERNATEKEY(Proparse.ALTERNATEKEY, "alternate-key", NodeTypesOption.KEYWORD),
  AMBIGUOUS(Proparse.AMBIGUOUS, "ambiguous", 5, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
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
  ANALYZE(Proparse.ANALYZE, "analyze", 6, NodeTypesOption.KEYWORD),
  AND(Proparse.AND, "and", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ANNOTATION(Proparse.ANNOTATION),
  ANSIONLY(Proparse.ANSIONLY, "ansi-only", NodeTypesOption.KEYWORD),
  ANY(Proparse.ANY, "any", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ANYWHERE(Proparse.ANYWHERE, "anywhere", NodeTypesOption.KEYWORD),
  APPEND(Proparse.APPEND, "append", NodeTypesOption.KEYWORD),
  APPLICATION(Proparse.APPLICATION, "application", NodeTypesOption.KEYWORD),
  APPLY(Proparse.APPLY, "apply", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ARRAYMESSAGE(Proparse.ARRAYMESSAGE, "array-message", 7, NodeTypesOption.KEYWORD),
  AS(Proparse.AS, "as", NodeTypesOption.KEYWORD),
  ASC(Proparse.ASC, "asc", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  ASCENDING(Proparse.ASCENDING, "ascending", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  ASKOVERWRITE(Proparse.ASKOVERWRITE, "ask-overwrite", NodeTypesOption.KEYWORD),
  ASSEMBLY(Proparse.ASSEMBLY, "assembly", NodeTypesOption.KEYWORD),
  ASSIGN(Proparse.ASSIGN, "assign", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ASSIGN_DYNAMIC_NEW(Proparse.Assign_dynamic_new, NodeTypesOption.STRUCTURE),
  ASYNCHRONOUS(Proparse.ASYNCHRONOUS, "asynchronous", NodeTypesOption.KEYWORD),
  AT(Proparse.AT, "at", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ATTACHMENT(Proparse.ATTACHMENT, "attachment", 6, NodeTypesOption.KEYWORD),
  ATTRSPACE(Proparse.ATTRSPACE, "attr-space", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  AUDITCONTROL(Proparse.AUDITCONTROL, "audit-control", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.SYSHDL),
  AUDITENABLED(Proparse.AUDITENABLED, "audit-enabled", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  AUDITPOLICY(Proparse.AUDITPOLICY, "audit-policy", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.SYSHDL),
  AUTHORIZATION(Proparse.AUTHORIZATION, "authorization", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  AUTOCOMPLETION(Proparse.AUTOCOMPLETION, "auto-completion", 9, NodeTypesOption.KEYWORD),
  AUTOENDKEY(Proparse.AUTOENDKEY, "auto-end-key", "auto-endkey", NodeTypesOption.KEYWORD),
  AUTOGO(Proparse.AUTOGO, "auto-go", NodeTypesOption.KEYWORD),
  AUTOMATIC(Proparse.AUTOMATIC, "automatic", NodeTypesOption.KEYWORD),
  AUTORETURN(Proparse.AUTORETURN, "auto-return", 8, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  AVAILABLE(Proparse.AVAILABLE, "available", 5, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  AVERAGE(Proparse.AVERAGE, "average", 3, NodeTypesOption.KEYWORD),
  AVG(Proparse.AVG, "avg", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  AGGREGATE_PHRASE(Proparse.Aggregate_phrase, NodeTypesOption.STRUCTURE),
  ARRAY_SUBSCRIPT(Proparse.Array_subscript, NodeTypesOption.STRUCTURE),
  ASSIGN_FROM_BUFFER(Proparse.Assign_from_buffer, NodeTypesOption.STRUCTURE),
  AUTOMATION_OBJECT(Proparse.Automationobject, NodeTypesOption.STRUCTURE),

  // B
  BACKGROUND(Proparse.BACKGROUND, "background", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BACKSLASH(Proparse.BACKSLASH, "\\", NodeTypesOption.SYMBOL),
  BACKTICK(Proparse.BACKTICK, "`", NodeTypesOption.SYMBOL),
  BACKWARDS(Proparse.BACKWARDS, "backwards", 8, NodeTypesOption.KEYWORD),
  BASE64(Proparse.BASE64, "base64", NodeTypesOption.KEYWORD),
  BASE64DECODE(Proparse.BASE64DECODE, "base64-decode", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  BASE64ENCODE(Proparse.BASE64ENCODE, "base64-encode", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  BASEKEY(Proparse.BASEKEY, "base-key", NodeTypesOption.KEYWORD),
  BATCHSIZE(Proparse.BATCHSIZE, "batch-size", NodeTypesOption.KEYWORD),
  BEFOREHIDE(Proparse.BEFOREHIDE, "before-hide", 8, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BEFORETABLE(Proparse.BEFORETABLE, "before-table", NodeTypesOption.KEYWORD),
  BEGINS(Proparse.BEGINS, "begins", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BELL(Proparse.BELL, "bell", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BETWEEN(Proparse.BETWEEN, "between", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BGCOLOR(Proparse.BGCOLOR, "bgcolor", 3, NodeTypesOption.KEYWORD),
  BIGENDIAN(Proparse.BIGENDIAN, "big-endian", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BIGINT(Proparse.BIGINT, "bigint", NodeTypesOption.KEYWORD),
  BINARY(Proparse.BINARY, "binary", NodeTypesOption.KEYWORD),
  BIND(Proparse.BIND, "bind", NodeTypesOption.KEYWORD),
  BINDWHERE(Proparse.BINDWHERE, "bind-where", NodeTypesOption.KEYWORD),
  BLANK(Proparse.BLANK, "blank", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BLOB(Proparse.BLOB, "blob", NodeTypesOption.KEYWORD),
  BLOCK_LABEL(Proparse.BLOCK_LABEL, NodeTypesOption.STRUCTURE),
  BLOCKLEVEL(Proparse.BLOCKLEVEL, "block-level", NodeTypesOption.KEYWORD),
  BOTH(Proparse.BOTH, "both", NodeTypesOption.KEYWORD),
  BOTTOM(Proparse.BOTTOM, "bottom", NodeTypesOption.KEYWORD),
  BOX(Proparse.BOX, "box", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  BREAK(Proparse.BREAK, "break", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BROWSE(Proparse.BROWSE, "browse", NodeTypesOption.KEYWORD),
  BTOS(Proparse.BTOS, "btos", NodeTypesOption.KEYWORD),
  BUFFER(Proparse.BUFFER, "buffer", NodeTypesOption.KEYWORD),
  BUFFERCHARS(Proparse.BUFFERCHARS, "buffer-chars", NodeTypesOption.KEYWORD),
  BUFFERCOMPARE(Proparse.BUFFERCOMPARE, "buffer-compare", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BUFFERCOPY(Proparse.BUFFERCOPY, "buffer-copy", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BUFFERGROUPID(Proparse.BUFFERGROUPID, "buffer-group-id", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  BUFFERGROUPNAME(Proparse.BUFFERGROUPNAME, "buffer-group-name", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  BUFFERLINES(Proparse.BUFFERLINES, "buffer-lines", NodeTypesOption.KEYWORD),
  BUFFERNAME(Proparse.BUFFERNAME, "buffer-name", 8, NodeTypesOption.KEYWORD),
  BUFFERTENANTNAME(Proparse.BUFFERTENANTNAME, "buffer-tenant-name", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  BUFFERTENANTID(Proparse.BUFFERTENANTID, "buffer-tenant-id", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  BUTTON(Proparse.BUTTON, "button", NodeTypesOption.KEYWORD),
  BUTTONS(Proparse.BUTTONS, "buttons", NodeTypesOption.KEYWORD),
  BY(Proparse.BY, "by", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BYPOINTER(Proparse.BYPOINTER, "by-pointer", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BYREFERENCE(Proparse.BYREFERENCE, "by-reference", NodeTypesOption.KEYWORD),
  BYTE(Proparse.BYTE, "byte", NodeTypesOption.KEYWORD),
  BYVALUE(Proparse.BYVALUE, "by-value", NodeTypesOption.KEYWORD),
  BYVARIANTPOINTER(Proparse.BYVARIANTPOINTER, "by-variant-pointer", 16, NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  BLOCK_ITERATOR(Proparse.Block_iterator, NodeTypesOption.STRUCTURE),

  // C
  CACHE(Proparse.CACHE, "cache", NodeTypesOption.KEYWORD),
  CACHESIZE(Proparse.CACHESIZE, "cache-size", NodeTypesOption.KEYWORD),
  CALL(Proparse.CALL, "call", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CANCELBUTTON(Proparse.CANCELBUTTON, "cancel-button", NodeTypesOption.KEYWORD),
  CANDO(Proparse.CANDO, "can-do", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  CANFIND(Proparse.CANFIND, "can-find", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  CANQUERY(Proparse.CANQUERY, "can-query", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  CANSET(Proparse.CANSET, "can-set", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  CAPS(Proparse.CAPS, "caps", "upper", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  CARET(Proparse.CARET, "^", NodeTypesOption.SYMBOL),
  CASE(Proparse.CASE, "case", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CASESENSITIVE(Proparse.CASESENSITIVE, "case-sensitive", 8, NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  CAST(Proparse.CAST, "cast", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  CATCH(Proparse.CATCH, "catch", NodeTypesOption.KEYWORD),
  CDECL(Proparse.CDECL_KW, "cdecl", NodeTypesOption.KEYWORD),
  CENTERED(Proparse.CENTERED, "centered", 6, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CHAINED(Proparse.CHAINED, "chained", NodeTypesOption.KEYWORD),
  CHARACTER(Proparse.CHARACTER, "character", 4, NodeTypesOption.KEYWORD),
  CHARACTERLENGTH(Proparse.CHARACTERLENGTH, "characterlength", NodeTypesOption.KEYWORD),
  CHARSET(Proparse.CHARSET, "charset", NodeTypesOption.KEYWORD),
  CHECK(Proparse.CHECK, "check", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CHECKED(Proparse.CHECKED, "checked", NodeTypesOption.KEYWORD),
  CHOOSE(Proparse.CHOOSE, "choose", NodeTypesOption.KEYWORD),
  CHR(Proparse.CHR, "chr", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  CLASS(Proparse.CLASS, "class", NodeTypesOption.KEYWORD),
  CLEAR(Proparse.CLEAR, "clear", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CLIENTPRINCIPAL(Proparse.CLIENTPRINCIPAL, "client-principal", NodeTypesOption.KEYWORD),
  CLIPBOARD(Proparse.CLIPBOARD, "clipboard", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.SYSHDL),
  CLOB(Proparse.CLOB, "clob", NodeTypesOption.KEYWORD),
  CLOSE(Proparse.CLOSE, "close", NodeTypesOption.KEYWORD),
  CODEBASELOCATOR(Proparse.CODEBASELOCATOR, "codebase-locator", NodeTypesOption.KEYWORD,
      NodeTypesOption.SYSHDL),
  CODEPAGE(Proparse.CODEPAGE, "codepage", NodeTypesOption.KEYWORD),
  CODEPAGECONVERT(Proparse.CODEPAGECONVERT, "codepage-convert", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  COLLATE(Proparse.COLLATE, "collate", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  COLOF(Proparse.COLOF, "col-of", NodeTypesOption.KEYWORD),
  COLON(Proparse.COLON, "colon", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  COLONALIGNED(Proparse.COLONALIGNED, "colon-aligned", 11, NodeTypesOption.KEYWORD),
  COLOR(Proparse.COLOR, "color", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  COLORTABLE(Proparse.COLORTABLE, "color-table", NodeTypesOption.KEYWORD, NodeTypesOption.SYSHDL),
  COLUMN(Proparse.COLUMN, "column", 3, NodeTypesOption.KEYWORD),
  COLUMNBGCOLOR(Proparse.COLUMNBGCOLOR, "column-bgcolor", 10, NodeTypesOption.KEYWORD),
  COLUMNCODEPAGE(Proparse.COLUMNCODEPAGE, "column-codepage", NodeTypesOption.KEYWORD),
  COLUMNDCOLOR(Proparse.COLUMNDCOLOR, "column-dcolor", NodeTypesOption.KEYWORD),
  COLUMNFGCOLOR(Proparse.COLUMNFGCOLOR, "column-fgcolor", 10, NodeTypesOption.KEYWORD),
  COLUMNFONT(Proparse.COLUMNFONT, "column-font", NodeTypesOption.KEYWORD),
  COLUMNLABEL(Proparse.COLUMNLABEL, "column-label", 10, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  COLUMNOF(Proparse.COLUMNOF, "column-of", NodeTypesOption.KEYWORD),
  COLUMNPFCOLOR(Proparse.COLUMNPFCOLOR, "column-pfcolor", 10, NodeTypesOption.KEYWORD),
  COLUMNS(Proparse.COLUMNS, "columns", NodeTypesOption.KEYWORD),
  COMBOBOX(Proparse.COMBOBOX, "combo-box", NodeTypesOption.KEYWORD),
  COMHANDLE(Proparse.COMHANDLE, "com-handle", "component-handle", NodeTypesOption.KEYWORD),
  COMMA(Proparse.COMMA, ",", NodeTypesOption.SYMBOL),
  COMMAND(Proparse.COMMAND, "command", NodeTypesOption.KEYWORD),
  COMMENT(Proparse.COMMENT, NodeTypesOption.NONPRINTABLE),
  COMMENTEND(Proparse.COMMENTEND, NodeTypesOption.NONPRINTABLE),
  COMMENTSTART(Proparse.COMMENTSTART, NodeTypesOption.NONPRINTABLE),
  COMPARE(Proparse.COMPARE, "compare", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  COMPARES(Proparse.COMPARES, "compares", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  COMPILE(Proparse.COMPILE, "compile", NodeTypesOption.KEYWORD),
  COMPILER(Proparse.COMPILER, "compiler", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.SYSHDL),
  COMPLETE(Proparse.COMPLETE, "complete", NodeTypesOption.KEYWORD),
  COMSELF(Proparse.COMSELF, "com-self", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.SYSHDL),
  CONFIGNAME(Proparse.CONFIGNAME, "config-name", NodeTypesOption.KEYWORD),
  CONNECT(Proparse.CONNECT, "connect", NodeTypesOption.KEYWORD),
  CONNECTED(Proparse.CONNECTED, "connected", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  CONSTRUCTOR(Proparse.CONSTRUCTOR, "constructor", NodeTypesOption.KEYWORD),
  CONTAINS(Proparse.CONTAINS, "contains", NodeTypesOption.KEYWORD),
  CONTENTS(Proparse.CONTENTS, "contents", NodeTypesOption.KEYWORD),
  CONTEXT(Proparse.CONTEXT, "context", NodeTypesOption.KEYWORD),
  CONTEXTHELP(Proparse.CONTEXTHELP, "context-help", NodeTypesOption.KEYWORD),
  CONTEXTHELPFILE(Proparse.CONTEXTHELPFILE, "context-help-file", NodeTypesOption.KEYWORD),
  CONTEXTHELPID(Proparse.CONTEXTHELPID, "context-help-id", NodeTypesOption.KEYWORD),
  CONTEXTPOPUP(Proparse.CONTEXTPOPUP, "context-popup", 11, NodeTypesOption.KEYWORD),
  CONTROL(Proparse.CONTROL, "control", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CONTROLFRAME(Proparse.CONTROLFRAME, "control-frame", NodeTypesOption.KEYWORD),
  CONVERT(Proparse.CONVERT, "convert", NodeTypesOption.KEYWORD),
  CONVERT3DCOLORS(Proparse.CONVERT3DCOLORS, "convert-3d-colors", 10, NodeTypesOption.KEYWORD),
  COPYDATASET(Proparse.COPYDATASET, "copy-dataset", NodeTypesOption.KEYWORD),
  COPYLOB(Proparse.COPYLOB, "copy-lob", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  COPYTEMPTABLE(Proparse.COPYTEMPTABLE, "copy-temp-table", NodeTypesOption.KEYWORD),
  COUNT(Proparse.COUNT, "count", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  COUNTOF(Proparse.COUNTOF, "count-of", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  CREATE(Proparse.CREATE, "create", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CREATELIKESEQUENTIAL(Proparse.CREATELIKESEQUENTIAL, "create-like-sequential", NodeTypesOption.KEYWORD),
  CREATETESTFILE(Proparse.CREATETESTFILE, "create-test-file", NodeTypesOption.KEYWORD),
  CURLYAMP(Proparse.CURLYAMP),
  CURLYNUMBER(Proparse.CURLYNUMBER),
  CURLYSTAR(Proparse.CURLYSTAR),
  CURRENCY(Proparse.CURRENCY, "currency", NodeTypesOption.KEYWORD),
  CURRENT(Proparse.CURRENT, "current", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CURRENTCHANGED(Proparse.CURRENTCHANGED, "current-changed", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  CURRENTENVIRONMENT(Proparse.CURRENTENVIRONMENT, "current-environment", 11, NodeTypesOption.KEYWORD),
  CURRENTLANGUAGE(Proparse.CURRENTLANGUAGE, "current-language", 12, NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED, NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  CURRENTQUERY(Proparse.CURRENTQUERY, "current-query", NodeTypesOption.KEYWORD),
  CURRENTRESULTROW(Proparse.CURRENTRESULTROW, "current-result-row", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  CURRENTVALUE(Proparse.CURRENTVALUE, "current-value", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  CURRENTWINDOW(Proparse.CURRENTWINDOW, "current-window", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.SYSHDL),
  CURSOR(Proparse.CURSOR, "cursor", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  CODE_BLOCK(Proparse.Code_block, NodeTypesOption.STRUCTURE),

  // D
  DATABASE(Proparse.DATABASE, "database", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DATABIND(Proparse.DATABIND, "data-bind", 6, NodeTypesOption.KEYWORD),
  DATARELATION(Proparse.DATARELATION, "data-relation", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DATASERVERS(Proparse.DATASERVERS, "dataservers", 11, "gateways", 7, NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED, NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  DATASET(Proparse.DATASET, "dataset", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DATASETHANDLE(Proparse.DATASETHANDLE, "dataset-handle", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DATASOURCE(Proparse.DATASOURCE, "data-source", NodeTypesOption.KEYWORD),
  DATASOURCEMODIFIED(Proparse.DATASOURCEMODIFIED, "data-source-modified", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DATASOURCEROWID(Proparse.DATASOURCEROWID, "data-source-rowid", NodeTypesOption.KEYWORD),
  DATE(Proparse.DATE, "date", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DATETIME(Proparse.DATETIME, "datetime", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DATETIMETZ(Proparse.DATETIMETZ, "datetime-tz", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DAY(Proparse.DAY, "day", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DBCODEPAGE(Proparse.DBCODEPAGE, "dbcodepage", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DBCOLLATION(Proparse.DBCOLLATION, "dbcollation", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DBIMS(Proparse.DBIMS, "dbims", NodeTypesOption.KEYWORD),
  DBNAME(Proparse.DBNAME, "dbname", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  DBPARAM(Proparse.DBPARAM, "dbparam", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DBREMOTEHOST(Proparse.DBREMOTEHOST, "db-remote-host", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DBRESTRICTIONS(Proparse.DBRESTRICTIONS, "dbrestrictions", 6, NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DBTASKID(Proparse.DBTASKID, "dbtaskid", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DBTYPE(Proparse.DBTYPE, "dbtype", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DBVERSION(Proparse.DBVERSION, "dbversion", 6, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DCOLOR(Proparse.DCOLOR, "dcolor", NodeTypesOption.KEYWORD),
  DDE(Proparse.DDE, "dde", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DEBLANK(Proparse.DEBLANK, "deblank", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DEBUG(Proparse.DEBUG, "debug", 4, NodeTypesOption.KEYWORD),
  DEBUGGER(Proparse.DEBUGGER, "debugger", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.SYSHDL),
  DEBUGLIST(Proparse.DEBUGLIST, "debug-list", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DECIMAL(Proparse.DECIMAL, "decimal", 3, NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DECIMALS(Proparse.DECIMALS, "decimals", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DECLARE(Proparse.DECLARE, "declare", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DECRYPT(Proparse.DECRYPT, "decrypt", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DEFAULT(Proparse.DEFAULT, "default", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DEFAULTBUTTON(Proparse.DEFAULTBUTTON, "default-button", 8, NodeTypesOption.KEYWORD),
  DEFAULTEXTENSION(Proparse.DEFAULTEXTENSION, "default-extension", 10, NodeTypesOption.KEYWORD),
  DEFAULTNOXLATE(Proparse.DEFAULTNOXLATE, "default-noxlate", 12, NodeTypesOption.KEYWORD),
  DEFAULTVALUE(Proparse.DEFAULTVALUE, "default-value", NodeTypesOption.KEYWORD),
  DEFAULTWINDOW(Proparse.DEFAULTWINDOW, "default-window", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.SYSHDL),
  DEFERLOBFETCH(Proparse.DEFERLOBFETCH, "defer-lob-fetch", NodeTypesOption.KEYWORD),
  DEFINE(Proparse.DEFINE, "define", 3, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DEFINED(Proparse.DEFINED, "defined", NodeTypesOption.KEYWORD),
  DEFINETEXT(Proparse.DEFINETEXT),
  DELEGATE(Proparse.DELEGATE, "delegate", NodeTypesOption.KEYWORD),
  DELETECHARACTER(Proparse.DELETECHARACTER, "delete-character", 11, NodeTypesOption.KEYWORD),
  DELETERESULTLISTENTRY(Proparse.DELETERESULTLISTENTRY, "delete-result-list-entry", NodeTypesOption.KEYWORD),
  DELETE(Proparse.DELETE_KW, "delete", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DELIMITER(Proparse.DELIMITER, "delimiter", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DESCENDING(Proparse.DESCENDING, "descending", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DESELECTION(Proparse.DESELECTION, "deselection", NodeTypesOption.KEYWORD),
  DESTRUCTOR(Proparse.DESTRUCTOR, "destructor", NodeTypesOption.KEYWORD),
  DIALOGBOX(Proparse.DIALOGBOX, "dialog-box", NodeTypesOption.KEYWORD),
  DIALOGHELP(Proparse.DIALOGHELP, "dialog-help", NodeTypesOption.KEYWORD),
  DICTIONARY(Proparse.DICTIONARY, "dictionary", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DIGITS(Proparse.DIGITS),
  DIGITSTART(Proparse.DIGITSTART),
  DIR(Proparse.DIR, "dir", NodeTypesOption.KEYWORD),
  DISABLE(Proparse.DISABLE, "disable", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DISABLEAUTOZAP(Proparse.DISABLEAUTOZAP, "disable-auto-zap", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  DISABLED(Proparse.DISABLED, "disabled", NodeTypesOption.KEYWORD),
  DISCONNECT(Proparse.DISCONNECT, "disconnect", 6, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DISPLAY(Proparse.DISPLAY, "display", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DISTINCT(Proparse.DISTINCT, "distinct", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DIVIDE(Proparse.DIVIDE),
  DO(Proparse.DO, "do", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DOS(Proparse.DOS, "dos", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DOT_COMMENT(Proparse.DOT_COMMENT),
  DOUBLE(Proparse.DOUBLE, "double", NodeTypesOption.KEYWORD),
  DOUBLECOLON(Proparse.DOUBLECOLON, "::", NodeTypesOption.SYMBOL),
  DOUBLEQUOTE(Proparse.DOUBLEQUOTE, "\"", NodeTypesOption.SYMBOL),
  DOWN(Proparse.DOWN, "down", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DQSTRING(Proparse.DQSTRING),
  DROP(Proparse.DROP, "drop", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DROPDOWN(Proparse.DROPDOWN, "drop-down", NodeTypesOption.KEYWORD),
  DROPDOWNLIST(Proparse.DROPDOWNLIST, "drop-down-list", NodeTypesOption.KEYWORD),
  DROPFILENOTIFY(Proparse.DROPFILENOTIFY, "drop-file-notify", NodeTypesOption.KEYWORD),
  DROPTARGET(Proparse.DROPTARGET, "drop-target", NodeTypesOption.KEYWORD),
  DUMP(Proparse.DUMP, "dump", NodeTypesOption.KEYWORD),
  DYNAMIC(Proparse.DYNAMIC, "dynamic", NodeTypesOption.KEYWORD),
  DYNAMICCAST(Proparse.DYNAMICCAST, "dynamic-cast", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DYNAMICCURRENTVALUE(Proparse.DYNAMICCURRENTVALUE, "dynamic-current-value", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DYNAMICFUNCTION(Proparse.DYNAMICFUNCTION, "dynamic-function", 12, NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DYNAMICINVOKE(Proparse.DYNAMICINVOKE, "dynamic-invoke", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DYNAMICNEW(Proparse.DYNAMICNEW, "dynamic-new", NodeTypesOption.KEYWORD),
  DYNAMICNEXTVALUE(Proparse.DYNAMICNEXTVALUE, "dynamic-next-value", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DYNAMICPROPERTY(Proparse.DYNAMICPROPERTY, "dynamic-property", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),

  // E
  EACH(Proparse.EACH, "each", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ECHO(Proparse.ECHO, "echo", NodeTypesOption.KEYWORD),
  EDGECHARS(Proparse.EDGECHARS, "edge-chars", 4, NodeTypesOption.KEYWORD),
  EDGEPIXELS(Proparse.EDGEPIXELS, "edge-pixels", 6, NodeTypesOption.KEYWORD),
  EDITING(Proparse.EDITING, "editing", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  EDITOR(Proparse.EDITOR, "editor", NodeTypesOption.KEYWORD),
  EDITUNDO(Proparse.EDITUNDO, "edit-undo", NodeTypesOption.KEYWORD),
  ELSE(Proparse.ELSE, "else", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  EMPTY(Proparse.EMPTY, "empty", NodeTypesOption.KEYWORD),
  ENABLE(Proparse.ENABLE, "enable", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ENABLEDFIELDS(Proparse.ENABLEDFIELDS, "enabled-fields", NodeTypesOption.KEYWORD),
  ENCODE(Proparse.ENCODE, "encode", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  ENCRYPT(Proparse.ENCRYPT, "encrypt", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  ENCRYPTIONSALT(Proparse.ENCRYPTIONSALT, "encryption-salt", NodeTypesOption.KEYWORD),
  END(Proparse.END, "end", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ENDKEY(Proparse.ENDKEY, "end-key", "endkey", NodeTypesOption.KEYWORD),
  ENDMOVE(Proparse.ENDMOVE, "end-move", NodeTypesOption.KEYWORD),
  ENDRESIZE(Proparse.ENDRESIZE, "end-resize", NodeTypesOption.KEYWORD),
  ENDROWRESIZE(Proparse.ENDROWRESIZE, "end-row-resize", NodeTypesOption.KEYWORD),
  ENTERED(Proparse.ENTERED, "entered", NodeTypesOption.KEYWORD),
  ENTRY(Proparse.ENTRY, "entry", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  ENUM(Proparse.ENUM, "enum", NodeTypesOption.KEYWORD),
  EQ(Proparse.EQ, "eq", NodeTypesOption.KEYWORD),
  EQUAL(Proparse.EQUAL, "=", NodeTypesOption.SYMBOL),
  ERROR(Proparse.ERROR, "error", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  ERRORCODE(Proparse.ERRORCODE, "error-code", NodeTypesOption.KEYWORD),
  ERRORSTACKTRACE(Proparse.ERRORSTACKTRACE, "error-stack-trace", NodeTypesOption.KEYWORD),
  ERRORSTATUS(Proparse.ERRORSTATUS, "error-status", 10, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.SYSHDL),
  ESCAPE(Proparse.ESCAPE, "escape", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ESCAPED_QUOTE(Proparse.ESCAPED_QUOTE),
  ETIME(Proparse.ETIME_KW, "etime", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  EVENT(Proparse.EVENT, "event", NodeTypesOption.KEYWORD),
  EVENTPROCEDURE(Proparse.EVENTPROCEDURE, "event-procedure", NodeTypesOption.KEYWORD),
  EVENTS(Proparse.EVENTS, "events", NodeTypesOption.KEYWORD),
  EXCEPT(Proparse.EXCEPT, "except", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  EXCLAMATION(Proparse.EXCLAMATION, "!", NodeTypesOption.SYMBOL),
  EXCLUSIVEID(Proparse.EXCLUSIVEID, "exclusive-id", NodeTypesOption.KEYWORD),
  EXCLUSIVELOCK(Proparse.EXCLUSIVELOCK, "exclusive-lock", 9, NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  EXCLUSIVEWEBUSER(Proparse.EXCLUSIVEWEBUSER, "exclusive-web-user", 13, NodeTypesOption.KEYWORD),
  EXECUTE(Proparse.EXECUTE, "execute", NodeTypesOption.KEYWORD),
  EXISTS(Proparse.EXISTS, "exists", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  EXP(Proparse.EXP, "exp", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  EXPAND(Proparse.EXPAND, "expand", NodeTypesOption.KEYWORD),
  EXPANDABLE(Proparse.EXPANDABLE, "expandable", NodeTypesOption.KEYWORD),
  EXPLICIT(Proparse.EXPLICIT, "explicit", NodeTypesOption.KEYWORD),
  EXPORT(Proparse.EXPORT, "export", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  EXTENDED(Proparse.EXTENDED, "extended", NodeTypesOption.KEYWORD),
  EXTENT(Proparse.EXTENT, "extent", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  EXTERNAL(Proparse.EXTERNAL, "external", NodeTypesOption.KEYWORD),
  EDITING_PHRASE(Proparse.Editing_phrase, NodeTypesOption.STRUCTURE),
  ENTERED_FUNC(Proparse.Entered_func, NodeTypesOption.STRUCTURE),
  EVENT_LIST(Proparse.Event_list, NodeTypesOption.STRUCTURE),
  EXPR_STATEMENT(Proparse.Expr_statement, NodeTypesOption.STRUCTURE),

  // F
  FALSELEAKS(Proparse.FALSELEAKS, "false-leaks", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FALSE(Proparse.FALSE_KW, "false", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FETCH(Proparse.FETCH, "fetch", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FGCOLOR(Proparse.FGCOLOR, "fgcolor", 3, NodeTypesOption.KEYWORD),
  FIELD(Proparse.FIELD, "field", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FIELDS(Proparse.FIELDS, "fields", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FILE(Proparse.FILE, "file", 4, "file-name", "filename", NodeTypesOption.KEYWORD),
  FILEINFORMATION(Proparse.FILEINFORMATION, "file-information", 9, NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED, NodeTypesOption.SYSHDL),
  FILENAME(Proparse.FILENAME),
  FILL(Proparse.FILL, "fill", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  FILLIN(Proparse.FILLIN, "fill-in", NodeTypesOption.KEYWORD),
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
  FINDWRAPAROUND(Proparse.FINDWRAPAROUND, "find-wrap-around", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  FIRST(Proparse.FIRST, "first", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  FIRSTFORM(Proparse.FIRSTFORM, "first-form", NodeTypesOption.KEYWORD),
  FIRSTOF(Proparse.FIRSTOF, "first-of", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  FITLASTCOLUMN(Proparse.FITLASTCOLUMN, "fit-last-column", NodeTypesOption.KEYWORD),
  FIXCHAR(Proparse.FIXCHAR, "fixchar", NodeTypesOption.KEYWORD),
  FIXCODEPAGE(Proparse.FIXCODEPAGE, "fix-codepage", NodeTypesOption.KEYWORD),
  FIXEDONLY(Proparse.FIXEDONLY, "fixed-only", NodeTypesOption.KEYWORD),
  FLAGS(Proparse.FLAGS, "flags", NodeTypesOption.KEYWORD),
  FLATBUTTON(Proparse.FLATBUTTON, "flat-button", NodeTypesOption.KEYWORD),
  FLOAT(Proparse.FLOAT, "float", NodeTypesOption.KEYWORD),
  FOCUS(Proparse.FOCUS, "focus", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED, NodeTypesOption.SYSHDL),
  FONT(Proparse.FONT, "font", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FONTBASEDLAYOUT(Proparse.FONTBASEDLAYOUT, "font-based-layout", NodeTypesOption.KEYWORD),
  FONTTABLE(Proparse.FONTTABLE, "font-table", NodeTypesOption.KEYWORD, NodeTypesOption.SYSHDL),
  FOR(Proparse.FOR, "for", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FORCEFILE(Proparse.FORCEFILE, "force-file", NodeTypesOption.KEYWORD),
  FOREIGNKEYHIDDEN(Proparse.FOREIGNKEYHIDDEN, "foreign-key-hidden", NodeTypesOption.KEYWORD),
  FORMAT(Proparse.FORMAT, "format", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FORMINPUT(Proparse.FORMINPUT, "forminput", NodeTypesOption.KEYWORD),
  FORMLONGINPUT(Proparse.FORMLONGINPUT, "form-long-input", NodeTypesOption.KEYWORD),
  FORWARDS(Proparse.FORWARDS, "forwards", 7, NodeTypesOption.KEYWORD),
  FRAME(Proparse.FRAME, "frame", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FRAMECOL(Proparse.FRAMECOL, "frame-col", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  FRAMEDB(Proparse.FRAMEDB, "frame-db", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  FRAMEDOWN(Proparse.FRAMEDOWN, "frame-down", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  FRAMEFIELD(Proparse.FRAMEFIELD, "frame-field", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  FRAMEFILE(Proparse.FRAMEFILE, "frame-file", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  FRAMEINDEX(Proparse.FRAMEINDEX, "frame-index", 10, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  FRAMELINE(Proparse.FRAMELINE, "frame-line", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  FRAMENAME(Proparse.FRAMENAME, "frame-name", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  FRAMEROW(Proparse.FRAMEROW, "frame-row", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  FRAMEVALUE(Proparse.FRAMEVALUE, "frame-value", 9, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  FREECHAR(Proparse.FREECHAR),
  FREQUENCY(Proparse.FREQUENCY, "frequency", NodeTypesOption.KEYWORD),
  FROM(Proparse.FROM, "from", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FROMCURRENT(Proparse.FROMCURRENT, "from-current", 8, NodeTypesOption.KEYWORD),
  FUNCTION(Proparse.FUNCTION, "function", NodeTypesOption.KEYWORD),
  FUNCTIONCALLTYPE(Proparse.FUNCTIONCALLTYPE, "function-call-type", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  FIELD_LIST(Proparse.Field_list, NodeTypesOption.STRUCTURE),
  FIELD_REF(Proparse.Field_ref, NodeTypesOption.STRUCTURE),
  FORM_ITEM(Proparse.Form_item, NodeTypesOption.STRUCTURE),
  FORMAT_PHRASE(Proparse.Format_phrase, NodeTypesOption.STRUCTURE),

  // G
  // XXX GATEWAYS(Proparse.GATEWAYS, NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  GE(Proparse.GE, "ge", NodeTypesOption.KEYWORD),
  GENERATEMD5(Proparse.GENERATEMD5, "generate-md5", NodeTypesOption.KEYWORD),
  GENERATEPBEKEY(Proparse.GENERATEPBEKEY, "generate-pbe-key", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GENERATEPBESALT(Proparse.GENERATEPBESALT, "generate-pbe-salt", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  GENERATERANDOMKEY(Proparse.GENERATERANDOMKEY, "generate-random-key", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  GENERATEUUID(Proparse.GENERATEUUID, "generate-uuid", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  GET(Proparse.GET, "get", NodeTypesOption.KEYWORD),
  GETATTRCALLTYPE(Proparse.GETATTRCALLTYPE, "get-attr-call-type", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  GETBITS(Proparse.GETBITS, "get-bits", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETBUFFERHANDLE(Proparse.GETBUFFERHANDLE, "get-buffer-handle", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  GETBYTE(Proparse.GETBYTE, "get-byte", "getbyte", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETBYTEORDER(Proparse.GETBYTEORDER, "get-byte-order", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETBYTES(Proparse.GETBYTES, "get-bytes", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETCGILIST(Proparse.GETCGILIST, "get-cgi-list", NodeTypesOption.KEYWORD),
  GETCGILONGVALUE(Proparse.GETCGILONGVALUE, "get-cgi-long-value", NodeTypesOption.KEYWORD),
  GETCGIVALUE(Proparse.GETCGIVALUE, "get-cgi-value", NodeTypesOption.KEYWORD),
  GETCLASS(Proparse.GETCLASS, "get-class", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETCODEPAGE(Proparse.GETCODEPAGE, "get-codepage", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETCODEPAGES(Proparse.GETCODEPAGES, "get-codepages", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETCOLLATIONS(Proparse.GETCOLLATIONS, "get-collations", 8, NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETCONFIGVALUE(Proparse.GETCONFIGVALUE, "get-config-value", NodeTypesOption.KEYWORD),
  GETDBCLIENT(Proparse.GETDBCLIENT, "get-db-client", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC, NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  GETDIR(Proparse.GETDIR, "get-dir", NodeTypesOption.KEYWORD),
  GETDOUBLE(Proparse.GETDOUBLE, "get-double", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETEFFECTIVETENANTID(Proparse.GETEFFECTIVETENANTID, "get-effective-tenant-id", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETEFFECTIVETENANTNAME(Proparse.GETEFFECTIVETENANTNAME, "get-effective-tenant-name",
      NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETFILE(Proparse.GETFILE, "get-file", NodeTypesOption.KEYWORD),
  GETFLOAT(Proparse.GETFLOAT, "get-float", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETINT64(Proparse.GETINT64, "get-int64", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETKEYVALUE(Proparse.GETKEYVALUE, "get-key-value", 11, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  GETLICENSE(Proparse.GETLICENSE, "get-license", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETLONG(Proparse.GETLONG, "get-long", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETPOINTERVALUE(Proparse.GETPOINTERVALUE, "get-pointer-value", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETSHORT(Proparse.GETSHORT, "get-short", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETSIZE(Proparse.GETSIZE, "get-size", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETSTRING(Proparse.GETSTRING, "get-string", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETUNSIGNEDLONG(Proparse.GETUNSIGNEDLONG, "get-unsigned-long", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETUNSIGNEDSHORT(Proparse.GETUNSIGNEDSHORT, "get-unsigned-short", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GLOBAL(Proparse.GLOBAL, "global", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  GLOBAL_DEFINE(Proparse.GLOBALDEFINE, NodeTypesOption.PREPROCESSOR),
  GOON(Proparse.GOON, "go-on", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  GOPENDING(Proparse.GOPENDING, "go-pending", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  GRANT(Proparse.GRANT, "grant", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  GRAPHICEDGE(Proparse.GRAPHICEDGE, "graphic-edge", 9, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  GROUP(Proparse.GROUP, "group", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  GROUPBOX(Proparse.GROUPBOX, "group-box", NodeTypesOption.KEYWORD),
  GTHAN(Proparse.GTHAN, "gt", NodeTypesOption.KEYWORD),
  GTOREQUAL(Proparse.GTOREQUAL, ">=", NodeTypesOption.SYMBOL),
  GTORLT(Proparse.GTORLT, "<>", NodeTypesOption.SYMBOL),
  GUID(Proparse.GUID, "guid", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC, NodeTypesOption.MAY_BE_NO_ARG_FUNC),

  // H
  HANDLE(Proparse.HANDLE, "handle", NodeTypesOption.KEYWORD),
  HAVING(Proparse.HAVING, "having", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  HEADER(Proparse.HEADER, "header", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  HEIGHT(Proparse.HEIGHT, "height", NodeTypesOption.KEYWORD),
  HEIGHTCHARS(Proparse.HEIGHTCHARS, "height-chars", 8, NodeTypesOption.KEYWORD),
  HEIGHTPIXELS(Proparse.HEIGHTPIXELS, "height-pixels", 8, NodeTypesOption.KEYWORD),
  HELP(Proparse.HELP, "help", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  HELPTOPIC(Proparse.HELPTOPIC, "help-topic", NodeTypesOption.KEYWORD),
  HEXDECODE(Proparse.HEXDECODE, "hex-decode", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  HEXENCODE(Proparse.HEXENCODE, "hex-encode", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  HIDDEN(Proparse.HIDDEN, "hidden", NodeTypesOption.KEYWORD),
  HIDE(Proparse.HIDE, "hide", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  HINT(Proparse.HINT, "hint", NodeTypesOption.KEYWORD),
  HORIZONTAL(Proparse.HORIZONTAL, "horizontal", 4, NodeTypesOption.KEYWORD),
  HOSTBYTEORDER(Proparse.HOSTBYTEORDER, "host-byte-order", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  HTMLENDOFLINE(Proparse.HTMLENDOFLINE, "html-end-of-line", NodeTypesOption.KEYWORD),
  HTMLFRAMEBEGIN(Proparse.HTMLFRAMEBEGIN, "html-frame-begin", NodeTypesOption.KEYWORD),
  HTMLFRAMEEND(Proparse.HTMLFRAMEEND, "html-frame-end", NodeTypesOption.KEYWORD),
  HTMLHEADERBEGIN(Proparse.HTMLHEADERBEGIN, "html-header-begin", NodeTypesOption.KEYWORD),
  HTMLHEADEREND(Proparse.HTMLHEADEREND, "html-header-end", NodeTypesOption.KEYWORD),
  HTMLTITLEBEGIN(Proparse.HTMLTITLEBEGIN, "html-title-begin", NodeTypesOption.KEYWORD),
  HTMLTITLEEND(Proparse.HTMLTITLEEND, "html-title-end", NodeTypesOption.KEYWORD),

  // I
  ID(Proparse.ID),
  ID_THREE(Proparse.ID_THREE),
  ID_TWO(Proparse.ID_TWO),
  IF(Proparse.IF, "if", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  IFCOND(Proparse.IFCOND),
  IMAGE(Proparse.IMAGE, "image", NodeTypesOption.KEYWORD),
  IMAGEDOWN(Proparse.IMAGEDOWN, "image-down", NodeTypesOption.KEYWORD),
  IMAGEINSENSITIVE(Proparse.IMAGEINSENSITIVE, "image-insensitive", NodeTypesOption.KEYWORD),
  IMAGESIZE(Proparse.IMAGESIZE, "image-size", NodeTypesOption.KEYWORD),
  IMAGESIZECHARS(Proparse.IMAGESIZECHARS, "image-size-chars", 12, NodeTypesOption.KEYWORD),
  IMAGESIZEPIXELS(Proparse.IMAGESIZEPIXELS, "image-size-pixels", 12, NodeTypesOption.KEYWORD),
  IMAGEUP(Proparse.IMAGEUP, "image-up", NodeTypesOption.KEYWORD),
  IMPLEMENTS(Proparse.IMPLEMENTS, "implements", NodeTypesOption.KEYWORD),
  IMPORT(Proparse.IMPORT, "import", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  IMPOSSIBLE_TOKEN(Proparse.IMPOSSIBLE_TOKEN),
  INCLUDEREFARG(Proparse.INCLUDEREFARG),
  INCREMENTEXCLUSIVEID(Proparse.INCREMENTEXCLUSIVEID, "increment-exclusive-id", NodeTypesOption.KEYWORD),
  INDEX(Proparse.INDEX, "index", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  INDEXEDREPOSITION(Proparse.INDEXEDREPOSITION, "indexed-reposition", NodeTypesOption.KEYWORD),
  INDEXHINT(Proparse.INDEXHINT, "index-hint", NodeTypesOption.KEYWORD),
  INDICATOR(Proparse.INDICATOR, "indicator", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  INFORMATION(Proparse.INFORMATION, "information", 4, NodeTypesOption.KEYWORD),
  INHERITBGCOLOR(Proparse.INHERITBGCOLOR, "inherit-bgcolor", 11, NodeTypesOption.KEYWORD),
  INHERITFGCOLOR(Proparse.INHERITFGCOLOR, "inherit-fgcolor", 11, NodeTypesOption.KEYWORD),
  INHERITS(Proparse.INHERITS, "inherits", NodeTypesOption.KEYWORD),
  INITIAL(Proparse.INITIAL, "initial", 4, NodeTypesOption.KEYWORD),
  INITIALDIR(Proparse.INITIALDIR, "initial-dir", NodeTypesOption.KEYWORD),
  INITIALFILTER(Proparse.INITIALFILTER, "initial-filter", NodeTypesOption.KEYWORD),
  INITIATE(Proparse.INITIATE, "initiate", NodeTypesOption.KEYWORD),
  INNER(Proparse.INNER, "inner", NodeTypesOption.KEYWORD),
  INNERCHARS(Proparse.INNERCHARS, "inner-chars", NodeTypesOption.KEYWORD),
  INNERLINES(Proparse.INNERLINES, "inner-lines", NodeTypesOption.KEYWORD),
  INPUT(Proparse.INPUT, "input", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  INPUTOUTPUT(Proparse.INPUTOUTPUT, "input-output", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  INSERT(Proparse.INSERT, "insert", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  INT64(Proparse.INT64, "int64", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  INTEGER(Proparse.INTEGER, "integer", 3, NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  INTERFACE(Proparse.INTERFACE, "interface", NodeTypesOption.KEYWORD),
  INTERVAL(Proparse.INTERVAL, "interval", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  INTO(Proparse.INTO, "into", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  IN(Proparse.IN_KW, "in", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  IS(Proparse.IS, "is", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ISATTRSPACE(Proparse.ISATTRSPACE, "is-attr-space", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  ISCODEPAGEFIXED(Proparse.ISCODEPAGEFIXED, "is-codepage-fixed", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  ISCOLUMNCODEPAGE(Proparse.ISCOLUMNCODEPAGE, "is-column-codepage", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  ISDBMULTITENANT(Proparse.ISDBMULTITENANT, "is-db-multi-tenant", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  ISLEADBYTE(Proparse.ISLEADBYTE, "is-lead-byte", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  ISMULTITENANT(Proparse.ISMULTITENANT, "is-multi-tenant", NodeTypesOption.KEYWORD),
  ISODATE(Proparse.ISODATE, "iso-date", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  ITEM(Proparse.ITEM, "item", NodeTypesOption.KEYWORD),
  IUNKNOWN(Proparse.IUNKNOWN, "iunknown", NodeTypesOption.KEYWORD),
  INLINE_DEFINITION(Proparse.Inline_definition, NodeTypesOption.STRUCTURE),

  // J
  JOIN(Proparse.JOIN, "join", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  JOINBYSQLDB(Proparse.JOINBYSQLDB, "join-by-sqldb", NodeTypesOption.KEYWORD),

  // K
  KBLABEL(Proparse.KBLABEL, "kblabel", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  KEEPMESSAGES(Proparse.KEEPMESSAGES, "keep-messages", NodeTypesOption.KEYWORD),
  KEEPTABORDER(Proparse.KEEPTABORDER, "keep-tab-order", NodeTypesOption.KEYWORD),
  KEY(Proparse.KEY, "key", NodeTypesOption.KEYWORD),
  KEYCODE(Proparse.KEYCODE, "key-code", "keycode", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  KEYFUNCTION(Proparse.KEYFUNCTION, "key-function", 8, "keyfunction", 7, NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  KEYLABEL(Proparse.KEYLABEL, "key-label", "keylabel", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  KEYS(Proparse.KEYS, "keys", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  KEYWORD(Proparse.KEYWORD, "keyword", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  KEYWORDALL(Proparse.KEYWORDALL, "keyword-all", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),

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
  LAST(Proparse.LAST, "last", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  LASTBATCH(Proparse.LASTBATCH, "last-batch", NodeTypesOption.KEYWORD),
  LASTEVENT(Proparse.LASTEVENT, "last-event", 9, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.SYSHDL),
  LASTFORM(Proparse.LASTFORM, "last-form", NodeTypesOption.KEYWORD),
  LASTKEY(Proparse.LASTKEY, "last-key", "lastkey", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  LASTOF(Proparse.LASTOF, "last-of", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  LC(Proparse.LC, "lc", "lower", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  LDBNAME(Proparse.LDBNAME, "ldbname", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  LE(Proparse.LE, "le", NodeTypesOption.KEYWORD),
  LEAKDETECTION(Proparse.LEAKDETECTION, "leak-detection", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LEAVE(Proparse.LEAVE, "leave", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LEFT(Proparse.LEFT, "left", NodeTypesOption.KEYWORD),
  LEFTALIGNED(Proparse.LEFTALIGNED, "left-aligned", 10, NodeTypesOption.KEYWORD),
  LEFTANGLE(Proparse.LEFTANGLE, "<", NodeTypesOption.SYMBOL),
  LEFTBRACE(Proparse.LEFTBRACE, "[", NodeTypesOption.SYMBOL),
  LEFTCURLY(Proparse.LEFTCURLY, "{", NodeTypesOption.SYMBOL),
  LEFTPAREN(Proparse.LEFTPAREN, "(", NodeTypesOption.SYMBOL),
  LEFTTRIM(Proparse.LEFTTRIM, "left-trim", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  LENGTH(Proparse.LENGTH, "length", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  LEXAT(Proparse.LEXAT, "@", NodeTypesOption.SYMBOL),
  LEXCOLON(Proparse.LEXCOLON, ":", NodeTypesOption.SYMBOL),
  LEXDATE(Proparse.LEXDATE, NodeTypesOption.NONPRINTABLE),
  LEXOTHER(Proparse.LEXOTHER, NodeTypesOption.NONPRINTABLE),
  LIBRARY(Proparse.LIBRARY, "library", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  LIKE(Proparse.LIKE, "like", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LIKESEQUENTIAL(Proparse.LIKESEQUENTIAL, "like-sequential", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  LINECOUNTER(Proparse.LINECOUNTER, "line-counter", 10, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  LISTEVENTS(Proparse.LISTEVENTS, "list-events", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  LISTING(Proparse.LISTING, "listing", 5, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LISTITEMPAIRS(Proparse.LISTITEMPAIRS, "list-item-pairs", NodeTypesOption.KEYWORD),
  LISTITEMS(Proparse.LISTITEMS, "list-items", NodeTypesOption.KEYWORD),
  LISTQUERYATTRS(Proparse.LISTQUERYATTRS, "list-query-attrs", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  LISTSETATTRS(Proparse.LISTSETATTRS, "list-set-attrs", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  LISTWIDGETS(Proparse.LISTWIDGETS, "list-widgets", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  LITTLEENDIAN(Proparse.LITTLEENDIAN, "little-endian", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LOAD(Proparse.LOAD, "load", NodeTypesOption.KEYWORD),
  LOADPICTURE(Proparse.LOADPICTURE, "load-picture", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  LOBDIR(Proparse.LOBDIR, "lob-dir", NodeTypesOption.KEYWORD),
  LOCAL_METHOD_REF(Proparse.LOCAL_METHOD_REF, NodeTypesOption.STRUCTURE),
  LOCKED(Proparse.LOCKED, "locked", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  LOG(Proparse.LOG, "log", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  LOGICAL(Proparse.LOGICAL, "logical", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  LOGMANAGER(Proparse.LOGMANAGER, "log-manager", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.SYSHDL),
  LONG(Proparse.LONG, "long", NodeTypesOption.KEYWORD),
  LONGCHAR(Proparse.LONGCHAR, "longchar", NodeTypesOption.KEYWORD),
  LOOKAHEAD(Proparse.LOOKAHEAD, "lookahead", NodeTypesOption.KEYWORD),
  LOOKUP(Proparse.LOOKUP, "lookup", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  LTHAN(Proparse.LTHAN, "lt", NodeTypesOption.KEYWORD),
  LTOREQUAL(Proparse.LTOREQUAL, ">=", NodeTypesOption.SYMBOL),
  LOOSE_END_KEEPER(Proparse.Loose_End_Keeper, NodeTypesOption.STRUCTURE),

  // M
  MACHINECLASS(Proparse.MACHINECLASS, "machine-class", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  MAP(Proparse.MAP, "map", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  MARGINEXTRA(Proparse.MARGINEXTRA, "margin-extra", NodeTypesOption.KEYWORD),
  MARKNEW(Proparse.MARKNEW, "mark-new", NodeTypesOption.KEYWORD),
  MARKROWSTATE(Proparse.MARKROWSTATE, "mark-row-state", NodeTypesOption.KEYWORD),
  MATCHES(Proparse.MATCHES, "matches", NodeTypesOption.KEYWORD),
  MAXCHARS(Proparse.MAXCHARS, "max-chars", NodeTypesOption.KEYWORD),
  MAXIMIZE(Proparse.MAXIMIZE, "maximize", NodeTypesOption.KEYWORD),
  MAXIMUM(Proparse.MAXIMUM, "max", "maximum", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  MAXIMUMLEVEL(Proparse.MAXIMUMLEVEL, "maximum-level", NodeTypesOption.KEYWORD),
  MAXROWS(Proparse.MAXROWS, "max-rows", NodeTypesOption.KEYWORD),
  MAXSIZE(Proparse.MAXSIZE, "max-size", NodeTypesOption.KEYWORD),
  MAXVALUE(Proparse.MAXVALUE, "max-value", 7, NodeTypesOption.KEYWORD),
  MD5DIGEST(Proparse.MD5DIGEST, "md5-digest", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  MEMBER(Proparse.MEMBER, "member", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  MEMPTR(Proparse.MEMPTR, "memptr", NodeTypesOption.KEYWORD),
  MENU(Proparse.MENU, "menu", NodeTypesOption.KEYWORD),
  MENUBAR(Proparse.MENUBAR, "menu-bar", "menubar", NodeTypesOption.KEYWORD),
  MENUITEM(Proparse.MENUITEM, "menu-item", NodeTypesOption.KEYWORD),
  MERGEBYFIELD(Proparse.MERGEBYFIELD, "merge-by-field", NodeTypesOption.KEYWORD),
  MESSAGE(Proparse.MESSAGE, "message", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  MESSAGEDIGEST(Proparse.MESSAGEDIGEST, "message-digest", NodeTypesOption.KEYWORD),
  MESSAGELINE(Proparse.MESSAGELINE, "message-line", NodeTypesOption.KEYWORD),
  MESSAGELINES(Proparse.MESSAGELINES, "message-lines", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  METHOD(Proparse.METHOD, "method", NodeTypesOption.KEYWORD),
  MINIMUM(Proparse.MINIMUM, "minimum", 3, NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  MINSIZE(Proparse.MINSIZE, "min-size", NodeTypesOption.KEYWORD),
  MINUS(Proparse.MINUS, "-", NodeTypesOption.SYMBOL),
  MINVALUE(Proparse.MINVALUE, "min-value", 7, NodeTypesOption.KEYWORD),
  MODULO(Proparse.MODULO, "modulo", 3, NodeTypesOption.KEYWORD),
  MONTH(Proparse.MONTH, "month", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  MOUSE(Proparse.MOUSE, "mouse", NodeTypesOption.KEYWORD, NodeTypesOption.SYSHDL),
  MOUSEPOINTER(Proparse.MOUSEPOINTER, "mouse-pointer", 7, NodeTypesOption.KEYWORD),
  MPE(Proparse.MPE, "mpe", NodeTypesOption.KEYWORD),
  MTIME(Proparse.MTIME, "mtime", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  MULTIPLE(Proparse.MULTIPLE, "multiple", NodeTypesOption.KEYWORD),
  MULTIPLEKEY(Proparse.MULTIPLEKEY, "multiple-key", NodeTypesOption.KEYWORD),
  MULTIPLY(Proparse.MULTIPLY, "*", NodeTypesOption.SYMBOL),
  MUSTEXIST(Proparse.MUSTEXIST, "must-exist", NodeTypesOption.KEYWORD),
  METHOD_PARAM_LIST(Proparse.Method_param_list, NodeTypesOption.STRUCTURE),
  METHOD_PARAMETER(Proparse.Method_parameter, NodeTypesOption.STRUCTURE),

  NAMEDOT(Proparse.NAMEDOT, ".", NodeTypesOption.SYMBOL),
  NAMESPACEPREFIX(Proparse.NAMESPACEPREFIX, "namespace-prefix", NodeTypesOption.KEYWORD),
  NAMESPACEURI(Proparse.NAMESPACEURI, "namespace-uri", NodeTypesOption.KEYWORD),
  NATIVE(Proparse.NATIVE, "native", NodeTypesOption.KEYWORD),
  NE(Proparse.NE, "ne", NodeTypesOption.KEYWORD),
  NESTED(Proparse.NESTED, "nested", NodeTypesOption.KEYWORD),
  NEW(Proparse.NEW, "new", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  NEWINSTANCE(Proparse.NEWINSTANCE, "new-instance", NodeTypesOption.KEYWORD),
  NEWLINE(Proparse.NEWLINE, NodeTypesOption.NONPRINTABLE),
  NEXT(Proparse.NEXT, "next", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NEXTPROMPT(Proparse.NEXTPROMPT, "next-prompt", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NEXTVALUE(Proparse.NEXTVALUE, "next-value", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  NO(Proparse.NO, "no", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOAPPLY(Proparse.NOAPPLY, "no-apply", NodeTypesOption.KEYWORD),
  // NOARRAYMESSAGE(Proparse.NOARRAYMESSAGE, 10, "no-array-message", NodeTypesOption.KEYWORD),
  NOASSIGN(Proparse.NOASSIGN, "no-assign", NodeTypesOption.KEYWORD),
  NOATTRLIST(Proparse.NOATTRLIST, "no-attr-list", 9, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOATTRSPACE(Proparse.NOATTRSPACE, "no-attr-space", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
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
  NOERROR(Proparse.NOERROR_KW, "no-error", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOFILL(Proparse.NOFILL, "no-fill", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOFOCUS(Proparse.NOFOCUS, "no-focus", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
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
  NORETURNVALUE(Proparse.NORETURNVALUE, "no-return-value", 13, NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  NORMAL(Proparse.NORMAL, "normal", NodeTypesOption.KEYWORD),
  NORMALIZE(Proparse.NORMALIZE, "normalize", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  NOROWMARKERS(Proparse.NOROWMARKERS, "no-row-markers", NodeTypesOption.KEYWORD),
  NOSCROLLBARVERTICAL(Proparse.NOSCROLLBARVERTICAL, "no-scrollbar-vertical", 14, NodeTypesOption.KEYWORD),
  NOSEPARATECONNECTION(Proparse.NOSEPARATECONNECTION, "no-separate-connection", NodeTypesOption.KEYWORD),
  NOSEPARATORS(Proparse.NOSEPARATORS, "no-separators", NodeTypesOption.KEYWORD),
  NOT(Proparse.NOT, "not", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOTACTIVE(Proparse.NOTACTIVE, "not-active", NodeTypesOption.KEYWORD),
  NOTABSTOP(Proparse.NOTABSTOP, "no-tab-stop", 6, NodeTypesOption.KEYWORD),
  NOUNDERLINE(Proparse.NOUNDERLINE, "no-underline", 6, NodeTypesOption.KEYWORD),
  NOUNDO(Proparse.NOUNDO, "no-undo", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOVALIDATE(Proparse.NOVALIDATE, "no-validate", 6, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOW(Proparse.NOW, "now", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  NOWAIT(Proparse.NOWAIT, "no-wait", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOWORDWRAP(Proparse.NOWORDWRAP, "no-word-wrap", NodeTypesOption.KEYWORD),
  NULL(Proparse.NULL_KW, "null", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NUMALIASES(Proparse.NUMALIASES, "num-aliases", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  NUMBER(Proparse.NUMBER, NodeTypesOption.SYMBOL),
  NUMCOPIES(Proparse.NUMCOPIES, "num-copies", NodeTypesOption.KEYWORD),
  NUMDBS(Proparse.NUMDBS, "num-dbs", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  NUMENTRIES(Proparse.NUMENTRIES, "num-entries", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  NUMERIC(Proparse.NUMERIC, "numeric", NodeTypesOption.KEYWORD),
  NUMRESULTS(Proparse.NUMRESULTS, "num-results", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  NOT_CASESENS(Proparse.Not_casesens, NodeTypesOption.STRUCTURE),
  NOT_NULL(Proparse.Not_null, NodeTypesOption.STRUCTURE),

  // O
  OBJCOLON(Proparse.OBJCOLON, ":", NodeTypesOption.SYMBOL),
  OBJECT(Proparse.OBJECT, "object", NodeTypesOption.KEYWORD),
  OCTETLENGTH(Proparse.OCTETLENGTH, "octet-length", NodeTypesOption.KEYWORD),
  OF(Proparse.OF, "of", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OFF(Proparse.OFF, "off", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OK(Proparse.OK, "ok", NodeTypesOption.KEYWORD),
  OKCANCEL(Proparse.OKCANCEL, "ok-cancel", NodeTypesOption.KEYWORD),
  OLD(Proparse.OLD, "old", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ON(Proparse.ON, "on", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ONLY(Proparse.ONLY, "only", NodeTypesOption.KEYWORD),
  OPEN(Proparse.OPEN, "open", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OPSYS(Proparse.OPSYS, "opsys", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  OPTION(Proparse.OPTION, "option", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OPTIONS(Proparse.OPTIONS, "options", NodeTypesOption.KEYWORD),
  OPTIONSFILE(Proparse.OPTIONSFILE, "options-file", NodeTypesOption.KEYWORD),
  OR(Proparse.OR, "or", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ORDER(Proparse.ORDER, "order", NodeTypesOption.KEYWORD),
  ORDEREDJOIN(Proparse.ORDEREDJOIN, "ordered-join", NodeTypesOption.KEYWORD),
  ORDINAL(Proparse.ORDINAL, "ordinal", NodeTypesOption.KEYWORD),
  OS2(Proparse.OS2, "os2", NodeTypesOption.KEYWORD),
  OS400(Proparse.OS400, "os400", NodeTypesOption.KEYWORD),
  OSAPPEND(Proparse.OSAPPEND, "os-append", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OSCOMMAND(Proparse.OSCOMMAND, "os-command", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OSCOPY(Proparse.OSCOPY, "os-copy", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OSCREATEDIR(Proparse.OSCREATEDIR, "os-create-dir", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OSDELETE(Proparse.OSDELETE, "os-delete", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OSDIR(Proparse.OSDIR, "os-dir", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OSDRIVES(Proparse.OSDRIVES, "os-drives", 8, NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  OSERROR(Proparse.OSERROR, "os-error", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  OSGETENV(Proparse.OSGETENV, "os-getenv", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  OSRENAME(Proparse.OSRENAME, "os-rename", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OTHERWISE(Proparse.OTHERWISE, "otherwise", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OUTER(Proparse.OUTER, "outer", NodeTypesOption.KEYWORD),
  OUTERJOIN(Proparse.OUTERJOIN, "outer-join", NodeTypesOption.KEYWORD),
  OUTPUT(Proparse.OUTPUT, "output", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OVERLAY(Proparse.OVERLAY, "overlay", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OVERRIDE(Proparse.OVERRIDE, "override", NodeTypesOption.KEYWORD),

  // P
  PAGE(Proparse.PAGE, "page", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PAGEBOTTOM(Proparse.PAGEBOTTOM, "page-bottom", 8, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PAGED(Proparse.PAGED, "paged", NodeTypesOption.KEYWORD),
  PAGENUMBER(Proparse.PAGENUMBER, "page-number", 8, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  PAGESIZE(Proparse.PAGESIZE_KW, "page-size", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_NO_ARG_FUNC,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  PAGETOP(Proparse.PAGETOP, "page-top", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PAGEWIDTH(Proparse.PAGEWIDTH, "page-width", 8, NodeTypesOption.KEYWORD),
  PARAMETER(Proparse.PARAMETER, "parameter", 5, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PARENT(Proparse.PARENT, "parent", NodeTypesOption.KEYWORD), // PARENT is not a reserved keyword (not what documentation says)
  PARENTFIELDSAFTER(Proparse.PARENTFIELDSAFTER, "parent-fields-after", NodeTypesOption.KEYWORD),
  PARENTFIELDSBEFORE(Proparse.PARENTFIELDSBEFORE, "parent-fields-before", NodeTypesOption.KEYWORD),
  PARENTIDFIELD(Proparse.PARENTIDFIELD, "parent-id-field", NodeTypesOption.KEYWORD),
  PARENTIDRELATION(Proparse.PARENTIDRELATION, "parent-id-relation", NodeTypesOption.KEYWORD),
  PARTIALKEY(Proparse.PARTIALKEY, "partial-key", NodeTypesOption.KEYWORD),
  PASCAL(Proparse.PASCAL_KW, "pascal", NodeTypesOption.KEYWORD),
  PASSWORDFIELD(Proparse.PASSWORDFIELD, "password-field", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PAUSE(Proparse.PAUSE, "pause", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PBEHASHALGORITHM(Proparse.PBEHASHALGORITHM, "pbe-hash-algorithm", 12, NodeTypesOption.KEYWORD),
  PBEKEYROUNDS(Proparse.PBEKEYROUNDS, "pbe-key-rounds", NodeTypesOption.KEYWORD),
  PDBNAME(Proparse.PDBNAME, "pdbname", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  PERFORMANCE(Proparse.PERFORMANCE, "performance", 4, NodeTypesOption.KEYWORD),
  PERIOD(Proparse.PERIOD, ".", NodeTypesOption.SYMBOL),
  PERIODSTART(Proparse.PERIODSTART, ".", NodeTypesOption.SYMBOL),
  PERSISTENT(Proparse.PERSISTENT, "persistent", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PFCOLOR(Proparse.PFCOLOR, "pfcolor", 3, NodeTypesOption.KEYWORD),
  PINNABLE(Proparse.PINNABLE, "pinnable", NodeTypesOption.KEYWORD),
  PIPE(Proparse.PIPE, "|", NodeTypesOption.SYMBOL),
  PLUS(Proparse.PLUS, "+", NodeTypesOption.SYMBOL),
  PLUSMINUSSTART(Proparse.PLUSMINUSSTART),
  PORTRAIT(Proparse.PORTRAIT, "portrait", NodeTypesOption.KEYWORD),
  POSITION(Proparse.POSITION, "position", NodeTypesOption.KEYWORD),
  PRECISION(Proparse.PRECISION, "precision", NodeTypesOption.KEYWORD),
  PREFERDATASET(Proparse.PREFERDATASET, "prefer-dataset", NodeTypesOption.KEYWORD),
  PREPROCESS(Proparse.PREPROCESS, "preprocess", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PREPROCESSDIRECTIVE(Proparse.PREPROCESSDIRECTIVE, NodeTypesOption.PREPROCESSOR),
  PREPROCESSELSE(Proparse.PREPROCESSELSE, NodeTypesOption.PREPROCESSOR),
  PREPROCESSELSEIF(Proparse.PREPROCESSELSEIF, NodeTypesOption.PREPROCESSOR),
  PREPROCESSENDIF(Proparse.PREPROCESSENDIF, NodeTypesOption.PREPROCESSOR),
  PREPROCESSIF(Proparse.PREPROCESSIF, NodeTypesOption.PREPROCESSOR),
  PREPROCESSJMESSAGE(Proparse.PREPROCESSJMESSAGE, NodeTypesOption.PREPROCESSOR),
  PREPROCESSMESSAGE(Proparse.PREPROCESSMESSAGE, NodeTypesOption.PREPROCESSOR),
  PREPROCESSTOKEN(Proparse.PREPROCESSTOKEN, NodeTypesOption.PREPROCESSOR),
  PREPROCESSUNDEFINE(Proparse.PREPROCESSUNDEFINE, NodeTypesOption.PREPROCESSOR),
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
  PROCESSARCHITECTURE(Proparse.PROCESSARCHITECTURE, "process-architecture", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  PROCHANDLE(Proparse.PROCHANDLE, "proc-handle", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  PROCSTATUS(Proparse.PROCSTATUS, "proc-status", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  PROCTEXT(Proparse.PROCTEXT, "proc-text", NodeTypesOption.KEYWORD),
  PROCTEXTBUFFER(Proparse.PROCTEXTBUFFER, "proc-text-buffer", NodeTypesOption.KEYWORD),
  PROFILER(Proparse.PROFILER, "profiler", NodeTypesOption.KEYWORD, NodeTypesOption.SYSHDL),
  PROGRAMNAME(Proparse.PROGRAMNAME, "program-name", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  PROGRESS(Proparse.PROGRESS, "progress", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  PROMPT(Proparse.PROMPT, "prompt", NodeTypesOption.KEYWORD),
  PROMPTFOR(Proparse.PROMPTFOR, "prompt-for", 8, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PROMSGS(Proparse.PROMSGS, "promsgs", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  PROPARSEDIRECTIVE(Proparse.PROPARSEDIRECTIVE, NodeTypesOption.PREPROCESSOR),
  PROPATH(Proparse.PROPATH, "propath", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  PROPERTY(Proparse.PROPERTY, "property", NodeTypesOption.KEYWORD),
  PROPERTY_GETTER(Proparse.Property_getter, NodeTypesOption.STRUCTURE),
  PROPERTY_SETTER(Proparse.Property_setter, NodeTypesOption.STRUCTURE),
  PROTECTED(Proparse.PROTECTED, "protected", NodeTypesOption.KEYWORD),
  PROVERSION(Proparse.PROVERSION, "proversion", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  PUBLIC(Proparse.PUBLIC, "public", NodeTypesOption.KEYWORD),
  PUBLISH(Proparse.PUBLISH, "publish", NodeTypesOption.KEYWORD),
  PUT(Proparse.PUT, "put", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PUTBITS(Proparse.PUTBITS, "put-bits", NodeTypesOption.KEYWORD),
  PUTBYTE(Proparse.PUTBYTE, "put-byte", "putbyte", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
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
  PARAMETER_LIST(Proparse.Parameter_list, NodeTypesOption.STRUCTURE),
  PROGRAM_ROOT(Proparse.Program_root, NodeTypesOption.STRUCTURE),
  PROGRAM_TAIL(Proparse.Program_tail, NodeTypesOption.STRUCTURE),

  // Q
  QSTRING(Proparse.QSTRING),
  QUERY(Proparse.QUERY, "query", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  QUERYCLOSE(Proparse.QUERYCLOSE, "query-close", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  QUERYOFFEND(Proparse.QUERYOFFEND, "query-off-end", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  QUERYTUNING(Proparse.QUERYTUNING, "query-tuning", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  QUESTION(Proparse.QUESTION, "question", NodeTypesOption.KEYWORD),
  QUIT(Proparse.QUIT, "quit", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  QUOTER(Proparse.QUOTER, "quoter", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),

  // R
  RADIOBUTTONS(Proparse.RADIOBUTTONS, "radio-buttons", NodeTypesOption.KEYWORD),
  RADIOSET(Proparse.RADIOSET, "radio-set", NodeTypesOption.KEYWORD),
  RANDOM(Proparse.RANDOM, "random", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  RAW(Proparse.RAW, "raw", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  RAWTRANSFER(Proparse.RAWTRANSFER, "raw-transfer", NodeTypesOption.KEYWORD),
  RCODEINFORMATION(Proparse.RCODEINFORMATION, "rcode-information", 10, NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED, NodeTypesOption.SYSHDL),
  READ(Proparse.READ, "read", NodeTypesOption.KEYWORD),
  READAVAILABLE(Proparse.READAVAILABLE, "read-available", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  READEXACTNUM(Proparse.READEXACTNUM, "read-exact-num", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  READKEY(Proparse.READKEY, "readkey", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  READONLY(Proparse.READONLY, "read-only", NodeTypesOption.KEYWORD),
  REAL(Proparse.REAL, "real", NodeTypesOption.KEYWORD),
  RECID(Proparse.RECID, "recid", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  RECORDLENGTH(Proparse.RECORDLENGTH, "record-length", 10, NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  RECORD_NAME(Proparse.RECORD_NAME, NodeTypesOption.STRUCTURE),
  RECTANGLE(Proparse.RECTANGLE, "rectangle", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  RECURSIVE(Proparse.RECURSIVE, "recursive", NodeTypesOption.KEYWORD),
  REFERENCEONLY(Proparse.REFERENCEONLY, "reference-only", NodeTypesOption.KEYWORD),
  REJECTED(Proparse.REJECTED, "rejected", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  RELATIONFIELDS(Proparse.RELATIONFIELDS, "relation-fields", 11, NodeTypesOption.KEYWORD),
  RELEASE(Proparse.RELEASE, "release", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  REPEAT(Proparse.REPEAT, "repeat", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  REPLACE(Proparse.REPLACE, "replace", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  REPLICATIONCREATE(Proparse.REPLICATIONCREATE, "replication-create", NodeTypesOption.KEYWORD),
  REPLICATIONDELETE(Proparse.REPLICATIONDELETE, "replication-delete", NodeTypesOption.KEYWORD),
  REPLICATIONWRITE(Proparse.REPLICATIONWRITE, "replication-write", NodeTypesOption.KEYWORD),
  REPOSITION(Proparse.REPOSITION, "reposition", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  REPOSITIONBACKWARD(Proparse.REPOSITIONBACKWARD, "reposition-backward", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  REPOSITIONFORWARD(Proparse.REPOSITIONFORWARD, "reposition-forward", NodeTypesOption.KEYWORD),
  REPOSITIONMODE(Proparse.REPOSITIONMODE, "reposition-mode", NodeTypesOption.KEYWORD),
  REPOSITIONTOROW(Proparse.REPOSITIONTOROW, "reposition-to-row", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  REPOSITIONTOROWID(Proparse.REPOSITIONTOROWID, "reposition-to-rowid", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  REQUEST(Proparse.REQUEST, "request", NodeTypesOption.KEYWORD),
  RESTARTROW(Proparse.RESTARTROW, "restart-row", NodeTypesOption.KEYWORD),
  RESULT(Proparse.RESULT, "result", NodeTypesOption.KEYWORD),
  RETAIN(Proparse.RETAIN, "retain", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  RETAINSHAPE(Proparse.RETAINSHAPE, "retain-shape", 8, NodeTypesOption.KEYWORD),
  RETRY(Proparse.RETRY, "retry", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  RETRYCANCEL(Proparse.RETRYCANCEL, "retry-cancel", NodeTypesOption.KEYWORD),
  RETURN(Proparse.RETURN, "return", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  RETURNS(Proparse.RETURNS, "returns", NodeTypesOption.KEYWORD), // Not a reserved keyword
  RETURNTOSTARTDIR(Proparse.RETURNTOSTARTDIR, "return-to-start-dir", 18, NodeTypesOption.KEYWORD),
  RETURNVALUE(Proparse.RETURNVALUE, "return-value", 10, NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  REVERSEFROM(Proparse.REVERSEFROM, "reverse-from", NodeTypesOption.KEYWORD),
  REVERT(Proparse.REVERT, "revert", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  REVOKE(Proparse.REVOKE, "revoke", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  RGBVALUE(Proparse.RGBVALUE, "rgb-value", 5, NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  RIGHT(Proparse.RIGHT, "right", NodeTypesOption.KEYWORD),
  RIGHTALIGNED(Proparse.RIGHTALIGNED, "right-aligned", 11, NodeTypesOption.KEYWORD),
  RIGHTANGLE(Proparse.RIGHTANGLE, ">", NodeTypesOption.SYMBOL),
  RIGHTBRACE(Proparse.RIGHTBRACE, "]", NodeTypesOption.SYMBOL),
  RIGHTCURLY(Proparse.RIGHTCURLY, "}", NodeTypesOption.SYMBOL),
  RIGHTPAREN(Proparse.RIGHTPAREN, ")", NodeTypesOption.SYMBOL),
  RIGHTTRIM(Proparse.RIGHTTRIM, "right-trim", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  RINDEX(Proparse.RINDEX, "r-index", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  ROUND(Proparse.ROUND, "round", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  ROUNDED(Proparse.ROUNDED, "rounded", NodeTypesOption.KEYWORD),
  ROUTINELEVEL(Proparse.ROUTINELEVEL, "routine-level", NodeTypesOption.KEYWORD),
  ROW(Proparse.ROW, "row", NodeTypesOption.KEYWORD),
  ROWCREATED(Proparse.ROWCREATED, "row-created", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ROWDELETED(Proparse.ROWDELETED, "row-deleted", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ROWHEIGHTCHARS(Proparse.ROWHEIGHTCHARS, "row-height", 10, "row-height-chars", 12, NodeTypesOption.KEYWORD),
  ROWHEIGHTPIXELS(Proparse.ROWHEIGHTPIXELS, "row-height-pixels", 12, NodeTypesOption.KEYWORD),
  ROWID(Proparse.ROWID, "rowid", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC), // Yes, ROWID is not a reserved keyword
  ROWMODIFIED(Proparse.ROWMODIFIED, "row-modified", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ROWOF(Proparse.ROWOF, "row-of", NodeTypesOption.KEYWORD),
  ROWSTATE(Proparse.ROWSTATE, "row-state", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  ROWUNMODIFIED(Proparse.ROWUNMODIFIED, "row-unmodified", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  RULE(Proparse.RULE, "rule", NodeTypesOption.KEYWORD),
  RUN(Proparse.RUN, "run", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  RUNPROCEDURE(Proparse.RUNPROCEDURE, "run-procedure", 8, NodeTypesOption.KEYWORD),

  // S
  SAVE(Proparse.SAVE, "save", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SAVEAS(Proparse.SAVEAS, "save-as", NodeTypesOption.KEYWORD),
  SAVECACHE(Proparse.SAVECACHE, "savecache", NodeTypesOption.KEYWORD),
  SAXATTRIBUTES(Proparse.SAXATTRIBUTES, "sax-attributes", NodeTypesOption.KEYWORD),
  SAXCOMPLETE(Proparse.SAXCOMPLETE, "sax-complete", 10, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SAXPARSERERROR(Proparse.SAXPARSERERROR, "sax-parser-error", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  SAXREADER(Proparse.SAXREADER, "sax-reader", NodeTypesOption.KEYWORD),
  SAXRUNNING(Proparse.SAXRUNNING, "sax-running", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SAXUNINITIALIZED(Proparse.SAXUNINITIALIZED, "sax-uninitialized", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  SAXWRITER(Proparse.SAXWRITER, "sax-writer", NodeTypesOption.KEYWORD),
  SAXWRITEBEGIN(Proparse.SAXWRITEBEGIN, "sax-write-begin", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  SAXWRITECOMPLETE(Proparse.SAXWRITECOMPLETE, "sax-write-complete", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  SAXWRITECONTENT(Proparse.SAXWRITECONTENT, "sax-write-content", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  SAXWRITEELEMENT(Proparse.SAXWRITEELEMENT, "sax-write-element", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  SAXWRITEERROR(Proparse.SAXWRITEERROR, "sax-write-error", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  SAXWRITEIDLE(Proparse.SAXWRITEIDLE, "sax-write-idle", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SAXWRITETAG(Proparse.SAXWRITETAG, "sax-write-tag", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SCHEMA(Proparse.SCHEMA, "schema", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SCOPEDDEFINE(Proparse.SCOPEDDEFINE, NodeTypesOption.PREPROCESSOR),
  SCREEN(Proparse.SCREEN, "screen", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SCREENIO(Proparse.SCREENIO, "screen-io", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SCREENLINES(Proparse.SCREENLINES, "screen-lines", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  SCREENVALUE(Proparse.SCREENVALUE, "screen-value", NodeTypesOption.KEYWORD),
  SCROLL(Proparse.SCROLL, "scroll", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SCROLLABLE(Proparse.SCROLLABLE, "scrollable", NodeTypesOption.KEYWORD),
  SCROLLBARHORIZONTAL(Proparse.SCROLLBARHORIZONTAL, "scrollbar-horizontal", 11, NodeTypesOption.KEYWORD),
  SCROLLBARVERTICAL(Proparse.SCROLLBARVERTICAL, "scrollbar-vertical", 11, NodeTypesOption.KEYWORD),
  SCROLLING(Proparse.SCROLLING, "scrolling", NodeTypesOption.KEYWORD),
  SDBNAME(Proparse.SDBNAME, "sdbname", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  SEARCH(Proparse.SEARCH, "search", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  SEARCHSELF(Proparse.SEARCHSELF, "search-self", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SEARCHTARGET(Proparse.SEARCHTARGET, "search-target", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SECTION(Proparse.SECTION, "section", NodeTypesOption.KEYWORD),
  SECURITYPOLICY(Proparse.SECURITYPOLICY, "security-policy", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED, NodeTypesOption.SYSHDL),
  SEEK(Proparse.SEEK, "seek", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  SELECT(Proparse.SELECT, "select", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SELECTION(Proparse.SELECTION, "selection", NodeTypesOption.KEYWORD),
  SELECTIONLIST(Proparse.SELECTIONLIST, "selection-list", NodeTypesOption.KEYWORD),
  SELF(Proparse.SELF, "self", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED, NodeTypesOption.SYSHDL),
  SEMI(Proparse.SEMI, ";", NodeTypesOption.SYMBOL),
  SEND(Proparse.SEND, "send", NodeTypesOption.KEYWORD),
  SENDSQLSTATEMENT(Proparse.SENDSQLSTATEMENT, "send-sql-statement", 8, NodeTypesOption.KEYWORD),
  SENSITIVE(Proparse.SENSITIVE, "sensitive", NodeTypesOption.KEYWORD),
  SEPARATECONNECTION(Proparse.SEPARATECONNECTION, "separate-connection", NodeTypesOption.KEYWORD),
  SEPARATORS(Proparse.SEPARATORS, "separators", NodeTypesOption.KEYWORD),
  SERIALIZABLE(Proparse.SERIALIZABLE, "serializable", NodeTypesOption.KEYWORD),
  SERIALIZEHIDDEN(Proparse.SERIALIZEHIDDEN, "serialize-hidden", NodeTypesOption.KEYWORD),
  SERIALIZENAME(Proparse.SERIALIZENAME, "serialize-name", NodeTypesOption.KEYWORD),
  SERVER(Proparse.SERVER, "server", NodeTypesOption.KEYWORD),
  SERVERSOCKET(Proparse.SERVERSOCKET, "server-socket", NodeTypesOption.KEYWORD),
  SESSION(Proparse.SESSION, "session", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.SYSHDL),
  SET(Proparse.SET, "set", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SETATTRCALLTYPE(Proparse.SETATTRCALLTYPE, "set-attr-call-type", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  SETBYTEORDER(Proparse.SETBYTEORDER, "set-byte-order", NodeTypesOption.KEYWORD),
  SETCONTENTS(Proparse.SETCONTENTS, "set-contents", NodeTypesOption.KEYWORD),
  SETCURRENTVALUE(Proparse.SETCURRENTVALUE, "set-current-value", NodeTypesOption.KEYWORD),
  SETDBCLIENT(Proparse.SETDBCLIENT, "set-db-client", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  SETEFFECTIVETENANT(Proparse.SETEFFECTIVETENANT, "set-effective-tenant", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  SETPOINTERVALUE(Proparse.SETPOINTERVALUE, "set-pointer-value", 15, NodeTypesOption.KEYWORD),
  SETSIZE(Proparse.SETSIZE, "set-size", NodeTypesOption.KEYWORD),
  SETUSERID(Proparse.SETUSERID, "setuserid", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  SHA1DIGEST(Proparse.SHA1DIGEST, "sha1-digest", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  SHARED(Proparse.SHARED, "shared", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SHARELOCK(Proparse.SHARELOCK, "share-lock", 5, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SHORT(Proparse.SHORT, "short", NodeTypesOption.KEYWORD),
  SHOWSTATS(Proparse.SHOWSTATS, "show-stats", 9, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SIDELABELS(Proparse.SIDELABELS, "side-labels", 8, NodeTypesOption.KEYWORD),
  SIGNATURE(Proparse.SIGNATURE, "signature", NodeTypesOption.KEYWORD),
  SILENT(Proparse.SILENT, "silent", NodeTypesOption.KEYWORD),
  SIMPLE(Proparse.SIMPLE, "simple", NodeTypesOption.KEYWORD),
  SINGLE(Proparse.SINGLE, "single", NodeTypesOption.KEYWORD),
  SINGLERUN(Proparse.SINGLERUN, "single-run", NodeTypesOption.KEYWORD),
  SINGLETON(Proparse.SINGLETON, "singleton", NodeTypesOption.KEYWORD),
  SINGLEQUOTE(Proparse.SINGLEQUOTE, "'", NodeTypesOption.SYMBOL),
  SIZE(Proparse.SIZE, "size", NodeTypesOption.KEYWORD),
  SIZECHARS(Proparse.SIZECHARS, "size-chars", 6, NodeTypesOption.KEYWORD),
  SIZEPIXELS(Proparse.SIZEPIXELS, "size-pixels", 6, NodeTypesOption.KEYWORD),
  SKIP(Proparse.SKIP, "skip", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SKIPDELETEDRECORD(Proparse.SKIPDELETEDRECORD, "skip-deleted-record", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  SKIPGROUPDUPLICATES(Proparse.SKIPGROUPDUPLICATES, "skip-group-duplicates", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  SLASH(Proparse.SLASH, "/", NodeTypesOption.SYMBOL),
  SLIDER(Proparse.SLIDER, "slider", NodeTypesOption.KEYWORD),
  SMALLINT(Proparse.SMALLINT, "smallint", NodeTypesOption.KEYWORD),
  SOAPHEADER(Proparse.SOAPHEADER, "soap-header", NodeTypesOption.KEYWORD),
  SOAPHEADERENTRYREF(Proparse.SOAPHEADERENTRYREF, "soap-header-entryref", NodeTypesOption.KEYWORD),
  SOCKET(Proparse.SOCKET, "socket", NodeTypesOption.KEYWORD),
  SOME(Proparse.SOME, "some", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SORT(Proparse.SORT, "sort", NodeTypesOption.KEYWORD),
  SOURCE(Proparse.SOURCE, "source", NodeTypesOption.KEYWORD),
  SOURCEPROCEDURE(Proparse.SOURCEPROCEDURE, "source-procedure", NodeTypesOption.KEYWORD,
      NodeTypesOption.SYSHDL),
  SPACE(Proparse.SPACE, "space", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SQL(Proparse.SQL, "sql", NodeTypesOption.KEYWORD),
  SQRT(Proparse.SQRT, "sqrt", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  SQSTRING(Proparse.SQSTRING),
  SSLSERVERNAME(Proparse.SSLSERVERNAME, "ssl-server-name", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  STAR(Proparse.STAR, "*", NodeTypesOption.SYMBOL),
  START(Proparse.START, "start", NodeTypesOption.KEYWORD),
  STARTING(Proparse.STARTING, "starting", NodeTypesOption.KEYWORD),
  STARTMOVE(Proparse.STARTMOVE, "start-move", NodeTypesOption.KEYWORD),
  STARTRESIZE(Proparse.STARTRESIZE, "start-resize", NodeTypesOption.KEYWORD),
  STARTROWRESIZE(Proparse.STARTROWRESIZE, "start-row-resize", NodeTypesOption.KEYWORD),
  STATIC(Proparse.STATIC, "static", NodeTypesOption.KEYWORD),
  STATUS(Proparse.STATUS, "status", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  STATUSBAR(Proparse.STATUSBAR, "status-bar", NodeTypesOption.KEYWORD),
  STDCALL(Proparse.STDCALL_KW, "stdcall", NodeTypesOption.KEYWORD),
  STOMPDETECTION(Proparse.STOMPDETECTION, "stomp-detection", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  STOMPFREQUENCY(Proparse.STOMPFREQUENCY, "stomp-frequency", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  STOP(Proparse.STOP, "stop", NodeTypesOption.KEYWORD),
  STOPAFTER(Proparse.STOPAFTER, "stop-after", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  STOREDPROCEDURE(Proparse.STOREDPROCEDURE, "stored-procedure", 11, NodeTypesOption.KEYWORD),
  STREAM(Proparse.STREAM, "stream", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  STREAMHANDLE(Proparse.STREAMHANDLE, "stream-handle", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  STREAMIO(Proparse.STREAMIO, "stream-io", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  STRETCHTOFIT(Proparse.STRETCHTOFIT, "stretch-to-fit", NodeTypesOption.KEYWORD),
  STRING(Proparse.STRING, "string", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  STRINGXREF(Proparse.STRINGXREF, "string-xref", NodeTypesOption.KEYWORD),
  SUBAVERAGE(Proparse.SUBAVERAGE, "sub-average", 7, NodeTypesOption.KEYWORD),
  SUBCOUNT(Proparse.SUBCOUNT, "sub-count", NodeTypesOption.KEYWORD),
  SUBMAXIMUM(Proparse.SUBMAXIMUM, "sub-maximum", 7, NodeTypesOption.KEYWORD),
  SUBMENU(Proparse.SUBMENU, "sub-menu", 4, NodeTypesOption.KEYWORD),
  SUBMENUHELP(Proparse.SUBMENUHELP, "sub-menu-help", NodeTypesOption.KEYWORD),
  SUBMINIMUM(Proparse.SUBMINIMUM, "sub-minimum", 7, NodeTypesOption.KEYWORD),
  SUBSCRIBE(Proparse.SUBSCRIBE, "subscribe", NodeTypesOption.KEYWORD),
  SUBSTITUTE(Proparse.SUBSTITUTE, "substitute", 5, NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  SUBSTRING(Proparse.SUBSTRING, "substring", 6, NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  SUBTOTAL(Proparse.SUBTOTAL, "sub-total", NodeTypesOption.KEYWORD),
  SUM(Proparse.SUM, "sum", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  SUMMARY(Proparse.SUMMARY, "summary", NodeTypesOption.KEYWORD),
  SUPER(Proparse.SUPER, "super", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_NO_ARG_FUNC,
      NodeTypesOption.MAY_BE_REGULAR_FUNC, NodeTypesOption.SYSHDL),
  SYMMETRICENCRYPTIONALGORITHM(Proparse.SYMMETRICENCRYPTIONALGORITHM, "symmetric-encryption-algorithm",
      NodeTypesOption.KEYWORD),
  SYMMETRICENCRYPTIONIV(Proparse.SYMMETRICENCRYPTIONIV, "symmetric-encryption-iv", NodeTypesOption.KEYWORD),
  SYMMETRICENCRYPTIONKEY(Proparse.SYMMETRICENCRYPTIONKEY, "symmetric-encryption-key",
      NodeTypesOption.KEYWORD),
  SYMMETRICSUPPORT(Proparse.SYMMETRICSUPPORT, "symmetric-support", NodeTypesOption.KEYWORD),
  SYSTEMDIALOG(Proparse.SYSTEMDIALOG, "system-dialog", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SYSTEMHELP(Proparse.SYSTEMHELP, "system-help", NodeTypesOption.KEYWORD),
  SCANNER_HEAD(Proparse.Scanner_head, NodeTypesOption.STRUCTURE),
  SCANNER_TAIL(Proparse.Scanner_tail, NodeTypesOption.STRUCTURE),
  SQL_BEGINS(Proparse.Sql_begins, NodeTypesOption.STRUCTURE),
  SQL_BETWEEN(Proparse.Sql_between, NodeTypesOption.STRUCTURE),
  SQL_COMP_QUERY(Proparse.Sql_comp_query, NodeTypesOption.STRUCTURE),
  SQL_IN(Proparse.Sql_in, NodeTypesOption.STRUCTURE),
  SQL_LIKE(Proparse.Sql_like, NodeTypesOption.STRUCTURE),
  SQL_NULL_TEST(Proparse.Sql_null_test, NodeTypesOption.STRUCTURE),
  SQL_SELECT_WHAT(Proparse.Sql_select_what, NodeTypesOption.STRUCTURE),

  // T
  TABLE(Proparse.TABLE, "table", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TABLEHANDLE(Proparse.TABLEHANDLE, "table-handle", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TABLENUMBER(Proparse.TABLENUMBER, "table-number", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TABLESCAN(Proparse.TABLESCAN, "table-scan", NodeTypesOption.KEYWORD),
  TARGET(Proparse.TARGET, "target", NodeTypesOption.KEYWORD),
  TARGETPROCEDURE(Proparse.TARGETPROCEDURE, "target-procedure", NodeTypesOption.KEYWORD,
      NodeTypesOption.SYSHDL),
  TEMPTABLE(Proparse.TEMPTABLE, "temp-table", NodeTypesOption.KEYWORD),
  TENANT(Proparse.TENANT, "tenant", NodeTypesOption.KEYWORD),
  TENANTID(Proparse.TENANTID, "tenant-id", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  TENANTNAME(Proparse.TENANTNAME, "tenant-name", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  TENANTNAMETOID(Proparse.TENANTNAMETOID, "tenant-name-to-id", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  TENANTWHERE(Proparse.TENANTWHERE, "tenant-where", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TERMINAL(Proparse.TERMINAL, "term", "terminal", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  TERMINATE(Proparse.TERMINATE, "terminate", NodeTypesOption.KEYWORD),
  TEXT(Proparse.TEXT, "text", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TEXTCURSOR(Proparse.TEXTCURSOR, "text-cursor", NodeTypesOption.KEYWORD, NodeTypesOption.SYSHDL),
  TEXTSEGGROW(Proparse.TEXTSEGGROW, "text-seg-growth", 8, NodeTypesOption.KEYWORD),
  THEN(Proparse.THEN, "then", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  THISOBJECT(Proparse.THISOBJECT, "this-object", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.SYSHDL),
  THISPROCEDURE(Proparse.THISPROCEDURE, "this-procedure", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.SYSHDL),
  THREED(Proparse.THREED, "three-d", NodeTypesOption.KEYWORD),
  THROUGH(Proparse.THROUGH, "through", "thru", NodeTypesOption.KEYWORD),
  THROW(Proparse.THROW, "throw", NodeTypesOption.KEYWORD),
  TICMARKS(Proparse.TICMARKS, "tic-marks", NodeTypesOption.KEYWORD),
  TILDE(Proparse.TILDE, "~", NodeTypesOption.SYMBOL),
  TIME(Proparse.TIME, "time", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  TIMESTAMP(Proparse.TIMESTAMP, "timestamp", NodeTypesOption.KEYWORD),
  TIMEZONE(Proparse.TIMEZONE, "timezone", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  TITLE(Proparse.TITLE, "title", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TO(Proparse.TO, "to", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TODAY(Proparse.TODAY, "today", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  TOGGLEBOX(Proparse.TOGGLEBOX, "toggle-box", NodeTypesOption.KEYWORD),
  TOOLBAR(Proparse.TOOLBAR, "tool-bar", NodeTypesOption.KEYWORD),
  TOOLTIP(Proparse.TOOLTIP, "tooltip", NodeTypesOption.KEYWORD),
  TOP(Proparse.TOP, "top", NodeTypesOption.KEYWORD),
  TOPIC(Proparse.TOPIC, "topic", NodeTypesOption.KEYWORD),
  TOPNAVQUERY(Proparse.TOPNAVQUERY, "top-nav-query", NodeTypesOption.KEYWORD),
  TOPONLY(Proparse.TOPONLY, "top-only", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TOROWID(Proparse.TOROWID, "to-rowid", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  TOTAL(Proparse.TOTAL, "total", NodeTypesOption.KEYWORD),
  TRAILING(Proparse.TRAILING, "trailing", 5, NodeTypesOption.KEYWORD),
  TRANSACTION(Proparse.TRANSACTION, "trans", 5, "transaction", 8, NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED, NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  TRANSACTIONMODE(Proparse.TRANSACTIONMODE, "transaction-mode", NodeTypesOption.KEYWORD),
  TRANSINITPROCEDURE(Proparse.TRANSINITPROCEDURE, "trans-init-procedure", NodeTypesOption.KEYWORD),
  TRANSPARENT(Proparse.TRANSPARENT, "transparent", 8, NodeTypesOption.KEYWORD),
  TRIGGER(Proparse.TRIGGER, "trigger", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TRIGGERS(Proparse.TRIGGERS, "triggers", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TRIM(Proparse.TRIM, "trim", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  TRUE(Proparse.TRUE_KW, "true", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TRUNCATE(Proparse.TRUNCATE, "truncate", 5, NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  TTCODEPAGE(Proparse.TTCODEPAGE, "ttcodepage", NodeTypesOption.KEYWORD),
  TYPE_NAME(Proparse.TYPE_NAME, NodeTypesOption.STRUCTURE),
  TYPELESS_TOKEN(Proparse.TYPELESS_TOKEN, NodeTypesOption.STRUCTURE),
  TYPEOF(Proparse.TYPEOF, "type-of", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),

  // U
  UNARY_MINUS(Proparse.UNARY_MINUS, "-", NodeTypesOption.SYMBOL),
  UNARY_PLUS(Proparse.UNARY_PLUS, "+", NodeTypesOption.SYMBOL),
  UNBOX(Proparse.UNBOX, "unbox", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  UNBUFFERED(Proparse.UNBUFFERED, "unbuffered", 6, NodeTypesOption.KEYWORD),
  UNDERLINE(Proparse.UNDERLINE, "underline", 6, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  UNDO(Proparse.UNDO, "undo", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  UNFORMATTED(Proparse.UNFORMATTED, "unformatted", 6, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  UNION(Proparse.UNION, "union", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  UNIQUE(Proparse.UNIQUE, "unique", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  UNIQUEMATCH(Proparse.UNIQUEMATCH, "unique-match", NodeTypesOption.KEYWORD),
  UNIX(Proparse.UNIX, "unix", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  UNKNOWNVALUE(Proparse.UNKNOWNVALUE, "?", NodeTypesOption.SYMBOL),
  UNLESSHIDDEN(Proparse.UNLESSHIDDEN, "unless-hidden", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  UNLOAD(Proparse.UNLOAD, "unload", NodeTypesOption.KEYWORD),
  UNQUOTEDSTRING(Proparse.UNQUOTEDSTRING),
  UNSIGNEDBYTE(Proparse.UNSIGNEDBYTE, "unsigned-byte", NodeTypesOption.KEYWORD),
  UNSIGNEDSHORT(Proparse.UNSIGNEDSHORT, "unsigned-short", NodeTypesOption.KEYWORD),
  UNSUBSCRIBE(Proparse.UNSUBSCRIBE, "unsubscribe", NodeTypesOption.KEYWORD),
  UP(Proparse.UP, "up", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  UPDATE(Proparse.UPDATE, "update", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  URLDECODE(Proparse.URLDECODE, "url-decode", NodeTypesOption.KEYWORD),
  URLENCODE(Proparse.URLENCODE, "url-encode", NodeTypesOption.KEYWORD),
  USE(Proparse.USE, "use", NodeTypesOption.KEYWORD),
  USEDICTEXPS(Proparse.USEDICTEXPS, "use-dict-exps", 7, NodeTypesOption.KEYWORD),
  USEFILENAME(Proparse.USEFILENAME, "use-filename", NodeTypesOption.KEYWORD),
  USEINDEX(Proparse.USEINDEX, "use-index", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  USER(Proparse.USER, "user", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_NO_ARG_FUNC,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  USEREVVIDEO(Proparse.USEREVVIDEO, "use-revvideo", NodeTypesOption.KEYWORD),
  USERID(Proparse.USERID, "userid", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  USER_FUNC(Proparse.USER_FUNC, NodeTypesOption.STRUCTURE),
  USETEXT(Proparse.USETEXT, "use-text", NodeTypesOption.KEYWORD),
  USEUNDERLINE(Proparse.USEUNDERLINE, "use-underline", NodeTypesOption.KEYWORD),
  USEWIDGETPOOL(Proparse.USEWIDGETPOOL, "use-widget-pool", NodeTypesOption.KEYWORD),
  USING(Proparse.USING, "using", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),

  // V
  V6FRAME(Proparse.V6FRAME, "v6frame", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  VALIDATE(Proparse.VALIDATE, "validate", NodeTypesOption.KEYWORD),
  VALIDEVENT(Proparse.VALIDEVENT, "valid-event", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  VALIDHANDLE(Proparse.VALIDHANDLE, "valid-handle", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  VALIDOBJECT(Proparse.VALIDOBJECT, "valid-object", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  VALUE(Proparse.VALUE, "value", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  VALUECHANGED(Proparse.VALUECHANGED, "value-changed", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  VALUES(Proparse.VALUES, "values", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  VARIABLE(Proparse.VARIABLE, "variable", 3, NodeTypesOption.KEYWORD),
  VERBOSE(Proparse.VERBOSE, "verbose", 4, NodeTypesOption.KEYWORD),
  VERTICAL(Proparse.VERTICAL, "vertical", 4, NodeTypesOption.KEYWORD),
  VIEW(Proparse.VIEW, "view", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  VIEWAS(Proparse.VIEWAS, "view-as", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  VISIBLE(Proparse.VISIBLE, "visible", NodeTypesOption.KEYWORD),
  VMS(Proparse.VMS, "vms", NodeTypesOption.KEYWORD),
  VOID(Proparse.VOID, "void", NodeTypesOption.KEYWORD),

  // W
  WAIT(Proparse.WAIT, "wait", NodeTypesOption.KEYWORD),
  WAITFOR(Proparse.WAITFOR, "wait-for", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WARNING(Proparse.WARNING, "warning", NodeTypesOption.KEYWORD),
  WEBCONTEXT(Proparse.WEBCONTEXT, "web-context", 7, NodeTypesOption.KEYWORD, NodeTypesOption.SYSHDL),
  WEEKDAY(Proparse.WEEKDAY, "weekday", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  WHEN(Proparse.WHEN, "when", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WHERE(Proparse.WHERE, "where", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WHILE(Proparse.WHILE, "while", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WIDGET(Proparse.WIDGET, "widget", NodeTypesOption.KEYWORD),
  WIDGETHANDLE(Proparse.WIDGETHANDLE, "widget-handle", 8, NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  WIDGETID(Proparse.WIDGETID, "widget-id", NodeTypesOption.KEYWORD),
  WIDGETPOOL(Proparse.WIDGETPOOL, "widget-pool", NodeTypesOption.KEYWORD),
  WIDTH(Proparse.WIDTH, "width", NodeTypesOption.KEYWORD),
  WIDTHCHARS(Proparse.WIDTHCHARS, "width-chars", 7, NodeTypesOption.KEYWORD),
  WIDTHPIXELS(Proparse.WIDTHPIXELS, "width-pixels", 7, NodeTypesOption.KEYWORD),
  WINDOW(Proparse.WINDOW, "window", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WINDOWDELAYEDMINIMIZE(Proparse.WINDOWDELAYEDMINIMIZE, "window-delayed-minimize", 18,
      NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WINDOWMAXIMIZED(Proparse.WINDOWMAXIMIZED, "window-maximized", 12, NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  WINDOWMINIMIZED(Proparse.WINDOWMINIMIZED, "window-minimized", 12, NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  WINDOWNAME(Proparse.WINDOWNAME, "window-name", NodeTypesOption.KEYWORD),
  WINDOWNORMAL(Proparse.WINDOWNORMAL, "window-normal", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WITH(Proparse.WITH, "with", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WORDINDEX(Proparse.WORDINDEX, "word-index", NodeTypesOption.KEYWORD),
  WORKTABLE(Proparse.WORKTABLE, "work-table", 8, "workfile", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  WRITE(Proparse.WRITE, "write", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WS(Proparse.WS),
  WIDGET_REF(Proparse.Widget_ref, NodeTypesOption.STRUCTURE),
  WITH_COLUMNS(Proparse.With_columns, NodeTypesOption.STRUCTURE),
  WITH_DOWN(Proparse.With_down, NodeTypesOption.STRUCTURE),

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
  XREFXML(Proparse.XREFXML, "xref-xml", NodeTypesOption.KEYWORD),

  // Y
  Y(Proparse.Y, "y", NodeTypesOption.KEYWORD),
  YEAR(Proparse.YEAR, "year", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  YES(Proparse.YES, "yes", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  YESNO(Proparse.YESNO, "yes-no", NodeTypesOption.KEYWORD),
  YESNOCANCEL(Proparse.YESNOCANCEL, "yes-no-cancel", NodeTypesOption.KEYWORD),
  YOF(Proparse.YOF, "y-of", NodeTypesOption.KEYWORD);

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

  public boolean isSystemHandleName() {
    return options.contains(NodeTypesOption.SYSHDL);
  }

  public boolean mayBeNoArgFunc() {
    return options.contains(NodeTypesOption.MAY_BE_NO_ARG_FUNC);
  }

  public boolean mayBeRegularFunc() {
    return options.contains(NodeTypesOption.MAY_BE_REGULAR_FUNC);
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
    return type.isSystemHandleName();
  }

  static boolean mayBeNoArgFunc(int nodeType) {
    ABLNodeType type = typeMap.get(nodeType);
    if (type == null)
      return false;
    return type.mayBeNoArgFunc();
  }

  static boolean mayBeRegularFunc(int nodeType) {
    ABLNodeType type = typeMap.get(nodeType);
    if (type == null)
      return false;
    return type.mayBeRegularFunc();
  }

}
