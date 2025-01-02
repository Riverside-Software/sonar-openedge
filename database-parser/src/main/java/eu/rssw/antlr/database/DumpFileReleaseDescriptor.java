/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2025 Riverside Software
 * contact AT riverside DASH software DOT fr
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package eu.rssw.antlr.database;

import java.io.PrintStream;

import eu.rssw.antlr.database.DumpFileGrammarParser.AddFieldContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.AddIndexContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.AddSequenceContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.AddTableContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.DropFieldContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.DropIndexContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.DropTableContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.RenameFieldContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.UpdateFieldContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.UpdateIndexContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.UpdateTableContext;

public class DumpFileReleaseDescriptor extends DumpFileGrammarBaseVisitor<Void> {
  private final PrintStream out;

  public DumpFileReleaseDescriptor(PrintStream stream) {
    this.out = stream;
  }

  // *************
  // FIELD SECTION
  // *************

  @Override
  public Void visitAddField(AddFieldContext ctx) {
    out.printf("a FIELD %1$s.%2$s [%3$s]%n", ctx.table.getText(), ctx.field.getText(), ctx.dataType.getText());

    return visitChildren(ctx);
  }

  @Override
  public Void visitDropField(DropFieldContext ctx) {
    out.printf("d FIELD %1$s.%2$s%n", ctx.table.getText(), ctx.field.getText());

    return visitChildren(ctx);
  }

  @Override
  public Void visitRenameField(RenameFieldContext ctx) {
    out.printf("r FIELD %1$s.%2$s --> %3$s%n", ctx.table.getText(), ctx.from.getText(), ctx.to.getText());

    return visitChildren(ctx);
  }

  @Override
  public Void visitUpdateField(UpdateFieldContext ctx) {
    out.printf("m FIELD %1$s.%2$s%n", ctx.table.getText(), ctx.field.getText());

    return visitChildren(ctx);
  }

  // *************
  // TABLE SECTION
  // *************

  @Override
  public Void visitAddTable(AddTableContext ctx) {
    out.printf("a TABLE %1$s%n", ctx.table.getText());

    return visitChildren(ctx);
  }

  @Override
  public Void visitDropTable(DropTableContext ctx) {
    out.printf("d TABLE %1$s%n", ctx.table.getText());

    return visitChildren(ctx);
  }

  @Override
  public Void visitUpdateTable(UpdateTableContext ctx) {
    out.printf("m TABLE %1$s%n", ctx.table.getText());

    return visitChildren(ctx);
  }

  // *************
  // INDEX SECTION
  // *************

  @Override
  public Void visitAddIndex(AddIndexContext ctx) {
    out.printf("a INDEX %1$s.%2$s%n", ctx.table.getText(), ctx.index.getText());

    return visitChildren(ctx);
  }

  @Override
  public Void visitDropIndex(DropIndexContext ctx) {
    out.printf("d INDEX %1$s.%2$s%n", ctx.table.getText(), ctx.index.getText());

    return visitChildren(ctx);
  }

  @Override
  public Void visitUpdateIndex(UpdateIndexContext ctx) {
    out.printf("m INDEX %1$s.%2$s%n", ctx.table.getText(), ctx.index.getText());

    return visitChildren(ctx);
  }

  // ****************
  // SEQUENCE SECTION
  // ****************

  @Override
  public Void visitAddSequence(AddSequenceContext ctx) {
    out.printf("a SEQUENCE %1$s%n", ctx.sequence.getText());

    return visitChildren(ctx);
  }

}
