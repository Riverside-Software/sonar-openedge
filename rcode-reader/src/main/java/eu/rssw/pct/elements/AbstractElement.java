package eu.rssw.pct.elements;

public abstract class AbstractElement {
  protected String name;

  public AbstractElement() {
    this("<noname>");
  }

  public AbstractElement(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public abstract int size();
}
