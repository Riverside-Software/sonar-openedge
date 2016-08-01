/*
 * OpenEdge plugin for SonarQube
 * Copyright (C) 2016 Riverside Software
 * contact AT riverside DASH software DOT fr
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.openedge.colorizer;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.sonar.channel.CodeReader;
import org.sonar.colorizer.HtmlCodeBuilder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("deprecation")
public class MultilinesAndNestedDocTokenizerTest {

  private HtmlCodeBuilder codeBuilder;

  @BeforeMethod
  public void init() {
    codeBuilder = new HtmlCodeBuilder();
  }

  @Test
  public void testStandardComment() {
    MultilinesAndNestedDocTokenizer tokenizer = new MultiLinesAndNestedDocTokenizerImpl("{[||", "");

    assertTrue(
        tokenizer.hasNextToken(new CodeReader("{[|| And here is strange  multi-line comment"), new HtmlCodeBuilder()));
    assertFalse(
        tokenizer.hasNextToken(new CodeReader("// this is not a strange multi-line comment"), new HtmlCodeBuilder()));
  }

  @Test
  public void testLongStartToken() {
    MultilinesAndNestedDocTokenizer tokenizer = new MultiLinesAndNestedDocTokenizerImpl("/***", "**/");
    assertTrue(tokenizer.consume(new CodeReader("/*** multi-line comment**/ private part"), codeBuilder));
    assertEquals("/*** multi-line comment**/", codeBuilder.toString());
  }

  @Test
  public void testStartTokenEndTokenOverlapping() {
    MultilinesAndNestedDocTokenizer tokenizer = new MultiLinesAndNestedDocTokenizerImpl("/*", "*/");
    assertTrue(tokenizer.consume(new CodeReader("/*// multi-line comment*/ private part"), codeBuilder));
    assertEquals("/*// multi-line comment*/", codeBuilder.toString());
  }

  @Test
  public void testNestedComment() {
    CodeReader reader = new CodeReader("/* Single line /* ABC /* DEF */ */ */ private part");
    MultilinesAndNestedDocTokenizer tokenizer = new MultiLinesAndNestedDocTokenizerImpl("/*", "*/");
    assertTrue(tokenizer.consume(reader, codeBuilder));
    assertEquals("/* Single line /* ABC /* DEF */ */ */", codeBuilder.toString());
  }

  @Test
  public void testNestedComment2() {
    CodeReader reader = new CodeReader("/* Single line /* ABC /* DEF */*/*/ private part");
    MultilinesAndNestedDocTokenizer tokenizer = new MultiLinesAndNestedDocTokenizerImpl("/*", "*/");
    assertTrue(tokenizer.consume(reader, codeBuilder));
    assertEquals("/* Single line /* ABC /* DEF */*/*/", codeBuilder.toString());
  }

  @Test
  public void testNestedComment3() {
    CodeReader reader = new CodeReader("/* Single line\n/* ABC\n/* DEF */\n*/\n*/ private part");
    MultilinesAndNestedDocTokenizer tokenizer = new MultiLinesAndNestedDocTokenizerImpl("/*", "*/");
    assertTrue(tokenizer.consume(reader, codeBuilder));
    assertEquals("/* Single line", codeBuilder.toString());
    reader.pop();
    assertTrue(tokenizer.consume(reader, codeBuilder));
    assertEquals("/* Single line/* ABC", codeBuilder.toString());
    reader.pop();
    assertTrue(tokenizer.consume(reader, codeBuilder));
    assertEquals("/* Single line/* ABC/* DEF */", codeBuilder.toString());
    reader.pop();
    assertTrue(tokenizer.consume(reader, codeBuilder));
    assertEquals("/* Single line/* ABC/* DEF */*/", codeBuilder.toString());
    reader.pop();
    assertTrue(tokenizer.consume(reader, codeBuilder));
    assertEquals("/* Single line/* ABC/* DEF */*/*/", codeBuilder.toString());
  }

  @Test
  public void testNestedComment4() {
    CodeReader reader = new CodeReader("/* Single line\n/* ABC\n/* DEF */ */ */ private part");
    MultilinesAndNestedDocTokenizer tokenizer = new MultiLinesAndNestedDocTokenizerImpl("/*", "*/");
    assertTrue(tokenizer.consume(reader, codeBuilder));
    assertEquals("/* Single line", codeBuilder.toString());
    reader.pop();
    assertTrue(tokenizer.consume(reader, codeBuilder));
    assertEquals("/* Single line/* ABC", codeBuilder.toString());
    reader.pop();
    assertTrue(tokenizer.consume(reader, codeBuilder));
    assertEquals("/* Single line/* ABC/* DEF */ */ */", codeBuilder.toString());
  }

  @Test
  public void testMultilinesComment1() {
    CodeReader reader = new CodeReader("/* multi-line \n comment */ private part");
    MultilinesAndNestedDocTokenizer tokenizer = new MultiLinesAndNestedDocTokenizerImpl("/*", "*/");
    assertTrue(tokenizer.consume(reader, codeBuilder));
    assertEquals("/* multi-line ", codeBuilder.toString());
    reader.pop();
    assertTrue(tokenizer.consume(reader, codeBuilder));
    assertEquals("/* multi-line  comment */", codeBuilder.toString());
  }

  @Test
  public void testMultilinesComment2() {
    CodeReader reader = new CodeReader("/* multi-line /* ABC /*DEF*/*/\n comment */ private part");
    MultilinesAndNestedDocTokenizer tokenizer = new MultiLinesAndNestedDocTokenizerImpl("/*", "*/");
    assertTrue(tokenizer.consume(reader, codeBuilder));
    assertEquals("/* multi-line /* ABC /*DEF*/*/", codeBuilder.toString());
    reader.pop();
    assertTrue(tokenizer.consume(reader, codeBuilder));
    assertEquals("/* multi-line /* ABC /*DEF*/*/ comment */", codeBuilder.toString());
  }

  @Test
  public void testMultilinesComment3() {
    CodeReader reader = new CodeReader("/* multi-line /* ABC /*DEF*\n*/*/*/ private part");
    MultilinesAndNestedDocTokenizer tokenizer = new MultiLinesAndNestedDocTokenizerImpl("/*", "*/");
    assertTrue(tokenizer.consume(reader, codeBuilder));
    assertEquals("/* multi-line /* ABC /*DEF*", codeBuilder.toString());
    reader.pop();
    assertTrue(tokenizer.consume(reader, codeBuilder));
    assertEquals("/* multi-line /* ABC /*DEF**/*/*/", codeBuilder.toString());
  }

  public class MultiLinesAndNestedDocTokenizerImpl extends MultilinesAndNestedDocTokenizer {
    public MultiLinesAndNestedDocTokenizerImpl(String startToken, String endToken) {
      super(startToken, endToken, "", "");
    }
  }

}