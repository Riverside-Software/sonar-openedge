package eu.rssw.antlr.database.objects;

public class IndexField {
  private final Field field;
  private final boolean ascending;

  public IndexField(Field field, boolean ascending) {
    this.field = field;
    this.ascending = ascending;
  }

  public Field getField() {
    return field;
  }

  public boolean isAscending() {
    return ascending;
  }
}