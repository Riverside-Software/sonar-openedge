package eu.rssw.pct.elements;

public interface ITypeInfoBuilder {
  ITypeInfoBuilder newTypeInfoBuilder(byte[] segment);
  ITypeInfoBuilder newMethod();
  ITypeInfo build();
}
