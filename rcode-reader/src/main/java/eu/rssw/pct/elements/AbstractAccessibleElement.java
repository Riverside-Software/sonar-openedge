package eu.rssw.pct.elements;

import java.util.Set;

import eu.rssw.pct.AccessType;

public abstract class AbstractAccessibleElement extends AbstractElement {
  protected Set<AccessType> accessType;

  public AbstractAccessibleElement(String name) {
    this(name, null);
  }

  public AbstractAccessibleElement(String name, Set<AccessType> accessType) {
    super(name);
    this.accessType = accessType;
  }

  public boolean isProtected() {
    return (this.accessType != null) && this.accessType.contains(AccessType.PROTECTED);
  }

  public boolean isPublic() {
    return (this.accessType != null) && this.accessType.contains(AccessType.PUBLIC);
  }

  public boolean isPrivate() {
    return (this.accessType != null) && this.accessType.contains(AccessType.PRIVATE);
  }

  public boolean isAbstract() {
    return (this.accessType != null) && this.accessType.contains(AccessType.ABSTRACT);
  }

  public boolean isStatic() {
    return (this.accessType != null) && this.accessType.contains(AccessType.STATIC);
  }
}
