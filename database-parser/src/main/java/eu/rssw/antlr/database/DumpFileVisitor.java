package eu.rssw.antlr.database;

import java.util.ArrayDeque;
import java.util.Deque;

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
import eu.rssw.antlr.database.objects.DatabaseDescription;
import eu.rssw.antlr.database.objects.Field;
import eu.rssw.antlr.database.objects.Index;
import eu.rssw.antlr.database.objects.IndexField;
import eu.rssw.antlr.database.objects.Sequence;
import eu.rssw.antlr.database.objects.Table;
import eu.rssw.antlr.database.objects.Trigger;
import eu.rssw.antlr.database.objects.TriggerType;

public class DumpFileVisitor extends DumpFileGrammarBaseVisitor<Void> {
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
    fields.peek().setDescription(ctx.val.getText());

    return null;
  }

  @Override
  public Void visitFieldExtent(FieldExtentContext ctx) {
    fields.peek().setExtent(Integer.parseInt(ctx.val.getText()));

    return null;
  }

  @Override
  public Void visitFieldFormat(FieldFormatContext ctx) {
    fields.peek().setFormat(ctx.val.getText());

    return super.visitFieldFormat(ctx);
  }

  @Override
  public Void visitFieldMaxWidth(FieldMaxWidthContext ctx) {
    fields.peek().setMaxWidth(Integer.parseInt(ctx.val.getText()));

    return null;
  }

  @Override
  public Void visitFieldOrder(FieldOrderContext ctx) {
    fields.peek().setOrder(ctx.val.getText());

    return null;
  }

  @Override
  public Void visitFieldTrigger(FieldTriggerContext ctx) {
    if (TriggerType.getTriggerType(ctx.type.getText()) != TriggerType.ASSIGN) {
      // Value can only be 'ASSIGN'. Any other value should probably be logged 
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
    tables.peek().setArea(ctx.val.getText());

    return null;
  }

  @Override
  public Void visitTableDescription(TableDescriptionContext ctx) {
    tables.peek().setDescription(ctx.val.getText());

    return null;
  }

  @Override
  public Void visitTableDumpName(TableDumpNameContext ctx) {
    tables.peek().setDumpName(ctx.val.getText());
    
    return null;
  }

  @Override
  public Void visitTableValMsg(TableValMsgContext ctx) {
    tables.peek().setValMsg(ctx.val.getText());

    return null;
  }

  @Override
  public Void visitTableTrigger(TableTriggerContext ctx) {
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
    indexes.peek().setArea(ctx.val.getText());
    return null;
  }

  @Override
  public Void visitIndexUnique(IndexUniqueContext ctx) {
    indexes.peek().setUnique(true);
    return null;
  }

  @Override
  public Void visitIndexPrimary(IndexPrimaryContext ctx) {
    indexes.peek().setPrimary(true);
    return null;
  }

  @Override
  public Void visitIndexWord(IndexWordContext ctx) {
    indexes.peek().setWord(true);
    return null;
  }

  @Override
  public Void visitIndexField(IndexFieldContext ctx) {
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
    sequences.peek().setCycleOnLimit("yes".equalsIgnoreCase(ctx.val.getText()));

    return null;
  }

  @Override
  public Void visitSeqIncrement(SeqIncrementContext ctx) {
    if (!"?".equals(ctx.val.getText()))
      sequences.peek().setIncrement(Long.parseLong(ctx.val.getText()));

    return null;
  }

  @Override
  public Void visitSeqInitial(SeqInitialContext ctx) {
    if (!"?".equals(ctx.val.getText()))
      sequences.peek().setInitialValue(Long.parseLong(ctx.val.getText()));

    return null;
  }

  @Override
  public Void visitSeqMinVal(SeqMinValContext ctx) {
    if (!"?".equals(ctx.val.getText()))
      sequences.peek().setMinValue(Long.parseLong(ctx.val.getText()));

    return null;
  }

  @Override
  public Void visitSeqMaxVal(SeqMaxValContext ctx) {
    if (!"?".equals(ctx.val.getText()))
      sequences.peek().setMaxValue(Long.parseLong(ctx.val.getText()));

    return null;
  }
}
