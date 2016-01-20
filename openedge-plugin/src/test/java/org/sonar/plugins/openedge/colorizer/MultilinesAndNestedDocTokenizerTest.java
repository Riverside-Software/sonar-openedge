package org.sonar.plugins.openedge.colorizer;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sonar.channel.CodeReader;
import org.sonar.colorizer.HtmlCodeBuilder;

@SuppressWarnings("deprecation")
public class MultilinesAndNestedDocTokenizerTest {

  private HtmlCodeBuilder codeBuilder;

  @Before
  public void init() {
    codeBuilder = new HtmlCodeBuilder();
  }

  @Test
  public void testStandardComment() {
    MultilinesAndNestedDocTokenizer tokenizer = new MultiLinesAndNestedDocTokenizerImpl("{[||", "");
    assertThat(tokenizer.hasNextToken(new CodeReader("{[|| And here is strange  multi-line comment"), new HtmlCodeBuilder()), is(true));
    assertThat(tokenizer.hasNextToken(new CodeReader("// this is not a strange multi-line comment"), new HtmlCodeBuilder()), is(false));
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