/********************************************************************************
 * Copyright (c) 2015-2026 Riverside Software
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU Lesser General Public License v3.0
 * which is available at https://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-3.0
 ********************************************************************************/
package com.progress.xref;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.List;

import org.testng.annotations.Test;

import com.progress.xref.CrossReference.Source;

public class CrossReferenceTest {

  @Test
  public void test00() {
    var xref = CrossReferenceUtils.parseXREF(Path.of("src/test/resources/xref/notAvailable.xref"));
    assertThat(xref).isNotNull().returns(List.of(), it -> it.getSource());
  }

  @Test
  public void test01() {
    // Same source file, plain text and XML XREF
    var xref = CrossReferenceUtils.parseXREF(Path.of("src/test/resources/xref/xref01.xml"));
    var src0 = assertThat(xref).isNotNull() //
      .returns(1, it -> it.getSource().size()) //
      .extracting(it -> it.getSource().get(0)) //
      .actual();
    var refList = assertThat(src0).extracting(Source::getReference).actual();
    assertThat(refList).hasSize(597);
    assertThat(refList).filteredOn(it -> it.getRefType() == ReferenceType.STRING).isEmpty();

    var txtXref = CrossReferenceUtils.parseXREF(Path.of("src/test/resources/xref/xref01.txt"));
    var txtSrc0 = assertThat(txtXref).isNotNull() //
      .returns(2, it -> it.getSource().size()) // Yes, plain text xref is broken...
      .extracting(it -> it.getSource().get(0)) //
      .actual();
    var refList2 = assertThat(txtSrc0).extracting(Source::getReference).actual();
    assertThat(refList2).hasSize(597);
    assertThat(refList2).filteredOn(it -> it.getRefType() == ReferenceType.STRING).isEmpty();
    assertThat(refList2).filteredOn(it -> it.getReferenceType() == null).isEmpty();
    assertThat(refList2).filteredOn(it -> it.getReferenceType() == "UNKNOWN").isEmpty();
    assertThat(refList2).filteredOn(it -> it.getReferenceType() == "INVOKE").isNotEmpty();
  }

  @Test
  public void test02() {
    // Same source file, plain text and XML XREF
    var xref = CrossReferenceUtils.parseXREF(Path.of("src/test/resources/xref/xref02.xml"));
    var src0 = assertThat(xref).isNotNull() //
      .returns(10, it -> it.getSource().size()) //
      .extracting(it -> it.getSource().get(0)) //
      .actual();
    var refList = assertThat(src0).extracting(Source::getReference).actual();
    assertThat(refList).hasSize(154);
    assertThat(refList).filteredOn(it -> it.getRefType() == ReferenceType.STRING).isEmpty();
    
    var txtXref = CrossReferenceUtils.parseXREF(Path.of("src/test/resources/xref/xref02.txt"));
    var txtSrc0 = assertThat(txtXref).isNotNull() //
      .returns(10, it -> it.getSource().size()) // Yes, plain text xref is broken...
      .extracting(it -> it.getSource().get(0)) //
      .actual();
    var refList2 = assertThat(txtSrc0).extracting(Source::getReference).actual();
    assertThat(refList2).hasSize(850); // Yes, clearly not consistent with XML XREF...
    assertThat(refList2).filteredOn(it -> it.getRefType() == ReferenceType.STRING).isEmpty();
  }

}
