/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2018 Riverside Software
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

import java.util.ArrayDeque;
import java.util.Deque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.rssw.antlr.database.DumpFileGrammarParser.AddFieldContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.AddIndexContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.AddSequenceContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.AddTableContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.FieldDescriptionContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.FieldExtentContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.FieldFormatContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.FieldMaxWidthContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.FieldOrderContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.FieldTriggerContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.IndexAreaContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.IndexFieldContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.IndexPrimaryContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.IndexUniqueContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.IndexWordContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.SeqCycleOnLimitContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.SeqIncrementContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.SeqInitialContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.SeqMaxValContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.SeqMinValContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.TableAreaContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.TableDescriptionContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.TableDumpNameContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.TableTriggerContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.TableValMsgContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.UpdateIndexBPContext;
import eu.rssw.antlr.database.objects.DatabaseDescription;
import eu.rssw.antlr.database.objects.Field;
import eu.rssw.antlr.database.objects.Index;
import eu.rssw.antlr.database.objects.IndexField;
import eu.rssw.antlr.database.objects.Sequence;
import eu.rssw.antlr.database.objects.Table;
import eu.rssw.antlr.database.objects.Trigger;
import eu.rssw.antlr.database.objects.TriggerType;

public class DumpFileVisitor extends DumpFileGrammarBaseVisitor<Void> {
  private static final Logger LOG = LoggerFactory.getLogger(DumpFileVisitor.class);

  private DatabaseDescription db;

  private Deque<Table> tables = new ArrayDeque<>();
  private Deque<Field> fields = new ArrayDeque<>();
  private Deque<Sequence> sequences = new ArrayDeque<>();
  private Deque<Index> indexes = new ArrayDeque<>();

  public DumpFileVisitor(String dbName) {
    this.db = new DatabaseDescription(dbName);
  }

  public DatabaseDescription getDatabase() {
    return db;
  }

  // *************
  // FIELD SECTION
  // *************

  @Override
  public Void visitAddField(AddFieldContext ctx) {
    Field field = new Field(ctx.field.getText(), ctx.dataType.getText());
    field.setFirstLine(ctx.getStart().getLine());
    field.setLastLine(ctx.getStop().getLine());
    fields.push(field);

    // Search for Table object for this field
    Table table = null;
    for (Table t : tables) {
      if (t.getName().equalsIgnoreCase(ctx.table.getText()))
        table = t;
    }
    if (table != null) {
      table.addField(field);
    } else {
      // Log error 
    }

    return visitChildren(ctx);
  }

  @Override
  public Void visitFieldDescription(FieldDescriptionContext ctx) {
    if (fields.isEmpty())
      return null;
    fields.peek().setDescription(ctx.val.getText());

    return null;
  }

  @Override
  public Void visitFieldExtent(FieldExtentContext ctx) {
    if (fields.isEmpty())
      return null;
    fields.peek().setExtent(Integer.parseInt(ctx.val.getText()));

    return null;
  }

  @Override
  public Void visitFieldFormat(FieldFormatContext ctx) {
    if (fields.isEmpty())
      return null;
    fields.peek().setFormat(ctx.val.getText());

    return super.visitFieldFormat(ctx);
  }

  @Override
  public Void visitFieldMaxWidth(FieldMaxWidthContext ctx) {
    if (fields.isEmpty())
      return null;
    fields.peek().setMaxWidth(Integer.parseInt(ctx.val.getText()));

    return null;
  }

  @Override
  public Void visitFieldOrder(FieldOrderContext ctx) {
    if (fields.isEmpty())
      return null;
    fields.peek().setOrder(Integer.parseInt(ctx.val.getText()));

    return null;
  }

  @Override
  public Void visitFieldTrigger(FieldTriggerContext ctx) {
    if (fields.isEmpty())
      return null;
    if (TriggerType.getTriggerType(ctx.type.getText()) != TriggerType.ASSIGN) {
      // Value can only be 'ASSIGN', but we just log the problem and return in case of different value
      LOG.error("'{}' FIELD-TRIGGER found at line {}", ctx.type.getText(), ctx.type.getLine());
      return null;
    }
    Trigger trigger = new Trigger(TriggerType.ASSIGN, ctx.triggerProcedure.getText());
    if (ctx.crc != null) {
      trigger.setCrc(ctx.crc.getText());
    }
    if (ctx.noOverride != null)
      trigger.setNoOverride(true);
    fields.peek().addTrigger(trigger);

    return null;
  }

  // *************
  // TABLE SECTION
  // *************

  @Override
  public Void visitAddTable(AddTableContext ctx) {
    Table table = new Table(ctx.table.getText());
    table.setFirstLine(ctx.getStart().getLine());
    table.setLastLine(ctx.getStop().getLine());
    tables.push(table);
    db.addTable(table);

    return visitChildren(ctx);
  }

  @Override
  public Void visitTableArea(TableAreaContext ctx) {
    if (tables.isEmpty())
      return null;
    tables.peek().setArea(ctx.val.getText());

    return null;
  }

  @Override
  public Void visitTableDescription(TableDescriptionContext ctx) {
    if (tables.isEmpty())
      return null;
    tables.peek().setDescription(ctx.val.getText());

    return null;
  }

  @Override
  public Void visitTableDumpName(TableDumpNameContext ctx) {
    if (tables.isEmpty())
      return null;
    tables.peek().setDumpName(ctx.val.getText());
    
    return null;
  }

  @Override
  public Void visitTableValMsg(TableValMsgContext ctx) {
    if (tables.isEmpty())
      return null;
    tables.peek().setValMsg(ctx.val.getText());

    return null;
  }

  @Override
  public Void visitTableTrigger(TableTriggerContext ctx) {
    if (tables.isEmpty())
      return null;
    Trigger trigger = new Trigger(TriggerType.getTriggerType(ctx.type.getText()), ctx.triggerProcedure.getText());
    if (ctx.crc != null) {
      trigger.setCrc(ctx.crc.getText());
    }
    if (ctx.noOverride != null)
      trigger.setNoOverride(true);
    tables.peek().addTrigger(trigger);

    return null;
  }

  // *************
  // INDEX SECTION
  // *************

  @Override
  public Void visitAddIndex(AddIndexContext ctx) {
    Index index = new Index(ctx.index.getText());
    index.setFirstLine(ctx.getStart().getLine());
    index.setLastLine(ctx.getStop().getLine());
    indexes.push(index);

    // Search for Table object for this field
    Table table = null;
    for (Table t : tables) {
      if (t.getName().equalsIgnoreCase(ctx.table.getText()))
        table = t;
    }
    if (table != null) {
      table.addIndex(index);
    } else {
      // Log error ?
    }

    return visitChildren(ctx);
  }

  @Override
  public Void visitIndexArea(IndexAreaContext ctx) {
    if (indexes.isEmpty())
      return null;
    indexes.peek().setArea(ctx.val.getText());
    return null;
  }

  @Override
  public Void visitIndexUnique(IndexUniqueContext ctx) {
    if (indexes.isEmpty())
      return null;
    indexes.peek().setUnique(true);
    return null;
  }

  @Override
  public Void visitIndexPrimary(IndexPrimaryContext ctx) {
    if (indexes.isEmpty())
      return null;
    indexes.peek().setPrimary(true);
    return null;
  }

  @Override
  public Void visitIndexWord(IndexWordContext ctx) {
    if (indexes.isEmpty())
      return null;
    indexes.peek().setWord(true);
    return null;
  }

  @Override
  public Void visitIndexField(IndexFieldContext ctx) {
    if (indexes.isEmpty())
      return null;
    // Search for Table object for this index
    String tableName = ((AddIndexContext) ctx.parent).table.getText();
    Table table = null;
    for (Table t : tables) {
      if (t.getName().equalsIgnoreCase(tableName))
        table = t;
    }
    if (table != null) {
      IndexField idxFld = new IndexField(table.getField(ctx.field.getText()),
          "ascending".equalsIgnoreCase(ctx.order.getText()));
      indexes.peek().addField(idxFld);
    } else {
      // Log error ?
    }

    return null;
  }

  @Override
  public Void visitUpdateIndexBP(UpdateIndexBPContext ctx) {
    String tableName = ctx.table.getText();
    String indexName = ctx.index.getText();
    Table table = db.getTable(tableName);
    if (table != null) {
      Index index = table.getIndex(indexName);
      if (index != null) {
        index.setBufferPool(ctx.value.getText());
      }
    }

    return null;
  }

  // ****************
  // SEQUENCE SECTION
  // ****************

  @Override
  public Void visitAddSequence(AddSequenceContext ctx) {
    Sequence seq = new Sequence(ctx.sequence.getText());
    seq.setFirstLine(ctx.getStart().getLine());
    seq.setLastLine(ctx.getStop().getLine());
    sequences.push(seq);
    db.addSequence(seq);

    return visitChildren(ctx);
  }

  @Override
  public Void visitSeqCycleOnLimit(SeqCycleOnLimitContext ctx) {
    if (sequences.isEmpty())
      return null;
    sequences.peek().setCycleOnLimit("yes".equalsIgnoreCase(ctx.val.getText()));

    return null;
  }

  @Override
  public Void visitSeqIncrement(SeqIncrementContext ctx) {
    if (sequences.isEmpty())
      return null;
    if (!"?".equals(ctx.val.getText()))
      sequences.peek().setIncrement(Long.parseLong(ctx.val.getText()));

    return null;
  }

  @Override
  public Void visitSeqInitial(SeqInitialContext ctx) {
    if (sequences.isEmpty())
      return null;
    if (!"?".equals(ctx.val.getText()))
      sequences.peek().setInitialValue(Long.parseLong(ctx.val.getText()));

    return null;
  }

  @Override
  public Void visitSeqMinVal(SeqMinValContext ctx) {
    if (sequences.isEmpty())
      return null;
    if (!"?".equals(ctx.val.getText()))
      sequences.peek().setMinValue(Long.parseLong(ctx.val.getText()));

    return null;
  }

  @Override
  public Void visitSeqMaxVal(SeqMaxValContext ctx) {
    if (sequences.isEmpty())
      return null;
    if (!"?".equals(ctx.val.getText()))
      sequences.peek().setMaxValue(Long.parseLong(ctx.val.getText()));

    return null;
  }
}
