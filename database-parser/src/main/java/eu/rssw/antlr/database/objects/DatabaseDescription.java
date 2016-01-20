package eu.rssw.antlr.database.objects;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DatabaseDescription {
  private String dbName;
  private Map<String, Sequence> sequences = new HashMap<String, Sequence>();
  private Map<String, Table> tables = new HashMap<String, Table>();

  public DatabaseDescription(String dbName) {
    this.dbName = dbName;
  }

  public String getDbName() {
    return dbName;
  }

  public Collection<Sequence> getSequences() {
    return sequences.values();
  }

  public Sequence getSequence(String name) {
    return sequences.get(name);
  }

  public Collection<Table> getTables() {
    return tables.values();
  }

  public Table getTable(String name) {
    return tables.get(name);
  }

  public void addTable(Table tbl) {
    tables.put(tbl.getName(), tbl);
  }

  public void addSequence(Sequence seq) {
    sequences.put(seq.getName(), seq);
  }
}
