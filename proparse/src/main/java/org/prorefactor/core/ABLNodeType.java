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
import org.prorefactor.proparse.ProParserTokenTypes;

import com.google.common.base.Strings;

public enum ABLNodeType {
  // Placeholders and unknown tokens
  EMPTY_NODE(-1000, NodeTypesOption.PLACEHOLDER),
  INVALID_NODE(Token.INVALID_TYPE, NodeTypesOption.PLACEHOLDER),
  EOF_ANTLR4(Token.EOF, NodeTypesOption.PLACEHOLDER),
  INCLUDEDIRECTIVE(ProParserTokenTypes.INCLUDEDIRECTIVE, NodeTypesOption.PLACEHOLDER),

  // A
  AACBIT(ProParserTokenTypes.AACBIT, "_cbit", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  AACONTROL(ProParserTokenTypes.AACONTROL, "_control", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  AALIST(ProParserTokenTypes.AALIST, "_list", NodeTypesOption.KEYWORD),
  AAMEMORY(ProParserTokenTypes.AAMEMORY, "_memory", NodeTypesOption.KEYWORD, NodeTypesOption.SYSHDL),
  AAMSG(ProParserTokenTypes.AAMSG, "_msg", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  AAPCONTROL(ProParserTokenTypes.AAPCONTROL, "_pcontrol", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  AASERIAL(ProParserTokenTypes.AASERIAL, "_serial-num", 7, NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  AATRACE(ProParserTokenTypes.AATRACE, "_trace", NodeTypesOption.KEYWORD),
  ABSOLUTE(ProParserTokenTypes.ABSOLUTE, "absolute", 3, NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  ABSTRACT(ProParserTokenTypes.ABSTRACT, "abstract", NodeTypesOption.KEYWORD),
  ACCELERATOR(ProParserTokenTypes.ACCELERATOR, "accelerator", NodeTypesOption.KEYWORD),
  ACCUMULATE(ProParserTokenTypes.ACCUMULATE, "accumulate", 5, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ACTIVEFORM(ProParserTokenTypes.ACTIVEFORM, "active-form", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.SYSHDL),
  ACTIVEWINDOW(ProParserTokenTypes.ACTIVEWINDOW, "active-window", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.SYSHDL),
  ADD(ProParserTokenTypes.ADD, "add", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ADDINTERVAL(ProParserTokenTypes.ADDINTERVAL, "add-interval", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  ADVISE(ProParserTokenTypes.ADVISE, "advise", NodeTypesOption.KEYWORD),
  ALERTBOX(ProParserTokenTypes.ALERTBOX, "alert-box", NodeTypesOption.KEYWORD),
  ALIAS(ProParserTokenTypes.ALIAS, "alias", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  ALL(ProParserTokenTypes.ALL, "all", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ALLOWREPLICATION(ProParserTokenTypes.ALLOWREPLICATION, "allow-replication", NodeTypesOption.KEYWORD),
  ALTER(ProParserTokenTypes.ALTER, "alter", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ALTERNATEKEY(ProParserTokenTypes.ALTERNATEKEY, "alternate-key", NodeTypesOption.KEYWORD),
  AMBIGUOUS(ProParserTokenTypes.AMBIGUOUS, "ambiguous", 5, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  AMPANALYZERESUME(ProParserTokenTypes.AMPANALYZERESUME, NodeTypesOption.PREPROCESSOR),
  AMPANALYZESUSPEND(ProParserTokenTypes.AMPANALYZESUSPEND, NodeTypesOption.PREPROCESSOR),
  AMPELSE(ProParserTokenTypes.AMPELSE, NodeTypesOption.PREPROCESSOR),
  AMPELSEIF(ProParserTokenTypes.AMPELSEIF, NodeTypesOption.PREPROCESSOR),
  AMPENDIF(ProParserTokenTypes.AMPENDIF, NodeTypesOption.PREPROCESSOR),
  AMPGLOBALDEFINE(ProParserTokenTypes.AMPGLOBALDEFINE, NodeTypesOption.PREPROCESSOR),
  AMPIF(ProParserTokenTypes.AMPIF, NodeTypesOption.PREPROCESSOR),
  AMPMESSAGE(ProParserTokenTypes.AMPMESSAGE, NodeTypesOption.PREPROCESSOR),
  AMPSCOPEDDEFINE(ProParserTokenTypes.AMPSCOPEDDEFINE, NodeTypesOption.PREPROCESSOR),
  AMPTHEN(ProParserTokenTypes.AMPTHEN, NodeTypesOption.PREPROCESSOR),
  AMPUNDEFINE(ProParserTokenTypes.AMPUNDEFINE, NodeTypesOption.PREPROCESSOR),
  ANALYZE(ProParserTokenTypes.ANALYZE, "analyze", 6, NodeTypesOption.KEYWORD),
  AND(ProParserTokenTypes.AND, "and", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ANNOTATION(ProParserTokenTypes.ANNOTATION),
  ANSIONLY(ProParserTokenTypes.ANSIONLY, "ansi-only", NodeTypesOption.KEYWORD),
  ANY(ProParserTokenTypes.ANY, "any", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ANYWHERE(ProParserTokenTypes.ANYWHERE, "anywhere", NodeTypesOption.KEYWORD),
  APPEND(ProParserTokenTypes.APPEND, "append", NodeTypesOption.KEYWORD),
  APPLICATION(ProParserTokenTypes.APPLICATION, "application", NodeTypesOption.KEYWORD),
  APPLY(ProParserTokenTypes.APPLY, "apply", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ARRAYMESSAGE(ProParserTokenTypes.ARRAYMESSAGE, "array-message", 7, NodeTypesOption.KEYWORD),
  AS(ProParserTokenTypes.AS, "as", NodeTypesOption.KEYWORD),
  ASC(ProParserTokenTypes.ASC, "asc", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  ASCENDING(ProParserTokenTypes.ASCENDING, "ascending", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  ASKOVERWRITE(ProParserTokenTypes.ASKOVERWRITE, "ask-overwrite", NodeTypesOption.KEYWORD),
  ASSEMBLY(ProParserTokenTypes.ASSEMBLY, "assembly", NodeTypesOption.KEYWORD),
  ASSIGN(ProParserTokenTypes.ASSIGN, "assign", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ASSIGN_DYNAMIC_NEW(ProParserTokenTypes.Assign_dynamic_new, NodeTypesOption.STRUCTURE),
  ASYNCHRONOUS(ProParserTokenTypes.ASYNCHRONOUS, "asynchronous", NodeTypesOption.KEYWORD),
  AT(ProParserTokenTypes.AT, "at", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ATTACHMENT(ProParserTokenTypes.ATTACHMENT, "attachment", 6, NodeTypesOption.KEYWORD),
  ATTRSPACE(ProParserTokenTypes.ATTRSPACE, "attr-space", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  AUDITCONTROL(ProParserTokenTypes.AUDITCONTROL, "audit-control", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.SYSHDL),
  AUDITENABLED(ProParserTokenTypes.AUDITENABLED, "audit-enabled", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  AUDITPOLICY(ProParserTokenTypes.AUDITPOLICY, "audit-policy", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.SYSHDL),
  AUTHORIZATION(ProParserTokenTypes.AUTHORIZATION, "authorization", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  AUTOCOMPLETION(ProParserTokenTypes.AUTOCOMPLETION, "auto-completion", 9, NodeTypesOption.KEYWORD),
  AUTOENDKEY(ProParserTokenTypes.AUTOENDKEY, "auto-end-key", "auto-endkey", NodeTypesOption.KEYWORD),
  AUTOGO(ProParserTokenTypes.AUTOGO, "auto-go", NodeTypesOption.KEYWORD),
  AUTOMATIC(ProParserTokenTypes.AUTOMATIC, "automatic", NodeTypesOption.KEYWORD),
  AUTORETURN(ProParserTokenTypes.AUTORETURN, "auto-return", 8, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  AVAILABLE(ProParserTokenTypes.AVAILABLE, "available", 5, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  AVERAGE(ProParserTokenTypes.AVERAGE, "average", 3, NodeTypesOption.KEYWORD),
  AVG(ProParserTokenTypes.AVG, "avg", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  AGGREGATE_PHRASE(ProParserTokenTypes.Aggregate_phrase, NodeTypesOption.STRUCTURE),
  ARRAY_SUBSCRIPT(ProParserTokenTypes.Array_subscript, NodeTypesOption.STRUCTURE),
  ASSIGN_FROM_BUFFER(ProParserTokenTypes.Assign_from_buffer, NodeTypesOption.STRUCTURE),
  AUTOMATION_OBJECT(ProParserTokenTypes.Automationobject, NodeTypesOption.STRUCTURE),

  // B
  BACKGROUND(ProParserTokenTypes.BACKGROUND, "background", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BACKSLASH(ProParserTokenTypes.BACKSLASH, "\\", NodeTypesOption.SYMBOL),
  BACKTICK(ProParserTokenTypes.BACKTICK, "`", NodeTypesOption.SYMBOL),
  BACKWARDS(ProParserTokenTypes.BACKWARDS, "backwards", 8, NodeTypesOption.KEYWORD),
  BASE64(ProParserTokenTypes.BASE64, "base64", NodeTypesOption.KEYWORD),
  BASE64DECODE(ProParserTokenTypes.BASE64DECODE, "base64-decode", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  BASE64ENCODE(ProParserTokenTypes.BASE64ENCODE, "base64-encode", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  BASEKEY(ProParserTokenTypes.BASEKEY, "base-key", NodeTypesOption.KEYWORD),
  BATCHSIZE(ProParserTokenTypes.BATCHSIZE, "batch-size", NodeTypesOption.KEYWORD),
  BEFOREHIDE(ProParserTokenTypes.BEFOREHIDE, "before-hide", 8, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BEFORETABLE(ProParserTokenTypes.BEFORETABLE, "before-table", NodeTypesOption.KEYWORD),
  BEGINS(ProParserTokenTypes.BEGINS, "begins", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BELL(ProParserTokenTypes.BELL, "bell", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BETWEEN(ProParserTokenTypes.BETWEEN, "between", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BGCOLOR(ProParserTokenTypes.BGCOLOR, "bgcolor", 3, NodeTypesOption.KEYWORD),
  BIGENDIAN(ProParserTokenTypes.BIGENDIAN, "big-endian", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BIGINT(ProParserTokenTypes.BIGINT, "bigint", NodeTypesOption.KEYWORD),
  BINARY(ProParserTokenTypes.BINARY, "binary", NodeTypesOption.KEYWORD),
  BIND(ProParserTokenTypes.BIND, "bind", NodeTypesOption.KEYWORD),
  BINDWHERE(ProParserTokenTypes.BINDWHERE, "bind-where", NodeTypesOption.KEYWORD),
  BLANK(ProParserTokenTypes.BLANK, "blank", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BLOB(ProParserTokenTypes.BLOB, "blob", NodeTypesOption.KEYWORD),
  BLOCK_LABEL(ProParserTokenTypes.BLOCK_LABEL, NodeTypesOption.STRUCTURE),
  BLOCKLEVEL(ProParserTokenTypes.BLOCKLEVEL, "block-level", NodeTypesOption.KEYWORD),
  BOTH(ProParserTokenTypes.BOTH, "both", NodeTypesOption.KEYWORD),
  BOTTOM(ProParserTokenTypes.BOTTOM, "bottom", NodeTypesOption.KEYWORD),
  BOX(ProParserTokenTypes.BOX, "box", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  BREAK(ProParserTokenTypes.BREAK, "break", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BROWSE(ProParserTokenTypes.BROWSE, "browse", NodeTypesOption.KEYWORD),
  BTOS(ProParserTokenTypes.BTOS, "btos", NodeTypesOption.KEYWORD),
  BUFFER(ProParserTokenTypes.BUFFER, "buffer", NodeTypesOption.KEYWORD),
  BUFFERCHARS(ProParserTokenTypes.BUFFERCHARS, "buffer-chars", NodeTypesOption.KEYWORD),
  BUFFERCOMPARE(ProParserTokenTypes.BUFFERCOMPARE, "buffer-compare", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BUFFERCOPY(ProParserTokenTypes.BUFFERCOPY, "buffer-copy", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BUFFERGROUPID(ProParserTokenTypes.BUFFERGROUPID, "buffer-group-id", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  BUFFERGROUPNAME(ProParserTokenTypes.BUFFERGROUPNAME, "buffer-group-name", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  BUFFERLINES(ProParserTokenTypes.BUFFERLINES, "buffer-lines", NodeTypesOption.KEYWORD),
  BUFFERNAME(ProParserTokenTypes.BUFFERNAME, "buffer-name", 8, NodeTypesOption.KEYWORD),
  BUFFERTENANTNAME(ProParserTokenTypes.BUFFERTENANTNAME, "buffer-tenant-name", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  BUFFERTENANTID(ProParserTokenTypes.BUFFERTENANTID, "buffer-tenant-id", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  BUTTON(ProParserTokenTypes.BUTTON, "button", NodeTypesOption.KEYWORD),
  BUTTONS(ProParserTokenTypes.BUTTONS, "buttons", NodeTypesOption.KEYWORD),
  BY(ProParserTokenTypes.BY, "by", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BYPOINTER(ProParserTokenTypes.BYPOINTER, "by-pointer", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  BYREFERENCE(ProParserTokenTypes.BYREFERENCE, "by-reference", NodeTypesOption.KEYWORD),
  BYTE(ProParserTokenTypes.BYTE, "byte", NodeTypesOption.KEYWORD),
  BYVALUE(ProParserTokenTypes.BYVALUE, "by-value", NodeTypesOption.KEYWORD),
  BYVARIANTPOINTER(ProParserTokenTypes.BYVARIANTPOINTER, "by-variant-pointer", 16, NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  BLOCK_ITERATOR(ProParserTokenTypes.Block_iterator, NodeTypesOption.STRUCTURE),

  // C
  CACHE(ProParserTokenTypes.CACHE, "cache", NodeTypesOption.KEYWORD),
  CACHESIZE(ProParserTokenTypes.CACHESIZE, "cache-size", NodeTypesOption.KEYWORD),
  CALL(ProParserTokenTypes.CALL, "call", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CANCELBUTTON(ProParserTokenTypes.CANCELBUTTON, "cancel-button", NodeTypesOption.KEYWORD),
  CANDO(ProParserTokenTypes.CANDO, "can-do", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  CANFIND(ProParserTokenTypes.CANFIND, "can-find", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  CANQUERY(ProParserTokenTypes.CANQUERY, "can-query", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  CANSET(ProParserTokenTypes.CANSET, "can-set", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  CAPS(ProParserTokenTypes.CAPS, "caps", "upper", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  CARET(ProParserTokenTypes.CARET, "^", NodeTypesOption.SYMBOL),
  CASE(ProParserTokenTypes.CASE, "case", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CASESENSITIVE(ProParserTokenTypes.CASESENSITIVE, "case-sensitive", 8, NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  CAST(ProParserTokenTypes.CAST, "cast", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  CATCH(ProParserTokenTypes.CATCH, "catch", NodeTypesOption.KEYWORD),
  CDECL(ProParserTokenTypes.CDECL_KW, "cdecl", NodeTypesOption.KEYWORD),
  CENTERED(ProParserTokenTypes.CENTERED, "centered", 6, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CHAINED(ProParserTokenTypes.CHAINED, "chained", NodeTypesOption.KEYWORD),
  CHARACTER(ProParserTokenTypes.CHARACTER, "character", 4, NodeTypesOption.KEYWORD),
  CHARACTERLENGTH(ProParserTokenTypes.CHARACTERLENGTH, "characterlength", NodeTypesOption.KEYWORD),
  CHECK(ProParserTokenTypes.CHECK, "check", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CHOOSE(ProParserTokenTypes.CHOOSE, "choose", NodeTypesOption.KEYWORD),
  CHR(ProParserTokenTypes.CHR, "chr", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  CLASS(ProParserTokenTypes.CLASS, "class", NodeTypesOption.KEYWORD),
  CLEAR(ProParserTokenTypes.CLEAR, "clear", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CLIENTPRINCIPAL(ProParserTokenTypes.CLIENTPRINCIPAL, "client-principal", NodeTypesOption.KEYWORD),
  CLIPBOARD(ProParserTokenTypes.CLIPBOARD, "clipboard", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.SYSHDL),
  CLOB(ProParserTokenTypes.CLOB, "clob", NodeTypesOption.KEYWORD),
  CLOSE(ProParserTokenTypes.CLOSE, "close", NodeTypesOption.KEYWORD),
  CODEBASELOCATOR(ProParserTokenTypes.CODEBASELOCATOR, "codebase-locator", NodeTypesOption.KEYWORD,
      NodeTypesOption.SYSHDL),
  CODEPAGE(ProParserTokenTypes.CODEPAGE, "codepage", NodeTypesOption.KEYWORD),
  CODEPAGECONVERT(ProParserTokenTypes.CODEPAGECONVERT, "codepage-convert", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  COLLATE(ProParserTokenTypes.COLLATE, "collate", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  COLOF(ProParserTokenTypes.COLOF, "col-of", NodeTypesOption.KEYWORD),
  COLON(ProParserTokenTypes.COLON, "colon", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  COLONALIGNED(ProParserTokenTypes.COLONALIGNED, "colon-aligned", 11, NodeTypesOption.KEYWORD),
  COLOR(ProParserTokenTypes.COLOR, "color", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  COLORTABLE(ProParserTokenTypes.COLORTABLE, "color-table", NodeTypesOption.KEYWORD, NodeTypesOption.SYSHDL),
  COLUMN(ProParserTokenTypes.COLUMN, "column", 3, NodeTypesOption.KEYWORD),
  COLUMNBGCOLOR(ProParserTokenTypes.COLUMNBGCOLOR, "column-bgcolor", 10, NodeTypesOption.KEYWORD),
  COLUMNCODEPAGE(ProParserTokenTypes.COLUMNCODEPAGE, "column-codepage", NodeTypesOption.KEYWORD),
  COLUMNDCOLOR(ProParserTokenTypes.COLUMNDCOLOR, "column-dcolor", NodeTypesOption.KEYWORD),
  COLUMNFGCOLOR(ProParserTokenTypes.COLUMNFGCOLOR, "column-fgcolor", 10, NodeTypesOption.KEYWORD),
  COLUMNFONT(ProParserTokenTypes.COLUMNFONT, "column-font", NodeTypesOption.KEYWORD),
  COLUMNLABEL(ProParserTokenTypes.COLUMNLABEL, "column-label", 10, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  COLUMNOF(ProParserTokenTypes.COLUMNOF, "column-of", NodeTypesOption.KEYWORD),
  COLUMNPFCOLOR(ProParserTokenTypes.COLUMNPFCOLOR, "column-pfcolor", 10, NodeTypesOption.KEYWORD),
  COLUMNS(ProParserTokenTypes.COLUMNS, "columns", NodeTypesOption.KEYWORD),
  COMBOBOX(ProParserTokenTypes.COMBOBOX, "combo-box", NodeTypesOption.KEYWORD),
  COMHANDLE(ProParserTokenTypes.COMHANDLE, "com-handle", "component-handle", NodeTypesOption.KEYWORD),
  COMMA(ProParserTokenTypes.COMMA, ",", NodeTypesOption.SYMBOL),
  COMMAND(ProParserTokenTypes.COMMAND, "command", NodeTypesOption.KEYWORD),
  COMMENT(ProParserTokenTypes.COMMENT, NodeTypesOption.NONPRINTABLE),
  COMMENTEND(ProParserTokenTypes.COMMENTEND, NodeTypesOption.NONPRINTABLE),
  COMMENTSTART(ProParserTokenTypes.COMMENTSTART, NodeTypesOption.NONPRINTABLE),
  COMPARE(ProParserTokenTypes.COMPARE, "compare", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  COMPARES(ProParserTokenTypes.COMPARES, "compares", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  COMPILE(ProParserTokenTypes.COMPILE, "compile", NodeTypesOption.KEYWORD),
  COMPILER(ProParserTokenTypes.COMPILER, "compiler", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.SYSHDL),
  COMPLETE(ProParserTokenTypes.COMPLETE, "complete", NodeTypesOption.KEYWORD),
  COMSELF(ProParserTokenTypes.COMSELF, "com-self", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.SYSHDL),
  CONFIGNAME(ProParserTokenTypes.CONFIGNAME, "config-name", NodeTypesOption.KEYWORD),
  CONNECT(ProParserTokenTypes.CONNECT, "connect", NodeTypesOption.KEYWORD),
  CONNECTED(ProParserTokenTypes.CONNECTED, "connected", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  CONSTRUCTOR(ProParserTokenTypes.CONSTRUCTOR, "constructor", NodeTypesOption.KEYWORD),
  CONTAINS(ProParserTokenTypes.CONTAINS, "contains", NodeTypesOption.KEYWORD),
  CONTENTS(ProParserTokenTypes.CONTENTS, "contents", NodeTypesOption.KEYWORD),
  CONTEXT(ProParserTokenTypes.CONTEXT, "context", NodeTypesOption.KEYWORD),
  CONTEXTHELP(ProParserTokenTypes.CONTEXTHELP, "context-help", NodeTypesOption.KEYWORD),
  CONTEXTHELPFILE(ProParserTokenTypes.CONTEXTHELPFILE, "context-help-file", NodeTypesOption.KEYWORD),
  CONTEXTHELPID(ProParserTokenTypes.CONTEXTHELPID, "context-help-id", NodeTypesOption.KEYWORD),
  CONTEXTPOPUP(ProParserTokenTypes.CONTEXTPOPUP, "context-popup", 11, NodeTypesOption.KEYWORD),
  CONTROL(ProParserTokenTypes.CONTROL, "control", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CONTROLFRAME(ProParserTokenTypes.CONTROLFRAME, "control-frame", NodeTypesOption.KEYWORD),
  CONVERT(ProParserTokenTypes.CONVERT, "convert", NodeTypesOption.KEYWORD),
  CONVERT3DCOLORS(ProParserTokenTypes.CONVERT3DCOLORS, "convert-3d-colors", 10, NodeTypesOption.KEYWORD),
  COPYDATASET(ProParserTokenTypes.COPYDATASET, "copy-dataset", NodeTypesOption.KEYWORD),
  COPYLOB(ProParserTokenTypes.COPYLOB, "copy-lob", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  COPYTEMPTABLE(ProParserTokenTypes.COPYTEMPTABLE, "copy-temp-table", NodeTypesOption.KEYWORD),
  COUNT(ProParserTokenTypes.COUNT, "count", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  COUNTOF(ProParserTokenTypes.COUNTOF, "count-of", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  CREATE(ProParserTokenTypes.CREATE, "create", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CREATELIKESEQUENTIAL(ProParserTokenTypes.CREATELIKESEQUENTIAL, "create-like-sequential", NodeTypesOption.KEYWORD),
  CREATETESTFILE(ProParserTokenTypes.CREATETESTFILE, "create-test-file", NodeTypesOption.KEYWORD),
  CURLYAMP(ProParserTokenTypes.CURLYAMP),
  CURLYNUMBER(ProParserTokenTypes.CURLYNUMBER),
  CURLYSTAR(ProParserTokenTypes.CURLYSTAR),
  CURRENCY(ProParserTokenTypes.CURRENCY, "currency", NodeTypesOption.KEYWORD),
  CURRENT(ProParserTokenTypes.CURRENT, "current", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  CURRENTCHANGED(ProParserTokenTypes.CURRENTCHANGED, "current-changed", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  CURRENTENVIRONMENT(ProParserTokenTypes.CURRENTENVIRONMENT, "current-environment", 11, NodeTypesOption.KEYWORD),
  CURRENTLANGUAGE(ProParserTokenTypes.CURRENTLANGUAGE, "current-language", 12, NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED, NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  CURRENTQUERY(ProParserTokenTypes.CURRENTQUERY, "current-query", NodeTypesOption.KEYWORD),
  CURRENTRESULTROW(ProParserTokenTypes.CURRENTRESULTROW, "current-result-row", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  CURRENTVALUE(ProParserTokenTypes.CURRENTVALUE, "current-value", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  CURRENTWINDOW(ProParserTokenTypes.CURRENTWINDOW, "current-window", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.SYSHDL),
  CURSOR(ProParserTokenTypes.CURSOR, "cursor", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  CODE_BLOCK(ProParserTokenTypes.Code_block, NodeTypesOption.STRUCTURE),

  // D
  DATABASE(ProParserTokenTypes.DATABASE, "database", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DATABIND(ProParserTokenTypes.DATABIND, "data-bind", 6, NodeTypesOption.KEYWORD),
  DATARELATION(ProParserTokenTypes.DATARELATION, "data-relation", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DATASERVERS(ProParserTokenTypes.DATASERVERS, "dataservers", 11, "gateways", 7, NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED, NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  DATASET(ProParserTokenTypes.DATASET, "dataset", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DATASETHANDLE(ProParserTokenTypes.DATASETHANDLE, "dataset-handle", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DATASOURCE(ProParserTokenTypes.DATASOURCE, "data-source", NodeTypesOption.KEYWORD),
  DATASOURCEMODIFIED(ProParserTokenTypes.DATASOURCEMODIFIED, "data-source-modified", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DATASOURCEROWID(ProParserTokenTypes.DATASOURCEROWID, "data-source-rowid", NodeTypesOption.KEYWORD),
  DATE(ProParserTokenTypes.DATE, "date", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DATETIME(ProParserTokenTypes.DATETIME, "datetime", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DATETIMETZ(ProParserTokenTypes.DATETIMETZ, "datetime-tz", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DAY(ProParserTokenTypes.DAY, "day", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DBCODEPAGE(ProParserTokenTypes.DBCODEPAGE, "dbcodepage", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DBCOLLATION(ProParserTokenTypes.DBCOLLATION, "dbcollation", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DBIMS(ProParserTokenTypes.DBIMS, "dbims", NodeTypesOption.KEYWORD),
  DBNAME(ProParserTokenTypes.DBNAME, "dbname", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  DBPARAM(ProParserTokenTypes.DBPARAM, "dbparam", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DBREMOTEHOST(ProParserTokenTypes.DBREMOTEHOST, "db-remote-host", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DBRESTRICTIONS(ProParserTokenTypes.DBRESTRICTIONS, "dbrestrictions", 6, NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DBTASKID(ProParserTokenTypes.DBTASKID, "dbtaskid", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DBTYPE(ProParserTokenTypes.DBTYPE, "dbtype", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DBVERSION(ProParserTokenTypes.DBVERSION, "dbversion", 6, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DCOLOR(ProParserTokenTypes.DCOLOR, "dcolor", NodeTypesOption.KEYWORD),
  DDE(ProParserTokenTypes.DDE, "dde", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DEBLANK(ProParserTokenTypes.DEBLANK, "deblank", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DEBUG(ProParserTokenTypes.DEBUG, "debug", 4, NodeTypesOption.KEYWORD),
  DEBUGGER(ProParserTokenTypes.DEBUGGER, "debugger", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.SYSHDL),
  DEBUGLIST(ProParserTokenTypes.DEBUGLIST, "debug-list", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DECIMAL(ProParserTokenTypes.DECIMAL, "decimal", 3, NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DECIMALS(ProParserTokenTypes.DECIMALS, "decimals", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DECLARE(ProParserTokenTypes.DECLARE, "declare", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DECRYPT(ProParserTokenTypes.DECRYPT, "decrypt", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DEFAULT(ProParserTokenTypes.DEFAULT, "default", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DEFAULTBUTTON(ProParserTokenTypes.DEFAULTBUTTON, "default-button", 8, NodeTypesOption.KEYWORD),
  DEFAULTEXTENSION(ProParserTokenTypes.DEFAULTEXTENSION, "default-extension", 10, NodeTypesOption.KEYWORD),
  DEFAULTNOXLATE(ProParserTokenTypes.DEFAULTNOXLATE, "default-noxlate", 12, NodeTypesOption.KEYWORD),
  DEFAULTVALUE(ProParserTokenTypes.DEFAULTVALUE, "default-value", NodeTypesOption.KEYWORD),
  DEFAULTWINDOW(ProParserTokenTypes.DEFAULTWINDOW, "default-window", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.SYSHDL),
  DEFERLOBFETCH(ProParserTokenTypes.DEFERLOBFETCH, "defer-lob-fetch", NodeTypesOption.KEYWORD),
  DEFINE(ProParserTokenTypes.DEFINE, "define", 3, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DEFINED(ProParserTokenTypes.DEFINED, "defined", NodeTypesOption.KEYWORD),
  DEFINETEXT(ProParserTokenTypes.DEFINETEXT),
  DELEGATE(ProParserTokenTypes.DELEGATE, "delegate", NodeTypesOption.KEYWORD),
  DELETECHARACTER(ProParserTokenTypes.DELETECHARACTER, "delete-character", 11, NodeTypesOption.KEYWORD),
  DELETERESULTLISTENTRY(ProParserTokenTypes.DELETERESULTLISTENTRY, "delete-result-list-entry", NodeTypesOption.KEYWORD),
  DELETE(ProParserTokenTypes.DELETE_KW, "delete", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DELIMITER(ProParserTokenTypes.DELIMITER, "delimiter", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DESCENDING(ProParserTokenTypes.DESCENDING, "descending", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DESELECTION(ProParserTokenTypes.DESELECTION, "deselection", NodeTypesOption.KEYWORD),
  DESTRUCTOR(ProParserTokenTypes.DESTRUCTOR, "destructor", NodeTypesOption.KEYWORD),
  DIALOGBOX(ProParserTokenTypes.DIALOGBOX, "dialog-box", NodeTypesOption.KEYWORD),
  DIALOGHELP(ProParserTokenTypes.DIALOGHELP, "dialog-help", NodeTypesOption.KEYWORD),
  DICTIONARY(ProParserTokenTypes.DICTIONARY, "dictionary", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DIGITS(ProParserTokenTypes.DIGITS),
  DIGITSTART(ProParserTokenTypes.DIGITSTART),
  DIR(ProParserTokenTypes.DIR, "dir", NodeTypesOption.KEYWORD),
  DISABLE(ProParserTokenTypes.DISABLE, "disable", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DISABLEAUTOZAP(ProParserTokenTypes.DISABLEAUTOZAP, "disable-auto-zap", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  DISABLED(ProParserTokenTypes.DISABLED, "disabled", NodeTypesOption.KEYWORD),
  DISCONNECT(ProParserTokenTypes.DISCONNECT, "disconnect", 6, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DISPLAY(ProParserTokenTypes.DISPLAY, "display", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DISTINCT(ProParserTokenTypes.DISTINCT, "distinct", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DIVIDE(ProParserTokenTypes.DIVIDE),
  DO(ProParserTokenTypes.DO, "do", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DOS(ProParserTokenTypes.DOS, "dos", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DOT_COMMENT(ProParserTokenTypes.DOT_COMMENT),
  DOUBLE(ProParserTokenTypes.DOUBLE, "double", NodeTypesOption.KEYWORD),
  DOUBLECOLON(ProParserTokenTypes.DOUBLECOLON, "::", NodeTypesOption.SYMBOL),
  DOUBLEQUOTE(ProParserTokenTypes.DOUBLEQUOTE, "\"", NodeTypesOption.SYMBOL),
  DOWN(ProParserTokenTypes.DOWN, "down", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DQSTRING(ProParserTokenTypes.DQSTRING),
  DROP(ProParserTokenTypes.DROP, "drop", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  DROPDOWN(ProParserTokenTypes.DROPDOWN, "drop-down", NodeTypesOption.KEYWORD),
  DROPDOWNLIST(ProParserTokenTypes.DROPDOWNLIST, "drop-down-list", NodeTypesOption.KEYWORD),
  DROPFILENOTIFY(ProParserTokenTypes.DROPFILENOTIFY, "drop-file-notify", NodeTypesOption.KEYWORD),
  DROPTARGET(ProParserTokenTypes.DROPTARGET, "drop-target", NodeTypesOption.KEYWORD),
  DUMP(ProParserTokenTypes.DUMP, "dump", NodeTypesOption.KEYWORD),
  DYNAMIC(ProParserTokenTypes.DYNAMIC, "dynamic", NodeTypesOption.KEYWORD),
  DYNAMICCAST(ProParserTokenTypes.DYNAMICCAST, "dynamic-cast", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DYNAMICCURRENTVALUE(ProParserTokenTypes.DYNAMICCURRENTVALUE, "dynamic-current-value", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DYNAMICFUNCTION(ProParserTokenTypes.DYNAMICFUNCTION, "dynamic-function", 12, NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DYNAMICINVOKE(ProParserTokenTypes.DYNAMICINVOKE, "dynamic-invoke", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  DYNAMICNEW(ProParserTokenTypes.DYNAMICNEW, "dynamic-new", NodeTypesOption.KEYWORD),
  DYNAMICNEXTVALUE(ProParserTokenTypes.DYNAMICNEXTVALUE, "dynamic-next-value", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),

  // E
  EACH(ProParserTokenTypes.EACH, "each", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ECHO(ProParserTokenTypes.ECHO, "echo", NodeTypesOption.KEYWORD),
  EDGECHARS(ProParserTokenTypes.EDGECHARS, "edge-chars", 4, NodeTypesOption.KEYWORD),
  EDGEPIXELS(ProParserTokenTypes.EDGEPIXELS, "edge-pixels", 6, NodeTypesOption.KEYWORD),
  EDITING(ProParserTokenTypes.EDITING, "editing", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  EDITOR(ProParserTokenTypes.EDITOR, "editor", NodeTypesOption.KEYWORD),
  EDITUNDO(ProParserTokenTypes.EDITUNDO, "edit-undo", NodeTypesOption.KEYWORD),
  ELSE(ProParserTokenTypes.ELSE, "else", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  EMPTY(ProParserTokenTypes.EMPTY, "empty", NodeTypesOption.KEYWORD),
  ENABLE(ProParserTokenTypes.ENABLE, "enable", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ENABLEDFIELDS(ProParserTokenTypes.ENABLEDFIELDS, "enabled-fields", NodeTypesOption.KEYWORD),
  ENCODE(ProParserTokenTypes.ENCODE, "encode", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  ENCRYPT(ProParserTokenTypes.ENCRYPT, "encrypt", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  ENCRYPTIONSALT(ProParserTokenTypes.ENCRYPTIONSALT, "encryption-salt", NodeTypesOption.KEYWORD),
  END(ProParserTokenTypes.END, "end", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ENDKEY(ProParserTokenTypes.ENDKEY, "end-key", "endkey", NodeTypesOption.KEYWORD),
  ENDMOVE(ProParserTokenTypes.ENDMOVE, "end-move", NodeTypesOption.KEYWORD),
  ENDRESIZE(ProParserTokenTypes.ENDRESIZE, "end-resize", NodeTypesOption.KEYWORD),
  ENDROWRESIZE(ProParserTokenTypes.ENDROWRESIZE, "end-row-resize", NodeTypesOption.KEYWORD),
  ENTERED(ProParserTokenTypes.ENTERED, "entered", NodeTypesOption.KEYWORD),
  ENTRY(ProParserTokenTypes.ENTRY, "entry", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  ENUM(ProParserTokenTypes.ENUM, "enum", NodeTypesOption.KEYWORD),
  EQ(ProParserTokenTypes.EQ, "eq", NodeTypesOption.KEYWORD),
  EQUAL(ProParserTokenTypes.EQUAL, "=", NodeTypesOption.SYMBOL),
  ERROR(ProParserTokenTypes.ERROR, "error", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  ERRORCODE(ProParserTokenTypes.ERRORCODE, "error-code", NodeTypesOption.KEYWORD),
  ERRORSTACKTRACE(ProParserTokenTypes.ERRORSTACKTRACE, "error-stack-trace", NodeTypesOption.KEYWORD),
  ERRORSTATUS(ProParserTokenTypes.ERRORSTATUS, "error-status", 10, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.SYSHDL),
  ESCAPE(ProParserTokenTypes.ESCAPE, "escape", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ESCAPED_QUOTE(ProParserTokenTypes.ESCAPED_QUOTE),
  ETIME(ProParserTokenTypes.ETIME_KW, "etime", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  EVENT(ProParserTokenTypes.EVENT, "event", NodeTypesOption.KEYWORD),
  EVENTPROCEDURE(ProParserTokenTypes.EVENTPROCEDURE, "event-procedure", NodeTypesOption.KEYWORD),
  EVENTS(ProParserTokenTypes.EVENTS, "events", NodeTypesOption.KEYWORD),
  EXCEPT(ProParserTokenTypes.EXCEPT, "except", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  EXCLAMATION(ProParserTokenTypes.EXCLAMATION, "!", NodeTypesOption.SYMBOL),
  EXCLUSIVEID(ProParserTokenTypes.EXCLUSIVEID, "exclusive-id", NodeTypesOption.KEYWORD),
  EXCLUSIVELOCK(ProParserTokenTypes.EXCLUSIVELOCK, "exclusive-lock", 9, NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  EXCLUSIVEWEBUSER(ProParserTokenTypes.EXCLUSIVEWEBUSER, "exclusive-web-user", 13, NodeTypesOption.KEYWORD),
  EXECUTE(ProParserTokenTypes.EXECUTE, "execute", NodeTypesOption.KEYWORD),
  EXISTS(ProParserTokenTypes.EXISTS, "exists", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  EXP(ProParserTokenTypes.EXP, "exp", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  EXPAND(ProParserTokenTypes.EXPAND, "expand", NodeTypesOption.KEYWORD),
  EXPANDABLE(ProParserTokenTypes.EXPANDABLE, "expandable", NodeTypesOption.KEYWORD),
  EXPLICIT(ProParserTokenTypes.EXPLICIT, "explicit", NodeTypesOption.KEYWORD),
  EXPORT(ProParserTokenTypes.EXPORT, "export", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  EXTENDED(ProParserTokenTypes.EXTENDED, "extended", NodeTypesOption.KEYWORD),
  EXTENT(ProParserTokenTypes.EXTENT, "extent", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  EXTERNAL(ProParserTokenTypes.EXTERNAL, "external", NodeTypesOption.KEYWORD),
  EDITING_PHRASE(ProParserTokenTypes.Editing_phrase, NodeTypesOption.STRUCTURE),
  ENTERED_FUNC(ProParserTokenTypes.Entered_func, NodeTypesOption.STRUCTURE),
  EVENT_LIST(ProParserTokenTypes.Event_list, NodeTypesOption.STRUCTURE),
  EXPR_STATEMENT(ProParserTokenTypes.Expr_statement, NodeTypesOption.STRUCTURE),

  // F
  FALSELEAKS(ProParserTokenTypes.FALSELEAKS, "false-leaks", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FALSE(ProParserTokenTypes.FALSE_KW, "false", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FETCH(ProParserTokenTypes.FETCH, "fetch", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FGCOLOR(ProParserTokenTypes.FGCOLOR, "fgcolor", 3, NodeTypesOption.KEYWORD),
  FIELD(ProParserTokenTypes.FIELD, "field", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FIELDS(ProParserTokenTypes.FIELDS, "fields", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FILE(ProParserTokenTypes.FILE, "file", 4, "file-name", "filename", NodeTypesOption.KEYWORD),
  FILEINFORMATION(ProParserTokenTypes.FILEINFORMATION, "file-information", 9, NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED, NodeTypesOption.SYSHDL),
  FILENAME(ProParserTokenTypes.FILENAME),
  FILL(ProParserTokenTypes.FILL, "fill", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  FILLIN(ProParserTokenTypes.FILLIN, "fill-in", NodeTypesOption.KEYWORD),
  FILTERS(ProParserTokenTypes.FILTERS, "filters", NodeTypesOption.KEYWORD),
  FINAL(ProParserTokenTypes.FINAL, "final", NodeTypesOption.KEYWORD),
  FINALLY(ProParserTokenTypes.FINALLY, "finally", NodeTypesOption.KEYWORD),
  FIND(ProParserTokenTypes.FIND, "find", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FINDCASESENSITIVE(ProParserTokenTypes.FINDCASESENSITIVE, "find-case-sensitive", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  FINDER(ProParserTokenTypes.FINDER, "finder", NodeTypesOption.KEYWORD),
  FINDGLOBAL(ProParserTokenTypes.FINDGLOBAL, "find-global", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FINDNEXTOCCURRENCE(ProParserTokenTypes.FINDNEXTOCCURRENCE, "find-next-occurrence", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  FINDPREVOCCURRENCE(ProParserTokenTypes.FINDPREVOCCURRENCE, "find-prev-occurrence", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  FINDSELECT(ProParserTokenTypes.FINDSELECT, "find-select", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FINDWRAPAROUND(ProParserTokenTypes.FINDWRAPAROUND, "find-wrap-around", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  FIRST(ProParserTokenTypes.FIRST, "first", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  FIRSTFORM(ProParserTokenTypes.FIRSTFORM, "first-form", NodeTypesOption.KEYWORD),
  FIRSTOF(ProParserTokenTypes.FIRSTOF, "first-of", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  FITLASTCOLUMN(ProParserTokenTypes.FITLASTCOLUMN, "fit-last-column", NodeTypesOption.KEYWORD),
  FIXCHAR(ProParserTokenTypes.FIXCHAR, "fixchar", NodeTypesOption.KEYWORD),
  FIXCODEPAGE(ProParserTokenTypes.FIXCODEPAGE, "fix-codepage", NodeTypesOption.KEYWORD),
  FIXEDONLY(ProParserTokenTypes.FIXEDONLY, "fixed-only", NodeTypesOption.KEYWORD),
  FLAGS(ProParserTokenTypes.FLAGS, "flags", NodeTypesOption.KEYWORD),
  FLATBUTTON(ProParserTokenTypes.FLATBUTTON, "flat-button", NodeTypesOption.KEYWORD),
  FLOAT(ProParserTokenTypes.FLOAT, "float", NodeTypesOption.KEYWORD),
  FOCUS(ProParserTokenTypes.FOCUS, "focus", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED, NodeTypesOption.SYSHDL),
  FONT(ProParserTokenTypes.FONT, "font", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FONTBASEDLAYOUT(ProParserTokenTypes.FONTBASEDLAYOUT, "font-based-layout", NodeTypesOption.KEYWORD),
  FONTTABLE(ProParserTokenTypes.FONTTABLE, "font-table", NodeTypesOption.KEYWORD, NodeTypesOption.SYSHDL),
  FOR(ProParserTokenTypes.FOR, "for", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FORCEFILE(ProParserTokenTypes.FORCEFILE, "force-file", NodeTypesOption.KEYWORD),
  FOREIGNKEYHIDDEN(ProParserTokenTypes.FOREIGNKEYHIDDEN, "foreign-key-hidden", NodeTypesOption.KEYWORD),
  FORMAT(ProParserTokenTypes.FORMAT, "format", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FORMINPUT(ProParserTokenTypes.FORMINPUT, "forminput", NodeTypesOption.KEYWORD),
  FORMLONGINPUT(ProParserTokenTypes.FORMLONGINPUT, "form-long-input", NodeTypesOption.KEYWORD),
  FORWARDS(ProParserTokenTypes.FORWARDS, "forwards", 7, NodeTypesOption.KEYWORD),
  FRAME(ProParserTokenTypes.FRAME, "frame", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FRAMECOL(ProParserTokenTypes.FRAMECOL, "frame-col", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  FRAMEDB(ProParserTokenTypes.FRAMEDB, "frame-db", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  FRAMEDOWN(ProParserTokenTypes.FRAMEDOWN, "frame-down", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  FRAMEFIELD(ProParserTokenTypes.FRAMEFIELD, "frame-field", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  FRAMEFILE(ProParserTokenTypes.FRAMEFILE, "frame-file", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  FRAMEINDEX(ProParserTokenTypes.FRAMEINDEX, "frame-index", 10, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  FRAMELINE(ProParserTokenTypes.FRAMELINE, "frame-line", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  FRAMENAME(ProParserTokenTypes.FRAMENAME, "frame-name", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  FRAMEROW(ProParserTokenTypes.FRAMEROW, "frame-row", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  FRAMEVALUE(ProParserTokenTypes.FRAMEVALUE, "frame-value", 9, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  FREECHAR(ProParserTokenTypes.FREECHAR),
  FREQUENCY(ProParserTokenTypes.FREQUENCY, "frequency", NodeTypesOption.KEYWORD),
  FROM(ProParserTokenTypes.FROM, "from", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  FROMCURRENT(ProParserTokenTypes.FROMCURRENT, "from-current", 8, NodeTypesOption.KEYWORD),
  FUNCTION(ProParserTokenTypes.FUNCTION, "function", NodeTypesOption.KEYWORD),
  FUNCTIONCALLTYPE(ProParserTokenTypes.FUNCTIONCALLTYPE, "function-call-type", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  FIELD_LIST(ProParserTokenTypes.Field_list, NodeTypesOption.STRUCTURE),
  FIELD_REF(ProParserTokenTypes.Field_ref, NodeTypesOption.STRUCTURE),
  FORM_ITEM(ProParserTokenTypes.Form_item, NodeTypesOption.STRUCTURE),
  FORMAT_PHRASE(ProParserTokenTypes.Format_phrase, NodeTypesOption.STRUCTURE),

  // G
  // XXX GATEWAYS(ProParserTokenTypes.GATEWAYS, NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  GE(ProParserTokenTypes.GE, "ge", NodeTypesOption.KEYWORD),
  GENERATEMD5(ProParserTokenTypes.GENERATEMD5, "generate-md5", NodeTypesOption.KEYWORD),
  GENERATEPBEKEY(ProParserTokenTypes.GENERATEPBEKEY, "generate-pbe-key", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GENERATEPBESALT(ProParserTokenTypes.GENERATEPBESALT, "generate-pbe-salt", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  GENERATERANDOMKEY(ProParserTokenTypes.GENERATERANDOMKEY, "generate-random-key", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  GENERATEUUID(ProParserTokenTypes.GENERATEUUID, "generate-uuid", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  GET(ProParserTokenTypes.GET, "get", NodeTypesOption.KEYWORD),
  GETATTRCALLTYPE(ProParserTokenTypes.GETATTRCALLTYPE, "get-attr-call-type", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  GETBITS(ProParserTokenTypes.GETBITS, "get-bits", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETBUFFERHANDLE(ProParserTokenTypes.GETBUFFERHANDLE, "get-buffer-handle", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  GETBYTE(ProParserTokenTypes.GETBYTE, "get-byte", "getbyte", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETBYTEORDER(ProParserTokenTypes.GETBYTEORDER, "get-byte-order", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETBYTES(ProParserTokenTypes.GETBYTES, "get-bytes", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETCGILIST(ProParserTokenTypes.GETCGILIST, "get-cgi-list", NodeTypesOption.KEYWORD),
  GETCGILONGVALUE(ProParserTokenTypes.GETCGILONGVALUE, "get-cgi-long-value", NodeTypesOption.KEYWORD),
  GETCGIVALUE(ProParserTokenTypes.GETCGIVALUE, "get-cgi-value", NodeTypesOption.KEYWORD),
  GETCLASS(ProParserTokenTypes.GETCLASS, "get-class", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETCODEPAGE(ProParserTokenTypes.GETCODEPAGE, "get-codepage", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETCODEPAGES(ProParserTokenTypes.GETCODEPAGES, "get-codepages", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETCOLLATIONS(ProParserTokenTypes.GETCOLLATIONS, "get-collations", 8, NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETCONFIGVALUE(ProParserTokenTypes.GETCONFIGVALUE, "get-config-value", NodeTypesOption.KEYWORD),
  GETDBCLIENT(ProParserTokenTypes.GETDBCLIENT, "get-db-client", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC, NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  GETDIR(ProParserTokenTypes.GETDIR, "get-dir", NodeTypesOption.KEYWORD),
  GETDOUBLE(ProParserTokenTypes.GETDOUBLE, "get-double", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETEFFECTIVETENANTID(ProParserTokenTypes.GETEFFECTIVETENANTID, "get-effective-tenant-id", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETEFFECTIVETENANTNAME(ProParserTokenTypes.GETEFFECTIVETENANTNAME, "get-effective-tenant-name",
      NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETFILE(ProParserTokenTypes.GETFILE, "get-file", NodeTypesOption.KEYWORD),
  GETFLOAT(ProParserTokenTypes.GETFLOAT, "get-float", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETINT64(ProParserTokenTypes.GETINT64, "get-int64", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETKEYVALUE(ProParserTokenTypes.GETKEYVALUE, "get-key-value", 11, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  GETLICENSE(ProParserTokenTypes.GETLICENSE, "get-license", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETLONG(ProParserTokenTypes.GETLONG, "get-long", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETPOINTERVALUE(ProParserTokenTypes.GETPOINTERVALUE, "get-pointer-value", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETSHORT(ProParserTokenTypes.GETSHORT, "get-short", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETSIZE(ProParserTokenTypes.GETSIZE, "get-size", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETSTRING(ProParserTokenTypes.GETSTRING, "get-string", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETUNSIGNEDLONG(ProParserTokenTypes.GETUNSIGNEDLONG, "get-unsigned-long", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GETUNSIGNEDSHORT(ProParserTokenTypes.GETUNSIGNEDSHORT, "get-unsigned-short", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  GLOBAL(ProParserTokenTypes.GLOBAL, "global", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  GLOBAL_DEFINE(ProParserTokenTypes.GLOBALDEFINE, NodeTypesOption.PREPROCESSOR),
  GOON(ProParserTokenTypes.GOON, "go-on", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  GOPENDING(ProParserTokenTypes.GOPENDING, "go-pending", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  GRANT(ProParserTokenTypes.GRANT, "grant", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  GRAPHICEDGE(ProParserTokenTypes.GRAPHICEDGE, "graphic-edge", 9, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  GROUP(ProParserTokenTypes.GROUP, "group", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  GROUPBOX(ProParserTokenTypes.GROUPBOX, "group-box", NodeTypesOption.KEYWORD),
  GTHAN(ProParserTokenTypes.GTHAN, "gt", NodeTypesOption.KEYWORD),
  GTOREQUAL(ProParserTokenTypes.GTOREQUAL, ">=", NodeTypesOption.SYMBOL),
  GTORLT(ProParserTokenTypes.GTORLT, "<>", NodeTypesOption.SYMBOL),
  GUID(ProParserTokenTypes.GUID, "guid", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC, NodeTypesOption.MAY_BE_NO_ARG_FUNC),

  // H
  HANDLE(ProParserTokenTypes.HANDLE, "handle", NodeTypesOption.KEYWORD),
  HAVING(ProParserTokenTypes.HAVING, "having", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  HEADER(ProParserTokenTypes.HEADER, "header", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  HEIGHT(ProParserTokenTypes.HEIGHT, "height", NodeTypesOption.KEYWORD),
  HEIGHTCHARS(ProParserTokenTypes.HEIGHTCHARS, "height-chars", 8, NodeTypesOption.KEYWORD),
  HEIGHTPIXELS(ProParserTokenTypes.HEIGHTPIXELS, "height-pixels", 8, NodeTypesOption.KEYWORD),
  HELP(ProParserTokenTypes.HELP, "help", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  HELPTOPIC(ProParserTokenTypes.HELPTOPIC, "help-topic", NodeTypesOption.KEYWORD),
  HEXDECODE(ProParserTokenTypes.HEXDECODE, "hex-decode", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  HEXENCODE(ProParserTokenTypes.HEXENCODE, "hex-encode", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  HIDE(ProParserTokenTypes.HIDE, "hide", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  HINT(ProParserTokenTypes.HINT, "hint", NodeTypesOption.KEYWORD),
  HORIZONTAL(ProParserTokenTypes.HORIZONTAL, "horizontal", 4, NodeTypesOption.KEYWORD),
  HOSTBYTEORDER(ProParserTokenTypes.HOSTBYTEORDER, "host-byte-order", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  HTMLENDOFLINE(ProParserTokenTypes.HTMLENDOFLINE, "html-end-of-line", NodeTypesOption.KEYWORD),
  HTMLFRAMEBEGIN(ProParserTokenTypes.HTMLFRAMEBEGIN, "html-frame-begin", NodeTypesOption.KEYWORD),
  HTMLFRAMEEND(ProParserTokenTypes.HTMLFRAMEEND, "html-frame-end", NodeTypesOption.KEYWORD),
  HTMLHEADERBEGIN(ProParserTokenTypes.HTMLHEADERBEGIN, "html-header-begin", NodeTypesOption.KEYWORD),
  HTMLHEADEREND(ProParserTokenTypes.HTMLHEADEREND, "html-header-end", NodeTypesOption.KEYWORD),
  HTMLTITLEBEGIN(ProParserTokenTypes.HTMLTITLEBEGIN, "html-title-begin", NodeTypesOption.KEYWORD),
  HTMLTITLEEND(ProParserTokenTypes.HTMLTITLEEND, "html-title-end", NodeTypesOption.KEYWORD),

  // I
  ID(ProParserTokenTypes.ID),
  ID_THREE(ProParserTokenTypes.ID_THREE),
  ID_TWO(ProParserTokenTypes.ID_TWO),
  IF(ProParserTokenTypes.IF, "if", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  IFCOND(ProParserTokenTypes.IFCOND),
  IMAGE(ProParserTokenTypes.IMAGE, "image", NodeTypesOption.KEYWORD),
  IMAGEDOWN(ProParserTokenTypes.IMAGEDOWN, "image-down", NodeTypesOption.KEYWORD),
  IMAGEINSENSITIVE(ProParserTokenTypes.IMAGEINSENSITIVE, "image-insensitive", NodeTypesOption.KEYWORD),
  IMAGESIZE(ProParserTokenTypes.IMAGESIZE, "image-size", NodeTypesOption.KEYWORD),
  IMAGESIZECHARS(ProParserTokenTypes.IMAGESIZECHARS, "image-size-chars", 12, NodeTypesOption.KEYWORD),
  IMAGESIZEPIXELS(ProParserTokenTypes.IMAGESIZEPIXELS, "image-size-pixels", 12, NodeTypesOption.KEYWORD),
  IMAGEUP(ProParserTokenTypes.IMAGEUP, "image-up", NodeTypesOption.KEYWORD),
  IMPLEMENTS(ProParserTokenTypes.IMPLEMENTS, "implements", NodeTypesOption.KEYWORD),
  IMPORT(ProParserTokenTypes.IMPORT, "import", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  IMPOSSIBLE_TOKEN(ProParserTokenTypes.IMPOSSIBLE_TOKEN),
  INCLUDEREFARG(ProParserTokenTypes.INCLUDEREFARG),
  INCREMENTEXCLUSIVEID(ProParserTokenTypes.INCREMENTEXCLUSIVEID, "increment-exclusive-id", NodeTypesOption.KEYWORD),
  INDEX(ProParserTokenTypes.INDEX, "index", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  INDEXEDREPOSITION(ProParserTokenTypes.INDEXEDREPOSITION, "indexed-reposition", NodeTypesOption.KEYWORD),
  INDEXHINT(ProParserTokenTypes.INDEXHINT, "index-hint", NodeTypesOption.KEYWORD),
  INDICATOR(ProParserTokenTypes.INDICATOR, "indicator", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  INFORMATION(ProParserTokenTypes.INFORMATION, "information", 4, NodeTypesOption.KEYWORD),
  INHERITBGCOLOR(ProParserTokenTypes.INHERITBGCOLOR, "inherit-bgcolor", 11, NodeTypesOption.KEYWORD),
  INHERITFGCOLOR(ProParserTokenTypes.INHERITFGCOLOR, "inherit-fgcolor", 11, NodeTypesOption.KEYWORD),
  INHERITS(ProParserTokenTypes.INHERITS, "inherits", NodeTypesOption.KEYWORD),
  INITIAL(ProParserTokenTypes.INITIAL, "initial", 4, NodeTypesOption.KEYWORD),
  INITIALDIR(ProParserTokenTypes.INITIALDIR, "initial-dir", NodeTypesOption.KEYWORD),
  INITIALFILTER(ProParserTokenTypes.INITIALFILTER, "initial-filter", NodeTypesOption.KEYWORD),
  INITIATE(ProParserTokenTypes.INITIATE, "initiate", NodeTypesOption.KEYWORD),
  INNER(ProParserTokenTypes.INNER, "inner", NodeTypesOption.KEYWORD),
  INNERCHARS(ProParserTokenTypes.INNERCHARS, "inner-chars", NodeTypesOption.KEYWORD),
  INNERLINES(ProParserTokenTypes.INNERLINES, "inner-lines", NodeTypesOption.KEYWORD),
  INPUT(ProParserTokenTypes.INPUT, "input", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  INPUTOUTPUT(ProParserTokenTypes.INPUTOUTPUT, "input-output", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  INSERT(ProParserTokenTypes.INSERT, "insert", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  INT64(ProParserTokenTypes.INT64, "int64", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  INTEGER(ProParserTokenTypes.INTEGER, "integer", 3, NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  INTERFACE(ProParserTokenTypes.INTERFACE, "interface", NodeTypesOption.KEYWORD),
  INTERVAL(ProParserTokenTypes.INTERVAL, "interval", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  INTO(ProParserTokenTypes.INTO, "into", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  IN(ProParserTokenTypes.IN_KW, "in", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  IS(ProParserTokenTypes.IS, "is", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ISATTRSPACE(ProParserTokenTypes.ISATTRSPACE, "is-attr-space", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  ISCODEPAGEFIXED(ProParserTokenTypes.ISCODEPAGEFIXED, "is-codepage-fixed", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  ISCOLUMNCODEPAGE(ProParserTokenTypes.ISCOLUMNCODEPAGE, "is-column-codepage", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  ISDBMULTITENANT(ProParserTokenTypes.ISDBMULTITENANT, "is-db-multi-tenant", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  ISLEADBYTE(ProParserTokenTypes.ISLEADBYTE, "is-lead-byte", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  ISMULTITENANT(ProParserTokenTypes.ISMULTITENANT, "is-multi-tenant", NodeTypesOption.KEYWORD),
  ISODATE(ProParserTokenTypes.ISODATE, "iso-date", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  ITEM(ProParserTokenTypes.ITEM, "item", NodeTypesOption.KEYWORD),
  IUNKNOWN(ProParserTokenTypes.IUNKNOWN, "iunknown", NodeTypesOption.KEYWORD),
  INLINE_DEFINITION(ProParserTokenTypes.Inline_definition, NodeTypesOption.STRUCTURE),

  // J
  JOIN(ProParserTokenTypes.JOIN, "join", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  JOINBYSQLDB(ProParserTokenTypes.JOINBYSQLDB, "join-by-sqldb", NodeTypesOption.KEYWORD),

  // K
  KBLABEL(ProParserTokenTypes.KBLABEL, "kblabel", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  KEEPMESSAGES(ProParserTokenTypes.KEEPMESSAGES, "keep-messages", NodeTypesOption.KEYWORD),
  KEEPTABORDER(ProParserTokenTypes.KEEPTABORDER, "keep-tab-order", NodeTypesOption.KEYWORD),
  KEY(ProParserTokenTypes.KEY, "key", NodeTypesOption.KEYWORD),
  KEYCODE(ProParserTokenTypes.KEYCODE, "key-code", "keycode", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  KEYFUNCTION(ProParserTokenTypes.KEYFUNCTION, "key-function", 8, "keyfunction", 7, NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  KEYLABEL(ProParserTokenTypes.KEYLABEL, "key-label", "keylabel", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  KEYS(ProParserTokenTypes.KEYS, "keys", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  KEYWORD(ProParserTokenTypes.KEYWORD, "keyword", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  KEYWORDALL(ProParserTokenTypes.KEYWORDALL, "keyword-all", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),

  // L
  LABEL(ProParserTokenTypes.LABEL, "label", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LABELBGCOLOR(ProParserTokenTypes.LABELBGCOLOR, "label-bgcolor", 9, NodeTypesOption.KEYWORD),
  LABELDCOLOR(ProParserTokenTypes.LABELDCOLOR, "label-dcolor", 8, NodeTypesOption.KEYWORD),
  LABELFGCOLOR(ProParserTokenTypes.LABELFGCOLOR, "label-fgcolor", 9, NodeTypesOption.KEYWORD),
  LABELFONT(ProParserTokenTypes.LABELFONT, "label-font", NodeTypesOption.KEYWORD),
  LANDSCAPE(ProParserTokenTypes.LANDSCAPE, "landscape", NodeTypesOption.KEYWORD),
  LANGUAGES(ProParserTokenTypes.LANGUAGES, "languages", 8, NodeTypesOption.KEYWORD),
  LARGE(ProParserTokenTypes.LARGE, "large", NodeTypesOption.KEYWORD),
  LARGETOSMALL(ProParserTokenTypes.LARGETOSMALL, "large-to-small", NodeTypesOption.KEYWORD),
  LAST(ProParserTokenTypes.LAST, "last", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  LASTBATCH(ProParserTokenTypes.LASTBATCH, "last-batch", NodeTypesOption.KEYWORD),
  LASTEVENT(ProParserTokenTypes.LASTEVENT, "last-event", 9, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.SYSHDL),
  LASTFORM(ProParserTokenTypes.LASTFORM, "last-form", NodeTypesOption.KEYWORD),
  LASTKEY(ProParserTokenTypes.LASTKEY, "last-key", "lastkey", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  LASTOF(ProParserTokenTypes.LASTOF, "last-of", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  LC(ProParserTokenTypes.LC, "lc", "lower", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  LDBNAME(ProParserTokenTypes.LDBNAME, "ldbname", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  LE(ProParserTokenTypes.LE, "le", NodeTypesOption.KEYWORD),
  LEAKDETECTION(ProParserTokenTypes.LEAKDETECTION, "leak-detection", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LEAVE(ProParserTokenTypes.LEAVE, "leave", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LEFT(ProParserTokenTypes.LEFT, "left", NodeTypesOption.KEYWORD),
  LEFTALIGNED(ProParserTokenTypes.LEFTALIGNED, "left-aligned", 10, NodeTypesOption.KEYWORD),
  LEFTANGLE(ProParserTokenTypes.LEFTANGLE, "<", NodeTypesOption.SYMBOL),
  LEFTBRACE(ProParserTokenTypes.LEFTBRACE, "[", NodeTypesOption.SYMBOL),
  LEFTCURLY(ProParserTokenTypes.LEFTCURLY, "{", NodeTypesOption.SYMBOL),
  LEFTPAREN(ProParserTokenTypes.LEFTPAREN, "(", NodeTypesOption.SYMBOL),
  LEFTTRIM(ProParserTokenTypes.LEFTTRIM, "left-trim", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  LENGTH(ProParserTokenTypes.LENGTH, "length", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  LEXAT(ProParserTokenTypes.LEXAT, "@", NodeTypesOption.SYMBOL),
  LEXCOLON(ProParserTokenTypes.LEXCOLON, ":", NodeTypesOption.SYMBOL),
  LEXDATE(ProParserTokenTypes.LEXDATE, NodeTypesOption.NONPRINTABLE),
  LEXOTHER(ProParserTokenTypes.LEXOTHER, NodeTypesOption.NONPRINTABLE),
  LIBRARY(ProParserTokenTypes.LIBRARY, "library", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  LIKE(ProParserTokenTypes.LIKE, "like", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LIKESEQUENTIAL(ProParserTokenTypes.LIKESEQUENTIAL, "like-sequential", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  LINECOUNTER(ProParserTokenTypes.LINECOUNTER, "line-counter", 10, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  LISTEVENTS(ProParserTokenTypes.LISTEVENTS, "list-events", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  LISTING(ProParserTokenTypes.LISTING, "listing", 5, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LISTITEMPAIRS(ProParserTokenTypes.LISTITEMPAIRS, "list-item-pairs", NodeTypesOption.KEYWORD),
  LISTITEMS(ProParserTokenTypes.LISTITEMS, "list-items", NodeTypesOption.KEYWORD),
  LISTQUERYATTRS(ProParserTokenTypes.LISTQUERYATTRS, "list-query-attrs", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  LISTSETATTRS(ProParserTokenTypes.LISTSETATTRS, "list-set-attrs", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  LISTWIDGETS(ProParserTokenTypes.LISTWIDGETS, "list-widgets", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  LITTLEENDIAN(ProParserTokenTypes.LITTLEENDIAN, "little-endian", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  LOAD(ProParserTokenTypes.LOAD, "load", NodeTypesOption.KEYWORD),
  LOADPICTURE(ProParserTokenTypes.LOADPICTURE, "load-picture", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  LOBDIR(ProParserTokenTypes.LOBDIR, "lob-dir", NodeTypesOption.KEYWORD),
  LOCAL_METHOD_REF(ProParserTokenTypes.LOCAL_METHOD_REF, NodeTypesOption.STRUCTURE),
  LOCKED(ProParserTokenTypes.LOCKED, "locked", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  LOG(ProParserTokenTypes.LOG, "log", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  LOGICAL(ProParserTokenTypes.LOGICAL, "logical", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  LOGMANAGER(ProParserTokenTypes.LOGMANAGER, "log-manager", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.SYSHDL),
  LONG(ProParserTokenTypes.LONG, "long", NodeTypesOption.KEYWORD),
  LONGCHAR(ProParserTokenTypes.LONGCHAR, "longchar", NodeTypesOption.KEYWORD),
  LOOKAHEAD(ProParserTokenTypes.LOOKAHEAD, "lookahead", NodeTypesOption.KEYWORD),
  LOOKUP(ProParserTokenTypes.LOOKUP, "lookup", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  LTHAN(ProParserTokenTypes.LTHAN, "lt", NodeTypesOption.KEYWORD),
  LTOREQUAL(ProParserTokenTypes.LTOREQUAL, ">=", NodeTypesOption.SYMBOL),
  LOOSE_END_KEEPER(ProParserTokenTypes.Loose_End_Keeper, NodeTypesOption.STRUCTURE),

  // M
  MACHINECLASS(ProParserTokenTypes.MACHINECLASS, "machine-class", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  MAP(ProParserTokenTypes.MAP, "map", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  MARGINEXTRA(ProParserTokenTypes.MARGINEXTRA, "margin-extra", NodeTypesOption.KEYWORD),
  MARKNEW(ProParserTokenTypes.MARKNEW, "mark-new", NodeTypesOption.KEYWORD),
  MARKROWSTATE(ProParserTokenTypes.MARKROWSTATE, "mark-row-state", NodeTypesOption.KEYWORD),
  MATCHES(ProParserTokenTypes.MATCHES, "matches", NodeTypesOption.KEYWORD),
  MAXCHARS(ProParserTokenTypes.MAXCHARS, "max-chars", NodeTypesOption.KEYWORD),
  MAXIMIZE(ProParserTokenTypes.MAXIMIZE, "maximize", NodeTypesOption.KEYWORD),
  MAXIMUM(ProParserTokenTypes.MAXIMUM, "max", "maximum", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  MAXIMUMLEVEL(ProParserTokenTypes.MAXIMUMLEVEL, "maximum-level", NodeTypesOption.KEYWORD),
  MAXROWS(ProParserTokenTypes.MAXROWS, "max-rows", NodeTypesOption.KEYWORD),
  MAXSIZE(ProParserTokenTypes.MAXSIZE, "max-size", NodeTypesOption.KEYWORD),
  MAXVALUE(ProParserTokenTypes.MAXVALUE, "max-value", 7, NodeTypesOption.KEYWORD),
  MD5DIGEST(ProParserTokenTypes.MD5DIGEST, "md5-digest", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  MEMBER(ProParserTokenTypes.MEMBER, "member", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  MEMPTR(ProParserTokenTypes.MEMPTR, "memptr", NodeTypesOption.KEYWORD),
  MENU(ProParserTokenTypes.MENU, "menu", NodeTypesOption.KEYWORD),
  MENUBAR(ProParserTokenTypes.MENUBAR, "menu-bar", "menubar", NodeTypesOption.KEYWORD),
  MENUITEM(ProParserTokenTypes.MENUITEM, "menu-item", NodeTypesOption.KEYWORD),
  MERGEBYFIELD(ProParserTokenTypes.MERGEBYFIELD, "merge-by-field", NodeTypesOption.KEYWORD),
  MESSAGE(ProParserTokenTypes.MESSAGE, "message", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  MESSAGEDIGEST(ProParserTokenTypes.MESSAGEDIGEST, "message-digest", NodeTypesOption.KEYWORD),
  MESSAGELINE(ProParserTokenTypes.MESSAGELINE, "message-line", NodeTypesOption.KEYWORD),
  MESSAGELINES(ProParserTokenTypes.MESSAGELINES, "message-lines", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  METHOD(ProParserTokenTypes.METHOD, "method", NodeTypesOption.KEYWORD),
  MINIMUM(ProParserTokenTypes.MINIMUM, "minimum", 3, NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  MINSIZE(ProParserTokenTypes.MINSIZE, "min-size", NodeTypesOption.KEYWORD),
  MINUS(ProParserTokenTypes.MINUS, "-", NodeTypesOption.SYMBOL),
  MINVALUE(ProParserTokenTypes.MINVALUE, "min-value", 7, NodeTypesOption.KEYWORD),
  MODULO(ProParserTokenTypes.MODULO, "modulo", 3, NodeTypesOption.KEYWORD),
  MONTH(ProParserTokenTypes.MONTH, "month", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  MOUSE(ProParserTokenTypes.MOUSE, "mouse", NodeTypesOption.KEYWORD, NodeTypesOption.SYSHDL),
  MOUSEPOINTER(ProParserTokenTypes.MOUSEPOINTER, "mouse-pointer", 7, NodeTypesOption.KEYWORD),
  MPE(ProParserTokenTypes.MPE, "mpe", NodeTypesOption.KEYWORD),
  MTIME(ProParserTokenTypes.MTIME, "mtime", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  MULTIPLE(ProParserTokenTypes.MULTIPLE, "multiple", NodeTypesOption.KEYWORD),
  MULTIPLEKEY(ProParserTokenTypes.MULTIPLEKEY, "multiple-key", NodeTypesOption.KEYWORD),
  MULTIPLY(ProParserTokenTypes.MULTIPLY, "*", NodeTypesOption.SYMBOL),
  MUSTEXIST(ProParserTokenTypes.MUSTEXIST, "must-exist", NodeTypesOption.KEYWORD),
  METHOD_PARAM_LIST(ProParserTokenTypes.Method_param_list, NodeTypesOption.STRUCTURE),
  METHOD_PARAMETER(ProParserTokenTypes.Method_parameter, NodeTypesOption.STRUCTURE),

  NAMEDOT(ProParserTokenTypes.NAMEDOT, ".", NodeTypesOption.SYMBOL),
  NAMESPACEPREFIX(ProParserTokenTypes.NAMESPACEPREFIX, "namespace-prefix", NodeTypesOption.KEYWORD),
  NAMESPACEURI(ProParserTokenTypes.NAMESPACEURI, "namespace-uri", NodeTypesOption.KEYWORD),
  NATIVE(ProParserTokenTypes.NATIVE, "native", NodeTypesOption.KEYWORD),
  NE(ProParserTokenTypes.NE, "ne", NodeTypesOption.KEYWORD),
  NESTED(ProParserTokenTypes.NESTED, "nested", NodeTypesOption.KEYWORD),
  NEW(ProParserTokenTypes.NEW, "new", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  NEWINSTANCE(ProParserTokenTypes.NEWINSTANCE, "new-instance", NodeTypesOption.KEYWORD),
  NEWLINE(ProParserTokenTypes.NEWLINE, NodeTypesOption.NONPRINTABLE),
  NEXT(ProParserTokenTypes.NEXT, "next", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NEXTPROMPT(ProParserTokenTypes.NEXTPROMPT, "next-prompt", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NEXTVALUE(ProParserTokenTypes.NEXTVALUE, "next-value", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  NO(ProParserTokenTypes.NO, "no", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOAPPLY(ProParserTokenTypes.NOAPPLY, "no-apply", NodeTypesOption.KEYWORD),
  // NOARRAYMESSAGE(ProParserTokenTypes.NOARRAYMESSAGE, 10, "no-array-message", NodeTypesOption.KEYWORD),
  NOASSIGN(ProParserTokenTypes.NOASSIGN, "no-assign", NodeTypesOption.KEYWORD),
  NOATTRLIST(ProParserTokenTypes.NOATTRLIST, "no-attr-list", 9, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOATTRSPACE(ProParserTokenTypes.NOATTRSPACE, "no-attr-space", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOAUTOVALIDATE(ProParserTokenTypes.NOAUTOVALIDATE, "no-auto-validate", NodeTypesOption.KEYWORD),
  NOBINDWHERE(ProParserTokenTypes.NOBINDWHERE, "no-bind-where", NodeTypesOption.KEYWORD),
  NOBOX(ProParserTokenTypes.NOBOX, "no-box", NodeTypesOption.KEYWORD),
  NOCOLUMNSCROLLING(ProParserTokenTypes.NOCOLUMNSCROLLING, "no-column-scrolling", 12, NodeTypesOption.KEYWORD),
  NOCONSOLE(ProParserTokenTypes.NOCONSOLE, "no-console", NodeTypesOption.KEYWORD),
  NOCONVERT(ProParserTokenTypes.NOCONVERT, "no-convert", NodeTypesOption.KEYWORD),
  NOCONVERT3DCOLORS(ProParserTokenTypes.NOCONVERT3DCOLORS, "no-convert-3d-colors", 13, NodeTypesOption.KEYWORD),
  NOCURRENTVALUE(ProParserTokenTypes.NOCURRENTVALUE, "no-current-value", NodeTypesOption.KEYWORD),
  NODEBUG(ProParserTokenTypes.NODEBUG, "no-debug", NodeTypesOption.KEYWORD),
  NODRAG(ProParserTokenTypes.NODRAG, "no-drag", NodeTypesOption.KEYWORD),
  NOECHO(ProParserTokenTypes.NOECHO, "no-echo", NodeTypesOption.KEYWORD),
  NOEMPTYSPACE(ProParserTokenTypes.NOEMPTYSPACE, "no-empty-space", NodeTypesOption.KEYWORD),
  NOERROR(ProParserTokenTypes.NOERROR_KW, "no-error", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOFILL(ProParserTokenTypes.NOFILL, "no-fill", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOFOCUS(ProParserTokenTypes.NOFOCUS, "no-focus", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOHELP(ProParserTokenTypes.NOHELP, "no-help", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOHIDE(ProParserTokenTypes.NOHIDE, "no-hide", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOINDEXHINT(ProParserTokenTypes.NOINDEXHINT, "no-index-hint", NodeTypesOption.KEYWORD),
  NOINHERITBGCOLOR(ProParserTokenTypes.NOINHERITBGCOLOR, "no-inherit-bgcolor", 14, NodeTypesOption.KEYWORD),
  NOINHERITFGCOLOR(ProParserTokenTypes.NOINHERITFGCOLOR, "no-inherit-fgcolor", 14, NodeTypesOption.KEYWORD),
  NOJOINBYSQLDB(ProParserTokenTypes.NOJOINBYSQLDB, "no-join-by-sqldb", NodeTypesOption.KEYWORD),
  NOLABELS(ProParserTokenTypes.NOLABELS, "no-labels", 8, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOLOBS(ProParserTokenTypes.NOLOBS, "no-lobs", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOLOCK(ProParserTokenTypes.NOLOCK, "no-lock", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOLOOKAHEAD(ProParserTokenTypes.NOLOOKAHEAD, "no-lookahead", NodeTypesOption.KEYWORD),
  NOMAP(ProParserTokenTypes.NOMAP, "no-map", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOMESSAGE(ProParserTokenTypes.NOMESSAGE, "no-message", 6, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NONE(ProParserTokenTypes.NONE, "none", NodeTypesOption.KEYWORD),
  NOPAUSE(ProParserTokenTypes.NOPAUSE, "no-pause", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOPREFETCH(ProParserTokenTypes.NOPREFETCH, "no-prefetch", 8, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NORETURNVALUE(ProParserTokenTypes.NORETURNVALUE, "no-return-value", 13, NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  NORMAL(ProParserTokenTypes.NORMAL, "normal", NodeTypesOption.KEYWORD),
  NORMALIZE(ProParserTokenTypes.NORMALIZE, "normalize", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  NOROWMARKERS(ProParserTokenTypes.NOROWMARKERS, "no-row-markers", NodeTypesOption.KEYWORD),
  NOSCROLLBARVERTICAL(ProParserTokenTypes.NOSCROLLBARVERTICAL, "no-scrollbar-vertical", 14, NodeTypesOption.KEYWORD),
  NOSEPARATECONNECTION(ProParserTokenTypes.NOSEPARATECONNECTION, "no-separate-connection", NodeTypesOption.KEYWORD),
  NOSEPARATORS(ProParserTokenTypes.NOSEPARATORS, "no-separators", NodeTypesOption.KEYWORD),
  NOT(ProParserTokenTypes.NOT, "not", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOTACTIVE(ProParserTokenTypes.NOTACTIVE, "not-active", NodeTypesOption.KEYWORD),
  NOTABSTOP(ProParserTokenTypes.NOTABSTOP, "no-tab-stop", 6, NodeTypesOption.KEYWORD),
  NOUNDERLINE(ProParserTokenTypes.NOUNDERLINE, "no-underline", 6, NodeTypesOption.KEYWORD),
  NOUNDO(ProParserTokenTypes.NOUNDO, "no-undo", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOVALIDATE(ProParserTokenTypes.NOVALIDATE, "no-validate", 6, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOW(ProParserTokenTypes.NOW, "now", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  NOWAIT(ProParserTokenTypes.NOWAIT, "no-wait", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NOWORDWRAP(ProParserTokenTypes.NOWORDWRAP, "no-word-wrap", NodeTypesOption.KEYWORD),
  NULL(ProParserTokenTypes.NULL_KW, "null", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  NUMALIASES(ProParserTokenTypes.NUMALIASES, "num-aliases", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  NUMBER(ProParserTokenTypes.NUMBER, NodeTypesOption.SYMBOL),
  NUMCOPIES(ProParserTokenTypes.NUMCOPIES, "num-copies", NodeTypesOption.KEYWORD),
  NUMDBS(ProParserTokenTypes.NUMDBS, "num-dbs", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  NUMENTRIES(ProParserTokenTypes.NUMENTRIES, "num-entries", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  NUMERIC(ProParserTokenTypes.NUMERIC, "numeric", NodeTypesOption.KEYWORD),
  NUMRESULTS(ProParserTokenTypes.NUMRESULTS, "num-results", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  NOT_CASESENS(ProParserTokenTypes.Not_casesens, NodeTypesOption.STRUCTURE),
  NOT_NULL(ProParserTokenTypes.Not_null, NodeTypesOption.STRUCTURE),

  // O
  OBJCOLON(ProParserTokenTypes.OBJCOLON, ":", NodeTypesOption.SYMBOL),
  OBJECT(ProParserTokenTypes.OBJECT, "object", NodeTypesOption.KEYWORD),
  OCTETLENGTH(ProParserTokenTypes.OCTETLENGTH, "octet-length", NodeTypesOption.KEYWORD),
  OF(ProParserTokenTypes.OF, "of", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OFF(ProParserTokenTypes.OFF, "off", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OK(ProParserTokenTypes.OK, "ok", NodeTypesOption.KEYWORD),
  OKCANCEL(ProParserTokenTypes.OKCANCEL, "ok-cancel", NodeTypesOption.KEYWORD),
  OLD(ProParserTokenTypes.OLD, "old", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ON(ProParserTokenTypes.ON, "on", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ONLY(ProParserTokenTypes.ONLY, "only", NodeTypesOption.KEYWORD),
  OPEN(ProParserTokenTypes.OPEN, "open", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OPSYS(ProParserTokenTypes.OPSYS, "opsys", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  OPTION(ProParserTokenTypes.OPTION, "option", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OPTIONS(ProParserTokenTypes.OPTIONS, "options", NodeTypesOption.KEYWORD),
  OPTIONSFILE(ProParserTokenTypes.OPTIONSFILE, "options-file", NodeTypesOption.KEYWORD),
  OR(ProParserTokenTypes.OR, "or", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ORDER(ProParserTokenTypes.ORDER, "order", NodeTypesOption.KEYWORD),
  ORDEREDJOIN(ProParserTokenTypes.ORDEREDJOIN, "ordered-join", NodeTypesOption.KEYWORD),
  ORDINAL(ProParserTokenTypes.ORDINAL, "ordinal", NodeTypesOption.KEYWORD),
  OS2(ProParserTokenTypes.OS2, "os2", NodeTypesOption.KEYWORD),
  OS400(ProParserTokenTypes.OS400, "os400", NodeTypesOption.KEYWORD),
  OSAPPEND(ProParserTokenTypes.OSAPPEND, "os-append", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OSCOMMAND(ProParserTokenTypes.OSCOMMAND, "os-command", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OSCOPY(ProParserTokenTypes.OSCOPY, "os-copy", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OSCREATEDIR(ProParserTokenTypes.OSCREATEDIR, "os-create-dir", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OSDELETE(ProParserTokenTypes.OSDELETE, "os-delete", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OSDIR(ProParserTokenTypes.OSDIR, "os-dir", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OSDRIVES(ProParserTokenTypes.OSDRIVES, "os-drives", 8, NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  OSERROR(ProParserTokenTypes.OSERROR, "os-error", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  OSGETENV(ProParserTokenTypes.OSGETENV, "os-getenv", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  OSRENAME(ProParserTokenTypes.OSRENAME, "os-rename", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OTHERWISE(ProParserTokenTypes.OTHERWISE, "otherwise", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OUTER(ProParserTokenTypes.OUTER, "outer", NodeTypesOption.KEYWORD),
  OUTERJOIN(ProParserTokenTypes.OUTERJOIN, "outer-join", NodeTypesOption.KEYWORD),
  OUTPUT(ProParserTokenTypes.OUTPUT, "output", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OVERLAY(ProParserTokenTypes.OVERLAY, "overlay", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  OVERRIDE(ProParserTokenTypes.OVERRIDE, "override", NodeTypesOption.KEYWORD),

  // P
  PAGE(ProParserTokenTypes.PAGE, "page", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PAGEBOTTOM(ProParserTokenTypes.PAGEBOTTOM, "page-bottom", 8, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PAGED(ProParserTokenTypes.PAGED, "paged", NodeTypesOption.KEYWORD),
  PAGENUMBER(ProParserTokenTypes.PAGENUMBER, "page-number", 8, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  PAGESIZE(ProParserTokenTypes.PAGESIZE_KW, "page-size", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_NO_ARG_FUNC,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  PAGETOP(ProParserTokenTypes.PAGETOP, "page-top", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PAGEWIDTH(ProParserTokenTypes.PAGEWIDTH, "page-width", 8, NodeTypesOption.KEYWORD),
  PARAMETER(ProParserTokenTypes.PARAMETER, "parameter", 5, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PARENT(ProParserTokenTypes.PARENT, "parent", NodeTypesOption.KEYWORD), // PARENT is not a reserved keyword (not what documentation says)
  PARENTFIELDSAFTER(ProParserTokenTypes.PARENTFIELDSAFTER, "parent-fields-after", NodeTypesOption.KEYWORD),
  PARENTFIELDSBEFORE(ProParserTokenTypes.PARENTFIELDSBEFORE, "parent-fields-before", NodeTypesOption.KEYWORD),
  PARENTIDFIELD(ProParserTokenTypes.PARENTIDFIELD, "parent-id-field", NodeTypesOption.KEYWORD),
  PARENTIDRELATION(ProParserTokenTypes.PARENTIDRELATION, "parent-id-relation", NodeTypesOption.KEYWORD),
  PARTIALKEY(ProParserTokenTypes.PARTIALKEY, "partial-key", NodeTypesOption.KEYWORD),
  PASCAL(ProParserTokenTypes.PASCAL_KW, "pascal", NodeTypesOption.KEYWORD),
  PASSWORDFIELD(ProParserTokenTypes.PASSWORDFIELD, "password-field", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PAUSE(ProParserTokenTypes.PAUSE, "pause", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PBEHASHALGORITHM(ProParserTokenTypes.PBEHASHALGORITHM, "pbe-hash-algorithm", 12, NodeTypesOption.KEYWORD),
  PBEKEYROUNDS(ProParserTokenTypes.PBEKEYROUNDS, "pbe-key-rounds", NodeTypesOption.KEYWORD),
  PDBNAME(ProParserTokenTypes.PDBNAME, "pdbname", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  PERFORMANCE(ProParserTokenTypes.PERFORMANCE, "performance", 4, NodeTypesOption.KEYWORD),
  PERIOD(ProParserTokenTypes.PERIOD, ".", NodeTypesOption.SYMBOL),
  PERIODSTART(ProParserTokenTypes.PERIODSTART, ".", NodeTypesOption.SYMBOL),
  PERSISTENT(ProParserTokenTypes.PERSISTENT, "persistent", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PFCOLOR(ProParserTokenTypes.PFCOLOR, "pfcolor", 3, NodeTypesOption.KEYWORD),
  PINNABLE(ProParserTokenTypes.PINNABLE, "pinnable", NodeTypesOption.KEYWORD),
  PIPE(ProParserTokenTypes.PIPE, "|", NodeTypesOption.SYMBOL),
  PLUS(ProParserTokenTypes.PLUS, "+", NodeTypesOption.SYMBOL),
  PLUSMINUSSTART(ProParserTokenTypes.PLUSMINUSSTART),
  PORTRAIT(ProParserTokenTypes.PORTRAIT, "portrait", NodeTypesOption.KEYWORD),
  POSITION(ProParserTokenTypes.POSITION, "position", NodeTypesOption.KEYWORD),
  PRECISION(ProParserTokenTypes.PRECISION, "precision", NodeTypesOption.KEYWORD),
  PREFERDATASET(ProParserTokenTypes.PREFERDATASET, "prefer-dataset", NodeTypesOption.KEYWORD),
  PREPROCESS(ProParserTokenTypes.PREPROCESS, "preprocess", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PREPROCESSDIRECTIVE(ProParserTokenTypes.PREPROCESSDIRECTIVE, NodeTypesOption.PREPROCESSOR),
  PREPROCESSELSE(ProParserTokenTypes.PREPROCESSELSE, NodeTypesOption.PREPROCESSOR),
  PREPROCESSELSEIF(ProParserTokenTypes.PREPROCESSELSEIF, NodeTypesOption.PREPROCESSOR),
  PREPROCESSENDIF(ProParserTokenTypes.PREPROCESSENDIF, NodeTypesOption.PREPROCESSOR),
  PREPROCESSIF(ProParserTokenTypes.PREPROCESSIF, NodeTypesOption.PREPROCESSOR),
  PREPROCESSJMESSAGE(ProParserTokenTypes.PREPROCESSJMESSAGE, NodeTypesOption.PREPROCESSOR),
  PREPROCESSMESSAGE(ProParserTokenTypes.PREPROCESSMESSAGE, NodeTypesOption.PREPROCESSOR),
  PREPROCESSTOKEN(ProParserTokenTypes.PREPROCESSTOKEN, NodeTypesOption.PREPROCESSOR),
  PREPROCESSUNDEFINE(ProParserTokenTypes.PREPROCESSUNDEFINE, NodeTypesOption.PREPROCESSOR),
  PRESELECT(ProParserTokenTypes.PRESELECT, "preselect", 6, NodeTypesOption.KEYWORD),
  PREV(ProParserTokenTypes.PREV, "prev", NodeTypesOption.KEYWORD),
  PRIMARY(ProParserTokenTypes.PRIMARY, "primary", NodeTypesOption.KEYWORD),
  PRINTER(ProParserTokenTypes.PRINTER, "printer", NodeTypesOption.KEYWORD),
  PRINTERSETUP(ProParserTokenTypes.PRINTERSETUP, "printer-setup", NodeTypesOption.KEYWORD),
  PRIVATE(ProParserTokenTypes.PRIVATE, "private", NodeTypesOption.KEYWORD),
  PRIVILEGES(ProParserTokenTypes.PRIVILEGES, "privileges", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PROCEDURE(ProParserTokenTypes.PROCEDURE, "procedure", 5, NodeTypesOption.KEYWORD),
  PROCEDURECALLTYPE(ProParserTokenTypes.PROCEDURECALLTYPE, "procedure-call-type", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  PROCESS(ProParserTokenTypes.PROCESS, "process", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PROCESSARCHITECTURE(ProParserTokenTypes.PROCESSARCHITECTURE, "process-architecture", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  PROCHANDLE(ProParserTokenTypes.PROCHANDLE, "proc-handle", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  PROCSTATUS(ProParserTokenTypes.PROCSTATUS, "proc-status", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  PROCTEXT(ProParserTokenTypes.PROCTEXT, "proc-text", NodeTypesOption.KEYWORD),
  PROCTEXTBUFFER(ProParserTokenTypes.PROCTEXTBUFFER, "proc-text-buffer", NodeTypesOption.KEYWORD),
  PROFILER(ProParserTokenTypes.PROFILER, "profiler", NodeTypesOption.KEYWORD, NodeTypesOption.SYSHDL),
  PROGRAMNAME(ProParserTokenTypes.PROGRAMNAME, "program-name", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  PROGRESS(ProParserTokenTypes.PROGRESS, "progress", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  PROMPT(ProParserTokenTypes.PROMPT, "prompt", NodeTypesOption.KEYWORD),
  PROMPTFOR(ProParserTokenTypes.PROMPTFOR, "prompt-for", 8, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PROMSGS(ProParserTokenTypes.PROMSGS, "promsgs", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  PROPARSEDIRECTIVE(ProParserTokenTypes.PROPARSEDIRECTIVE, NodeTypesOption.PREPROCESSOR),
  PROPATH(ProParserTokenTypes.PROPATH, "propath", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  PROPERTY(ProParserTokenTypes.PROPERTY, "property", NodeTypesOption.KEYWORD),
  PROPERTY_GETTER(ProParserTokenTypes.Property_getter, NodeTypesOption.STRUCTURE),
  PROPERTY_SETTER(ProParserTokenTypes.Property_setter, NodeTypesOption.STRUCTURE),
  PROTECTED(ProParserTokenTypes.PROTECTED, "protected", NodeTypesOption.KEYWORD),
  PROVERSION(ProParserTokenTypes.PROVERSION, "proversion", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  PUBLIC(ProParserTokenTypes.PUBLIC, "public", NodeTypesOption.KEYWORD),
  PUBLISH(ProParserTokenTypes.PUBLISH, "publish", NodeTypesOption.KEYWORD),
  PUT(ProParserTokenTypes.PUT, "put", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PUTBITS(ProParserTokenTypes.PUTBITS, "put-bits", NodeTypesOption.KEYWORD),
  PUTBYTE(ProParserTokenTypes.PUTBYTE, "put-byte", "putbyte", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PUTBYTES(ProParserTokenTypes.PUTBYTES, "put-bytes", NodeTypesOption.KEYWORD),
  PUTDOUBLE(ProParserTokenTypes.PUTDOUBLE, "put-double", NodeTypesOption.KEYWORD),
  PUTFLOAT(ProParserTokenTypes.PUTFLOAT, "put-float", NodeTypesOption.KEYWORD),
  PUTINT64(ProParserTokenTypes.PUTINT64, "put-int64", NodeTypesOption.KEYWORD),
  PUTKEYVALUE(ProParserTokenTypes.PUTKEYVALUE, "put-key-value", 11, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  PUTLONG(ProParserTokenTypes.PUTLONG, "put-long", NodeTypesOption.KEYWORD),
  PUTSHORT(ProParserTokenTypes.PUTSHORT, "put-short", NodeTypesOption.KEYWORD),
  PUTSTRING(ProParserTokenTypes.PUTSTRING, "put-string", NodeTypesOption.KEYWORD),
  PUTUNSIGNEDLONG(ProParserTokenTypes.PUTUNSIGNEDLONG, "put-unsigned-long", NodeTypesOption.KEYWORD),
  PUTUNSIGNEDSHORT(ProParserTokenTypes.PUTUNSIGNEDSHORT, "put-unsigned-short", NodeTypesOption.KEYWORD),
  PARAMETER_LIST(ProParserTokenTypes.Parameter_list, NodeTypesOption.STRUCTURE),
  PROGRAM_ROOT(ProParserTokenTypes.Program_root, NodeTypesOption.STRUCTURE),
  PROGRAM_TAIL(ProParserTokenTypes.Program_tail, NodeTypesOption.STRUCTURE),

  // Q
  QSTRING(ProParserTokenTypes.QSTRING),
  QUERY(ProParserTokenTypes.QUERY, "query", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  QUERYCLOSE(ProParserTokenTypes.QUERYCLOSE, "query-close", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  QUERYOFFEND(ProParserTokenTypes.QUERYOFFEND, "query-off-end", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  QUERYTUNING(ProParserTokenTypes.QUERYTUNING, "query-tuning", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  QUESTION(ProParserTokenTypes.QUESTION, "question", NodeTypesOption.KEYWORD),
  QUIT(ProParserTokenTypes.QUIT, "quit", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  QUOTER(ProParserTokenTypes.QUOTER, "quoter", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),

  // R
  RADIOBUTTONS(ProParserTokenTypes.RADIOBUTTONS, "radio-buttons", NodeTypesOption.KEYWORD),
  RADIOSET(ProParserTokenTypes.RADIOSET, "radio-set", NodeTypesOption.KEYWORD),
  RANDOM(ProParserTokenTypes.RANDOM, "random", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  RAW(ProParserTokenTypes.RAW, "raw", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  RAWTRANSFER(ProParserTokenTypes.RAWTRANSFER, "raw-transfer", NodeTypesOption.KEYWORD),
  RCODEINFORMATION(ProParserTokenTypes.RCODEINFORMATION, "rcode-information", 10, NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED, NodeTypesOption.SYSHDL),
  READ(ProParserTokenTypes.READ, "read", NodeTypesOption.KEYWORD),
  READAVAILABLE(ProParserTokenTypes.READAVAILABLE, "read-available", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  READEXACTNUM(ProParserTokenTypes.READEXACTNUM, "read-exact-num", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  READKEY(ProParserTokenTypes.READKEY, "readkey", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  READONLY(ProParserTokenTypes.READONLY, "read-only", NodeTypesOption.KEYWORD),
  REAL(ProParserTokenTypes.REAL, "real", NodeTypesOption.KEYWORD),
  RECID(ProParserTokenTypes.RECID, "recid", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  RECORDLENGTH(ProParserTokenTypes.RECORDLENGTH, "record-length", 10, NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  RECORD_NAME(ProParserTokenTypes.RECORD_NAME, NodeTypesOption.STRUCTURE),
  RECTANGLE(ProParserTokenTypes.RECTANGLE, "rectangle", 4, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  RECURSIVE(ProParserTokenTypes.RECURSIVE, "recursive", NodeTypesOption.KEYWORD),
  REFERENCEONLY(ProParserTokenTypes.REFERENCEONLY, "reference-only", NodeTypesOption.KEYWORD),
  REJECTED(ProParserTokenTypes.REJECTED, "rejected", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  RELATIONFIELDS(ProParserTokenTypes.RELATIONFIELDS, "relation-fields", 11, NodeTypesOption.KEYWORD),
  RELEASE(ProParserTokenTypes.RELEASE, "release", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  REPEAT(ProParserTokenTypes.REPEAT, "repeat", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  REPLACE(ProParserTokenTypes.REPLACE, "replace", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  REPLICATIONCREATE(ProParserTokenTypes.REPLICATIONCREATE, "replication-create", NodeTypesOption.KEYWORD),
  REPLICATIONDELETE(ProParserTokenTypes.REPLICATIONDELETE, "replication-delete", NodeTypesOption.KEYWORD),
  REPLICATIONWRITE(ProParserTokenTypes.REPLICATIONWRITE, "replication-write", NodeTypesOption.KEYWORD),
  REPOSITION(ProParserTokenTypes.REPOSITION, "reposition", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  REPOSITIONBACKWARD(ProParserTokenTypes.REPOSITIONBACKWARD, "reposition-backward", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  REPOSITIONFORWARD(ProParserTokenTypes.REPOSITIONFORWARD, "reposition-forward", NodeTypesOption.KEYWORD),
  REPOSITIONMODE(ProParserTokenTypes.REPOSITIONMODE, "reposition-mode", NodeTypesOption.KEYWORD),
  REPOSITIONTOROW(ProParserTokenTypes.REPOSITIONTOROW, "reposition-to-row", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  REPOSITIONTOROWID(ProParserTokenTypes.REPOSITIONTOROWID, "reposition-to-rowid", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  REQUEST(ProParserTokenTypes.REQUEST, "request", NodeTypesOption.KEYWORD),
  RESTARTROW(ProParserTokenTypes.RESTARTROW, "restart-row", NodeTypesOption.KEYWORD),
  RESULT(ProParserTokenTypes.RESULT, "result", NodeTypesOption.KEYWORD),
  RETAIN(ProParserTokenTypes.RETAIN, "retain", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  RETAINSHAPE(ProParserTokenTypes.RETAINSHAPE, "retain-shape", 8, NodeTypesOption.KEYWORD),
  RETRY(ProParserTokenTypes.RETRY, "retry", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  RETRYCANCEL(ProParserTokenTypes.RETRYCANCEL, "retry-cancel", NodeTypesOption.KEYWORD),
  RETURN(ProParserTokenTypes.RETURN, "return", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  RETURNS(ProParserTokenTypes.RETURNS, "returns", NodeTypesOption.KEYWORD), // Not a reserved keyword
  RETURNTOSTARTDIR(ProParserTokenTypes.RETURNTOSTARTDIR, "return-to-start-dir", 18, NodeTypesOption.KEYWORD),
  RETURNVALUE(ProParserTokenTypes.RETURNVALUE, "return-value", 10, NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  REVERSEFROM(ProParserTokenTypes.REVERSEFROM, "reverse-from", NodeTypesOption.KEYWORD),
  REVERT(ProParserTokenTypes.REVERT, "revert", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  REVOKE(ProParserTokenTypes.REVOKE, "revoke", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  RGBVALUE(ProParserTokenTypes.RGBVALUE, "rgb-value", 5, NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  RIGHT(ProParserTokenTypes.RIGHT, "right", NodeTypesOption.KEYWORD),
  RIGHTALIGNED(ProParserTokenTypes.RIGHTALIGNED, "right-aligned", 11, NodeTypesOption.KEYWORD),
  RIGHTANGLE(ProParserTokenTypes.RIGHTANGLE, ">", NodeTypesOption.SYMBOL),
  RIGHTBRACE(ProParserTokenTypes.RIGHTBRACE, "]", NodeTypesOption.SYMBOL),
  RIGHTCURLY(ProParserTokenTypes.RIGHTCURLY, "}", NodeTypesOption.SYMBOL),
  RIGHTPAREN(ProParserTokenTypes.RIGHTPAREN, ")", NodeTypesOption.SYMBOL),
  RIGHTTRIM(ProParserTokenTypes.RIGHTTRIM, "right-trim", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  RINDEX(ProParserTokenTypes.RINDEX, "r-index", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  ROUND(ProParserTokenTypes.ROUND, "round", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  ROUNDED(ProParserTokenTypes.ROUNDED, "rounded", NodeTypesOption.KEYWORD),
  ROUTINELEVEL(ProParserTokenTypes.ROUTINELEVEL, "routine-level", NodeTypesOption.KEYWORD),
  ROW(ProParserTokenTypes.ROW, "row", NodeTypesOption.KEYWORD),
  ROWCREATED(ProParserTokenTypes.ROWCREATED, "row-created", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ROWDELETED(ProParserTokenTypes.ROWDELETED, "row-deleted", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ROWHEIGHTCHARS(ProParserTokenTypes.ROWHEIGHTCHARS, "row-height", 10, "row-height-chars", 12, NodeTypesOption.KEYWORD),
  ROWHEIGHTPIXELS(ProParserTokenTypes.ROWHEIGHTPIXELS, "row-height-pixels", 12, NodeTypesOption.KEYWORD),
  ROWID(ProParserTokenTypes.ROWID, "rowid", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC), // Yes, ROWID is not a reserved keyword
  ROWMODIFIED(ProParserTokenTypes.ROWMODIFIED, "row-modified", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  ROWOF(ProParserTokenTypes.ROWOF, "row-of", NodeTypesOption.KEYWORD),
  ROWSTATE(ProParserTokenTypes.ROWSTATE, "row-state", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  ROWUNMODIFIED(ProParserTokenTypes.ROWUNMODIFIED, "row-unmodified", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  RULE(ProParserTokenTypes.RULE, "rule", NodeTypesOption.KEYWORD),
  RUN(ProParserTokenTypes.RUN, "run", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  RUNPROCEDURE(ProParserTokenTypes.RUNPROCEDURE, "run-procedure", 8, NodeTypesOption.KEYWORD),

  // S
  SAVE(ProParserTokenTypes.SAVE, "save", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SAVEAS(ProParserTokenTypes.SAVEAS, "save-as", NodeTypesOption.KEYWORD),
  SAVECACHE(ProParserTokenTypes.SAVECACHE, "savecache", NodeTypesOption.KEYWORD),
  SAXATTRIBUTES(ProParserTokenTypes.SAXATTRIBUTES, "sax-attributes", NodeTypesOption.KEYWORD),
  SAXCOMPLETE(ProParserTokenTypes.SAXCOMPLETE, "sax-complete", 10, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SAXPARSERERROR(ProParserTokenTypes.SAXPARSERERROR, "sax-parser-error", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  SAXREADER(ProParserTokenTypes.SAXREADER, "sax-reader", NodeTypesOption.KEYWORD),
  SAXRUNNING(ProParserTokenTypes.SAXRUNNING, "sax-running", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SAXUNINITIALIZED(ProParserTokenTypes.SAXUNINITIALIZED, "sax-uninitialized", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  SAXWRITER(ProParserTokenTypes.SAXWRITER, "sax-writer", NodeTypesOption.KEYWORD),
  SAXWRITEBEGIN(ProParserTokenTypes.SAXWRITEBEGIN, "sax-write-begin", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  SAXWRITECOMPLETE(ProParserTokenTypes.SAXWRITECOMPLETE, "sax-write-complete", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  SAXWRITECONTENT(ProParserTokenTypes.SAXWRITECONTENT, "sax-write-content", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  SAXWRITEELEMENT(ProParserTokenTypes.SAXWRITEELEMENT, "sax-write-element", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  SAXWRITEERROR(ProParserTokenTypes.SAXWRITEERROR, "sax-write-error", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  SAXWRITEIDLE(ProParserTokenTypes.SAXWRITEIDLE, "sax-write-idle", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SAXWRITETAG(ProParserTokenTypes.SAXWRITETAG, "sax-write-tag", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SCHEMA(ProParserTokenTypes.SCHEMA, "schema", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SCOPEDDEFINE(ProParserTokenTypes.SCOPEDDEFINE, NodeTypesOption.PREPROCESSOR),
  SCREEN(ProParserTokenTypes.SCREEN, "screen", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SCREENIO(ProParserTokenTypes.SCREENIO, "screen-io", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SCREENLINES(ProParserTokenTypes.SCREENLINES, "screen-lines", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  SCROLL(ProParserTokenTypes.SCROLL, "scroll", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SCROLLABLE(ProParserTokenTypes.SCROLLABLE, "scrollable", NodeTypesOption.KEYWORD),
  SCROLLBARHORIZONTAL(ProParserTokenTypes.SCROLLBARHORIZONTAL, "scrollbar-horizontal", 11, NodeTypesOption.KEYWORD),
  SCROLLBARVERTICAL(ProParserTokenTypes.SCROLLBARVERTICAL, "scrollbar-vertical", 11, NodeTypesOption.KEYWORD),
  SCROLLING(ProParserTokenTypes.SCROLLING, "scrolling", NodeTypesOption.KEYWORD),
  SDBNAME(ProParserTokenTypes.SDBNAME, "sdbname", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  SEARCH(ProParserTokenTypes.SEARCH, "search", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  SEARCHSELF(ProParserTokenTypes.SEARCHSELF, "search-self", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SEARCHTARGET(ProParserTokenTypes.SEARCHTARGET, "search-target", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SECTION(ProParserTokenTypes.SECTION, "section", NodeTypesOption.KEYWORD),
  SECURITYPOLICY(ProParserTokenTypes.SECURITYPOLICY, "security-policy", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED, NodeTypesOption.SYSHDL),
  SEEK(ProParserTokenTypes.SEEK, "seek", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  SELECT(ProParserTokenTypes.SELECT, "select", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SELECTION(ProParserTokenTypes.SELECTION, "selection", NodeTypesOption.KEYWORD),
  SELECTIONLIST(ProParserTokenTypes.SELECTIONLIST, "selection-list", NodeTypesOption.KEYWORD),
  SELF(ProParserTokenTypes.SELF, "self", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED, NodeTypesOption.SYSHDL),
  SEMI(ProParserTokenTypes.SEMI, ";", NodeTypesOption.SYMBOL),
  SEND(ProParserTokenTypes.SEND, "send", NodeTypesOption.KEYWORD),
  SENDSQLSTATEMENT(ProParserTokenTypes.SENDSQLSTATEMENT, "send-sql-statement", 8, NodeTypesOption.KEYWORD),
  SEPARATECONNECTION(ProParserTokenTypes.SEPARATECONNECTION, "separate-connection", NodeTypesOption.KEYWORD),
  SEPARATORS(ProParserTokenTypes.SEPARATORS, "separators", NodeTypesOption.KEYWORD),
  SERIALIZABLE(ProParserTokenTypes.SERIALIZABLE, "serializable", NodeTypesOption.KEYWORD),
  SERIALIZEHIDDEN(ProParserTokenTypes.SERIALIZEHIDDEN, "serialize-hidden", NodeTypesOption.KEYWORD),
  SERIALIZENAME(ProParserTokenTypes.SERIALIZENAME, "serialize-name", NodeTypesOption.KEYWORD),
  SERVER(ProParserTokenTypes.SERVER, "server", NodeTypesOption.KEYWORD),
  SERVERSOCKET(ProParserTokenTypes.SERVERSOCKET, "server-socket", NodeTypesOption.KEYWORD),
  SESSION(ProParserTokenTypes.SESSION, "session", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.SYSHDL),
  SET(ProParserTokenTypes.SET, "set", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SETATTRCALLTYPE(ProParserTokenTypes.SETATTRCALLTYPE, "set-attr-call-type", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  SETBYTEORDER(ProParserTokenTypes.SETBYTEORDER, "set-byte-order", NodeTypesOption.KEYWORD),
  SETCONTENTS(ProParserTokenTypes.SETCONTENTS, "set-contents", NodeTypesOption.KEYWORD),
  SETCURRENTVALUE(ProParserTokenTypes.SETCURRENTVALUE, "set-current-value", NodeTypesOption.KEYWORD),
  SETDBCLIENT(ProParserTokenTypes.SETDBCLIENT, "set-db-client", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  SETEFFECTIVETENANT(ProParserTokenTypes.SETEFFECTIVETENANT, "set-effective-tenant", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  SETPOINTERVALUE(ProParserTokenTypes.SETPOINTERVALUE, "set-pointer-value", 15, NodeTypesOption.KEYWORD),
  SETSIZE(ProParserTokenTypes.SETSIZE, "set-size", NodeTypesOption.KEYWORD),
  SETUSERID(ProParserTokenTypes.SETUSERID, "setuserid", 7, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  SHA1DIGEST(ProParserTokenTypes.SHA1DIGEST, "sha1-digest", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  SHARED(ProParserTokenTypes.SHARED, "shared", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SHARELOCK(ProParserTokenTypes.SHARELOCK, "share-lock", 5, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SHORT(ProParserTokenTypes.SHORT, "short", NodeTypesOption.KEYWORD),
  SHOWSTATS(ProParserTokenTypes.SHOWSTATS, "show-stats", 9, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SIDELABELS(ProParserTokenTypes.SIDELABELS, "side-labels", 8, NodeTypesOption.KEYWORD),
  SIGNATURE(ProParserTokenTypes.SIGNATURE, "signature", NodeTypesOption.KEYWORD),
  SILENT(ProParserTokenTypes.SILENT, "silent", NodeTypesOption.KEYWORD),
  SIMPLE(ProParserTokenTypes.SIMPLE, "simple", NodeTypesOption.KEYWORD),
  SINGLE(ProParserTokenTypes.SINGLE, "single", NodeTypesOption.KEYWORD),
  SINGLERUN(ProParserTokenTypes.SINGLERUN, "single-run", NodeTypesOption.KEYWORD),
  SINGLETON(ProParserTokenTypes.SINGLETON, "singleton", NodeTypesOption.KEYWORD),
  SINGLEQUOTE(ProParserTokenTypes.SINGLEQUOTE, "'", NodeTypesOption.SYMBOL),
  SIZE(ProParserTokenTypes.SIZE, "size", NodeTypesOption.KEYWORD),
  SIZECHARS(ProParserTokenTypes.SIZECHARS, "size-chars", 6, NodeTypesOption.KEYWORD),
  SIZEPIXELS(ProParserTokenTypes.SIZEPIXELS, "size-pixels", 6, NodeTypesOption.KEYWORD),
  SKIP(ProParserTokenTypes.SKIP, "skip", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SKIPDELETEDRECORD(ProParserTokenTypes.SKIPDELETEDRECORD, "skip-deleted-record", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  SKIPGROUPDUPLICATES(ProParserTokenTypes.SKIPGROUPDUPLICATES, "skip-group-duplicates", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  SLASH(ProParserTokenTypes.SLASH, "/", NodeTypesOption.SYMBOL),
  SLIDER(ProParserTokenTypes.SLIDER, "slider", NodeTypesOption.KEYWORD),
  SMALLINT(ProParserTokenTypes.SMALLINT, "smallint", NodeTypesOption.KEYWORD),
  SOAPHEADER(ProParserTokenTypes.SOAPHEADER, "soap-header", NodeTypesOption.KEYWORD),
  SOAPHEADERENTRYREF(ProParserTokenTypes.SOAPHEADERENTRYREF, "soap-header-entryref", NodeTypesOption.KEYWORD),
  SOCKET(ProParserTokenTypes.SOCKET, "socket", NodeTypesOption.KEYWORD),
  SOME(ProParserTokenTypes.SOME, "some", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SORT(ProParserTokenTypes.SORT, "sort", NodeTypesOption.KEYWORD),
  SOURCE(ProParserTokenTypes.SOURCE, "source", NodeTypesOption.KEYWORD),
  SOURCEPROCEDURE(ProParserTokenTypes.SOURCEPROCEDURE, "source-procedure", NodeTypesOption.KEYWORD,
      NodeTypesOption.SYSHDL),
  SPACE(ProParserTokenTypes.SPACE, "space", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SQL(ProParserTokenTypes.SQL, "sql", NodeTypesOption.KEYWORD),
  SQRT(ProParserTokenTypes.SQRT, "sqrt", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  SQSTRING(ProParserTokenTypes.SQSTRING),
  SSLSERVERNAME(ProParserTokenTypes.SSLSERVERNAME, "ssl-server-name", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  STAR(ProParserTokenTypes.STAR, "*", NodeTypesOption.SYMBOL),
  START(ProParserTokenTypes.START, "start", NodeTypesOption.KEYWORD),
  STARTING(ProParserTokenTypes.STARTING, "starting", NodeTypesOption.KEYWORD),
  STARTMOVE(ProParserTokenTypes.STARTMOVE, "start-move", NodeTypesOption.KEYWORD),
  STARTRESIZE(ProParserTokenTypes.STARTRESIZE, "start-resize", NodeTypesOption.KEYWORD),
  STARTROWRESIZE(ProParserTokenTypes.STARTROWRESIZE, "start-row-resize", NodeTypesOption.KEYWORD),
  STATIC(ProParserTokenTypes.STATIC, "static", NodeTypesOption.KEYWORD),
  STATUS(ProParserTokenTypes.STATUS, "status", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  STATUSBAR(ProParserTokenTypes.STATUSBAR, "status-bar", NodeTypesOption.KEYWORD),
  STDCALL(ProParserTokenTypes.STDCALL_KW, "stdcall", NodeTypesOption.KEYWORD),
  STOMPDETECTION(ProParserTokenTypes.STOMPDETECTION, "stomp-detection", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  STOMPFREQUENCY(ProParserTokenTypes.STOMPFREQUENCY, "stomp-frequency", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  STOP(ProParserTokenTypes.STOP, "stop", NodeTypesOption.KEYWORD),
  STOPAFTER(ProParserTokenTypes.STOPAFTER, "stop-after", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  STOREDPROCEDURE(ProParserTokenTypes.STOREDPROCEDURE, "stored-procedure", 11, NodeTypesOption.KEYWORD),
  STREAM(ProParserTokenTypes.STREAM, "stream", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  STREAMHANDLE(ProParserTokenTypes.STREAMHANDLE, "stream-handle", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  STREAMIO(ProParserTokenTypes.STREAMIO, "stream-io", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  STRETCHTOFIT(ProParserTokenTypes.STRETCHTOFIT, "stretch-to-fit", NodeTypesOption.KEYWORD),
  STRING(ProParserTokenTypes.STRING, "string", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  STRINGXREF(ProParserTokenTypes.STRINGXREF, "string-xref", NodeTypesOption.KEYWORD),
  SUBAVERAGE(ProParserTokenTypes.SUBAVERAGE, "sub-average", 7, NodeTypesOption.KEYWORD),
  SUBCOUNT(ProParserTokenTypes.SUBCOUNT, "sub-count", NodeTypesOption.KEYWORD),
  SUBMAXIMUM(ProParserTokenTypes.SUBMAXIMUM, "sub-maximum", 7, NodeTypesOption.KEYWORD),
  SUBMENU(ProParserTokenTypes.SUBMENU, "sub-menu", 4, NodeTypesOption.KEYWORD),
  SUBMENUHELP(ProParserTokenTypes.SUBMENUHELP, "sub-menu-help", NodeTypesOption.KEYWORD),
  SUBMINIMUM(ProParserTokenTypes.SUBMINIMUM, "sub-minimum", 7, NodeTypesOption.KEYWORD),
  SUBSCRIBE(ProParserTokenTypes.SUBSCRIBE, "subscribe", NodeTypesOption.KEYWORD),
  SUBSTITUTE(ProParserTokenTypes.SUBSTITUTE, "substitute", 5, NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  SUBSTRING(ProParserTokenTypes.SUBSTRING, "substring", 6, NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  SUBTOTAL(ProParserTokenTypes.SUBTOTAL, "sub-total", NodeTypesOption.KEYWORD),
  SUM(ProParserTokenTypes.SUM, "sum", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  SUMMARY(ProParserTokenTypes.SUMMARY, "summary", NodeTypesOption.KEYWORD),
  SUPER(ProParserTokenTypes.SUPER, "super", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_NO_ARG_FUNC,
      NodeTypesOption.MAY_BE_REGULAR_FUNC, NodeTypesOption.SYSHDL),
  SYMMETRICENCRYPTIONALGORITHM(ProParserTokenTypes.SYMMETRICENCRYPTIONALGORITHM, "symmetric-encryption-algorithm",
      NodeTypesOption.KEYWORD),
  SYMMETRICENCRYPTIONIV(ProParserTokenTypes.SYMMETRICENCRYPTIONIV, "symmetric-encryption-iv", NodeTypesOption.KEYWORD),
  SYMMETRICENCRYPTIONKEY(ProParserTokenTypes.SYMMETRICENCRYPTIONKEY, "symmetric-encryption-key",
      NodeTypesOption.KEYWORD),
  SYMMETRICSUPPORT(ProParserTokenTypes.SYMMETRICSUPPORT, "symmetric-support", NodeTypesOption.KEYWORD),
  SYSTEMDIALOG(ProParserTokenTypes.SYSTEMDIALOG, "system-dialog", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  SYSTEMHELP(ProParserTokenTypes.SYSTEMHELP, "system-help", NodeTypesOption.KEYWORD),
  SCANNER_HEAD(ProParserTokenTypes.Scanner_head, NodeTypesOption.STRUCTURE),
  SCANNER_TAIL(ProParserTokenTypes.Scanner_tail, NodeTypesOption.STRUCTURE),
  SQL_BEGINS(ProParserTokenTypes.Sql_begins, NodeTypesOption.STRUCTURE),
  SQL_BETWEEN(ProParserTokenTypes.Sql_between, NodeTypesOption.STRUCTURE),
  SQL_COMP_QUERY(ProParserTokenTypes.Sql_comp_query, NodeTypesOption.STRUCTURE),
  SQL_IN(ProParserTokenTypes.Sql_in, NodeTypesOption.STRUCTURE),
  SQL_LIKE(ProParserTokenTypes.Sql_like, NodeTypesOption.STRUCTURE),
  SQL_NULL_TEST(ProParserTokenTypes.Sql_null_test, NodeTypesOption.STRUCTURE),
  SQL_SELECT_WHAT(ProParserTokenTypes.Sql_select_what, NodeTypesOption.STRUCTURE),

  // T
  TABLE(ProParserTokenTypes.TABLE, "table", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TABLEHANDLE(ProParserTokenTypes.TABLEHANDLE, "table-handle", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TABLENUMBER(ProParserTokenTypes.TABLENUMBER, "table-number", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TABLESCAN(ProParserTokenTypes.TABLESCAN, "table-scan", NodeTypesOption.KEYWORD),
  TARGET(ProParserTokenTypes.TARGET, "target", NodeTypesOption.KEYWORD),
  TARGETPROCEDURE(ProParserTokenTypes.TARGETPROCEDURE, "target-procedure", NodeTypesOption.KEYWORD,
      NodeTypesOption.SYSHDL),
  TEMPTABLE(ProParserTokenTypes.TEMPTABLE, "temp-table", NodeTypesOption.KEYWORD),
  TENANT(ProParserTokenTypes.TENANT, "tenant", NodeTypesOption.KEYWORD),
  TENANTID(ProParserTokenTypes.TENANTID, "tenant-id", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  TENANTNAME(ProParserTokenTypes.TENANTNAME, "tenant-name", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  TENANTNAMETOID(ProParserTokenTypes.TENANTNAMETOID, "tenant-name-to-id", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  TENANTWHERE(ProParserTokenTypes.TENANTWHERE, "tenant-where", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TERMINAL(ProParserTokenTypes.TERMINAL, "term", "terminal", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  TERMINATE(ProParserTokenTypes.TERMINATE, "terminate", NodeTypesOption.KEYWORD),
  TEXT(ProParserTokenTypes.TEXT, "text", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TEXTCURSOR(ProParserTokenTypes.TEXTCURSOR, "text-cursor", NodeTypesOption.KEYWORD, NodeTypesOption.SYSHDL),
  TEXTSEGGROW(ProParserTokenTypes.TEXTSEGGROW, "text-seg-growth", 8, NodeTypesOption.KEYWORD),
  THEN(ProParserTokenTypes.THEN, "then", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  THISOBJECT(ProParserTokenTypes.THISOBJECT, "this-object", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.SYSHDL),
  THISPROCEDURE(ProParserTokenTypes.THISPROCEDURE, "this-procedure", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.SYSHDL),
  THREED(ProParserTokenTypes.THREED, "three-d", NodeTypesOption.KEYWORD),
  THROUGH(ProParserTokenTypes.THROUGH, "through", "thru", NodeTypesOption.KEYWORD),
  THROW(ProParserTokenTypes.THROW, "throw", NodeTypesOption.KEYWORD),
  TICMARKS(ProParserTokenTypes.TICMARKS, "tic-marks", NodeTypesOption.KEYWORD),
  TILDE(ProParserTokenTypes.TILDE, "~", NodeTypesOption.SYMBOL),
  TIME(ProParserTokenTypes.TIME, "time", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  TIMESTAMP(ProParserTokenTypes.TIMESTAMP, "timestamp", NodeTypesOption.KEYWORD),
  TIMEZONE(ProParserTokenTypes.TIMEZONE, "timezone", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  TITLE(ProParserTokenTypes.TITLE, "title", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TO(ProParserTokenTypes.TO, "to", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TODAY(ProParserTokenTypes.TODAY, "today", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  TOGGLEBOX(ProParserTokenTypes.TOGGLEBOX, "toggle-box", NodeTypesOption.KEYWORD),
  TOOLBAR(ProParserTokenTypes.TOOLBAR, "tool-bar", NodeTypesOption.KEYWORD),
  TOOLTIP(ProParserTokenTypes.TOOLTIP, "tooltip", NodeTypesOption.KEYWORD),
  TOP(ProParserTokenTypes.TOP, "top", NodeTypesOption.KEYWORD),
  TOPIC(ProParserTokenTypes.TOPIC, "topic", NodeTypesOption.KEYWORD),
  TOPNAVQUERY(ProParserTokenTypes.TOPNAVQUERY, "top-nav-query", NodeTypesOption.KEYWORD),
  TOPONLY(ProParserTokenTypes.TOPONLY, "top-only", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TOROWID(ProParserTokenTypes.TOROWID, "to-rowid", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  TOTAL(ProParserTokenTypes.TOTAL, "total", NodeTypesOption.KEYWORD),
  TRAILING(ProParserTokenTypes.TRAILING, "trailing", 5, NodeTypesOption.KEYWORD),
  TRANSACTION(ProParserTokenTypes.TRANSACTION, "trans", 5, "transaction", 8, NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED, NodeTypesOption.MAY_BE_NO_ARG_FUNC),
  TRANSACTIONMODE(ProParserTokenTypes.TRANSACTIONMODE, "transaction-mode", NodeTypesOption.KEYWORD),
  TRANSINITPROCEDURE(ProParserTokenTypes.TRANSINITPROCEDURE, "trans-init-procedure", NodeTypesOption.KEYWORD),
  TRANSPARENT(ProParserTokenTypes.TRANSPARENT, "transparent", 8, NodeTypesOption.KEYWORD),
  TRIGGER(ProParserTokenTypes.TRIGGER, "trigger", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TRIGGERS(ProParserTokenTypes.TRIGGERS, "triggers", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TRIM(ProParserTokenTypes.TRIM, "trim", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  TRUE(ProParserTokenTypes.TRUE_KW, "true", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  TRUNCATE(ProParserTokenTypes.TRUNCATE, "truncate", 5, NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  TTCODEPAGE(ProParserTokenTypes.TTCODEPAGE, "ttcodepage", NodeTypesOption.KEYWORD),
  TYPE_NAME(ProParserTokenTypes.TYPE_NAME, NodeTypesOption.STRUCTURE),
  TYPELESS_TOKEN(ProParserTokenTypes.TYPELESS_TOKEN, NodeTypesOption.STRUCTURE),
  TYPEOF(ProParserTokenTypes.TYPEOF, "type-of", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),

  // U
  UNARY_MINUS(ProParserTokenTypes.UNARY_MINUS, "-", NodeTypesOption.SYMBOL),
  UNARY_PLUS(ProParserTokenTypes.UNARY_PLUS, "+", NodeTypesOption.SYMBOL),
  UNBOX(ProParserTokenTypes.UNBOX, "unbox", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  UNBUFFERED(ProParserTokenTypes.UNBUFFERED, "unbuffered", 6, NodeTypesOption.KEYWORD),
  UNDERLINE(ProParserTokenTypes.UNDERLINE, "underline", 6, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  UNDO(ProParserTokenTypes.UNDO, "undo", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  UNFORMATTED(ProParserTokenTypes.UNFORMATTED, "unformatted", 6, NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  UNION(ProParserTokenTypes.UNION, "union", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  UNIQUE(ProParserTokenTypes.UNIQUE, "unique", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  UNIQUEMATCH(ProParserTokenTypes.UNIQUEMATCH, "unique-match", NodeTypesOption.KEYWORD),
  UNIX(ProParserTokenTypes.UNIX, "unix", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  UNKNOWNVALUE(ProParserTokenTypes.UNKNOWNVALUE, "?", NodeTypesOption.SYMBOL),
  UNLESSHIDDEN(ProParserTokenTypes.UNLESSHIDDEN, "unless-hidden", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  UNLOAD(ProParserTokenTypes.UNLOAD, "unload", NodeTypesOption.KEYWORD),
  UNQUOTEDSTRING(ProParserTokenTypes.UNQUOTEDSTRING),
  UNSIGNEDBYTE(ProParserTokenTypes.UNSIGNEDBYTE, "unsigned-byte", NodeTypesOption.KEYWORD),
  UNSIGNEDSHORT(ProParserTokenTypes.UNSIGNEDSHORT, "unsigned-short", NodeTypesOption.KEYWORD),
  UNSUBSCRIBE(ProParserTokenTypes.UNSUBSCRIBE, "unsubscribe", NodeTypesOption.KEYWORD),
  UP(ProParserTokenTypes.UP, "up", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  UPDATE(ProParserTokenTypes.UPDATE, "update", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  URLDECODE(ProParserTokenTypes.URLDECODE, "url-decode", NodeTypesOption.KEYWORD),
  URLENCODE(ProParserTokenTypes.URLENCODE, "url-encode", NodeTypesOption.KEYWORD),
  USE(ProParserTokenTypes.USE, "use", NodeTypesOption.KEYWORD),
  USEDICTEXPS(ProParserTokenTypes.USEDICTEXPS, "use-dict-exps", 7, NodeTypesOption.KEYWORD),
  USEFILENAME(ProParserTokenTypes.USEFILENAME, "use-filename", NodeTypesOption.KEYWORD),
  USEINDEX(ProParserTokenTypes.USEINDEX, "use-index", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  USER(ProParserTokenTypes.USER, "user", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_NO_ARG_FUNC,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  USEREVVIDEO(ProParserTokenTypes.USEREVVIDEO, "use-revvideo", NodeTypesOption.KEYWORD),
  USERID(ProParserTokenTypes.USERID, "userid", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED,
      NodeTypesOption.MAY_BE_NO_ARG_FUNC, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  USER_FUNC(ProParserTokenTypes.USER_FUNC, NodeTypesOption.STRUCTURE),
  USETEXT(ProParserTokenTypes.USETEXT, "use-text", NodeTypesOption.KEYWORD),
  USEUNDERLINE(ProParserTokenTypes.USEUNDERLINE, "use-underline", NodeTypesOption.KEYWORD),
  USEWIDGETPOOL(ProParserTokenTypes.USEWIDGETPOOL, "use-widget-pool", NodeTypesOption.KEYWORD),
  USING(ProParserTokenTypes.USING, "using", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),

  // V
  V6FRAME(ProParserTokenTypes.V6FRAME, "v6frame", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  VALIDATE(ProParserTokenTypes.VALIDATE, "validate", NodeTypesOption.KEYWORD),
  VALIDEVENT(ProParserTokenTypes.VALIDEVENT, "valid-event", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  VALIDHANDLE(ProParserTokenTypes.VALIDHANDLE, "valid-handle", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  VALIDOBJECT(ProParserTokenTypes.VALIDOBJECT, "valid-object", NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  VALUE(ProParserTokenTypes.VALUE, "value", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  VALUECHANGED(ProParserTokenTypes.VALUECHANGED, "value-changed", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  VALUES(ProParserTokenTypes.VALUES, "values", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  VARIABLE(ProParserTokenTypes.VARIABLE, "variable", 3, NodeTypesOption.KEYWORD),
  VERBOSE(ProParserTokenTypes.VERBOSE, "verbose", 4, NodeTypesOption.KEYWORD),
  VERTICAL(ProParserTokenTypes.VERTICAL, "vertical", 4, NodeTypesOption.KEYWORD),
  VIEW(ProParserTokenTypes.VIEW, "view", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  VIEWAS(ProParserTokenTypes.VIEWAS, "view-as", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  VMS(ProParserTokenTypes.VMS, "vms", NodeTypesOption.KEYWORD),
  VOID(ProParserTokenTypes.VOID, "void", NodeTypesOption.KEYWORD),

  // W
  WAIT(ProParserTokenTypes.WAIT, "wait", NodeTypesOption.KEYWORD),
  WAITFOR(ProParserTokenTypes.WAITFOR, "wait-for", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WARNING(ProParserTokenTypes.WARNING, "warning", NodeTypesOption.KEYWORD),
  WEBCONTEXT(ProParserTokenTypes.WEBCONTEXT, "web-context", 7, NodeTypesOption.KEYWORD, NodeTypesOption.SYSHDL),
  WEEKDAY(ProParserTokenTypes.WEEKDAY, "weekday", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  WHEN(ProParserTokenTypes.WHEN, "when", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WHERE(ProParserTokenTypes.WHERE, "where", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WHILE(ProParserTokenTypes.WHILE, "while", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WIDGET(ProParserTokenTypes.WIDGET, "widget", NodeTypesOption.KEYWORD),
  WIDGETHANDLE(ProParserTokenTypes.WIDGETHANDLE, "widget-handle", 8, NodeTypesOption.KEYWORD,
      NodeTypesOption.MAY_BE_REGULAR_FUNC),
  WIDGETID(ProParserTokenTypes.WIDGETID, "widget-id", NodeTypesOption.KEYWORD),
  WIDGETPOOL(ProParserTokenTypes.WIDGETPOOL, "widget-pool", NodeTypesOption.KEYWORD),
  WIDTH(ProParserTokenTypes.WIDTH, "width", NodeTypesOption.KEYWORD),
  WIDTHCHARS(ProParserTokenTypes.WIDTHCHARS, "width-chars", 7, NodeTypesOption.KEYWORD),
  WIDTHPIXELS(ProParserTokenTypes.WIDTHPIXELS, "width-pixels", 7, NodeTypesOption.KEYWORD),
  WINDOW(ProParserTokenTypes.WINDOW, "window", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WINDOWDELAYEDMINIMIZE(ProParserTokenTypes.WINDOWDELAYEDMINIMIZE, "window-delayed-minimize", 18,
      NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WINDOWMAXIMIZED(ProParserTokenTypes.WINDOWMAXIMIZED, "window-maximized", 12, NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  WINDOWMINIMIZED(ProParserTokenTypes.WINDOWMINIMIZED, "window-minimized", 12, NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  WINDOWNAME(ProParserTokenTypes.WINDOWNAME, "window-name", NodeTypesOption.KEYWORD),
  WINDOWNORMAL(ProParserTokenTypes.WINDOWNORMAL, "window-normal", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WITH(ProParserTokenTypes.WITH, "with", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WORDINDEX(ProParserTokenTypes.WORDINDEX, "word-index", NodeTypesOption.KEYWORD),
  WORKTABLE(ProParserTokenTypes.WORKTABLE, "work-table", 8, "workfile", NodeTypesOption.KEYWORD,
      NodeTypesOption.RESERVED),
  WRITE(ProParserTokenTypes.WRITE, "write", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  WS(ProParserTokenTypes.WS),
  WIDGET_REF(ProParserTokenTypes.Widget_ref, NodeTypesOption.STRUCTURE),
  WITH_COLUMNS(ProParserTokenTypes.With_columns, NodeTypesOption.STRUCTURE),
  WITH_DOWN(ProParserTokenTypes.With_down, NodeTypesOption.STRUCTURE),

  // X
  X(ProParserTokenTypes.X, "x", NodeTypesOption.KEYWORD),
  XCODE(ProParserTokenTypes.XCODE, "xcode", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  XDOCUMENT(ProParserTokenTypes.XDOCUMENT, "x-document", NodeTypesOption.KEYWORD),
  XMLDATATYPE(ProParserTokenTypes.XMLDATATYPE, "xml-data-type", NodeTypesOption.KEYWORD),
  XMLNODENAME(ProParserTokenTypes.XMLNODENAME, "xml-node-name", NodeTypesOption.KEYWORD),
  XMLNODETYPE(ProParserTokenTypes.XMLNODETYPE, "xml-node-type", NodeTypesOption.KEYWORD),
  XNODEREF(ProParserTokenTypes.XNODEREF, "x-noderef", NodeTypesOption.KEYWORD),
  XOF(ProParserTokenTypes.XOF, "x-of", NodeTypesOption.KEYWORD),
  XREF(ProParserTokenTypes.XREF, "xref", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  XREFXML(ProParserTokenTypes.XREFXML, "xref-xml", NodeTypesOption.KEYWORD),

  // Y
  Y(ProParserTokenTypes.Y, "y", NodeTypesOption.KEYWORD),
  YEAR(ProParserTokenTypes.YEAR, "year", NodeTypesOption.KEYWORD, NodeTypesOption.MAY_BE_REGULAR_FUNC),
  YES(ProParserTokenTypes.YES, "yes", NodeTypesOption.KEYWORD, NodeTypesOption.RESERVED),
  YESNO(ProParserTokenTypes.YESNO, "yes-no", NodeTypesOption.KEYWORD),
  YESNOCANCEL(ProParserTokenTypes.YESNOCANCEL, "yes-no-cancel", NodeTypesOption.KEYWORD),
  YOF(ProParserTokenTypes.YOF, "y-of", NodeTypesOption.KEYWORD);

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
