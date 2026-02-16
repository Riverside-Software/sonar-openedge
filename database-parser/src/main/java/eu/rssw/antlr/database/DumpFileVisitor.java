/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2026 Riverside Software
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
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.rssw.antlr.database.DumpFileGrammarParser.AddFieldContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.AddIndexContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.AddSequenceContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.AddTableContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.FieldColumnLabelContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.FieldDescriptionContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.FieldExtentContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.FieldFormatContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.FieldInitialContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.FieldLabelContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.FieldLobAreaContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.FieldMaxWidthContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.FieldOrderContext;
import eu.rssw.antlr.database.DumpFileGrammarParser.FieldPositionContext;
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
import eu.rssw.antlr.database.DumpFileGrammarParser.TableFrozenContext;
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

  private final String dbName;

  private Map<String, Table.Builder> tableBuilders = new LinkedHashMap<>();
  private Deque<Table.Builder> tables = new ArrayDeque<>();
  private Deque<Field.Builder> fields = new ArrayDeque<>();
  private Deque<Sequence.Builder> sequences = new ArrayDeque<>();
  private Deque<Index.Builder> indexes = new ArrayDeque<>();

  public DumpFileVisitor(String dbName) {
    this.dbName = dbName;
  }

  public DatabaseDescription getDatabase() {
    DatabaseDescription db = new DatabaseDescription(dbName);
    for (Sequence.Builder seqBuilder : sequences) {
      db.addSequence(seqBuilder.build());
    }
    for (Table.Builder tblBuilder : tableBuilders.values()) {
      db.addTable(tblBuilder.build());
    }
    return db;
  }

  // *************
  // FIELD SECTION
  // *************

  @Override
  public Void visitAddField(AddFieldContext ctx) {
    Field.Builder fieldBuilder = new Field.Builder(ctx.field.getText(), ctx.dataType.getText())
        .setFirstLine(ctx.getStart().getLine())
        .setLastLine(ctx.getStop().getLine());
    fields.push(fieldBuilder);

    // Visit children to populate field properties
    visitChildren(ctx);

    // Build the field and add to parent table
    Field field = fields.pop().build();
    for (Table.Builder t : tables) {
      if (t.getName().equalsIgnoreCase(ctx.table.getText())) {
        t.addField(field);
        break;
      }
    }

    return null;
  }

  @Override
  public Void visitFieldDescription(FieldDescriptionContext ctx) {
    if (!fields.isEmpty())
      fields.peek().setDescription(ctx.val.getText());

    return null;
  }

  @Override
  public Void visitFieldLabel(FieldLabelContext ctx) {
    if (!fields.isEmpty() && (ctx.QUOTED_STRING() != null))
      fields.peek().setLabel(ctx.QUOTED_STRING().getText());

    return null;
  }

  @Override
  public Void visitFieldColumnLabel(FieldColumnLabelContext ctx) {
    if (!fields.isEmpty() && (ctx.QUOTED_STRING() != null))
      fields.peek().setColumnLabel(ctx.QUOTED_STRING().getText());

    return null;
  }

  @Override
  public Void visitFieldMandatory(DumpFileGrammarParser.FieldMandatoryContext ctx) {
    if (!fields.isEmpty() && ctx != null)
      fields.peek().setMandatory(!ctx.isEmpty());

    return null;
  }

  @Override
  public Void visitFieldInitial(FieldInitialContext ctx) {
    if (!fields.isEmpty()) {
      if (ctx.QUOTED_STRING() != null)
        fields.peek().setInitial(ctx.QUOTED_STRING().getText());
      else if (ctx.UNQUOTED_STRING() != null)
        fields.peek().setInitial(ctx.UNQUOTED_STRING().getText());
    }

    return null;
  }

  @Override
  public Void visitFieldPosition(FieldPositionContext ctx) {
    if (!fields.isEmpty()) {
      try {
        fields.peek().setPosition(Integer.parseInt(ctx.val.getText()));
      } catch (Exception e) {
        // Nothing
      }
    }

    return null;
  }

  @Override
  public Void visitFieldExtent(FieldExtentContext ctx) {
    if (!fields.isEmpty())
      fields.peek().setExtent(Integer.parseInt(ctx.val.getText()));

    return null;
  }

  @Override
  public Void visitFieldFormat(FieldFormatContext ctx) {
    if (!fields.isEmpty())
      fields.peek().setFormat(ctx.val.getText());

    return super.visitFieldFormat(ctx);
  }

  @Override
  public Void visitFieldMaxWidth(FieldMaxWidthContext ctx) {
    if (!fields.isEmpty())
      fields.peek().setMaxWidth(Integer.parseInt(ctx.val.getText()));

    return null;
  }

  @Override
  public Void visitFieldOrder(FieldOrderContext ctx) {
    if (!fields.isEmpty())
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
    Trigger.Builder triggerBuilder = new Trigger.Builder(TriggerType.ASSIGN, ctx.triggerProcedure.getText());
    if (ctx.crc != null) {
      triggerBuilder.setCrc(ctx.crc.getText());
    }
    if (ctx.noOverride != null)
      triggerBuilder.setNoOverride(true);
    fields.peek().addTrigger(triggerBuilder.build());

    return null;
  }

  @Override
  public Void visitFieldLobArea(FieldLobAreaContext ctx) {
    if (!fields.isEmpty())
      fields.peek().setLobArea(ctx.val.getText());

    return null;
  }

  // *************
  // TABLE SECTION
  // *************

  @Override
  public Void visitAddTable(AddTableContext ctx) {
    Table.Builder tableBuilder = new Table.Builder(ctx.table.getText())
        .setFirstLine(ctx.getStart().getLine())
        .setLastLine(ctx.getStop().getLine());
    tables.push(tableBuilder);
    tableBuilders.put(ctx.table.getText(), tableBuilder);

    return visitChildren(ctx);
  }

  @Override
  public Void visitTableArea(TableAreaContext ctx) {
    if (!tables.isEmpty())
      tables.peek().setArea(ctx.val.getText());

    return null;
  }

  @Override
  public Void visitTableDescription(TableDescriptionContext ctx) {
    if (!tables.isEmpty())
      tables.peek().setDescription(ctx.val.getText());

    return null;
  }

  @Override
  public Void visitTableDumpName(TableDumpNameContext ctx) {
    if (!tables.isEmpty())
      tables.peek().setDumpName(ctx.val.getText());

    return null;
  }

  @Override
  public Void visitTableValMsg(TableValMsgContext ctx) {
    if (!tables.isEmpty())
      tables.peek().setValMsg(ctx.val.getText());

    return null;
  }

  @Override
  public Void visitTableTrigger(TableTriggerContext ctx) {
    if (tables.isEmpty())
      return null;
    Trigger.Builder triggerBuilder = new Trigger.Builder(TriggerType.getTriggerType(ctx.type.getText()), ctx.triggerProcedure.getText());
    if (ctx.crc != null) {
      triggerBuilder.setCrc(ctx.crc.getText());
    }
    if (ctx.noOverride != null)
      triggerBuilder.setNoOverride(true);
    tables.peek().addTrigger(triggerBuilder.build());

    return null;
  }

  @Override
  public Void visitTableFrozen(TableFrozenContext ctx) {
    if (!tables.isEmpty())
      tables.peek().setFrozen(true);

    return null;
  }

  // *************
  // INDEX SECTION
  // *************

  @Override
  public Void visitAddIndex(AddIndexContext ctx) {
    Index.Builder indexBuilder = new Index.Builder(ctx.index.getText())
        .setFirstLine(ctx.getStart().getLine())
        .setLastLine(ctx.getStop().getLine());
    indexes.push(indexBuilder);

    if (ctx.uniq != null)
      indexBuilder.setUnique(true);

    // Visit children to populate index properties
    visitChildren(ctx);

    // Build the index and add to parent table
    Index index = indexes.pop().build();
    for (Table.Builder t : tables) {
      if (t.getName().equalsIgnoreCase(ctx.table.getText())) {
        t.addIndex(index);
        break;
      }
    }

    return null;
  }

  @Override
  public Void visitIndexArea(IndexAreaContext ctx) {
    if (!indexes.isEmpty())
      indexes.peek().setArea(ctx.val.getText());

    return null;
  }

  @Override
  public Void visitIndexUnique(IndexUniqueContext ctx) {
    if (!indexes.isEmpty())
      indexes.peek().setUnique(true);

    return null;
  }

  @Override
  public Void visitIndexPrimary(IndexPrimaryContext ctx) {
    if (!indexes.isEmpty())
      indexes.peek().setPrimary(true);

    return null;
  }

  @Override
  public Void visitIndexWord(IndexWordContext ctx) {
    if (!indexes.isEmpty())
      indexes.peek().setWord(true);

    return null;
  }

  @Override
  public Void visitIndexField(IndexFieldContext ctx) {
    if (indexes.isEmpty())
      return null;
    // Search for Table builder for this index
    String tableName = ((AddIndexContext) ctx.parent).table.getText();
    Table.Builder tableBuilder = null;
    for (Table.Builder t : tables) {
      if (t.getName().equalsIgnoreCase(tableName))
        tableBuilder = t;
    }
    if (tableBuilder != null) {
      IndexField idxFld = new IndexField(tableBuilder.getField(ctx.field.getText()),
          ((ctx.order == null) || "ascending".equalsIgnoreCase(ctx.order.getText())));
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
    Table.Builder tableBuilder = tableBuilders.get(tableName);
    if (tableBuilder != null) {
      tableBuilder.updateIndexBufferPool(indexName, ctx.value.getText());
    }

    return null;
  }

  // ****************
  // SEQUENCE SECTION
  // ****************

  @Override
  public Void visitAddSequence(AddSequenceContext ctx) {
    Sequence.Builder seqBuilder = new Sequence.Builder(ctx.sequence.getText())
        .setFirstLine(ctx.getStart().getLine())
        .setLastLine(ctx.getStop().getLine());
    sequences.push(seqBuilder);

    return visitChildren(ctx);
  }

  @Override
  public Void visitSeqCycleOnLimit(SeqCycleOnLimitContext ctx) {
    if (!sequences.isEmpty())
      sequences.peek().setCycleOnLimit("yes".equalsIgnoreCase(ctx.val.getText()));

    return null;
  }

  @Override
  public Void visitSeqIncrement(SeqIncrementContext ctx) {
    if (!sequences.isEmpty() && !"?".equals(ctx.val.getText()))
      sequences.peek().setIncrement(Long.parseLong(ctx.val.getText()));

    return null;
  }

  @Override
  public Void visitSeqInitial(SeqInitialContext ctx) {
    if (!sequences.isEmpty() && !"?".equals(ctx.val.getText()))
      sequences.peek().setInitialValue(Long.parseLong(ctx.val.getText()));

    return null;
  }

  @Override
  public Void visitSeqMinVal(SeqMinValContext ctx) {
    if (!sequences.isEmpty() && !"?".equals(ctx.val.getText()))
      sequences.peek().setMinValue(Long.parseLong(ctx.val.getText()));

    return null;
  }

  @Override
  public Void visitSeqMaxVal(SeqMaxValContext ctx) {
    if (!sequences.isEmpty() && !"?".equals(ctx.val.getText()))
      sequences.peek().setMaxValue(Long.parseLong(ctx.val.getText()));

    return null;
  }
}
