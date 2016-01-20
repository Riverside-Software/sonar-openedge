package eu.rssw.antlr.database;

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

  // *************
  // FIELD SECTION
  // *************

  @Override
  public Void visitAddField(AddFieldContext ctx) {
    System.out.printf("a FIELD %1$s.%2$s [%3$s]%n", ctx.table.getText(), ctx.field.getText(), ctx.dataType.getText());

    return visitChildren(ctx);
  }

  @Override
  public Void visitDropField(DropFieldContext ctx) {
    System.out.printf("d FIELD %1$s.%2$s%n", ctx.table.getText(), ctx.field.getText());

    return visitChildren(ctx);
  }

  @Override
  public Void visitRenameField(RenameFieldContext ctx) {
    System.out.printf("r FIELD %1$s.%2$s --> %3$s%n", ctx.table.getText(), ctx.from.getText(), ctx.to.getText());

    return visitChildren(ctx);
  }

  @Override
  public Void visitUpdateField(UpdateFieldContext ctx) {
    System.out.printf("m FIELD %1$s.%2$s%n", ctx.table.getText(), ctx.field.getText());

    return visitChildren(ctx);
  }

  // *************
  // TABLE SECTION
  // *************

  @Override
  public Void visitAddTable(AddTableContext ctx) {
    System.out.printf("a TABLE %1$s%n", ctx.table.getText());

    return visitChildren(ctx);
  }

  @Override
  public Void visitDropTable(DropTableContext ctx) {
    System.out.printf("d TABLE %1$s%n", ctx.table.getText());

    return visitChildren(ctx);
  }

  @Override
  public Void visitUpdateTable(UpdateTableContext ctx) {
    System.out.printf("m TABLE %1$s%n", ctx.table.getText());

    return visitChildren(ctx);
  }

  // *************
  // INDEX SECTION
  // *************

  @Override
  public Void visitAddIndex(AddIndexContext ctx) {
    System.out.printf("a INDEX %1$s.%2$s%n", ctx.table.getText(), ctx.index.getText());

    return visitChildren(ctx);
  }

  @Override
  public Void visitDropIndex(DropIndexContext ctx) {
    System.out.printf("d INDEX %1$s.%2$s%n", ctx.table.getText(), ctx.index.getText());

    return visitChildren(ctx);
  }

  @Override
  public Void visitUpdateIndex(UpdateIndexContext ctx) {
    System.out.printf("m INDEX %1$s.%2$s%n", ctx.table.getText(), ctx.index.getText());

    return visitChildren(ctx);
  }

  // ****************
  // SEQUENCE SECTION
  // ****************

  @Override
  public Void visitAddSequence(AddSequenceContext ctx) {
    System.out.printf("a SEQUENCE %1$s%n", ctx.sequence.getText());

    return visitChildren(ctx);
  }

}
