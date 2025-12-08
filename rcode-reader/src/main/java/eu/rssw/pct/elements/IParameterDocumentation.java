package eu.rssw.pct.elements;

public interface IParameterDocumentation extends IElementDocumentation {
  boolean isOptional();

  DataType getDataType();

}
