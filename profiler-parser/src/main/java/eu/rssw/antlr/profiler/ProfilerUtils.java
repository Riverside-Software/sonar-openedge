/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2018 Riverside Software
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
package eu.rssw.antlr.profiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public final class ProfilerUtils {

  private ProfilerUtils() {
    // No-op
  }

  public static final ProfilerSession getProfilerSession(File file) throws IOException {
    return getProfilerSession(new FileInputStream(file));
  }

  public static final ProfilerSession getProfilerSession(InputStream input) throws IOException {
    ProfilerGrammarLexer lexer = new ProfilerGrammarLexer(CharStreams.fromStream(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    ProfilerGrammarParser parser = new ProfilerGrammarParser(tokens);
    ParseTree tree = parser.profiler();

    ProfilerSessionVisitor visitor = new ProfilerSessionVisitor();
    visitor.visit(tree);

    return visitor.getSession();
  }

  public static final void dumpCoverageAsXml(CoverageSession session, Collection<File> propath, File xmlFile) throws IOException {
    try {
      Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

      // Root element
      Element root = document.createElement("coverage");
      root.setAttribute("version", "1");
      document.appendChild(root);

      for (FileCoverage profiledFile : session.getFiles()) {
        Element fileElement = document.createElement("file");
        fileElement.setAttribute("path", getFilePath(propath, profiledFile.getFileName()));
        root.appendChild(fileElement);

        for (Integer lineNumber : profiledFile.getLinesToCover()) {
          Element lineElement = document.createElement("lineToCover");
          lineElement.setAttribute("lineNumber", lineNumber.toString());
          lineElement.setAttribute("covered", Boolean.toString(profiledFile.getCoveredLines().contains(lineNumber)));

          fileElement.appendChild(lineElement);
        }
      }

      TransformerFactory factory = TransformerFactory.newInstance();
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

      Transformer transformer = factory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(OutputKeys.METHOD, "xml");
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
      transformer.transform(new DOMSource(document), new StreamResult(new FileOutputStream(xmlFile)));
    } catch (ParserConfigurationException | TransformerException caught) {
      throw new IOException(caught);
    }
  }

  /**
   * Returns absolute file name if found in work directory or in propath
   */
  private static final String getFilePath(Collection<File> propath, String fileName) {
    if (new File(fileName).exists())
      return fileName;

    for (File file : propath) {
      File stdName = new File(file, fileName);
      File clsName = new File(file, fileName.replace('.', '/') + ".cls");
      if (stdName.exists())
        return stdName.getAbsolutePath();
      if (clsName.exists())
        return clsName.getAbsolutePath();
    }

    return fileName;
  }
}
