package eu.rssw.pct;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;

import eu.rssw.pct.elements.MethodElement;
import eu.rssw.pct.elements.MethodParameter;

public final class ProgressClasses {
  private static final IParameter[] EMPTY_PARAMETERS = new MethodParameter[] {};
  private static final String PROGRESS_LANG_OBJECT = "Progress.Lang.Object";

  private ProgressClasses() {
    // No-op
  }

  public static final Collection<TypeInfo> getProgressClasses() {
    Collection<TypeInfo> coll = new ArrayList<>();
    coll.add(getProgressLangObject());

    return coll;
  }

  private static final TypeInfo getProgressLangObject() {
    TypeInfo info = new TypeInfo();
    info.typeName = PROGRESS_LANG_OBJECT;
    info.getMethods().add(new MethodElement("Clone", EnumSet.of(AccessType.PUBLIC), 0, DataType.CLASS.getNum(),
        PROGRESS_LANG_OBJECT, 0, EMPTY_PARAMETERS));
    info.getMethods().add(new MethodElement("Equals", EnumSet.of(AccessType.PUBLIC), 0, DataType.LOGICAL.getNum(), "",
        0, new MethodParameter[] {
            new MethodParameter(0, "otherObj", 1, 1, 1, DataType.CLASS.getNum(), PROGRESS_LANG_OBJECT, 0)}));
    info.getMethods().add(new MethodElement("GetClass", EnumSet.of(AccessType.PUBLIC), 0, DataType.CLASS.getNum(),
        "Progress.Lang.Class", 0, EMPTY_PARAMETERS));
    info.getMethods().add(new MethodElement("ToString", EnumSet.of(AccessType.PUBLIC), 0, DataType.CHARACTER.getNum(),
        "", 0, EMPTY_PARAMETERS));

    return info;
  }
}
