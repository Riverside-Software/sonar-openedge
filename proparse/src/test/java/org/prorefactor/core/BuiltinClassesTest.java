package org.prorefactor.core;

import static org.testng.Assert.assertNotNull;

import org.prorefactor.refactor.BuiltinClasses;
import org.testng.annotations.Test;

import eu.rssw.pct.elements.IMethodElement;
import eu.rssw.pct.elements.ITypeInfo;

public class BuiltinClassesTest {

  @Test
  private void testSignatures() {
    // Assert all signatures can be fetched
    for (ITypeInfo typeInfo: BuiltinClasses.getBuiltinClasses()) {
      for (IMethodElement method : typeInfo.getMethods()) {
        assertNotNull(method.getSignature());
      }
    }
  }
}
