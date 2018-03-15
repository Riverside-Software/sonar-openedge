package org.sonar.plugins.openedge.sensor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.testng.annotations.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class InvalidXMLFilterStreamTest {
  private static final Set<Integer> bytes = new HashSet<>();

  static {
    bytes.add(1);
    bytes.add(2);
    bytes.add(4);
  }

  @Test
  public void testValidFile() throws IOException {
    InputStream input = new FileInputStream(new File("src/test/resources/file1.xml"));
    InvalidXMLFilterStream input2 = new InvalidXMLFilterStream(bytes, input);

    try {
      DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input2);
    } catch (ParserConfigurationException | SAXException uncaught) {
      throw new RuntimeException(uncaught);
    }
  }

  @Test
  public void testInvalidFile() throws IOException {
    InputStream input = new FileInputStream(new File("src/test/resources/file2.xml"));
    InvalidXMLFilterStream input2 = new InvalidXMLFilterStream(bytes, input);

    try {
      DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input2);
    } catch (ParserConfigurationException | SAXException uncaught) {
      throw new RuntimeException(uncaught);
    }
  }

  @Test(expectedExceptions = SAXParseException.class)
  public void testInvalidFile2() throws IOException, SAXException {
    try (InputStream input = new FileInputStream(new File("src/test/resources/file2.xml"))) {
      DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
    } catch (ParserConfigurationException uncaught) {
      throw new RuntimeException(uncaught);
    }
  }

}
