/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2024 Riverside Software
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
package org.sonar.plugins.openedge.sensor;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.progress.xref.InvalidXMLFilterStream;

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
      Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input2);
      assertNotNull(doc.getFirstChild());
      assertTrue(doc.getFirstChild().getChildNodes().getLength() > 2);
      Node node = doc.getFirstChild().getChildNodes().item(1);
      assertEquals(node.getAttributes().getNamedItem("File-name").getNodeValue(), "src\\procedures\\ sample\\test7.p");
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
