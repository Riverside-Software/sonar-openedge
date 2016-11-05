package org.sonar.plugins.openedge.sensor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.sonar.plugins.openedge.sensor.OpenEdgeXREFSensor.InvalidXMLFilterStream;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class InvalidXMLFilterStreamTest {

  @Test
  public void testValidFile() throws IOException {
    InputStream input = new FileInputStream(new File("src/test/resources/file1.xml"));
    InvalidXMLFilterStream input2 = new InvalidXMLFilterStream(input);

    try {
      DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input2);
    } catch (ParserConfigurationException | SAXException uncaught) {
      throw new RuntimeException(uncaught);
    }
  }

  @Test
  public void testInvalidFile() throws IOException {
    InputStream input = new FileInputStream(new File("src/test/resources/file2.xml"));
    InvalidXMLFilterStream input2 = new InvalidXMLFilterStream(input);

    try {
      DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input2);
    } catch (ParserConfigurationException | SAXException uncaught) {
      throw new RuntimeException(uncaught);
    }
  }

  @Test(expectedExceptions = SAXParseException.class)
  public void testInvalidFile2() throws IOException, SAXException {
    InputStream input = new FileInputStream(new File("src/test/resources/file2.xml"));

    try {
      DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
    } catch (ParserConfigurationException uncaught) {
      throw new RuntimeException(uncaught);
    }
  }

}
